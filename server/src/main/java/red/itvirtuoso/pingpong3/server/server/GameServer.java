package red.itvirtuoso.pingpong3.server.server;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import red.itvirtuoso.pingpong3.server.Packet;
import red.itvirtuoso.pingpong3.server.PacketType;
import red.itvirtuoso.pingpong3.server.client.ClientProxy;
import red.itvirtuoso.pingpong3.server.server.action.Action;
import red.itvirtuoso.pingpong3.server.server.action.ModeAction;
import red.itvirtuoso.pingpong3.server.server.action.PacketAction;
import red.itvirtuoso.pingpong3.server.server.action.ScoreAction;

/**
 * Created by kenji on 15/05/04.
 */
public class GameServer implements Runnable {
    public static final long STEP_TIME = 350;

    private long originalStepTime;
    private long stepTime;
    private ClientProxy client1;
    private ClientProxy client2;
    private Mode mode;
    private ArrayList<Action> actions = new ArrayList<>();

    public GameServer(ClientProxy client1, long stepTime) {
        this.client1 = client1;
        this.originalStepTime = stepTime;
        this.stepTime = stepTime;
        mode = Mode.INITIALIZE;
    }

    public GameServer(ClientProxy client1) {
        this(client1, STEP_TIME);
    }

    public void challenge(ClientProxy client2, boolean hasServe) {
        this.client1.send(new Packet(PacketType.BEGIN));
        client2.send(new Packet(PacketType.BEGIN));

        if (hasServe) {
            ClientProxy client1 = this.client1;
            this.client1 = client2;
            challenge(client1);
        } else {
            challenge(client2);
        }
    }

    private void challenge(ClientProxy client2) {
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
            doClient1(client1.receive());
            doClient2(client2.receive());
            executeActions();
        }
        System.out.println("End game");
    }

    private void doClient1(Packet packet) {
        if (packet == null) {
            return;
        }

        if (packet.getType() == PacketType.SWING) {
            if (mode == Mode.CLIENT1_READY) {
                clientTurn(Target.CLIENT1, true);
            } else if (mode == Mode.CLIENT1_RETURN) {
                clientTurn(Target.CLIENT1, false);
            }
        }
    }

    private void doClient2(Packet packet) {
        if (packet == null) {
            return;
        }

        if (packet.getType() == PacketType.SWING) {
            if (mode == Mode.CLIENT2_READY) {
                clientTurn(Target.CLIENT2, true);
            } else if (mode == Mode.CLIENT2_RETURN) {
                clientTurn(Target.CLIENT2, false);
            }
        }
    }

    private void clientTurn(Target target1, boolean isServe) {
        Target target2 = (target1 == Target.CLIENT1 ? Target.CLIENT2 : Target.CLIENT1);
        long currentTime = System.currentTimeMillis();
        synchronized (actions) {
            actions.clear();
            stepTime -= (stepTime - 100 > 10 ? 10 : stepTime - 100);
            addModeAction(currentTime, 0, Mode.BUSY);
            if (isServe) {
                addPacketAction(currentTime, target1, 0, PacketType.ME_SERVE);
                addPacketAction(currentTime, target2, 0, PacketType.RIVAL_SERVE);
                addPacketAction(currentTime, target1, 2, PacketType.RIVAL_BOUND_MY_AREA);
                addPacketAction(currentTime, target2, 2, PacketType.ME_BOUND_RIVAL_AREA);
            } else {
                addPacketAction(currentTime, target1, 0, PacketType.ME_RETURN);
                addPacketAction(currentTime, target2, 0, PacketType.RIVAL_RETURN);
            }
            addPacketAction(currentTime, target1, 4, PacketType.RIVAL_BOUND_RIVAL_AREA);
            addPacketAction(currentTime, target2, 4, PacketType.ME_BOUND_MY_AREA);
            addReturnModeAction(currentTime, 5, target1);
            addModeAction(currentTime, 7, Mode.BUSY);
            addScoreAction(currentTime, 8, target1);
            addPacketAction(currentTime, target1, 8, PacketType.ME_POINT);
            addPacketAction(currentTime, target2, 8, PacketType.RIVAL_POINT);
            addReadyModeAction(currentTime, 12, target1);
            addPacketAction(currentTime, target1, 12, PacketType.ME_READY);
            addPacketAction(currentTime, target2, 12, PacketType.RIVAL_READY);
        }
    }

    private void addPacketAction(long currentTime, Target target, int step, final PacketType type) {
        long time = currentTime + step * stepTime;
        actions.add(new PacketAction(time, target, type) {
            @Override
            public void execute() {
                ClientProxy client1 = (getTarget() == Target.CLIENT1 ? GameServer.this.client1 : GameServer.this.client2);
                ClientProxy client2 = (getTarget() == Target.CLIENT1 ? GameServer.this.client2 : GameServer.this.client1);

                Packet packet;
                if (type == PacketType.ME_POINT || type == PacketType.RIVAL_POINT) {
                    packet = new Packet(getType(), client1.getScore(), client2.getScore());
                    GameServer.this.stepTime = GameServer.this.originalStepTime;
                } else {
                    packet = new Packet(getType());
                }
                client1.send(packet);
            }
        });
    }

    private void addReturnModeAction(long currentTime, int step, Target target) {
        if (target == Target.CLIENT1) {
            addModeAction(currentTime, step, Mode.CLIENT2_RETURN);
        } else if (target == Target.CLIENT2) {
            addModeAction(currentTime, step, Mode.CLIENT1_RETURN);
        } else {
            /* NOP */
        }
    }

    private void addReadyModeAction(long currentTime, int step, Target target) {
        if (target == Target.CLIENT1) {
            addModeAction(currentTime, step, Mode.CLIENT1_READY);
        } else if (target == Target.CLIENT2) {
            addModeAction(currentTime, step, Mode.CLIENT2_READY);
        } else {
            /* NOP */
        }
    }

    private void addModeAction(long currentTime, int step, Mode mode) {
        long time = currentTime + step * stepTime;
        actions.add(new ModeAction(time, mode) {
            @Override
            public void execute() {
                GameServer.this.mode = getMode();
            }
        });
    }

    private void addScoreAction(long currentTime, int step, Target target) {
        long time = currentTime + step * stepTime;
        actions.add(new ScoreAction(time, target) {
            @Override
            public void execute() {
                if (getTarget() == Target.CLIENT1) {
                    client1.incrementScore();
                } else if (getTarget() == Target.CLIENT2) {
                    client2.incrementScore();
                } else {
                    /* NOP */
                }
            }
        });
    }

    private void executeActions() {
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
