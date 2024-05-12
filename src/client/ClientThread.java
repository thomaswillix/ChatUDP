package client;


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class ClientThread extends Thread{
    private DatagramSocket socket;
    private byte[] buffer = new byte[256];

    public ClientThread(DatagramSocket socket){
        this.socket = socket;
    }

    @Override
    public void run() {
        System.out.println("starting thread");
        String message;
        do {
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            try {
                socket.receive(packet);
            } catch (IOException e){
                throw new RuntimeException();
            }
            message = new String(packet.getData(), 0, packet.getLength()) + "\n";
            System.out.print(message);
        } while (!message.equals("exit"));
    }
}