package hlt;

public class Command {
    public final String command;

    public static Command spawnShip() {
        return new Command("g");
    }

    public static Command transformShipIntoDropoffSite(final EntityId id) {
        return new Command("c " + id);
    }

    public static Command move(final EntityId id, final Direction direction) {
        return new Command("m " + id + ' ' + direction.charValue);
    }

    private Command(final String command) {
        this.command = command;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Command command1 = (Command) o;

        return command.equals(command1.command);
    }

    @Override
    public int hashCode() {
        return command.hashCode();
    }

    @Override
    public String toString() {
        return command;
    }
}
