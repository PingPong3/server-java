package red.itvirtuoso.pingpong3.server;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import red.itvirtuoso.pingpong3.server.client.ClientProxy;
import red.itvirtuoso.pingpong3.server.client.Packet;
import red.itvirtuoso.pingpong3.server.client.PacketType;

/**
 * Created by kenji on 15/05/04.
 */
public class GameServer implements Runnable {
    private ClientProxy client1;
    private ClientProxy client2;

    public GameServer(ClientProxy client1) {
        this.client1 = client1;
        this.client1.send(new Packet(PacketType.CONNECT_SUCCESS));
    }

    public void challenge(ClientProxy client2) {
        this.client2 = client2;
        this.client2.send(new Packet(PacketType.CONNECT_SUCCESS));
        this.client1.send(new Packet(PacketType.ME_READY));
        this.client2.send(new Packet(PacketType.RIVAL_READY));

        ExecutorService service = Executors.newSingleThreadExecutor();
        service.execute(this);
        service.shutdown();
    }

    @Override
    public void run() {
        System.out.println("Start game");
        while (!client1.isClosed() && !client2.isClosed()) {
            Thread.yield();
        }
        System.out.println("End game");
    }
}
