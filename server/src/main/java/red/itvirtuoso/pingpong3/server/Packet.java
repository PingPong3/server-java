package red.itvirtuoso.pingpong3.server;

import java.util.Arrays;

/**
 * Created by kenji on 15/05/04.
 */
public class Packet {
    private final PacketType type;
    private int[] data;

    public Packet(PacketType type, int... data) {
        this.type = type;
        this.data = (data != null ? data : new int[0]);
    }

    public PacketType getType() {
        return type;
    }

    public int[] getData() {
        return data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Packet packet = (Packet) o;

        if (!Arrays.equals(data, packet.data)) return false;
        if (type != packet.type) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + Arrays.hashCode(data);
        return result;
    }

    @Override
    public String toString() {
        return "Packet{" +
                "type=" + type +
                ", data=" + Arrays.toString(data) +
                '}';
    }
}
