import com.gleb.pycrunch.TestRunResult;
import junit.framework.TestCase;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TestRunResultTests  extends TestCase {
    public void test_parse_run_result() throws JSONException {
        String data =
                "{\n" +
                        "  \"coverage\": {\n" +
                        "    \"all_runs\": {\n" +
                        "      \"tests_two:test_failing\": {\n" +
                        "        \"percentage_covered\": 40,\n" +
                        "        \"time_elapsed\": 146.26,\n" +
                        "        \"test_metadata\": {\n" +
                        "          \"fqn\": \"tests_two:test_failing\",\n" +
                        "          \"filename\": \"/Users/gleb/code/PyCrunch/tests_two.py\",\n" +
                        "          \"module\": \"tests_two\",\n" +
                        "          \"name\": \"test_failing\",\n" +
                        "          \"state\": \"failed\"\n" +
                        "        },\n" +
                        "        \"variables_state\": [],\n" +

                        "        \"captured_output\": \"'/Users/gleb/code/PyCrunch/tests_two.py::test_failing'\\n" +
                                "48574\\n" +
                                "F\\n" +
                                "=================================== FAILURES ===================================\\n" +
                                "_________________________________ test_failing _________________________________\\n" +
                                "\\n" +
                                "    def test_failing():\\n" +
                                "        assert 3 == 3\\n" +
                                ">       assert 3 == 2\\n" +
                                "E       assert 3 == 2\\n" +
                                "\\n" +
                                "tests_two.py:28: AssertionError\\n" +
                                "1 failed in 0.02 seconds\\n" +
                                "48574\\n" +
                                "{'passed_tests': set()}\\n" +
                                "{'failed_tests': {'tests_two.py::test_failing'}}\\n" +
                                "testing output interception\\n" +
                                "\",\n" +
                        "        \"files\": [\n" +
                        "          {\n" +
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
                        "          }\n" +
                        "        ],\n" +
                        "        \"entry_point\": \"tests_two:test_failing\",\n" +
                        "        \"status\": \"failed\"\n" +
                        "      }\n" +
                        "    }\n" +
                        "  },\n" +
                        "  \"event_type\": \"test_run_completed\",\n" +
                        "  \"data\": [\n" +
                        "    {\n" +
                        "      \"fqn\": \"tests_two:test_failing\",\n" +
                        "      \"filename\": \"/Users/gleb/code/PyCrunch/tests_two.py\",\n" +
                        "      \"module\": \"tests_two\",\n" +
                        "      \"name\": \"test_failing\",\n" +
                        "      \"state\": \"failed\"\n" +
                        "    }\n" +
                        "  ],\n" +
                        "  \"timings\": {\n" +
                        "    \"start\": 337851.471943206,\n" +
                        "    \"end\": 337852.390076842\n" +
                        "  }\n" +
                        "}";
        JSONObject j = new JSONObject(data);
        JSONObject cov = j.getJSONObject("coverage");
        JSONObject all_runs = cov.getJSONObject("all_runs");
        JSONArray keys = all_runs.names ();

        for (int i = 0; i < keys.length (); ++i) {

            String key = keys.getString(i);
            JSONObject value = all_runs.getJSONObject(key);
            TestRunResult result = TestRunResult.from_json(value);
            assertTrue(result.captured_output.contains("E       assert 3 == 2"));
            assertEquals(1, result.files_covered.size());
            assertEquals("failed", result.status);
            System.out.println(value.toString());
        }
    }
}
