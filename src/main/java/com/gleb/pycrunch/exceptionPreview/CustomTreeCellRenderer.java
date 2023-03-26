package com.gleb.pycrunch.exceptionPreview;

import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.speedSearch.SpeedSearchUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;

public class CustomTreeCellRenderer extends ColoredTreeCellRenderer {

    private final int MAX_LENGTH_TO_RENDER_IN_TREE = 250;

    @Override
    public void customizeCellRenderer(
            @NotNull JTree tree, Object value, boolean selected, boolean expanded,
            boolean leaf, int row, boolean hasFocus) {

        if (value instanceof DefaultMutableTreeNode) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
            Object userObject = node.getUserObject();
            if (userObject instanceof VariableTreeUserObject) {
                var nodeObject = (VariableTreeUserObject) userObject;
                String propertyName = nodeObject.name;
                var propertyValue = JsonSummary.getJsonSummary(nodeObject.value, MAX_LENGTH_TO_RENDER_IN_TREE);

                // Set the property name color and style
                append(propertyName +" =", SimpleTextAttributes.GRAY_ATTRIBUTES);

                // Set the property value color and style
                append(" " + propertyValue, SimpleTextAttributes.REGULAR_ATTRIBUTES);
            }
        }
        SpeedSearchUtil.applySpeedSearchHighlighting(tree, this, false, selected);

    }
}
