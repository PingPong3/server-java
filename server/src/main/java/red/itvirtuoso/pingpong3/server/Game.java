package red.itvirtuoso.pingpong3.server;

import red.itvirtuoso.pingpong3.server.client.ClientProxy;
import red.itvirtuoso.pingpong3.server.client.Packet;
import red.itvirtuoso.pingpong3.server.client.PacketType;

/**
 * Created by kenji on 15/05/04.
 */
public class Game {
    private ClientProxy client1;
    private ClientProxy client2;

    public Game (ClientProxy client1) {
        this.client1 = client1;
        this.client1.send(new Packet(PacketType.CONNECT_SUCCESS));
    }

    public void join(ClientProxy client2) {
        this.client2 = client2;

        this.client2.send(new Packet(PacketType.CONNECT_SUCCESS));
        this.client1.send(new Packet(PacketType.ME_READY));
        this.client2.send(new Packet(PacketType.RIVAL_READY));
    }
}
