package com.gleb.pycrunch;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import org.json.JSONArray;
import org.json.JSONException;

import com.gleb.pycrunch.actions.TogglePinnedTestsAction;
import com.gleb.pycrunch.actions.UpdateModeAction;
import com.gleb.pycrunch.activation.ActivationValidation;
import com.gleb.pycrunch.activation.MyStateService;
import com.gleb.pycrunch.debugging.PyRemoteDebugState;
import com.gleb.pycrunch.exceptionPreview.CapturedException;
import com.gleb.pycrunch.messaging.PycrunchBusNotifier;
import com.gleb.pycrunch.messaging.PycrunchWatchdogBusNotifier;
import com.gleb.pycrunch.shared.GlobalKeys;
import com.gleb.pycrunch.shared.IdeNotifications;
import com.gleb.pycrunch.shared.MyPasswordStore;
import com.intellij.ide.ActivityTracker;
import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBus;
import org.json.JSONObject;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PycrunchConnector {
    private static int CounterOfSingletons = 0;
    //    This is to mimic nzkava socketio library
    private final ConnectionState _connectionState = new ConnectionState();
    private final MyStateService _persistentState;
    // Sets the maximum allowed number of opened projects.
    public  Project _project;
    private ConcurrentHashMap<String, TestRunResult> _results = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, PycrunchTestMetadata> _tests = new ConcurrentHashMap<>();

    private MessageBus _bus;
    private String api_uri = "http://127.0.0.1";
    private int _port;
    private PycrunchCombinedCoverage _combined_coverage;
    private boolean _upgradeNoticeAlreadyShownInCurrentSession;
    public boolean _canTerminateTestRun;

    public PycrunchConnector() {
        PycrunchConnector.CounterOfSingletons++;
        _persistentState = ApplicationManager.getApplication().getService(MyStateService.class);
    }

    private Socket _socket;

    private Emitter.Listener onNewMessage = args -> didReceiveSocketEvent(args);
    private Emitter.Listener onSocketClose = args -> socketDidDisconnect(args);
    private Emitter.Listener onSocketConnect = args -> socketDidConnect(args);
    private Emitter.Listener onSocketConnectError = args -> socketWillHaveConnectError(args);

    private void socketDidDisconnect(Object... args) {
        engineWillDisconnect();
    }

    private void socketWillHaveConnectError(Object... args) {
        System.out.println("socketWillHaveConnectError - connection failed " + _connectionState.current_retry + " of " + _connectionState.max_retries);
        _connectionState.connectionFailed();
        socketDidHaveConnectError();
    }


    private void socketDidConnect(Object... args) {
        engineWillConnect();
        try {
            _connectionState.reset();
            post_version_info();
            post_discovery_command();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void post_version_info() throws JSONException {
        PluginId pluginId = PluginId.getId("com.pycrunch.intellijconnector");
        var pluginDescriptor = PluginManagerCore.getPlugin(pluginId);
        String pluginVersion = pluginDescriptor != null ? pluginDescriptor.getVersion() : "unknown";

        JSONObject obj = new JSONObject();
        obj.put("action", "plugin_version");
        obj.put("plugin_version", pluginVersion);

        this._socket.emit("my event", obj);

    }

    private void ApplyCombinedCoverage(JSONObject data) throws JSONException {
        _combined_coverage = PycrunchCombinedCoverage.from_json(data);

        combinedCoverageDidUpdate();
    }

    private void engineDidLoadMode(String new_mode) {
        EventQueue.invokeLater(() -> {
            ((PycrunchBusNotifier) this._bus.syncPublisher(PycrunchBusNotifier.CHANGE_ACTION_TOPIC)).engineDidLoadMode(new_mode);
        });
    }

    private void engineDidLoadVersion(int version_major, int version_minor, int version_patch) {
        EventQueue.invokeLater(() -> {
            ((PycrunchBusNotifier) this._bus.syncPublisher(PycrunchBusNotifier.CHANGE_ACTION_TOPIC)).engineDidLoadVersion(version_major, version_minor, version_patch);
        });
    }

    private void ApplyInitialConnectionProps(JSONObject data)throws JSONException {
        JSONObject version = data.getJSONObject("version");
        if (version == null) {
            // pre-v1
            return;
        }
        int version_major = version.getInt("major");
        int version_minor = version.getInt("minor");
        int version_patch = 0;
        if (version.has("patch")) {
//            patch field added only in 1.5.x,
            version_patch = version.getInt("patch");
        }


        String mode = data.getString("engine_mode");

        this.engineDidLoadMode(mode);
        this.showUpgradeNoticeIfEngineOutdated(version_major, version_minor, version_patch);
        this.engineDidLoadVersion(version_major, version_minor, version_patch);
    }

    private void showUpgradeNoticeIfEngineOutdated(int major, int minor, int version_patch) {
//        last known version at the moment of writing is 1.3
        if (_upgradeNoticeAlreadyShownInCurrentSession) {
            return;
        }
        boolean should_show_warning_now = false;
        boolean reallyOld = major < 1;
        boolean minorVersionIsOld = major == 1 && minor < 6;
        if (reallyOld || minorVersionIsOld) {
            should_show_warning_now = true;
        }

        if (major == 1 && minor == 5) {
            if (version_patch < 1) {
                should_show_warning_now = true;
            }
        }

        if (should_show_warning_now) {
            IdeNotifications.notify(_project,"New pycrunch-engine version is available!", "To install updated engine, please run \n\n pip install --upgrade pycrunch-engine\n\n ", NotificationType.WARNING);
            _upgradeNoticeAlreadyShownInCurrentSession = true;

        }
    }


    public void AttachToEngine(Project project) throws Exception {
        _project = project;

        _bus = project.getMessageBus();
        invalidateLicenseStateAndNotifyUI();
        Object pycrunch_port = project.getUserData(GlobalKeys.PORT_KEY);
        if (pycrunch_port == null) {
            System.out.println("No port specified");
            return;
        }
        _port = (int) pycrunch_port;

        try {
            int MAX_RETRIES = 30;

            if (_socket != null) {
                _socket.off();
                _socket.disconnect();
            }
            _connectionState.current_retry = 0;
            _connectionState.max_retries = MAX_RETRIES;
            IO.Options options = new IO.Options();
            options.reconnection = true;
            options.forceNew = true;

//            In milliseconds
            options.reconnectionDelay = 1000;
            options.reconnectionDelayMax = 3000;
            options.reconnectionAttempts = MAX_RETRIES;

            _socket = IO.socket(full_api_url(), options);
            _socket.on("event", onNewMessage)
                    .on(Socket.EVENT_DISCONNECT, onSocketClose)
                    .on(Socket.EVENT_CONNECT, onSocketConnect)
                    .on(Socket.EVENT_CONNECT_ERROR, onSocketConnectError)
            ;

            _socket.connect();
        } catch (URISyntaxException e) {
            System.out.println("error" + e.toString());
        }

//        post_discovery_command();
    }

    public String GetCapturedOutput(String fqn) {
        TestRunResult runResult = _results.getOrDefault(fqn, null);
        if (runResult == null) {
            return "Test `" +  fqn + "` has not run yet";
        }
        return runResult.captured_output;
    }

    private String full_api_url() {
        return api_uri + ':' + _port;
    }
    public void TerminateRun() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("action", "watchdog-terminate");
        this._socket.emit("my event", obj);
    }

    public void RunTests(List<PycrunchTestMetadata> tests) throws JSONException {
        this.DebugOrRunTests(tests, "run-tests", 0);
    }

    public void DebugTests(List<PycrunchTestMetadata> tests) throws JSONException {
        PyRemoteDebugState debugState = _project.getService(com.gleb.pycrunch.debugging.PyRemoteDebugState.class);

        debugState.build_configuration_and_run_debugger(_project);
        int userData = (int)_project.getUserData(GlobalKeys.REMOTE_DEBUG_PORT_KEY);
        this.DebugOrRunTests(tests, "debug-tests", userData);
    }

    private void DebugOrRunTests(List<PycrunchTestMetadata> tests, String action, int port) throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("action", action);

        JSONArray payload = new JSONArray();
        for (PycrunchTestMetadata __ : tests) {
            payload.put(__.to_json());
        }
        if (action.equals("debug-tests")) {
            obj.put("debugger_port", port);
        }

        obj.put("tests", payload);
        this._socket.emit("my event", obj);
    }

    private void ApplyTestRunResults(JSONObject data) throws JSONException {
        JSONObject cov = data.getJSONObject("coverage");
        JSONObject all_runs = cov.getJSONObject("all_runs");
        JSONArray keys = all_runs.names();
        if (keys == null) {
            System.out.println("cannot ApplyTestRunResults, keys are null");
            return;
        }
        for (int i = 0; i < keys.length(); ++i) {
            String fqn = keys.getString(i);
            JSONObject value = all_runs.getJSONObject(fqn);
            _results.put(fqn, TestRunResult.from_json(value));
        }
        queueMessageBusEvent();
    }
    private void queueMessageBusEvent() {
        EventQueue.invokeLater(() -> {
            if (this._bus.isDisposed()) {
                return;
            }

            ((PycrunchBusNotifier) this._bus.syncPublisher(PycrunchBusNotifier.CHANGE_ACTION_TOPIC)).beforeAction("---");
        });
    }

    private void combinedCoverageDidUpdate() {
        EventQueue.invokeLater(() -> {
            if (this._bus.isDisposed()) {
                return;
            }

            ((PycrunchBusNotifier) this._bus.syncPublisher(PycrunchBusNotifier.CHANGE_ACTION_TOPIC)).combinedCoverageDidUpdate("---");
        });
    }

    private void watchdogWillBegin() {
        EventQueue.invokeLater(() -> {
            if (this._bus.isDisposed()) {
                return;
            }

            ((PycrunchBusNotifier) this._bus.syncPublisher(PycrunchBusNotifier.CHANGE_ACTION_TOPIC)).combinedCoverageDidUpdate("---");
        });
    }

    private void engineWillConnect() {
        EventQueue.invokeLater(() -> {
            if (this._bus.isDisposed()) {
                return;
            }

            ((PycrunchBusNotifier) this._bus.syncPublisher(PycrunchBusNotifier.CHANGE_ACTION_TOPIC)).engineDidConnect(full_api_url());
        });
    }

    private void socketDidHaveConnectError() {
        EventQueue.invokeLater(() -> {
            if (this._bus.isDisposed()) {
                return;
            }

            this._bus.syncPublisher(PycrunchBusNotifier.CHANGE_ACTION_TOPIC).engineDidFailToReconnect(_connectionState);
        });
    }


    private void engineWillDisconnect() {
        EventQueue.invokeLater(() -> {
            if (this._bus.isDisposed()) {
                return;
            }

            ((PycrunchBusNotifier) this._bus.syncPublisher(PycrunchBusNotifier.CHANGE_ACTION_TOPIC)).engineDidDisconnect("---");
        });
    }

    private void licenceActivated() {
        EventQueue.invokeLater(() -> {
            ((PycrunchBusNotifier) this._bus.syncPublisher(PycrunchBusNotifier.CHANGE_ACTION_TOPIC)).licenceActivated();
        });
    }

    private void licenceInvalid() {
        EventQueue.invokeLater(() -> {
            ((PycrunchBusNotifier) this._bus.syncPublisher(PycrunchBusNotifier.CHANGE_ACTION_TOPIC)).licenceInvalid();
        });
    }

    private void ApplyTestDiscoveryResults(JSONObject data) throws JSONException {
        HashSet<String> actual_tests = new HashSet<>();

        JSONArray tests = data.getJSONArray("tests");
        for (int i=0; i < tests.length(); i++) {
            JSONObject x = tests.getJSONObject(i);
            PycrunchTestMetadata testMetadata = PycrunchTestMetadata.from_json(x);
            actual_tests.add(testMetadata.fqn);
            _tests.put(testMetadata.fqn, testMetadata);
        }
        for (String fqn : new HashSet<String>(_tests.keySet())) {
            if (!actual_tests.contains(fqn)) {
                discard_outdated_test(fqn);
            }
        }

        queueMessageBusEvent();
        combinedCoverageDidUpdate();
    }

    private void discard_outdated_test(String fqn) {
        _tests.remove(fqn);
        _results.remove(fqn);
    }

    private void post_discovery_command() throws IOException, JSONException {
        JSONObject obj = new JSONObject();
        obj.put("action", "discovery");
        this._socket.emit("my event", obj);
    }


    public static String convertStreamToString(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder(2048); // Define a size if you have an idea of it.
        char[] read = new char[128]; // Your buffer size.
        try (InputStreamReader ir = new InputStreamReader(is, StandardCharsets.UTF_8)) {
            for (int i; -1 != (i = ir.read(read)); sb.append(read, 0, i));
        }
        return sb.toString();
    }
    public String GetModuleStatus(String module_name) {
        String last_known_status = null;
        for (PycrunchTestMetadata test : _tests.values()) {
            if (!test.module.equals(module_name)) {
                continue;
            }

            if (test.state.equals("queued")) {
                last_known_status = "queued";
                break;
            }


            if (test.state.equals("failed")) {
                last_known_status = "failed";
                break;
            }

            if (test.state.equals("pending")) {
                last_known_status = "pending";
            }

            if (test.state.equals("success")) {
                last_known_status = "success";
            }

        }

        return last_known_status;
    }

    public Collection<PycrunchTestMetadata> GetTestsSorted() {
        if (_tests == null) {
            return new ArrayList<PycrunchTestMetadata>();
        }
        Map<String, PycrunchTestMetadata> map = new TreeMap<>(_tests);
        return map.values();
    }


    public SingleFileCombinedCoverage GetCoveredLinesForFile(String absolute_path) {
        if (_combined_coverage == null) {
            return null;
        }

        return _combined_coverage.GetSingleFileCoverage(absolute_path);
    }


    public ConcurrentHashMap<String, TestRunResult> get_test_run_results(){
        return _results;
    }

    /*
      Returns the exception for the given test, or null if there is no exception
     */
    public CapturedException getExceptionFor(String fqn){
        TestRunResult testRunResult = _results.get(fqn);
        if (testRunResult == null){
            return null;
        }
        if (testRunResult.captured_exception == null){
            return null;
        }

        return testRunResult.captured_exception;
    }
    public MessageBus GetMessageBus() {
        return _bus;
    }

    public String get_marker_color_for(String absolute_path, Integer line_number, HashSet<Integer> exceptions_in_current_file) {
        SingleFileCombinedCoverage singleFileCombinedCoverage = _combined_coverage.GetSingleFileCoverage(absolute_path);
        if (singleFileCombinedCoverage == null) {
            System.out.println("get_marker_color_for " + absolute_path + " returned null");
            return "queued";
        }

        HashSet<String> tests_at_line = singleFileCombinedCoverage.TestsAtLine(line_number);
        if (tests_at_line.isEmpty()) {
            if (exceptions_in_current_file.contains(line_number)) {
                return "exception";
            }
        }

//      1.  First, check if there are any non-success tests at this line, and return early
        for (String fqn: tests_at_line) {
            String status = GetTestStatus(fqn);
            if (status.equals("pending")) {
//                Pending?? This is when the test ran, but connector invalidates statuses.
//                Or, web-ui connected and tests are rediscovered
                return "pending";
            }
            if (status.equals("failed")) {
                if (exceptions_in_current_file.contains(line_number)) {
                    return "exception";
                }
                return "failed";

            }
            if (status.equals("queued")) {
                return "queued";
            }
        }
//       2. All tests are green, so return success
        return "success";
    }


    public PycrunchTestMetadata FindTestByFqn(String fqn) {
        return _tests.getOrDefault(fqn, null);
    }


    public String GetTestStatus(String fqn) {
        PycrunchTestMetadata found = _tests.getOrDefault(fqn, null);
        if (found == null) {
            return "no__test__found";
        }
        return found.state;
    }
    private void didReceiveSocketEvent(Object... args) {
        JSONObject data = (JSONObject) args[args.length - 1];

        try {
            String evt = data.getString("event_type");
            switch (evt) {
                case "connected":
                    ApplyInitialConnectionProps(data);
                    break;
                case "discovery_did_become_available":
                    ApplyTestDiscoveryResults(data);
                    break;
                case "test_run_completed":
                    ApplyTestRunResults(data);
                    break;
                case "combined_coverage_updated":
                    ApplyCombinedCoverage(data);
                    break;
                case "watchdog_begin":
                    WatchdogBegin(data);
                    break;
                case "watchdog_end":
                    WatchdogEnd(data);
                    break;
            }
        } catch (JSONException e) {
            System.out.println(e.toString());
        }
    }

    private void WatchdogEnd(JSONObject data) {
        _canTerminateTestRun = false;
        emit_syntetic_event();
        EventQueue.invokeLater(() -> {
            emit_syntetic_event();
            this._bus.syncPublisher(PycrunchWatchdogBusNotifier.CHANGE_ACTION_TOPIC).watchdogEnd();
        });
    }

    private void WatchdogBegin(JSONObject data) throws JSONException {
        int totalTests = data.getInt("total_tests");
        _canTerminateTestRun = true;
        emit_syntetic_event();
        EventQueue.invokeLater(() -> {
            emit_syntetic_event();
            this._bus.syncPublisher(PycrunchWatchdogBusNotifier.CHANGE_ACTION_TOPIC).watchdogBegin(totalTests);
        });
    }

    private void emit_syntetic_event() {
        ActivityTracker.getInstance().inc();
    }

    public void pin_tests(List<PycrunchTestMetadata> tests) throws JSONException {
       new TogglePinnedTestsAction(this._socket).pin_tests(tests);
    }

    public void unpin_tests(List<PycrunchTestMetadata> tests) throws JSONException {
        new TogglePinnedTestsAction(this._socket).unpin_tests(tests);
    }

    public void update_mode(String mode) {
        try {
            new UpdateModeAction(this._socket).run(mode);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public boolean invalidateLicenseStateAndNotifyUI() {
        ActivationValidation activationValidation = new ActivationValidation();
        activationValidation.ping_user_id(_persistentState);
        licenceActivated();
        return true;

//        ActivationValidation activationValidation = new ActivationValidation();
//        activationValidation.try_renew(_project, _persistentState);
//        boolean valid_licence = activationValidation.is_valid_licence(_persistentState);
//
//        if (valid_licence) {
//            licenceActivated();
//        } else {
//            licenceInvalid();
//        }
//
//        return valid_licence;
    }


    public void remove_license() {
        _persistentState.ActivationData = null;
        _persistentState.Sig = null;
//        this will allow trial to be persistent across accounts
//        _persistentState.Exp = null;
//        _persistentState.ExpSig = null;
        MyPasswordStore.clearCredentials();
        invalidateLicenseStateAndNotifyUI();
    }

    public JSONArray GetVariablesState(String fqn) {
        TestRunResult pycrunchTestMetadata = _results.get(fqn);
        if (pycrunchTestMetadata != null) {
            return pycrunchTestMetadata.variables_state;
        }
        return new JSONArray();
    }

    public Set<Integer> get_captured_exception_lines_for_file(String absolutePath) {
        Set<Integer> exception_lines = new HashSet<>();
        for (TestRunResult testRunResult : _results.values()) {
            if (testRunResult.captured_exception != null) {
                CapturedException capturedException = testRunResult.captured_exception;
                if (capturedException.filename.equals(absolutePath)) {
                    exception_lines.add(testRunResult.captured_exception.line_number);
                }
            }
        }
        return exception_lines;
    }
}