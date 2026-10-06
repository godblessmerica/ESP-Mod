package com.espmod.client;

public class EntityESPEntry {
    public String entityTypeId;
    public boolean enabled;
    public int color;

    public EntityESPEntry() {}

    public EntityESPEntry(String entityTypeId, boolean enabled, int color) {
        this.entityTypeId = entityTypeId;
        this.enabled = enabled;
        this.color = color;
    }
}
