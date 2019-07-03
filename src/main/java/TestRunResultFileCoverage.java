import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;

public class TestRunResultFileCoverage {
    public Set<Integer> lines_covered;
    public String filename;

    public static TestRunResultFileCoverage from_json(JSONObject x) throws JSONException {
        TestRunResultFileCoverage coverage = new TestRunResultFileCoverage();
        coverage.filename = x.getString("filename");
        coverage.lines_covered = new HashSet<Integer>();
        JSONArray lines = x.getJSONArray("lines_covered");
        for (int i=0; i < lines.length(); i++) {
            int line_number = lines.getInt(i);
            coverage.lines_covered.add(line_number);
        }
        return coverage;
    }
}
