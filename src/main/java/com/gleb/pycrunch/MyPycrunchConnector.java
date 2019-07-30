package com.gleb.pycrunch;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.gleb.pycrunch.activation.ActivationConnector;
import com.gleb.pycrunch.activation.ActivationInfo;
import com.gleb.pycrunch.activation.ActivationValidation;
import com.gleb.pycrunch.activation.MyStateService;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.util.messages.MessageBus;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.awt.*;
import java.io.*;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;

public class MyPycrunchConnector {
    private static int CounterOfSingletons = 0;
    private final MyStateService _persistentState;
    // Sets the maximum allowed number of opened projects.
    public  Project _project;
    private HashMap<String, TestRunResult> _results = new HashMap<>();
    private Set<Integer> _visited_lines;
    private MessageBus _bus;
    private String api_uri = "http://127.0.0.1:5000";
    private PycrunchCombinedCoverage _combined_coverage;

    public MyPycrunchConnector() {
        MyPycrunchConnector.CounterOfSingletons++;
        _persistentState = ServiceManager.getService(MyStateService.class);

        System.out.println("load Email - " + _persistentState.Email);
        System.out.println("load Pass - " + _persistentState.Password);
    }

    private HashMap<String, PycrunchTestMetadata> _tests = new HashMap<>();
    private Socket _socket;

    private Emitter.Listener onNewMessage = args -> didReceiveSocketEvent(args);
    private Emitter.Listener onSocketClose = args -> socketDidDisconnect(args);
    private Emitter.Listener onSocketConnect = args -> socketDidConnect(args);

    private void socketDidDisconnect(Object... args) {
        engineWillDisconnect();
    }

    private void socketDidConnect(Object... args) {
        engineWillConnect();
    }
    private void ApplyCombinedCoverage(JSONObject data) throws JSONException {
        _combined_coverage = PycrunchCombinedCoverage.from_json(data);

        combinedCoverageDidUpdate();
    }

    public void AttachToEngine(Project project) throws Exception {
        _project = project;
        _bus = project.getMessageBus();

        invalidateLicenseStateAndNotifyUI();
        try {
            _socket = IO.socket(api_uri);
            _socket.on("event", onNewMessage)
                    .on(Socket.EVENT_DISCONNECT, onSocketClose)
                    .on(Socket.EVENT_CONNECT, onSocketConnect);

            _socket.connect();
        } catch (URISyntaxException e) {}

        post_discovery_command();
    }

    public String GetCapturedOutput(String fqn) {
        TestRunResult runResult = _results.getOrDefault(fqn, null);
        if (runResult == null) {
            return "Test `" +  fqn + "` has not run yet";
        }
        return runResult.captured_output;
    }

    public void RunTests(List<PycrunchTestMetadata> tests) throws JSONException {
        String d =  api_uri + "/run-tests";
        HttpPost post = new HttpPost(d);
        JSONObject final_payload = new JSONObject();

        JSONArray payload = new JSONArray();
        for (PycrunchTestMetadata __ : tests) {
            payload.put(__.to_json());
        }
        final_payload.put("tests", payload);
        post.setEntity(new StringEntity(final_payload.toString(), ContentType.APPLICATION_JSON));
        HttpClient httpclient = HttpClients.createDefault();
        HttpClient client = HttpClients.createDefault();
        try {
            HttpResponse response = client.execute(post);
        } catch (IOException e) {
            e.printStackTrace();
        }

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
            ((PycrunchBusNotifier) this._bus.syncPublisher(PycrunchBusNotifier.CHANGE_ACTION_TOPIC)).engineDidConnect("---");
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
        JSONArray tests = data.getJSONArray("tests");
        for (int i=0; i < tests.length(); i++) {
            JSONObject x = tests.getJSONObject(i);
            PycrunchTestMetadata testMetadata = PycrunchTestMetadata.from_json(x);
            _tests.put(testMetadata.fqn, testMetadata);
        }

        queueMessageBusEvent();
        combinedCoverageDidUpdate();
    }

    private void post_discovery_command() throws IOException {
        HttpClient httpclient = HttpClients.createDefault();
        String d =  api_uri + "/discover?folder=%2FUsers%2Fgleb%2Fcode%2Fbc%2Fbriteapps-admin";
        HttpGet httppost = new HttpGet(d);

        HttpResponse response = httpclient.execute(httppost);
        HttpEntity entity = response.getEntity();

        if (entity != null) {
            try (InputStream inputStream = entity.getContent()) {
                String theString = convertStreamToString(inputStream);
            }
            catch (Exception e) {

            }
        }
    }


    public static String convertStreamToString(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder(2048); // Define a size if you have an idea of it.
        char[] read = new char[128]; // Your buffer size.
        try (InputStreamReader ir = new InputStreamReader(is, StandardCharsets.UTF_8)) {
            for (int i; -1 != (i = ir.read(read)); sb.append(read, 0, i));
        }
        return sb.toString();
    }
    public Collection<PycrunchTestMetadata> GetTests() {
        if (_tests == null) {
            return new ArrayList<PycrunchTestMetadata>();
        }

        return _tests.values();
    }

    public boolean should_create_marker_for(PsiFile containingFile, int lineNum) {
        if (_visited_lines == null) {
            _visited_lines = new HashSet<>();
        }

        if (_visited_lines.contains(lineNum)){
            return false;
        }
        _visited_lines.add(lineNum);
        return true;
    }

    public void clear_markers_cache() {
        _visited_lines = new HashSet<>();
    }

    public SingleFileCombinedCoverage GetCoveredLinesForFile(String absolute_path) {
        if (_combined_coverage == null) {
            return null;
        }

        return _combined_coverage.GetLinesCovering(absolute_path);
    }


    public HashMap<String, TestRunResult> get_results(){
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
        SingleFileCombinedCoverage singleFileCombinedCoverage = _combined_coverage._files.get(absolute_path);
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
//                username = data.getString("username");
//                message = data.getString("message");
        } catch (JSONException e) {
            System.out.println(e.toString());

        }
    }

    public void pin_tests(List<PycrunchTestMetadata> tests) throws JSONException {
        ArrayList<String> list = new ArrayList<>();
        for (PycrunchTestMetadata test: tests) {
            list.add(test.fqn);
        }

        String action = "pin";
        post_toggle_pin_command(action, list);

    }

    public void unpin_tests(List<PycrunchTestMetadata> tests) throws JSONException {
        ArrayList<String> list = new ArrayList<>();
        for (PycrunchTestMetadata test: tests) {
            list.add(test.fqn);
        }

        String action = "unpin";
        post_toggle_pin_command(action, list);

    }

    private void post_toggle_pin_command(String action, ArrayList<String> fqns) throws JSONException {
        String d =  api_uri + "/" + action + "-tests";
        HttpPost post = new HttpPost(d);
        JSONObject final_payload = new JSONObject();

        JSONArray payload = new JSONArray(fqns);
        final_payload.put("fqns", payload);
        post.setEntity(new StringEntity(final_payload.toString(), ContentType.APPLICATION_JSON));
        HttpClient client = HttpClients.createDefault();
        try {
            HttpResponse response = client.execute(post);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void update_mode(String mode) {
        String d =  api_uri + "/engine-mode";
        HttpPost post = new HttpPost(d);
        JSONObject final_payload = new JSONObject();

        try {
            final_payload.put("mode", mode);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        post.setEntity(new StringEntity(final_payload.toString(), ContentType.APPLICATION_JSON));
        HttpClient client = HttpClients.createDefault();
        try {
            HttpResponse response = client.execute(post);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void tryActivate(String email, String password) {
        _persistentState.Email = email;
        _persistentState.Password = password;
        ActivationConnector activationConnector = new ActivationConnector();
        ActivationInfo activated = activationConnector.activate(email, password);
        System.out.println("sig ok:  " + activated.verify_sig());
        _persistentState.ActivationData = activated.file;
        _persistentState.Sig = activated.sig;
        invalidateLicenseStateAndNotifyUI();

    }

    private void invalidateLicenseStateAndNotifyUI() {
        ActivationValidation activationValidation = new ActivationValidation();
        boolean valid_licence = activationValidation.is_valid_licence(_persistentState);
        if (valid_licence) {
            licenceActivated();
        } else {
            licenceInvalid();
        }
    }


}