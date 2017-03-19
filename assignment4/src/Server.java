/*
 * EIDs of group members
 * ran679, kmg2969
 */

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

public class Server {
    private static AtomicInteger logicClock;
    private static int serverID;
    private static int numServers;
    private static int port;
    private static int ack;
    private static String address;
    private static ArrayList<String> serversAddress;
    private static ArrayList<Integer> serversPort;
    private static LinkedList<message> processLine;
    public static void main (String[] args) {
        if (args.length != 1) {
            System.out.println("ERROR: Please provide a CFG file");
            System.exit(-1);
        }
        Scanner sc;
        try {
            sc = new Scanner(new FileReader(args[0]));
        } catch (FileNotFoundException e) {
            System.out.println("FATAL ERROR:CFG file not found");
            System.exit(-1);
        }
        int line_iter = 0;
        String filename;
        while(sc.hasNextLine()){
            String config = sc.nextLine();
            //setup for first line of the CFG file
            if(line_iter == 0){
                String[] tokens = config.split(" ");
                serverID = Integer.parseInt(tokens[0]);
                numServers = Integer.parseInt(tokens[1]);
                filename = tokens[2];
            //setup for the rest of the CFG file (server info)
            }else{
                String[] tokens = config.split(":");
                int portTrans = Integer.parseInt(tokens[1]);
                serversAddress.add(tokens[0]);
                serversPort.add(portTrans);
                if(line_iter == serverID){
                    address = tokens[0];
                    port = portTrans;
                }
                //InetAddress ia = InetAddress.getByName(tokens[0]);
            }
            line_iter++;
        }

        // Set up store
        Store store = new Store(filename);

        // Setup Logic Clock
        logicClock = new AtomicInteger(0);

        // Setup queue for processes
        processLine = new LinkedList<message>();

        // Start Threads
        TCPListener tcpListener = new TCPListener(port, store);
        tcpListener.start();
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
                    logicClock.getAndIncrement();
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
                    String[] tokens = message.split(" ");
                    if (tokens[0] == "RQT"){
                        //Request message is received

                    }else if(tokens[0] == "REL"){
                        //Release message is received

                    }else{

                        // Perform task for client
                        Thread requester = new Thread(new TCPRequest());
                        while(ack != numServers-1){
                            wait();
                        }
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
                    logicClock.getAndIncrement();
                }
            } catch (IOException e) {
                System.out.println("ABORTING..." + e);
            } catch (InterruptedException e){
                System.out.println("ABORTING..." + e);
            }
        }
    }

    private static class TCPRequest implements Runnable{
        public void run(){
            try{
                for( int i = 0; i < numServers; i++){
                    if((i+1) == serverID ){
                        continue;
                    }
                    InetAddress ia = InetAddress.getByName(serversAddress.get(i));
                    Socket sock = new Socket(ia, serversPort.get(i));
                    PrintWriter pout = new PrintWriter(sock.getOutputStream());
                    Scanner receiver = new Scanner(sock.getInputStream());
                    pout.println("RQT "+ logicClock.toString() + " " + Integer.toString(port));
                    pout.flush();
                    while(receiver.hasNextLine()){
                        String reply = receiver.nextLine();
                        if(reply == "ACK"){
                            ack++;
                            break;
                        }
                    }
                    sock.close();
                }
            } catch(IOException e){
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
    private static class message{
        private int timestamp;
        private int server;

        message(int timestamp, int server){
            this.timestamp = timestamp;
            this.server = server;
        }
        public int gettimestamp(){
            return this.timestamp;
        }
        public int getserver(){
            return this.server;
        }
    }
}
