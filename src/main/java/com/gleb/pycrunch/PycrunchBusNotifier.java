package com.gleb.pycrunch;

import com.intellij.util.messages.Topic;

public interface PycrunchBusNotifier {

    Topic<PycrunchBusNotifier> CHANGE_ACTION_TOPIC = Topic.create("pycrunch.event", PycrunchBusNotifier.class);

    void beforeAction(String context);
    void engineDidConnect(String context);
    void engineDidDisconnect(String context);
    void combinedCoverageDidUpdate(String context);
}