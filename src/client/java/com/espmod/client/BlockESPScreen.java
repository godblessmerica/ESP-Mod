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
import net.minecraft.world.level.block.Block;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class BlockESPScreen extends Screen {

    private final Screen parent;

    private static final int BOX_W   = 280;
    private static final int BOX_H   = 260;
    private static final int TITLE_H  = 22;
    private static final int FOOTER_H = 32;

    // Per-ore colors matching vanilla ore textures
    private static final Map<String, Integer> ORE_PRESET = new LinkedHashMap<>();
    static {
        int coal      = 0xFF888888; // gray
        int iron      = 0xFFD4A574; // tan/beige
        int copper    = 0xFFFF8800; // orange
        int gold      = 0xFFFFD700; // gold yellow
        int lapis     = 0xFF2255EE; // blue
        int redstone  = 0xFFFF2200; // red
        int diamond   = 0xFF00FFFF; // cyan
        int emerald   = 0xFF00FF44; // bright green
        int quartz    = 0xFFEEEEEE; // near white
        int debris    = 0xFFCC4400; // dark orange/rust
        int amethyst  = 0xFFCC44FF; // purple

        ORE_PRESET.put("minecraft:coal_ore",             coal);
        ORE_PRESET.put("minecraft:deepslate_coal_ore",   coal);
        ORE_PRESET.put("minecraft:iron_ore",             iron);
        ORE_PRESET.put("minecraft:deepslate_iron_ore",   iron);
        ORE_PRESET.put("minecraft:copper_ore",           copper);
        ORE_PRESET.put("minecraft:deepslate_copper_ore", copper);
        ORE_PRESET.put("minecraft:gold_ore",             gold);
        ORE_PRESET.put("minecraft:deepslate_gold_ore",   gold);
        ORE_PRESET.put("minecraft:nether_gold_ore",      gold);
        ORE_PRESET.put("minecraft:lapis_ore",            lapis);
        ORE_PRESET.put("minecraft:deepslate_lapis_ore",  lapis);
        ORE_PRESET.put("minecraft:redstone_ore",         redstone);
        ORE_PRESET.put("minecraft:deepslate_redstone_ore", redstone);
        ORE_PRESET.put("minecraft:diamond_ore",          diamond);
        ORE_PRESET.put("minecraft:deepslate_diamond_ore",diamond);
        ORE_PRESET.put("minecraft:emerald_ore",          emerald);
        ORE_PRESET.put("minecraft:deepslate_emerald_ore",emerald);
        ORE_PRESET.put("minecraft:nether_quartz_ore",    quartz);
        ORE_PRESET.put("minecraft:ancient_debris",       debris);
        ORE_PRESET.put("minecraft:small_amethyst_bud",   amethyst);
        ORE_PRESET.put("minecraft:medium_amethyst_bud",  amethyst);
        ORE_PRESET.put("minecraft:large_amethyst_bud",   amethyst);
        ORE_PRESET.put("minecraft:amethyst_cluster",     amethyst);
        ORE_PRESET.put("minecraft:budding_amethyst",     amethyst);
    }

    private static final Map<String, Integer> CONTAINER_PRESET = new LinkedHashMap<>();
    static {
        int color = 0xFFFFAA00;
        CONTAINER_PRESET.put("minecraft:chest",         color);
        CONTAINER_PRESET.put("minecraft:trapped_chest", color);
        CONTAINER_PRESET.put("minecraft:ender_chest",   color);
        CONTAINER_PRESET.put("minecraft:barrel",        color);
        CONTAINER_PRESET.put("minecraft:hopper",        color);
        CONTAINER_PRESET.put("minecraft:dropper",       color);
        CONTAINER_PRESET.put("minecraft:dispenser",     color);
        CONTAINER_PRESET.put("minecraft:shulker_box",             color);
        CONTAINER_PRESET.put("minecraft:white_shulker_box",      color);
        CONTAINER_PRESET.put("minecraft:orange_shulker_box",     color);
        CONTAINER_PRESET.put("minecraft:magenta_shulker_box",    color);
        CONTAINER_PRESET.put("minecraft:light_blue_shulker_box", color);
        CONTAINER_PRESET.put("minecraft:yellow_shulker_box",     color);
        CONTAINER_PRESET.put("minecraft:lime_shulker_box",       color);
        CONTAINER_PRESET.put("minecraft:pink_shulker_box",       color);
        CONTAINER_PRESET.put("minecraft:gray_shulker_box",       color);
        CONTAINER_PRESET.put("minecraft:light_gray_shulker_box", color);
        CONTAINER_PRESET.put("minecraft:cyan_shulker_box",       color);
        CONTAINER_PRESET.put("minecraft:purple_shulker_box",     color);
        CONTAINER_PRESET.put("minecraft:blue_shulker_box",       color);
        CONTAINER_PRESET.put("minecraft:brown_shulker_box",      color);
        CONTAINER_PRESET.put("minecraft:green_shulker_box",      color);
        CONTAINER_PRESET.put("minecraft:red_shulker_box",        color);
        CONTAINER_PRESET.put("minecraft:black_shulker_box",      color);
        CONTAINER_PRESET.put("minecraft:furnace",       color);
        CONTAINER_PRESET.put("minecraft:blast_furnace", color);
        CONTAINER_PRESET.put("minecraft:smoker",        color);
    }

    private static final Map<String, Integer> VALUABLE_PRESET = new LinkedHashMap<>();
    static {
        int color = 0xFFFF55FF;
        VALUABLE_PRESET.put("minecraft:spawner",          color);
        VALUABLE_PRESET.put("minecraft:trial_spawner",    color);
        VALUABLE_PRESET.put("minecraft:beacon",           color);
        VALUABLE_PRESET.put("minecraft:end_portal_frame", color);
    }

    public BlockESPScreen(Screen parent) {
        super(Minecraft.getInstance(), Minecraft.getInstance().font, Component.literal("Block ESP"));
        this.parent = parent;
    }

    public Screen getParent() { return parent; }

    @Override
    protected void init() {
        int boxX = (width  - BOX_W) / 2;
        int boxY = (height - BOX_H) / 2;

        BlockList list = new BlockList(minecraft,
                BOX_W, BOX_H - TITLE_H - FOOTER_H, boxY + TITLE_H, 22);
        list.setX(boxX);

        list.addEntry(new BlockList.Header("Tracked Blocks"));
        if (BlockESPConfig.blocks.isEmpty()) {
            list.addEntry(new BlockList.EmptyEntry());
        } else {
            for (BlockESPEntry entry : BlockESPConfig.blocks) {
                list.addEntry(new BlockList.BlockRow(entry, this));
            }
        }

        list.addEntry(new BlockList.NoteEntry("Scans loaded chunks gradually.", 0xFFAAAAAA));

        list.addEntry(new BlockList.Header("Presets"));
        list.addEntry(new BlockList.ButtonEntry("Clear All", () -> {
            BlockESPConfig.blocks.clear();
            BlockESPConfig.rebuildLookup();
            BlockESPConfig.save();
            BlockScanner.rescanAll();
            minecraft.gui.setScreen(new BlockESPScreen(parent));
        }));
        list.addEntry(new BlockList.ButtonEntry("+ Ores", () -> {
            applyPreset(ORE_PRESET);
            minecraft.gui.setScreen(new BlockESPScreen(parent));
        }));
        list.addEntry(new BlockList.ButtonEntry("+ Containers", () -> {
            applyPreset(CONTAINER_PRESET);
            minecraft.gui.setScreen(new BlockESPScreen(parent));
        }));
        list.addEntry(new BlockList.ButtonEntry("+ Valuables", () -> {
            applyPreset(VALUABLE_PRESET);
            minecraft.gui.setScreen(new BlockESPScreen(parent));
        }));

        addRenderableWidget(list);

        int footerY = boxY + BOX_H - 26;
        addRenderableWidget(Button.builder(Component.literal("+ Add Block"),
                btn -> minecraft.gui.setScreen(new AddBlockScreen(this)))
            .bounds(boxX + 6, footerY, 110, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Done"),
                btn -> { BlockESPConfig.save(); minecraft.gui.setScreen(parent); })
            .bounds(boxX + BOX_W - 116, footerY, 110, 20).build());

        var titleLabel = new StringWidget(
                Component.literal("Block ESP").withStyle(s -> s.withColor(0xFFFFFF)), font);
        titleLabel.setX(boxX + 8);
        titleLabel.setY(boxY + (TITLE_H - 8) / 2);
        titleLabel.setWidth(80);
        titleLabel.setHeight(8);
        addRenderableWidget(titleLabel);

    }

    private static void applyPreset(Map<String, Integer> preset) {
        for (Map.Entry<String, Integer> pe : preset.entrySet()) {
            String id    = pe.getKey();
            int    color = pe.getValue();
            BlockESPEntry existing = BlockESPConfig.blocks.stream()
                .filter(e -> e.blockId.equals(id)).findFirst().orElse(null);
            if (existing != null) {
                existing.color = color;
                continue;
            }
            BuiltInRegistries.BLOCK.getOptional(Identifier.parse(id)).ifPresent(block ->
                BlockESPConfig.blocks.add(new BlockESPEntry(id, true, color)));
        }
        BlockESPConfig.rebuildLookup();
        BlockESPConfig.save();
        BlockScanner.rescanAll();
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
    public void onClose() { BlockESPConfig.save(); minecraft.gui.setScreen(parent); }

    @Override public boolean isPauseScreen() { return false; }

    static class BlockList extends ContainerObjectSelectionList<BlockList.BaseEntry> {
        BlockList(Minecraft mc, int w, int h, int y, int ih) { super(mc, w, h, y, ih); }
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
                var msg = Component.literal("No blocks tracked yet").withStyle(s -> s.withColor(0x888888));
                int tw = font.width(msg);
                var lbl = new StringWidget(msg, font);
                lbl.setX(cx + (w - tw) / 2); lbl.setY(cy + (h - 8) / 2);
                lbl.setWidth(tw); lbl.setHeight(8);
                lbl.extractRenderState(g, mx, my, delta);
            }
        }

        static class BlockRow extends BaseEntry {
            @Override public List<? extends AbstractWidget> children() { return List.of(toggleBtn, swatchBtn, removeBtn); }
            private final BlockESPEntry entry;
            private final StringWidget  labelWidget;
            private final CycleButton<Boolean> toggleBtn;
            private final Button swatchBtn;
            private final Button removeBtn;

            BlockRow(BlockESPEntry entry, BlockESPScreen screen) {
                this.entry = entry;
                Screen outerParent = screen.parent;

                String displayName = getDisplayName(entry.blockId);
                this.labelWidget = new StringWidget(Component.literal(displayName), Minecraft.getInstance().font);

                this.toggleBtn = CycleButton.<Boolean>builder(
                        val -> val ? Component.literal("Show").withStyle(s -> s.withColor(0x55FF55))
                                   : Component.literal("Hide").withStyle(s -> s.withColor(0xFF5555)),
                        entry.enabled)
                    .withValues(List.of(true, false)).displayOnlyValue()
                    .create(0, 0, 65, 20, Component.empty(),
                        (b, val) -> { entry.enabled = val; BlockESPConfig.rebuildLookup(); BlockESPConfig.save(); });

                this.swatchBtn = Button.builder(Component.empty(), b -> {
                    Minecraft mc = Minecraft.getInstance();
                    mc.gui.setScreen(new ColorPickerScreen(
                        mc.gui.screen(), displayName, entry.color,
                        newColor -> { entry.color = newColor; BlockESPConfig.save(); }
                    ));
                }).bounds(0, 0, 20, 18).build();

                this.removeBtn = Button.builder(Component.literal("×"), b -> {
                    BlockESPConfig.removeBlock(entry.blockId);
                    BlockESPConfig.save();
                    BlockScanner.rescanAll();
                    Minecraft.getInstance().gui.setScreen(new BlockESPScreen(outerParent));
                }).bounds(0, 0, 16, 18).build();
            }

            private static String getDisplayName(String blockId) {
                try {
                    Optional<Block> opt = BuiltInRegistries.BLOCK.getOptional(Identifier.parse(blockId));
                    return opt.map(b -> b.getName().getString()).orElse(blockId);
                } catch (Exception e) {
                    return blockId;
                }
            }

            @Override
            public void extractContent(GuiGraphicsExtractor g, int mx, int my, boolean hovered, float delta) {
                int cx = getContentX(), cy = getContentY(), w = getContentWidth(), h = getContentHeight();
                g.fill(cx, cy, cx + w, cy + h, hovered ? 0xFF3A3A3A : 0xFF111111);

                int rx = cx + w - 6 - 16;
                removeBtn.setX(rx); removeBtn.setY(cy + 1);
                removeBtn.setWidth(16); removeBtn.setHeight(h - 2);
                removeBtn.extractRenderState(g, mx, my, delta);

                int tx = rx - 4 - 65;
                toggleBtn.setValue(entry.enabled);
                toggleBtn.setX(tx); toggleBtn.setY(cy + 1);
                toggleBtn.setWidth(65); toggleBtn.setHeight(h - 2);
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

        static class NoteEntry extends BaseEntry {
            @Override public List<? extends AbstractWidget> children() { return List.of(widget); }
            private final StringWidget widget;
            NoteEntry(String text, int color) {
                this.widget = new StringWidget(
                    Component.literal(text).withStyle(s -> s.withColor(color)),
                    Minecraft.getInstance().font);
            }
            @Override
            public void extractContent(GuiGraphicsExtractor g, int mx, int my, boolean hovered, float delta) {
                int cx = getContentX(), cy = getContentY(), w = getContentWidth(), h = getContentHeight();
                g.fill(cx, cy, cx + w, cy + h, 0xFF111111);
                widget.setX(cx + 6); widget.setY(cy + (h - 8) / 2);
                widget.setWidth(w - 12); widget.setHeight(h);
                widget.extractRenderState(g, mx, my, delta);
            }
        }

        abstract static class BaseEntry extends ContainerObjectSelectionList.Entry<BaseEntry> {
            @Override public List<? extends AbstractWidget> children() { return List.of(); }
            @Override public List<? extends NarratableEntry> narratables() { return children(); }
            @Override public abstract void extractContent(GuiGraphicsExtractor g, int mx, int my, boolean hovered, float delta);
        }
    }
}
