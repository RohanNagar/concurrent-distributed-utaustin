/*
 * EIDs of group members
 * ran679, kmg2969
 */

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.net.*;
import java.io.*;

public class Client {
  public static final int TIMEOUT = 100;

  private static final List<ServerInformation> servers = new ArrayList<>();

  private static int currentServerNumber = 0;
  private static Socket currentConnection;

  public static void main(String[] args) {
    // Obtain number of servers to have available
    Scanner sc = new Scanner(System.in);
    int numServers = Integer.parseInt(sc.nextLine());

    // Populate server information
    for (int i = 0; i < numServers; i++) {
      String server = sc.nextLine();
      String[] info = server.split(":");

      try {
        InetAddress address = InetAddress.getByName(info[0]);
        int port = Integer.parseInt(info[1]);

        servers.add(new ServerInformation(address, port));
      } catch (UnknownHostException e) {
        System.out.println("An error occurred adding server: " + server);
        System.out.println("Ignoring server and continuing...");
      }
    }

    try {
      connectToNewServer(false);

      PrintWriter pout = new PrintWriter(currentConnection.getOutputStream());
      Scanner receiver = new Scanner(currentConnection.getInputStream());

      while (sc.hasNextLine()) {
        String cmd = sc.nextLine();
        String[] tokens = cmd.split(" ");

        switch (tokens[0]) {
          case "purchase":
          case "cancel":
          case "search":
          case "list":
            performCommand(pout, receiver, cmd);
            break;

          default:
            System.out.println("ERROR: No such command");
        }
      }

      // This client is done, close our connection
      currentConnection.close();
    } catch (IOException e) {
      System.err.println("ERROR: Unknown error occurred: " + e);
    }
  }

  /**
   * Performs a command by sending it to the server,
   * waiting for a response, and printing the response.
   *
   * @param writer The output stream to send messages on.
   * @param receiver The input stream to receive replies on.
   * @param message The message to send to the server.
   */
  private static void performCommand(PrintWriter writer, Scanner receiver, String message) {
    writer.println(message);
    writer.flush();

    // TODO: handle timeout and connect to new servers

    while (receiver.hasNextLine()) {
      String reply = receiver.nextLine();
      if (reply.equals("done")) {
        break;
      }
      System.out.println(reply);
    }
  }

  /**
   * Connects to a new server from the possible servers list, attempting to connect
   * in the same order as the list.
   *
   * @param oldServerDead Whether or not the previous server has crashed. If true, the previous
   *                      server will be removed from the list.
   */
  private static void connectToNewServer(boolean oldServerDead) {
    currentConnection = null;

    if (oldServerDead) {
      // If the previous server is down, we won't connect again, so remove it
      servers.remove(currentServerNumber);
    }

    // Attempt to connect starting from the last connected server and continuing in order
    for (int i = 0; i < servers.size(); i++) {
      ServerInformation information = servers.get(i);

      try {
        currentConnection = new Socket(information.getAddress(), information.getPort());

        // Return if we successfully connected
        currentServerNumber = i;
        return;
      } catch (IOException e) {
        System.out.println("Server " + information + " is unavailable.");
      }
    }

    // If here, we never successfully connected
    if (currentConnection == null) {
      System.out.println("Unable to connect to any servers.");
      System.out.println("Ending execution...");

      System.exit(-1);
    }
  }

  /**
   * A class to hold information necessary to connect to a server.
   */
  private static class ServerInformation {
    private final InetAddress address;
    private final int port;

    ServerInformation(InetAddress address, int port) {
      this.address = address;
      this.port = port;
    }

    InetAddress getAddress() {
      return address;
    }

    int getPort() {
      return port;
    }

    public String toString() {
      return address.getHostName() + ":" + port;
    }
  }
}