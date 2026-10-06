package com.espmod.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.util.function.Consumer;

public class ColorPickerScreen extends Screen {

    private final Screen parent;
    private final Consumer<Integer> onApply;

    private int r, g, b;
    private ColorSlider rSlider, gSlider, bSlider;
    private EditBox hexBox;
    private boolean syncing = false;

    private static final int BOX_W = 240;
    private static final int BOX_H = 188;

    public ColorPickerScreen(Screen parent, String label, int argb, Consumer<Integer> onApply) {
        super(Minecraft.getInstance(), Minecraft.getInstance().font, Component.literal("Color: " + label));
        this.parent  = parent;
        this.onApply = onApply;
        this.r = (argb >> 16) & 0xFF;
        this.g = (argb >>  8) & 0xFF;
        this.b =  argb        & 0xFF;
    }

    @Override
    protected void init() {
        int bx = (width - BOX_W) / 2;
        int by = (height - BOX_H) / 2;
        int cx = bx + 12;
        int cw = BOX_W - 24;
        int y  = by + 30;

        rSlider = new ColorSlider(cx, y, cw, "R", r / 255.0, v -> { r = v; syncHex(); }); addRenderableWidget(rSlider); y += 26;
        gSlider = new ColorSlider(cx, y, cw, "G", g / 255.0, v -> { g = v; syncHex(); }); addRenderableWidget(gSlider); y += 26;
        bSlider = new ColorSlider(cx, y, cw, "B", b / 255.0, v -> { b = v; syncHex(); }); addRenderableWidget(bSlider); y += 30;

        hexBox = new EditBox(font, cx, y, cw, 18, Component.empty());
        hexBox.setMaxLength(7);
        hexBox.setValue(toHex());
        hexBox.setResponder(this::onHexInput);
        addRenderableWidget(hexBox);
        y += 26;

        int half = (cw - 4) / 2;
        addRenderableWidget(Button.builder(Component.literal("Cancel"),
                btn -> minecraft.gui.setScreen(parent))
            .bounds(cx, y, half, 20).build());
        addRenderableWidget(Button.builder(Component.literal("OK"),
                btn -> { onApply.accept(toArgb()); minecraft.gui.setScreen(parent); })
            .bounds(cx + half + 4, y, half, 20).build());
    }

    private String toHex()  { return String.format("#%02X%02X%02X", r, g, b); }
    private int    toArgb() { return 0xFF000000 | (r << 16) | (g << 8) | b; }

    private void syncHex() {
        if (!syncing && hexBox != null) {
            syncing = true;
            hexBox.setValue(toHex());
            syncing = false;
        }
    }

    private void onHexInput(String s) {
        if (syncing || s.length() != 7 || !s.startsWith("#")) return;
        try {
            int v = Integer.parseInt(s.substring(1), 16);
            r = (v >> 16) & 0xFF;
            g = (v >>  8) & 0xFF;
            b =  v        & 0xFF;
            syncing = true;
            if (rSlider != null) rSlider.updateExternalValue(r / 255.0);
            if (gSlider != null) gSlider.updateExternalValue(g / 255.0);
            if (bSlider != null) bSlider.updateExternalValue(b / 255.0);
            syncing = false;
        } catch (NumberFormatException ignored) {}
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float delta) {
        int bx = (width - BOX_W) / 2, by = (height - BOX_H) / 2;
        g.fill(bx - 1, by - 1, bx + BOX_W + 1, by + BOX_H + 1, 0xFF555555);
        g.fill(bx, by, bx + BOX_W, by + BOX_H, 0xF01A1A1A);
        g.fill(bx, by, bx + BOX_W, by + 22, 0xF0252525);
        g.fill(bx + BOX_W - 38, by + 3, bx + BOX_W - 6, by + 19, toArgb()); // live preview
        super.extractRenderState(g, mx, my, delta);
    }

    @Override public boolean isPauseScreen() { return false; }
    @Override public void onClose() { minecraft.gui.setScreen(parent); }

    static class ColorSlider extends AbstractSliderButton {
        private final String channel;
        private final Consumer<Integer> onChange;

        ColorSlider(int x, int y, int w, String channel, double initial, Consumer<Integer> onChange) {
            super(x, y, w, 18, Component.empty(), initial);
            this.channel  = channel;
            this.onChange = onChange;
            updateMessage();
        }

        @Override protected void updateMessage() {
            setMessage(Component.literal(channel + ": " + (int) Math.round(value * 255)));
        }

        @Override protected void applyValue() {
            onChange.accept((int) Math.round(value * 255));
        }

        void updateExternalValue(double v) {
            this.value = Math.max(0, Math.min(1, v));
            updateMessage();
        }
    }
}
