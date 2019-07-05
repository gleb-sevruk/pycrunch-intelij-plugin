import junit.framework.TestCase;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;

public class CombinedCoverageTest extends TestCase {
    public void test_has_correct_aggregated_statuses() throws IOException, JSONException {
        PycrunchCombinedCoverage target = createTarget();

        assertEquals("success", target.GetTestStatus("tests_one:test_1"));
        assertEquals("success", target.GetTestStatus("tests_one:test_6"));
        assertEquals("failed", target.GetTestStatus("tests_two:test_failing"));
    }

    public void test_has_coverage() throws IOException, JSONException {
        PycrunchCombinedCoverage target = createTarget();

        String filename = "/Users/gleb/code/PyCrunch/tests_one.py";
        SingleFileCombinedCoverage actual = target.GetLinesCovering(filename);
        HashSet<String> strings = actual._lines_hit_by_run.get(1);
        assertTrue(strings.contains("tests_one:test_1"));
        assertTrue(strings.contains("tests_one:test_6"));


        assertTrue(actual._lines_hit_by_run.get(9).contains("tests_one:test_1"));
        assertEquals(1, actual._lines_hit_by_run.get(9).size());


    }

    private PycrunchCombinedCoverage createTarget() throws JSONException, IOException {
        InputStream resource = this.getClass().getResourceAsStream("/combined_coverage_example.json");

        JSONObject j = new JSONObject(convertStreamToString(resource));
        return PycrunchCombinedCoverage.from_json(j);
    }

    public static String convertStreamToString(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder(2048); // Define a size if you have an idea of it.
        char[] read = new char[128]; // Your buffer size.
        try (InputStreamReader ir = new InputStreamReader(is, StandardCharsets.UTF_8)) {
            for (int i; -1 != (i = ir.read(read)); sb.append(read, 0, i));
        }
        return sb.toString();
    }
}
