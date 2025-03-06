import javax.swing.*;
import java.io.IOException;

public class TestClient {
    public static void main(String[] args) throws IOException {
        SwingUtilities.invokeLater(() -> {
            try {
                new ContactClientGUI().setVisible(true);
               // new ContactClientGUI().setVisible(true);
                //new ContactClientGUI().setVisible(true);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null,
                        "Error connecting to server: " + e.getMessage(),
                        "Connection Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }

}
