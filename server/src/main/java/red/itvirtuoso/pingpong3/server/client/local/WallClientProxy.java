package red.itvirtuoso.pingpong3.server.client.local;

import red.itvirtuoso.pingpong3.server.client.ClientException;
import red.itvirtuoso.pingpong3.server.client.ClientProxy;
import red.itvirtuoso.pingpong3.server.client.Packet;

/**
 * Created by kenji on 15/05/06.
 */
public class WallClientProxy extends ClientProxy {
    private boolean isClosed;

    public WallClientProxy() {
        super();
        this.isClosed = false;
    }

    @Override
    public boolean isClosed() {
        return isClosed;
    }

    @Override
    public void send(Packet packet) throws ClientException {
        /* TODO */
    }
}
