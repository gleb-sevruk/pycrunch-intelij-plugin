import org.json.JSONException;
import org.json.JSONObject;

public class PycrunchTestMetadata {
    public String fqn;
    public String filename;
    public String module;
    public String state;
    public String name;

    public static PycrunchTestMetadata from_json(JSONObject json) throws JSONException {
        PycrunchTestMetadata metadata = new PycrunchTestMetadata();
        metadata.fqn = json.getString("fqn");
        metadata.filename = json.getString("filename");
        metadata.module = json.getString("module");
        metadata.state = json.getString("state");
        metadata.name = json.getString("name");
        return metadata;
    }

    @Override
    public String toString() {
        return this.state + " - " + this.fqn;
    }
}
