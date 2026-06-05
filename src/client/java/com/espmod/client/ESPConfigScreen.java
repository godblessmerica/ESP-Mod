package com.espmod.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class ESPConfigScreen extends Screen {

    private final Screen parent;

    private static final int BOX_W  = 250;
    private static final int BOX_H  = 220;
    private static final int TITLE_H  = 22;
    private static final int FOOTER_H = 32;

    // Which keybind is currently waiting for a key press (null = not listening)
    static KeyMapping listeningFor = null;

    public ESPConfigScreen(Screen parent) {
        super(Minecraft.getInstance(), Minecraft.getInstance().font, Component.literal("ESP Mod"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        listeningFor = null;
        int boxX = (this.width  - BOX_W) / 2;
        int boxY = (this.height - BOX_H) / 2;

        SettingsList list = new SettingsList(this.minecraft,
            BOX_W, BOX_H - TITLE_H - FOOTER_H, boxY + TITLE_H, 22);
        list.setX(boxX);

        list.addEntry(new SettingsList.HeaderEntry("ESP Display"));
        list.addEntry(new SettingsList.ToggleEntry("Show Outline (glow)",
            () -> ESPConfig.showOutline, v -> ESPConfig.showOutline = v));
        list.addEntry(new SettingsList.ToggleEntry("Show Hitbox (box)",
            () -> ESPConfig.showHitbox, v -> ESPConfig.showHitbox = v));

        list.addEntry(new SettingsList.HeaderEntry("Entities"));
        list.addEntry(new SettingsList.ToggleEntry("Players",
            () -> ESPConfig.playerESP, v -> { ESPConfig.playerESP = v; ESPConfig.applyPlayerPreset(v); }));
        list.addEntry(new SettingsList.ToggleEntry("Mobs",
            () -> ESPConfig.mobESP, v -> { ESPConfig.mobESP = v; ESPConfig.applyMobPreset(v); }));
        list.addEntry(new SettingsList.ToggleEntry("Vehicles",
            () -> ESPConfig.vehicleESP, v -> { ESPConfig.vehicleESP = v; ESPConfig.applyVehiclePreset(v); }));
        list.addEntry(new SettingsList.ToggleEntry("Technical",
            () -> ESPConfig.technicalESP, v -> { ESPConfig.technicalESP = v; ESPConfig.applyTechnicalPreset(v); }));
        list.addEntry(new SettingsList.ToggleEntry("All Entities",
            () -> ESPConfig.allEntityESP, v -> { ESPConfig.allEntityESP = v; ESPConfig.applyAllEntitiesPreset(v); }));
        list.addEntry(new SettingsList.ButtonEntry("Advanced Entity Settings...",
            () -> this.minecraft.setScreen(new AdvancedESPScreen(this))));

        list.addEntry(new SettingsList.HeaderEntry("Controls"));
        list.addEntry(new SettingsList.KeyBindEntry("Toggle ESP",      PlayerESPClient.toggleKey));
        list.addEntry(new SettingsList.KeyBindEntry("Open Menu",       PlayerESPClient.openScreenKey));

        addRenderableWidget(list);

        int titleTextW = this.font.width("ESP Mod");
        var titleLabel = new StringWidget(
            Component.literal("ESP Mod").withStyle(s -> s.withColor(0xFFFFFF)), this.font);
        titleLabel.setX(boxX + (BOX_W - titleTextW) / 2);
        titleLabel.setY(boxY + (TITLE_H - 8) / 2);
        titleLabel.setWidth(titleTextW);
        titleLabel.setHeight(8);
        addRenderableWidget(titleLabel);

        addRenderableWidget(Button.builder(Component.literal("Done"),
                btn -> { ESPConfig.save(); listeningFor = null; this.minecraft.setScreen(parent); })
            .bounds(boxX + BOX_W / 2 - 60, boxY + BOX_H - 26, 120, 20).build());
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (listeningFor != null) {
            InputConstants.Key newKey = InputConstants.getKey(event);
            // Escape cancels rebinding
            if (newKey.getValue() != org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE) {
                listeningFor.setKey(newKey);
                KeyMapping.resetMapping();
                this.minecraft.options.save();
            }
            listeningFor = null;
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float delta) {
        int boxX = (this.width  - BOX_W) / 2;
        int boxY = (this.height - BOX_H) / 2;
        g.fill(boxX - 1, boxY - 1, boxX + BOX_W + 1, boxY + BOX_H + 1, 0xFF555555);
        g.fill(boxX, boxY, boxX + BOX_W, boxY + BOX_H, 0xF01A1A1A);
        g.fill(boxX, boxY, boxX + BOX_W, boxY + TITLE_H, 0xF0252525);
        super.extractRenderState(g, mx, my, delta);
    }

    @Override
    public void onClose() { ESPConfig.save(); listeningFor = null; this.minecraft.setScreen(parent); }

    // ─────────────────────────────────────────────────────────────────

    static class SettingsList extends AbstractSelectionList<SettingsList.BaseEntry> {
        SettingsList(Minecraft mc, int w, int h, int y, int ih) { super(mc, w, h, y, ih); }
        @Override public int addEntry(BaseEntry e) { return super.addEntry(e); }
        @Override protected void updateWidgetNarration(NarrationElementOutput o) {}
        @Override protected void extractListBackground(GuiGraphicsExtractor g) {}

        static class HeaderEntry extends BaseEntry {
            private final StringWidget widget;
            HeaderEntry(String t) {
                this.widget = new StringWidget(
                    Component.literal(t).withStyle(s -> s.withColor(0xAAAAAA)),
                    Minecraft.getInstance().font);
            }
            @Override
            public void extractContent(GuiGraphicsExtractor g, int mx, int my, boolean hovered, float delta) {
                int cx = getContentX(), cy = getContentY(), w = getContentWidth(), h = getContentHeight();
                widget.setX(cx + 6); widget.setY(cy + (h - 8) / 2);
                widget.setWidth(w - 12); widget.setHeight(h);
                widget.extractRenderState(g, mx, my, delta);
            }
            @Override public boolean mouseClicked(MouseButtonEvent e, boolean bl) { return false; }
        }

        static class ToggleEntry extends BaseEntry {
            private final StringWidget labelWidget;
            private final BooleanSupplier getter;
            private final Consumer<Boolean> setter;
            private final CycleButton<Boolean> btn;

            ToggleEntry(String label, BooleanSupplier getter, Consumer<Boolean> setter) {
                this.getter = getter; this.setter = setter;
                this.labelWidget = new StringWidget(Component.literal(label), Minecraft.getInstance().font);
                this.btn = CycleButton.<Boolean>builder(
                        val -> val ? Component.literal("Show").withStyle(s -> s.withColor(0x55FF55))
                                   : Component.literal("Hide").withStyle(s -> s.withColor(0xFF5555)),
                        getter.getAsBoolean())
                    .withValues(List.of(true, false)).displayOnlyValue()
                    .create(0, 0, 65, 20, Component.empty(),
                        (b, val) -> { setter.accept(val); ESPConfig.save(); });
            }

            @Override
            public void extractContent(GuiGraphicsExtractor g, int mx, int my, boolean hovered, float delta) {
                int cx = getContentX(), cy = getContentY(), w = getContentWidth(), h = getContentHeight();
                g.fill(cx, cy, cx + w, cy + h, hovered ? 0xFF3A3A3A : 0xFF111111);
                labelWidget.setX(cx + 6); labelWidget.setY(cy + (h - 8) / 2);
                labelWidget.setWidth(w - 77); labelWidget.setHeight(8);
                labelWidget.extractRenderState(g, mx, my, delta);
                btn.setValue(getter.getAsBoolean());
                btn.setX(cx + w - 71); btn.setY(cy + 1);
                btn.setWidth(65); btn.setHeight(h - 2);
                btn.extractRenderState(g, mx, my, delta);
            }
            @Override public boolean mouseClicked(MouseButtonEvent e, boolean bl) { return btn.mouseClicked(e, bl); }
        }

        // ── Keybind row ──────────────────────────────────────────────
        static class KeyBindEntry extends BaseEntry {
            private final String label;
            private final KeyMapping mapping;
            private final StringWidget labelWidget;
            private final Button keyBtn;

            KeyBindEntry(String label, KeyMapping mapping) {
                this.label   = label;
                this.mapping = mapping;
                this.labelWidget = new StringWidget(Component.literal(label), Minecraft.getInstance().font);
                this.keyBtn  = Button.builder(KeyMappingHelper.getBoundKeyOf(mapping).getDisplayName(), b -> {
                    listeningFor = mapping;
                }).bounds(0, 0, 85, 20).build();
            }

            @Override
            public void extractContent(GuiGraphicsExtractor g, int mx, int my, boolean hovered, float delta) {
                int cx = getContentX(), cy = getContentY(), w = getContentWidth(), h = getContentHeight();
                boolean listening = listeningFor == mapping;

                g.fill(cx, cy, cx + w, cy + h, hovered ? 0xFF3A3A3A : 0xFF111111);

                labelWidget.setX(cx + 6); labelWidget.setY(cy + (h - 8) / 2);
                labelWidget.setWidth(w - 91); labelWidget.setHeight(8);
                labelWidget.extractRenderState(g, mx, my, delta);

                // Update button label — show "> Press key <" when listening
                Component keyLabel = listening
                    ? Component.literal("> Press key <").withStyle(s -> s.withColor(0xFFFF55))
                    : KeyMappingHelper.getBoundKeyOf(mapping).getDisplayName();
                keyBtn.setMessage(keyLabel);
                keyBtn.setX(cx + w - 91); keyBtn.setY(cy + 1);
                keyBtn.setWidth(85); keyBtn.setHeight(h - 2);
                keyBtn.extractRenderState(g, mx, my, delta);
            }

            @Override
            public boolean mouseClicked(MouseButtonEvent e, boolean bl) {
                return keyBtn.mouseClicked(e, bl);
            }
        }

        static class ButtonEntry extends BaseEntry {
            private final Button btn;
            ButtonEntry(String label, Runnable action) {
                this.btn = Button.builder(Component.literal(label), b -> action.run())
                    .bounds(0, 0, 200, 20).build();
            }
            @Override
            public void extractContent(GuiGraphicsExtractor g, int mx, int my, boolean hovered, float delta) {
                int cx = getContentX(), cy = getContentY(), w = getContentWidth(), h = getContentHeight();
                btn.setX(cx + w / 2 - 100); btn.setY(cy + 1);
                btn.setWidth(200); btn.setHeight(h - 2);
                btn.extractRenderState(g, mx, my, delta);
            }
            @Override public boolean mouseClicked(MouseButtonEvent e, boolean bl) { return btn.mouseClicked(e, bl); }
        }

        abstract static class BaseEntry extends AbstractSelectionList.Entry<BaseEntry> {
            @Override public abstract void extractContent(GuiGraphicsExtractor g, int mx, int my, boolean hovered, float delta);
            @Override public abstract boolean mouseClicked(MouseButtonEvent e, boolean bl);
        }
    }
}
