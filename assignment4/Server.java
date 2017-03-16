/*
 * EIDs of group members
 * ran679, kmg2969
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class Server {
    public static void main (String[] args) {
        if (args.length != 3) {
            System.out.println("ERROR: Provide 3 arguments");
            System.out.println("\t(1) <tcpPort>: the port number for TCP connection");
            System.out.println("\t(2) <udpPort>: the port number for UDP connection");
            System.out.println("\t(3) <file>: the file of inventory");

            System.exit(-1);
        }

        // Obtain provided information
        int tcpPort = Integer.parseInt(args[0]);
        int udpPort = Integer.parseInt(args[1]);
        String filename = args[2];

        // Set up store
        Store store = new Store(filename);

        // Start Threads
        UDPListener udpListener = new UDPListener(udpPort, store);
        udpListener.start();

        TCPListener tcpListener = new TCPListener(tcpPort, store);
        tcpListener.start();
    }

  /* ----- UDP/TCP Listener Classes ----- */

    /**
     * Provides an implementation for the online store using UDP.
     */
    private static class UDPListener extends Thread {
        private static final int BUFFER_SIZE = 2048;

        private DatagramSocket socket;
        private Store store;
        private byte[] buffer;

        UDPListener(int port, Store store) {
            this.store = store;
            this.buffer = new byte[BUFFER_SIZE];

            try {
                this.socket = new DatagramSocket(port);
            } catch (SocketException e) {
                System.out.println("An error occurred: " + e);

                System.exit(-1);
            }
        }

        @Override
        public void run() {
            DatagramPacket receivePacket;

            while (true) {
                receivePacket = new DatagramPacket(buffer, buffer.length);

                try {
                    socket.receive(receivePacket);

                    // Start a worker thread and keep listening
                    Thread worker = new Thread(new UDPWorker(store, socket, receivePacket));
                    worker.start();
                } catch (IOException e) {
                    System.out.println("ABORTING. An error occurred: " + e);

                    socket.close();
                    return;
                }
            }
        }
    }

    /**
     * Provides an implementation for the online store using TCP.
     */
    private static class TCPListener extends Thread {
        private ServerSocket socket;
        private Store store;

        TCPListener(int port, Store store) {
            this.store = store;

            try {
                this.socket = new ServerSocket(port);
            } catch (IOException e) {
                System.out.println("An error occurred: " + e);

                System.exit(-1);
            }
        }

        @Override
        public void run() {
            while (true) {
                try {
                    Socket receiveSocket = socket.accept();

                    Thread worker = new Thread(new TCPWorker(store, receiveSocket));
                    worker.start();
                } catch (IOException e) {
                    System.out.println("ABORTING. An error occurred: " + e);

                    return;
                }

            }
        }
    }

  /* ----- Workers ----- */

    /**
     * A class to perform the work of parsing a message and
     * updating the data structures inside the online store.
     */
    private static class UDPWorker implements Runnable {
        private final Store store;
        private final DatagramSocket socket;
        private final DatagramPacket packet;

        UDPWorker(Store store, DatagramSocket socket, DatagramPacket packet) {
            this.store = store;
            this.socket = socket;
            this.packet = packet;
        }

        @Override
        public void run() {
            // Retrieve message
            String message;

            try {
                message = new String(packet.getData(), 0, packet.getLength(), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                System.out.println("ABORTING... An error occurred: " + e);
                return;
            }

            // Perform task of message
            String reply = performTask(store, message);
            if (reply == null) {
                System.out.println("ERROR: UNKNOWN MESSAGE - " + message);
                return;
            }

            // Send reply
            try {
                byte[] returnBytes = reply.getBytes("UTF-8");
                DatagramPacket returnPacket = new DatagramPacket(
                        returnBytes,
                        returnBytes.length,
                        packet.getAddress(),
                        packet.getPort());
                socket.send(returnPacket);
            } catch (IOException e) {
                System.out.println("ERROR: UNABLE TO SEND REPLY: " + e);
            }
        }
    }

    private static class TCPWorker implements Runnable {
        private final Store store;
        private final Socket socket;

        TCPWorker(Store store, Socket socket) {
            this.store = store;
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                BufferedReader inStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter outStream = new PrintWriter(socket.getOutputStream());

                // Keep reading while the connection is open
                while (true) {
                    // Retrieve message
                    String message = inStream.readLine();
                    if (message == null)
                        break;

                    // Perform task
                    String reply = performTask(store, message);
                    if (reply == null) {
                        System.out.println("ERROR: UNKNOWN MESSAGE - " + message);
                        continue;
                    }
                    reply += "\ndone";
                    // Send reply
                    outStream.println(reply);
                    outStream.flush();
                }
            } catch (IOException e) {
                System.out.println("ABORTING..." + e);
            }
        }
    }

    private static String performTask(Store store, String task) {
        String[] tokens = task.split(" ");
        switch (tokens[0].trim()) {
            case "purchase":
                return store.purchase(
                        tokens[1].trim(),
                        tokens[2].trim(),
                        Integer.parseInt(tokens[3].trim()));

            case "cancel":
                return store.cancel(Integer.parseInt(tokens[1].trim()));

            case "search":
                return store.getOrdersForUser(tokens[1].trim());

            case "list":
                return store.readInventory();

            default:
                return null;
        }

    }
}
