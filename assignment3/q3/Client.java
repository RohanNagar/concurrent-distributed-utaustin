import java.util.Scanner;
import java.net.*;
import java.io.*;
public class Client {
  public static void main (String[] args) {
    String hostAddress;
    int tcpPort;
    int udpPort;
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

    hostAddress = args[0];
    tcpPort = Integer.parseInt(args[1]);
    udpPort = Integer.parseInt(args[2]);
    try{
    	ia = InetAddress.getByName(hostAddress);
    	udpSock = new DatagramSocket();
    	tcpSock = new Socket(ia,tcpPort);
        Scanner sc = new Scanner(System.in);
        System.out.println("you can print");
        while(sc.hasNextLine()) {
          String cmd = sc.nextLine();
          String[] tokens = cmd.split(" ");
          // TODO: set the mode of communication for sending commands to the server 
          // and display the name of the protocol that will be used in future
          if (tokens[0].equals("setmode")) {
        	  if(tokens[1].equals("T")){
        		 whichSock = "T";
        		 System.out.println("Current Communication Protocol: TCP\n");
        	  }else if(tokens[1].equals("U")){
        		 whichSock = "U";
        		 System.out.println("Current Communication Protocol: UDP\n");
        	  }else{
        		 System.out.println("Error: Please input either 'T' or 'U'");
        	  }
          }
          else if (tokens[0].equals("purchase")) {
            // TODO: send appropriate command to the server and display the
            // appropriate responses form the server
        	String output = tokens[0] + " " + tokens[1]+ " " + tokens[2]+ " " + tokens[3];
        	byte[] toServe = output.getBytes();
        	if(whichSock.equals("U")){
        		DatagramPacket sendPacket = new DatagramPacket(toServe, toServe.length, ia, udpPort);
        		udpSock.send(sendPacket);
        		byte[] rbuff = new byte[200];
        		DatagramPacket recPacket = new DatagramPacket(rbuff, rbuff.length);
        		udpSock.receive(recPacket);
        		String reply = new String(recPacket.getData(), 0, recPacket.getLength());
        		System.out.println(reply);
        	}else{
        		PrintWriter pout = new PrintWriter(tcpSock.getOutputStream());
        		Scanner receiver = new Scanner(tcpSock.getInputStream());
        		pout.println(output);
        		pout.flush();
      		    while(receiver.hasNextLine()){
    			    String reply = receiver.nextLine();
    			    if(reply.equals("done")){
    				    break;
    			    }
    			    System.out.println(reply);
    		    }
        	}
          } else if (tokens[0].equals("cancel")) {
        	  String output = tokens[0] + " " + tokens[1];
        	  byte[] toServe = output.getBytes();
          	  if(whichSock.equals("U")){
        		  DatagramPacket sendPacket = new DatagramPacket(toServe, toServe.length, ia, udpPort);
        		  udpSock.send(sendPacket);
        		  byte[] rbuff = new byte[200];
        		  DatagramPacket recPacket = new DatagramPacket(rbuff, rbuff.length);
        		  udpSock.receive(recPacket);
        		  String reply = new String(recPacket.getData(), 0, recPacket.getLength());
        		  System.out.println(reply);
        	  }else{
        		  PrintWriter pout = new PrintWriter(tcpSock.getOutputStream());
        		  Scanner receiver = new Scanner(tcpSock.getInputStream());
        		  pout.println(output);
        		  pout.flush();
        		  while(receiver.hasNextLine()){
        			  String reply = receiver.nextLine();
        			  if(reply.equals("done")){
        				  break;
        			  }
        			  System.out.println(reply);
        		  }
        	  }
            // TODO: send appropriate command to the server and display the
            // appropriate responses form the server
          } else if (tokens[0].equals("search")) {
        	  String output = tokens[0] + " " + tokens[1];
        	  byte[] toServe = output.getBytes();
          	  if(whichSock.equals("U")){
        		  DatagramPacket sendPacket = new DatagramPacket(toServe, toServe.length, ia, udpPort);
        		  udpSock.send(sendPacket);
        		  byte[] rbuff = new byte[200];
        		  DatagramPacket recPacket = new DatagramPacket(rbuff, rbuff.length);
        		  udpSock.receive(recPacket);
        		  String reply = new String(recPacket.getData(), 0, recPacket.getLength());
        		  System.out.println(reply);
        	  }else{
        		  PrintWriter pout = new PrintWriter(tcpSock.getOutputStream());
        		  Scanner receiver = new Scanner(tcpSock.getInputStream());
        		  pout.println(output);
        		  pout.flush();
        		  while(receiver.hasNextLine()){
        			  String reply = receiver.nextLine();
        			  if(reply.equals("done")){
        				  break;
        			  }
        			  System.out.println(reply);
        		  }
        	  }
            // TODO: send appropriate command to the server and display the
            // appropriate responses form the server
          } else if (tokens[0].equals("list")) {
        	  String output = tokens[0];
        	  byte[] toServe = output.getBytes();
          	  if(whichSock.equals("U")){
        		  DatagramPacket sendPacket = new DatagramPacket(toServe, toServe.length, ia, udpPort);
        		  udpSock.send(sendPacket);
        		  byte[] rbuff = new byte[200];
        		  DatagramPacket recPacket = new DatagramPacket(rbuff, rbuff.length);
        		  udpSock.receive(recPacket);
        		  String reply = new String(recPacket.getData(), 0, recPacket.getLength());
        		  System.out.println(reply);
        	  }else{
        		  PrintWriter pout = new PrintWriter(tcpSock.getOutputStream());
        		  Scanner receiver = new Scanner(tcpSock.getInputStream());
        		  pout.println(output);
        		  pout.flush();
        		  while(receiver.hasNextLine()){
        			  String reply = receiver.nextLine();
        			  if(reply.equals("done")){
        				  break;
        			  }
        			  System.out.println(reply);
        		  }
        	  }
            // TODO: send appropriate command to the server and display the
            // appropriate responses form the server
          } else {
            System.out.println("ERROR: No such command");
          }
        }
        udpSock.close();
        tcpSock.close();
    }catch(UnknownHostException e){
    	System.err.println(e);
    }catch(SocketException e){
    	System.err.println(e);
    }catch(IOException e){
    	System.err.println(e);
    }
  }
}