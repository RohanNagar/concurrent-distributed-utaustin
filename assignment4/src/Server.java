/*
 * EIDs of group members
 * ran679, kmg2969
 */

import java.io.*;
import java.net.*;
import java.sql.Time;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Comparator;

import static java.lang.Thread.sleep;


public class Server {

    private static LamportClock lc;
    private static boolean sentRQT;
    private static int serverID;
    private static int clientID;
    private static int numServers;
    private static int port;
    private static int ack;
    private static String address;
    private static ArrayList<String> serversAddress;
    private static ArrayList<Integer> serversPort;
    private static Queue<Timestamp> processLine;
    private static Queue<Socket> clientQ;
    private static Map<String, Socket> serverMap;
    public static void main (String[] args) {

        if (args.length != 1) {
            System.out.println("ERROR: Please provide a CFG file");
            System.exit(-1);
        }
        Scanner sc;
        Store store;
        try {
            String thefile = args[0];
            sc = new Scanner(new FileReader(thefile));
            int line_iter = 1;
            String filename;
            String config = sc.nextLine();
            String[] tokens = config.split(" ");
            serverID = Integer.parseInt(tokens[0]);
            numServers = Integer.parseInt(tokens[1]);
            filename = tokens[2];
            serversAddress = new ArrayList<String>();
            serversPort = new ArrayList<Integer>();
            while(sc.hasNextLine()){
                config = sc.nextLine();
                tokens = config.split(":");
                int portTrans = Integer.parseInt(tokens[1]);
                String addTrans = tokens[0];
                serversAddress.add(addTrans);
                serversPort.add(portTrans);
                if(line_iter == serverID){
                    address = addTrans;
                    port = portTrans;
                }
                line_iter++;
            }
            sc.close();
            // Set up store
            store = new Store(filename);

            // Setup Logic Clock
            lc = new LamportClock();
            Comparator<Timestamp> toCompare = new StringLengthComparator();
            // Setup queue for processes
            processLine = new PriorityQueue<Timestamp>(11, toCompare);


            //Setup queue for clients that will be handled
            clientQ = new LinkedList<Socket>();
            //A flag to see if the server is currently attempting to get into the critical section
            sentRQT = false;
            //set up socket connections to all of the other servers
            serverMap = new HashMap<>();
            /*
            for(int i = 0; i < serversAddress.size(); i++){
                String address = serversAddress.get(i);
                InetAddress iaNew = InetAddress.getByName(address);
                int popo = serversPort.get(i);
                Socket nextSocket = new Socket(iaNew, popo);
                serverMap.put(Integer.toString(i), nextSocket);
            }
            */
            // Start Threads
            TCPListener tcpListener = new TCPListener(port, store);
            tcpListener.start();
        } catch (FileNotFoundException e) {
            System.out.println("FATAL ERROR:CFG file not found");
            System.exit(-1);
        }catch(IOException e){
            System.out.print("Failed in intial server socket setup... "+ e);
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
            for(int i = 0; i < serversAddress.size(); i++){
                if(i+1 == serverID){
                    continue;
                }
                try {
                    String address = serversAddress.get(i);
                    InetAddress iaNew = InetAddress.getByName(address);
                    int popo = serversPort.get(i);
                    Socket nextSocket = new Socket(iaNew, popo);
                    serverMap.put(Integer.toString(i+1), nextSocket);
                    PrintWriter harbringer = new PrintWriter(nextSocket.getOutputStream());
                    harbringer.println("server " + Integer.toString(i+1));
                }catch (IOException e){
                    System.out.print("Server "+ Integer.toString(i) + " not up yet.");
                }
            }
        }

        @Override
        public void run() {
            while (true) {
                try {
                    Socket receiveSocket = socket.accept();
                    BufferedReader inStream = new BufferedReader(new InputStreamReader(receiveSocket.getInputStream()));
                    String message = inStream.readLine();
                    String[] tokens = message.split(" ");
                    if(tokens[0] == "ACK"){
                        ack++;
                        int sClock = Integer.parseInt(tokens[0]);
                        lc.receiveAction(sClock);
                        if(ack == serverMap.size() - 1){
                            Socket doCS = clientQ.remove();
                            PrintWriter getGoing = new PrintWriter(doCS.getOutputStream());
                            Scanner theReturn = new Scanner(doCS.getInputStream());
                            getGoing.println("go");
                            getGoing.flush();
                            while(theReturn.hasNextLine()){
                                String reply = theReturn.nextLine();
                                if (reply.equals("cool")) {
                                    sentRQT = false;
                                    ack = 0;
                                    //do Release;
                                    break;
                                }
                            }
                        }
                    }else if(tokens[0] == "Client"){ // add to queue of clients that need to be serviced
                        clientQ.add(receiveSocket);
                        processLine.add(new Timestamp(lc.getValue(), serverID));
                        if(!sentRQT){ //there is a client who is already requesting to use critical section
                            if(amIPregnant()) { //send rqt to use CS to other servers
                                sendInvitesToBabyShower();
                                sentRQT = true;
                            }
                        }
                    }else if(tokens[0] == "RQT"){  // add server timestamp to queue
                        int chaClock = Integer.parseInt(tokens[1]);
                        int chaID = Integer.parseInt(tokens[2]);
                        Timestamp challenger = new Timestamp(chaClock, chaID);
                        lc.receiveAction(chaClock);
                        processLine.add(challenger);

                    }else if(tokens[0] == "RLS"){  // this message should also change the store. sequence of messages ending with done

                    }else if(tokens[0] == "Server"){  // this message should also change the store. sequence of messages ending with done
                        int whichS = Integer.parseInt(tokens[1]);
                        String address = serversAddress.get(whichS);
                        InetAddress iaNew = InetAddress.getByName(address);
                        int popo = serversPort.get(whichS);
                        Socket nextSocket = new Socket(iaNew, popo);
                        serverMap.put(Integer.toString(whichS), nextSocket);
                    } else { //create a thread that it used to continue communication with client
                        lc.tick();
                        Thread newClient = new Thread(new clientStorage(receiveSocket, message, Integer.toString(clientID), store));
                        clientID++;
                        newClient.start();
                    }

                } catch (IOException e) {
                    System.out.println("ABORTING. An error occurred: " + e);

                    return;
                }

            }
        }
    }

  /* ----- Workers ----- */

    private static class clientStorage implements Runnable {
        private final Socket socket;
        private final String first;
        private final String clId;
        private final Store store;

        clientStorage(Socket socket, String first, String clId, Store store) {
            this.socket = socket;
            this.first = first;
            this.clId = clId;
            this.store = store;
        }

        @Override
        public void run() {
            try {
                //BufferedReader inStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                Scanner inStream = new Scanner(socket.getInputStream());
                PrintWriter outStream = new PrintWriter(socket.getOutputStream());
                String toSend = "Client";
                InetAddress iAd = InetAddress.getByName(address);
                Socket momma = new Socket(iAd, port);
                PrintWriter mommawrite = new PrintWriter(momma.getOutputStream());
                Scanner mommarec = new Scanner(momma.getInputStream());
                mommawrite.println(toSend);
                mommawrite.flush();
                while(mommarec.hasNextLine()){ //gets message back from momma
                    String reply = mommarec.nextLine();
                    if(reply == "go"){
                        toSend = performTask(store, first);
                    }
                    //lets change this so that it completes transaction with store
                    outStream.println(toSend);
                    outStream.flush();
                    break;
                }
                // Keep reading while the connection is open
                while (true) {
                    while(inStream.hasNextLine()){
                        toSend = "Client";
                        mommawrite.println(toSend);
                        mommawrite.flush();
                        while(mommarec.hasNextLine()){ //not sure if it would block if I just did an if statement
                            String reply = mommarec.nextLine();
                            if(reply == "go"){
                                toSend = performTask(store, first);
                            }
                            //change this so that it completes transaction
                            outStream.println(toSend);
                            outStream.flush();
                            break;
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println("ABORTING..." + e);
            }
        }
    }

    private static class TCPRequest implements Runnable{
        Socket sock;
        String thetime;
        TCPRequest(Socket sock, String thetime){
            this.sock = sock;
            this.thetime = thetime;
        }
        public void run(){
            try{
                PrintWriter pout = new PrintWriter(sock.getOutputStream());
                Scanner receiver = new Scanner(sock.getInputStream());
                pout.println("RQT "+ thetime + " " + serverID);
                pout.flush();
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
    //test to see if the front of the queue is from this server
    private static boolean amIPregnant(){
        Timestamp thebaby = processLine.peek();
        if (thebaby.getPid() == serverID){
            //she got you for 18 years
            return true;
        }else{
            // aint your baby
            return false;
        }
    }

    private static void sendInvitesToBabyShower(){
        Iterator it = serverMap.entrySet().iterator();

        while(it.hasNext()){
            Map.Entry<String,Socket> pair = (Map.Entry)it.next();
            Socket nextServe = pair.getValue();
            lc.sendAction();
            String thetime = Integer.toString(lc.getValue());
            TCPRequest yourMom = new TCPRequest(nextServe, thetime);
            yourMom.run();
        }
    }
    private static class StringLengthComparator implements Comparator<Timestamp> {

        @Override
        public int compare(Timestamp x, Timestamp y) {
            int xstamp= x.getLogicalClock();
            int ystamp= y.getLogicalClock();

            if (xstamp > ystamp)
                return 1;
            if (xstamp <  ystamp)
                return -1;
            /*
            if (a.pid > b.pid) return 1;
            if (a.pid < b.pid)
                return -1;
            */
            return 0;
        }
    }
}
