package red.itvirtuoso.pingpong3.server.server.action;

import red.itvirtuoso.pingpong3.server.PacketType;
import red.itvirtuoso.pingpong3.server.server.Target;

/**
 * Created by kenji on 15/05/03.
 */
public abstract class PacketAction extends Action {
    private Target target;
    private PacketType type;

    public PacketAction(long time, Target target, PacketType type) {
        super(time);
        this.target = target;
        this.type = type;
    }

    public Target getTarget() {
        return target;
    }

    protected PacketType getType() {
        return type;
    }
}
