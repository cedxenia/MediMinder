import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MediminderNotesUI extends JFrame {
    private static final DateTimeFormatter DATE_FORMAT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private final DefaultTableModel notesModel = new DefaultTableModel(
        new Object[] {"Date", "Medicine", "Feeling", "Note", "Record"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    private final JTable notesTable = new JTable(notesModel);
    private final JComboBox<Integer> period = new JComboBox<>(new Integer[] {14, 30, 90});

    public MediminderNotesUI() {
        super("MediMinder - Health Notes & Summary");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(950, 620);
        getContentPane().setBackground(new Color(130, 200, 229));
        setLayout(new BorderLayout(12, 12));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(20, 25, 0, 25));
        JButton back = new JButton("← Menu");
        back.addActionListener(event -> {
            new MediminderMainMenuUI().setVisible(true);
            dispose();
        });
        JLabel title = new JLabel("HEALTH NOTES & DOCTOR SUMMARY", SwingConstants.CENTER);
        title.setFont(new Font("Serif", Font.BOLD, 26));
        header.add(back, BorderLayout.WEST);
        header.add(title, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        actions.setOpaque(false);
        addButton(actions, "Add health note", this::showAddNoteDialog);
        addButton(actions, "Allergies & conditions", this::showHealthProfileDialog);
        actions.add(new JLabel("Period:"));
        actions.add(period);
        addButton(actions, "Generate summary", this::showReport);
        addButton(actions, "Refresh notes", this::refreshNotes);
        addButton(actions, "Delete selected note", this::deleteSelectedNote);
        addButton(actions, "Delete all notes", this::deleteAllNotes);
        period.addActionListener(event -> refreshNotes());

        notesTable.setRowHeight(26);
        notesTable.setFillsViewportHeight(true);
        notesTable.removeColumn(notesTable.getColumnModel().getColumn(4)); // hidden "Record"
        JScrollPane notesScroll = new JScrollPane(notesTable);
        notesScroll.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));
        JLabel hint = new JLabel("Personal notes only; this journal is not medical advice.",
            SwingConstants.CENTER);
        hint.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));

        JPanel north = new JPanel(new BorderLayout());
        north.setOpaque(false);
        north.add(header, BorderLayout.NORTH);
        north.add(actions, BorderLayout.SOUTH);
        add(north, BorderLayout.NORTH);
        add(notesScroll, BorderLayout.CENTER);
        add(hint, BorderLayout.SOUTH);
        refreshNotes();
        setLocationRelativeTo(null);
    }

    private static void addButton(JPanel panel, String text, Runnable action) {
        JButton button = new JButton(text);
        button.addActionListener(event -> action.run());
        panel.add(button);
    }

    private int periodDays() {
        return (Integer) period.getSelectedItem();
    }

    private boolean confirm(String message, String title) {
        return JOptionPane.showConfirmDialog(this, message, title,
            JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    /** Runs a store action and shows any validation/storage error. */
    private void attempt(Runnable action) {
        try {
            action.run();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            showError(ex.getMessage());
        }
    }

    private void refreshNotes() {
        notesModel.setRowCount(0);
        LocalDate cutoff = LocalDate.now().minusDays(periodDays() - 1);
        for (MediminderStore.HealthNote note : MediminderStore.getNotes()) {
            if (note.createdAt.toLocalDate().isBefore(cutoff)) {
                continue;
            }
            MediminderStore.Medicine medicine = MediminderStore.findMedicine(note.medicineId);
            notesModel.addRow(new Object[] {
                note.createdAt.format(DATE_FORMAT),
                medicine == null ? "Medicine removed" : medicine.name,
                note.feeling, note.details, note
            });
        }
    }

    private void deleteSelectedNote() {
        int row = notesTable.getSelectedRow();
        if (row < 0) {
            showError("Select a note to delete.");
            return;
        }
        MediminderStore.HealthNote note = (MediminderStore.HealthNote)
            notesModel.getValueAt(notesTable.convertRowIndexToModel(row), 4);
        if (confirm("Delete this note permanently?", "Delete note")) {
            attempt(() -> {
                MediminderStore.deleteNote(note);
                refreshNotes();
            });
        }
    }

    private void deleteAllNotes() {
        if (confirm("Delete all saved health notes permanently?", "Delete all notes")) {
            attempt(() -> {
                MediminderStore.deleteAllNotes();
                refreshNotes();
            });
        }
    }

    private void showAddNoteDialog() {
        // Only medicines that have at least one TAKEN dose can receive a note
        Set<String> takenIds = new HashSet<String>();
        for (MediminderStore.Dose dose : MediminderStore.getDoses()) {
            if ("TAKEN".equals(dose.status)) {
                takenIds.add(dose.medicineId);
            }
        }
        List<MediminderStore.Medicine> medicines = new ArrayList<MediminderStore.Medicine>();
        for (MediminderStore.Medicine candidate : MediminderStore.getMedicines()) {
            if (takenIds.contains(candidate.id)) {
                medicines.add(candidate);
            }
        }
        if (medicines.isEmpty()) {
            showError("Mark a dose as taken in Medicines & Reminders before adding a health note.");
            return;
        }
        JComboBox<MediminderStore.Medicine> medicine =
            new JComboBox<>(medicines.toArray(new MediminderStore.Medicine[0]));
        JComboBox<DoseChoice> dose = new JComboBox<>();
        refreshDoseChoices(dose, (MediminderStore.Medicine) medicine.getSelectedItem());
        medicine.addActionListener(event -> refreshDoseChoices(
            dose, (MediminderStore.Medicine) medicine.getSelectedItem()));
        JComboBox<String> feeling = new JComboBox<>(
            new String[] {"Fine", "Dizzy", "Nauseous", "Headache", "Other"});
        feeling.setEditable(true);
        JTextArea details = new JTextArea(5, 28);

        JPanel fields = new JPanel(new GridLayout(0, 2, 8, 8));
        fields.add(new JLabel("Medicine:"));
        fields.add(medicine);
        fields.add(new JLabel("Taken dose:"));
        fields.add(dose);
        fields.add(new JLabel("How did you feel?"));
        fields.add(feeling);
        fields.add(new JLabel("Optional details:"));
        fields.add(new JScrollPane(details));
        if (JOptionPane.showConfirmDialog(this, fields, "Add health note",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return;
        }
        MediminderStore.Medicine selectedMedicine =
            (MediminderStore.Medicine) medicine.getSelectedItem();
        DoseChoice selectedDose = (DoseChoice) dose.getSelectedItem();
        Object selectedFeeling = feeling.getEditor().getItem();
        if (selectedDose == null) {
            showError("Select a medicine and a taken dose.");
            return;
        }
        attempt(() -> {
            MediminderStore.addNote(selectedMedicine.id, selectedDose.scheduledAt,
                selectedFeeling == null ? "" : selectedFeeling.toString(), details.getText());
            refreshNotes();
        });
    }

    private void refreshDoseChoices(JComboBox<DoseChoice> choices,
            MediminderStore.Medicine medicine) {
        choices.removeAllItems();
        if (medicine == null) {
            return;
        }
        List<MediminderStore.Dose> doses = MediminderStore.getDoses();
        doses.sort(Comparator.comparing((MediminderStore.Dose dose) -> dose.scheduledAt).reversed());
        for (MediminderStore.Dose dose : doses) {
            if (dose.medicineId.equals(medicine.id) && "TAKEN".equals(dose.status)) {
                choices.addItem(new DoseChoice(medicine.name, dose.scheduledAt));
            }
        }
    }

    private static final class DoseChoice {
        private final String medicineName;
        private final LocalDateTime scheduledAt;

        private DoseChoice(String medicineName, LocalDateTime scheduledAt) {
            this.medicineName = medicineName;
            this.scheduledAt = scheduledAt;
        }

        @Override
        public String toString() {
            return medicineName + " — " + scheduledAt.format(DATE_FORMAT);
        }
    }

    private void showHealthProfileDialog() {
        JTextArea allergies = new JTextArea(MediminderStore.getAllergies(), 4, 30);
        JTextArea conditions = new JTextArea(MediminderStore.getConditions(), 4, 30);
        JPanel fields = new JPanel(new GridLayout(0, 1, 6, 6));
        fields.add(new JLabel("Allergies (optional):"));
        fields.add(new JScrollPane(allergies));
        fields.add(new JLabel("Conditions (optional):"));
        fields.add(new JScrollPane(conditions));
        if (JOptionPane.showConfirmDialog(this, fields, "Allergies & conditions",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
            attempt(() -> MediminderStore.setHealthProfile(
                allergies.getText(), conditions.getText()));
        }
    }

    private void showReport() {
        int days = periodDays();
        String report;
        try {
            report = MediminderStore.buildReport(days);
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
            return;
        }
        JTextArea reportText = new JTextArea(report, 22, 65);
        reportText.setEditable(false);
        reportText.setCaretPosition(0);
        JButton copy = new JButton("Copy report");
        copy.addActionListener(event -> {
            try {
                Toolkit.getDefaultToolkit().getSystemClipboard()
                    .setContents(new StringSelection(reportText.getText()), null);
                JOptionPane.showMessageDialog(this, "Summary copied to clipboard.");
            } catch (HeadlessException | SecurityException ex) {
                showError("Clipboard access is unavailable or was denied.");
            }
        });
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.add(new JScrollPane(reportText), BorderLayout.CENTER);
        panel.add(copy, BorderLayout.SOUTH);
        JOptionPane.showMessageDialog(this, panel,
            "Doctor visit summary (" + days + " days)", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "MediMinder", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MediminderNotesUI().setVisible(true));
    }
}
