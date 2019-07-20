import com.gleb.pycrunch.PycrunchTestMetadata;
import junit.framework.TestCase;
import org.json.JSONException;
import org.json.JSONObject;

public class PycrunchTestMetadataTests extends TestCase {
    public void test_one() throws JSONException {
        String test;
        test = "{\"fqn\":\"tests_one:test_1\"," +
                "\"filename\":\"/Users/gleb/code/PyCrunch/tests_one.py\"," +
                "\"pinned\": true," +
                "\"module\":\"tests_one\",\"name\":\"test_1\"," +
                "\"state\":\"pending\"}";
        JSONObject x = new JSONObject(test);

        PycrunchTestMetadata actual = PycrunchTestMetadata.from_json(x);
        assertEquals("tests_one:test_1", actual.fqn);
        assertEquals("/Users/gleb/code/PyCrunch/tests_one.py", actual.filename);
        assertEquals("tests_one", actual.module);
        assertEquals("test_1", actual.name);
        assertEquals("pending", actual.state);
        assertTrue(actual.pinned);

    }
}
