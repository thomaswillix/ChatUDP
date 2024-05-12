package server;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Server {

    private static byte[] incoming = new byte[256];
    private static final int PORT = 8000;

    private static DatagramSocket socket;

    static {
        try {
            socket = new DatagramSocket(PORT);
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }

    private static HashMap<Integer, String> users = new HashMap<>();

    private static final InetAddress address;

    static {
        try {
            address = InetAddress.getByName("localhost");
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }


    public static void main(String[] args) {

        System.out.println("Server started on port " + PORT);

        while (true) {
            DatagramPacket packet = new DatagramPacket(incoming, incoming.length); // prepare packet
            try {
                socket.receive(packet); // receive packet
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            String message = new String(packet.getData(), 0, packet.getLength()); // create a string
            System.out.println("Server received: " + message);

            int userPort = packet.getPort();  // get port from the packet

            if (message.contains("init; ")) {
                validateUser(message, packet);
            } else if(message.equals("/exit")){
                String threadFlag = "exit";
                byte[] threadFlagBytes = threadFlag.getBytes(); // convert the string to bytes

                // This will terminate the ClientThread
                DatagramPacket forward = new DatagramPacket(threadFlagBytes, threadFlagBytes.length, address, userPort);
                try {
                    socket.send(forward);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                //Broadcast to the rest of the users
                String userExited = users.get(packet.getPort()) +  " HAS LEFT THE CHAT"; // ANNOUNCEMENT
                broadcastMessage(userExited, userPort);
                users.remove(packet.getPort());
            }
            // forward
            else {
                String otherMessage = users.get(userPort) + ": " + message; //This will be shown in the screen whenever someone else types
                broadcastMessage(otherMessage, userPort);
            }
        }
    }

    private static void broadcastMessage(String message, int userPort){
        byte[] byteMessage = message.getBytes(); // convert the string to bytes

        // forward to all other users (except the one who sent the message)
        for (int forward_port : users.keySet()) {
            if (forward_port != userPort) {
                DatagramPacket forward = new DatagramPacket(byteMessage, byteMessage.length, address, forward_port);
                try {
                    socket.send(forward);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private static void validateUser(String message, DatagramPacket packet) {
        String[] splitMessage = message.split(" ");
        String error = "Ese usuario no está displonible, introduzca uno nuevo";
        String correct = "Username set to: " + splitMessage[1];

        if (users.containsValue(splitMessage[1])){
            DatagramPacket forward = new DatagramPacket(error.getBytes(), error.getBytes().length, address, packet.getPort());
            try {
                socket.send(forward);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            users.put(packet.getPort(), splitMessage[1]);
            DatagramPacket forward = new DatagramPacket(correct.getBytes(), correct.getBytes().length, address, packet.getPort());
            try {
                socket.send(forward);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}