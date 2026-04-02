package com.anthony.magicgame.client;

import com.anthony.magicgame.spell.GlyphCategory;
import com.anthony.magicgame.spell.GlyphDefinition;
import com.anthony.magicgame.spell.registry.CoreGlyphRegistry;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Simple dev-build spell composer that lets players mash together glyph chains without typing chat commands.
 */
public final class GlyphComposerScreen extends Screen {
    private static final int GLYPHS_PER_PAGE = 12;
    private static final int CHAIN_PREVIEW_WIDTH = 336;
    private static final int CHAIN_PREVIEW_HEIGHT = 32;
    private static final Map<GlyphCategory, List<GlyphDefinition>> GLYPHS_BY_CATEGORY = buildGlyphMap();

    private GlyphCategory selectedCategory = firstPopulatedCategory();
    private int page;

    public GlyphComposerScreen() {
        super(Component.literal("Glyph Composer"));
    }

    @Override
    protected void init() {
        refreshComposerWidgets();
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        int keyCode = event.key();
        if (keyCode == 257 || keyCode == 335) {
            MagicCastingClientController.castCurrentChain(Minecraft.getInstance());
            onClose();
            return true;
        }
        if (keyCode == 259) {
            GlyphComposerState.removeLastGlyph();
            refreshComposerWidgets();
            return true;
        }
        if (keyCode == 261) {
            GlyphComposerState.clearCurrentGlyphs();
            refreshComposerWidgets();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float partialTick) {
        int left = Math.max(16, width / 2 - 180);
        int top = 18;
        context.fill(0, 0, width, height, 0xC010141A);
        context.fill(left - 8, top - 8, left + 360, height - 18, 0xE0141C28);

        super.render(context, mouseX, mouseY, partialTick);

        context.drawString(font, title, left, top, 0xFFFFFF, false);
        context.drawString(font, Component.literal("Hold a Glyph Focus. Enter casts, Backspace removes, Delete clears."), left, top + 12, 0xBFD7E8, false);

        int chainTop = top + 28;
        context.drawString(font, Component.literal("Current Chain"), left, chainTop, 0xFFE8A3, false);
        renderChainPreview(context, left, chainTop + 12);

        int footerY = height - 54;
        context.drawString(font, Component.literal("Last Quick-Cast: " + lastCastLabel()), left, footerY, 0xA7F3FF, false);
        context.drawString(font, Component.literal("Category Page " + (page + 1) + "/" + Math.max(1, pageCount())), left, footerY + 12, 0x9FB2C7, false);
    }

    private void renderChainPreview(GuiGraphics context, int left, int top) {
        context.fill(left - 4, top - 4, left + CHAIN_PREVIEW_WIDTH + 4, top + CHAIN_PREVIEW_HEIGHT, 0xA30A1018);
        List<String> previewLines = currentChainPreviewLines(CHAIN_PREVIEW_WIDTH);
        for (int index = 0; index < Math.min(2, previewLines.size()); index++) {
            context.drawString(font, previewLines.get(index), left, top + index * (font.lineHeight + 2), 0xFFFFFF, false);
        }
    }

    private void refreshComposerWidgets() {
        clearWidgets();

        int left = Math.max(16, width / 2 - 180);
        int top = 94;
        int categoryWidth = 68;
        int categoryHeight = 20;
        int categoryGap = 4;
        int categoryIndex = 0;
        for (GlyphCategory category : GlyphCategory.values()) {
            List<GlyphDefinition> glyphs = GLYPHS_BY_CATEGORY.getOrDefault(category, List.of());
            if (glyphs.isEmpty()) {
                continue;
            }
            int x = left + categoryIndex * (categoryWidth + categoryGap);
            addRenderableWidget(Button.builder(Component.literal(formatCategoryLabel(category)), button -> {
                selectedCategory = category;
                page = 0;
                refreshComposerWidgets();
            }).bounds(x, top, categoryWidth, categoryHeight).build()).active = category != selectedCategory;
            categoryIndex++;
        }

        List<GlyphDefinition> glyphs = glyphsForSelectedPage();
        int gridTop = top + 30;
        int columns = 4;
        int buttonWidth = 86;
        int buttonHeight = 20;
        int gap = 4;
        for (int index = 0; index < glyphs.size(); index++) {
            GlyphDefinition glyph = glyphs.get(index);
            int column = index % columns;
            int row = index / columns;
            int x = left + column * (buttonWidth + gap);
            int y = gridTop + row * (buttonHeight + gap);
            addRenderableWidget(Button.builder(Component.literal(glyph.displayName()), button -> {
                GlyphComposerState.appendGlyph(glyph.id());
                refreshComposerWidgets();
            }).bounds(x, y, buttonWidth, buttonHeight).build());
        }

        int controlsTop = height - 84;
        addRenderableWidget(Button.builder(Component.literal("Analyze"), button -> {
            MagicCastingClientController.analyzeCurrentChain(Minecraft.getInstance());
        }).bounds(left, controlsTop, 84, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Cast"), button -> {
            MagicCastingClientController.castCurrentChain(Minecraft.getInstance());
            onClose();
        }).bounds(left + 90, controlsTop, 84, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Backspace"), button -> {
            GlyphComposerState.removeLastGlyph();
            refreshComposerWidgets();
        }).bounds(left + 180, controlsTop, 84, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Clear"), button -> {
            GlyphComposerState.clearCurrentGlyphs();
            refreshComposerWidgets();
        }).bounds(left + 270, controlsTop, 84, 20).build());

        int pagingTop = controlsTop + 24;
        addRenderableWidget(Button.builder(Component.literal("< Prev"), button -> {
            page = Math.max(0, page - 1);
            refreshComposerWidgets();
        }).bounds(left, pagingTop, 84, 20).build()).active = page > 0;
        addRenderableWidget(Button.builder(Component.literal("Next >"), button -> {
            page = Math.min(pageCount() - 1, page + 1);
            refreshComposerWidgets();
        }).bounds(left + 90, pagingTop, 84, 20).build()).active = page + 1 < pageCount();
        addRenderableWidget(Button.builder(Component.literal("Close"), button -> onClose()).bounds(left + 270, pagingTop, 84, 20).build());
    }

    private static Map<GlyphCategory, List<GlyphDefinition>> buildGlyphMap() {
        Map<GlyphCategory, List<GlyphDefinition>> glyphsByCategory = new EnumMap<>(GlyphCategory.class);
        for (GlyphCategory category : GlyphCategory.values()) {
            glyphsByCategory.put(category, CoreGlyphRegistry.all().stream()
                    .filter(glyph -> glyph.category() == category)
                    .toList());
        }
        return glyphsByCategory;
    }

    private GlyphCategory firstPopulatedCategory() {
        for (GlyphCategory category : GlyphCategory.values()) {
            if (!GLYPHS_BY_CATEGORY.getOrDefault(category, List.of()).isEmpty()) {
                return category;
            }
        }
        return GlyphCategory.PRINCIPLE;
    }

    private List<GlyphDefinition> glyphsForSelectedPage() {
        List<GlyphDefinition> glyphs = GLYPHS_BY_CATEGORY.getOrDefault(selectedCategory, List.of());
        int start = page * GLYPHS_PER_PAGE;
        int end = Math.min(glyphs.size(), start + GLYPHS_PER_PAGE);
        if (start >= glyphs.size()) {
            page = 0;
            start = 0;
            end = Math.min(glyphs.size(), GLYPHS_PER_PAGE);
        }
        return glyphs.subList(start, end);
    }

    private int pageCount() {
        List<GlyphDefinition> glyphs = GLYPHS_BY_CATEGORY.getOrDefault(selectedCategory, List.of());
        return Math.max(1, (glyphs.size() + GLYPHS_PER_PAGE - 1) / GLYPHS_PER_PAGE);
    }

    private static String formatCategoryLabel(GlyphCategory category) {
        String normalized = category.name().toLowerCase(Locale.ROOT).replace('_', ' ');
        return Character.toUpperCase(normalized.charAt(0)) + normalized.substring(1);
    }

    private static String lastCastLabel() {
        return GlyphComposerState.hasLastCastChain() ? GlyphComposerState.lastCastChainText() : "(none)";
    }

    private List<String> currentChainPreviewLines(int maxWidth) {
        if (!GlyphComposerState.hasCurrentChain()) {
            return List.of("(empty)");
        }

        List<String> lines = new ArrayList<>();
        StringBuilder currentLine = new StringBuilder();
        for (String glyphId : GlyphComposerState.currentGlyphs()) {
            String glyphLabel = previewGlyphLabel(glyphId);
            String candidate = currentLine.isEmpty() ? glyphLabel : currentLine + " -> " + glyphLabel;
            if (!currentLine.isEmpty() && font.width(candidate) > maxWidth) {
                lines.add(currentLine.toString());
                currentLine = new StringBuilder(glyphLabel);
                continue;
            }
            currentLine = new StringBuilder(candidate);
        }
        if (!currentLine.isEmpty()) {
            lines.add(currentLine.toString());
        }
        return lines.isEmpty() ? List.of("(empty)") : lines;
    }

    private static String previewGlyphLabel(String glyphId) {
        Optional<GlyphDefinition> glyph = CoreGlyphRegistry.find(glyphId);
        return glyph.map(GlyphDefinition::displayName).orElse(glyphId.replace('_', ' '));
    }
}
