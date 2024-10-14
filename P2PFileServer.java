import java.util.concurrent.*;
import java.io.*;
import java.net.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class P2PFileServer {
    private static final String SHARED_DIR = "shared/";

    public static void main(String[] args) {
        try (ServerSocket peerSocket = new ServerSocket(5001)) {
            System.out.println("Peer server is running on port 5001...");

            ExecutorService threadPool = Executors.newFixedThreadPool(5);  // Max 5 concurrent file transfers

            while (true) {
                Socket incomingRequest = peerSocket.accept();
                threadPool.submit(new FileTransferHandler(incomingRequest));
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    static class FileTransferHandler implements Runnable {
        private final Socket clientRequest;

        public FileTransferHandler(Socket socket) {
            this.clientRequest = socket;
        }

        @Override
        public void run() {
            try (BufferedReader inputReader = new BufferedReader(new InputStreamReader(clientRequest.getInputStream()));
                 OutputStream outputStream = clientRequest.getOutputStream()) {

                String clientMessage = inputReader.readLine();
                String[] messageParts = clientMessage.split(" ");
                String operation = messageParts[0];

                if (operation.equals("RETRIEVE")) {
                    String requestedFile = messageParts[1];
                    File fileToRetrieve = new File(SHARED_DIR + requestedFile);

                    if (fileToRetrieve.exists() && !fileToRetrieve.isDirectory()) {
                        try (FileInputStream fileReader = new FileInputStream(fileToRetrieve)) {
                            long fileSize = fileToRetrieve.length();
                            String fileChecksum = calculateChecksum(fileToRetrieve);  // Calculate file checksum

                            // Send file size and checksum to the client
                            outputStream.write(("SIZE " + fileSize + " CHECKSUM " + fileChecksum + "\n").getBytes());
                            outputStream.flush();

                            byte[] dataBuffer = new byte[4096];
                            int readBytes;

                            while ((readBytes = fileReader.read(dataBuffer)) != -1) {
                                outputStream.write(dataBuffer, 0, readBytes);
                            }

                            System.out.println("File Sent: " + requestedFile);

                        } catch (IOException e) {
                            System.out.println("Error reading file: " + requestedFile);
                        }
                    } else {
                        outputStream.write("ERROR File Not Found\n".getBytes());
                        System.out.println("File Not Found: " + requestedFile);
                    }
                } else {
                    outputStream.write("ERROR Invalid Command\n".getBytes());
                    System.out.println("Invalid command received");
                }

            } catch (IOException ioException) {
                ioException.printStackTrace();
            } finally {
                try {
                    clientRequest.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // Method to calculate file checksum (e.g., MD5 or SHA-256)
        private String calculateChecksum(File file) {
            try {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                try (FileInputStream fis = new FileInputStream(file)) {
                    byte[] byteArray = new byte[1024];
                    int bytesCount;
                    while ((bytesCount = fis.read(byteArray)) != -1) {
                        digest.update(byteArray, 0, bytesCount);
                    }
                }

                byte[] bytes = digest.digest();
                StringBuilder sb = new StringBuilder();
                for (byte b : bytes) {
                    sb.append(String.format("%02x", b));  // Convert byte to hex
                }
                return sb.toString();
            } catch (NoSuchAlgorithmException | IOException e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}

