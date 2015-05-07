package red.itvirtuoso.pingpong3.server.client;

/**
 * Created by kenji on 15/05/04.
 */
public abstract class ClientProxy {
    public ClientProxy() {
        System.out.println(getClass().getSimpleName() + " is connected");
    }

    public abstract boolean isClosed();
    public abstract void send(Packet packet) throws ClientException;

    public abstract void close();

    protected final void onClose() {
        System.out.println(getClass().getSimpleName() + " is closed");
    }

    protected void addPacket(Packet packet) {
        /* nop */
    }

    public final Packet receive() {
        return null;
    }
}
