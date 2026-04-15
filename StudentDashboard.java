import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class StudentDashboard extends JFrame {
    private final String username;
    private JLabel guideLabel;
    private JLabel areasMetric;
    private JLabel mailMetric;
    private JLabel guideMetric;
    private JLabel sidebarGuideValue;
    private JLabel sidebarAreaValue;
    private JLabel sidebarMailValue;
    private JLabel workspaceTitle;
    private JLabel workspaceStatus;
    private JPanel workspaceBody;

    public StudentDashboard(String username) {
        this.username = username;
        setTitle("Student Dashboard - " + username);
        setSize(1280, 820);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        UITheme.styleFrame(this);
        UITheme.configureFullScreen(this);

        JPanel root = new JPanel(new BorderLayout(18, 18));
        root.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        root.setBackground(UITheme.SOFT_BG);

        root.add(UITheme.createGradientHeader(
                "Student Dashboard",
                "Actions stay on top, guide info stays left, and all features open in the main workspace"),
                BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(18, 18));
        body.setOpaque(false);
        root.add(body, BorderLayout.CENTER);

        JPanel summaryStrip = UITheme.createRoundedPanel(new BorderLayout());
        summaryStrip.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(214, 223, 247), 1, true),
                BorderFactory.createEmptyBorder(12, 18, 12, 18)));
        guideLabel = new JLabel("", SwingConstants.LEFT);
        UITheme.styleLabel(guideLabel, Font.PLAIN, 13, UITheme.MIDNIGHT);
        summaryStrip.add(guideLabel, BorderLayout.CENTER);
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
        leftSummary.add(createSidebarMetricRow("Guide", sidebarGuideValue = createSidebarValueLabel()));
        leftSummary.add(Box.createVerticalStrut(8));
        leftSummary.add(createSidebarMetricRow("Open Areas", sidebarAreaValue = createSidebarValueLabel()));
        leftSummary.add(Box.createVerticalStrut(8));
        leftSummary.add(createSidebarMetricRow("Mailbox", sidebarMailValue = createSidebarValueLabel()));
        leftSummary.add(Box.createVerticalStrut(12));

        JTextArea noteArea = new JTextArea(
            "Browse project areas and submit requests from the top bar. All feature views load in the large center-right workspace.");
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

        JPanel topActionBar = UITheme.createRoundedPanel(new GridLayout(1, 3, 10, 10));
        topActionBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(214, 223, 247), 1, true),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));
        topActionBar.add(createTopActionButton("Browse Project Areas", UITheme.ROYAL, e -> browseAreas()));
        topActionBar.add(createTopActionButton("View Mailbox", UITheme.CYAN, e -> viewMail()));
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

        refreshGuideInfo();
        showHomePanel();

        setContentPane(root);
        setVisible(true);
    }

    private void showHomePanel() {
        workspaceTitle.setText("Workspace Home");
        workspaceStatus.setText("Choose an action from the top bar");
        workspaceBody.removeAll();

        JPanel metricsPanel = new JPanel(new GridLayout(1, 3, 12, 12));
        metricsPanel.setOpaque(false);
        areasMetric = createMetricValueLabel();
        mailMetric = createMetricValueLabel();
        guideMetric = createMetricValueLabel();
        metricsPanel.add(createMetricCard("Available Areas", areasMetric, UITheme.ROYAL));
        metricsPanel.add(createMetricCard("Mailbox", mailMetric, UITheme.MINT));
        metricsPanel.add(createMetricCard("Guide Status", guideMetric, UITheme.AMBER));
        workspaceBody.add(metricsPanel, BorderLayout.NORTH);

        JPanel detailGrid = new JPanel(new GridLayout(1, 2, 16, 16));
        detailGrid.setOpaque(false);

        JPanel browseCard = UITheme.createRoundedPanel(new BorderLayout(10, 10));
        browseCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(214, 223, 247), 1, true),
                BorderFactory.createEmptyBorder(18, 18, 18, 18)));
        JLabel browseTitle = new JLabel("Browse and Submit");
        UITheme.styleLabel(browseTitle, Font.BOLD, 18, UITheme.DEEP_NAVY);
        browseCard.add(browseTitle, BorderLayout.NORTH);

        JTextArea browseText = new JTextArea(
                "Use Browse Project Areas to explore options and submit your request directly in this workspace.");
        browseText.setEditable(false);
        UITheme.styleTextArea(browseText);
        browseText.setBackground(Color.WHITE);
        browseCard.add(browseText, BorderLayout.CENTER);

        JPanel mailboxCard = UITheme.createRoundedPanel(new BorderLayout(10, 10));
        mailboxCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(214, 223, 247), 1, true),
                BorderFactory.createEmptyBorder(18, 18, 18, 18)));
        JLabel mailboxTitle = new JLabel("Mailbox Notes");
        UITheme.styleLabel(mailboxTitle, Font.BOLD, 18, UITheme.DEEP_NAVY);
        mailboxCard.add(mailboxTitle, BorderLayout.NORTH);

        JTextArea mailboxText = new JTextArea(
                "Status updates from teachers appear here and remain saved in the CSV file between runs.");
        mailboxText.setEditable(false);
        UITheme.styleTextArea(mailboxText);
        mailboxText.setBackground(Color.WHITE);
        mailboxCard.add(mailboxText, BorderLayout.CENTER);

        detailGrid.add(browseCard);
        detailGrid.add(mailboxCard);
        workspaceBody.add(detailGrid, BorderLayout.CENTER);

        refreshGuideInfo();
        workspaceBody.revalidate();
        workspaceBody.repaint();
    }

    private void browseAreas() {
        Map<String, List<String>> slots = DataManager.getAvailableSlotsForStudent(username);
        workspaceTitle.setText("Browse Project Areas");
        workspaceBody.removeAll();

        if (slots.isEmpty()) {
            workspaceStatus.setText("No available slots right now");
            workspaceBody.add(createNoticePanel("No available slots at the moment."), BorderLayout.CENTER);
            workspaceBody.revalidate();
            workspaceBody.repaint();
            return;
        }

        workspaceStatus.setText("Select an area to continue");
        workspaceBody.add(createAreaSelectionPanel(slots), BorderLayout.CENTER);
        workspaceBody.revalidate();
        workspaceBody.repaint();
    }

    private JPanel createAreaSelectionPanel(Map<String, List<String>> slots) {
        JPanel container = new JPanel(new BorderLayout(10, 10));
        container.setOpaque(false);

        JLabel label = new JLabel("Available Project Areas");
        UITheme.styleLabel(label, Font.BOLD, 16, UITheme.DEEP_NAVY);
        container.add(label, BorderLayout.NORTH);

        JPanel areaPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        areaPanel.setOpaque(false);

        for (String area : slots.keySet()) {
            JButton areaButton = new JButton(area);
            UITheme.styleButton(areaButton, UITheme.AMBER, UITheme.DEEP_NAVY);
            areaButton.addActionListener(e -> selectTeacher(area, slots.get(area)));
            areaPanel.add(areaButton);
        }

        JScrollPane scrollPane = new JScrollPane(areaPanel);
        UITheme.styleScrollPane(scrollPane);
        container.add(scrollPane, BorderLayout.CENTER);

        return container;
    }

    private void selectTeacher(String area, List<String> teachers) {
        workspaceTitle.setText("Select Teacher - " + area);
        workspaceStatus.setText("Choose a teacher to open submission form");
        workspaceBody.removeAll();

        JPanel container = new JPanel(new BorderLayout(10, 10));
        container.setOpaque(false);

        JLabel label = new JLabel("Available Teachers");
        UITheme.styleLabel(label, Font.BOLD, 16, UITheme.DEEP_NAVY);
        container.add(label, BorderLayout.NORTH);

        JPanel teacherPanel = new JPanel(new GridLayout(0, 1, 10, 10));
        teacherPanel.setOpaque(false);

        for (String teacher : teachers) {
            JButton teacherButton = new JButton(teacher + " (Available)");
            UITheme.styleButton(teacherButton, UITheme.MINT, Color.WHITE);
            teacherButton.addActionListener(e -> showSubmitProjectForm(teacher, area));
            teacherPanel.add(teacherButton);
        }

        JScrollPane scrollPane = new JScrollPane(teacherPanel);
        UITheme.styleScrollPane(scrollPane);
        container.add(scrollPane, BorderLayout.CENTER);

        JButton backButton = new JButton("Back to Areas");
        UITheme.styleButton(backButton, UITheme.MIDNIGHT, Color.WHITE);
        backButton.addActionListener(e -> browseAreas());
        container.add(backButton, BorderLayout.SOUTH);

        workspaceBody.add(container, BorderLayout.CENTER);
        workspaceBody.revalidate();
        workspaceBody.repaint();
    }

    private void showSubmitProjectForm(String teacher, String area) {
        workspaceTitle.setText("Submit Project Request");
        workspaceStatus.setText("Complete the form and submit");
        workspaceBody.removeAll();

        JPanel container = new JPanel(new BorderLayout(12, 12));
        container.setOpaque(false);

        JPanel formPanel = UITheme.createRoundedPanel(new GridLayout(5, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(214, 223, 247), 1, true),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)));

        JLabel studentLabel = new JLabel("Student:");
        UITheme.styleLabel(studentLabel, Font.PLAIN, 13, UITheme.DEEP_NAVY);
        formPanel.add(studentLabel);
        JLabel studentValue = new JLabel(username);
        UITheme.styleLabel(studentValue, Font.BOLD, 13, UITheme.MIDNIGHT);
        formPanel.add(studentValue);

        JLabel teacherLabel = new JLabel("Teacher:");
        UITheme.styleLabel(teacherLabel, Font.PLAIN, 13, UITheme.DEEP_NAVY);
        formPanel.add(teacherLabel);
        JLabel teacherValue = new JLabel(teacher);
        UITheme.styleLabel(teacherValue, Font.BOLD, 13, UITheme.MIDNIGHT);
        formPanel.add(teacherValue);

        JLabel areaLabel = new JLabel("Area:");
        UITheme.styleLabel(areaLabel, Font.PLAIN, 13, UITheme.DEEP_NAVY);
        formPanel.add(areaLabel);
        JLabel areaValue = new JLabel(area);
        UITheme.styleLabel(areaValue, Font.BOLD, 13, UITheme.MIDNIGHT);
        formPanel.add(areaValue);

        JLabel titleLabel = new JLabel("Project Title:");
        UITheme.styleLabel(titleLabel, Font.PLAIN, 13, UITheme.DEEP_NAVY);
        formPanel.add(titleLabel);
        JTextField titleField = new JTextField();
        UITheme.styleTextField(titleField);
        formPanel.add(titleField);

        JLabel descLabel = new JLabel("Description:");
        UITheme.styleLabel(descLabel, Font.PLAIN, 13, UITheme.DEEP_NAVY);
        formPanel.add(descLabel);
        JTextField descField = new JTextField();
        UITheme.styleTextField(descField);
        formPanel.add(descField);

        container.add(formPanel, BorderLayout.CENTER);

        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionRow.setOpaque(false);

        JButton backButton = new JButton("Back to Teachers");
        UITheme.styleButton(backButton, UITheme.MIDNIGHT, Color.WHITE);
        backButton.addActionListener(e -> selectTeacher(area, DataManager.getAvailableSlotsForStudent(username)
                .getOrDefault(area, java.util.Collections.emptyList())));
        actionRow.add(backButton);

        JButton submitButton = new JButton("Submit Request");
        UITheme.styleButton(submitButton, UITheme.ROYAL, Color.WHITE);
        submitButton.addActionListener(e -> {
            String title = titleField.getText().trim();
            String description = descField.getText().trim();

            if (title.isEmpty() || description.isEmpty()) {
                workspaceStatus.setText("Please fill all fields before submitting");
                return;
            }

            if (DataManager.submitProject(username, teacher, area, title, description)) {
                workspaceStatus.setText("Project request submitted successfully");
                showHomePanel();
            } else {
                workspaceStatus.setText("Failed to submit request. Check slot availability or guide rules");
            }
        });
        actionRow.add(submitButton);
        container.add(actionRow, BorderLayout.SOUTH);

        workspaceBody.add(container, BorderLayout.CENTER);
        workspaceBody.revalidate();
        workspaceBody.repaint();
    }

    private void viewMail() {
        List<String> messages = DataManager.getMail(username);
        workspaceTitle.setText("Your Mailbox");
        workspaceStatus.setText("Loaded " + messages.size() + " message(s)");
        workspaceBody.removeAll();

        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        UITheme.styleTextArea(textArea);
        textArea.setMargin(new Insets(10, 10, 10, 10));

        if (messages.isEmpty()) {
            textArea.setText("No messages in your mailbox.");
        } else {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < messages.size(); i++) {
                sb.append(i + 1).append(". ").append(messages.get(i)).append("\n\n");
            }
            textArea.setText(sb.toString());
        }

        JScrollPane scrollPane = new JScrollPane(textArea);
        UITheme.styleScrollPane(scrollPane);
        workspaceBody.add(scrollPane, BorderLayout.CENTER);
        workspaceBody.revalidate();
        workspaceBody.repaint();

        refreshGuideInfo();
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

    private void refreshGuideInfo() {
        String teacher = DataManager.getTeacherForStudent(username);
        int availableAreas = DataManager.getAvailableSlotsForStudent(username).size();
        int mailboxCount = DataManager.getMail(username).size();

        if (teacher == null) {
            guideLabel.setText("Guide assignment: Open (you can choose any available teacher)");
            if (guideMetric != null) {
                guideMetric.setText("Open");
            }
            if (sidebarGuideValue != null) {
                sidebarGuideValue.setText("Open");
            }
        } else {
            guideLabel.setText("Guide assignment: " + teacher + " (showing only this guide's open areas)");
            if (guideMetric != null) {
                guideMetric.setText(teacher);
            }
            if (sidebarGuideValue != null) {
                sidebarGuideValue.setText(teacher);
            }
        }

        if (areasMetric != null) {
            areasMetric.setText(String.valueOf(availableAreas));
        }
        if (mailMetric != null) {
            mailMetric.setText(String.valueOf(mailboxCount));
        }
        if (sidebarAreaValue != null) {
            sidebarAreaValue.setText(String.valueOf(availableAreas));
        }
        if (sidebarMailValue != null) {
            sidebarMailValue.setText(String.valueOf(mailboxCount));
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
        JLabel label = new JLabel("-");
        UITheme.styleLabel(label, Font.BOLD, 12, UITheme.DEEP_NAVY);
        return label;
    }
}