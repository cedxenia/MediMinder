import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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
    private static final int ID_COLUMN = 4;
    private static final int SCHEDULED_COLUMN = 5;

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
    private final Timer refreshTimer = new Timer(30000, event -> refreshRows());

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
        addButton(actions, "Add medicine", () -> showMedicineForm(null));
        addButton(actions, "Scan prescription image", this::choosePrescription);
        addButton(actions, "Update selected supply", this::updateSelectedSupply);
        addButton(actions, "Toggle hard-to-find", this::toggleSelectedHardToFind);
        addButton(actions, "Remove selected", this::removeSelectedMedicine);

        table.setRowHeight(28);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true);
        for (int i = 0; i < 2; i++) { // hide "Medicine ID" and "Scheduled at"
            table.removeColumn(table.getColumnModel().getColumn(ID_COLUMN));
        }
        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));

        JPanel doseActions = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 8));
        doseActions.setOpaque(false);
        addButton(doseActions, "Mark taken", () -> updateSelectedDose("TAKEN"));
        addButton(doseActions, "Mark skipped", () -> updateSelectedDose("SKIPPED"));
        addButton(doseActions, "Log how I felt", this::showNoteForm);
        addButton(doseActions, "Delete dose record", this::clearSelectedDose);
        addButton(doseActions, "Refresh", this::refreshRows);

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
        refreshTimer.start();
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent event) {
                refreshTimer.stop();
            }
        });
        setLocationRelativeTo(null);
    }

    // ---------- Small helpers ----------

    private static void addButton(JPanel panel, String text, Runnable action) {
        JButton button = new JButton(text);
        button.addActionListener(event -> action.run());
        panel.add(button);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "MediMinder", JOptionPane.ERROR_MESSAGE);
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

    /** Returns the selected model row, or -1 after showing the given message. */
    private int selectedRow(String message) {
        int row = table.getSelectedRow();
        if (row < 0) {
            showError(message);
            return -1;
        }
        return table.convertRowIndexToModel(row);
    }

    private String idAt(int modelRow) {
        return (String) tableModel.getValueAt(modelRow, ID_COLUMN);
    }

    private LocalDateTime timeAt(int modelRow) {
        return (LocalDateTime) tableModel.getValueAt(modelRow, SCHEDULED_COLUMN);
    }

    private MediminderStore.Medicine getSelectedMedicine() {
        int row = selectedRow("Select a medicine first.");
        return row < 0 ? null : MediminderStore.findMedicine(idAt(row));
    }

    // ---------- Table ----------

    private void refreshRows() {
        tableModel.setRowCount(0);
        LocalDate today = LocalDate.now();
        for (MediminderStore.Medicine medicine : MediminderStore.getMedicines()) {
            boolean lowSupply = MediminderStore.getSupplyDays(medicine)
                <= MediminderStore.getLowSupplyThreshold(medicine);
            for (LocalTime time : medicine.times) {
                LocalDateTime scheduledAt = LocalDateTime.of(today, time);
                String status = MediminderStore.getDoseStatus(medicine.id, scheduledAt);
                tableModel.addRow(new Object[] {
                    medicine.name, medicine.dosage, time.format(TIME_FORMAT),
                    (status.isEmpty() ? "Upcoming / not recorded" : status)
                        + (lowSupply ? " - LOW SUPPLY" : ""),
                    medicine.id, scheduledAt
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
        MediminderStore.Medicine medicine =
            MediminderStore.findMedicine(idAt(table.convertRowIndexToModel(row)));
        if (medicine == null) {
            supplyStatus.setText("Selected medicine was removed.");
            return;
        }
        int days = MediminderStore.getSupplyDays(medicine);
        int threshold = MediminderStore.getLowSupplyThreshold(medicine);
        supplyStatus.setText(medicine.quantityRemaining + " doses remaining; approximately "
            + days + " days at " + medicine.times.size() + " doses/day."
            + (medicine.hardToFind ? " Hard-to-find alert enabled." : "")
            + (days <= threshold
                ? "  LOW SUPPLY: refill warning threshold is " + threshold + " days." : ""));
    }

    // ---------- Doses ----------

    private void updateSelectedDose(String status) {
        int row = selectedRow("Select a scheduled dose first.");
        if (row < 0 || MediminderStore.findMedicine(idAt(row)) == null) {
            return;
        }
        attempt(() -> {
            MediminderStore.markDose(idAt(row), timeAt(row), status);
            refreshRows();
        });
    }

    private void clearSelectedDose() {
        int row = selectedRow("Select a dose record first.");
        if (row >= 0 && confirm("Delete this dose status and any note attached to it?",
                "Delete dose record")) {
            attempt(() -> {
                MediminderStore.clearDoseRecord(idAt(row), timeAt(row));
                refreshRows();
            });
        }
    }

    private void showNoteForm() {
        int row = selectedRow("Select a scheduled dose first.");
        if (row < 0) {
            return;
        }
        MediminderStore.Medicine medicine = MediminderStore.findMedicine(idAt(row));
        if (medicine == null) {
            showError("The selected medicine no longer exists.");
            return;
        }
        LocalDateTime scheduledAt = timeAt(row);
        if (!"TAKEN".equals(MediminderStore.getDoseStatus(medicine.id, scheduledAt))) {
            showError("Mark this dose as taken before recording how you felt.");
            return;
        }
        JTextField feeling = new JTextField();
        JTextArea details = new JTextArea(5, 25);
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.add(new JLabel("How did you feel after " + medicine.name + "?"), BorderLayout.NORTH);
        panel.add(feeling, BorderLayout.CENTER);
        panel.add(new JScrollPane(details), BorderLayout.SOUTH);
        if (JOptionPane.showConfirmDialog(this, panel, "Log a health note",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
            attempt(() -> MediminderStore.addNote(
                medicine.id, scheduledAt, feeling.getText(), details.getText()));
        }
    }

    // ---------- Medicines ----------

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

        while (true) { // re-show the form until it is valid or cancelled
            if (JOptionPane.showConfirmDialog(this, panel,
                    imagePath == null ? "Add medicine" : "Review prescription details",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE)
                    != JOptionPane.OK_OPTION) {
                return;
            }
            try {
                MediminderStore.Medicine medicine = MediminderStore.addMedicine(
                    name.getText(), dosage.getText(), parseTimes(times.getText()),
                    Integer.parseInt(quantity.getText().trim()), hardToFind.isSelected());
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
            if (!part.trim().isEmpty()) {
                LocalTime time = LocalTime.parse(part.trim(), TIME_FORMAT);
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
        List<MediminderStore.Pharmacy> pharmacies = MediminderStore.getRankedPharmacies(medicine.id);
        StringBuilder message = new StringBuilder("Suggested saved pharmacies for ")
            .append(medicine.name).append(" (in-stock first):\n\n");
        if (pharmacies.isEmpty()) {
            message.append("No pharmacies are saved yet. Add them from Medicine / Pharmacy Search.");
        }
        for (MediminderStore.Pharmacy pharmacy : pharmacies) {
            message.append(pharmacy.name).append(" — ").append(pharmacy.location).append(" — ")
                .append(MediminderStore.getPharmacyStock(pharmacy, medicine.id)).append('\n');
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
        } catch (IllegalArgumentException | IllegalStateException ex) {
            showError(ex.getMessage());
        }
    }

    private void toggleSelectedHardToFind() {
        MediminderStore.Medicine medicine = getSelectedMedicine();
        if (medicine != null) {
            attempt(() -> {
                MediminderStore.setHardToFind(medicine.id, !medicine.hardToFind);
                refreshRows();
            });
        }
    }

    private void removeSelectedMedicine() {
        MediminderStore.Medicine medicine = getSelectedMedicine();
        if (medicine != null && confirm(
                "Remove " + medicine.name + " and its dose and note history?", "Confirm removal")) {
            attempt(() -> {
                MediminderStore.removeMedicine(medicine.id);
                refreshRows();
            });
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MediminderReminderUI().setVisible(true));
    }
}
