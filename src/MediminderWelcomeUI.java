import javax.swing.*;
import java.awt.*;

public class MediminderWelcomeUI extends JFrame {

    public MediminderWelcomeUI() {
        setTitle("Mediminder");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 560);
        setLocationRelativeTo(null);

        // Two equal columns: text on the left, logo on the right
        JPanel mainPanel = new JPanel(new GridLayout(1, 2, 40, 0));
        mainPanel.setBackground(new Color(137, 207, 240));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(50, 75, 50, 75));

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setOpaque(false);

        JPanel miniHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        miniHeader.setOpaque(false);
        miniHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        miniHeader.add(loadLogo(60, 50));
        miniHeader.add(new JLabel("Mediminder"));

        JLabel titleLabel = new JLabel("<html>Hello, I'm<br>Mediminder</html>");
        titleLabel.setFont(new Font("Serif", Font.PLAIN, 60));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("I'm here to help with your medicine!");
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 18));
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        boolean returningUser = !MediminderStore.getUserName().isEmpty();
        JButton getStartedBtn = new JButton(returningUser ? "Continue" : "Let's Get Started");
        getStartedBtn.setFont(new Font("SansSerif", Font.PLAIN, 15));
        getStartedBtn.setBackground(Color.WHITE);
        getStartedBtn.setFocusPainted(false);
        getStartedBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        getStartedBtn.setMaximumSize(new Dimension(170, 42));
        getStartedBtn.addActionListener(event -> {
            JFrame next = MediminderStore.getUserName().isEmpty()
                ? new MediMinderLogin() : new MediminderMainMenuUI();
            next.setVisible(true);
            dispose();
        });

        leftPanel.add(Box.createVerticalStrut(20));
        leftPanel.add(miniHeader);
        leftPanel.add(Box.createVerticalStrut(20));
        leftPanel.add(titleLabel);
        leftPanel.add(Box.createVerticalStrut(15));
        leftPanel.add(subtitleLabel);
        leftPanel.add(Box.createVerticalStrut(45));
        leftPanel.add(getStartedBtn);

        JPanel rightPanel = new JPanel(new GridBagLayout()); // centers the image
        rightPanel.setOpaque(false);
        rightPanel.add(loadLogo(300, 250));

        mainPanel.add(leftPanel);
        mainPanel.add(rightPanel);
        setContentPane(mainPanel);
    }

    private JLabel loadLogo(int width, int height) {
        Image logo = new ImageIcon("MediminderLogo.png").getImage();
        return new JLabel(new ImageIcon(logo.getScaledInstance(width, height, Image.SCALE_SMOOTH)));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MediminderWelcomeUI().setVisible(true));
    }
}
