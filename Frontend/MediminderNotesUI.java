package Frontend;
import javax.swing.*;
import java.awt.*;

public class MediminderNotesUI extends JFrame {

    public MediminderNotesUI() {
        super("Mediminder - Notes");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 580);
        getContentPane().setBackground(new Color(130, 200, 229)); // Theme sky-blue
        setLayout(new GridLayout(1, 2));

        // ================= LEFT PANEL =================
        JPanel left = new JPanel(new GridLayout(5, 1, 0, 15));
        left.setOpaque(false);
        left.setBorder(BorderFactory.createEmptyBorder(30, 45, 30, 45));

        // 1. Top Row: Back Button + Title
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        top.setOpaque(false);
        JButton backBtn = new JButton("\u2190");
        backBtn.setBackground(Color.WHITE);
        backBtn.addActionListener(e -> {
            new MediminderMainMenuUI().setVisible(true);
            this.dispose();
        });
        JLabel title = new JLabel("NOTES");
        title.setFont(new Font("Serif", Font.BOLD, 34));
        top.add(backBtn);
        top.add(title);

        // 2. Date + VIEW Row
        JPanel dateRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        dateRow.setOpaque(false);
        JLabel date = new JLabel("12/20/2005");
        date.setFont(new Font("Serif", Font.ITALIC, 18));
        JButton viewBtn = new JButton("VIEW");
        viewBtn.setBackground(Color.WHITE);
        dateRow.add(date);
        dateRow.add(viewBtn);

        // 3. Add Note + [ + ] Row
        JPanel addRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 10));
        addRow.setOpaque(false);
        JLabel addLabel = new JLabel("Add Note");
        addLabel.setFont(new Font("Serif", Font.ITALIC, 18));
        JButton plusBtn = new JButton("+");
        plusBtn.setBackground(Color.WHITE);
        addRow.add(addLabel);
        addRow.add(plusBtn);

        // 4. Summarize + Past Days Row
        JPanel bottomRow = new JPanel(new BorderLayout(15, 0));
        bottomRow.setOpaque(false);
        JButton sumBtn = new JButton("SUMMARIZE →");
        sumBtn.setBackground(Color.WHITE);

        JPanel days = new JPanel(new GridLayout(3, 1, 0, 5));
        days.setOpaque(false);
        days.add(new JButton("PAST 7 DAYS"));
        days.add(new JButton("PAST 14 DAYS"));
        days.add(new JButton("PAST 31 DAYS"));

        bottomRow.add(sumBtn, BorderLayout.WEST);
        bottomRow.add(days, BorderLayout.CENTER);

        left.add(top);
        left.add(dateRow);
        left.add(addRow);
        left.add(new JLabel()); // Simple spacer
        left.add(bottomRow);

        // ================= RIGHT PANEL =================
        JPanel right = new JPanel(new BorderLayout());
        right.setOpaque(false);
        right.setBorder(BorderFactory.createEmptyBorder(45, 35, 45, 45));

        JTextArea noteArea = new JTextArea(
            "Hello World! This is a test."
        );
        noteArea.setFont(new Font("Serif", Font.ITALIC, 18));
        noteArea.setBackground(new Color(236, 245, 250));
        noteArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(74, 137, 189), 2),
            BorderFactory.createEmptyBorder(25, 25, 25, 25)
        ));
        right.add(noteArea, BorderLayout.CENTER);

        add(left);
        add(right);
        setLocationRelativeTo(null);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MediminderNotesUI().setVisible(true));
    }
}