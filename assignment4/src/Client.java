/*
 * EIDs of group members
 * ran679, kmg2969
 */

import java.util.Scanner;
import java.net.*;
import java.io.*;

public class Client {
    private static final int BUFFER_SIZE = 2048;

    public static void main(String[] args) {
        Socket tcpSock;
        InetAddress ia;

        if (args.length != 3) {
            System.out.println("ERROR: Provide 3 arguments");
            System.out.println("\t(1) <hostAddress>: the address of the server");
            System.out.println("\t(2) <tcpPort>: the port number for TCP connection");
            System.exit(-1);
        }

        String hostAddress = args[0];
        int tcpPort = Integer.parseInt(args[1]);

        try {
            ia = InetAddress.getByName(hostAddress);
            tcpSock = new Socket(ia, tcpPort);

            PrintWriter pout = new PrintWriter(tcpSock.getOutputStream());
            Scanner receiver = new Scanner(tcpSock.getInputStream());

            Scanner sc = new Scanner(System.in);

            while (sc.hasNextLine()) {
                String cmd = sc.nextLine();
                String[] tokens = cmd.split(" ");

                switch (tokens[0]) {
                    case "purchase":
                    case "cancel":
                    case "search":
                    case "list":
                        sendAndReceiveTCP(pout, receiver, cmd);
                        break;

                    default:
                        System.out.println("ERROR: No such command");
                }
            }

            tcpSock.close();
        } catch (IOException e) {
            System.err.println("ERROR: Unknown error occurred: " + e);
        }
    }

    private static void sendAndReceiveTCP(PrintWriter writer, Scanner receiver, String message) {
        writer.println(message);
        writer.flush();

        while (receiver.hasNextLine()) {
            String reply = receiver.nextLine();
            if (reply.equals("done")) {
                break;
            }
            System.out.println(reply);
        }
    }
}