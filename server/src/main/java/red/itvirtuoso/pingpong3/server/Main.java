package red.itvirtuoso.pingpong3.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketTimeoutException;

import red.itvirtuoso.pingpong3.server.client.ClientProxy;
import red.itvirtuoso.pingpong3.server.client.SocketClientProxy;
import red.itvirtuoso.pingpong3.server.client.WallClientProxy;
import red.itvirtuoso.pingpong3.server.server.GameServer;

public class Main {
    private static final int TIMEOUT = 10 * 1000;

    public static void main(String[] args) {
        try (final ServerSocket listener = new ServerSocket()) {
            listener.setReuseAddress(true);
            listener.bind(new InetSocketAddress(5000));
            System.out.println("Server listening port 5000...");
            while (true) {
                try {
                    System.out.println("start new loop");
                    ClientProxy clientProxy1 = new SocketClientProxy(listener.accept());
                    System.out.println("Client1 is connected.");
                    GameServer gameServer = new GameServer(clientProxy1);
                    ClientProxy clientProxy2 = null;
                    try {
                        listener.setSoTimeout(TIMEOUT);
                        clientProxy2 = new SocketClientProxy(listener.accept());
                        System.out.println("Client2 is connected.");
                    } catch (SocketTimeoutException e) {
                        clientProxy2 = new WallClientProxy(GameServer.STEP_TIME);
                        System.out.println("Client2 is cpu player.");
                    } catch (Exception e) {
                        if (clientProxy1 != null) {
                            clientProxy1.close();
                        }
                        if (clientProxy2 != null) {
                            clientProxy2.close();
                        }
                        continue;
                    } finally {
                        listener.setSoTimeout(0);
                    }
                    gameServer.challenge(clientProxy2, false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
