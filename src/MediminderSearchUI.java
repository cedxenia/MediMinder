import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

public class MediminderSearchUI extends JFrame {
    private final JTextField searchField = new JTextField();
    private final JTextField locationField = new JTextField();
    private final JTextArea results = new JTextArea();

    public MediminderSearchUI() {
        super("MediMinder - Medicine & Pharmacy Search");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        getContentPane().setBackground(new Color(130, 200, 229));
        setLayout(new BorderLayout(12, 12));

        // Header: back button + search box
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(20, 25, 0, 25));
        JButton back = new JButton("← Menu");
        back.addActionListener(event -> {
            new MediminderMainMenuUI().setVisible(true);
            dispose();
        });
        searchField.setToolTipText("Search your saved medicines by name or dosage");
        searchField.setPreferredSize(new Dimension(400, 36));
        JButton search = new JButton("Search");
        JPanel searchBox = new JPanel(new BorderLayout(8, 0));
        searchBox.setOpaque(false);
        searchBox.add(searchField, BorderLayout.CENTER);
        searchBox.add(search, BorderLayout.EAST);
        header.add(back, BorderLayout.WEST);
        header.add(searchBox, BorderLayout.CENTER);
        search.addActionListener(event -> refreshResults());
        searchField.addActionListener(event -> refreshResults());
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent event) { refreshResults(); }
            @Override public void removeUpdate(DocumentEvent event) { refreshResults(); }
            @Override public void changedUpdate(DocumentEvent event) { refreshResults(); }
        });

        // Results area
        results.setEditable(false);
        results.setLineWrap(true);
        results.setWrapStyleWord(true);
        results.setFont(new Font("SansSerif", Font.PLAIN, 15));
        results.setBackground(new Color(236, 245, 250));
        results.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        JScrollPane resultScroll = new JScrollPane(results);
        resultScroll.setBorder(BorderFactory.createEmptyBorder(0, 25, 0, 25));

        // Pharmacy controls
        JPanel controls = new JPanel(new GridBagLayout());
        controls.setOpaque(false);
        controls.setBorder(BorderFactory.createEmptyBorder(0, 25, 16, 25));
        place(controls, new JLabel("City / neighborhood (location is entered manually):"), 0, 0, 0);
        place(controls, locationField, 1, 0, 1);
        place(controls, button("Find nearby pharmacies", this::openMapSearch), 2, 0, 0);
        place(controls, button("Add saved pharmacy", this::addPharmacy), 0, 1, 0);
        place(controls, button("Update medicine stock status", this::updateStockStatus), 1, 1, 0);
        place(controls, button("Delete saved pharmacy", this::deletePharmacy), 2, 1, 0);

        JPanel north = new JPanel(new BorderLayout());
        north.setOpaque(false);
        north.add(header, BorderLayout.NORTH);
        JLabel explanation = new JLabel("Saved medicine search and pharmacy recommendations",
            SwingConstants.CENTER);
        explanation.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
        north.add(explanation, BorderLayout.SOUTH);
        add(north, BorderLayout.NORTH);
        add(resultScroll, BorderLayout.CENTER);
        add(controls, BorderLayout.SOUTH);
        refreshResults();
        setLocationRelativeTo(null);
    }

    private static JButton button(String text, Runnable action) {
        JButton button = new JButton(text);
        button.addActionListener(event -> action.run());
        return button;
    }

    private static void place(JPanel panel, Component component, int x, int y, double weightX) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(3, 5, 3, 5);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = x;
        constraints.gridy = y;
        constraints.weightx = weightX;
        panel.add(component, constraints);
    }

    private static boolean matches(String query, String... fields) {
        for (String field : fields) {
            if (field.toLowerCase().contains(query)) {
                return true;
            }
        }
        return false;
    }

    private void refreshResults() {
        String query = searchField.getText().trim().toLowerCase();
        StringBuilder text = new StringBuilder();
        MediminderStore.Medicine matched = null;
        for (MediminderStore.Medicine medicine : MediminderStore.getMedicines()) {
            if (!matches(query, medicine.name, medicine.dosage)) {
                continue;
            }
            if (matched == null) {
                matched = medicine;
            }
            text.append("MEDICINE: ").append(medicine.name)
                .append("\nDosage: ").append(medicine.dosage)
                .append("\nSchedule: ").append(medicine.times.stream()
                    .map(LocalTime::toString).collect(Collectors.joining(", ")))
                .append("\nSupply: ").append(medicine.quantityRemaining)
                .append(" doses (~").append(MediminderStore.getSupplyDays(medicine))
                .append(" days remaining)")
                .append(medicine.hardToFind ? "\nHard-to-find alert: 14-day threshold" : "")
                .append("\n\n");
        }
        StringBuilder saved = new StringBuilder();
        for (MediminderStore.Pharmacy pharmacy : MediminderStore.getPharmacies()) {
            if (matches(query, pharmacy.name, pharmacy.location)) {
                saved.append(pharmacy.name).append(" — ").append(pharmacy.location).append('\n');
            }
        }
        if (matched == null) {
            text.append(saved.length() > 0 ? "SAVED PHARMACIES\n" + saved
                : query.isEmpty() ? "No medicines or pharmacies have been added yet."
                : "No saved medicine or pharmacy matches that search.");
        } else {
            if (!query.isEmpty() && saved.length() > 0) {
                text.append("SAVED PHARMACIES\n").append(saved).append('\n');
            }
            text.append("PHARMACIES FOR ").append(matched.name.toUpperCase())
                .append(" (in-stock first)\n");
            List<MediminderStore.Pharmacy> ranked = MediminderStore.getRankedPharmacies(matched.id);
            if (ranked.isEmpty()) {
                text.append("No pharmacies saved. Add a pharmacy below to keep stock information.\n");
            }
            for (MediminderStore.Pharmacy pharmacy : ranked) {
                text.append("• ").append(pharmacy.name).append(" — ")
                    .append(pharmacy.location).append(" — ")
                    .append(MediminderStore.getPharmacyStock(pharmacy, matched.id)).append('\n');
            }
        }
        results.setText(text.toString());
        results.setCaretPosition(0);
    }

    /** Runs a store action and shows any validation/storage error. */
    private void attempt(Runnable action) {
        try {
            action.run();
            refreshResults();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            showError(ex.getMessage());
        }
    }

    private void addPharmacy() {
        JTextField name = new JTextField();
        JTextField location = new JTextField(locationField.getText());
        JPanel fields = new JPanel(new GridLayout(0, 2, 8, 8));
        fields.add(new JLabel("Pharmacy name:"));
        fields.add(name);
        fields.add(new JLabel("City / neighborhood:"));
        fields.add(location);
        if (JOptionPane.showConfirmDialog(this, fields, "Save pharmacy",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
            attempt(() -> {
                MediminderStore.addPharmacy(name.getText(), location.getText());
                locationField.setText(location.getText());
            });
        }
    }

    private void updateStockStatus() {
        List<MediminderStore.Medicine> medicines = MediminderStore.getMedicines();
        List<MediminderStore.Pharmacy> pharmacies = MediminderStore.getPharmacies();
        if (medicines.isEmpty() || pharmacies.isEmpty()) {
            showError("Add at least one medicine and one saved pharmacy first.");
            return;
        }
        JComboBox<MediminderStore.Medicine> medicine =
            new JComboBox<>(medicines.toArray(new MediminderStore.Medicine[0]));
        JComboBox<MediminderStore.Pharmacy> pharmacy =
            new JComboBox<>(pharmacies.toArray(new MediminderStore.Pharmacy[0]));
        JComboBox<String> status =
            new JComboBox<>(new String[] {"Not checked", "In stock", "Out of stock"});
        JPanel fields = new JPanel(new GridLayout(0, 2, 8, 8));
        fields.add(new JLabel("Medicine:"));
        fields.add(medicine);
        fields.add(new JLabel("Pharmacy:"));
        fields.add(pharmacy);
        fields.add(new JLabel("Stock status:"));
        fields.add(status);
        if (JOptionPane.showConfirmDialog(this, fields, "Update stock status",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return;
        }
        String code = new String[] {"UNKNOWN", "IN_STOCK", "OUT_OF_STOCK"}[status.getSelectedIndex()];
        attempt(() -> MediminderStore.setPharmacyStock(
            ((MediminderStore.Pharmacy) pharmacy.getSelectedItem()).id,
            ((MediminderStore.Medicine) medicine.getSelectedItem()).id, code));
    }

    private void deletePharmacy() {
        List<MediminderStore.Pharmacy> pharmacies = MediminderStore.getPharmacies();
        if (pharmacies.isEmpty()) {
            showError("There are no saved pharmacies to delete.");
            return;
        }
        JComboBox<MediminderStore.Pharmacy> choice =
            new JComboBox<>(pharmacies.toArray(new MediminderStore.Pharmacy[0]));
        if (JOptionPane.showConfirmDialog(this, choice, "Choose a pharmacy to delete",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return;
        }
        MediminderStore.Pharmacy selected = (MediminderStore.Pharmacy) choice.getSelectedItem();
        if (JOptionPane.showConfirmDialog(this,
                "Delete " + selected.name + " and its stock records?",
                "Delete pharmacy", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            attempt(() -> MediminderStore.removePharmacy(selected.id));
        }
    }

    private void openMapSearch() {
        String medicine = searchField.getText().trim();
        if (medicine.isEmpty()) {
            showError("Enter a medicine name before searching for nearby pharmacies.");
            return;
        }
        String location = locationField.getText().trim();
        String query = "pharmacy " + medicine + (location.isEmpty() ? "" : " " + location);
        try {
            if (!Desktop.isDesktopSupported()
                    || !Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                throw new IOException("Opening web links is not supported on this computer.");
            }
            String encoded = URLEncoder.encode(query, "UTF-8").replace("+", "%20");
            Desktop.getDesktop().browse(URI.create(
                "https://www.openstreetmap.org/search?query=" + encoded));
        } catch (IOException | RuntimeException ex) {
            showError("Could not open the pharmacy map: " + ex.getMessage());
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "MediMinder", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MediminderSearchUI().setVisible(true));
    }
}
