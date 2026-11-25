import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class MediminderReminderUI extends JFrame {
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    private final DefaultTableModel tableModel = new DefaultTableModel(
        new Object[] {"Medicine", "Dosage", "Reminder time", "Status", "Medicine ID", "Scheduled at"},
        0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    private final JTable table = new JTable(tableModel);
    private final JLabel supplyStatus = new JLabel(" ");
    private final Timer refreshTimer;

    public MediminderReminderUI() {
        super("MediMinder - Medicines & Reminders");
        MediminderReminderService.start();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(950, 620);
        getContentPane().setBackground(new Color(130, 200, 229));
        setLayout(new BorderLayout(12, 12));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.setBorder(BorderFactory.createEmptyBorder(20, 25, 0, 25));
        JButton back = new JButton("← Menu");
        back.addActionListener(event -> {
            new MediminderMainMenuUI().setVisible(true);
            dispose();
        });
        JLabel title = new JLabel("MEDICINES & REMINDERS", SwingConstants.CENTER);
        title.setFont(new Font("Serif", Font.BOLD, 28));
        top.add(back, BorderLayout.WEST);
        top.add(title, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        actions.setOpaque(false);
        JButton add = new JButton("Add medicine");
        JButton scan = new JButton("Scan prescription image");
        JButton updateSupply = new JButton("Update selected supply");
        JButton toggleHardToFind = new JButton("Toggle hard-to-find");
        JButton remove = new JButton("Remove selected");
        actions.add(add);
        actions.add(scan);
        actions.add(updateSupply);
        actions.add(toggleHardToFind);
        actions.add(remove);

        add.addActionListener(event -> showMedicineForm(null));
        scan.addActionListener(event -> choosePrescription());
        updateSupply.addActionListener(event -> updateSelectedSupply());
        toggleHardToFind.addActionListener(event -> toggleSelectedHardToFind());
        remove.addActionListener(event -> removeSelectedMedicine());

        table.setRowHeight(28);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true);
        hideColumn(4);
        hideColumn(4);
        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));

        JPanel doseActions = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 8));
        doseActions.setOpaque(false);
        JButton taken = new JButton("Mark taken");
        JButton skipped = new JButton("Mark skipped");
        JButton logFeeling = new JButton("Log how I felt");
        JButton clearStatus = new JButton("Delete dose record");
        JButton refresh = new JButton("Refresh");
        doseActions.add(taken);
        doseActions.add(skipped);
        doseActions.add(logFeeling);
        doseActions.add(clearStatus);
        doseActions.add(refresh);
        taken.addActionListener(event -> updateSelectedDose("TAKEN"));
        skipped.addActionListener(event -> updateSelectedDose("SKIPPED"));
        logFeeling.addActionListener(event -> showNoteForm());
        clearStatus.addActionListener(event -> clearSelectedDose());
        refresh.addActionListener(event -> refreshRows());

        supplyStatus.setBorder(BorderFactory.createEmptyBorder(0, 25, 5, 25));
        JPanel south = new JPanel(new BorderLayout());
        south.setOpaque(false);
        south.add(doseActions, BorderLayout.CENTER);
        south.add(supplyStatus, BorderLayout.SOUTH);

        JPanel north = new JPanel(new BorderLayout());
        north.setOpaque(false);
        north.add(top, BorderLayout.NORTH);
        north.add(actions, BorderLayout.SOUTH);
        add(north, BorderLayout.NORTH);
        add(tableScroll, BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);

        table.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                updateSupplyStatus();
            }
        });
        refreshRows();
        refreshTimer = new Timer(30000, event -> {
            refreshRows();
        });
        refreshTimer.start();
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent event) {
                refreshTimer.stop();
            }
        });
        setLocationRelativeTo(null);
    }

    private void hideColumn(int index) {
        table.getColumnModel().removeColumn(table.getColumnModel().getColumn(index));
    }

    private void refreshRows() {
        tableModel.setRowCount(0);
        LocalDate today = LocalDate.now();
        for (MediminderStore.Medicine medicine : MediminderStore.getMedicines()) {
            int daysRemaining = MediminderStore.getSupplyDays(medicine);
            for (LocalTime time : medicine.times) {
                LocalDateTime scheduledAt = LocalDateTime.of(today, time);
                String status = MediminderStore.getDoseStatus(medicine.id, scheduledAt);
                String displayStatus = status.isEmpty() ? "Upcoming / not recorded" : status;
                if (daysRemaining <= MediminderStore.getLowSupplyThreshold(medicine)) {
                    displayStatus += " - LOW SUPPLY";
                }
                tableModel.addRow(new Object[] {
                    medicine.name, medicine.dosage, time.format(TIME_FORMAT),
                    displayStatus, medicine.id, scheduledAt
                });
            }
        }
        updateSupplyStatus();
    }

    private void updateSupplyStatus() {
        int row = table.getSelectedRow();
        if (row < 0) {
            supplyStatus.setText("Select a reminder to mark it taken/skipped. Supply is measured in doses.");
            return;
        }
        MediminderStore.Medicine medicine = MediminderStore.findMedicine(
            (String) tableModel.getValueAt(table.convertRowIndexToModel(row), 4));
        if (medicine == null) {
            supplyStatus.setText("Selected medicine was removed.");
            return;
        }
        int days = MediminderStore.getSupplyDays(medicine);
        int threshold = MediminderStore.getLowSupplyThreshold(medicine);
        String warning = days <= threshold
            ? "  LOW SUPPLY: refill warning threshold is " + threshold + " days."
            : "";
        supplyStatus.setText(medicine.quantityRemaining + " doses remaining; approximately "
            + days + " days at " + medicine.times.size() + " doses/day."
            + (medicine.hardToFind ? " Hard-to-find alert enabled." : "") + warning);
    }

    private void updateSelectedDose(String status) {
        int row = table.getSelectedRow();
        if (row < 0) {
            showError("Select a scheduled dose first.");
            return;
        }
        int modelRow = table.convertRowIndexToModel(row);
        String medicineId = (String) tableModel.getValueAt(modelRow, 4);
        LocalDateTime scheduledAt = (LocalDateTime) tableModel.getValueAt(modelRow, 5);
        MediminderStore.Medicine medicine = MediminderStore.findMedicine(medicineId);
        if (medicine != null) {
            recordDose(medicine, scheduledAt, status);
        }
    }

    private void clearSelectedDose() {
        int row = table.getSelectedRow();
        if (row < 0) {
            showError("Select a dose record first.");
            return;
        }
        int modelRow = table.convertRowIndexToModel(row);
        String medicineId = (String) tableModel.getValueAt(modelRow, 4);
        LocalDateTime scheduledAt = (LocalDateTime) tableModel.getValueAt(modelRow, 5);
        int result = JOptionPane.showConfirmDialog(this,
            "Delete this dose status and any note attached to it?",
            "Delete dose record", JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.YES_OPTION) {
            try {
                MediminderStore.clearDoseRecord(medicineId, scheduledAt);
                refreshRows();
            } catch (IllegalArgumentException ex) {
                showError(ex.getMessage());
            } catch (IllegalStateException ex) {
                showError(ex.getMessage());
            }
        }
    }

    private void recordDose(MediminderStore.Medicine medicine, LocalDateTime scheduledAt,
            String status) {
        try {
            MediminderStore.markDose(medicine.id, scheduledAt, status);
            refreshRows();
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        } catch (IllegalStateException ex) {
            showError(ex.getMessage());
        }
    }

    private void showMedicineForm(String imagePath) {
        JTextField name = new JTextField();
        JTextField dosage = new JTextField();
        JTextField times = new JTextField("08:00");
        JTextField quantity = new JTextField("30");
        JCheckBox hardToFind = new JCheckBox("Hard to find (warn when about 14 days remain)");
        JLabel imageLabel = new JLabel(imagePath == null ? "No image selected"
            : new File(imagePath).getName());

        JPanel panel = new JPanel(new GridLayout(0, 2, 8, 8));
        panel.add(new JLabel("Medicine name:"));
        panel.add(name);
        panel.add(new JLabel("Dosage:"));
        panel.add(dosage);
        panel.add(new JLabel("Reminder times (HH:mm, comma-separated):"));
        panel.add(times);
        panel.add(new JLabel("Quantity remaining (doses):"));
        panel.add(quantity);
        panel.add(new JLabel("Prescription image:"));
        panel.add(imageLabel);
        panel.add(new JLabel("Supply alert:"));
        panel.add(hardToFind);
        if (imagePath != null) {
            panel.add(new JLabel("Image text recognition:"));
            panel.add(new JLabel("Not configured; review and enter details manually."));
        }

        while (true) {
            int result = JOptionPane.showConfirmDialog(this, panel,
                imagePath == null ? "Add medicine" : "Review prescription details",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (result != JOptionPane.OK_OPTION) {
                return;
            }
            try {
                List<LocalTime> schedule = parseTimes(times.getText());
                int remaining = Integer.parseInt(quantity.getText().trim());
                MediminderStore.Medicine medicine = MediminderStore.addMedicine(
                    name.getText(), dosage.getText(), schedule,
                    remaining, hardToFind.isSelected());
                refreshRows();
                if (imagePath != null) {
                    showPharmacyRecommendations(medicine);
                }
                return;
            } catch (NumberFormatException ex) {
                showError("Enter quantity as a whole number.");
            } catch (DateTimeParseException ex) {
                showError("Enter reminder times using 24-hour HH:mm format, for example 08:00,20:00.");
            } catch (IllegalArgumentException ex) {
                showError(ex.getMessage());
            } catch (IllegalStateException ex) {
                showError(ex.getMessage());
                return;
            }
        }
    }

    private List<LocalTime> parseTimes(String value) {
        List<LocalTime> parsed = new ArrayList<LocalTime>();
        for (String part : value.split(",")) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                LocalTime time = LocalTime.parse(trimmed, TIME_FORMAT);
                if (parsed.contains(time)) {
                    throw new IllegalArgumentException("Reminder times must be unique.");
                }
                parsed.add(time);
            }
        }
        if (parsed.isEmpty()) {
            throw new IllegalArgumentException("Enter at least one reminder time.");
        }
        return parsed;
    }

    private void choosePrescription() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Choose a prescription or medicine-label image");
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            "Image files", "png", "jpg", "jpeg", "gif", "bmp"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            showMedicineForm(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void showPharmacyRecommendations(MediminderStore.Medicine medicine) {
        List<MediminderStore.Pharmacy> pharmacies =
            MediminderStore.getRankedPharmacies(medicine.id);
        StringBuilder message = new StringBuilder("Suggested saved pharmacies for ")
            .append(medicine.name).append(" (in-stock first):\n\n");
        if (pharmacies.isEmpty()) {
            message.append("No pharmacies are saved yet. Add them from Medicine / Pharmacy Search.");
        } else {
            for (MediminderStore.Pharmacy pharmacy : pharmacies) {
                message.append(pharmacy.name).append(" — ").append(pharmacy.location)
                    .append(" — ")
                    .append(MediminderStore.getPharmacyStock(pharmacy, medicine.id))
                    .append('\n');
            }
        }
        JOptionPane.showMessageDialog(this, message.toString(),
            "Pharmacy suggestions", JOptionPane.INFORMATION_MESSAGE);
    }

    private void updateSelectedSupply() {
        MediminderStore.Medicine medicine = getSelectedMedicine();
        if (medicine == null) {
            return;
        }
        String value = JOptionPane.showInputDialog(this,
            "Enter remaining doses for " + medicine.name + ":",
            Integer.toString(medicine.quantityRemaining));
        if (value == null) {
            return;
        }
        try {
            MediminderStore.updateSupply(medicine.id, Integer.parseInt(value.trim()));
            refreshRows();
        } catch (NumberFormatException ex) {
            showError("Enter quantity as a whole number.");
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        } catch (IllegalStateException ex) {
            showError(ex.getMessage());
        }
    }

    private void toggleSelectedHardToFind() {
        MediminderStore.Medicine medicine = getSelectedMedicine();
        if (medicine == null) {
            return;
        }
        try {
            MediminderStore.setHardToFind(medicine.id, !medicine.hardToFind);
            refreshRows();
        } catch (IllegalStateException ex) {
            showError(ex.getMessage());
        }
    }

    private void removeSelectedMedicine() {
        MediminderStore.Medicine medicine = getSelectedMedicine();
        if (medicine == null) {
            return;
        }
        int result = JOptionPane.showConfirmDialog(this,
            "Remove " + medicine.name + " and its dose and note history?",
            "Confirm removal", JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.YES_OPTION) {
            try {
                MediminderStore.removeMedicine(medicine.id);
                refreshRows();
            } catch (IllegalArgumentException ex) {
                showError(ex.getMessage());
            } catch (IllegalStateException ex) {
                showError(ex.getMessage());
            }
        }
    }

    private MediminderStore.Medicine getSelectedMedicine() {
        int row = table.getSelectedRow();
        if (row < 0) {
            showError("Select a medicine first.");
            return null;
        }
        int modelRow = table.convertRowIndexToModel(row);
        return MediminderStore.findMedicine((String) tableModel.getValueAt(modelRow, 4));
    }

    private void showNoteForm() {
        int row = table.getSelectedRow();
        if (row < 0) {
            showError("Select a scheduled dose first.");
            return;
        }
        int modelRow = table.convertRowIndexToModel(row);
        String medicineId = (String) tableModel.getValueAt(modelRow, 4);
        LocalDateTime scheduledAt = (LocalDateTime) tableModel.getValueAt(modelRow, 5);
        MediminderStore.Medicine medicine = MediminderStore.findMedicine(medicineId);
        if (medicine == null) {
            showError("The selected medicine no longer exists.");
            return;
        }
        if (!"TAKEN".equals(MediminderStore.getDoseStatus(medicineId, scheduledAt))) {
            showError("Mark this dose as taken before recording how you felt.");
            return;
        }
        JTextField feeling = new JTextField();
        JTextArea details = new JTextArea(5, 25);
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.add(new JLabel("How did you feel after " + medicine.name + "?"),
            BorderLayout.NORTH);
        panel.add(feeling, BorderLayout.CENTER);
        panel.add(new JScrollPane(details), BorderLayout.SOUTH);
        int result = JOptionPane.showConfirmDialog(this, panel, "Log a health note",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            try {
                MediminderStore.addNote(medicine.id, scheduledAt,
                    feeling.getText(), details.getText());
            } catch (IllegalArgumentException ex) {
                showError(ex.getMessage());
            } catch (IllegalStateException ex) {
                showError(ex.getMessage());
            }
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "MediMinder", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MediminderReminderUI().setVisible(true));
    }
}
