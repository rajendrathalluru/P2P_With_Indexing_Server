import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class CentralIndexingServer {
    // Thread-safe maps to store file and peer information
    private static final Map<String, List<String>> fileIndex = new ConcurrentHashMap<>();  // File -> List of Peer IDs
    private static final Map<String, String> peerAddresses = new ConcurrentHashMap<>();    // Peer ID -> IP address
    private static final int SERVER_PORT = 5000;  // Port for the central indexing server

    
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(SERVER_PORT);  // Listen on port 5000
        System.out.println("Central Indexing Server started on port " + SERVER_PORT + "...");

        // ExecutorService to handle multiple client connections concurrently
        ExecutorService executor = Executors.newFixedThreadPool(10); // Max 10 concurrent clients

        // Graceful shutdown hook to close the ExecutorService when the server is stopped
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down Central Indexing Server...");
            executor.shutdown();
            try {
                if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
            }
        }));

        try {
            while (true) {
                Socket clientSocket = serverSocket.accept();  // Accept new client connections
                clientSocket.setSoTimeout(30000);  // Set a 30-second timeout for client operations
                System.out.println("Accepted connection from client: " + clientSocket.getInetAddress());

                // Submit the client request to be handled by the thread pool
                executor.submit(new ClientHandler(clientSocket));
            }
        } catch (IOException e) {
            System.out.println("Error while accepting client connection: " + e.getMessage());
        } finally {
            serverSocket.close();  // Close server socket if server is stopped
        }
    }

    // Runnable class to handle requests from connected peers
    static class ClientHandler implements Runnable {
        private final Socket clientSocket;

        // Constructor to initialize client socket
        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

                // Read the command sent by the peer
                String request = in.readLine();
                if (request != null) {
                    System.out.println("Received request: " + request);

                    // Parse the request and determine the command (REGISTER, SEARCH, or DEREGISTER)
                    String[] tokens = request.split(" ");
                    String command = tokens[0];

                    // Handle REGISTER command
                    if (command.equals("REGISTER")) {
                        String peerId = tokens[1];
                        String peerIp = tokens[2];
                        List<String> files = Arrays.asList(tokens).subList(3, tokens.length);
                        register(peerId, peerIp, files);
                        out.println("Files have been registered successfully.");

                    // Handle SEARCH command
                    } else if (command.equals("SEARCH")) {
                        String fileName = tokens[1];
                        List<String> peers = search(fileName);
                        out.println(peers.isEmpty() ? "File not found." : String.join(", ", peers));

                    // Handle DEREGISTER command
                    } else if (command.equals("DEREGISTER")) {
                        String peerId = tokens[1];
                        String fileName = tokens[2];
                        deregister(peerId, fileName);
                        out.println("File has been deregistered.");
                    }
                } else {
                    System.out.println("Received an empty request. Ignoring...");
                }

            } catch (SocketTimeoutException e) {
                System.out.println("Connection timed out for client: " + clientSocket.getInetAddress());
            } catch (IOException e) {
                System.out.println("I/O error while communicating with client: " + e.getMessage());
            } finally {
                try {
                    clientSocket.close();  // Ensure socket is closed after handling request
                } catch (IOException e) {
                    System.out.println("Error while closing client socket: " + e.getMessage());
                }
            }
        }

        // Register a peer's files and map them to the peer ID and IP address
        private synchronized void register(String peerId, String peerIp, List<String> files) {
            peerAddresses.put(peerId, peerIp);  // Store peer's IP address
            for (String file : files) {
                fileIndex.computeIfAbsent(file, k -> new ArrayList<>()).add(peerId);  // Add peer to file index
            }
            System.out.println("Registered files for peer " + peerId + ": " + files);
        }

        // Search for peers that have a particular file
        private List<String> search(String fileName) {
            List<String> peerIds = fileIndex.getOrDefault(fileName, Collections.emptyList());
            List<String> peerIps = new ArrayList<>();
            for (String peerId : peerIds) {
                peerIps.add(peerId + "@" + peerAddresses.get(peerId));  // Return peer IDs and their corresponding IPs
            }
            System.out.println("Search for file '" + fileName + "' found peers: " + peerIps);
            return peerIps;
        }

        // Deregister a peer's file when it is deleted
        private synchronized void deregister(String peerId, String fileName) {
            List<String> peers = fileIndex.get(fileName);
            if (peers != null) {
                peers.remove(peerId);  // Remove peer from file's peer list
                if (peers.isEmpty()) {
                    fileIndex.remove(fileName);  // Remove file entry if no peers have the file
                }
            }
            System.out.println("Deregistered file '" + fileName + "' from peer " + peerId);
        }
    }
}
