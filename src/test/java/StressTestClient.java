import javax.net.ssl.*;
import java.io.*;
import java.security.KeyStore;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StressTestClient implements Runnable {
    private final String serverAddress;
    private final int serverPort;

    public StressTestClient(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
    }

    public static void main(String[] args) {
        // Change this number to increase/decrease the number of simulated clients.
        int clientCount = 100;
        // Using a fixed thread pool to control concurrency
        ExecutorService executor = Executors.newFixedThreadPool(20);

        for (int i = 0; i < clientCount; i++) {
            executor.submit(new StressTestClient("localhost", 12345));
        }
        executor.shutdown();
    }

    @Override
    public void run() {
        try {
            // Set up the SSL context for the client.
            SSLContext sslContext = SSLContext.getInstance("TLS");
            KeyStore trustStore = KeyStore.getInstance("JKS");
            try (FileInputStream tsIS = new FileInputStream("clienttruststore.jks")) {
                trustStore.load(tsIS, "changeit".toCharArray());
            }
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
            tmf.init(trustStore);
            sslContext.init(null, tmf.getTrustManagers(), null);
            SSLSocketFactory ssf = sslContext.getSocketFactory();

            // Create and connect the SSLSocket.
            try (SSLSocket socket = (SSLSocket) ssf.createSocket(serverAddress, serverPort);
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                //set allowed type.
                out.println(new Message(Message.Type.SET_ALLOWED_TYPE, "SALES").toString());

                //GET_CONTACTS request.
                out.println(new Message(Message.Type.GET_CONTACTS, "").toString());


               out.println(new Message(Message.Type.ADD_CONTACT, "TestName::go@ds.cdf:TestComment:lllloook").toString());

                // Read and print the server's response.
                String response;
                while ((response = in.readLine()) != null) {
                    System.out.println("Client " + Thread.currentThread().getName() + " received: " + response);
                    // Break after reading one response if you don't want to hang.
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println("Client error on thread " + Thread.currentThread().getName() + ": " + e.getMessage());
        }
    }
}
