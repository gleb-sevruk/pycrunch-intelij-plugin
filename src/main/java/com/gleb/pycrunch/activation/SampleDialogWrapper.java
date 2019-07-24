package com.gleb.pycrunch.activation;

import com.intellij.openapi.ui.DialogWrapper;
import jdk.internal.jline.internal.Nullable;

import javax.swing.*;
import java.awt.*;

public class SampleDialogWrapper extends DialogWrapper {

    public JTextField _emailTextBox;
    public JPasswordField _passwordTextBox;

    public SampleDialogWrapper() {
        super(true); // use current window as parent
        init();
        setTitle("Activate PyCrunch");
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 1));

        // User Label
        JLabel user_label = new JLabel();
        user_label.setText("Email:");
        _emailTextBox = new JTextField();

        // Password

        JLabel password_label = new JLabel();
        password_label.setText("Password:");
        _passwordTextBox = new JPasswordField();

        // Submit



        panel.add(user_label);
        panel.add(_emailTextBox);
        panel.add(password_label);
        panel.add(_passwordTextBox);


        return panel;
    }
}
