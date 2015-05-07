package red.itvirtuoso.pingpong3.server.client;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

/**
 * Created by kenji on 15/05/08.
 */
public class ClientProxyTest {
    private class TestClientProxy extends ClientProxy {
        @Override
        public boolean isClosed() {
            return false;
        }

        @Override
        public void send(Packet packet) throws ClientException {
            /* nop */
        }

        @Override
        public void close() {
            onClose();
        }
    }

    @Test
    public void パケットを何も追加していない時のreceiveメソッドの返り値() throws Exception {
        TestClientProxy clientProxy = new TestClientProxy();
        assertThat(clientProxy.receive(), is(nullValue()));
        clientProxy.close();
    }

    @Test
    public void パケットを追加したらその順にreceiveメソッドの返り値が取得できる() throws Exception {
        Packet packet1 = new Packet(PacketType.ME_READY);
        Packet packet2 = new Packet(PacketType.RIVAL_READY);
        TestClientProxy clientProxy = new TestClientProxy();
        clientProxy.addPacket(packet1);
        clientProxy.addPacket(packet2);
        assertThat(clientProxy.receive(), is(packet1));
        assertThat(clientProxy.receive(), is(packet2));
        assertThat(clientProxy.receive(), is(nullValue()));
        clientProxy.close();
    }
}
