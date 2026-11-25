package Frontend;
import javax.swing.*;
import java.awt.*;

public class MediminderSearchUI extends JFrame {

    public MediminderSearchUI() {
        super("Mediminder - Search");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 580);
        getContentPane().setBackground(new Color(130, 200, 229)); // Theme sky-blue
        setLayout(new BorderLayout(20, 20));

        // 1. Top Bar: Back Button + Pill Search Bar
        JPanel topBar = new JPanel(new BorderLayout(15, 0));
        topBar.setOpaque(false);
        topBar.setBorder(BorderFactory.createEmptyBorder(25, 45, 0, 45));

        JButton backBtn = createPill("\u2190", 45);
        backBtn.setPreferredSize(new Dimension(55, 45));
        backBtn.addActionListener(e -> {
            new MediminderMainMenuUI().setVisible(true);
            this.dispose();
        });

        // Pill-shaped Search Bar
        JPanel searchBarPanel = new JPanel(new BorderLayout(10, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(1, 1, getWidth() - 2, getHeight() - 2, getHeight(), getHeight());
                g2.setColor(new Color(74, 137, 189));
                g2.setStroke(new BasicStroke(1.8f));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, getHeight(), getHeight());
            }
        };
        searchBarPanel.setOpaque(false);
        searchBarPanel.setPreferredSize(new Dimension(500, 45));
        searchBarPanel.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 20));

        JLabel searchIcon = new JLabel("\uD83D\uDD0D");
        searchIcon.setFont(new Font("SansSerif", Font.PLAIN, 16));

        JTextField searchField = new JTextField();
        searchField.setBorder(null);
        searchField.setOpaque(false);
        searchField.setFont(new Font("SansSerif", Font.BOLD, 14));
        searchField.setForeground(new Color(11, 34, 45));
        searchField.setToolTipText("Search based on your medicines and pharmacies");

        searchBarPanel.add(searchIcon, BorderLayout.WEST);
        searchBarPanel.add(searchField, BorderLayout.CENTER);

        topBar.add(backBtn, BorderLayout.WEST);
        topBar.add(searchBarPanel, BorderLayout.CENTER);
        add(topBar, BorderLayout.NORTH);

        // 2. Center: Two Pill Cards + Pill Badges Underneath
        JPanel center = new JPanel(new GridLayout(1, 2, 95, 0));
        center.setOpaque(false);
        center.setBorder(BorderFactory.createEmptyBorder(15, 130, 35, 130));

        // Left Column (Medicine)
        JPanel leftCol = new JPanel(new BorderLayout(0, 15));
        leftCol.setOpaque(false);
        JButton medCard = createPill("Medicine", 180);
        JButton medBadge = createPill("<html><center>MEDICINE YOU ALREADY<br>KNOW!</center></html>", 50);
        medBadge.setPreferredSize(new Dimension(200, 50));
        medBadge.setFont(new Font("SansSerif", Font.BOLD, 12));

        leftCol.add(medCard, BorderLayout.CENTER);
        leftCol.add(medBadge, BorderLayout.SOUTH);

        // Right Column (Pharmacy Nearby)
        JPanel rightCol = new JPanel(new BorderLayout(0, 15));
        rightCol.setOpaque(false);
        JButton pharmCard = createPill("Pharmacy Nearby", 180);
        JButton pharmBadge = createPill("TRACK USING GPS", 50);
        pharmBadge.setPreferredSize(new Dimension(200, 50));
        pharmBadge.setFont(new Font("SansSerif", Font.BOLD, 12));

        rightCol.add(pharmCard, BorderLayout.CENTER);
        rightCol.add(pharmBadge, BorderLayout.SOUTH);

        center.add(leftCol);
        center.add(rightCol);
        add(center, BorderLayout.CENTER);

        setLocationRelativeTo(null);
    }

    // Pure front-end pill helper (no if/else conditions)
    private JButton createPill(String text, int arc) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(Color.WHITE);
                g2.fillRoundRect(1, 1, getWidth() - 2, getHeight() - 2, arc, arc);

                g2.setColor(new Color(74, 137, 189));
                g2.setStroke(new BasicStroke(1.8f));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, arc, arc);

                super.paintComponent(g);
            }
        };
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setFont(new Font("SansSerif", Font.BOLD, 15));
        btn.setForeground(new Color(11, 34, 45));
        return btn;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MediminderSearchUI().setVisible(true));
    }
}