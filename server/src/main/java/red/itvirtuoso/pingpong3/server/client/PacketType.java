package red.itvirtuoso.pingpong3.server.client;

/**
 * Created by kenji on 15/05/04.
 */
public enum PacketType {
    SWING(1),
    ME_READY(2),
    ME_SERVE(3),
    ME_RETURN(4),
    ME_BOUND_MY_AREA(5),
    ME_BOUND_RIVAL_AREA(6),
    ME_POINT(7),
    RIVAL_READY(8),
    RIVAL_SERVE(9),
    RIVAL_RETURN(10),
    RIVAL_BOUND_MY_AREA(11),
    RIVAL_BOUND_RIVAL_AREA(12),
    RIVAL_POINT(13),
    ;

    private int id;

    private PacketType(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static PacketType valueOf(int id) {
        for (PacketType type : values()) {
            if (type.getId() == id) {
                return type;
            }
        }
        return null;
    }
}
