package hlt;

public class PlayerId {
    public final int id;

    public PlayerId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return String.valueOf(id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PlayerId playerId = (PlayerId) o;

        return id == playerId.id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
