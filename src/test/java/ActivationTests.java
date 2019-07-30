import com.gleb.pycrunch.activation.ActivationConnector;
import com.gleb.pycrunch.activation.ActivationInfo;
import com.gleb.pycrunch.activation.DateWrapper;
import junit.framework.TestCase;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Base64;
import java.util.Date;

public class ActivationTests extends TestCase {

    public void test_activation_complete() throws JSONException {
        ActivationConnector sut = new ActivationConnector();
        ActivationInfo activation_info = sut.activate("x", "y");
        assertTrue(activation_info.verify_sig());
        String decoded = decodeString(activation_info.file);
        JSONObject j = new JSONObject(decoded);
        String until = j.getString("license_valid_until");
        Instant date = DateWrapper.parse_from_iso(until);


    }

    public static String decodeString(String encodedString) {
        byte[] bytes = Base64.getDecoder().decode(encodedString);
        return new String(bytes);
    }
}
