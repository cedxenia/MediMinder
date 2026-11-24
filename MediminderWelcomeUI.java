import javax.swing.*;
import java.awt.*;


public class MediminderWelcomeUI extends JFrame {

public MediminderWelcomeUI() {
        // 1. Basic Window Setup
        setTitle("Mediminder");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 560);
        setLocationRelativeTo(null);

        // 2. Main Panel split into 2 equal columns (Left & Right)
        JPanel mainPanel = new JPanel(new GridLayout(1, 2, 40, 0));
        mainPanel.setBackground(new Color(242, 242, 242));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(50, 75, 50, 75));

        // ================= LEFT COLUMN =================
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setOpaque(false);

        // Top-left small logo + brand name
        JPanel miniHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        miniHeader.setOpaque(false);
        miniHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        miniHeader.add(loadLogo("logo.png", 28, 28));
        miniHeader.add(new JLabel("Mediminder"));

        // Main Headline
        JLabel titleLabel = new JLabel("<html>Hello, I’m<br>Mediminder</html>");
        titleLabel.setFont(new Font("Serif", Font.PLAIN, 60));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Subtitle
        JLabel subtitleLabel = new JLabel("I'm here to help with your medicine!");
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 18));
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Standard "Let's Get Started" Button
        JButton getStartedBtn = new JButton("Let's Get Started");
        getStartedBtn.setFont(new Font("SansSerif", Font.PLAIN, 15));
        getStartedBtn.setBackground(Color.WHITE);
        getStartedBtn.setFocusPainted(false);
        getStartedBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        getStartedBtn.setMaximumSize(new Dimension(170, 42));
        getStartedBtn.addActionListener(e -> 
            JOptionPane.showMessageDialog(this, "Navigating to Medicine Setup...")
        );

        // Add items to left column with vertical spacing
        leftPanel.add(Box.createVerticalStrut(20));
        leftPanel.add(miniHeader);
        leftPanel.add(Box.createVerticalStrut(20));
        leftPanel.add(titleLabel);
        leftPanel.add(Box.createVerticalStrut(15));
        leftPanel.add(subtitleLabel);
        leftPanel.add(Box.createVerticalStrut(45));
        leftPanel.add(getStartedBtn);

        // ================= RIGHT COLUMN =================
        JPanel rightPanel = new JPanel(new GridBagLayout()); // Centers the image
        rightPanel.setOpaque(false);
        rightPanel.add(loadLogo("logo.png", 250, 220));

        // Add both columns to main window
        mainPanel.add(leftPanel);
        mainPanel.add(rightPanel);
        setContentPane(mainPanel);
    }

    // Simple helper method to load and resize your image
    private JLabel loadLogo(String path, int width, int height) {
        ImageIcon icon = new ImageIcon(path);
        Image scaled = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new JLabel(new ImageIcon(scaled));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MediminderWelcomeUI().setVisible(true));
    }
}