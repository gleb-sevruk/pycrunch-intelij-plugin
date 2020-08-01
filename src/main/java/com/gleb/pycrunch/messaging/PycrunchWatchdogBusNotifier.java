package com.gleb.pycrunch.messaging;

import com.intellij.util.messages.Topic;

public interface PycrunchWatchdogBusNotifier {

    Topic<PycrunchWatchdogBusNotifier> CHANGE_ACTION_TOPIC = Topic.create("pycrunch.watchdog_event", PycrunchWatchdogBusNotifier.class);

    void watchdogBegin(int test_count);

    void watchdogEnd();
}