package com.espmod.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.PlainTextButton;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AdvancedESPScreen extends Screen {

    private final Screen parent;
    private EntityList list;
    private EditBox searchBox;
    private final List<EntityType<?>> allTypes = new ArrayList<>();

    private static final int BOX_W   = 250;
    private static final int BOX_H   = 220;
    private static final int TITLE_H = 22;
    private static final int SEARCH_H = 22;
    private static final int FOOTER_H = 32;

    enum Cat {
        PLAYERS    ("Players"),
        MOBS       ("Mobs"),
        VEHICLES   ("Vehicles  (boats, minecarts)"),
        DECORATIVE ("Decorative  (armor stands, frames)"),
        PROJECTILES("Projectiles"),
        ITEMS      ("Items & Drops"),
        TECHNICAL  ("Technical"),
        OTHER      ("Other");
        final String label;
        Cat(String l) { this.label = l; }
    }

    private static Cat categorize(EntityType<?> type) {
        if (type == EntityType.PLAYER) return Cat.PLAYERS;
        MobCategory mc = type.getCategory();
        if (mc != MobCategory.MISC) return Cat.MOBS;
        String p = EntityType.getKey(type).toString();
        if (p.contains("area_effect_cloud") || p.contains("marker")   ||
            p.contains("interaction")       || p.contains("_display") ||
            p.contains("lightning")         || p.contains("falling_block") ||
            p.contains("end_crystal")       || p.contains("end_gateway"))
            return Cat.TECHNICAL;
        if (p.contains("boat") || p.contains("raft") || p.contains("minecart"))
            return Cat.VEHICLES;
        if (p.contains("armor_stand") || p.contains("item_frame") ||
            p.contains("painting")    || p.contains("lead_knot"))
            return Cat.DECORATIVE;
        if (p.contains("arrow")        || p.contains("fireball")      ||
            p.contains("snowball")     || p.contains("egg")           ||
            p.contains("ender_pearl")  || p.contains("trident")       ||
            p.contains("potion")       || p.contains("firework")      ||
            p.contains("fishing")      || p.contains("shulker_bullet")||
            p.contains("wind_charge")  || p.contains("wither_skull"))
            return Cat.PROJECTILES;
        if (p.contains("item") || p.contains("experience") || p.contains("xp_orb"))
            return Cat.ITEMS;
        return Cat.OTHER;
    }

    public AdvancedESPScreen(Screen parent) {
        super(Minecraft.getInstance(), Minecraft.getInstance().font,
            Component.literal("Advanced Entity Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        if (allTypes.isEmpty()) {
            for (EntityType<?> t : BuiltInRegistries.ENTITY_TYPE) allTypes.add(t);
            allTypes.sort(Comparator.comparing(t -> t.getDescription().getString().toLowerCase()));
        }

        int boxX = (this.width  - BOX_W) / 2;
        int boxY = (this.height - BOX_H) / 2;

        // Title
        int titleTextW = this.font.width("Advanced Entity Settings");
        var titleLabel = new StringWidget(
            Component.literal("Advanced Entity Settings").withStyle(s -> s.withColor(0xFFFFFF)),
            this.font);
        titleLabel.setX(boxX + (BOX_W - titleTextW) / 2);
        titleLabel.setY(boxY + (TITLE_H - 8) / 2);
        titleLabel.setWidth(titleTextW);
        titleLabel.setHeight(8);
        addRenderableWidget(titleLabel);

        int searchY = boxY + TITLE_H + 2;
        int listY = searchY + SEARCH_H;
        int listH = BOX_H - TITLE_H - SEARCH_H - FOOTER_H;

        // Add list FIRST so it renders before the EditBox sets any clip state
        list = new EntityList(this.minecraft, BOX_W, listH, listY, 22);
        list.setX(boxX);
        addRenderableWidget(list);

        // Search box added AFTER list
        searchBox = new EditBox(this.font,
            boxX + 4, searchY, BOX_W - 8, SEARCH_H - 4,
            Component.literal("Search..."));
        searchBox.setMaxLength(50);
        searchBox.setHint(Component.literal("Search..."));
        searchBox.setResponder(this::rebuild);
        addRenderableWidget(searchBox);
        rebuild("");

        // Done
        addRenderableWidget(Button.builder(Component.literal("Done"),
                btn -> { ESPConfig.save(); this.minecraft.setScreen(parent); })
            .bounds(boxX + BOX_W / 2 - 60, boxY + BOX_H - 26, 120, 20).build());
    }

    private void rebuild(String filter) {
        list.clearAll();
        String q = filter.toLowerCase();
        Map<Cat, List<EntityType<?>>> groups = new LinkedHashMap<>();
        for (Cat c : Cat.values()) groups.put(c, new ArrayList<>());
        for (EntityType<?> t : allTypes) {
            String name = t.getDescription().getString().toLowerCase();
            String id   = EntityType.getKey(t).toString().toLowerCase();
            if (q.isEmpty() || name.contains(q) || id.contains(q))
                groups.get(categorize(t)).add(t);
        }
        for (Map.Entry<Cat, List<EntityType<?>>> entry : groups.entrySet()) {
            if (entry.getValue().isEmpty()) continue;
            list.addEntry(new EntityList.Header(entry.getKey().label));
            for (EntityType<?> t : entry.getValue())
                list.addEntry(new EntityList.Row(t));
        }
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
    public void onClose() { ESPConfig.save(); this.minecraft.setScreen(parent); }

    // ─────────────────────────────────────────────────────────────────

    static class EntityList extends AbstractSelectionList<EntityList.BaseEntry> {
        EntityList(Minecraft mc, int w, int h, int y, int ih) { super(mc, w, h, y, ih); }
        @Override public int addEntry(BaseEntry e) { return super.addEntry(e); }
        public void clearAll() { clearEntries(); }
        @Override protected void updateWidgetNarration(NarrationElementOutput o) {}
        @Override protected void extractListBackground(GuiGraphicsExtractor g) {}

        static class Header extends BaseEntry {
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
            @Override public boolean mouseClicked(MouseButtonEvent e, boolean bl) { return false; }
        }

        static class Row extends BaseEntry {
            private final EntityType<?> type;
            private final String typeId;
            private final String name;
            private final StringWidget nameWidget;
            private final CycleButton<Boolean> btn;

            Row(EntityType<?> type) {
                this.type   = type;
                this.typeId = EntityType.getKey(type).toString();
                String rawName = type.getDescription().getString();
                if (rawName.isBlank()) {
                    String id = EntityType.getKey(type).toString();
                    String path = id.contains(":") ? id.substring(id.indexOf(':') + 1) : id;
                    String[] words = path.replace('_', ' ').split(" ");
                    StringBuilder sb = new StringBuilder();
                    for (String w : words) {
                        if (!w.isEmpty()) {
                            if (sb.length() > 0) sb.append(' ');
                            sb.append(Character.toUpperCase(w.charAt(0)));
                            sb.append(w.substring(1));
                        }
                    }
                    this.name = sb.toString();
                } else {
                    this.name = rawName;
                }
                this.nameWidget = new StringWidget(Component.literal(this.name), Minecraft.getInstance().font);
                this.btn = CycleButton.<Boolean>builder(
                        val -> val ? Component.literal("Show").withStyle(s -> s.withColor(0x55FF55))
                                   : Component.literal("Hide").withStyle(s -> s.withColor(0xFF5555)),
                        effectiveEnabled())
                    .withValues(List.of(true, false)).displayOnlyValue()
                    .create(0, 0, 65, 20, Component.empty(), (b, val) -> {
                        EntitySettings ov = ESPConfig.entityOverrides.get(typeId);
                        if (ov != null) ov.enabled = val;
                        else ESPConfig.entityOverrides.put(typeId,
                            new EntitySettings(val, ESPConfig.showOutline, ESPConfig.showHitbox));
                        ESPConfig.save();
                    });
            }

            private boolean effectiveEnabled() {
                EntitySettings ov = ESPConfig.entityOverrides.get(typeId);
                return ov != null && ov.enabled;
            }

            @Override
            public void extractContent(GuiGraphicsExtractor g, int mx, int my, boolean hovered, float delta) {
                int cx = getContentX(), cy = getContentY(), w = getContentWidth(), h = getContentHeight();
                var font = Minecraft.getInstance().font;

                g.fill(cx, cy, cx + w, cy + h, hovered ? 0xFF3A3A3A : 0xFF111111);

                nameWidget.setX(cx + 6);
                nameWidget.setY(cy + (h - 8) / 2);
                nameWidget.setWidth(w - 77);
                nameWidget.setHeight(8);
                nameWidget.extractRenderState(g, mx, my, delta);

                btn.setValue(effectiveEnabled());
                btn.setX(cx + w - 71); btn.setY(cy + 1);
                btn.setWidth(65); btn.setHeight(h - 2);
                btn.extractRenderState(g, mx, my, delta);
            }

            @Override
            public boolean mouseClicked(MouseButtonEvent e, boolean bl) { return btn.mouseClicked(e, bl); }
        }

        abstract static class BaseEntry extends AbstractSelectionList.Entry<BaseEntry> {
            @Override public abstract void extractContent(GuiGraphicsExtractor g, int mx, int my, boolean hovered, float delta);
            @Override public abstract boolean mouseClicked(MouseButtonEvent e, boolean bl);
        }
    }
}
