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
    public final void onClose() {
        System.out.println("ClientProxy is closed");
    }
    public final void receive(Packet packet) {
        /* TODO */
    }
}
