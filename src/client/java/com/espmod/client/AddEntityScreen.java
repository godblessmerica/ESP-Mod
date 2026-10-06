package com.espmod.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class AddEntityScreen extends Screen {

    private final EntityESPScreen parent;
    private EntityPickerList list;
    private EditBox searchBox;
    private final List<EntityType<?>> allEntities = new ArrayList<>();

    private static final int BOX_W    = 260;
    private static final int BOX_H    = 220;
    private static final int TITLE_H  = 22;
    private static final int SEARCH_H = 22;
    private static final int FOOTER_H = 32;

    public AddEntityScreen(EntityESPScreen parent) {
        super(Minecraft.getInstance(), Minecraft.getInstance().font, Component.literal("Add Entity"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        if (allEntities.isEmpty()) {
            BuiltInRegistries.ENTITY_TYPE.forEach(allEntities::add);
            allEntities.sort(Comparator.comparing(t ->
                EntityESPScreen.EntityList.EntityRow.getDisplayName(EntityType.getKey(t).toString()).toLowerCase(Locale.ROOT)));
        }

        int boxX = (width  - BOX_W) / 2;
        int boxY = (height - BOX_H) / 2;

        var titleLabel = new StringWidget(
                Component.literal("Add Entity").withStyle(s -> s.withColor(0xFFFFFF)), font);
        int tw = font.width("Add Entity");
        titleLabel.setX(boxX + (BOX_W - tw) / 2);
        titleLabel.setY(boxY + (TITLE_H - 8) / 2);
        titleLabel.setWidth(tw);
        titleLabel.setHeight(8);
        addRenderableWidget(titleLabel);

        int searchY = boxY + TITLE_H + 2;
        int listY   = searchY + SEARCH_H;
        int listH   = BOX_H - TITLE_H - SEARCH_H - FOOTER_H;

        list = new EntityPickerList(minecraft, BOX_W, listH, listY, 22);
        list.setX(boxX);
        addRenderableWidget(list);

        searchBox = new EditBox(font, boxX + 4, searchY, BOX_W - 8, SEARCH_H - 4, Component.empty());
        searchBox.setMaxLength(64);
        searchBox.setHint(Component.literal("Search entities..."));
        searchBox.setResponder(this::rebuild);
        addRenderableWidget(searchBox);
        rebuild("");

        addRenderableWidget(Button.builder(Component.literal("Done"),
                btn -> { EntityESPConfig.save(); minecraft.gui.setScreen(new EntityESPScreen(parent.getParent())); })
            .bounds(boxX + BOX_W / 2 - 60, boxY + BOX_H - 26, 120, 20).build());
    }

    private void rebuild(String filter) {
        list.clearAll();
        String q = filter.toLowerCase(Locale.ROOT);
        for (EntityType<?> t : allEntities) {
            String id   = EntityType.getKey(t).toString();
            String name = EntityESPScreen.EntityList.EntityRow.getDisplayName(id).toLowerCase(Locale.ROOT);
            if (q.isEmpty() || name.contains(q) || id.contains(q))
                list.addEntry(new EntityPickerList.Row(t));
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float delta) {
        int bx = (width - BOX_W) / 2, by = (height - BOX_H) / 2;
        g.fill(bx - 1, by - 1, bx + BOX_W + 1, by + BOX_H + 1, 0xFF555555);
        g.fill(bx, by, bx + BOX_W, by + BOX_H, 0xF01A1A1A);
        g.fill(bx, by, bx + BOX_W, by + TITLE_H, 0xF0252525);
        super.extractRenderState(g, mx, my, delta);
    }

    @Override public boolean isPauseScreen() { return false; }
    @Override public void onClose() { minecraft.gui.setScreen(parent); }

    static class EntityPickerList extends ContainerObjectSelectionList<EntityPickerList.Row> {
        EntityPickerList(Minecraft mc, int w, int h, int y, int ih) { super(mc, w, h, y, ih); }
        @Override public int addEntry(Row e) { return super.addEntry(e); }
        public void clearAll() { clearEntries(); }
        @Override protected void extractListBackground(GuiGraphicsExtractor g) {}

        static class Row extends ContainerObjectSelectionList.Entry<Row> {
            @Override public List<? extends AbstractWidget> children() { return List.of(addBtn); }
            @Override public List<? extends NarratableEntry> narratables() { return children(); }
            private final EntityType<?> type;
            private final String entityTypeId;
            private final StringWidget nameWidget;
            private final Button addBtn;

            Row(EntityType<?> type) {
                this.type = type;
                this.entityTypeId = EntityType.getKey(type).toString();
                String name = EntityESPScreen.EntityList.EntityRow.getDisplayName(entityTypeId);
                this.nameWidget = new StringWidget(Component.literal(name), Minecraft.getInstance().font);
                this.addBtn = Button.builder(Component.literal("+ Add"), b -> {
                    if (!isTracked()) {
                        EntityESPConfig.addEntity(new EntityESPEntry(entityTypeId, true, 0xFFFF5555));
                        EntityESPConfig.save();
                    }
                }).bounds(0, 0, 52, 20).build();
            }

            private boolean isTracked() {
                return EntityESPConfig.getEntityLookup().containsKey(type);
            }

            @Override
            public void extractContent(GuiGraphicsExtractor g, int mx, int my, boolean hovered, float delta) {
                int cx = getContentX(), cy = getContentY(), w = getContentWidth(), h = getContentHeight();
                boolean tracked = isTracked();
                addBtn.active = !tracked;

                g.fill(cx, cy, cx + w, cy + h, hovered && !tracked ? 0xFF3A3A3A : 0xFF111111);

                addBtn.setMessage(tracked
                    ? Component.literal("✓ Added").withStyle(s -> s.withColor(0x55FF55))
                    : Component.literal("+ Add"));
                addBtn.setX(cx + w - 58); addBtn.setY(cy + 1);
                addBtn.setWidth(52); addBtn.setHeight(h - 2);
                addBtn.extractRenderState(g, mx, my, delta);

                nameWidget.setX(cx + 6); nameWidget.setY(cy + (h - 8) / 2);
                nameWidget.setWidth(w - 64); nameWidget.setHeight(8);
                nameWidget.extractRenderState(g, mx, my, delta);
            }

        }
    }
}
