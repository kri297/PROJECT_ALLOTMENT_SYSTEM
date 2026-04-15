import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class TeacherDashboard extends JFrame {
    private static final String PASSWORD_REVEAL_KEY = "2006";
    private final String username;
    private JLabel summaryLabel;
    private JLabel studentsMetric;
    private JLabel pendingMetric;
    private JLabel slotsMetric;
    private JLabel sidebarStudentsValue;
    private JLabel sidebarPendingValue;
    private JLabel sidebarSlotsValue;
    private JLabel sidebarPasswordState;
    private boolean teacherPasswordsUnlocked;
    private JLabel workspaceTitle;
    private JLabel workspaceStatus;
    private JPanel workspaceBody;

    public TeacherDashboard(String username) {
        this.username = username;
        setTitle("Teacher Dashboard - " + username);
        setSize(1280, 820);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        UITheme.styleFrame(this);
        UITheme.configureFullScreen(this);

        JPanel root = new JPanel(new BorderLayout(18, 18));
        root.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        root.setBackground(UITheme.SOFT_BG);

        root.add(UITheme.createGradientHeader(
                "Teacher Dashboard",
                "Top actions, left guide panel, and no popup screens for core features"),
                BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(18, 18));
        body.setOpaque(false);
        root.add(body, BorderLayout.CENTER);

        JPanel summaryStrip = UITheme.createRoundedPanel(new BorderLayout());
        summaryStrip.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(214, 223, 247), 1, true),
                BorderFactory.createEmptyBorder(12, 18, 12, 18)));
        summaryLabel = new JLabel("", SwingConstants.LEFT);
        UITheme.styleLabel(summaryLabel, Font.PLAIN, 13, UITheme.MIDNIGHT);
        summaryStrip.add(summaryLabel, BorderLayout.CENTER);
        body.add(summaryStrip, BorderLayout.NORTH);

        JPanel mainArea = new JPanel(new BorderLayout(18, 18));
        mainArea.setOpaque(false);
        body.add(mainArea, BorderLayout.CENTER);

        JPanel sidebar = UITheme.createRoundedPanel(new BorderLayout(12, 12));
        sidebar.setPreferredSize(new Dimension(300, 0));
        sidebar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(214, 223, 247), 1, true),
                BorderFactory.createEmptyBorder(18, 18, 18, 18)));

        JLabel sidebarTitle = new JLabel("Guide Panel");
        UITheme.styleLabel(sidebarTitle, Font.BOLD, 18, UITheme.DEEP_NAVY);
        sidebar.add(sidebarTitle, BorderLayout.NORTH);

        JPanel leftSummary = new JPanel();
        leftSummary.setLayout(new BoxLayout(leftSummary, BoxLayout.Y_AXIS));
        leftSummary.setOpaque(false);
        leftSummary.add(createSidebarMetricRow("My Students", sidebarStudentsValue = createSidebarValueLabel()));
        leftSummary.add(Box.createVerticalStrut(8));
        leftSummary.add(createSidebarMetricRow("Pending", sidebarPendingValue = createSidebarValueLabel()));
        leftSummary.add(Box.createVerticalStrut(8));
        leftSummary.add(createSidebarMetricRow("Open Slots", sidebarSlotsValue = createSidebarValueLabel()));
        leftSummary.add(Box.createVerticalStrut(12));

        sidebarPasswordState = new JLabel("Passwords: Locked");
        UITheme.styleLabel(sidebarPasswordState, Font.BOLD, 12, UITheme.MIDNIGHT);
        leftSummary.add(sidebarPasswordState);
        leftSummary.add(Box.createVerticalStrut(12));

        JTextArea noteArea = new JTextArea(
            "Student passwords are masked as xxxx in My Students. Enter key 2006 on that screen to reveal.");
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

        JPanel topActionBar = UITheme.createRoundedPanel(new GridLayout(1, 4, 10, 10));
        topActionBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(214, 223, 247), 1, true),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));
        topActionBar.add(createTopActionButton("View Pending Requests", UITheme.ROYAL, e -> viewPendingRequests()));
        topActionBar.add(createTopActionButton("Add Student", UITheme.MINT, e -> addStudent()));
        topActionBar.add(createTopActionButton("View My Students", UITheme.AMBER, e -> viewMyStudents()));
        topActionBar.add(createTopActionButton("Logout", UITheme.CORAL, e -> logout()));
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
        refreshSummary();

        setContentPane(root);
        setVisible(true);
    }

    private void showHomePanel() {
        workspaceTitle.setText("Workspace Home");
        workspaceStatus.setText("Choose an action from the top bar");
        workspaceBody.removeAll();

        JPanel metricsPanel = new JPanel(new GridLayout(1, 3, 12, 12));
        metricsPanel.setOpaque(false);
        studentsMetric = createMetricValueLabel();
        pendingMetric = createMetricValueLabel();
        slotsMetric = createMetricValueLabel();
        metricsPanel.add(createMetricCard("My Students", studentsMetric, UITheme.MINT));
        metricsPanel.add(createMetricCard("Pending Reviews", pendingMetric, UITheme.AMBER));
        metricsPanel.add(createMetricCard("Open Slots", slotsMetric, UITheme.ROYAL));
        workspaceBody.add(metricsPanel, BorderLayout.NORTH);

        JPanel detailGrid = new JPanel(new GridLayout(1, 2, 16, 16));
        detailGrid.setOpaque(false);

        JPanel workflowCard = UITheme.createRoundedPanel(new BorderLayout(10, 10));
        workflowCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(214, 223, 247), 1, true),
                BorderFactory.createEmptyBorder(18, 18, 18, 18)));
        JLabel workflowTitle = new JLabel("Workflow Notes");
        UITheme.styleLabel(workflowTitle, Font.BOLD, 18, UITheme.DEEP_NAVY);
        workflowCard.add(workflowTitle, BorderLayout.NORTH);

        JTextArea workflowText = new JTextArea(
                "Review pending proposals, add new students, and check your student list without leaving this screen.");
        workflowText.setEditable(false);
        UITheme.styleTextArea(workflowText);
        workflowText.setBackground(Color.WHITE);
        workflowCard.add(workflowText, BorderLayout.CENTER);

        JPanel mapCard = UITheme.createRoundedPanel(new BorderLayout(10, 10));
        mapCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(214, 223, 247), 1, true),
                BorderFactory.createEmptyBorder(18, 18, 18, 18)));
        JLabel mapTitle = new JLabel("Guide Summary");
        UITheme.styleLabel(mapTitle, Font.BOLD, 18, UITheme.DEEP_NAVY);
        mapCard.add(mapTitle, BorderLayout.NORTH);

        JTextArea mapText = new JTextArea(
                "The left side stays fixed as a panel. Use the top bar to load full feature views in the center-right workspace.");
        mapText.setEditable(false);
        UITheme.styleTextArea(mapText);
        mapText.setBackground(Color.WHITE);
        mapCard.add(mapText, BorderLayout.CENTER);

        detailGrid.add(workflowCard);
        detailGrid.add(mapCard);
        workspaceBody.add(detailGrid, BorderLayout.CENTER);

        refreshSummary();
        workspaceBody.revalidate();
        workspaceBody.repaint();
    }

    private void viewPendingRequests() {
        List<String[]> projects = DataManager.getPendingProjects(username);
        workspaceTitle.setText("Pending Project Requests");
        workspaceBody.removeAll();

        if (projects.isEmpty()) {
            workspaceStatus.setText("No pending requests");
            workspaceBody.add(createNoticePanel("No pending requests at the moment."), BorderLayout.CENTER);
            workspaceBody.revalidate();
            workspaceBody.repaint();
            refreshSummary();
            return;
        }

        workspaceStatus.setText("Loaded " + projects.size() + " pending request(s)");
        JPanel projectsPanel = new JPanel();
        projectsPanel.setLayout(new BoxLayout(projectsPanel, BoxLayout.Y_AXIS));
        projectsPanel.setOpaque(false);

        for (String[] project : projects) {
            JPanel projectCard = createProjectCard(project);
            projectsPanel.add(projectCard);
            projectsPanel.add(Box.createVerticalStrut(10));
        }

        JScrollPane scrollPane = new JScrollPane(projectsPanel);
        UITheme.styleScrollPane(scrollPane);
        workspaceBody.add(scrollPane, BorderLayout.CENTER);
        workspaceBody.revalidate();
        workspaceBody.repaint();
    }

    private JPanel createProjectCard(String[] project) {
        JPanel card = UITheme.createRoundedPanel(new BorderLayout(10, 10));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(214, 223, 247), 1, true),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));

        JPanel detailsPanel = new JPanel(new GridLayout(4, 1, 5, 5));
        detailsPanel.setOpaque(false);

        JLabel studentLabel = new JLabel("Student: " + project[0]);
        UITheme.styleLabel(studentLabel, Font.BOLD, 12, UITheme.DEEP_NAVY);
        detailsPanel.add(studentLabel);

        JLabel areaLabel = new JLabel("Area: " + project[2]);
        UITheme.styleLabel(areaLabel, Font.PLAIN, 12, UITheme.MIDNIGHT);
        detailsPanel.add(areaLabel);

        JLabel titleLabel = new JLabel("Title: " + project[3]);
        UITheme.styleLabel(titleLabel, Font.BOLD, 12, UITheme.DEEP_NAVY);
        detailsPanel.add(titleLabel);

        JLabel descLabel = new JLabel("Description: " + project[4]);
        UITheme.styleLabel(descLabel, Font.PLAIN, 12, UITheme.MIDNIGHT);
        detailsPanel.add(descLabel);

        card.add(detailsPanel, BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new BorderLayout(10, 10));
        actionPanel.setOpaque(false);

        JTextField reasonField = new JTextField();
        UITheme.styleTextField(reasonField);
        reasonField.setToolTipText("Optional reason used when rejecting");
        actionPanel.add(reasonField, BorderLayout.CENTER);

        JPanel buttonsPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        buttonsPanel.setOpaque(false);

        JButton approveButton = new JButton("Approve");
        UITheme.styleButton(approveButton, UITheme.MINT, Color.WHITE);
        approveButton.addActionListener(e -> approveProject(project));
        buttonsPanel.add(approveButton);

        JButton rejectButton = new JButton("Reject");
        UITheme.styleButton(rejectButton, UITheme.CORAL, Color.WHITE);
        rejectButton.addActionListener(e -> rejectProject(project, reasonField.getText().trim()));
        buttonsPanel.add(rejectButton);

        actionPanel.add(buttonsPanel, BorderLayout.EAST);
        card.add(actionPanel, BorderLayout.SOUTH);
        return card;
    }

    private void approveProject(String[] project) {
        boolean approved = DataManager.approveProject(project[0], project[1], project[2], project[3], project[4]);
        if (approved) {
            workspaceStatus.setText("Project approved for " + project[0]);
        } else {
            workspaceStatus.setText("Unable to approve project for " + project[0]);
        }
        viewPendingRequests();
        refreshSummary();
    }

    private void rejectProject(String[] project, String reason) {
        String safeReason = reason.isEmpty() ? "No reason provided" : reason;
        boolean rejected = DataManager.rejectProject(project[0], project[1], safeReason);
        if (rejected) {
            workspaceStatus.setText("Project rejected for " + project[0]);
        } else {
            workspaceStatus.setText("Unable to reject project for " + project[0]);
        }
        viewPendingRequests();
        refreshSummary();
    }

    private void addStudent() {
        workspaceTitle.setText("Add Student");
        workspaceStatus.setText("Create a student account below");
        workspaceBody.removeAll();

        JPanel formPanel = UITheme.createRoundedPanel(new GridLayout(3, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(214, 223, 247), 1, true),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)));

        JLabel usernameLabel = new JLabel("Student Username:");
        UITheme.styleLabel(usernameLabel, Font.PLAIN, 13, UITheme.DEEP_NAVY);
        formPanel.add(usernameLabel);
        JTextField usernameField = new JTextField();
        UITheme.styleTextField(usernameField);
        formPanel.add(usernameField);

        JLabel passwordLabel = new JLabel("Password:");
        UITheme.styleLabel(passwordLabel, Font.PLAIN, 13, UITheme.DEEP_NAVY);
        formPanel.add(passwordLabel);
        JTextField passwordField = new JTextField(PasswordGenerator.generatePassword(8));
        UITheme.styleTextField(passwordField);
        formPanel.add(passwordField);

        JButton generateButton = new JButton("Generate Password");
        UITheme.styleButton(generateButton, UITheme.CYAN, UITheme.DEEP_NAVY);
        generateButton.addActionListener(e -> passwordField.setText(PasswordGenerator.generatePassword(8)));
        formPanel.add(generateButton);

        JButton createButton = new JButton("Create Student");
        UITheme.styleButton(createButton, UITheme.ROYAL, Color.WHITE);
        createButton.addActionListener(e -> {
            String student = usernameField.getText().trim();
            String password = passwordField.getText().trim();

            if (student.isEmpty() || password.isEmpty()) {
                workspaceStatus.setText("Please fill all fields");
                return;
            }

            boolean created = DataManager.addStudentByTeacher(username, student, password);
            if (created) {
                workspaceStatus.setText("Student created: " + student);
                showHomePanel();
            } else {
                workspaceStatus.setText("Could not add student. Username may already exist");
            }
            refreshSummary();
        });
        formPanel.add(createButton);

        workspaceBody.add(formPanel, BorderLayout.NORTH);
        workspaceBody.revalidate();
        workspaceBody.repaint();
    }

    private void viewMyStudents() {
        List<String[]> students = DataManager.getStudentsOfTeacher(username);
        workspaceTitle.setText("My Students");
        workspaceBody.removeAll();

        if (students.isEmpty()) {
            workspaceStatus.setText("No students assigned");
            workspaceBody.add(createNoticePanel("No students assigned yet."), BorderLayout.CENTER);
            workspaceBody.revalidate();
            workspaceBody.repaint();
            refreshSummary();
            return;
        }

        workspaceStatus.setText("Loaded " + students.size() + " student(s)");
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
            teacherPasswordsUnlocked = PASSWORD_REVEAL_KEY.equals(entered);
            if (teacherPasswordsUnlocked) {
                workspaceStatus.setText("Passwords unlocked for this teacher session");
            } else {
                workspaceStatus.setText("Invalid key. Passwords remain masked");
            }
            sidebarPasswordState.setText("Passwords: " + (teacherPasswordsUnlocked ? "Unlocked" : "Locked"));
            viewMyStudents();
        });
        controls.add(unlockButton);

        JButton lockButton = new JButton("Lock");
        UITheme.styleButton(lockButton, UITheme.CORAL, Color.WHITE);
        lockButton.addActionListener(e -> {
            teacherPasswordsUnlocked = false;
            workspaceStatus.setText("Passwords locked");
            sidebarPasswordState.setText("Passwords: Locked");
            viewMyStudents();
        });
        controls.add(lockButton);
        workspaceBody.add(controls, BorderLayout.NORTH);

        String[] columns = {"Student", "Password"};
        Object[][] data = new Object[students.size()][2];
        for (int i = 0; i < students.size(); i++) {
            String[] student = students.get(i);
            data[i] = new Object[]{
                    student[0],
                    teacherPasswordsUnlocked ? student[1] : "xxxx"
            };
        }

        JTable table = new JTable(data, columns);
        UITheme.styleTable(table);
        JScrollPane scrollPane = new JScrollPane(table);
        UITheme.styleScrollPane(scrollPane);
        workspaceBody.add(scrollPane, BorderLayout.CENTER);
        workspaceBody.revalidate();
        workspaceBody.repaint();
        refreshSummary();
    }

    private void refreshSummary() {
        int pendingCount = DataManager.getPendingProjects(username).size();
        int studentCount = DataManager.getStudentsOfTeacher(username).size();
        int openSlots = countOpenSlotsForTeacher();

        if (summaryLabel != null) {
            summaryLabel.setText("Live CSV sync active. My students: " + studentCount
                    + " | Pending requests: " + pendingCount
                    + " | Open slots: " + openSlots);
        }

        if (studentsMetric != null) {
            studentsMetric.setText(String.valueOf(studentCount));
        }
        if (pendingMetric != null) {
            pendingMetric.setText(String.valueOf(pendingCount));
        }
        if (slotsMetric != null) {
            slotsMetric.setText(String.valueOf(openSlots));
        }
        if (sidebarStudentsValue != null) {
            sidebarStudentsValue.setText(String.valueOf(studentCount));
        }
        if (sidebarPendingValue != null) {
            sidebarPendingValue.setText(String.valueOf(pendingCount));
        }
        if (sidebarSlotsValue != null) {
            sidebarSlotsValue.setText(String.valueOf(openSlots));
        }
        if (sidebarPasswordState != null) {
            sidebarPasswordState.setText("Passwords: " + (teacherPasswordsUnlocked ? "Unlocked" : "Locked"));
        }
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

    private int countOpenSlotsForTeacher() {
        int count = 0;
        Map<String, List<String>> availableSlots = DataManager.getAvailableSlots();
        for (List<String> teachers : availableSlots.values()) {
            for (String teacher : teachers) {
                if (username.equalsIgnoreCase(teacher)) {
                    count++;
                }
            }
        }
        return count;
    }

    private void logout() {
        dispose();
        new LoginSystem();
    }
}