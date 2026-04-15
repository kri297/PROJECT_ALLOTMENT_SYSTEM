import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;

public final class UITheme {
    public static final Color DEEP_NAVY = new Color(18, 24, 52);
    public static final Color MIDNIGHT = new Color(28, 43, 92);
    public static final Color ROYAL = new Color(67, 97, 238);
    public static final Color CYAN = new Color(72, 202, 228);
    public static final Color MINT = new Color(77, 201, 176);
    public static final Color AMBER = new Color(255, 184, 0);
    public static final Color CORAL = new Color(255, 107, 107);
    public static final Color SOFT_BG = new Color(243, 247, 255);
    public static final Color CARD_BG = Color.WHITE;

    private UITheme() {
    }

    public static void applyGlobalLookAndFeel() {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception ignored) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignoredAgain) {
                // Keep default look and feel if the platform look and feel is unavailable.
            }
        }
    }

    public static JPanel createGradientHeader(String title, String subtitle) {
        JPanel header = new GradientPanel(new Color[]{DEEP_NAVY, ROYAL, CYAN});
        header.setLayout(new BorderLayout());
        header.setBorder(new EmptyBorder(18, 22, 18, 22));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.add(titleLabel);

        if (subtitle != null && !subtitle.trim().isEmpty()) {
            JLabel subtitleLabel = new JLabel(subtitle);
            subtitleLabel.setForeground(new Color(235, 243, 255));
            subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
            subtitleLabel.setBorder(new EmptyBorder(4, 0, 0, 0));
            textPanel.add(subtitleLabel);
        }

        header.add(textPanel, BorderLayout.WEST);
        return header;
    }

    public static JPanel createBodyPanel() {
        JPanel body = new JPanel(new BorderLayout(12, 12));
        body.setBackground(SOFT_BG);
        body.setBorder(new EmptyBorder(18, 18, 18, 18));
        return body;
    }

    public static JPanel createCardPanel(LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setBackground(CARD_BG);
        panel.setBorder(new CompoundBorder(
                new LineBorder(new Color(214, 223, 247), 1, true),
                new EmptyBorder(16, 16, 16, 16)));
        return panel;
    }

    public static JPanel createRoundedPanel(LayoutManager layout) {
        return new RoundedPanel(layout, Color.WHITE, new Color(214, 223, 247), 28);
    }

    public static JPanel createMetricCard(String title, String value, Color accent) {
        JPanel card = createRoundedPanel(new BorderLayout());
        card.setBorder(new CompoundBorder(
                new LineBorder(accent, 2, true),
                new EmptyBorder(16, 18, 16, 18)));

        JLabel titleLabel = new JLabel(title);
        styleLabel(titleLabel, Font.PLAIN, 12, new Color(88, 96, 126));

        JLabel valueLabel = new JLabel(value);
        styleLabel(valueLabel, Font.BOLD, 24, DEEP_NAVY);

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.add(titleLabel);
        textPanel.add(Box.createVerticalStrut(6));
        textPanel.add(valueLabel);

        JPanel accentBar = new JPanel();
        accentBar.setPreferredSize(new Dimension(18, 18));
        accentBar.setBackground(accent);
        accentBar.setBorder(new LineBorder(accent.darker(), 1, true));

        card.add(textPanel, BorderLayout.CENTER);
        card.add(accentBar, BorderLayout.EAST);
        return card;
    }

    public static void configureFullScreen(JFrame frame) {
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setMinimumSize(new Dimension(1100, 720));
    }

    public static void styleButton(JButton button, Color background, Color foreground) {
        button.setBackground(background);
        button.setForeground(foreground);
        button.setFocusPainted(false);
        button.setFont(new Font("SansSerif", Font.BOLD, 13));
        button.setBorder(new CompoundBorder(
                new LineBorder(background.darker(), 1, true),
                new EmptyBorder(8, 14, 8, 14)));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setOpaque(true);
    }

    public static void styleTextField(JTextField field) {
        field.setFont(new Font("SansSerif", Font.PLAIN, 14));
        field.setBorder(new CompoundBorder(
                new LineBorder(new Color(196, 206, 235), 1, true),
                new EmptyBorder(8, 10, 8, 10)));
    }

    public static void stylePasswordField(JPasswordField field) {
        styleTextField(field);
    }

    public static void styleLabel(JLabel label, int style, int size, Color color) {
        label.setFont(new Font("SansSerif", style, size));
        label.setForeground(color);
    }

    public static void styleTable(JTable table) {
        table.setRowHeight(26);
        table.setShowHorizontalLines(false);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFillsViewportHeight(true);
        table.setBackground(Color.WHITE);
        table.setSelectionBackground(new Color(215, 231, 255));
        table.setSelectionForeground(Color.BLACK);

        JTableHeader header = table.getTableHeader();
        header.setBackground(ROYAL);
        header.setForeground(Color.WHITE);
        header.setFont(new Font("SansSerif", Font.BOLD, 13));
        header.setReorderingAllowed(false);

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setBorder(new EmptyBorder(0, 8, 0, 8));
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }
    }

    public static void styleTextArea(JTextArea area) {
        area.setFont(new Font("SansSerif", Font.PLAIN, 13));
        area.setBackground(Color.WHITE);
        area.setBorder(new EmptyBorder(12, 12, 12, 12));
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
    }

    public static void styleScrollPane(JScrollPane scrollPane) {
        scrollPane.setBorder(new LineBorder(new Color(214, 223, 247), 1, true));
        scrollPane.getViewport().setBackground(Color.WHITE);
    }

    public static void styleDialog(JDialog dialog) {
        dialog.getContentPane().setBackground(SOFT_BG);
    }

    public static void styleFrame(JFrame frame) {
        frame.getContentPane().setBackground(SOFT_BG);
    }

    public static Border sectionBorder() {
        return new CompoundBorder(
                new LineBorder(new Color(214, 223, 247), 1, true),
                new EmptyBorder(14, 14, 14, 14));
    }

    private static final class GradientPanel extends JPanel {
        private final Color[] colors;

        private GradientPanel(Color[] colors) {
            this.colors = colors;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int width = getWidth();
            int height = getHeight();
            GradientPaint base = new GradientPaint(0, 0, colors[0], width, height, colors[1]);
            g2.setPaint(base);
            g2.fillRect(0, 0, width, height);
            g2.setColor(new Color(colors[2].getRed(), colors[2].getGreen(), colors[2].getBlue(), 80));
            g2.fillOval(width - 180, -70, 260, 260);
            g2.fillOval(-60, height - 120, 220, 220);
            g2.dispose();
        }
    }

    private static final class RoundedPanel extends JPanel {
        private final Color fillColor;
        private final Color borderColor;
        private final int arc;

        private RoundedPanel(LayoutManager layout, Color fillColor, Color borderColor, int arc) {
            super(layout);
            this.fillColor = fillColor;
            this.borderColor = borderColor;
            this.arc = arc;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(fillColor);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
            g2.setColor(borderColor);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}