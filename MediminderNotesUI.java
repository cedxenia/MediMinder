import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
    private final JComboBox<Integer> period = new JComboBox<Integer>(
        new Integer[] {14, 30, 90});

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
        JButton addNote = new JButton("Add health note");
        JButton profile = new JButton("Allergies & conditions");
        JButton report = new JButton("Generate summary");
        JButton refresh = new JButton("Refresh notes");
        JButton deleteNote = new JButton("Delete selected note");
        JButton deleteAllNotes = new JButton("Delete all notes");
        period.setSelectedItem(14);
        actions.add(addNote);
        actions.add(profile);
        actions.add(new JLabel("Period:"));
        actions.add(period);
        actions.add(report);
        actions.add(refresh);
        actions.add(deleteNote);
        actions.add(deleteAllNotes);
        period.addActionListener(event -> refreshNotes());
        addNote.addActionListener(event -> showAddNoteDialog());
        profile.addActionListener(event -> showHealthProfileDialog());
        report.addActionListener(event -> showReport());
        refresh.addActionListener(event -> refreshNotes());
        deleteNote.addActionListener(event -> deleteSelectedNote());
        deleteAllNotes.addActionListener(event -> deleteAllNotes());

        notesTable.setRowHeight(26);
        notesTable.setFillsViewportHeight(true);
        notesTable.getColumnModel().removeColumn(notesTable.getColumnModel().getColumn(4));
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

    private void refreshNotes() {
        notesModel.setRowCount(0);
        Integer days = (Integer) period.getSelectedItem();
        LocalDate cutoff = LocalDate.now().minusDays(days == null ? 90 : days - 1);
        for (MediminderStore.HealthNote note : MediminderStore.getNotes()) {
            if (note.createdAt.toLocalDate().isBefore(cutoff)) {
                continue;
            }
            MediminderStore.Medicine medicine = MediminderStore.findMedicine(note.medicineId);
            notesModel.addRow(new Object[] {
                note.createdAt.format(DATE_FORMAT),
                medicine == null ? "Medicine removed" : medicine.name,
                note.feeling,
                note.details,
                note
            });
        }
    }

    private void deleteSelectedNote() {
        int row = notesTable.getSelectedRow();
        if (row < 0) {
            showError("Select a note to delete.");
            return;
        }
        int modelRow = notesTable.convertRowIndexToModel(row);
        MediminderStore.HealthNote note =
            (MediminderStore.HealthNote) notesModel.getValueAt(modelRow, 4);
        int result = JOptionPane.showConfirmDialog(this,
            "Delete this note permanently?", "Delete note", JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.YES_OPTION) {
            try {
                MediminderStore.deleteNote(note);
                refreshNotes();
            } catch (IllegalArgumentException ex) {
                showError(ex.getMessage());
            } catch (IllegalStateException ex) {
                showError(ex.getMessage());
            }
        }
    }

    private void deleteAllNotes() {
        int result = JOptionPane.showConfirmDialog(this,
            "Delete all saved health notes permanently?",
            "Delete all notes", JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.YES_OPTION) {
            try {
                MediminderStore.deleteAllNotes();
                refreshNotes();
            } catch (IllegalStateException ex) {
                showError(ex.getMessage());
            }
        }
    }

    private void showAddNoteDialog() {
        java.util.List<MediminderStore.Medicine> medicines =
            new java.util.ArrayList<MediminderStore.Medicine>();
        java.util.List<MediminderStore.Dose> takenDoses = MediminderStore.getDoses();
        for (MediminderStore.Medicine candidate : MediminderStore.getMedicines()) {
            for (MediminderStore.Dose dose : takenDoses) {
                if (dose.medicineId.equals(candidate.id) && "TAKEN".equals(dose.status)) {
                    medicines.add(candidate);
                    break;
                }
            }
        }
        if (medicines.isEmpty()) {
            showError("Mark a dose as taken in Medicines & Reminders before adding a health note.");
            return;
        }
        JComboBox<MediminderStore.Medicine> medicine = new JComboBox<MediminderStore.Medicine>(
            medicines.toArray(new MediminderStore.Medicine[medicines.size()]));
        JComboBox<DoseChoice> dose = new JComboBox<DoseChoice>();
        refreshDoseChoices(dose, (MediminderStore.Medicine) medicine.getSelectedItem());
        medicine.addActionListener(event -> refreshDoseChoices(
            dose, (MediminderStore.Medicine) medicine.getSelectedItem()));
        JComboBox<String> feeling = new JComboBox<String>(
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

        int result = JOptionPane.showConfirmDialog(this, fields, "Add health note",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }
        MediminderStore.Medicine selectedMedicine =
            (MediminderStore.Medicine) medicine.getSelectedItem();
        DoseChoice selectedDose = (DoseChoice) dose.getSelectedItem();
        Object selectedFeeling = feeling.getEditor().getItem();
        try {
            if (selectedMedicine == null || selectedDose == null) {
                throw new IllegalArgumentException("Select a medicine and a taken dose.");
            }
            MediminderStore.addNote(selectedMedicine.id, selectedDose.scheduledAt,
                selectedFeeling == null ? "" : selectedFeeling.toString(), details.getText());
            refreshNotes();
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        } catch (IllegalStateException ex) {
            showError(ex.getMessage());
        }
    }

    private void refreshDoseChoices(JComboBox<DoseChoice> choices,
            MediminderStore.Medicine medicine) {
        choices.removeAllItems();
        if (medicine == null) {
            return;
        }
        java.util.List<MediminderStore.Dose> doses =
            new java.util.ArrayList<MediminderStore.Dose>(MediminderStore.getDoses());
        java.util.Collections.sort(doses, (first, second) ->
            second.scheduledAt.compareTo(first.scheduledAt));
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
        int result = JOptionPane.showConfirmDialog(this, fields,
            "Allergies & conditions", JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            try {
                MediminderStore.setHealthProfile(allergies.getText(), conditions.getText());
            } catch (IllegalStateException ex) {
                showError(ex.getMessage());
            }
        }
    }

    private void showReport() {
        int days = (Integer) period.getSelectedItem();
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
        JScrollPane scroll = new JScrollPane(reportText);
        JButton copy = new JButton("Copy report");
        copy.addActionListener(event -> {
            try {
                Toolkit.getDefaultToolkit().getSystemClipboard()
                    .setContents(new StringSelection(reportText.getText()), null);
                JOptionPane.showMessageDialog(this, "Summary copied to clipboard.");
            } catch (HeadlessException ex) {
                showError("Clipboard access is unavailable in this environment.");
            } catch (SecurityException ex) {
                showError("Permission to access the clipboard was denied.");
            }
        });
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.add(scroll, BorderLayout.CENTER);
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
