package com.gleb.pycrunch.actions;

import com.gleb.pycrunch.PycrunchConnector;
import com.gleb.pycrunch.PycrunchTestMetadata;
import org.json.JSONException;

import java.util.List;

public class ToggleTestPinnedState {
    public void Run(List<PycrunchTestMetadata> tests, PycrunchConnector connector, boolean new_state) {
        if (new_state) {
            try {
                connector.pin_tests(tests);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            try {
                connector.unpin_tests(tests);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

    }
}
