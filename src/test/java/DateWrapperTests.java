import com.gleb.pycrunch.activation.ActivationConnector;
import com.gleb.pycrunch.activation.ActivationInfo;
import com.gleb.pycrunch.activation.DateWrapper;
import junit.framework.TestCase;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.*;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;

public class DateWrapperTests extends TestCase {

    public void test_iso_date_parsed() throws JSONException {
        String test_case = "2019-07-29T20:42:45.000000+00:00";
        Instant actual = DateWrapper.parse_from_iso(test_case);

        LocalDateTime l = LocalDateTime.of(2019, 7, 29, 20, 42, 45);
        Instant expected = l.toInstant(ZoneOffset.UTC);
        assertEquals(actual, expected);
    }

    public void test_expiration_date_still_valid() {
        LocalDateTime l = LocalDateTime.of(2999, 7, 29, 20, 42, 45);
        assertTrue(DateWrapper.licence_still_valid(l.toInstant(ZoneOffset.UTC)));
    }

    public void test_expiration_date_expired() {
        LocalDateTime l = LocalDateTime.of(1999, 7, 29, 20, 42, 45);
        assertFalse(DateWrapper.licence_still_valid(l.toInstant(ZoneOffset.UTC)));
    }
}
