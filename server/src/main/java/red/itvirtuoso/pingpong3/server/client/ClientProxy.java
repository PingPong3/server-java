package red.itvirtuoso.pingpong3.server.client;

import java.util.ArrayList;

import red.itvirtuoso.pingpong3.server.Packet;

/**
 * Created by kenji on 15/05/04.
 */
public abstract class ClientProxy {
    private ArrayList<Packet> packets = new ArrayList<>();
    private int score = 0;

    public abstract boolean isClosed();
    public abstract void send(Packet packet) throws ClientException;

    public abstract void close();

    protected void addPacket(Packet packet) {
        synchronized (packets) {
            packets.add(packet);
        }
    }

    public final Packet receive() {
        synchronized (packets) {
            Packet packet;
            if (packets.size() > 0) {
                packet = packets.remove(0);
            } else {
                packet = null;
            }
            return packet;
        }
    }
}
