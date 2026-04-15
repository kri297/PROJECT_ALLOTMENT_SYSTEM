import javax.swing.*;
import java.awt.*;

public class LoginSystem extends JFrame {

    public LoginSystem() {
        setTitle("Project Allocation System - Login");
        setSize(560, 420);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        UITheme.styleFrame(this);

        JPanel root = new JPanel(new BorderLayout());
        root.setOpaque(true);
        root.setBackground(UITheme.SOFT_BG);

        JPanel header = UITheme.createGradientHeader(
                "Project Allocation System",
                "Beautiful CSV-backed login with role-based dashboards");
        root.add(header, BorderLayout.NORTH);

        JPanel body = UITheme.createBodyPanel();
        JPanel card = UITheme.createCardPanel(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(8, 8, 8, 8);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1.0;

        JLabel introLabel = new JLabel("Sign in to continue");
        UITheme.styleLabel(introLabel, Font.BOLD, 18, UITheme.DEEP_NAVY);
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 2;
        card.add(introLabel, constraints);

        JLabel usernameLabel = new JLabel("Username");
        UITheme.styleLabel(usernameLabel, Font.PLAIN, 13, UITheme.DEEP_NAVY);
        constraints.gridy = 1;
        constraints.gridwidth = 1;
        constraints.gridx = 0;
        card.add(usernameLabel, constraints);

        JTextField usernameField = new JTextField();
        UITheme.styleTextField(usernameField);
        constraints.gridx = 1;
        card.add(usernameField, constraints);

        JLabel passwordLabel = new JLabel("Password");
        UITheme.styleLabel(passwordLabel, Font.PLAIN, 13, UITheme.DEEP_NAVY);
        constraints.gridy = 2;
        constraints.gridx = 0;
        card.add(passwordLabel, constraints);

        JPasswordField passwordField = new JPasswordField();
        UITheme.stylePasswordField(passwordField);
        constraints.gridx = 1;
        card.add(passwordField, constraints);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 6));
        buttonPanel.setOpaque(false);

        JButton loginButton = new JButton("Login");
        UITheme.styleButton(loginButton, UITheme.ROYAL, Color.WHITE);
        loginButton.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter both username and password!",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String role = DataManager.authenticateUser(username, password);
            if (role != null) {
                String canonicalUsername = DataManager.getCanonicalUsername(username);
                dispose();
                openDashboard(canonicalUsername != null ? canonicalUsername : username, role);
            } else {
                JOptionPane.showMessageDialog(this, "Invalid username or password!",
                        "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        });
        buttonPanel.add(loginButton);

        JButton exitButton = new JButton("Exit");
        UITheme.styleButton(exitButton, UITheme.CORAL, Color.WHITE);
        exitButton.addActionListener(e -> System.exit(0));
        buttonPanel.add(exitButton);

        constraints.gridy = 3;
        constraints.gridx = 0;
        constraints.gridwidth = 2;
        card.add(buttonPanel, constraints);

        body.add(card, BorderLayout.CENTER);
        root.add(body, BorderLayout.CENTER);

        setContentPane(root);
        setVisible(true);

        // Add Enter key functionality
        passwordField.addActionListener(e -> loginButton.doClick());
    }

    private void openDashboard(String username, String role) {
        switch (role.toLowerCase()) {
            case "student":
                new StudentDashboard(username);
                break;
            case "teacher":
                new TeacherDashboard(username);
                break;
            case "admin":
                new AdminDashboard(username);
                break;
            default:
                JOptionPane.showMessageDialog(null, "Invalid role!",
                        "Error", JOptionPane.ERROR_MESSAGE);
                new LoginSystem();
        }
    }

    public static void main(String[] args) {
        // Initialize data layer (auto-detects MongoDB or CSV)
        DataManager.initialize();

        // Set look and feel
        UITheme.applyGlobalLookAndFeel();

        // Create and show login window
        SwingUtilities.invokeLater(() -> new LoginSystem());
    }
}
