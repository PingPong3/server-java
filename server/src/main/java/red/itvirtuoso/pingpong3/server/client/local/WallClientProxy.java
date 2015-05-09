package red.itvirtuoso.pingpong3.server.client.local;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import red.itvirtuoso.pingpong3.server.Packet;
import red.itvirtuoso.pingpong3.server.PacketType;
import red.itvirtuoso.pingpong3.server.client.ClientException;
import red.itvirtuoso.pingpong3.server.client.ClientProxy;

/**
 * Created by kenji on 15/05/06.
 */
public class WallClientProxy extends ClientProxy {
    private long stepTime;
    private boolean isClosed;
    private Random random;

    public WallClientProxy(long stepTime) {
        super();
        this.stepTime = stepTime;
        this.isClosed = false;
        this.random = new Random();
    }

    @Override
    public boolean isClosed() {
        return isClosed;
    }

    @Override
    public void send(Packet packet) throws ClientException {
        if (packet.getType() == PacketType.ME_READY) {
            receiveSwing(6);
        } else if (packet.getType() == PacketType.ME_BOUND_MY_AREA) {
            receiveSwing(2);
        }
    }

    private void receiveSwing(long step) {
        if (random.nextInt(4) == 0) {
            /* 一定の確率て空振りさせる */
            return;
        }
        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
        service.schedule(new Runnable() {
            @Override
            public void run() {
                addPacket(new Packet(PacketType.SWING));
            }
        }, step * stepTime, TimeUnit.MILLISECONDS);
    }

    @Override
    public void close() {
        isClosed = true;
    }
}
