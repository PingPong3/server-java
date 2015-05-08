package red.itvirtuoso.pingpong3.server;

/**
 * Created by kenji on 15/05/03.
 */
abstract class Action implements Comparable<Action> {
    private long time;

    public Action(long time) {
        this.time = time;
    }

    public long getTime() {
        return time;
    }

    public abstract void execute();

    @Override
    public int compareTo(Action that) {
        Long thisValue = Long.valueOf(time);
        Long thatValue = Long.valueOf(that.getTime());
        return thisValue.compareTo(thatValue);
    }
}
