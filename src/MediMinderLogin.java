import javax.swing.*;
import java.awt.*;

public class MediMinderLogin extends JFrame {
    private final JTextField nameField = new JTextField(22);

    public MediMinderLogin() {
        super("MediMinder - Your name");
        MediminderReminderService.start();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(420, 220);
        setLocationRelativeTo(null);

        nameField.setText(MediminderStore.getUserName());
        JButton save = new JButton("Save name and continue");
        save.addActionListener(event -> saveName());
        nameField.addActionListener(event -> saveName());

        JPanel form = new JPanel(new GridLayout(0, 1, 8, 8));
        form.setBorder(BorderFactory.createEmptyBorder(25, 30, 20, 30));
        form.add(new JLabel("What should MediMinder call you?"));
        form.add(nameField);
        form.add(save);
        setContentPane(form);
    }

    private void saveName() {
        try {
            MediminderStore.setUserName(nameField.getText());
            new MediminderMainMenuUI().setVisible(true);
            dispose();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                "MediMinder", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MediMinderLogin().setVisible(true));
    }
}
