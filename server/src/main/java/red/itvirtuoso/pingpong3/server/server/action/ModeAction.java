package red.itvirtuoso.pingpong3.server.server.action;

import red.itvirtuoso.pingpong3.server.server.Mode;

/**
 * Created by kenji on 15/05/08.
 */
public abstract class ModeAction extends Action {
    private Mode mode;

    protected ModeAction(long time, Mode mode) {
        super(time);
        this.mode = mode;
    }

    public Mode getMode() {
        return mode;
    }
}
