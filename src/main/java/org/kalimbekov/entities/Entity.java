package org.kalimbekov.entities;

import java.util.UUID;

abstract public class Entity {
    protected final UUID id;

    protected Entity(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }

    @Override
    public String toString() {
        return this.id.toString();
    }
}
