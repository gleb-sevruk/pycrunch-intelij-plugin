package com.gleb.pycrunch;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.gleb.pycrunch.actions.TogglePinnedTestsAction;
import com.gleb.pycrunch.actions.UpdateModeAction;
import com.gleb.pycrunch.activation.ActivationValidation;
import com.gleb.pycrunch.activation.MyStateService;
import com.gleb.pycrunch.shared.GlobalKeys;
import com.gleb.pycrunch.shared.MyPasswordStore;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.awt.*;
import java.io.*;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class PycrunchConnector {
    private static int CounterOfSingletons = 0;
    private final MyStateService _persistentState;
    // Sets the maximum allowed number of opened projects.
    public  Project _project;
    private ConcurrentHashMap<String, TestRunResult> _results = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, PycrunchTestMetadata> _tests = new ConcurrentHashMap<>();

    private MessageBus _bus;
    private String api_uri = "http://127.0.0.1";
    private int _port;
    private PycrunchCombinedCoverage _combined_coverage;

    public PycrunchConnector() {
        PycrunchConnector.CounterOfSingletons++;
        _persistentState = ServiceManager.getService(MyStateService.class);
    }

    private Socket _socket;

    private Emitter.Listener onNewMessage = args -> didReceiveSocketEvent(args);
    private Emitter.Listener onSocketClose = args -> socketDidDisconnect(args);
    private Emitter.Listener onSocketConnect = args -> socketDidConnect(args);

    private void socketDidDisconnect(Object... args) {
        engineWillDisconnect();
    }

    private void socketDidConnect(Object... args) {
        engineWillConnect();
        try {
            post_discovery_command();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void ApplyCombinedCoverage(JSONObject data) throws JSONException {
        _combined_coverage = PycrunchCombinedCoverage.from_json(data);

        combinedCoverageDidUpdate();
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
            if (_socket != null) {
                _socket.off();
                _socket.disconnect();
            }
            _socket = IO.socket(full_api_url());
            _socket.on("event", onNewMessage)
                    .on(Socket.EVENT_DISCONNECT, onSocketClose)
                    .on(Socket.EVENT_CONNECT, onSocketConnect);

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

    public void RunTests(List<PycrunchTestMetadata> tests) throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("action", "run-tests");

        JSONArray payload = new JSONArray();
        for (PycrunchTestMetadata __ : tests) {
            payload.put(__.to_json());
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
            ((PycrunchBusNotifier) this._bus.syncPublisher(PycrunchBusNotifier.CHANGE_ACTION_TOPIC)).beforeAction("---");
        });
    }

    private void combinedCoverageDidUpdate() {
        EventQueue.invokeLater(() -> {
            ((PycrunchBusNotifier) this._bus.syncPublisher(PycrunchBusNotifier.CHANGE_ACTION_TOPIC)).combinedCoverageDidUpdate("---");
        });
    }

    private void engineWillConnect() {
        EventQueue.invokeLater(() -> {
            ((PycrunchBusNotifier) this._bus.syncPublisher(PycrunchBusNotifier.CHANGE_ACTION_TOPIC)).engineDidConnect(full_api_url());
        });
    }
    private void engineWillDisconnect() {
        EventQueue.invokeLater(() -> {
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

        return _combined_coverage.GetLinesCovering(absolute_path);
    }


    public ConcurrentHashMap<String, TestRunResult> get_results(){
        return _results;
    }

    public MessageBus GetMessageBus() {
        return _bus;
    }

//    public String get_result_status() {
//        if (_result == null) {
//            return "unknown";
//        }
//
//        return _result.status;
//    }

    public String get_marker_color_for(String absolute_path, Integer line_number) {
        SingleFileCombinedCoverage singleFileCombinedCoverage = _combined_coverage.GetLinesCovering(absolute_path);
        if (singleFileCombinedCoverage == null) {
            System.out.println("get_marker_color_for " + absolute_path + " returned null");
            return "queued";
        }
        HashSet<String> tests_at_line = singleFileCombinedCoverage.TestsAtLine(line_number);
        for (String fqn: tests_at_line) {
            String status = GetTestStatus(fqn);
            // todo: this is piece of bullshit. need correct status icons
//            if (status.equals("pending")) {
//                return "pending";
//            }
            if (status.equals("failed")) {
                return "failed";
            }
            if (status.equals("queued")) {
                return "queued";
            }



        }

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
        String str = data.toString();
        String username;
        String message;

        try {
            String evt = data.getString("event_type");
            if (evt.equals("discovery_did_become_available")) {

                ApplyTestDiscoveryResults(data);
            }
            if (evt.equals("test_run_completed")) {

                ApplyTestRunResults(data);
            }
            if (evt.equals("combined_coverage_updated")) {

                ApplyCombinedCoverage(data);
            }
        } catch (JSONException e) {
            System.out.println(e.toString());

        }
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
        System.out.println("invoking GetVariablesState" );

        TestRunResult pycrunchTestMetadata = _results.get(fqn);
        if (pycrunchTestMetadata != null) {
            return pycrunchTestMetadata.variables_state;
        }
        return new JSONArray();
    }
}