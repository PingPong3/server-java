package red.itvirtuoso.pingpong3.server;

/**
 * Created by kenji on 15/05/04.
 */
public enum PacketType {
    BEGIN(1),
    SWING(2),
    ME_READY(3),
    ME_SERVE(4),
    ME_RETURN(5),
    ME_BOUND_MY_AREA(6),
    ME_BOUND_RIVAL_AREA(7),
    ME_POINT(8),
    RIVAL_READY(9),
    RIVAL_SERVE(10),
    RIVAL_RETURN(11),
    RIVAL_BOUND_MY_AREA(12),
    RIVAL_BOUND_RIVAL_AREA(13),
    RIVAL_POINT(14),
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
