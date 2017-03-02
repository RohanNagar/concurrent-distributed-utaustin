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
    DatagramSocket udpSock;
    Socket tcpSock;
    InetAddress ia;

    String whichSock = "T";
    if (args.length != 3) {
      System.out.println("ERROR: Provide 3 arguments");
      System.out.println("\t(1) <hostAddress>: the address of the server");
      System.out.println("\t(2) <tcpPort>: the port number for TCP connection");
      System.out.println("\t(3) <udpPort>: the port number for UDP connection");
      System.exit(-1);
    }

    String hostAddress = args[0];
    int tcpPort = Integer.parseInt(args[1]);
    int udpPort = Integer.parseInt(args[2]);

    try {
      ia = InetAddress.getByName(hostAddress);
      udpSock = new DatagramSocket();
      tcpSock = new Socket(ia, tcpPort);

      PrintWriter pout = new PrintWriter(tcpSock.getOutputStream());
      Scanner receiver = new Scanner(tcpSock.getInputStream());

      Scanner sc = new Scanner(System.in);

      while (sc.hasNextLine()) {
        String cmd = sc.nextLine();
        String[] tokens = cmd.split(" ");

        switch (tokens[0]) {
          case "setmode":
            if (tokens[1].equals("T")) {
              whichSock = "T";
              System.out.println("TCP");
            } else if (tokens[1].equals("U")) {
              whichSock = "U";
              System.out.println("UDP");
            } else {
              System.out.println("Error: Please input either 'T' or 'U'");
            }

            break;

          case "purchase":
          case "cancel":
          case "search":
          case "list":
            if (whichSock.equals("U")) {
              sendAndReceiveUDP(ia, udpSock, udpPort, cmd);
            } else {
              sendAndReceiveTCP(pout, receiver, cmd);
            }

            break;

          default:
            System.out.println("ERROR: No such command");
        }
      }

      udpSock.close();
      tcpSock.close();
    } catch (IOException e) {
      System.err.println("ERROR: Unknown error occurred: " + e);
    }
  }

  private static void sendAndReceiveUDP(InetAddress ia, DatagramSocket socket, int port, String message) {
    byte[] toServe = message.getBytes();

    try {
      DatagramPacket sendPacket = new DatagramPacket(toServe, toServe.length, ia, port);
      socket.send(sendPacket);

      byte[] rbuff = new byte[BUFFER_SIZE];
      DatagramPacket recPacket = new DatagramPacket(rbuff, rbuff.length);
      socket.receive(recPacket);

      String reply = new String(recPacket.getData(), 0, recPacket.getLength());
      System.out.println(reply);
    } catch (IOException e) {
      System.out.println("ERROR: Unknown error occurred: " + e);
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