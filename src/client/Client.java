package client;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.*;
import java.util.Scanner;

public class Client extends Application {

    private static final DatagramSocket socket;

    static {
        try {
            socket = new DatagramSocket(); // init to any available port
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }

    private static final InetAddress address;

    static {
        try {
            address = InetAddress.getByName("localhost");
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    private static String identifier;

    private static final int SERVER_PORT = 8000; // send to server

    private static final TextArea messageArea = new TextArea();

    private static final TextField inputBox = new TextField();


    public static void main(String[] args) throws IOException {

        Scanner sc  = new Scanner(System.in);
        identifier = sc.nextLine();

        // thread for receiving messages
        ClientThread clientThread = new ClientThread(socket, messageArea);
        clientThread.start();

        // send initialization message to the server
        String error = "Ese usuario no está displonible, introduzca uno nuevo";
        byte[] errorBytes = error.getBytes();

        /*DatagramPacket packet = new DatagramPacket(errorBytes, errorBytes.length);
        do{
            byte[] uuid = ("init; " + identifier).getBytes();
            DatagramPacket initialize = new DatagramPacket(uuid, uuid.length, address, SERVER_PORT);
            socket.send(initialize);
        }
        while (socket.receive(packet.getData()) != );*/

        launch(); // launch GUI

    }


    @Override
    public void start(Stage primaryStage) {

        messageArea.setMaxWidth(500);
        messageArea.setEditable(false);


        inputBox.setMaxWidth(500);
        inputBox.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                String temp = identifier + ";" + inputBox.getText(); // message to send
                messageArea.setText(messageArea.getText() + inputBox.getText() + "\n"); // update messages on screen
                byte[] msg = temp.getBytes(); // convert to bytes
                inputBox.setText(""); // remove text from input box

                // create a packet & send
                DatagramPacket send = new DatagramPacket(msg, msg.length, address, SERVER_PORT);
                try {
                    socket.send(send);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        // put everything on screen
        Scene scene = new Scene(new VBox(35, messageArea, inputBox), 550, 300);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}