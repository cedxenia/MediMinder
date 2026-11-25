package Frontend;
import javax.swing.SwingUtilities;



public class Mediminder{
    public static void main(String[] args){
        SwingUtilities.invokeLater(() -> {
            MediminderWelcomeUI welcomeScreen = new MediminderWelcomeUI();
            welcomeScreen.setVisible(true);
        });
    }
}