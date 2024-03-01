package client;


import javafx.scene.control.TextArea;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class ClientThread extends Thread{
    private DatagramSocket socket;
    private byte[] buffer = new byte[256];
    private TextArea textArea;

    public ClientThread(DatagramSocket socket, TextArea textArea){
        this.socket = socket;
        this.textArea = textArea;
    }

    @Override
    public void run() {
        System.out.println("starting thread");
        while (true){
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            try {
                socket.receive(packet);
            } catch (IOException e){
                throw new RuntimeException();
            }
            String message = new String(packet.getData(), 0, packet.getLength()) + "\n";
            String current = textArea.getText();
            textArea.setText(current+message);
        }
    }
}
