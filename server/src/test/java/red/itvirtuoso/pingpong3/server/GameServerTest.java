package red.itvirtuoso.pingpong3.server;

import org.junit.Test;

import java.util.ArrayList;

import red.itvirtuoso.pingpong3.server.client.ClientProxy;
import red.itvirtuoso.pingpong3.server.client.Packet;
import red.itvirtuoso.pingpong3.server.client.PacketType;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;

/**
 * Created by kenji on 15/05/04.
 */
public class GameServerTest {
    private class TestClientProxy extends ClientProxy {
        private ArrayList<Packet> packets = new ArrayList<>();

        @Override
        public boolean isClosed() {
            return true;
        }

        @Override
        public void send(Packet packet) {
            packets.add(packet);
        }
    }
    @Test
    public void 一人目がGameインスタンスを作成する() throws Exception {
        TestClientProxy client1 = new TestClientProxy();
        GameServer gameServer = new GameServer(client1);

        assertThat(client1.packets, is(contains(
                new Packet(PacketType.CONNECT_SUCCESS)
        )));
    }

    @Test
    public void 二人目がGameに参加する() throws Exception {
        TestClientProxy client1 = new TestClientProxy();
        GameServer gameServer = new GameServer(client1);
        TestClientProxy client2 = new TestClientProxy();
        gameServer.challenge(client2);

        assertThat(client1.packets, is(contains(
                new Packet(PacketType.CONNECT_SUCCESS),
                new Packet(PacketType.ME_READY)
        )));
        assertThat(client2.packets, is(contains(
                new Packet(PacketType.CONNECT_SUCCESS),
                new Packet(PacketType.RIVAL_READY)
        )));
    }
}
