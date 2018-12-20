package hlt;

public class EntityId {
    public static final EntityId NONE = new EntityId(-1);

    public final int id;

    public EntityId(int id) {
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

        EntityId entityId = (EntityId) o;

        return id == entityId.id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
