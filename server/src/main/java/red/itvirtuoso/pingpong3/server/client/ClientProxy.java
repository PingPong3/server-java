package red.itvirtuoso.pingpong3.server.client;

import java.util.ArrayList;

/**
 * Created by kenji on 15/05/04.
 */
public abstract class ClientProxy {
    private ArrayList<Packet> packets;

    public ClientProxy() {
        packets = new ArrayList<>();
        System.out.println(getClass().getSimpleName() + " is connected");
    }

    public abstract boolean isClosed();
    public abstract void send(Packet packet) throws ClientException;

    public abstract void close();

    protected final void onClose() {
        System.out.println(getClass().getSimpleName() + " is closed");
    }

    protected void addPacket(Packet packet) {
        synchronized (packets) {
            packets.add(packet);
        }
    }

    public final Packet receive() {
        synchronized (packets) {
            return packets.size() == 0 ? null : packets.remove(0);
        }
    }
}
