package com.espmod.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import com.mojang.blaze3d.platform.InputConstants;

public class ESPModClient implements ClientModInitializer {

    public static final KeyMapping.Category CATEGORY =
        KeyMapping.Category.register(Identifier.fromNamespaceAndPath("espmod", "esp_mod"));

    public static KeyMapping openScreenKey;
    public static KeyMapping toggleKey;

    @Override
    public void onInitializeClient() {
        EntityESPConfig.load();
        BlockESPConfig.load();

        openScreenKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.espmod.open_screen",
            InputConstants.Type.KEYBOARD,
            InputConstants.KEY_BACKSLASH,
            CATEGORY
        ));

        toggleKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.espmod.toggle",
            InputConstants.Type.KEYBOARD,
            InputConstants.UNKNOWN.getValue(),
            CATEGORY
        ));

        HitboxRenderer.register();
        BlockScanner.register();
        BlockESPRenderer.register();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (openScreenKey.consumeClick() && client.gui.screen() == null) {
                client.gui.setScreen(new ESPConfigScreen(null));
            }

            if (toggleKey.consumeClick() && client.gui.screen() == null) {
                boolean next = !(EntityESPConfig.enabled || BlockESPConfig.enabled);
                EntityESPConfig.enabled = next;
                BlockESPConfig.enabled  = next;
                EntityESPConfig.save();
                BlockESPConfig.save();
                if (client.player != null) {
                    client.player.sendOverlayMessage(Component.literal(
                        "ESP " + (next ? "§aON" : "§cOFF")));
                }
            }
        });
    }
}
