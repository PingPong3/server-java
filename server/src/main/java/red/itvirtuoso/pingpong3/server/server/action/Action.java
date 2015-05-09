package red.itvirtuoso.pingpong3.server.server.action;

/**
 * Created by kenji on 15/05/03.
 */
public abstract class Action {
    private long time;

    public Action(long time) {
        this.time = time;
    }

    public long getTime() {
        return time;
    }

    public abstract void execute();
}
