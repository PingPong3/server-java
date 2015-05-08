package red.itvirtuoso.pingpong3.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import red.itvirtuoso.pingpong3.server.client.local.WallClientProxy;
import red.itvirtuoso.pingpong3.server.client.socket.SocketClientProxy;
import red.itvirtuoso.pingpong3.server.server.GameServer;

public class Main {
    public static void main(String[] args) {
        try (ServerSocket listener = new ServerSocket()) {
            listener.setReuseAddress(true);
            listener.bind(new InetSocketAddress(5000));
            System.out.println("Server listening port 5000...");
            while (true) {
                Socket socket1 = listener.accept();
                GameServer gameServer = new GameServer(new SocketClientProxy(socket1));
                gameServer.challenge(new WallClientProxy());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
