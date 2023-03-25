package com.gleb.pycrunch.exceptionPreview;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

public class VariableTreeUserObject {
    public String name;
    public Object value;

    public VariableTreeUserObject(String name, Object value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public String toString() {
//        Overridden to allow copying value in the tree.
//        Without this it would copy <VariableTreeUserObject at 0x000>
//        This is NOT CONTRIBUTING to the way how it will be shown in UI
        return value.toString();
    }
}

