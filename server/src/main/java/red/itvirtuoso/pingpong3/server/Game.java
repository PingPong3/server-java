package red.itvirtuoso.pingpong3.server;

import red.itvirtuoso.pingpong3.server.client.ClientProxy;
import red.itvirtuoso.pingpong3.server.client.Packet;
import red.itvirtuoso.pingpong3.server.client.PacketType;

/**
 * Created by kenji on 15/05/04.
 */
public class Game {
    private ClientProxy client1;

    public Game (ClientProxy client1) {
        this.client1 = client1;
        Packet packet = new Packet(PacketType.ON_CONNECT_SUCCESS);
        this.client1.send(packet);
    }
}
