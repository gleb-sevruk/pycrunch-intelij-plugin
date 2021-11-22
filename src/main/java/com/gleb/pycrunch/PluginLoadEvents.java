package com.gleb.pycrunch;


import com.intellij.ide.plugins.DynamicPluginListener;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import org.jetbrains.annotations.NotNull;

public final class PluginLoadEvents implements DynamicPluginListener {
    @Override
    public void pluginUnloaded(@NotNull IdeaPluginDescriptor pluginDescriptor, boolean isUpdate) {
//        System.out.println("pluginUnloaded ");

    }

    @Override
    public void pluginLoaded(@NotNull IdeaPluginDescriptor pluginDescriptor) {
//        System.out.println("pluginLoaded ");
    }
}