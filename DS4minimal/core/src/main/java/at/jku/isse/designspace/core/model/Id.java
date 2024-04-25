package at.jku.isse.designspace.core.model;

import java.io.Serializable;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.util.Assert;

/**
 * This class is used throughout the project to uniquely identify
 * entities such as instances or events. Even though an id is essentially a long, it is advised to always declare ids
 * using this ID class to avoid confusion with normal "long" value used otherwise (e.g., a operation value being an ID
 * would represent an instance)
 */
public class Id implements Serializable {

    public static AtomicLong currentId = new AtomicLong(1);

    /**
     * The actual value of the ID.
     */
    private final long id;

    private Id(Long id) {
        Assert.notNull(id, "ID must not be null.");
        this.id = id;
    }

    /**
     * @return A new ID object with a random value. This object
     * can safely be assumed to be globally unique.
     */
    public static Id newId() {
        Long id = currentId.getAndIncrement();
        return new Id(id);
    }

    public static Id of(long idLong) {
        return new Id(idLong);
    }

    public long value() { return id; }

    @Override
    public boolean equals(Object otherId) {
        if (otherId==null || getClass() != otherId.getClass()) return false;
        if (otherId==this) return true;
        return id == ((Id)otherId).id;
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return ""+id;
    }
}
