package com.gleb.pycrunch.activation;

import com.gleb.pycrunch.PycrunchConnector;
import com.gleb.pycrunch.shared.IdeNotifications;
import com.gleb.pycrunch.shared.MyPasswordStore;
import com.intellij.credentialStore.Credentials;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
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
    private final PycrunchConnector _connector;
    public JPasswordField _password;
    private JPanel centerPanel;
    public JTextField _email;
    private JLabel label_status;
    private JLabel label_go_to_site;
    private Project _project;
    private MyStateService _persistentState;

    public ActivationForm(@Nullable Project project) {
        super(project);
        _bus = project.getMessageBus();
        _connector = ServiceManager.getService(project, PycrunchConnector.class);
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
        prefillUsername();
    }

    private void prefillUsername() {
        Credentials credentials = MyPasswordStore.getAccountCredentials();
        if (credentials == null) {
            return;
        }

        _email.setText(credentials.getUserName());
        _password.setText(credentials.getPasswordAsString());
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
        MyPasswordStore.saveCredentials(email, password);
        ActivationConnector activationConnector = new ActivationConnector();
        ActivationInfo activated = activationConnector.activate(email, password);
        if (activated == null) {
            label_status.setText("Invalid email or password");

        }
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
            IdeNotifications.notify( _project,"Licence for Pycrunch is now active!", license_details, NotificationType.INFORMATION);
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
