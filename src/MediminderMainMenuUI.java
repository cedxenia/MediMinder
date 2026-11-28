import javax.swing.*;
import java.awt.*;
import java.util.function.Supplier;

public class MediminderMainMenuUI extends JFrame {
    public MediminderMainMenuUI() {
        super("MediMinder");
        MediminderReminderService.start();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 580);
        getContentPane().setBackground(new Color(130, 200, 229));
        setLayout(new BorderLayout(0, 20));

        String userName = MediminderStore.getUserName();
        JLabel title = new JLabel(userName.isEmpty() ? "MediMinder" : "Welcome, " + userName,
            SwingConstants.CENTER);
        title.setFont(new Font("Serif", Font.BOLD, 36));
        title.setBorder(BorderFactory.createEmptyBorder(25, 0, 0, 0));
        add(title, BorderLayout.NORTH);

        JPanel center = new JPanel(new GridLayout(1, 3, 35, 0));
        center.setOpaque(false);
        center.setBorder(BorderFactory.createEmptyBorder(10, 40, 10, 40));
        addPillButton(center, "MEDICINES & REMINDERS", MediminderReminderUI::new);
        addPillButton(center, "NOTES & DOCTOR SUMMARY", MediminderNotesUI::new);
        addPillButton(center, "MEDICINE / PHARMACY SEARCH", MediminderSearchUI::new);
        add(center, BorderLayout.CENTER);

        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.setBorder(BorderFactory.createEmptyBorder(0, 0, 25, 0));
        JButton lowSupply = new JButton("CHECK MEDICINES THAT NEED REFILLS");
        lowSupply.setPreferredSize(new Dimension(550, 45));
        lowSupply.setBackground(Color.WHITE);
        lowSupply.addActionListener(event -> showLowSupply());
        JButton changeName = new JButton("Change name");
        changeName.setBackground(Color.WHITE);
        changeName.addActionListener(event -> open(new MediMinderLogin()));
        bottom.add(lowSupply);
        bottom.add(changeName);
        add(bottom, BorderLayout.SOUTH);
        setLocationRelativeTo(null);
    }

    private void open(JFrame window) {
        window.setVisible(true);
        dispose();
    }

    private void showLowSupply() {
        StringBuilder message = new StringBuilder();
        for (MediminderStore.Medicine medicine : MediminderStore.getMedicines()) {
            int days = MediminderStore.getSupplyDays(medicine);
            int threshold = MediminderStore.getLowSupplyThreshold(medicine);
            if (days <= threshold) {
                message.append(medicine.name).append(": ")
                    .append(medicine.quantityRemaining).append(" doses left (~")
                    .append(days).append(" days). Refill threshold: ")
                    .append(threshold).append(" days.\n");
            }
        }
        if (message.length() == 0) {
            message.append("No saved medicines are currently below their refill threshold.");
        }
        JOptionPane.showMessageDialog(this, message.toString(),
            "Low supply overview", JOptionPane.INFORMATION_MESSAGE);
    }

    private void addPillButton(JPanel panel, String text, Supplier<JFrame> window) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
                int arc = Math.min(getWidth(), getHeight());
                g.setColor(getModel().isPressed() ? new Color(185, 224, 242)
                    : getModel().isRollover() ? new Color(224, 243, 252) : Color.WHITE);
                g.fillRoundRect(1, 1, getWidth() - 2, getHeight() - 2, arc, arc);
                g.setColor(new Color(74, 137, 189));
                g.setStroke(new BasicStroke(getModel().isRollover() ? 2.5f : 1.5f));
                g.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, arc, arc);
                g.dispose();
                super.paintComponent(graphics);
            }
        };
        button.setRolloverEnabled(true);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setFont(new Font("SansSerif", Font.BOLD, 13));
        button.setForeground(new Color(11, 34, 45));
        button.addActionListener(event -> open(window.get()));
        panel.add(button);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MediminderMainMenuUI().setVisible(true));
    }
}
