import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.List;

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
            @Override
            public void insertUpdate(DocumentEvent event) {
                refreshResults();
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                refreshResults();
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                refreshResults();
            }
        });

        results.setEditable(false);
        results.setLineWrap(true);
        results.setWrapStyleWord(true);
        results.setFont(new Font("SansSerif", Font.PLAIN, 15));
        results.setBackground(new Color(236, 245, 250));
        results.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        JScrollPane resultScroll = new JScrollPane(results);
        resultScroll.setBorder(BorderFactory.createEmptyBorder(0, 25, 0, 25));

        JPanel pharmacyControls = new JPanel(new GridBagLayout());
        pharmacyControls.setOpaque(false);
        pharmacyControls.setBorder(BorderFactory.createEmptyBorder(0, 25, 16, 25));
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(3, 5, 3, 5);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 0;
        constraints.gridy = 0;
        pharmacyControls.add(new JLabel("City / neighborhood (location is entered manually):"),
            constraints);
        constraints.gridx = 1;
        constraints.weightx = 1;
        pharmacyControls.add(locationField, constraints);
        constraints.gridx = 2;
        constraints.weightx = 0;
        JButton nearby = new JButton("Find nearby pharmacies");
        pharmacyControls.add(nearby, constraints);
        constraints.gridx = 0;
        constraints.gridy = 1;
        JButton addPharmacy = new JButton("Add saved pharmacy");
        pharmacyControls.add(addPharmacy, constraints);
        constraints.gridx = 1;
        JButton updateStock = new JButton("Update medicine stock status");
        pharmacyControls.add(updateStock, constraints);
        constraints.gridx = 2;
        JButton deletePharmacy = new JButton("Delete saved pharmacy");
        pharmacyControls.add(deletePharmacy, constraints);

        nearby.addActionListener(event -> openMapSearch());
        addPharmacy.addActionListener(event -> addPharmacy());
        updateStock.addActionListener(event -> updateStockStatus());
        deletePharmacy.addActionListener(event -> deletePharmacy());

        JPanel north = new JPanel(new BorderLayout());
        north.setOpaque(false);
        north.add(header, BorderLayout.NORTH);
        JLabel explanation = new JLabel("Saved medicine search and pharmacy recommendations",
            SwingConstants.CENTER);
        explanation.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
        north.add(explanation, BorderLayout.SOUTH);
        add(north, BorderLayout.NORTH);
        add(resultScroll, BorderLayout.CENTER);
        add(pharmacyControls, BorderLayout.SOUTH);
        refreshResults();
        setLocationRelativeTo(null);
    }

    private void refreshResults() {
        String query = searchField.getText().trim().toLowerCase();
        StringBuilder text = new StringBuilder();
        MediminderStore.Medicine matchedMedicine = null;
        for (MediminderStore.Medicine medicine : MediminderStore.getMedicines()) {
            if (query.isEmpty() || medicine.name.toLowerCase().contains(query)
                    || medicine.dosage.toLowerCase().contains(query)) {
                if (matchedMedicine == null) {
                    matchedMedicine = medicine;
                }
                text.append("MEDICINE: ").append(medicine.name)
                    .append("\nDosage: ").append(medicine.dosage)
                    .append("\nSchedule: ").append(joinTimes(medicine.times))
                    .append("\nSupply: ").append(medicine.quantityRemaining)
                    .append(" doses (~").append(MediminderStore.getSupplyDays(medicine))
                    .append(" days remaining)")
                    .append(medicine.hardToFind ? "\nHard-to-find alert: 14-day threshold" : "")
                    .append("\n\n");
            }
        }
        StringBuilder savedMatches = new StringBuilder();
        for (MediminderStore.Pharmacy pharmacy : MediminderStore.getPharmacies()) {
            if (query.isEmpty() || pharmacy.name.toLowerCase().contains(query)
                    || pharmacy.location.toLowerCase().contains(query)) {
                savedMatches.append(pharmacy.name).append(" — ")
                    .append(pharmacy.location).append('\n');
            }
        }
        if (matchedMedicine == null) {
            if (savedMatches.length() > 0) {
                text.append("SAVED PHARMACIES\n").append(savedMatches);
            } else {
                text.append(query.isEmpty()
                    ? "No medicines or pharmacies have been added yet."
                    : "No saved medicine or pharmacy matches that search.");
            }
        } else {
            if (!query.isEmpty() && savedMatches.length() > 0) {
                text.append("SAVED PHARMACIES\n").append(savedMatches).append('\n');
            }
            text.append("PHARMACIES FOR ").append(matchedMedicine.name.toUpperCase())
                .append(" (in-stock first)\n");
            List<MediminderStore.Pharmacy> pharmacies =
                MediminderStore.getRankedPharmacies(matchedMedicine.id);
            if (pharmacies.isEmpty()) {
                text.append("No pharmacies saved. Add a pharmacy below to keep stock information.\n");
            }
            for (MediminderStore.Pharmacy pharmacy : pharmacies) {
                text.append("• ").append(pharmacy.name).append(" — ")
                    .append(pharmacy.location).append(" — ")
                    .append(MediminderStore.getPharmacyStock(pharmacy, matchedMedicine.id))
                    .append('\n');
            }
        }
        results.setText(text.toString());
        results.setCaretPosition(0);
    }

    private String joinTimes(List<java.time.LocalTime> times) {
        StringBuilder value = new StringBuilder();
        for (java.time.LocalTime time : times) {
            if (value.length() > 0) {
                value.append(", ");
            }
            value.append(time.toString());
        }
        return value.toString();
    }

    private void addPharmacy() {
        JTextField name = new JTextField();
        JTextField location = new JTextField(locationField.getText());
        JPanel fields = new JPanel(new GridLayout(0, 2, 8, 8));
        fields.add(new JLabel("Pharmacy name:"));
        fields.add(name);
        fields.add(new JLabel("City / neighborhood:"));
        fields.add(location);
        int result = JOptionPane.showConfirmDialog(this, fields, "Save pharmacy",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }
        try {
            MediminderStore.addPharmacy(name.getText(), location.getText());
            locationField.setText(location.getText());
            refreshResults();
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        } catch (IllegalStateException ex) {
            showError(ex.getMessage());
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
            new JComboBox<MediminderStore.Medicine>(
                medicines.toArray(new MediminderStore.Medicine[medicines.size()]));
        JComboBox<MediminderStore.Pharmacy> pharmacy =
            new JComboBox<MediminderStore.Pharmacy>(
                pharmacies.toArray(new MediminderStore.Pharmacy[pharmacies.size()]));
        JComboBox<String> status = new JComboBox<String>(
            new String[] {"Not checked", "In stock", "Out of stock"});
        JPanel fields = new JPanel(new GridLayout(0, 2, 8, 8));
        fields.add(new JLabel("Medicine:"));
        fields.add(medicine);
        fields.add(new JLabel("Pharmacy:"));
        fields.add(pharmacy);
        fields.add(new JLabel("Stock status:"));
        fields.add(status);
        int result = JOptionPane.showConfirmDialog(this, fields, "Update stock status",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }
        MediminderStore.Medicine selectedMedicine =
            (MediminderStore.Medicine) medicine.getSelectedItem();
        MediminderStore.Pharmacy selectedPharmacy =
            (MediminderStore.Pharmacy) pharmacy.getSelectedItem();
        String selectedStatus = (String) status.getSelectedItem();
        try {
            MediminderStore.setPharmacyStock(selectedPharmacy.id, selectedMedicine.id,
                "In stock".equals(selectedStatus) ? "IN_STOCK"
                    : "Out of stock".equals(selectedStatus) ? "OUT_OF_STOCK" : "UNKNOWN");
            refreshResults();
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        } catch (IllegalStateException ex) {
            showError(ex.getMessage());
        }
    }

    private void deletePharmacy() {
        List<MediminderStore.Pharmacy> pharmacies = MediminderStore.getPharmacies();
        if (pharmacies.isEmpty()) {
            showError("There are no saved pharmacies to delete.");
            return;
        }
        JComboBox<MediminderStore.Pharmacy> pharmacy =
            new JComboBox<MediminderStore.Pharmacy>(
                pharmacies.toArray(new MediminderStore.Pharmacy[pharmacies.size()]));
        int selected = JOptionPane.showConfirmDialog(this, pharmacy,
            "Choose a pharmacy to delete", JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE);
        if (selected != JOptionPane.OK_OPTION) {
            return;
        }
        MediminderStore.Pharmacy selectedPharmacy =
            (MediminderStore.Pharmacy) pharmacy.getSelectedItem();
        int confirmation = JOptionPane.showConfirmDialog(this,
            "Delete " + selectedPharmacy.name + " and its stock records?",
            "Delete pharmacy", JOptionPane.YES_NO_OPTION);
        if (confirmation == JOptionPane.YES_OPTION) {
            try {
                MediminderStore.removePharmacy(selectedPharmacy.id);
                refreshResults();
            } catch (IllegalArgumentException ex) {
                showError(ex.getMessage());
            } catch (IllegalStateException ex) {
                showError(ex.getMessage());
            }
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
        } catch (IOException ex) {
            showError("Could not open the pharmacy map: " + ex.getMessage());
        } catch (SecurityException ex) {
            showError("Permission to open the pharmacy map was denied.");
        } catch (UnsupportedOperationException ex) {
            showError("Opening web links is not supported on this computer.");
        } catch (IllegalArgumentException ex) {
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
