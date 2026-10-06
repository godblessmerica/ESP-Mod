package com.espmod.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.StringWidget;
import java.util.List;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public class ESPConfigScreen extends Screen {

    private final Screen parent;

    private static final int BOX_W  = 250;
    private static final int BOX_H  = 260;
    private static final int TITLE_H  = 22;
    private static final int FOOTER_H = 32;

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

        list.addEntry(new SettingsList.ButtonEntry("Entity ESP...",
            () -> this.minecraft.gui.setScreen(new EntityESPScreen(this))));

        list.addEntry(new SettingsList.ButtonEntry("Block ESP...",
            () -> this.minecraft.gui.setScreen(new BlockESPScreen(this))));

        list.addEntry(new SettingsList.HeaderEntry("Settings"));
        list.addEntry(new SettingsList.SliderEntry("Box Range", 8, 512,
            EntityESPConfig::getEffectiveHitboxRange,
            v -> EntityESPConfig.hitboxRange = v));
        list.addEntry(new SettingsList.ButtonEntry("Use Render Distance", () -> {
            EntityESPConfig.hitboxRange = 0;
            EntityESPConfig.save();
            this.minecraft.gui.setScreen(new ESPConfigScreen(parent));
        }));

        list.addEntry(new SettingsList.HeaderEntry("Controls"));
        list.addEntry(new SettingsList.KeyBindEntry("Open Menu",   ESPModClient.openScreenKey));
        list.addEntry(new SettingsList.KeyBindEntry("Toggle ESP",  ESPModClient.toggleKey));

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
                btn -> onClose())
            .bounds(boxX + BOX_W / 2 - 60, boxY + BOX_H - 26, 120, 20).build());

        addRenderableWidget(CycleButton.<Boolean>builder(
                val -> val ? Component.literal("ON").withStyle(s -> s.withColor(0x55FF55))
                           : Component.literal("OFF").withStyle(s -> s.withColor(0xFF5555)),
                EntityESPConfig.enabled || BlockESPConfig.enabled)
            .withValues(List.of(true, false)).displayOnlyValue()
            .create(boxX + BOX_W - 52, boxY + 3, 46, 16, Component.empty(),
                (b, val) -> {
                    EntityESPConfig.enabled = val;
                    BlockESPConfig.enabled  = val;
                    EntityESPConfig.save();
                    BlockESPConfig.save();
                }));
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (listeningFor != null) {
            InputConstants.Key newKey = InputConstants.getKey(event);
            if (newKey.getValue() != InputConstants.KEY_ESCAPE) {
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
    public void onClose() { listeningFor = null; this.minecraft.gui.setScreen(parent); }

    @Override public void removed() { listeningFor = null; EntityESPConfig.save(); }

    static class SettingsList extends ContainerObjectSelectionList<SettingsList.BaseEntry> {
        SettingsList(Minecraft mc, int w, int h, int y, int ih) { super(mc, w, h, y, ih); }
        @Override public int addEntry(BaseEntry e) { return super.addEntry(e); }
        @Override protected void extractListBackground(GuiGraphicsExtractor g) {}

        static class HeaderEntry extends BaseEntry {
            @Override public List<? extends AbstractWidget> children() { return List.of(widget); }
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
        }

        static class KeyBindEntry extends BaseEntry {
            @Override public List<? extends AbstractWidget> children() { return List.of(keyBtn); }
            private final KeyMapping mapping;
            private final StringWidget labelWidget;
            private final Button keyBtn;

            KeyBindEntry(String label, KeyMapping mapping) {
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

                Component keyLabel = listening
                    ? Component.literal("> Press key <").withStyle(s -> s.withColor(0xFFFF55))
                    : KeyMappingHelper.getBoundKeyOf(mapping).getDisplayName();
                keyBtn.setMessage(keyLabel);
                keyBtn.setX(cx + w - 91); keyBtn.setY(cy + 1);
                keyBtn.setWidth(85); keyBtn.setHeight(h - 2);
                keyBtn.extractRenderState(g, mx, my, delta);
            }

        }

        static class ButtonEntry extends BaseEntry {
            @Override public List<? extends AbstractWidget> children() { return List.of(btn); }
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
        }

        static class SliderEntry extends BaseEntry {
            @Override public List<? extends AbstractWidget> children() { return List.of(slider); }
            private final StringWidget labelWidget;
            private final AbstractSliderButton slider;

            SliderEntry(String label, int min, int max, IntSupplier getter, IntConsumer setter) {
                final int fMin = min, fMax = max;
                this.labelWidget = new StringWidget(Component.literal(label), Minecraft.getInstance().font);
                double initial = (double)(getter.getAsInt() - fMin) / (fMax - fMin);
                this.slider = new AbstractSliderButton(0, 0, 120, 20,
                        Component.literal(getter.getAsInt() + " blocks"), initial) {
                    @Override protected void updateMessage() {
                        setMessage(Component.literal(
                            (int) Math.round(fMin + value * (fMax - fMin)) + " blocks"));
                    }
                    @Override protected void applyValue() {
                        setter.accept((int) Math.round(fMin + value * (fMax - fMin)));
                    }
                };
            }

            @Override
            public void extractContent(GuiGraphicsExtractor g, int mx, int my, boolean hovered, float delta) {
                int cx = getContentX(), cy = getContentY(), w = getContentWidth(), h = getContentHeight();
                g.fill(cx, cy, cx + w, cy + h, hovered ? 0xFF3A3A3A : 0xFF111111);
                labelWidget.setX(cx + 6); labelWidget.setY(cy + (h - 8) / 2);
                labelWidget.setWidth(w - 134); labelWidget.setHeight(8);
                labelWidget.extractRenderState(g, mx, my, delta);
                slider.setX(cx + w - 128); slider.setY(cy + 1);
                slider.setWidth(122); slider.setHeight(h - 2);
                slider.extractRenderState(g, mx, my, delta);
            }

        }

        abstract static class BaseEntry extends ContainerObjectSelectionList.Entry<BaseEntry> {
            @Override public List<? extends AbstractWidget> children() { return List.of(); }
            @Override public List<? extends NarratableEntry> narratables() { return children(); }
            @Override public abstract void extractContent(GuiGraphicsExtractor g, int mx, int my, boolean hovered, float delta);
        }
    }
}
