import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class MediminderStore {
    private static final Path DATA_FILE = Paths.get(
        System.getProperty("user.home"), ".mediminder", "mediminder-data.bin");
    private static final DateTimeFormatter DATE_TIME_FORMAT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static Data data = load();

    private MediminderStore() {
    }

    // ---------- User ----------

    public static synchronized String getUserName() {
        return data.userName == null ? "" : data.userName;
    }

    public static synchronized void setUserName(String userName) {
        check(!blank(userName), "Enter your name.");
        data.userName = userName.trim();
        save();
    }

    // ---------- Medicines ----------

    public static synchronized List<Medicine> getMedicines() {
        return new ArrayList<Medicine>(data.medicines);
    }

    public static synchronized Medicine findMedicine(String id) {
        for (Medicine medicine : data.medicines) {
            if (medicine.id.equals(id)) {
                return medicine;
            }
        }
        return null;
    }

    public static synchronized Medicine addMedicine(String name, String dosage,
            List<LocalTime> times, int quantity, boolean hardToFind) {
        check(!blank(name), "Enter a medicine name.");
        check(!blank(dosage), "Enter a dosage.");
        check(times != null && !times.isEmpty(), "Enter at least one reminder time.");
        check(quantity >= 1, "Quantity must be greater than zero.");
        List<LocalTime> schedule = new ArrayList<LocalTime>(times);
        Collections.sort(schedule);
        Medicine medicine = new Medicine(UUID.randomUUID().toString(), name.trim(),
            dosage.trim(), schedule, quantity, LocalDate.now(), hardToFind);
        data.medicines.add(medicine);
        save();
        return medicine;
    }

    public static synchronized void updateSupply(String medicineId, int quantity) {
        check(quantity >= 0, "Quantity cannot be negative.");
        requireMedicine(medicineId).quantityRemaining = quantity;
        save();
    }

    public static synchronized void setHardToFind(String medicineId, boolean value) {
        requireMedicine(medicineId).hardToFind = value;
        save();
    }

    public static synchronized void removeMedicine(String medicineId) {
        requireMedicine(medicineId);
        data.medicines.removeIf(medicine -> medicine.id.equals(medicineId));
        data.doses.removeIf(dose -> dose.medicineId.equals(medicineId));
        data.notes.removeIf(note -> note.medicineId.equals(medicineId));
        for (Pharmacy pharmacy : data.pharmacies) {
            pharmacy.stockByMedicine.remove(medicineId);
        }
        save();
    }

    public static synchronized int getSupplyDays(Medicine medicine) {
        return medicine.times.isEmpty() ? 0 : medicine.quantityRemaining / medicine.times.size();
    }

    public static synchronized int getLowSupplyThreshold(Medicine medicine) {
        return medicine.hardToFind ? 14 : 3;
    }

    // ---------- Doses ----------

    public static synchronized void markDose(String medicineId, LocalDateTime scheduledAt,
            String status) {
        check("TAKEN".equals(status) || "SKIPPED".equals(status),
            "Dose status must be TAKEN or SKIPPED.");
        Medicine medicine = requireMedicine(medicineId);
        Dose dose = findDose(medicineId, scheduledAt);
        String oldStatus = dose == null ? "" : dose.status;
        if (status.equals(oldStatus)) {
            return;
        }
        if ("TAKEN".equals(status)) {
            check(medicine.quantityRemaining >= 1, "No doses remain in the recorded supply.");
            medicine.quantityRemaining--;
        } else if ("TAKEN".equals(oldStatus)) {
            medicine.quantityRemaining++;
        }
        if (dose == null) {
            dose = new Dose(medicineId, scheduledAt);
            data.doses.add(dose);
        }
        dose.status = status;
        save();
    }

    public static synchronized void clearDoseRecord(String medicineId,
            LocalDateTime scheduledAt) {
        Medicine medicine = requireMedicine(medicineId);
        Dose dose = findDose(medicineId, scheduledAt);
        check(dose != null, "There is no saved status for this dose.");
        if ("TAKEN".equals(dose.status)) {
            medicine.quantityRemaining++;
        }
        data.doses.remove(dose);
        data.notes.removeIf(note -> note.medicineId.equals(medicineId)
            && note.scheduledAt.equals(scheduledAt));
        save();
    }

    public static synchronized String getDoseStatus(String medicineId, LocalDateTime scheduledAt) {
        Dose dose = findDose(medicineId, scheduledAt);
        return dose == null ? "" : dose.status;
    }

    public static synchronized List<Dose> getDoses() {
        return new ArrayList<Dose>(data.doses);
    }

    // ---------- Notes & health profile ----------

    public static synchronized void addNote(String medicineId, LocalDateTime scheduledAt,
            String feeling, String details) {
        requireMedicine(medicineId);
        check(!blank(feeling), "Enter how you felt.");
        check("TAKEN".equals(getDoseStatus(medicineId, scheduledAt)),
            "Mark this dose as taken before adding a health note.");
        data.notes.add(new HealthNote(medicineId, LocalDateTime.now(), scheduledAt,
            feeling.trim(), details == null ? "" : details.trim()));
        save();
    }

    public static synchronized List<HealthNote> getNotes() {
        return new ArrayList<HealthNote>(data.notes);
    }

    public static synchronized void deleteNote(HealthNote note) {
        check(note != null && data.notes.remove(note), "The selected note no longer exists.");
        save();
    }

    public static synchronized void deleteAllNotes() {
        data.notes.clear();
        save();
    }

    public static synchronized void setHealthProfile(String allergies, String conditions) {
        data.allergies = allergies == null ? "" : allergies.trim();
        data.conditions = conditions == null ? "" : conditions.trim();
        save();
    }

    public static synchronized String getAllergies() {
        return data.allergies;
    }

    public static synchronized String getConditions() {
        return data.conditions;
    }

    // ---------- Pharmacies ----------

    public static synchronized Pharmacy addPharmacy(String name, String location) {
        check(!blank(name), "Enter a pharmacy name.");
        check(!blank(location), "Enter a city or neighborhood.");
        Pharmacy pharmacy = new Pharmacy(UUID.randomUUID().toString(),
            name.trim(), location.trim());
        data.pharmacies.add(pharmacy);
        save();
        return pharmacy;
    }

    public static synchronized List<Pharmacy> getPharmacies() {
        return new ArrayList<Pharmacy>(data.pharmacies);
    }

    public static synchronized void removePharmacy(String pharmacyId) {
        check(data.pharmacies.removeIf(pharmacy -> pharmacy.id.equals(pharmacyId)),
            "The selected pharmacy no longer exists.");
        save();
    }

    public static synchronized void setPharmacyStock(String pharmacyId, String medicineId,
            String status) {
        check(Arrays.asList("IN_STOCK", "OUT_OF_STOCK", "UNKNOWN").contains(status),
            "Choose a valid stock status.");
        requireMedicine(medicineId);
        Pharmacy target = null;
        for (Pharmacy pharmacy : data.pharmacies) {
            if (pharmacy.id.equals(pharmacyId)) {
                target = pharmacy;
            }
        }
        check(target != null, "The selected pharmacy no longer exists.");
        target.stockByMedicine.put(medicineId, status);
        save();
    }

    public static synchronized List<Pharmacy> getRankedPharmacies(String medicineId) {
        requireMedicine(medicineId);
        List<Pharmacy> result = new ArrayList<Pharmacy>(data.pharmacies);
        result.sort(Comparator.comparingInt((Pharmacy pharmacy) ->
            stockRank(pharmacy.stockByMedicine.get(medicineId))));
        return result;
    }

    public static synchronized String getPharmacyStock(Pharmacy pharmacy, String medicineId) {
        String status = pharmacy.stockByMedicine.get(medicineId);
        return "IN_STOCK".equals(status) ? "In stock"
            : "OUT_OF_STOCK".equals(status) ? "Out of stock" : "Not checked";
    }

    // ---------- Doctor report ----------

    public static synchronized String buildReport(int days) {
        check(days == 14 || days == 30 || days == 90, "Choose a 14, 30, or 90 day report.");
        LocalDate today = LocalDate.now();
        LocalDate windowStart = today.minusDays(days - 1);
        LocalDateTime now = LocalDateTime.now();
        StringBuilder report = new StringBuilder("MEDIMINDER DOCTOR VISIT SUMMARY\n")
            .append(windowStart).append(" to ").append(today)
            .append(" (").append(days).append(" days)\n\n");

        int totalScheduled = 0;
        int totalTaken = 0;
        int totalSkipped = 0;
        Map<String, Integer> takenByMedicine = new HashMap<String, Integer>();

        for (Medicine medicine : data.medicines) {
            int scheduled = 0;
            int taken = 0;
            int skipped = 0;
            LocalDate firstDay = medicine.startDate.isAfter(windowStart)
                ? medicine.startDate : windowStart;
            for (LocalDate day = firstDay; !day.isAfter(today); day = day.plusDays(1)) {
                for (LocalTime time : medicine.times) {
                    LocalDateTime scheduledAt = LocalDateTime.of(day, time);
                    if (scheduledAt.isAfter(now)) {
                        continue;
                    }
                    scheduled++;
                    String status = getDoseStatus(medicine.id, scheduledAt);
                    if ("TAKEN".equals(status)) {
                        taken++;
                    } else if ("SKIPPED".equals(status)) {
                        skipped++;
                    }
                }
            }
            totalScheduled += scheduled;
            totalTaken += taken;
            totalSkipped += skipped;
            takenByMedicine.put(medicine.id, taken);
            report.append(medicine.name).append(": Taken ").append(taken)
                .append(" of ").append(scheduled).append(" (")
                .append(percent(taken, scheduled)).append("%), skipped ").append(skipped)
                .append(". Supply: ").append(medicine.quantityRemaining)
                .append(" doses (~").append(getSupplyDays(medicine)).append(" days left).");
            if (medicine.quantityRemaining / (double) medicine.times.size()
                    <= getLowSupplyThreshold(medicine)) {
                report.append(" LOW SUPPLY");
            }
            report.append('\n');
        }
        report.append("\nOverall adherence: Taken ").append(totalTaken)
            .append(" of ").append(totalScheduled).append(" (")
            .append(percent(totalTaken, totalScheduled)).append("%); skipped ")
            .append(totalSkipped).append(".\n");

        report.append("\nRECURRING FEELINGS\n");
        boolean hasPattern = false;
        for (Medicine medicine : data.medicines) {
            int takenDoses = takenByMedicine.get(medicine.id);
            if (takenDoses == 0) {
                continue;
            }
            Map<String, Set<LocalDateTime>> feelingDoses =
                new HashMap<String, Set<LocalDateTime>>();
            for (HealthNote note : data.notes) {
                if (note.medicineId.equals(medicine.id) && inWindow(note, windowStart, today)
                        && "TAKEN".equals(getDoseStatus(note.medicineId, note.scheduledAt))) {
                    feelingDoses.computeIfAbsent(note.feeling.toLowerCase(),
                        key -> new HashSet<LocalDateTime>()).add(note.scheduledAt);
                }
            }
            for (Map.Entry<String, Set<LocalDateTime>> entry : feelingDoses.entrySet()) {
                int occurrences = entry.getValue().size();
                if (occurrences >= 2 && occurrences / (double) takenDoses >= 0.4) {
                    report.append(medicine.name).append(": ").append(entry.getKey())
                        .append(" after ").append(occurrences).append(" of ")
                        .append(takenDoses).append(" taken doses.\n");
                    hasPattern = true;
                }
            }
        }
        if (!hasPattern) {
            report.append("No recurring feeling met the reporting threshold.\n");
        }

        report.append("\nHOW I FELT\n");
        boolean hasNotes = false;
        for (HealthNote note : data.notes) {
            if (inWindow(note, windowStart, today)) {
                Medicine medicine = findMedicine(note.medicineId);
                report.append(note.createdAt.format(DATE_TIME_FORMAT)).append(" - ")
                    .append(medicine == null ? "Medicine removed" : medicine.name)
                    .append(": ").append(note.feeling);
                if (!note.details.isEmpty()) {
                    report.append(" — ").append(note.details);
                }
                report.append('\n');
                hasNotes = true;
            }
        }
        if (!hasNotes) {
            report.append("No notes recorded in this period.\n");
        }
        return report.append("\nAllergies: ").append(orNone(data.allergies))
            .append("\nConditions: ").append(orNone(data.conditions)).toString();
    }

    // ---------- Helpers ----------

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }

    private static boolean blank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static String orNone(String value) {
        return blank(value) ? "None recorded" : value;
    }

    private static int percent(int part, int total) {
        return total == 0 ? 0 : (int) Math.round(part * 100.0 / total);
    }

    private static boolean inWindow(HealthNote note, LocalDate start, LocalDate end) {
        LocalDate date = note.createdAt.toLocalDate();
        return !date.isBefore(start) && !date.isAfter(end);
    }

    private static int stockRank(String status) {
        return "IN_STOCK".equals(status) ? 0 : "OUT_OF_STOCK".equals(status) ? 2 : 1;
    }

    private static Medicine requireMedicine(String id) {
        Medicine medicine = findMedicine(id);
        check(medicine != null, "The selected medicine no longer exists.");
        return medicine;
    }

    private static Dose findDose(String medicineId, LocalDateTime scheduledAt) {
        for (Dose dose : data.doses) {
            if (dose.medicineId.equals(medicineId) && dose.scheduledAt.equals(scheduledAt)) {
                return dose;
            }
        }
        return null;
    }

    // ---------- Persistence ----------

    private static Data load() {
        if (!Files.exists(DATA_FILE)) {
            return new Data();
        }
        try (ObjectInputStream input = new ObjectInputStream(Files.newInputStream(DATA_FILE))) {
            Object stored = input.readObject();
            if (!(stored instanceof Data)) {
                throw new IllegalStateException("The MediMinder data file has an invalid format.");
            }
            return (Data) stored;
        } catch (IOException | ClassNotFoundException ex) {
            throw new IllegalStateException("Could not read MediMinder data from "
                + DATA_FILE + ".", ex);
        }
    }

    private static void save() {
        Path parent = DATA_FILE.getParent();
        Path temporary = null;
        try {
            Files.createDirectories(parent);
            temporary = Files.createTempFile(parent, "mediminder-", ".tmp");
            try (ObjectOutputStream output = new ObjectOutputStream(
                    Files.newOutputStream(temporary))) {
                output.writeObject(data);
            }
            try {
                Files.move(temporary, DATA_FILE, StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException ex) {
                Files.move(temporary, DATA_FILE, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Could not save MediMinder data to "
                + DATA_FILE + ".", ex);
        } finally {
            try {
                if (temporary != null) {
                    Files.deleteIfExists(temporary);
                }
            } catch (IOException ex) {
                System.err.println("Could not remove temporary data file " + temporary);
            }
        }
    }

    // ---------- Data classes (field names kept so existing saved data still loads) ----------

    private static final class Data implements Serializable {
        private static final long serialVersionUID = 1L;
        private final List<Medicine> medicines = new ArrayList<Medicine>();
        private final List<Dose> doses = new ArrayList<Dose>();
        private final List<HealthNote> notes = new ArrayList<HealthNote>();
        private final List<Pharmacy> pharmacies = new ArrayList<Pharmacy>();
        private String userName = "";
        private String allergies = "";
        private String conditions = "";
    }

    public static final class Medicine implements Serializable {
        private static final long serialVersionUID = 1L;
        public final String id;
        public final String name;
        public final String dosage;
        public final List<LocalTime> times;
        public final LocalDate startDate;
        public int quantityRemaining;
        public boolean hardToFind;

        private Medicine(String id, String name, String dosage, List<LocalTime> times,
                int quantityRemaining, LocalDate startDate, boolean hardToFind) {
            this.id = id;
            this.name = name;
            this.dosage = dosage;
            this.times = times;
            this.quantityRemaining = quantityRemaining;
            this.startDate = startDate;
            this.hardToFind = hardToFind;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public static final class Dose implements Serializable {
        private static final long serialVersionUID = 1L;
        public final String medicineId;
        public final LocalDateTime scheduledAt;
        public String status = "";

        private Dose(String medicineId, LocalDateTime scheduledAt) {
            this.medicineId = medicineId;
            this.scheduledAt = scheduledAt;
        }
    }

    public static final class HealthNote implements Serializable {
        private static final long serialVersionUID = 1L;
        public final String medicineId;
        public final LocalDateTime createdAt;
        public final LocalDateTime scheduledAt;
        public final String feeling;
        public final String details;

        private HealthNote(String medicineId, LocalDateTime createdAt, LocalDateTime scheduledAt,
                String feeling, String details) {
            this.medicineId = medicineId;
            this.createdAt = createdAt;
            this.scheduledAt = scheduledAt;
            this.feeling = feeling;
            this.details = details;
        }
    }

    public static final class Pharmacy implements Serializable {
        private static final long serialVersionUID = 1L;
        public final String id;
        public final String name;
        public final String location;
        private final Map<String, String> stockByMedicine = new HashMap<String, String>();

        private Pharmacy(String id, String name, String location) {
            this.id = id;
            this.name = name;
            this.location = location;
        }

        @Override
        public String toString() {
            return name + " (" + location + ")";
        }
    }
}
