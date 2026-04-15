import javax.swing.*;
import java.awt.*;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class AdminDashboard extends JFrame {
    private static final String PASSWORD_REVEAL_KEY = "2006";
    private JLabel statsLabel;
    private JLabel usersMetric;
    private JLabel teachersMetric;
    private JLabel studentsMetric;
    private JLabel pendingMetric;
    private JLabel confirmedMetric;
    private JLabel sidebarUsersValue;
    private JLabel sidebarTeachersValue;
    private JLabel sidebarStudentsValue;
    private JLabel sidebarPendingValue;
    private JLabel sidebarConfirmedValue;
    private JLabel sidebarPasswordState;
    private boolean adminPasswordsUnlocked;
    private JLabel workspaceTitle;
    private JLabel workspaceStatus;
    private JPanel workspaceBody;

    public AdminDashboard(String username) {
        setTitle("Admin Dashboard - " + username);
        setSize(1280, 820);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        UITheme.styleFrame(this);
        UITheme.configureFullScreen(this);

        JPanel root = new JPanel(new BorderLayout(18, 18));
        root.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        root.setBackground(UITheme.SOFT_BG);

        root.add(UITheme.createGradientHeader(
                "Admin Dashboard",
                "Top action bar, fixed left panel, and feature views in the full center-right workspace"),
                BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(18, 18));
        body.setOpaque(false);
        root.add(body, BorderLayout.CENTER);

        JPanel summaryStrip = UITheme.createRoundedPanel(new BorderLayout());
        summaryStrip.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(214, 223, 247), 1, true),
                BorderFactory.createEmptyBorder(12, 18, 12, 18)));
        statsLabel = new JLabel("", SwingConstants.LEFT);
        UITheme.styleLabel(statsLabel, Font.PLAIN, 13, UITheme.MIDNIGHT);
        summaryStrip.add(statsLabel, BorderLayout.CENTER);
        body.add(summaryStrip, BorderLayout.NORTH);

        JPanel mainArea = new JPanel(new BorderLayout(18, 18));
        mainArea.setOpaque(false);
        body.add(mainArea, BorderLayout.CENTER);

        JPanel sidebar = UITheme.createRoundedPanel(new BorderLayout(12, 12));
        sidebar.setPreferredSize(new Dimension(300, 0));
        sidebar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(214, 223, 247), 1, true),
                BorderFactory.createEmptyBorder(18, 18, 18, 18)));

        JLabel sidebarTitle = new JLabel("Admin Panel");
        UITheme.styleLabel(sidebarTitle, Font.BOLD, 18, UITheme.DEEP_NAVY);
        sidebar.add(sidebarTitle, BorderLayout.NORTH);

        JPanel leftSummary = new JPanel();
        leftSummary.setLayout(new BoxLayout(leftSummary, BoxLayout.Y_AXIS));
        leftSummary.setOpaque(false);
        leftSummary.add(createSidebarMetricRow("Users", sidebarUsersValue = createSidebarValueLabel()));
        leftSummary.add(Box.createVerticalStrut(8));
        leftSummary.add(createSidebarMetricRow("Teachers", sidebarTeachersValue = createSidebarValueLabel()));
        leftSummary.add(Box.createVerticalStrut(8));
        leftSummary.add(createSidebarMetricRow("Students", sidebarStudentsValue = createSidebarValueLabel()));
        leftSummary.add(Box.createVerticalStrut(8));
        leftSummary.add(createSidebarMetricRow("Pending", sidebarPendingValue = createSidebarValueLabel()));
        leftSummary.add(Box.createVerticalStrut(8));
        leftSummary.add(createSidebarMetricRow("Confirmed", sidebarConfirmedValue = createSidebarValueLabel()));
        leftSummary.add(Box.createVerticalStrut(12));

        sidebarPasswordState = new JLabel("Passwords: Locked");
        UITheme.styleLabel(sidebarPasswordState, Font.BOLD, 12, UITheme.MIDNIGHT);
        leftSummary.add(sidebarPasswordState);
        leftSummary.add(Box.createVerticalStrut(12));

        JTextArea noteArea = new JTextArea(
            "User passwords are masked as xxxx by default. Enter key 2006 in View All Users to reveal them.");
        noteArea.setEditable(false);
        UITheme.styleTextArea(noteArea);
        noteArea.setBackground(new Color(248, 250, 255));
        noteArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        leftSummary.add(noteArea);
        sidebar.add(leftSummary, BorderLayout.CENTER);
        mainArea.add(sidebar, BorderLayout.WEST);

        JPanel workspace = new JPanel(new BorderLayout(16, 16));
        workspace.setOpaque(false);
        mainArea.add(workspace, BorderLayout.CENTER);

        JPanel topActionBar = UITheme.createRoundedPanel(new GridLayout(2, 3, 10, 10));
        topActionBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(214, 223, 247), 1, true),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));
        topActionBar.add(createTopActionButton("Add New User", UITheme.ROYAL, e -> addUser()));
        topActionBar.add(createTopActionButton("Remove User", UITheme.CORAL, e -> removeUser()));
        topActionBar.add(createTopActionButton("Change Password", UITheme.MINT, e -> changePassword()));
        topActionBar.add(createTopActionButton("View All Users", UITheme.AMBER, e -> viewAllUsers()));
        topActionBar.add(createTopActionButton("View All Projects", UITheme.MIDNIGHT, e -> viewAllProjects()));
        topActionBar.add(createTopActionButton("Logout", new Color(94, 114, 228), e -> logout()));
        workspace.add(topActionBar, BorderLayout.NORTH);

        JPanel contentPanel = UITheme.createRoundedPanel(new BorderLayout(12, 12));
        contentPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(214, 223, 247), 1, true),
                BorderFactory.createEmptyBorder(18, 18, 18, 18)));

        JPanel contentHeader = new JPanel(new BorderLayout(12, 12));
        contentHeader.setOpaque(false);

        workspaceTitle = new JLabel("Workspace Home");
        UITheme.styleLabel(workspaceTitle, Font.BOLD, 22, UITheme.DEEP_NAVY);
        contentHeader.add(workspaceTitle, BorderLayout.WEST);

        workspaceStatus = new JLabel("Ready", SwingConstants.RIGHT);
        UITheme.styleLabel(workspaceStatus, Font.PLAIN, 13, UITheme.MIDNIGHT);
        contentHeader.add(workspaceStatus, BorderLayout.EAST);
        contentPanel.add(contentHeader, BorderLayout.NORTH);

        workspaceBody = new JPanel(new BorderLayout(16, 16));
        workspaceBody.setOpaque(false);
        contentPanel.add(workspaceBody, BorderLayout.CENTER);

        workspace.add(contentPanel, BorderLayout.CENTER);

        showHomePanel();
        refreshStats();

        setContentPane(root);
        setVisible(true);
    }

    private void showHomePanel() {
        workspaceTitle.setText("Workspace Home");
        workspaceStatus.setText("Choose an action from the top bar");
        workspaceBody.removeAll();

        JPanel metricsPanel = new JPanel(new GridLayout(1, 5, 12, 12));
        metricsPanel.setOpaque(false);
        usersMetric = createMetricValueLabel();
        teachersMetric = createMetricValueLabel();
        studentsMetric = createMetricValueLabel();
        pendingMetric = createMetricValueLabel();
        confirmedMetric = createMetricValueLabel();

        metricsPanel.add(createMetricCard("Users", usersMetric, UITheme.ROYAL));
        metricsPanel.add(createMetricCard("Teachers", teachersMetric, UITheme.MINT));
        metricsPanel.add(createMetricCard("Students", studentsMetric, UITheme.CYAN));
        metricsPanel.add(createMetricCard("Pending", pendingMetric, UITheme.AMBER));
        metricsPanel.add(createMetricCard("Confirmed", confirmedMetric, UITheme.CORAL));
        workspaceBody.add(metricsPanel, BorderLayout.NORTH);

        JPanel detailGrid = new JPanel(new GridLayout(1, 2, 16, 16));
        detailGrid.setOpaque(false);

        JPanel overviewCard = UITheme.createRoundedPanel(new BorderLayout(10, 10));
        overviewCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(214, 223, 247), 1, true),
                BorderFactory.createEmptyBorder(18, 18, 18, 18)));

        JLabel overviewTitle = new JLabel("System Overview");
        UITheme.styleLabel(overviewTitle, Font.BOLD, 18, UITheme.DEEP_NAVY);
        overviewCard.add(overviewTitle, BorderLayout.NORTH);

        JTextArea overviewText = new JTextArea(
                "Manage users, credentials, and records from one large workspace without popup windows.");
        overviewText.setEditable(false);
        UITheme.styleTextArea(overviewText);
        overviewText.setBackground(Color.WHITE);
        overviewCard.add(overviewText, BorderLayout.CENTER);

        JPanel csvCard = UITheme.createRoundedPanel(new BorderLayout(10, 10));
        csvCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(214, 223, 247), 1, true),
                BorderFactory.createEmptyBorder(18, 18, 18, 18)));

        JLabel csvTitle = new JLabel("Starter CSV Files");
        UITheme.styleLabel(csvTitle, Font.BOLD, 18, UITheme.DEEP_NAVY);
        csvCard.add(csvTitle, BorderLayout.NORTH);

        JTextArea csvText = new JTextArea(
                "- data/users.csv\n- data/slots.csv\n- data/projects.csv\n- data/confirmed_projects.csv\n- data/messages.csv\n- data/teacher_students.csv");
        csvText.setEditable(false);
        UITheme.styleTextArea(csvText);
        csvText.setBackground(Color.WHITE);
        csvCard.add(csvText, BorderLayout.CENTER);

        detailGrid.add(overviewCard);
        detailGrid.add(csvCard);
        workspaceBody.add(detailGrid, BorderLayout.CENTER);

        refreshStats();
        workspaceBody.revalidate();
        workspaceBody.repaint();
    }

    private void addUser() {
        workspaceTitle.setText("Add New User");
        workspaceStatus.setText("Fill details and create user");
        workspaceBody.removeAll();

        JPanel formPanel = UITheme.createRoundedPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(214, 223, 247), 1, true),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.weightx = 0;
        gbc.weighty = 0;
        int row = 0;

        JLabel usernameLabel = new JLabel("Username:");
        UITheme.styleLabel(usernameLabel, Font.PLAIN, 13, UITheme.DEEP_NAVY);
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        formPanel.add(usernameLabel, gbc);

        JTextField usernameField = new JTextField();
        UITheme.styleTextField(usernameField);
        gbc.gridx = 1;
        gbc.weightx = 1;
        formPanel.add(usernameField, gbc);
        row++;
        gbc.weightx = 0;

        JLabel passwordLabel = new JLabel("Password:");
        UITheme.styleLabel(passwordLabel, Font.PLAIN, 13, UITheme.DEEP_NAVY);
        gbc.gridx = 0;
        gbc.gridy = row;
        formPanel.add(passwordLabel, gbc);

        JTextField passwordField = new JTextField();
        UITheme.styleTextField(passwordField);
        gbc.gridx = 1;
        gbc.weightx = 1;
        formPanel.add(passwordField, gbc);
        row++;
        gbc.weightx = 0;

        JLabel roleLabel = new JLabel("Role:");
        UITheme.styleLabel(roleLabel, Font.PLAIN, 13, UITheme.DEEP_NAVY);
        gbc.gridx = 0;
        gbc.gridy = row;
        formPanel.add(roleLabel, gbc);

        String[] roles = {"student", "teacher", "admin"};
        JComboBox<String> roleCombo = new JComboBox<>(roles);
        gbc.gridx = 1;
        gbc.weightx = 1;
        formPanel.add(roleCombo, gbc);
        row++;
        gbc.weightx = 0;

        JLabel areaLabel = new JLabel("Teacher Areas (multi-select):");
        UITheme.styleLabel(areaLabel, Font.PLAIN, 13, UITheme.DEEP_NAVY);
        String[] teacherAreaOptions = {
            "Machine Learning",
            "Data Science",
            "Web Development",
            "Cyber Security",
            "Cloud Computing",
            "Mobile App Development",
            "AI and NLP"
        };
        JList<String> areaList = new JList<>(teacherAreaOptions);
        areaList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        areaList.setVisibleRowCount(5);
        JScrollPane areaScrollPane = new JScrollPane(areaList);
        UITheme.styleScrollPane(areaScrollPane);
        areaScrollPane.setPreferredSize(new Dimension(250, 110));

        JPanel teacherAreaPanel = new JPanel(new BorderLayout(6, 6));
        teacherAreaPanel.setOpaque(false);
        teacherAreaPanel.add(areaLabel, BorderLayout.NORTH);
        teacherAreaPanel.add(areaScrollPane, BorderLayout.CENTER);
        teacherAreaPanel.setVisible(false);
        areaList.setEnabled(false);

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        formPanel.add(teacherAreaPanel, gbc);
        row++;

        JLabel hintLabel = new JLabel("Used only for teacher accounts");
        UITheme.styleLabel(hintLabel, Font.PLAIN, 12, UITheme.MIDNIGHT);
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        formPanel.add(hintLabel, gbc);
        row++;

        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actionsPanel.setOpaque(false);

        JButton generateButton = new JButton("Generate Password");
        UITheme.styleButton(generateButton, UITheme.CYAN, UITheme.DEEP_NAVY);
        generateButton.addActionListener(e -> passwordField.setText(PasswordGenerator.generatePassword(8)));
        actionsPanel.add(generateButton);

        JButton addButton = new JButton("Add User");
        UITheme.styleButton(addButton, UITheme.ROYAL, Color.WHITE);
        actionsPanel.add(addButton);

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        formPanel.add(actionsPanel, gbc);

        roleCombo.addActionListener(e -> {
            boolean teacherSelected = "teacher".equals(roleCombo.getSelectedItem());
            teacherAreaPanel.setVisible(teacherSelected);
            areaList.setEnabled(teacherSelected);
            if (teacherSelected) {
                if (areaList.getSelectedIndices().length == 0) {
                    areaList.setSelectedIndex(0);
                }
            } else {
                areaList.clearSelection();
            }

            formPanel.revalidate();
            formPanel.repaint();
        });

        workspaceBody.add(formPanel, BorderLayout.NORTH);

        addButton.addActionListener(e -> {
            String user = usernameField.getText().trim();
            String pass = passwordField.getText().trim();
            String role = (String) roleCombo.getSelectedItem();
            List<String> selectedAreas = areaList.getSelectedValuesList();

            if (user.isEmpty() || pass.isEmpty()) {
                workspaceStatus.setText("Please fill all required fields");
                return;
            }

            boolean created;
            if ("teacher".equals(role)) {
                if (selectedAreas.isEmpty()) {
                    workspaceStatus.setText("Please select at least one teacher area");
                    return;
                }
                created = DataManager.addTeacher(user, pass, selectedAreas);
            } else {
                created = DataManager.addUser(user, pass, role);
            }

            if (created) {
                workspaceStatus.setText("User added: " + user);
                showHomePanel();
            } else {
                workspaceStatus.setText("User not added. Check duplicate username or invalid data");
            }
            refreshStats();
        });

        workspaceBody.revalidate();
        workspaceBody.repaint();
    }

    private void removeUser() {
        workspaceTitle.setText("Remove User");
        workspaceStatus.setText("Enter a username to remove");
        workspaceBody.removeAll();

        JPanel form = UITheme.createRoundedPanel(new GridLayout(1, 2, 10, 10));
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(214, 223, 247), 1, true),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)));

        JTextField usernameField = new JTextField();
        UITheme.styleTextField(usernameField);
        form.add(usernameField);

        JButton removeButton = new JButton("Remove");
        UITheme.styleButton(removeButton, UITheme.CORAL, Color.WHITE);
        removeButton.addActionListener(e -> {
            String username = usernameField.getText().trim();
            if (username.isEmpty()) {
                workspaceStatus.setText("Please enter a username");
                return;
            }

            if (DataManager.removeUser(username)) {
                workspaceStatus.setText("User removed: " + username);
                showHomePanel();
            } else {
                workspaceStatus.setText("User not found: " + username);
            }
            refreshStats();
        });
        form.add(removeButton);

        workspaceBody.add(form, BorderLayout.NORTH);
        workspaceBody.revalidate();
        workspaceBody.repaint();
    }

    private void changePassword() {
        workspaceTitle.setText("Change User Password");
        workspaceStatus.setText("Enter username and new password");
        workspaceBody.removeAll();

        JPanel formPanel = UITheme.createRoundedPanel(new GridLayout(3, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(214, 223, 247), 1, true),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)));

        JLabel usernameLabel = new JLabel("Username:");
        UITheme.styleLabel(usernameLabel, Font.PLAIN, 13, UITheme.DEEP_NAVY);
        formPanel.add(usernameLabel);
        JTextField usernameField = new JTextField();
        UITheme.styleTextField(usernameField);
        formPanel.add(usernameField);

        JLabel passwordLabel = new JLabel("New Password:");
        UITheme.styleLabel(passwordLabel, Font.PLAIN, 13, UITheme.DEEP_NAVY);
        formPanel.add(passwordLabel);
        JTextField passwordField = new JTextField();
        UITheme.styleTextField(passwordField);
        formPanel.add(passwordField);

        formPanel.add(new JLabel(""));
        JButton generateButton = new JButton("Generate Password");
        UITheme.styleButton(generateButton, UITheme.CYAN, UITheme.DEEP_NAVY);
        generateButton.addActionListener(e -> passwordField.setText(PasswordGenerator.generatePassword(8)));
        formPanel.add(generateButton);

        workspaceBody.add(formPanel, BorderLayout.NORTH);

        JButton changeButton = new JButton("Change Password");
        UITheme.styleButton(changeButton, UITheme.MINT, Color.WHITE);
        changeButton.addActionListener(e -> {
            String user = usernameField.getText().trim();
            String pass = passwordField.getText().trim();

            if (user.isEmpty() || pass.isEmpty()) {
                workspaceStatus.setText("Please fill all fields");
                return;
            }

            if (DataManager.changePassword(user, pass)) {
                workspaceStatus.setText("Password changed for: " + user);
                showHomePanel();
            } else {
                workspaceStatus.setText("User not found: " + user);
            }
            refreshStats();
        });
        workspaceBody.add(changeButton, BorderLayout.SOUTH);

        workspaceBody.revalidate();
        workspaceBody.repaint();
    }

    private void viewAllUsers() {
        workspaceTitle.setText("All Users");
        workspaceBody.removeAll();

        List<String[]> users = DataManager.getAllUsers();
        if (users.isEmpty()) {
            workspaceStatus.setText("No users available");
            workspaceBody.add(createNoticePanel("No users found."), BorderLayout.CENTER);
            workspaceBody.revalidate();
            workspaceBody.repaint();
            return;
        }

        workspaceStatus.setText("Loaded " + users.size() + " user(s)");
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        controls.setOpaque(false);

        JLabel keyLabel = new JLabel("Reveal Key:");
        UITheme.styleLabel(keyLabel, Font.PLAIN, 12, UITheme.MIDNIGHT);
        controls.add(keyLabel);

        JPasswordField keyField = new JPasswordField(8);
        controls.add(keyField);

        JButton unlockButton = new JButton("Unlock Passwords");
        UITheme.styleButton(unlockButton, UITheme.ROYAL, Color.WHITE);
        unlockButton.addActionListener(e -> {
            String entered = new String(keyField.getPassword());
            adminPasswordsUnlocked = PASSWORD_REVEAL_KEY.equals(entered);
            if (adminPasswordsUnlocked) {
                workspaceStatus.setText("Passwords unlocked for this admin session");
            } else {
                workspaceStatus.setText("Invalid key. Passwords remain masked");
            }
            sidebarPasswordState.setText("Passwords: " + (adminPasswordsUnlocked ? "Unlocked" : "Locked"));
            viewAllUsers();
        });
        controls.add(unlockButton);

        JButton lockButton = new JButton("Lock");
        UITheme.styleButton(lockButton, UITheme.CORAL, Color.WHITE);
        lockButton.addActionListener(e -> {
            adminPasswordsUnlocked = false;
            workspaceStatus.setText("Passwords locked");
            sidebarPasswordState.setText("Passwords: Locked");
            viewAllUsers();
        });
        controls.add(lockButton);
        workspaceBody.add(controls, BorderLayout.NORTH);

        String[] columns = {"Username", "Password", "Role"};
        Object[][] data = new Object[users.size()][3];

        for (int i = 0; i < users.size(); i++) {
            String[] user = users.get(i);
            data[i] = new Object[]{
                    user[0],
                    adminPasswordsUnlocked ? user[1] : "xxxx",
                    user[2]
            };
        }

        JTable table = new JTable(data, columns);
        UITheme.styleTable(table);

        JScrollPane scrollPane = new JScrollPane(table);
        UITheme.styleScrollPane(scrollPane);
        workspaceBody.add(scrollPane, BorderLayout.CENTER);

        workspaceBody.revalidate();
        workspaceBody.repaint();
    }

    private void viewAllProjects() {
        workspaceTitle.setText("All Confirmed Projects");
        workspaceBody.removeAll();

        List<String[]> projects = DataManager.getAllProjects();
        if (projects.isEmpty()) {
            workspaceStatus.setText("No confirmed projects");
            workspaceBody.add(createNoticePanel("No confirmed projects yet."), BorderLayout.CENTER);
            workspaceBody.revalidate();
            workspaceBody.repaint();
            return;
        }

        workspaceStatus.setText("Loaded " + projects.size() + " confirmed project(s)");
        String[] columns = {"Student", "Teacher", "Area", "Title", "Description"};
        Object[][] data = new Object[projects.size()][5];

        for (int i = 0; i < projects.size(); i++) {
            data[i] = projects.get(i);
        }

        JTable table = new JTable(data, columns);
        UITheme.styleTable(table);

        JScrollPane scrollPane = new JScrollPane(table);
        UITheme.styleScrollPane(scrollPane);
        workspaceBody.add(scrollPane, BorderLayout.CENTER);

        workspaceBody.revalidate();
        workspaceBody.repaint();
    }

    private JPanel createNoticePanel(String message) {
        JPanel panel = UITheme.createRoundedPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(214, 223, 247), 1, true),
                BorderFactory.createEmptyBorder(18, 18, 18, 18)));
        JLabel notice = new JLabel(message, SwingConstants.CENTER);
        UITheme.styleLabel(notice, Font.PLAIN, 16, UITheme.MIDNIGHT);
        panel.add(notice, BorderLayout.CENTER);
        return panel;
    }

    private void logout() {
        dispose();
        new LoginSystem();
    }

    private void refreshStats() {
        Map<String, Integer> stats = DataManager.getStats();
        if (statsLabel != null) {
            statsLabel.setText("Live CSV sync active. Changes are persisted immediately to the data folder.");
        }
        if (usersMetric != null) {
            usersMetric.setText(String.valueOf(stats.get("users")));
        }
        if (teachersMetric != null) {
            teachersMetric.setText(String.valueOf(stats.get("teachers")));
        }
        if (studentsMetric != null) {
            studentsMetric.setText(String.valueOf(stats.get("students")));
        }
        if (pendingMetric != null) {
            pendingMetric.setText(String.valueOf(stats.get("pendingProjects")));
        }
        if (confirmedMetric != null) {
            confirmedMetric.setText(String.valueOf(stats.get("confirmedProjects")));
        }
        if (sidebarUsersValue != null) {
            sidebarUsersValue.setText(String.valueOf(stats.get("users")));
        }
        if (sidebarTeachersValue != null) {
            sidebarTeachersValue.setText(String.valueOf(stats.get("teachers")));
        }
        if (sidebarStudentsValue != null) {
            sidebarStudentsValue.setText(String.valueOf(stats.get("students")));
        }
        if (sidebarPendingValue != null) {
            sidebarPendingValue.setText(String.valueOf(stats.get("pendingProjects")));
        }
        if (sidebarConfirmedValue != null) {
            sidebarConfirmedValue.setText(String.valueOf(stats.get("confirmedProjects")));
        }
        if (sidebarPasswordState != null) {
            sidebarPasswordState.setText("Passwords: " + (adminPasswordsUnlocked ? "Unlocked" : "Locked"));
        }
    }

    private JButton createTopActionButton(String text, Color background, java.awt.event.ActionListener actionListener) {
        JButton button = new JButton(text);
        UITheme.styleButton(button, background, Color.WHITE);
        button.addActionListener(actionListener);
        return button;
    }

    private JLabel createMetricValueLabel() {
        JLabel label = new JLabel("0", SwingConstants.LEFT);
        UITheme.styleLabel(label, Font.BOLD, 26, UITheme.DEEP_NAVY);
        return label;
    }

    private JPanel createMetricCard(String title, JLabel valueLabel, Color accent) {
        JPanel card = UITheme.createRoundedPanel(new BorderLayout(8, 8));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(214, 223, 247), 1, true),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)));

        JLabel titleLabel = new JLabel(title);
        UITheme.styleLabel(titleLabel, Font.PLAIN, 13, new Color(88, 96, 126));

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        topRow.add(titleLabel, BorderLayout.WEST);

        JPanel accentDot = new JPanel();
        accentDot.setPreferredSize(new Dimension(14, 14));
        accentDot.setBackground(accent);
        accentDot.setBorder(BorderFactory.createLineBorder(accent.darker(), 1, true));
        topRow.add(accentDot, BorderLayout.EAST);

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.add(topRow);
        textPanel.add(Box.createVerticalStrut(8));
        textPanel.add(valueLabel);

        card.add(textPanel, BorderLayout.CENTER);
        return card;
    }

    private JPanel createSidebarMetricRow(String labelText, JLabel valueLabel) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);

        JLabel label = new JLabel(labelText + ":");
        UITheme.styleLabel(label, Font.PLAIN, 12, UITheme.MIDNIGHT);
        row.add(label, BorderLayout.WEST);
        row.add(valueLabel, BorderLayout.EAST);
        return row;
    }

    private JLabel createSidebarValueLabel() {
        JLabel label = new JLabel("0");
        UITheme.styleLabel(label, Font.BOLD, 12, UITheme.DEEP_NAVY);
        return label;
    }

}