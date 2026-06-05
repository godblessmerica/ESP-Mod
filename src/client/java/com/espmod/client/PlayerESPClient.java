package com.espmod.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;

public class PlayerESPClient implements ClientModInitializer {

    // Custom keybind category — shows as "ESP Mod" in Controls screen
    public static final KeyMapping.Category CATEGORY =
        KeyMapping.Category.register(Identifier.fromNamespaceAndPath("espmod", "esp_mod"));

    public static KeyMapping toggleKey;
    public static KeyMapping openScreenKey;

    @Override
    public void onInitializeClient() {
        ESPConfig.load();

        toggleKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.espmod.toggle",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            CATEGORY
        ));

        openScreenKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.espmod.open_screen",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_BACKSLASH,
            CATEGORY
        ));

        HitboxRenderer.register();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleKey.consumeClick()) {
                ESPConfig.enabled = !ESPConfig.enabled;
                if (client.player != null) {
                    client.player.sendOverlayMessage(
                        Component.literal("ESP " + (ESPConfig.enabled ? "§aON" : "§cOFF"))
                    );
                }
            }

            while (openScreenKey.consumeClick()) {
                if (client.screen == null) {
                    client.setScreen(new ESPConfigScreen(null));
                }
            }
        });
    }
}
