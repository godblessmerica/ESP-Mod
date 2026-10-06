package com.espmod.client;

public class BlockESPEntry {
    public String blockId;
    public boolean enabled;
    public int color;

    public BlockESPEntry() {}

    public BlockESPEntry(String blockId, boolean enabled, int color) {
        this.blockId = blockId;
        this.enabled = enabled;
        this.color   = color;
    }
}
