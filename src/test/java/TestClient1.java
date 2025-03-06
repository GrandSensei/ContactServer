import javax.swing.*;
import java.io.IOException;

public class TestClient1 {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new ContactClientGUI().setVisible(true);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null,
                        "Error connecting to server: " + e.getMessage(),
                        "Connection Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
