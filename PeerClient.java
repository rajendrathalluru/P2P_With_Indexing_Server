import java.io.*;
import java.net.*;
import java.util.Scanner;

public class PeerClient {
    // Change the directory to your desired location, e.g., "C:\\files\\" for Windows
    private static final String SHARED_DIR = "C:\\files1\\";

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);

        // Continuous loop until user types "exit"
        while (true) {
            System.out.println("Choose an action: 'register' to register a file, 'search' to search for files, 'download' to download a file, 'delete' to delete a file, or 'exit' to exit.");
            String action = scanner.nextLine().toLowerCase();

            switch (action) {
                case "register":
                    registerFile(scanner);
                    break;
                case "search":
                    searchFile(scanner);
                    break;
                case "download":
                    downloadFile(scanner);
                    break;
                case "delete":
                    deleteFile(scanner);
                    break;
                case "exit":
                    System.out.println("Exiting the program.");
                    return;
                default:
                    System.out.println("Invalid action.");
                    break;
            }
        }
    }

    // Register a specific file with the CentralServer
    private static void registerFile(Scanner scanner) {
        System.out.print("Enter your peer ID: ");
        String peerId = scanner.nextLine();

        System.out.print("Enter the file name to register: ");
        String fileName = scanner.nextLine();

        // Ensure the file exists in the shared directory
        File file = new File(SHARED_DIR + fileName);
        if (!file.exists()) {
            System.out.println("File does not exist in the shared directory.");
            return;
        }

        String peerIp = "localhost";  // Assuming all peers run on localhost for simplicity

        // Connect to the central server and send registration request
        try (Socket socket = new Socket("localhost", 5000);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            // Send registration command to the server
            String registerCommand = "REGISTER " + peerId + " " + peerIp + " " + file.getName();
            out.println(registerCommand);
            System.out.println("Register command sent: " + registerCommand);

            // Read and display the server's response
            String response = in.readLine();
            System.out.println("Server response: " + response);

        } catch (IOException e) {
            System.out.println("Error while registering the file: " + e.getMessage());
        }
    }

    // Search for a file on the CentralServer
    private static void searchFile(Scanner scanner) {
        System.out.print("Enter the file name you want to search: ");
        String fileName = scanner.nextLine();

        try (Socket socket = new Socket("localhost", 5000);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            // Send search command to the server
            out.println("SEARCH " + fileName);
            String response = in.readLine();
            System.out.println("Peers with the file: " + response);

        } catch (IOException e) {
            System.out.println("Error while searching for the file: " + e.getMessage());
        }
    }

    // Download a file from another peer
    private static void downloadFile(Scanner scanner) {
        System.out.print("Enter the IP address to download the file from: ");
        String peerIp = scanner.nextLine();

        System.out.print("Enter the file name to download: ");
        String fileName = scanner.nextLine();

        try (Socket socket = new Socket(peerIp, 5001);
             InputStream in = socket.getInputStream();
             FileOutputStream fos = new FileOutputStream(SHARED_DIR + fileName)) {

            byte[] buffer = new byte[4096];
            int bytesRead;
            long totalBytesRead = 0;

            // Show progress during download
            System.out.println("Downloading file...");

            // Read and write the file in chunks
            while ((bytesRead = in.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
                totalBytesRead += bytesRead;
                System.out.printf("Downloaded %d bytes.\r", totalBytesRead);
            }

            System.out.println("\nFile downloaded successfully: " + fileName + " from peer " + peerIp);

        } catch (IOException e) {
            System.out.println("Error while downloading the file: " + e.getMessage());
        }
    }

    // Delete a file from the peer and notify the CentralServer to deregister the file
    private static void deleteFile(Scanner scanner) {
        System.out.print("Enter the file name to delete: ");
        String fileName = scanner.nextLine();

        File file = new File(SHARED_DIR + fileName);

        // Ensure the file exists before attempting to delete
        if (file.exists()) {
            if (file.delete()) {
                System.out.println("File deleted: " + fileName);
                deregisterFile(fileName);  // Notify server of file deletion
            } else {
                System.out.println("Failed to delete file.");
            }
        } else {
            System.out.println("File not found: " + fileName);
        }
    }

    // Deregister a file from the CentralServer
    private static void deregisterFile(String fileName) {
        System.out.print("Enter your peer ID to deregister: ");
        Scanner scanner = new Scanner(System.in);
        String peerId = scanner.nextLine();

        try (Socket socket = new Socket("localhost", 5000);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            // Send deregistration command to the server
            out.println("DEREGISTER " + peerId + " " + fileName);
            String response = in.readLine();
            System.out.println("Server response: " + response);

        } catch (IOException e) {
            System.out.println("Error while deregistering the file: " + e.getMessage());
        }
    }
}