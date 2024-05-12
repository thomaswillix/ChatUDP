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
    private static byte[] incoming = new byte[1024];


    public static void main(String[] args) throws IOException {
        System.out.println("Dime tu nombre de usuario: ");

        // send initialization message to the server and validate username
        String received;
        do {
            username = sc.nextLine();
            byte[] uuid = ("init; " + username).getBytes();
            DatagramPacket initialize = new DatagramPacket(uuid, uuid.length, address, SERVER_PORT);
            socket.send(initialize);

            DatagramPacket packet = new DatagramPacket(incoming, incoming.length);
            socket.receive(packet);
            received = new String(packet.getData(), 0, packet.getLength()) + "\n";
            System.out.print(received);

        }while (received.equals("Ese usuario no está displonible, introduzca uno nuevo\n"));

        String msgSalida;
        ClientThread thread  = new ClientThread(socket);
        thread.start();

        do {
            msgSalida = sc.nextLine();
            byte[] sendData = msgSalida.getBytes();
            DatagramPacket salida = new DatagramPacket(sendData, sendData.length, address, SERVER_PORT);
            socket.send(salida);
        } while (!msgSalida.equals("/exit"));
        System.err.println("Fin de ejecución");
        socket.close();
    }
}