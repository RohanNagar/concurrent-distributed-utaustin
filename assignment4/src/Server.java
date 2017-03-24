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
    private static final int TIMEOUT = 200;
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
    private static volatile Map<String, Boolean> serverCheck;
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
            serverCheck = new HashMap<>();
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
        }

    }

    /**
     * Provides an implementation for the online store using TCP.
     */
    private static class TCPListener extends Thread {
        private ServerSocket socket;
        private Store store;
        private Timestamp topDog;
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
                    serverCheck.put(Integer.toString(i+1), false);
                    PrintWriter harbringer = new PrintWriter(nextSocket.getOutputStream());
                    Scanner returnmess = new Scanner(nextSocket.getInputStream());
                    harbringer.println("server " + Integer.toString(serverID));
                    harbringer.flush();
                    Thread sThread = new Thread(new serverStorage(nextSocket,Integer.toString(i+1)));
                    sThread.start();
                    System.out.print("Server "+ Integer.toString(i+1) + " is up.\n");

                }catch (IOException e){
                    System.out.print("Server "+ Integer.toString(i+1) + " not up yet.\n");
                }
            }
        }

        @Override
        public void run() {
            while (true) {
                try {
                    Socket receiveSocket = socket.accept();
                    BufferedReader inStream = new BufferedReader(new InputStreamReader(receiveSocket.getInputStream()));
                    /*
                    while(true){
                        if(inStream.ready()){
                            break;
                        }
                    }
                    */
                    String message = inStream.readLine();
                    String[] tokens = message.split(" ");
                    if(tokens[0].equals("ACK")){
                        ack++;
                        System.out.println("Got acknowledgement\n");
                        int sClock = Integer.parseInt(tokens[1]);
                        lc.receiveAction(sClock);
                        if(ack == serverMap.size()){
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
                                    topDog = null;
                                    //do Release;
                                    break;
                                }
                            }
                        }
                    }else if(tokens[0].equals("Client")){ // add to queue of clients that need to be serviced
                        clientQ.add(receiveSocket);
                        System.out.println("Client connected\n");
                        //PrintWriter testme = new PrintWriter(receiveSocket.getOutputStream());
                        //testme.println("hey babe");
                        //testme.flush();
                        processLine.add(new Timestamp(lc.getValue(), serverID));
                        if(!sentRQT){ //there is a client who is already requesting to use critical section
                            if(amIPregnant()) { //send rqt to use CS to other servers
                                sendInvitesToBabyShower();
                                sentRQT = true;
                            }
                        }
                    }else if(tokens[0].equals("RQT")){  // add server timestamp to queue
                        System.out.println("got RQT\n");
                        int chaClock = Integer.parseInt(tokens[1]);
                        int chaID = Integer.parseInt(tokens[2]);
                        Timestamp challenger = new Timestamp(chaClock, chaID);
                        lc.receiveAction(chaClock);
                        processLine.add(challenger);
                        //if(challenger.getLogicalClock() < topDog.getLogicalClock() || topDog == null){
                        Socket returnSock = serverMap.get(Integer.toString(challenger.getPid()));
                        PrintWriter sendAck = new PrintWriter(returnSock.getOutputStream());
                        sendAck.println("ACK " + Integer.toString(lc.getValue()));
                        sendAck.flush();
                        //}

                    }else if(tokens[0].equals("RLS")){  // this message should also change the store. sequence of messages ending with done

                    }else if(tokens[0].equals("CHECK")){  // this message should also change the store. sequence of messages ending with done
                        if(ack == serverMap.size()) {
                            Socket doCS = clientQ.remove();
                            PrintWriter getGoing = new PrintWriter(doCS.getOutputStream());
                            Scanner theReturn = new Scanner(doCS.getInputStream());
                            getGoing.println("go");
                            getGoing.flush();
                            while (theReturn.hasNextLine()) {
                                String reply = theReturn.nextLine();
                                if (reply.equals("cool")) {
                                    sentRQT = false;
                                    ack = 0;
                                    topDog = null;
                                    //do Release;
                                    break;
                                }
                            }
                        }
                    }else if(tokens[0].equals("server")){  // this message should also change the store. sequence of messages ending with done
                        int whichS = Integer.parseInt(tokens[1]) - 1;
                        String address = serversAddress.get(whichS);
                        //InetAddress iaNew = InetAddress.getByName(address);
                        //int popo = serversPort.get(whichS);
                        //Socket nextSocket = new Socket(iaNew, popo);
                        serverMap.put(tokens[1], receiveSocket);
                        serverCheck.put(tokens[1], false);
                        System.out.println("Server " + tokens[1] + " is now up\n");
                        Thread sThread = new Thread(new serverStorage(receiveSocket, tokens[1]));

                        sThread.start();
                    } else { //create a thread that it used to continue communication with client
                        lc.tick();
                        Thread newClient = new Thread(new clientStorage(receiveSocket, message, Integer.toString(clientID), store));
                        clientID++;
                        newClient.start();
                        newClient.interrupt();
                        System.out.println("Initial connection to a new client\n");
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
                // Keep reading while the connection is open
                while (true) {
                    while(inStream.hasNextLine()){
                        Socket momma = new Socket(iAd, port);
                        PrintWriter mommawrite = new PrintWriter(momma.getOutputStream());
                        Scanner mommarec = new Scanner(momma.getInputStream());
                        toSend = "Client";
                        mommawrite.println(toSend);
                        mommawrite.flush();
                        while(mommarec.hasNextLine()){ //not sure if it would block if I just did an if statement
                            String reply = mommarec.nextLine();
                            if(reply.equals("go")){
                                String theCommand = inStream.nextLine();
                                toSend = performTask(store, theCommand);
                            }
                            //change this so that it completes transaction
                            outStream.println(toSend + "\ndone");
                            outStream.flush();
                            mommawrite.println("cool");
                            mommawrite.flush();
                            momma.close();
                            break;
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println("ABORTING..." + e);
            }
        }
    }

    private static class serverStorage implements Runnable {
        private final Socket socket;
        private final String sId;
        private boolean ackCheck;
        private InetAddress iAd;

        serverStorage(Socket socket, String sId) {
            this.socket = socket;
            this.sId = sId;
            ackCheck = false;

        }

        @Override
        public void run() {
            try {
                BufferedReader inStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                //Scanner inStream = new Scanner(socket.getInputStream());
                PrintWriter outStream = new PrintWriter(socket.getOutputStream());
                iAd = InetAddress.getByName(address);
                // Keep reading while the connection is open
                while (true) {

                    if(serverCheck.get(sId)){
                        Utilities.waitOrTimeout(inStream, TIMEOUT);
                    }

                    if(inStream.ready()){
                        Socket momma = new Socket(iAd, port);
                        PrintWriter mommawrite = new PrintWriter(momma.getOutputStream());
                        Scanner mommarec = new Scanner(momma.getInputStream());
                        String toSend = inStream.readLine();
                        String[] tokens = toSend.split(" ");
                        if(tokens[0].equals("ACK")){
                            //ackCheck = true;
                            serverCheck.replace(sId,false);
                        }
                        mommawrite.println(toSend);
                        mommawrite.flush();
                        momma.close();
                    }
                }
            }catch (SocketTimeoutException e) {
                // Timeout, connect to new server and try again
                serverCheck.remove(sId);
                serverMap.remove(sId);
                System.out.println("server " + sId + " disconnected\n");
                System.out.println("serverMap = " + Integer.toString(serverMap.size()) + "\n");
                try {
                    Socket momma = new Socket(iAd, port);
                    PrintWriter mommawrite = new PrintWriter(momma.getOutputStream());
                    Scanner mommarec = new Scanner(momma.getInputStream());
                    String toSend = "CHECK";
                    mommawrite.println(toSend);
                    mommawrite.flush();
                    momma.close();
                } catch (IOException f) {
                    System.out.println("ABORTING..." + f);
                }




            } catch (IOException e) {
                System.out.println("ABORTING..." + e);
            }
        }
    }

    private static class TCPRequest implements Runnable{
        Socket sock;
        String thetime;
        String sId;
        TCPRequest(Socket sock, String thetime, String sId){
            this.sock = sock;
            this.thetime = thetime;
            this.sId = sId;
        }
        public void run(){
            try{
                PrintWriter pout = new PrintWriter(sock.getOutputStream());
                pout.println("RQT "+ thetime + " " + serverID);
                pout.flush();
                serverCheck.replace(sId,true);

            } catch(IOException e){
                System.out.println("Abort.... ");

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
            String theId = pair.getKey();
            lc.sendAction();
            String thetime = Integer.toString(lc.getValue());
            TCPRequest yourMom = new TCPRequest(nextServe, thetime, theId);
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
