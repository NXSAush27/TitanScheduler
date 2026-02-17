package titan_scheduler.models;

public class Action{
    private final String name;
    private final Runnable action;

    public Action(String name, Runnable action) {
        this.name = name;
        this.action = action;
    }

    public String getName() {
        return name;
    }

    public boolean execute() {
        action.run();
        return true;
    }

    public static final Action EXECUTE = new Action("EXECUTE", () -> {
        System.out.println("Executing action...");
    });
    public static final Action OPEN_DESKTOP = new Action("OPEN_DESKTOP", () -> {
        System.out.println("Opening Desktop...");
    });
}
