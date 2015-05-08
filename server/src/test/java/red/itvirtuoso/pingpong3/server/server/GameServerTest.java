package red.itvirtuoso.pingpong3.server.server;

import org.junit.Test;

import java.util.ArrayList;

import red.itvirtuoso.pingpong3.server.client.ClientProxy;
import red.itvirtuoso.pingpong3.server.Packet;
import red.itvirtuoso.pingpong3.server.PacketType;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;

/**
 * Created by kenji on 15/05/04.
 */
public class GameServerTest {
    /* ゲームループの単位時間。テスト用に短くしている */
    private static final long STEP_TIME = 50;

    private class _LogBuilder {
        private long beginTime;
        private long stepTime;

        private _LogBuilder(long stepTime) {
            this.beginTime = System.currentTimeMillis();
            this.stepTime = stepTime;
        }

        private _Log create(PacketType type) {
            return new _Log((System.currentTimeMillis() - this.beginTime) / this.stepTime, type);
        }

        private _Log create(long step, PacketType type) {
            return new _Log(step, type);
        }
    }

    /* サーバからのパケット送信を記録するテスト用のクラス */
    private class _Log {
        private long step;
        private PacketType type;

        private _Log(long step, PacketType type) {
            this.step = step;
            this.type = type;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            _Log log = (_Log) o;

            if (step != log.step) return false;
            if (type != log.type) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = (int) (step ^ (step >>> 32));
            result = 31 * result + type.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + "{" +
                    "step=" + step +
                    ", type=" + type +
                    '}';
        }
    }

    private class TestClientProxy extends ClientProxy {
        private boolean isClosed = false;
        private _LogBuilder builder = new _LogBuilder(STEP_TIME);
        private ArrayList<_Log> sendLogs = new ArrayList<>();

        @Override
        public boolean isClosed() {
            return isClosed;
        }

        @Override
        public void send(Packet packet) {
            sendLogs.add(builder.create(packet.getType()));
        }

        @Override
        public void close() {
            isClosed = true;
        }

        @Override
        protected void addPacket(Packet packet) {
            super.addPacket(packet);
        }

        private void clearPackets() {
            sendLogs.clear();
        }
    }

    @Test
    public void ゲームを開始する() throws Exception {
        /*
         * 次のパケットが順に送信される
         * <p>client1</p>
         * <ul>
         *     <li>0, ME_READY</li>
         * </ul>
         * <p>client2</p>
         * <ul>
         *     <li>0, RIVAL_READY</li>
         * </ul>
         */

        TestClientProxy client1 = new TestClientProxy();
        _LogBuilder builder = new _LogBuilder(STEP_TIME);
        GameServer gameServer = new GameServer(client1, STEP_TIME);
        TestClientProxy client2 = new TestClientProxy();
        gameServer.challenge(client2);

        assertThat(client1.sendLogs, is(contains(
                builder.create(0, PacketType.ME_READY)
        )));
        assertThat(client2.sendLogs, is(contains(
                builder.create(0, PacketType.RIVAL_READY)
        )));
    }

    @Test
    public void 一人目がサーブして二人目がミスする() throws Exception {
        /*
         * 次のパケットが順に送信される
         * <p>client1</p>
         * <ul>
         *     <li>0, ME_SERVE</li>
         *     <li>2, RIVAL_BOUND_MY_AREA</li>
         *     <li>4, RIVAL_BOUND_RIVAL_AREA</li>
         *     <li>8, ME_POINT</li>
         * </ul>
         * <p>client2</p>
         * <ul>
         *     <li>0, RIVAL_SERVE</li>
         *     <li>2, ME_BOUND_RIVAL_AREA</li>
         *     <li>4, ME_BOUND_ME_AREA</li>
         *     <li>8, RIVAL_POINT</li>
         * </ul>
         */

        TestClientProxy client1 = new TestClientProxy();
        GameServer gameServer = new GameServer(client1, STEP_TIME);
        TestClientProxy client2 = new TestClientProxy();
        gameServer.challenge(client2);
        client1.clearPackets();
        client2.clearPackets();

        client1.addPacket(new Packet(PacketType.SWING));
        Thread.sleep(STEP_TIME * 8);
        Thread.sleep(STEP_TIME);

        _LogBuilder builder = new _LogBuilder(STEP_TIME);
        assertThat(client1.sendLogs, is(contains(
                builder.create(0, PacketType.ME_SERVE),
                builder.create(2, PacketType.RIVAL_BOUND_MY_AREA),
                builder.create(4, PacketType.RIVAL_BOUND_RIVAL_AREA),
                builder.create(8, PacketType.ME_POINT)
        )));
        assertThat(client2.sendLogs, is(contains(
                builder.create(0, PacketType.RIVAL_SERVE),
                builder.create(2, PacketType.ME_BOUND_RIVAL_AREA),
                builder.create(4, PacketType.ME_BOUND_MY_AREA),
                builder.create(8, PacketType.RIVAL_POINT)
        )));
    }

    @Test
    public void 一人目がサーブして二人目がリターンする() throws Exception {
        /*
         * 次のパケットが順に送信される
         * <p>client1</p>
         * <ul>
         *     <li>0, ME_SERVE</li>
         *     <li>2, RIVAL_BOUND_MY_AREA</li>
         *     <li>4, RIVAL_BOUND_RIVAL_AREA</li>
         *     <li>6, RIVAL_RETURN</li>
         *     <li>10, ME_BOUND_MY_AREA</li>
         *     <li>14, RIVAL_POINT</li>
         * </ul>
         * <p>client2</p>
         * <ul>
         *     <li>0, RIVAL_SERVE</li>
         *     <li>2, ME_BOUND_RIVAL_AREA</li>
         *     <li>4, ME_BOUND_ME_AREA</li>
         *     <li>6, ME_RETURN</li>
         *     <li>10, RIVAL_BOUND_RIVAL_AREA</li>
         *     <li>14, ME_POINT</li>
         * </ul>
         */

        TestClientProxy client1 = new TestClientProxy();
        GameServer gameServer = new GameServer(client1, STEP_TIME);
        TestClientProxy client2 = new TestClientProxy();
        gameServer.challenge(client2);
        client1.clearPackets();
        client2.clearPackets();

        client1.addPacket(new Packet(PacketType.SWING));
        Thread.sleep(STEP_TIME * 6);
        client2.addPacket(new Packet(PacketType.SWING));
        Thread.sleep(STEP_TIME * 8);
        Thread.sleep(STEP_TIME);

        _LogBuilder builder = new _LogBuilder(STEP_TIME);
        assertThat(client1.sendLogs, is(contains(
                builder.create(0, PacketType.ME_SERVE),
                builder.create(2, PacketType.RIVAL_BOUND_MY_AREA),
                builder.create(4, PacketType.RIVAL_BOUND_RIVAL_AREA),
                builder.create(6, PacketType.RIVAL_RETURN),
                builder.create(10, PacketType.ME_BOUND_MY_AREA),
                builder.create(14, PacketType.RIVAL_POINT)
        )));
        assertThat(client2.sendLogs, is(contains(
                builder.create(0, PacketType.RIVAL_SERVE),
                builder.create(2, PacketType.ME_BOUND_RIVAL_AREA),
                builder.create(4, PacketType.ME_BOUND_MY_AREA),
                builder.create(6, PacketType.ME_RETURN),
                builder.create(10, PacketType.RIVAL_BOUND_RIVAL_AREA),
                builder.create(14, PacketType.ME_POINT)
        )));
    }
}
