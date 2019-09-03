package com.gleb.pycrunch.ui;

import com.gleb.pycrunch.PycrunchTestMetadata;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

public class PycrunchDefaultTestTree {
    private final   String     ROOT  = "Root node (invisible)";

    private final ArrayList<PycrunchTestMetadata> _tests;

    public PycrunchDefaultTestTree(ArrayList<PycrunchTestMetadata> tests) {
        _tests = tests;
    }

    public DefaultMutableTreeNode getRoot() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(ROOT);

        TreeMap<String, ArrayList<PycrunchTestMetadata>> modules = build_module_tree();
        for (String module_name : modules.keySet()) {
            DefaultMutableTreeNode module_tree_node = new DefaultMutableTreeNode(module_name);
            ArrayList<PycrunchTestMetadata> pycrunchTestMetadata = modules.get(module_name);
            for (PycrunchTestMetadata test : pycrunchTestMetadata) {
                DefaultMutableTreeNode test_leaf_node = new DefaultMutableTreeNode(test, false);
                module_tree_node.add(test_leaf_node);
            }
            root.add(module_tree_node);
        }
        return root;
    }

    @NotNull
    private TreeMap<String, ArrayList<PycrunchTestMetadata>> build_module_tree() {
        TreeMap<String, ArrayList<PycrunchTestMetadata>> modules = new TreeMap<>();
        for (PycrunchTestMetadata t : _tests) {
            ArrayList<PycrunchTestMetadata> my;
            if (!modules.containsKey(t.module)) {
                modules.put(t.module, new ArrayList<>());
            }
            my = modules.get(t.module);
            my.add(t);
        }
        return modules;
    }
}
