package server;

import java.net.DatagramSocket;
import java.net.SocketException;

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

}
