package com.spectralesp.client;

public class EntitySettings {
    public boolean enabled;
    public boolean showOutline;
    public boolean showHitbox;

    public EntitySettings(boolean enabled, boolean showOutline, boolean showHitbox) {
        this.enabled     = enabled;
        this.showOutline = showOutline;
        this.showHitbox  = showHitbox;
    }

    // Returns a copy with global defaults applied
    public static EntitySettings fromGlobals() {
        return new EntitySettings(true, ESPConfig.showOutline, ESPConfig.showHitbox);
    }
}
