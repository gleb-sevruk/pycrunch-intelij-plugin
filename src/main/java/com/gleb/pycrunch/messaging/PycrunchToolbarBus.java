package com.gleb.pycrunch.messaging;

import com.intellij.util.messages.Topic;

public interface PycrunchToolbarBus {
// This is a connector to wire old ui form with JB action system
    Topic<PycrunchToolbarBus> CHANGE_ACTION_TOPIC = Topic.create("pycrunch.toolbar", PycrunchToolbarBus.class);

    void runSelectedTests();
    void terminateTestRun();

    void refillTestList();

    void collapseAll();
    void expandAll();

    void applyWordWrap();
}