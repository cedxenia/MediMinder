import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;


public class MediminderWelcomeUI extends JFrame {

    public MediminderWelcomeUI() {
        setTitle("Mediminder");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1024, 576);
        setLocationRelativeTo(null);

        // Main light-gray background panel using GridBagLayout for clean two-column alignment
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(new Color(242, 242, 242)); // #F2F2F2 background
        mainPanel.setBorder(BorderFactory.createEmptyBorder(40, 75, 40, 75));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        // ================= LEFT COLUMN =================
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setOpaque(false);

        // 1. Top-left small brand header (Mini Logo + "Mediminder")
        JPanel miniHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        miniHeader.setOpaque(false);
        miniHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        miniHeader.add(new LogoIconPanel(28, false));

        JLabel smallBrandLabel = new JLabel("Mediminder");
        smallBrandLabel.setFont(new Font("SansSerif", Font.PLAIN, 15));
        smallBrandLabel.setForeground(new Color(30, 30, 30));
        miniHeader.add(smallBrandLabel);

        // 2. Main Serif Headline ("Hello, I'm Mediminder")
        // Using HTML to keep tight line spacing between the two lines
        JLabel titleLabel = new JLabel(
            "<html><div style='line-height: 0.92;'>Hello, I’m<br>Mediminder</div></html>"
        );
        titleLabel.setFont(new Font("Serif", Font.PLAIN, 62));
        titleLabel.setForeground(new Color(17, 17, 17));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // 3. Subtitle ("Im here to help with your medicine!")
        JLabel subtitleLabel = new JLabel("Im here to help with your medicine!");
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 19));
        subtitleLabel.setForeground(new Color(34, 34, 34));
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // 4. Pill-shaped "Let's Get Started" button
        PillButton getStartedBtn = new PillButton("Let's Get Started");
        getStartedBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        getStartedBtn.addActionListener(e -> 
            JOptionPane.showMessageDialog(this, "Navigating to Medicine Setup...")
        );

        // Assemble left column with exact vertical spacing
        leftPanel.add(Box.createVerticalStrut(25));
        leftPanel.add(miniHeader);
        leftPanel.add(Box.createVerticalStrut(18));
        leftPanel.add(titleLabel);
        leftPanel.add(Box.createVerticalStrut(14));
        leftPanel.add(subtitleLabel);
        leftPanel.add(Box.createVerticalStrut(50));
        leftPanel.add(getStartedBtn);
        leftPanel.add(Box.createVerticalGlue());

        gbc.gridx = 0;
        gbc.weightx = 0.55;
        mainPanel.add(leftPanel, gbc);

        // ================= RIGHT COLUMN =================
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setOpaque(false);

        // White logo card container on the right
        JPanel logoCard = new JPanel(new BorderLayout());
        logoCard.setBackground(new Color(250, 252, 255));
        logoCard.setPreferredSize(new Dimension(265, 230));
        logoCard.add(new LogoIconPanel(180, true), BorderLayout.CENTER);

        rightPanel.add(logoCard);

        gbc.gridx = 1;
        gbc.weightx = 0.45;
        mainPanel.add(rightPanel, gbc);

        setContentPane(mainPanel);
    }

    /**
     * Custom Pill-Shaped Outlined Button ("Let's Get Started")
     */
    static class PillButton extends JButton {
        public PillButton(String text) {
            super(text);
            setFont(new Font("SansSerif", Font.PLAIN, 15));
            setForeground(new Color(25, 25, 25));
            setBackground(Color.WHITE);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            Dimension size = new Dimension(170, 42);
            setPreferredSize(size);
            setMinimumSize(size);
            setMaximumSize(size);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Subtle hover effect
            if (getModel().isRollover()) {
                g2.setColor(new Color(245, 248, 252));
            } else {
                g2.setColor(getBackground());
            }

            int arc = getHeight();
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, arc, arc));

            // Thin dark border outline
            g2.setColor(new Color(70, 70, 70));
            g2.setStroke(new BasicStroke(1.2f));
            g2.draw(new RoundRectangle2D.Double(0.5, 0.5, getWidth() - 2, getHeight() - 2, arc, arc));

            g2.dispose();
            super.paintComponent(g);
        }
    }

    /**
     * Custom Graphics2D Vector Panel drawing the Blue Heart + Green/White Capsule Logo
     */
    static class LogoIconPanel extends JPanel {
        private final int iconSize;
        private final boolean showBrandText;

        public LogoIconPanel(int iconSize, boolean showBrandText) {
            this.iconSize = iconSize;
            this.showBrandText = showBrandText;
            setOpaque(false);
            if (!showBrandText) {
                setPreferredSize(new Dimension(iconSize, iconSize));
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            double scale = (showBrandText ? 115.0 : iconSize) / 100.0;
            double cx = w / 2.0;
            double cy = showBrandText ? (h / 2.0) - 24 : h / 2.0;

            AffineTransform oldTransform = g2.getTransform();
            g2.translate(cx, cy);
            g2.scale(scale, scale);

            // 1. Draw Blue Heart
            Path2D heart = new Path2D.Double();
            heart.moveTo(0, 42);
            heart.curveTo(-52, 8, -55, -34, -24, -42);
            heart.curveTo(-8, -46, 0, -32, 0, -24);
            heart.curveTo(0, -32, 8, -46, 24, -42);
            heart.curveTo(55, -34, 52, 8, 0, 42);
            heart.closePath();

            g2.setPaint(new GradientPaint(-30, -30, new Color(77, 173, 232), 20, 40, new Color(33, 128, 200)));
            g2.fill(heart);

            // 2. Draw Green Outer Swoosh Ring
            g2.setColor(new Color(102, 198, 92));
            g2.setStroke(new BasicStroke(5.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawArc(-34, -34, 72, 72, -65, 195);

            // 3. Draw Diagonal Pill Capsule
            g2.rotate(Math.toRadians(-45));
            RoundRectangle2D pill = new RoundRectangle2D.Double(-32, -14, 64, 28, 28, 28);

            // White left half
            g2.setColor(Color.WHITE);
            g2.fill(pill);

            // Green right half (clipped to right side of pill)
            Shape oldClip = g2.getClip();
            g2.clip(pill);
            g2.setColor(new Color(115, 204, 102));
            g2.fillRect(0, -15, 35, 30);
            g2.setClip(oldClip);

            // Pill outline
            g2.setColor(new Color(28, 86, 130));
            g2.setStroke(new BasicStroke(2.2f));
            g2.draw(pill);

            // Checkmark inside white half of pill
            g2.rotate(Math.toRadians(45)); // Un-rotate for upright checkmark
            g2.setColor(new Color(28, 86, 130));
            g2.setStroke(new BasicStroke(3.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine(-13, 8, -8, 13);
            g2.drawLine(-8, 13, 0, 4);

            g2.setTransform(oldTransform);

            // 4. Draw "MediMinder" text under the large logo card
            if (showBrandText) {
                Font brandFont = new Font("SansSerif", Font.BOLD, 30);
                g2.setFont(brandFont);
                FontMetrics fm = g2.getFontMetrics();
                String text = "MediMinder";
                int textX = (w - fm.stringWidth(text)) / 2;
                int textY = (int) (cy + 85);

                g2.setColor(new Color(27, 89, 138));
                g2.drawString("Medi", textX, textY);

                int mediWidth = fm.stringWidth("Medi");
                g2.setColor(new Color(38, 118, 168));
                g2.drawString("Minder", textX + mediWidth, textY);
            }

            g2.dispose();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MediminderWelcomeUI().setVisible(true);
        });
    }
}