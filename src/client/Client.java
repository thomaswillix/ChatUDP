package client;

import java.io.IOException;
import java.net.*;
import java.util.Scanner;

public class Client {

    private static final DatagramSocket socket;

    static {
        try {
            socket = new DatagramSocket(); // init to any available port
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }

    static Scanner sc = new Scanner(System.in);

    private static final InetAddress address;

    static {
        try {
            address = InetAddress.getByName("localhost");
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    private static String username;
    private static final int SERVER_PORT = 8000; // send to server
    private static byte[] incoming = new byte[256];


    public static void main(String[] args) throws IOException {
        System.out.println("Dime tu nombre de usuario: ");
        username = sc.nextLine();

        // send initialization message to the server and validate username
        String received;
        do {
            byte[] uuid = ("init; " + username).getBytes();
            DatagramPacket initialize = new DatagramPacket(uuid, uuid.length, address, SERVER_PORT);
            socket.send(initialize);

            DatagramPacket packet = new DatagramPacket(incoming, incoming.length);
            try {
                socket.receive(packet);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            socket.receive(packet);
            received = new String(packet.getData(), 0, packet.getLength()) + "\n";
            System.out.println(received);
        }while (received.equals("Ese usuario no está displonible, introduzca uno nuevo"));
        try {
            DatagramSocket socket = null;

            while (true) {
                String msgSalida = sc.nextLine();
                byte[] sendData = msgSalida.getBytes();

                DatagramPacket salida = new DatagramPacket(sendData, sendData.length, address, SERVER_PORT);
                socket.send(salida);

                byte[] receiveData = new byte[1024];
                DatagramPacket entrada = new DatagramPacket(receiveData, receiveData.length);
                socket.receive(entrada);

                String serverMessage = new String(entrada.getData(), 0, entrada.getLength());
                System.out.println(serverMessage);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }

        }
        System.out.println("Fuera del bucle");

    }
}