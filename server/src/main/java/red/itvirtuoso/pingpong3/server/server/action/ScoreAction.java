package red.itvirtuoso.pingpong3.server.server.action;

import red.itvirtuoso.pingpong3.server.server.Target;

/**
 * Created by kenji on 15/05/30.
 */
public abstract class ScoreAction extends Action {
    private Target target;

    public ScoreAction(long time, Target target) {
        super(time);
        this.target = target;
    }

    public Target getTarget() {
        return target;
    }
}
