package com.gleb.pycrunch.activation;

import com.gleb.pycrunch.MyPycrunchConnector;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.MessageType;
import com.intellij.util.messages.MessageBus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

public class ActivationForm extends DialogWrapper {
    private final MessageBus _bus;
    private final MyPycrunchConnector _connector;
    public JPasswordField _password;
    private JPanel centerPanel;
    public JTextField _email;
    private JLabel label_status;
    private JLabel label_go_to_site;
    public static final NotificationGroup GROUP_DISPLAY_ID_INFO =
            new NotificationGroup("Pycrunch",
                    NotificationDisplayType.BALLOON, true);
    private Project _project;
    private MyStateService _persistentState;

    public ActivationForm(@Nullable Project project) {
        super(project);
        _bus = project.getMessageBus();
        _connector = ServiceManager.getService(MyPycrunchConnector.class);
        _persistentState = ServiceManager.getService(MyStateService.class);

        this._project = project;
        init();
        label_go_to_site.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                open_pycrunch_site();
            }
        });;
        setTitle("Pycrunch Activation");
    }

    private void open_pycrunch_site() {
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
            try {
                desktop.browse(new URI(ActivationConnector.site_url));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void doOKAction() {
        label_status.setText("");
        System.out.println("dermo!");
        String email = _email.getText();
        String password = _password.getText();
        _persistentState.Email = email;
        _persistentState.Password = password;
        ActivationConnector activationConnector = new ActivationConnector();
        ActivationInfo activated = activationConnector.activate(email, password);
        _persistentState.ActivationData = activated.file;
        _persistentState.Sig = activated.sig;

        if (_persistentState.Exp == null) {
//            set only once for trial on current machine
            _persistentState.Exp = activated.exp;
            _persistentState.ExpSig = activated.exp_sig;
        } else {
            System.out.println("User had previously started trial, cannot start new one even with new user");
        }

        ActivationInfo info = new ActivationInfo(_persistentState.ActivationData, _persistentState.Sig, _persistentState.Exp, _persistentState.ExpSig);

        boolean licence_valid = _connector.invalidateLicenseStateAndNotifyUI();
        if (licence_valid) {
            String license_details = info.get_details(_persistentState);
            Notification orel = GROUP_DISPLAY_ID_INFO.createNotification("Pycrunch", "Licence for Pycrunch is now active!", license_details, NotificationType.INFORMATION);
            orel.notify(_project);
            this.close(0);
        } else {
            String details = "Generic licence error";
            if (!info.has_license()) {
                if (info.verify_trial_sig()) {
                    Instant exp = new ActivationValidation().trial_exp_date(_persistentState);
                    String date = format_date(exp);
                    details = "trial expired on " + date;
                }
            } else {
                if (info.verify_license_sig()) {
                    ActivationValidation validation = new ActivationValidation();
                    Instant exp = validation.license_exp_date(_persistentState);
                    if (exp != null) {
                        String format = format_date(exp);
                        details = "Licence expired on " + format;
                    }
                }
            }
            label_status.setText("Activation failed. \n" + details);

        }


    }

    @NotNull
    private String format_date(Instant exp) {
        DateTimeFormatter formatter =
                DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
                        .withLocale(Locale.getDefault())
                        .withZone(ZoneId.systemDefault());
        return formatter.format(exp);
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return centerPanel;
    }


}
