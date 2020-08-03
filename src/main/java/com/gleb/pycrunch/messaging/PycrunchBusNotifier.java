package com.gleb.pycrunch.messaging;

import com.gleb.pycrunch.PycrunchTestMetadata;
import com.intellij.util.messages.Topic;

public interface PycrunchBusNotifier {

    Topic<PycrunchBusNotifier> CHANGE_ACTION_TOPIC = Topic.create("pycrunch.event", PycrunchBusNotifier.class);

    void beforeAction(String context);
    void engineDidConnect(String context);
    void engineDidDisconnect(String context);
    void engineWillTryToReconnect(String context);
    void combinedCoverageDidUpdate(String context);
    void licenceActivated();
    void licenceInvalid();

    void did_select_test(PycrunchTestMetadata userObject);

    void engineDidLoadMode(String new_mode);

    void engineDidLoadVersion(int version_major, int version_minor);

    void engineDidFailToReconnect(String dummy);
}