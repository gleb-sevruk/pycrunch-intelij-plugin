import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
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
    // Sets the maximum allowed number of opened projects.
    public  Project _project;
    private HashMap<String, TestRunResult> _results = new HashMap<>();
    private Set<Integer> _visited_lines;
    private MessageBus _bus;
    private String api_uri = "http://127.0.0.1:5000";
    private PycrunchCombinedCoverage _combined_coverage;

    public MyPycrunchConnector() {
        MyPycrunchConnector.CounterOfSingletons++;

    }
    private JSONObject _discovered_tests;
    private ArrayList<PycrunchTestMetadata> _tests;
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
    private void ApplyTestDiscoveryResults(JSONObject data) throws JSONException {

        _discovered_tests = data;
        _tests = new ArrayList<>();
        JSONArray tests = _discovered_tests.getJSONArray("tests");
        for (int i=0; i < tests.length(); i++) {
            JSONObject x = tests.getJSONObject(i);
            _tests.add(PycrunchTestMetadata.from_json(x));
        }

        queueMessageBusEvent();
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
    public ArrayList<PycrunchTestMetadata> GetTests() {
        if (_tests == null) {
            return new ArrayList<PycrunchTestMetadata>();
        }

        return _tests;
    }
    public String GetDiscoveredString() {
        StringBuilder sb = new StringBuilder();
        for (PycrunchTestMetadata t: _tests) {
            sb.append(t.fqn);
            sb.append(" - ");
            sb.append(t.state);
            sb.append('\n');
        }

        return sb.toString();
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
        return _combined_coverage.get_marker_color_for(absolute_path, line_number);
    }

    public PycrunchTestMetadata FindTestByFqn(String fqn) {
        for (PycrunchTestMetadata testMetadata: _tests){
            if (testMetadata.fqn.equals(fqn)) {
                return testMetadata;
            }
        }
        return null;
    }

    public String GetTestStatus(String fqn) {
        return _combined_coverage.GetTestStatus(fqn);
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

        }
    }
}