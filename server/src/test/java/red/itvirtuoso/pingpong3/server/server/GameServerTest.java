package red.itvirtuoso.pingpong3.server.server;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

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

        private _Log create(Packet packet) {
            return new _Log((System.currentTimeMillis() - this.beginTime) / this.stepTime, packet.getType(), packet.getData());
        }

        private _Log create(long step, PacketType type, int... data) {
            return new _Log(step, type, data);
        }
    }

    /* サーバからのパケット送信を記録するテスト用のクラス */
    private class _Log {
        private long step;
        private PacketType type;
        private int[] data;

        private _Log(long step, PacketType type, int... data) {
            this.step = step;
            this.type = type;
            this.data = (data != null ? data : new int[0]);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            _Log log = (_Log) o;

            if (step != log.step) return false;
            if (!Arrays.equals(data, log.data)) return false;
            if (type != log.type) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = (int) (step ^ (step >>> 32));
            result = 31 * result + type.hashCode();
            result = 31 * result + Arrays.hashCode(data);
            return result;
        }

        @Override
        public String toString() {
            return "_Log{" +
                    "step=" + step +
                    ", type=" + type +
                    ", data=" + Arrays.toString(data) +
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
            sendLogs.add(builder.create(packet));
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
            builder = new _LogBuilder(STEP_TIME);
        }
    }

    @Test
    public void ゲームを開始する() throws Exception {
        /* ゲーム開始 */
        TestClientProxy client1 = new TestClientProxy();
        GameServer gameServer = new GameServer(client1, STEP_TIME);
        TestClientProxy client2 = new TestClientProxy();
        gameServer.challenge(client2, false);

        /* パケットチェック */
        _LogBuilder builder = new _LogBuilder(STEP_TIME);
        assertThat(client1.sendLogs, is(contains(
                builder.create(0, PacketType.BEGIN),
                builder.create(0, PacketType.ME_READY)
        )));
        assertThat(client2.sendLogs, is(contains(
                builder.create(0, PacketType.BEGIN),
                builder.create(0, PacketType.RIVAL_READY)
        )));
    }

    @Test
    public void 後から参加したクライアントにサーブ権がある状態でゲームを開始する() throws Exception {
        /* ゲーム開始 */
        TestClientProxy client1 = new TestClientProxy();
        GameServer gameServer = new GameServer(client1, STEP_TIME);
        TestClientProxy client2 = new TestClientProxy();
        gameServer.challenge(client2, true);

        /* パケットチェック */
        _LogBuilder builder = new _LogBuilder(STEP_TIME);
        assertThat(client1.sendLogs, is(contains(
                builder.create(0, PacketType.BEGIN),
                builder.create(0, PacketType.RIVAL_READY)
        )));
        assertThat(client2.sendLogs, is(contains(
                builder.create(0, PacketType.BEGIN),
                builder.create(0, PacketType.ME_READY)
        )));
    }

    /*
     * 以下、次のアクションをテストする。複数のアクションを一つのテストで兼ねていることもある。
     * <ul>
     *     <li>一人目のサーブ</li>
     *     <li>一人目のリターン</li>
     *     <li>一人目のミス</li>
     *     <li>二人目のサーブ</li>
     *     <li>二人目のリターン</li>
     *     <li>二人目のミス</li>
     */

    @Test
    public void 一人目がサーブして二人目がミスする() throws Exception {
        /* ゲーム開始 */
        TestClientProxy client1 = new TestClientProxy();
        GameServer gameServer = new GameServer(client1, STEP_TIME);
        TestClientProxy client2 = new TestClientProxy();
        gameServer.challenge(client2, false);
        client1.clearPackets();
        client2.clearPackets();
        /* 一人目がサーブ */
        client1.addPacket(new Packet(PacketType.SWING));
        Thread.sleep(STEP_TIME * 12);
        Thread.sleep(STEP_TIME);

        /* パケットチェック */
        _LogBuilder builder = new _LogBuilder(STEP_TIME);
        assertThat(client1.sendLogs, is(contains(
                builder.create(0, PacketType.ME_SERVE),
                builder.create(2, PacketType.RIVAL_BOUND_MY_AREA),
                builder.create(4, PacketType.RIVAL_BOUND_RIVAL_AREA),
                builder.create(8, PacketType.ME_POINT, 1, 0),
                builder.create(12, PacketType.ME_READY)
        )));
        assertThat(client2.sendLogs, is(contains(
                builder.create(0, PacketType.RIVAL_SERVE),
                builder.create(2, PacketType.ME_BOUND_RIVAL_AREA),
                builder.create(4, PacketType.ME_BOUND_MY_AREA),
                builder.create(8, PacketType.RIVAL_POINT, 0, 1),
                builder.create(12, PacketType.RIVAL_READY)
        )));
    }

    @Test
    public void 二人目がリターンして一人目がミスする() throws Exception {
        /* ゲーム開始 */
        TestClientProxy client1 = new TestClientProxy();
        GameServer gameServer = new GameServer(client1, STEP_TIME);
        TestClientProxy client2 = new TestClientProxy();
        gameServer.challenge(client2, false);
        /* 一人目がサーブ */
        client1.addPacket(new Packet(PacketType.SWING));
        Thread.sleep(STEP_TIME * 6);
        /* パケットのクリア */
        client1.clearPackets();
        client2.clearPackets();
        /* 二人目がリターン */
        client2.addPacket(new Packet(PacketType.SWING));
        Thread.sleep(STEP_TIME * 12);
        Thread.sleep(STEP_TIME);

        /* パケットチェック */
        _LogBuilder builder = new _LogBuilder(STEP_TIME);
        assertThat(client1.sendLogs, is(contains(
                builder.create(0, PacketType.RIVAL_RETURN),
                builder.create(4, PacketType.ME_BOUND_MY_AREA),
                builder.create(8, PacketType.RIVAL_POINT, 0, 1),
                builder.create(12, PacketType.RIVAL_READY)
        )));
        assertThat(client2.sendLogs, is(contains(
                builder.create(0, PacketType.ME_RETURN),
                builder.create(4, PacketType.RIVAL_BOUND_RIVAL_AREA),
                builder.create(8, PacketType.ME_POINT, 1, 0),
                builder.create(12, PacketType.ME_READY)
        )));
    }

    @Test
    public void 一人目がリターンして二人目がミスする() throws Exception {
        /* ゲーム開始 */
        TestClientProxy client1 = new TestClientProxy();
        GameServer gameServer = new GameServer(client1, STEP_TIME);
        TestClientProxy client2 = new TestClientProxy();
        gameServer.challenge(client2, false);
        /* 一人目がサーブ */
        client1.addPacket(new Packet(PacketType.SWING));
        Thread.sleep(STEP_TIME * 6);
        /* 二人目がリターン */
        client2.addPacket(new Packet(PacketType.SWING));
        Thread.sleep(STEP_TIME * 6);
        /* パケットのクリア */
        client1.clearPackets();
        client2.clearPackets();
        /* 一人目がリターン */
        client1.addPacket(new Packet(PacketType.SWING));
        Thread.sleep(STEP_TIME * 12);
        Thread.sleep(STEP_TIME);

        /* パケットチェック */
        _LogBuilder builder = new _LogBuilder(STEP_TIME);
        assertThat(client1.sendLogs, is(contains(
                builder.create(0, PacketType.ME_RETURN),
                builder.create(4, PacketType.RIVAL_BOUND_RIVAL_AREA),
                builder.create(8, PacketType.ME_POINT, 1, 0),
                builder.create(12, PacketType.ME_READY)
        )));
        assertThat(client2.sendLogs, is(contains(
                builder.create(0, PacketType.RIVAL_RETURN),
                builder.create(4, PacketType.ME_BOUND_MY_AREA),
                builder.create(8, PacketType.RIVAL_POINT, 0, 1),
                builder.create(12, PacketType.RIVAL_READY)
        )));
    }

    @Test
    public void 二人目がサーブして一人目がミスする() throws Exception {
        /* ゲーム開始 */
        TestClientProxy client1 = new TestClientProxy();
        GameServer gameServer = new GameServer(client1, STEP_TIME);
        TestClientProxy client2 = new TestClientProxy();
        gameServer.challenge(client2, false);
        /* 一人目がサーブ */
        client1.addPacket(new Packet(PacketType.SWING));
        Thread.sleep(STEP_TIME * 6);
        /* 二人目がリターン */
        client2.addPacket(new Packet(PacketType.SWING));
        Thread.sleep(STEP_TIME * 12);
        /* パケットのクリア */
        client1.clearPackets();
        client2.clearPackets();
        /* 二人目がサーブ */
        client2.addPacket(new Packet(PacketType.SWING));
        Thread.sleep(STEP_TIME * 12);
        Thread.sleep(STEP_TIME);

        /* パケットチェック */
        _LogBuilder builder = new _LogBuilder(STEP_TIME);
        assertThat(client1.sendLogs, is(contains(
                builder.create(0, PacketType.RIVAL_SERVE),
                builder.create(2, PacketType.ME_BOUND_RIVAL_AREA),
                builder.create(4, PacketType.ME_BOUND_MY_AREA),
                builder.create(8, PacketType.RIVAL_POINT, 0, 2),
                builder.create(12, PacketType.RIVAL_READY)
        )));
        assertThat(client2.sendLogs, is(contains(
                builder.create(0, PacketType.ME_SERVE),
                builder.create(2, PacketType.RIVAL_BOUND_MY_AREA),
                builder.create(4, PacketType.RIVAL_BOUND_RIVAL_AREA),
                builder.create(8, PacketType.ME_POINT, 2, 0),
                builder.create(12, PacketType.ME_READY)
        )));
    }
}
