package com.gleb.pycrunch.exceptionPreview;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import java.util.*;

public class VariableTreeJsonTreeStructure {

    private final DefaultMutableTreeNode myRoot;

    public VariableTreeJsonTreeStructure(JSONObject json) throws JSONException {
        myRoot = new DefaultMutableTreeNode("Root");

        processNode(json, myRoot);
    }

    private void processNode(Object json, DefaultMutableTreeNode parentNode) throws JSONException {
        if (json instanceof JSONObject) {
            JSONObject jsonObject = (JSONObject) json;
            Iterator<String> keys = jsonObject.sortedKeys();
            ArrayList<String> keyList = new ArrayList<>();
            while (keys.hasNext()) {
                String key = keys.next();
                keyList.add(key);
                Object value = jsonObject.get(key);

                String nodeName = key + ": " + value;
                VariableTreeUserObject userObject = new VariableTreeUserObject(key, value);
                DefaultMutableTreeNode node = new DefaultMutableTreeNode(userObject);
                parentNode.add(node);

                processNode(value, node);
            }
        } else if (json instanceof JSONArray) {
            JSONArray jsonArray = (JSONArray) json;
            for (int i = 0; i < jsonArray.length(); i++) {
                Object value = jsonArray.get(i);

                String nodeName = "[" + i + "]";
                VariableTreeUserObject userObject = new VariableTreeUserObject(nodeName, value);
                DefaultMutableTreeNode node = new DefaultMutableTreeNode(userObject);

                parentNode.add(node);

                processNode(value, node);
            }
        }
    }

    public DefaultMutableTreeNode getRoot() {
        return myRoot;
    }
}






