package red.itvirtuoso.pingpong3.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import red.itvirtuoso.pingpong3.server.client.ClientProxy;
import red.itvirtuoso.pingpong3.server.client.local.WallClientProxy;
import red.itvirtuoso.pingpong3.server.client.socket.SocketClientProxy;
import red.itvirtuoso.pingpong3.server.server.GameServer;

public class Main {
    public static void main(String[] args) {
        try (final ServerSocket listener = new ServerSocket()) {
            listener.setReuseAddress(true);
            listener.bind(new InetSocketAddress(5000));
            System.out.println("Server listening port 5000...");
            while (true) {
                ClientProxy clientProxy1 = new SocketClientProxy(listener.accept());
                GameServer gameServer = new GameServer(clientProxy1);
                ExecutorService service = Executors.newSingleThreadExecutor();
                Future<ClientProxy> future = service.submit(new Callable<ClientProxy>() {
                    @Override
                    public ClientProxy call() throws Exception {
                        return new SocketClientProxy(listener.accept());
                    }
                });
                ClientProxy clientProxy2 = null;
                try {
                    clientProxy2 = future.get(10, TimeUnit.SECONDS);
                } catch (TimeoutException e) {
                    clientProxy2 = new WallClientProxy(GameServer.STEP_TIME);
                } catch (Exception e) {
                    if (clientProxy1 != null) {
                        clientProxy1.close();
                    }
                    if (clientProxy2 != null) {
                        clientProxy2.close();
                    }
                    continue;
                }
                gameServer.challenge(clientProxy2, false);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
