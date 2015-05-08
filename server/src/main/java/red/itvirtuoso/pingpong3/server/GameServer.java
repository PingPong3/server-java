package red.itvirtuoso.pingpong3.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import red.itvirtuoso.pingpong3.server.client.ClientProxy;
import red.itvirtuoso.pingpong3.server.client.Packet;
import red.itvirtuoso.pingpong3.server.client.PacketType;

/**
 * Created by kenji on 15/05/04.
 */
public class GameServer implements Runnable {
    private static final long STEP_TIME = 750;

    private long stepTime;
    private ClientProxy client1;
    private ClientProxy client2;
    private Mode mode;
    private ArrayList<Action> actions = new ArrayList<>();

    public GameServer(ClientProxy client1, long stepTime) {
        this.client1 = client1;
        this.stepTime = stepTime;
        mode = Mode.INITIALIZE;
    }

    public GameServer(ClientProxy client1) {
        this(client1, STEP_TIME);
    }

    public void challenge(ClientProxy client2) {
        this.client2 = client2;
        this.client1.send(new Packet(PacketType.ME_READY));
        this.client2.send(new Packet(PacketType.RIVAL_READY));

        ExecutorService service = Executors.newSingleThreadExecutor();
        service.execute(this);
        service.shutdown();
        mode = Mode.CLIENT1_READY;
    }

    @Override
    public void run() {
        System.out.println("Start game");
        while (!client1.isClosed() && !client2.isClosed()) {
            Thread.yield();
            doClient1Action(client1.receive());
//            doClient2Action(client2.receive());
            doReservedActions();
        }
        System.out.println("End game");
    }

    private void doClient1Action(Packet packet) {
        if (packet == null) {
            return;
        }

        System.out.println("doClient1Action packet = " + packet + ", mode = " + mode);
        if (packet.getType() == PacketType.SWING) {
            if (mode == Mode.CLIENT1_READY) {
                client1Serve();
            }
        }
    }

    private void client1Serve() {
        long currentTime = System.currentTimeMillis();
        synchronized (actions) {
            addPacketAction(currentTime, Target.CLIENT1, 0, PacketType.ME_SERVE);
            addPacketAction(currentTime, Target.CLIENT1, 1, PacketType.RIVAL_BOUND_MY_AREA);
            addPacketAction(currentTime, Target.CLIENT1, 2, PacketType.RIVAL_BOUND_RIVAL_AREA);
            addPacketAction(currentTime, Target.CLIENT1, 4, PacketType.ME_POINT);

            addPacketAction(currentTime, Target.CLIENT2, 0, PacketType.RIVAL_SERVE);
            addPacketAction(currentTime, Target.CLIENT2, 1, PacketType.ME_BOUND_RIVAL_AREA);
            addPacketAction(currentTime, Target.CLIENT2, 2, PacketType.ME_BOUND_MY_AREA);
            addPacketAction(currentTime, Target.CLIENT2, 4, PacketType.RIVAL_POINT);

            Collections.sort(actions);
        }
    }

    private void addPacketAction(long currentTime, Target target, int step, PacketType type) {
        long time = currentTime + step * stepTime;
        actions.add(new PacketAction(time, target, type) {
            @Override
            public void execute() {
                Packet packet = new Packet(getType());
                switch (getTarget()) {
                    case CLIENT1:
                        client1.send(packet);
                        break;
                    case CLIENT2:
                        client2.send(packet);
                        break;
                    default: /* nop */
                }
            }
        });
    }

    private void doReservedActions() {
        synchronized (actions) {
            while (actions.size() > 0) {
                Action action = actions.get(0);
                if (action.getTime() > System.currentTimeMillis()) {
                    break;
                }
                action.execute();
                actions.remove(0);
            }
        }
    }
}
