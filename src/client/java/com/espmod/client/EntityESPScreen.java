package com.espmod.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import java.util.List;
import java.util.Optional;

public class EntityESPScreen extends Screen {

    private final Screen parent;

    private static final int BOX_W    = 280;
    private static final int BOX_H    = 260;
    private static final int TITLE_H  = 22;
    private static final int FOOTER_H = 32;

    public EntityESPScreen(Screen parent) {
        super(Minecraft.getInstance(), Minecraft.getInstance().font, Component.literal("Entity ESP"));
        this.parent = parent;
    }

    public Screen getParent() { return parent; }

    @Override
    protected void init() {
        int boxX = (width  - BOX_W) / 2;
        int boxY = (height - BOX_H) / 2;

        EntityList list = new EntityList(minecraft,
                BOX_W, BOX_H - TITLE_H - FOOTER_H, boxY + TITLE_H, 22);
        list.setX(boxX);

        // Settings at the top
        list.addEntry(new EntityList.Header("Settings"));
        list.addEntry(new EntityList.ToggleEntry("Show Outline (glow)",
                () -> EntityESPConfig.showOutline, v -> { EntityESPConfig.showOutline = v; EntityESPConfig.save(); }));
        list.addEntry(new EntityList.ToggleEntry("Show Hitbox (box)",
                () -> EntityESPConfig.showHitbox,  v -> { EntityESPConfig.showHitbox  = v; EntityESPConfig.save(); }));

        // Tracked entities
        list.addEntry(new EntityList.Header("Tracked Entities"));
        if (EntityESPConfig.entities.isEmpty()) {
            list.addEntry(new EntityList.EmptyEntry());
        } else {
            for (EntityESPEntry entry : EntityESPConfig.entities) {
                list.addEntry(new EntityList.EntityRow(entry, this));
            }
        }

        // Presets at the bottom
        list.addEntry(new EntityList.Header("Presets"));
        list.addEntry(new EntityList.ButtonEntry("Clear All", () -> {
            EntityESPConfig.entities.clear();
            EntityESPConfig.rebuildLookup();
            EntityESPConfig.save();
            minecraft.gui.setScreen(new EntityESPScreen(parent));
        }));
        list.addEntry(new EntityList.ButtonEntry("+ Players",     () -> { EntityESPConfig.applyPreset("player");     minecraft.gui.setScreen(new EntityESPScreen(parent)); }));
        list.addEntry(new EntityList.ButtonEntry("+ Mobs",        () -> { EntityESPConfig.applyPreset("mob");        minecraft.gui.setScreen(new EntityESPScreen(parent)); }));
        list.addEntry(new EntityList.ButtonEntry("+ Vehicles",    () -> { EntityESPConfig.applyPreset("vehicle");    minecraft.gui.setScreen(new EntityESPScreen(parent)); }));
        list.addEntry(new EntityList.ButtonEntry("+ Decorative",  () -> { EntityESPConfig.applyPreset("decorative"); minecraft.gui.setScreen(new EntityESPScreen(parent)); }));
        list.addEntry(new EntityList.ButtonEntry("+ Projectiles", () -> { EntityESPConfig.applyPreset("projectile"); minecraft.gui.setScreen(new EntityESPScreen(parent)); }));
        list.addEntry(new EntityList.ButtonEntry("+ Items & Drops", () -> { EntityESPConfig.applyPreset("items");   minecraft.gui.setScreen(new EntityESPScreen(parent)); }));
        list.addEntry(new EntityList.ButtonEntry("+ Mods",        () -> { EntityESPConfig.applyPreset("mods");      minecraft.gui.setScreen(new EntityESPScreen(parent)); }));
        list.addEntry(new EntityList.ButtonEntry("+ Other",       () -> { EntityESPConfig.applyPreset("other");     minecraft.gui.setScreen(new EntityESPScreen(parent)); }));

        addRenderableWidget(list);

        int footerY = boxY + BOX_H - 26;
        addRenderableWidget(Button.builder(Component.literal("+ Add Entity"),
                btn -> minecraft.gui.setScreen(new AddEntityScreen(this)))
            .bounds(boxX + 6, footerY, 120, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Done"),
                btn -> { EntityESPConfig.save(); minecraft.gui.setScreen(parent); })
            .bounds(boxX + BOX_W - 126, footerY, 120, 20).build());

        var titleLabel = new StringWidget(
                Component.literal("Entity ESP").withStyle(s -> s.withColor(0xFFFFFF)), font);
        titleLabel.setX(boxX + 8);
        titleLabel.setY(boxY + (TITLE_H - 8) / 2);
        titleLabel.setWidth(80);
        titleLabel.setHeight(8);
        addRenderableWidget(titleLabel);

    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float delta) {
        int bx = (width  - BOX_W) / 2, by = (height - BOX_H) / 2;
        g.fill(bx - 1, by - 1, bx + BOX_W + 1, by + BOX_H + 1, 0xFF555555);
        g.fill(bx, by, bx + BOX_W, by + BOX_H, 0xF01A1A1A);
        g.fill(bx, by, bx + BOX_W, by + TITLE_H, 0xF0252525);
        super.extractRenderState(g, mx, my, delta);
    }

    @Override
    public void onClose() { EntityESPConfig.save(); minecraft.gui.setScreen(parent); }

    @Override public boolean isPauseScreen() { return false; }

    static class EntityList extends ContainerObjectSelectionList<EntityList.BaseEntry> {
        EntityList(Minecraft mc, int w, int h, int y, int ih) { super(mc, w, h, y, ih); }
        @Override public int addEntry(BaseEntry e) { return super.addEntry(e); }
        @Override protected void extractListBackground(GuiGraphicsExtractor g) {}

        static class Header extends BaseEntry {
            @Override public List<? extends AbstractWidget> children() { return List.of(widget); }
            private final StringWidget widget;
            Header(String t) {
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

        static class EmptyEntry extends BaseEntry {
            @Override
            public void extractContent(GuiGraphicsExtractor g, int mx, int my, boolean hovered, float delta) {
                int cx = getContentX(), cy = getContentY(), w = getContentWidth(), h = getContentHeight();
                var font = Minecraft.getInstance().font;
                var msg = Component.literal("No entities tracked yet").withStyle(s -> s.withColor(0x888888));
                int tw = font.width(msg);
                var lbl = new StringWidget(msg, font);
                lbl.setX(cx + (w - tw) / 2); lbl.setY(cy + (h - 8) / 2);
                lbl.setWidth(tw); lbl.setHeight(8);
                lbl.extractRenderState(g, mx, my, delta);
            }
        }

        static class ToggleEntry extends BaseEntry {
            @Override public List<? extends AbstractWidget> children() { return List.of(btn); }
            private final StringWidget labelWidget;
            private final java.util.function.BooleanSupplier getter;
            private final CycleButton<Boolean> btn;

            ToggleEntry(String label, java.util.function.BooleanSupplier getter,
                        java.util.function.Consumer<Boolean> setter) {
                this.getter = getter;
                this.labelWidget = new StringWidget(Component.literal(label), Minecraft.getInstance().font);
                this.btn = CycleButton.<Boolean>builder(
                        val -> val ? Component.literal("Show").withStyle(s -> s.withColor(0x55FF55))
                                   : Component.literal("Hide").withStyle(s -> s.withColor(0xFF5555)),
                        getter.getAsBoolean())
                    .withValues(List.of(true, false)).displayOnlyValue()
                    .create(0, 0, 65, 20, Component.empty(), (b, val) -> setter.accept(val));
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
        }

        static class EntityRow extends BaseEntry {
            @Override public List<? extends AbstractWidget> children() { return List.of(toggleBtn, swatchBtn, removeBtn); }
            private final EntityESPEntry entry;
            private final StringWidget   labelWidget;
            private final CycleButton<Boolean> toggleBtn;
            private final Button swatchBtn;
            private final Button removeBtn;

            EntityRow(EntityESPEntry entry, EntityESPScreen screen) {
                this.entry = entry;
                Screen outerParent = screen.parent;

                String displayName = getDisplayName(entry.entityTypeId);
                this.labelWidget = new StringWidget(Component.literal(displayName), Minecraft.getInstance().font);

                this.toggleBtn = CycleButton.<Boolean>builder(
                        val -> val ? Component.literal("Show").withStyle(s -> s.withColor(0x55FF55))
                                   : Component.literal("Hide").withStyle(s -> s.withColor(0xFF5555)),
                        entry.enabled)
                    .withValues(List.of(true, false)).displayOnlyValue()
                    .create(0, 0, 52, 20, Component.empty(),
                        (b, val) -> { entry.enabled = val; EntityESPConfig.rebuildLookup(); EntityESPConfig.save(); });

                this.swatchBtn = Button.builder(Component.empty(), b -> {
                    Minecraft mc = Minecraft.getInstance();
                    mc.gui.setScreen(new ColorPickerScreen(
                        mc.gui.screen(), displayName, entry.color,
                        newColor -> { entry.color = newColor; EntityESPConfig.save(); }
                    ));
                }).bounds(0, 0, 20, 18).build();

                this.removeBtn = Button.builder(Component.literal("×"), b -> {
                    EntityESPConfig.removeEntity(entry.entityTypeId);
                    EntityESPConfig.save();
                    Minecraft.getInstance().gui.setScreen(new EntityESPScreen(outerParent));
                }).bounds(0, 0, 16, 18).build();
            }

            static String getDisplayName(String entityTypeId) {
                try {
                    Optional<EntityType<?>> opt = BuiltInRegistries.ENTITY_TYPE
                        .getOptional(Identifier.parse(entityTypeId));
                    if (opt.isEmpty()) return entityTypeId;
                    String raw = opt.get().getDescription().getString();
                    if (!raw.isBlank()) return raw;
                    String path = entityTypeId.contains(":") ? entityTypeId.substring(entityTypeId.indexOf(':') + 1) : entityTypeId;
                    return formatPath(path);
                } catch (Exception e) { return entityTypeId; }
            }

            private static String formatPath(String path) {
                String[] words = path.replace('_', ' ').split(" ");
                StringBuilder sb = new StringBuilder();
                for (String w : words) {
                    if (!w.isEmpty()) {
                        if (sb.length() > 0) sb.append(' ');
                        sb.append(Character.toUpperCase(w.charAt(0)));
                        sb.append(w.substring(1));
                    }
                }
                return sb.toString();
            }

            @Override
            public void extractContent(GuiGraphicsExtractor g, int mx, int my, boolean hovered, float delta) {
                int cx = getContentX(), cy = getContentY(), w = getContentWidth(), h = getContentHeight();
                g.fill(cx, cy, cx + w, cy + h, hovered ? 0xFF3A3A3A : 0xFF111111);

                int rx = cx + w - 6 - 16;
                removeBtn.setX(rx); removeBtn.setY(cy + 1);
                removeBtn.setWidth(16); removeBtn.setHeight(h - 2);
                removeBtn.extractRenderState(g, mx, my, delta);

                int tx = rx - 4 - 52;
                toggleBtn.setValue(entry.enabled);
                toggleBtn.setX(tx); toggleBtn.setY(cy + 1);
                toggleBtn.setWidth(52); toggleBtn.setHeight(h - 2);
                toggleBtn.extractRenderState(g, mx, my, delta);

                int sx = tx - 4 - 20, sy = cy + 1, sh = h - 2;
                swatchBtn.setX(sx); swatchBtn.setY(sy);
                swatchBtn.setWidth(20); swatchBtn.setHeight(sh);
                swatchBtn.extractRenderState(g, mx, my, delta);
                g.fill(sx + 1, sy + 1, sx + 19, sy + sh - 1, entry.color);

                labelWidget.setX(cx + 6); labelWidget.setY(cy + (h - 8) / 2);
                labelWidget.setWidth(sx - cx - 10); labelWidget.setHeight(8);
                labelWidget.extractRenderState(g, mx, my, delta);
            }

        }

        abstract static class BaseEntry extends ContainerObjectSelectionList.Entry<BaseEntry> {
            @Override public List<? extends AbstractWidget> children() { return List.of(); }
            @Override public List<? extends NarratableEntry> narratables() { return children(); }
            @Override public abstract void extractContent(GuiGraphicsExtractor g, int mx, int my, boolean hovered, float delta);
        }
    }
}
