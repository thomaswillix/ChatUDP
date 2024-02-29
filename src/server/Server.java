package server;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;

public class Server {
    private static byte [] buffer = new byte[256];
    private static final int PORT = 9999;

    private static DatagramSocket socket;

    static {
        try{
          socket = new DatagramSocket(PORT);
        } catch (SocketException e){
            throw new RuntimeException(e);
        }
    }
    private static ArrayList<Integer> users = new ArrayList<>();

    private static final InetAddress address;
    static {
        try{
            address = InetAddress.getByName("localhost");
        }catch (UnknownHostException e){
            throw new RuntimeException();
        }
    }

    public static void main(String[] args) {
        System.out.println("Server started on port: " + PORT);

        DatagramPacket packet;
        while (true) {
            packet = new DatagramPacket(buffer, buffer.length);
            try {
                socket.receive(packet); // receive packet
            } catch (IOException e) {
                throw new RuntimeException();
            }

            String message = new String(packet.getData(), 0, packet.getLength());
            System.out.println("Server received: " + message);

            if (message.contains("init;")){
                users.add(packet.getPort());
            }
            //forward
            else{
                int userPort = packet.getPort(); //get port from the packet
                byte[] byteMessage = message.getBytes(); //convert the string to bytes (sort of like an OutPutStream)

                //forward to all other users (except the one who sent the message)
                for(int forward_port: users){
                    DatagramPacket forward = new DatagramPacket(byteMessage, byteMessage.length, address, forward_port);
                    try {
                        socket.send(forward);
                    } catch (IOException e){
                        throw new RuntimeException();
                    }
                }
            }
        }
    }

}
