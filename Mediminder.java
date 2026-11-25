
import javax.swing.SwingUtilities;



public class Mediminder{
    public static void main(String[] args){
        SwingUtilities.invokeLater(() -> {
            new MediminderWelcomeUI().setVisible(true);
            MediminderReminderService.start();
        });
    }
}