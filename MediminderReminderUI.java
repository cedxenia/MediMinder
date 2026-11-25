import javax.swing.*;
import java.awt.*;

public class MediminderReminderUI extends JFrame {

    public MediminderReminderUI() {
        super("Mediminder - Medicine & Pharmacies");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 580);
        getContentPane().setBackground(new Color(130, 200, 229)); // Theme sky-blue
        setLayout(new BorderLayout(0, 15));

        // 1. Top Bar: Back Button + Page Title
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        topBar.setOpaque(false);
        topBar.setBorder(BorderFactory.createEmptyBorder(25, 45, 0, 45));

        JButton backBtn = new JButton("\u2190");
        backBtn.setFont(new Font("SansSerif", Font.BOLD, 16));
        backBtn.setBackground(Color.WHITE);
        backBtn.setFocusPainted(false);
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.addActionListener(e -> {
            new MediminderMainMenuUI().setVisible(true);
            this.dispose();
        });

        JLabel title = new JLabel("MEDICINE & PHARMACIES");
        title.setFont(new Font("Serif", Font.BOLD, 28));
        title.setForeground(new Color(11, 34, 45));

        topBar.add(backBtn);
        topBar.add(title);
        add(topBar, BorderLayout.NORTH);

        // 2. Center: Two Columns (5 rows each, matching your Page 5 mockup)
        JPanel grid = new JPanel(new GridLayout(5, 2, 75, 18));
        grid.setOpaque(false);
        grid.setBorder(BorderFactory.createEmptyBorder(15, 110, 45, 110));

        for (int i = 0; i < 5; i++) {
            grid.add(createItemRow("MEDICINE", "Dosage / Time / Location"));
            grid.add(createItemRow("MEDICINE", "Will alarm in XX:XX"));
        }

        add(grid, BorderLayout.CENTER);
        setLocationRelativeTo(null);
    }

    // Simple helper for each icon + two-line label item
    private JPanel createItemRow(String heading, String subtext) {
        JPanel row = new JPanel(new BorderLayout(15, 0));
        row.setOpaque(false);

        // Circular badge icon on the left
        JLabel icon = new JLabel("💊", SwingConstants.CENTER);
        icon.setPreferredSize(new Dimension(48, 48));
        icon.setOpaque(true);
        icon.setBackground(Color.WHITE);
        icon.setBorder(BorderFactory.createLineBorder(new Color(74, 137, 189), 2));

        // Two-line text on the right
        JLabel text = new JLabel(
            "<html><b style='font-size:11px; color:#0b222d;'>" + heading + "</b><br>" +
            "<span style='font-size:13px; color:#0b222d;'>" + subtext + "</span></html>"
        );

        row.add(icon, BorderLayout.WEST);
        row.add(text, BorderLayout.CENTER);
        return row;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MediminderReminderUI().setVisible(true));
    }
}

