import javax.swing.JOptionPane;
import javax.swing.Timer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

public final class MediminderReminderService {
    private static final Set<String> ALERTED_SLOTS = new HashSet<String>();
    private static LocalDate alertedDate = LocalDate.now();
    private static Timer timer;

    private MediminderReminderService() {
    }

    public static void start() {
        if (timer == null) {
            timer = new Timer(30000, event -> checkDueDoses());
            timer.start();
            checkDueDoses();
        }
    }

    private static void checkDueDoses() {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();
        if (!today.equals(alertedDate)) {
            ALERTED_SLOTS.clear();
            alertedDate = today;
        }
        for (MediminderStore.Medicine medicine : MediminderStore.getMedicines()) {
            for (LocalTime time : medicine.times) {
                LocalDateTime scheduledAt = LocalDateTime.of(today, time);
                if (!now.isBefore(scheduledAt) && now.isBefore(scheduledAt.plusMinutes(2))
                        && MediminderStore.getDoseStatus(medicine.id, scheduledAt).isEmpty()
                        && ALERTED_SLOTS.add(medicine.id + scheduledAt)) {
                    showReminder(medicine, scheduledAt);
                    return;
                }
            }
        }
    }

    private static void showReminder(MediminderStore.Medicine medicine,
            LocalDateTime scheduledAt) {
        int choice = JOptionPane.showOptionDialog(null,
            medicine.name + " (" + medicine.dosage + ") is scheduled now.",
            "MediMinder reminder", JOptionPane.DEFAULT_OPTION,
            JOptionPane.INFORMATION_MESSAGE, null,
            new String[] {"Mark taken", "Skip", "Later"}, "Mark taken");
        if (choice == 0 || choice == 1) {
            try {
                MediminderStore.markDose(medicine.id, scheduledAt,
                    choice == 0 ? "TAKEN" : "SKIPPED");
            } catch (IllegalArgumentException | IllegalStateException ex) {
                JOptionPane.showMessageDialog(null, ex.getMessage(),
                    "MediMinder", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
