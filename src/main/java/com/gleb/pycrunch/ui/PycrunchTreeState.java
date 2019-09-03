package com.gleb.pycrunch.ui;

import java.util.HashSet;

public class PycrunchTreeState {
    private HashSet<String> _expandedNodes = new HashSet<>();
    public void nodeWillExpand(String module_name) {
        _expandedNodes.add(module_name);
    }

    public boolean isNodeExpanded(String module_name) {
        return _expandedNodes.contains(module_name);
    }

    public boolean nodeWillCollapse(String module_name) {
        return _expandedNodes.remove(module_name);
    }

}
