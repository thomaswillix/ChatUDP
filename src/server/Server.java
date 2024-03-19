package server;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;

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

    private static ArrayList<Integer> userPorts = new ArrayList<>();
    private static ArrayList<String> users = new ArrayList<>();

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


            if (message.contains("init; ")) {
                userPorts.add(packet.getPort());
                validateUser(message, packet);
            }
            // forward
            else {
                int userPort = packet.getPort();  // get port from the packet
                byte[] byteMessage = message.getBytes(); // convert the string to bytes

                // forward to all other users (except the one who sent the message)
                for (int forward_port : userPorts) {
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


        }
    }

    private static void validateUser(String message, DatagramPacket packet) {
        String[] splitMessage = message.split(" ");
        String error = "Ese usuario no está displonible, introduzca uno nuevo";
        String correct = "Username set to: " + splitMessage[1];
        int userPort = packet.getPort();  // get port from the packet
        for (int forward_port : userPorts) {
            if (forward_port == userPort) {
                if (users.contains(splitMessage[1])){
                    DatagramPacket forward = new DatagramPacket(error.getBytes(), error.getBytes().length, address, forward_port);
                    try {
                        socket.send(forward);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    users.add(splitMessage[1]);
                    DatagramPacket forward = new DatagramPacket(correct.getBytes(), correct.getBytes().length, address, forward_port);
                    try {
                        socket.send(forward);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }
}