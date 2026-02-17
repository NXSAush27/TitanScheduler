package titan_scheduler.core;

import titan_scheduler.models.Action;
import titan_scheduler.models.ScheduledJob;

public class Main {
    public static void main(String[] args) {
        TitanEngine TE = new TitanEngine();
        TE.submitJob(new ScheduledJob(1, Action.EXECUTE));
        TE.submitJob(new ScheduledJob(2, Action.OPEN_DESKTOP));
        TE.run();
    }
}