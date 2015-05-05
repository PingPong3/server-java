package red.itvirtuoso.pingpong3.server;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    public static void main(String[] args) {
        try (ServerSocket listener = new ServerSocket()) {
            listener.setReuseAddress(true);
            listener.bind(new InetSocketAddress(5000));
            System.out.println("Server listening port 5000...");
            while (true) {
                try (Socket socket = listener.accept()) {
                    InputStream stream = socket.getInputStream();
                    while (true) {
                        int data = stream.read();
                        if (data < 0) {
                            break;
                        }
                        System.out.println(data);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
