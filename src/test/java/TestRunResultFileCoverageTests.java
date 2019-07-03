import junit.framework.TestCase;
import org.json.JSONException;
import org.json.JSONObject;

public class TestRunResultFileCoverageTests extends TestCase {
    public void test_coverage_parsed_correctly() throws JSONException {
        String test_data =
                "{\n" +
                        "            \"filename\": \"/Users/gleb/code/PyCrunch/tests_two.py\",\n" +
                        "            \"lines_covered\": [\n" +
                        "              1,\n" +
                        "              2,\n" +
                        "              5,\n" +
                        "              15,\n" +
                        "              23,\n" +
                        "              26,\n" +
                        "              27,\n" +
                        "              28\n" +
                        "            ],\n" +
                        "            \"analysis\": [\n" +
                        "              \"/Users/gleb/code/PyCrunch/tests_two.py\",\n" +
                        "              [\n" +
                        "                1,\n" +
                        "                2,\n" +
                        "                5,\n" +
                        "                7,\n" +
                        "                8,\n" +
                        "                9,\n" +
                        "                10,\n" +
                        "                11,\n" +
                        "                13,\n" +
                        "                15,\n" +
                        "                16,\n" +
                        "                17,\n" +
                        "                18,\n" +
                        "                20,\n" +
                        "                21,\n" +
                        "                23,\n" +
                        "                24,\n" +
                        "                26,\n" +
                        "                27,\n" +
                        "                28\n" +
                        "              ],\n" +
                        "              [\n" +
                        "                \n" +
                        "              ],\n" +
                        "              [\n" +
                        "                7,\n" +
                        "                8,\n" +
                        "                9,\n" +
                        "                10,\n" +
                        "                11,\n" +
                        "                13,\n" +
                        "                16,\n" +
                        "                17,\n" +
                        "                18,\n" +
                        "                20,\n" +
                        "                21,\n" +
                        "                24\n" +
                        "              ],\n" +
                        "              \"7-13, 16-21, 24\"\n" +
                        "            ],\n" +
                        "            \"arcs\": [\n" +
                        "              [\n" +
                        "                -1,\n" +
                        "                1\n" +
                        "              ],\n" +
                        "              [\n" +
                        "                1,\n" +
                        "                2\n" +
                        "              ],\n" +
                        "              [\n" +
                        "                2,\n" +
                        "                5\n" +
                        "              ],\n" +
                        "              [\n" +
                        "                5,\n" +
                        "                15\n" +
                        "              ],\n" +
                        "              [\n" +
                        "                15,\n" +
                        "                23\n" +
                        "              ],\n" +
                        "              [\n" +
                        "                23,\n" +
                        "                26\n" +
                        "              ],\n" +
                        "              [\n" +
                        "                26,\n" +
                        "                -1\n" +
                        "              ],\n" +
                        "              [\n" +
                        "                -26,\n" +
                        "                27\n" +
                        "              ],\n" +
                        "              [\n" +
                        "                27,\n" +
                        "                28\n" +
                        "              ],\n" +
                        "              [\n" +
                        "                28,\n" +
                        "                -26\n" +
                        "              ]\n" +
                        "            ]\n" +
                        "          }\n";
        JSONObject j = new JSONObject(test_data);
        TestRunResultFileCoverage actual = TestRunResultFileCoverage.from_json(j);
        assertEquals("/Users/gleb/code/PyCrunch/tests_two.py", actual.filename);
        assertTrue(actual.lines_covered.size() == 8);
        assertTrue(actual.lines_covered.contains(1));
        assertTrue(actual.lines_covered.contains(23));
        assertTrue(actual.lines_covered.contains(28));
        assertFalse(actual.lines_covered.contains(42));
    }
}
