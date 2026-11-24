import javax.swing.*;
import java.awt.*;


public class MediminderMainMenuUI extends JFrame {
    public MediminderMainMenuUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 580);
        getContentPane().setBackground(new Color(130, 200, 229));
        setLayout(new BorderLayout(0, 20));

        // 1. Title
        JLabel title = new JLabel("MediMenu", SwingConstants.CENTER);
        title.setFont(new Font("Serif", Font.BOLD, 36));
        title.setBorder(BorderFactory.createEmptyBorder(25, 0, 0, 0));
        add(title, BorderLayout.NORTH);

        // 2. Center Pill Buttons
        JPanel center = new JPanel(new GridLayout(1, 3, 45, 0));
        center.setOpaque(false);
        center.setBorder(BorderFactory.createEmptyBorder(10, 50, 10, 50));

        JButton searchButton = createPillButton("SEARCH");
        searchButton.addActionListener(e -> {
            new MediminderSearchUI().setVisible(true);
            this.dispose();
        });
        center.add(searchButton);
        center.add(createPillButton("ADD NOTES"));
        center.add(createPillButton("MEDICINE OR PHARMACIES"));
        add(center, BorderLayout.CENTER);

        // 3. Bottom Bar
        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.setBorder(BorderFactory.createEmptyBorder(0, 0, 25, 0));
        
        JButton bar = new JButton("INCLUDE WIDGET IN YOUR DESKTOP");
        bar.setPreferredSize(new Dimension(550, 45));
        bar.setBackground(Color.WHITE);
        bottom.add(bar);
        add(bottom, BorderLayout.SOUTH);

        setLocationRelativeTo(null);
    }
    
    // Helper method to make any JButton pill-shaped
private JButton createPillButton(String text) {
    JButton btn = new JButton(text) {
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int arc = Math.min(getWidth(), getHeight());

            // 1. Fill color based on state (Pressed -> Hover -> Normal)
            if (getModel().isPressed()) {
                g2.setColor(new Color(185, 224, 242)); // Deeper sky-blue when clicked
            } else if (getModel().isRollover()) {
                g2.setColor(new Color(224, 243, 252)); // Soft icy-blue tint on hover
            } else {
                g2.setColor(Color.WHITE);              // Default white
            }
            g2.fillRoundRect(1, 1, getWidth() - 2, getHeight() - 2, arc, arc);

            // 2. Border color (bold darker blue on hover)
            if (getModel().isRollover()) {
                g2.setColor(new Color(33, 97, 140));
                g2.setStroke(new BasicStroke(2.5f));
            } else {
                g2.setColor(new Color(74, 137, 189));
                g2.setStroke(new BasicStroke(1.5f));
            }
            g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, arc, arc);

            super.paintComponent(g);
        }
    };
    btn.setRolloverEnabled(true); // Ensures hover detection is active
    btn.setContentAreaFilled(false);
    btn.setBorderPainted(false);
    btn.setFocusPainted(false);
    btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    btn.setFont(new Font("SansSerif", Font.BOLD, 14));
    btn.setForeground(new Color(11, 34, 45)); // Dark navy text matching your theme
    return btn;
}
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MediminderMainMenuUI().setVisible(true));
    }
}

