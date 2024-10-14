import java.io.*;
import java.net.*;
import java.util.Scanner;

public class PeerClientExperiment
 {
    // Change the directory to your desired location, e.g., "C:\\files\\" for Windows
    private static final String SHARED_DIR = "C:\\files1\\";
    private static final String OUTPUT_FILE = "response_times_concurent_500.txt";  // File to log response times

    public static void main(String[] args) throws IOException, InterruptedException {
        Scanner scanner = new Scanner(System.in);
        BufferedWriter writer = new BufferedWriter(new FileWriter(OUTPUT_FILE)); // File writer

        while (true) {
            System.out.println("Choose an action: 'sequential' for sequential requests, 'concurrent' for concurrent requests, or 'exit' to exit.");
            String action = scanner.nextLine().toLowerCase();

            switch (action) {
                case "sequential":
                    performSequentialRequests(writer);
                    break;
                case "concurrent":
                    System.out.print("Enter the number of concurrent clients: ");
                    int numClients = Integer.parseInt(scanner.nextLine());
                    performConcurrentRequests(writer, numClients);
                    break;
                case "exit":
                    System.out.println("Exiting the program.");
                    writer.close(); // Close file writer
                    return;
                default:
                    System.out.println("Invalid action.");
                    break;
            }
        }
    }

    // Perform 500 sequential requests and measure response time
    private static void performSequentialRequests(BufferedWriter writer) {
        int numRequests = 500;
        long totalResponseTime = 0;

        try {
            for (int i = 0; i < numRequests; i++) {
                long startTime = System.nanoTime();

                // Search for the file (replace with your search logic)
                searchFile("example.txt");

                long endTime = System.nanoTime();
                long responseTime = (endTime - startTime) / 1_000_000; // Convert to milliseconds
                totalResponseTime += responseTime;

                String logEntry = String.format("Request %d: Response Time (ms): %d%n", i + 1, responseTime);
                System.out.print(logEntry); // Print to console
                writer.write(logEntry); // Write to file
            }

            long averageResponseTime = totalResponseTime / numRequests;
            String avgLogEntry = String.format("Average Response Time for Sequential Requests (ms): %d%n", averageResponseTime);
            System.out.print(avgLogEntry); // Print to console
            writer.write(avgLogEntry); // Write to file
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Perform concurrent requests using multiple clients (threads)
    private static void performConcurrentRequests(BufferedWriter writer, int numClients) throws InterruptedException {
        Thread[] clientThreads = new Thread[numClients];

        for (int i = 0; i < numClients; i++) {
            clientThreads[i] = new Thread(() -> {
                try {
                    long startTime = System.nanoTime();

                    // Search for the file (replace with your search logic)
                    searchFile("example.txt");

                    long endTime = System.nanoTime();
                    long responseTime = (endTime - startTime) / 1_000_000; // Convert to milliseconds
                    String logEntry = String.format("Thread %s: Response Time (ms): %d%n", Thread.currentThread().getName(), responseTime);
                    System.out.print(logEntry); // Print to console
                    synchronized (writer) { // Synchronize access to the writer
                        writer.write(logEntry); // Write to file
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            clientThreads[i].start();
        }

        // Wait for all threads to finish
        for (Thread thread : clientThreads) {
            thread.join();
        }
    }

    // Search for a file on the CentralServer
    private static void searchFile(String fileName) throws IOException {
        try (Socket socket = new Socket("localhost", 5000);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            // Send search command to the server
            out.println("SEARCH " + fileName);
            String response = in.readLine();
            System.out.println("Search result: " + response);
        }
    }
}
