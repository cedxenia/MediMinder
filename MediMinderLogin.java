import javax.swing.*;
import java.awt.*;

public class MediMinderLogin extends JFrame {

    public MediMinderLogin() {
        setTitle("Mediminder - Login");
        setSize(500, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(null);
        panel.setBackground(new Color(135, 206, 235));

        JLabel title = new JLabel("LOGIN");
        title.setFont(new Font("Georgia", Font.BOLD, 28));
        title.setBounds(195, 30, 150, 35);
        panel.add(title);

        JLabel accLabel = new JLabel("Account:");
        accLabel.setFont(new Font("Georgia", Font.ITALIC, 16));
        accLabel.setBounds(90, 100, 90, 25);
        panel.add(accLabel);

        JTextField accField = new JTextField();
        accField.setBounds(190, 100, 190, 28);
        panel.add(accField);

        JLabel passLabel = new JLabel("Password:");
        passLabel.setFont(new Font("Georgia", Font.ITALIC, 16));
        passLabel.setBounds(90, 150, 90, 25);
        panel.add(passLabel);

        JPasswordField passField = new JPasswordField();
        passField.setBounds(190, 150, 190, 28);
        panel.add(passField);

        JButton loginBtn = new JButton("LOGIN");
        loginBtn.setBackground(Color.WHITE);
        loginBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        loginBtn.setBounds(195, 215, 110, 32);
        panel.add(loginBtn);

        loginBtn.addActionListener(e -> {
            String account = accField.getText();
            String password = new String(passField.getPassword());
            new MediminderMainMenuUI().setVisible(true);
            this.dispose();

        });

        add(panel);
    }

    public static void main(String[] args) {
        new MediMinderLogin().setVisible(true);
    }
}