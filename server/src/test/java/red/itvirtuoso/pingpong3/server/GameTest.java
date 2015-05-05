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
public class GameTest {
    private class TestClientProxy implements ClientProxy {
        private ArrayList<Packet> packets = new ArrayList<>();

        @Override
        public void send(Packet packet) {
            packets.add(packet);
        }
    }
    @Test
    public void Gameインスタンスを作成する() throws Exception {
        TestClientProxy client1 = new TestClientProxy();
        Game game = new Game(client1);
        assertThat(client1.packets, is(contains(
                new Packet(PacketType.ON_CONNECT_SUCCESS)
        )));
    }
}
