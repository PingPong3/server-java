package red.itvirtuoso.pingpong3.server.client;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import red.itvirtuoso.pingpong3.server.Packet;
import red.itvirtuoso.pingpong3.server.PacketType;

/**
 * Created by kenji on 15/05/06.
 */
public class WallClientProxy extends ClientProxy {
    private long originalStepTime;
    private long stepTime;
    private boolean isClosed;
    private Random random;

    public WallClientProxy(long stepTime) {
        super();
        this.originalStepTime = stepTime;
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
            this.stepTime = this.originalStepTime;
            receiveSwing(6);
        } else if (packet.getType() == PacketType.RIVAL_READY) {
            this.stepTime = this.originalStepTime;
        } else if (packet.getType() == PacketType.ME_BOUND_MY_AREA) {
//            if (random.nextInt(4) == 0) {
//            /* 一定の確率て空振りさせる */
//                return;
//            }
            receiveSwing(2);
        }
    }

    private void receiveSwing(long step) {
        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
        service.schedule(new Runnable() {
            @Override
            public void run() {
                addPacket(new Packet(PacketType.SWING));
            }
        }, step * stepTime, TimeUnit.MILLISECONDS);
        stepTime -= (stepTime - 100 > 20 ? 20 : stepTime - 100);
    }

    @Override
    public void close() {
        isClosed = true;
    }
}
