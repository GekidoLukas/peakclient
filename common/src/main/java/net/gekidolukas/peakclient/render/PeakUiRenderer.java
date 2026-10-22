package net.gekidolukas.peakclient.render;

import net.gekidolukas.peakclient.ui.PeakPaint;
import com.mojang.blaze3d.systems.RenderSystem;
import net.gekidolukas.peakclient.PeakLog;
import net.gekidolukas.peakclient.debug.PeakDebugState;
import net.gekidolukas.peakclient.ui.PeakAbstractProgress;
import net.gekidolukas.peakclient.ui.PeakAlign;
import net.gekidolukas.peakclient.ui.PeakCarousel;
import net.gekidolukas.peakclient.ui.PeakCondition;
import net.gekidolukas.peakclient.ui.PeakConditional;
import net.gekidolukas.peakclient.ui.PeakConstraints;
import net.gekidolukas.peakclient.ui.PeakContainer;
import net.gekidolukas.peakclient.ui.PeakCycler;
import net.gekidolukas.peakclient.ui.PeakDocument;
import net.gekidolukas.peakclient.ui.PeakElement;
import net.gekidolukas.peakclient.ui.PeakIconBar;
import net.gekidolukas.peakclient.ui.PeakImage;
import net.gekidolukas.peakclient.ui.PeakInsets;
import net.gekidolukas.peakclient.ui.PeakLayoutDirection;
import net.gekidolukas.peakclient.ui.PeakOverflow;
import net.gekidolukas.peakclient.ui.PeakPixelSize;
import net.gekidolukas.peakclient.ui.PeakPoint;
import net.gekidolukas.peakclient.ui.PeakPosition;
import net.gekidolukas.peakclient.ui.PeakPositionMode;
import net.gekidolukas.peakclient.ui.PeakScaling;
import net.gekidolukas.peakclient.ui.PeakScrollbarStyle;
import net.gekidolukas.peakclient.ui.PeakSize;
import net.gekidolukas.peakclient.ui.PeakStyle;
import net.gekidolukas.peakclient.ui.PeakTableGrid;
import net.gekidolukas.peakclient.ui.PeakFeed;
import net.gekidolukas.peakclient.ui.PeakFlow;
import net.gekidolukas.peakclient.ui.PeakWorldAnchor;
import net.gekidolukas.peakclient.ui.PeakReel;
import net.gekidolukas.peakclient.ui.PeakShape;
import net.gekidolukas.peakclient.ui.PeakSortedList;
import net.gekidolukas.peakclient.ui.PeakStagger;
import net.gekidolukas.peakclient.ui.PeakTableLayoutPanel;
import net.gekidolukas.peakclient.ui.PeakTableStyle;
import net.gekidolukas.peakclient.ui.PeakTileSpans;
import net.gekidolukas.peakclient.ui.PeakTrack;
import net.gekidolukas.peakclient.ui.PeakTrackStyle;
import net.gekidolukas.peakclient.ui.client.PeakActionScope;
import net.gekidolukas.peakclient.ui.client.PeakClientUiState;
import net.gekidolukas.peakclient.ui.client.PeakEventRuntime;
import net.gekidolukas.peakclient.ui.client.PeakClientWaypointRegistry;
import net.gekidolukas.peakclient.ui.client.PeakMenuInteractionState;
import net.gekidolukas.peakclient.ui.client.PeakScrollbarRegion;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.joml.Quaternionf;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Supplier;
import java.util.Map;

public final class PeakUiRenderer {

    private static final List<String> PENDING_REMOVALS = new ArrayList<>();
    private static int percentBaseWidth = -1;
    private static int percentBaseHeight = -1;
    private static PeakLayoutProbe activeProbe;
    private static int clipLeft = PeakLayoutEntry.NO_CLIP_MIN;
    private static int clipTop = PeakLayoutEntry.NO_CLIP_MIN;
    private static int clipRight = PeakLayoutEntry.NO_CLIP_MAX;
    private static int clipBottom = PeakLayoutEntry.NO_CLIP_MAX;

    record ClipBounds(int left, int top, int right, int bottom) {}

    @FunctionalInterface
    interface ChildPainter {
        void paint(PeakElement<?> child, int index, int count, int x, int y);
    }

    private PeakUiRenderer() {}

    static ClipBounds currentClip() {
        return new ClipBounds(clipLeft, clipTop, clipRight, clipBottom);
    }

    public static void renderAll(GuiGraphics graphics, Minecraft mc) {
        PeakClientUiState.tickDocumentLifecycles();
        PeakClientWaypointRegistry.tickExpiry();
        PeakDebugState.beginFrame();
        PeakRenderStats.beginFrame();

        PeakLayoutProbe previousProbe = activeProbe;
        activeProbe = PeakDebugState.isEnabled() ? PeakDebugState.currentProbe() : null;
        try {
            for (PeakDocument document : PeakClientUiState.getDocuments()) {
                try {
                    renderDocument(graphics, mc, document);
                } catch (Exception e) {
                    PeakLog.error("Failed to render document '" + document.getId() + "'", e);
                }
            }
        } finally {
            activeProbe = previousProbe;
        }

        PeakRenderStats.endFrame();
        PeakDebugState.endFrame();

        if (!PENDING_REMOVALS.isEmpty()) {
            for (String id : PENDING_REMOVALS) PeakClientUiState.removeElementById(id);
            PENDING_REMOVALS.clear();
        }
    }

    static void requestRemoval(String elementId) {
        PENDING_REMOVALS.add(elementId);
    }

    public static void renderMenu(GuiGraphics graphics, Minecraft mc, PeakDocument document, PeakLayoutProbe probe) {
        PeakLayoutProbe previousProbe = activeProbe;
        int savedLeft = clipLeft, savedTop = clipTop, savedRight = clipRight, savedBottom = clipBottom;
        activeProbe = probe;
        clipLeft = PeakLayoutEntry.NO_CLIP_MIN;
        clipTop = PeakLayoutEntry.NO_CLIP_MIN;
        clipRight = PeakLayoutEntry.NO_CLIP_MAX;
        clipBottom = PeakLayoutEntry.NO_CLIP_MAX;
        try {
            renderDocument(graphics, mc, document);
        } catch (Exception e) {
            PeakLog.error("Failed to render menu '" + document.getId() + "'", e);
        } finally {
            activeProbe = previousProbe;
            clipLeft = savedLeft;
            clipTop = savedTop;
            clipRight = savedRight;
            clipBottom = savedBottom;
        }
    }

    public static PeakPixelSize measureFloating(Font font, PeakElement<?> element) {
        int savedWidth = percentBaseWidth, savedHeight = percentBaseHeight;
        percentBaseWidth = -1;
        percentBaseHeight = -1;
        try {
            return footprint(font, element);
        } finally {
            percentBaseWidth = savedWidth;
            percentBaseHeight = savedHeight;
        }
    }

    public static void renderFloating(GuiGraphics graphics, Minecraft mc, PeakElement<?> element, int x, int y) {
        PeakLayoutProbe previousProbe = activeProbe;
        int savedLeft = clipLeft, savedTop = clipTop, savedRight = clipRight, savedBottom = clipBottom;
        int savedWidth = percentBaseWidth, savedHeight = percentBaseHeight;
        activeProbe = null;
        clipLeft = PeakLayoutEntry.NO_CLIP_MIN;
        clipTop = PeakLayoutEntry.NO_CLIP_MIN;
        clipRight = PeakLayoutEntry.NO_CLIP_MAX;
        clipBottom = PeakLayoutEntry.NO_CLIP_MAX;
        percentBaseWidth = -1;
        percentBaseHeight = -1;
        try {
            renderChild(graphics, mc.font, element, x, y, 0, 0, 1f, 1f, "peak:tooltip", 0);
        } catch (Exception e) {
            PeakLog.error("Failed to render tooltip element '" + element.getId() + "'", e);
        } finally {
            activeProbe = previousProbe;
            clipLeft = savedLeft;
            clipTop = savedTop;
            clipRight = savedRight;
            clipBottom = savedBottom;
            percentBaseWidth = savedWidth;
            percentBaseHeight = savedHeight;
        }
    }

    static void renderDocument(GuiGraphics graphics, Minecraft mc, PeakDocument document) {
        PeakOffscreenOpacity.resetDepth();
        if (PeakDebugState.isEnabled()) {
            long bindingStart = PeakRenderStats.beginBindings();
            PeakBindingResolver.applyTree(document);
            PeakRenderStats.endBindings(bindingStart);
        } else {
            PeakBindingResolver.applyTree(document);
        }
        PeakEventRuntime.evaluate(document, document.isMenu()
                ? PeakActionScope.menu(document.getId()) : PeakActionScope.document(document.getId()));
        if (isCollapsed(document)) return;

        if (document.getScaling() == PeakScaling.FIXED) {
            float guiScale = (float) mc.getWindow().getGuiScale();
            float inverse = 1f / guiScale;
            graphics.pose().pushPose();
            graphics.pose().scale(inverse, inverse, 1f);
            if (document.isMenu()) {
                PeakContainerRenderer.drawBox(graphics, document.getStyle(),
                        mc.getWindow().getWidth(), mc.getWindow().getHeight());
            }
            layoutAndRenderChildren(graphics, mc.font, document, 0, 0,
                    mc.getWindow().getWidth(), mc.getWindow().getHeight(),
                    0, 0, 1f, inverse, document.getId(), 0);
            graphics.pose().popPose();
        } else {
            if (document.isMenu()) {
                PeakContainerRenderer.drawBox(graphics, document.getStyle(),
                        mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight());
            }
            layoutAndRenderChildren(graphics, mc.font, document, 0, 0,
                    mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight(),
                    0, 0, 1f, 1f, document.getId(), 0);
        }
    }

    static void layoutAndRenderChildren(GuiGraphics graphics, Font font, PeakContainer<?> container,
                                                int originX, int originY, int boxWidth, int boxHeight,
                                                int absX, int absY, float absScale, float scissorUnitFactor,
                                        String documentId, int depth) {
        PeakInsets padding = container.getStyle().getPadding();
        int contentX = originX + padding.getLeft();
        int contentY = originY + padding.getTop();
        int contentWidth = boxWidth - padding.getLeft() - padding.getRight();
        int contentHeight = boxHeight - padding.getTop() - padding.getBottom();

        PeakOverflow overflow = container.getOverflow();
        boolean scissoring = overflow == PeakOverflow.HIDDEN || overflow == PeakOverflow.SCROLL
                || container instanceof PeakCarousel || container instanceof PeakReel || container instanceof PeakShape;
        boolean scrollable = overflow == PeakOverflow.SCROLL && PeakMenuInteractionState.isActive();
        int savedClipLeft = clipLeft, savedClipTop = clipTop, savedClipRight = clipRight, savedClipBottom = clipBottom;
        if (scissoring) {
            PeakStyle style = container.getStyle();
            boolean hasBorder = style.getBorderWidth() > 0 && !style.getBorderColor().isNone();
            int border = hasBorder ? Math.min(style.getBorderWidth(), Math.min(boxWidth, boxHeight) / 2) : 0;
            int boxClipLeft = originX + Math.max(padding.getLeft(), border);
            int boxClipTop = originY + Math.max(padding.getTop(), border);
            int boxClipRight = originX + boxWidth - Math.max(padding.getRight(), border);
            int boxClipBottom = originY + boxHeight - Math.max(padding.getBottom(), border);
            int scissorLeft = Math.round((absX + boxClipLeft * absScale) * scissorUnitFactor);
            int scissorTop = Math.round((absY + boxClipTop * absScale) * scissorUnitFactor);
            int scissorRight = Math.round((absX + boxClipRight * absScale) * scissorUnitFactor);
            int scissorBottom = Math.round((absY + boxClipBottom * absScale) * scissorUnitFactor);
            if (container instanceof PeakShape popShape && popShape.hasPopOut()) {
                Minecraft popMinecraft = Minecraft.getInstance();
                if (popShape.getPopOutTop() > 0f) scissorTop = 0;
                if (popShape.getPopOutLeft() > 0f) scissorLeft = 0;
                if (popShape.getPopOutBottom() > 0f) scissorBottom = Math.max(scissorBottom, popMinecraft.getWindow().getGuiScaledHeight());
                if (popShape.getPopOutRight() > 0f) scissorRight = Math.max(scissorRight, popMinecraft.getWindow().getGuiScaledWidth());
            }
            graphics.enableScissor(scissorLeft, scissorTop, scissorRight, scissorBottom);
            clipLeft = Math.max(clipLeft, scissorLeft);
            clipTop = Math.max(clipTop, scissorTop);
            clipRight = Math.min(clipRight, scissorRight);
            clipBottom = Math.min(clipBottom, scissorBottom);
        }

        int savedBaseWidth = percentBaseWidth;
        int savedBaseHeight = percentBaseHeight;
        boolean isRoot = container instanceof PeakDocument;
        boolean widthResolved = isRoot || resolveDimension(container.getSize().getWidth(), savedBaseWidth) >= 0;
        boolean heightResolved = isRoot || resolveDimension(container.getSize().getHeight(), savedBaseHeight) >= 0;
        boolean scrollVertical = container.scrollsVertically();
        int scrollOffset = 0;
        int scrollMax = 0;
        try {
            percentBaseWidth = widthResolved ? Math.max(0, contentWidth) : -1;
            percentBaseHeight = heightResolved ? Math.max(0, contentHeight) : -1;

            if (scrollable) {
                PeakPixelSize content = container instanceof PeakFlow scrollFlow
                        ? PeakFlowLayout.measureWithLimit(font, scrollFlow, scrollFlow.getDirection() == PeakLayoutDirection.ROW ? contentWidth : contentHeight)
                        : measureContainerContentUnresolved(font, container);
                scrollMax = Math.max(0, scrollVertical ? content.height() - boxHeight : content.width() - boxWidth);
                scrollOffset = PeakMenuInteractionState.reportScroll(container.getId(), scrollMax);
            }
            int flowX = scrollVertical ? contentX : contentX - scrollOffset;
            int flowY = scrollVertical ? contentY - scrollOffset : contentY;

            if (container instanceof PeakTableLayoutPanel table) {
                renderTableChildren(graphics, font, table, flowX, flowY,
                        widthResolved ? Math.max(0, contentWidth) : -1, heightResolved ? Math.max(0, contentHeight) : -1,
                        absX, absY, absScale, scissorUnitFactor, documentId, depth);
            } else if (container instanceof PeakCarousel carousel) {
                PeakCarouselLayout.render(graphics, font, carousel, contentX, contentY, contentWidth, contentHeight,
                        absX, absY, absScale, scissorUnitFactor, documentId, depth);
            } else if (container instanceof PeakCycler cycler) {
                PeakCyclerLayout.render(graphics, font, cycler, contentX, contentY, contentWidth, contentHeight,
                        absX, absY, absScale, scissorUnitFactor, documentId, depth);
            } else if (container instanceof PeakFeed feed) {
                PeakFeedLayout.render(graphics, font, feed, contentX, contentY, contentWidth, contentHeight,
                        absX, absY, absScale, scissorUnitFactor, documentId, depth);
            } else if (container instanceof PeakSortedList sortedList) {
                PeakSortedListLayout.render(graphics, font, sortedList, contentX, contentY, contentWidth, contentHeight,
                        absX, absY, absScale, scissorUnitFactor, documentId, depth);
            } else if (container instanceof PeakReel reel) {
                PeakReelLayout.render(graphics, font, reel, contentX, contentY, contentWidth, contentHeight,
                        absX, absY, absScale, scissorUnitFactor, documentId, depth);
            } else if (container instanceof PeakFlow flow) {
                PeakFlowLayout.render(graphics, font, flow, flowX, flowY, contentWidth, contentHeight,
                        absX, absY, absScale, scissorUnitFactor, documentId, depth);
            } else {
                renderFlowChildren(graphics, font, container, flowX, flowY, contentWidth, contentHeight,
                        absX, absY, absScale, scissorUnitFactor, documentId, depth,
                        container instanceof PeakStagger stagger
                                ? PeakStaggerLayout.painter(graphics, font, stagger, absX, absY, absScale, scissorUnitFactor, documentId, depth)
                                : null);
            }

            percentBaseWidth = widthResolved ? Math.max(0, boxWidth) : -1;
            percentBaseHeight = heightResolved ? Math.max(0, boxHeight) : -1;
            List<PeakWorldAnchor> worldAnchors = null;
            for (PeakElement<?> child : container.getChildren()) {
                if (isCollapsed(child) || child.getPositionMode() != PeakPositionMode.ABSOLUTE) continue;
                if (child instanceof PeakWorldAnchor worldAnchor) {
                    if (worldAnchors == null) worldAnchors = new ArrayList<>();
                    worldAnchors.add(worldAnchor);
                    continue;
                }
                PeakPixelSize childFootprint = footprint(font, child);
                PeakPoint position = resolveAbsolutePosition(child, originX, originY, boxWidth, boxHeight, childFootprint);
                renderChild(graphics, font, child, position.x(), position.y(), absX, absY, absScale, scissorUnitFactor,documentId,depth+1);
            }
            if (worldAnchors != null) {
                PeakWorldAnchorLayout.renderAll(graphics, font, worldAnchors, absX, absY, absScale, scissorUnitFactor, documentId, depth);
            }
        } finally {
            percentBaseWidth = savedBaseWidth;
            percentBaseHeight = savedBaseHeight;
            clipLeft = savedClipLeft;
            clipTop = savedClipTop;
            clipRight = savedClipRight;
            clipBottom = savedClipBottom;
        }

        if (scissoring) {
            graphics.disableScissor();
        }

        PeakScrollbarStyle scrollbarStyle = scrollable && scrollMax > 0
                ? PeakMenuInteractionState.scrollbar(container.getScrollbar()) : null;
        if (scrollbarStyle != null && scrollbarStyle.isVisible()) {
            drawScrollbar(graphics, container, scrollbarStyle, originX, originY, boxWidth, boxHeight, scrollVertical,
                    scrollOffset, scrollMax, absX, absY, absScale, scissorUnitFactor);
        }
    }

    private static void drawScrollbar(GuiGraphics graphics, PeakContainer<?> container, PeakScrollbarStyle style,
                                      int originX, int originY, int boxWidth, int boxHeight, boolean vertical, int offset, int max,
                                      int absX, int absY, float absScale, float scissorUnitFactor) {
        drawBoxScrollbar(graphics, container.getId(), style, originX, originY, boxWidth, boxHeight,
                vertical, offset, max, absX, absY, absScale, scissorUnitFactor);
    }

    static void drawBoxScrollbar(GuiGraphics graphics, String ownerId, PeakScrollbarStyle style, int originX, int originY,
                                 int boxWidth, int boxHeight, boolean vertical, int offset, int max,
                                 int absX, int absY, float absScale, float scissorUnitFactor) {
        PeakScrollbars.Bar bar = PeakScrollbars.layout(style, vertical, originX, originY, boxWidth, boxHeight, offset, max);
        PeakScrollbars.draw(graphics, style, bar, PeakMenuInteractionState.scrollbarState(ownerId));

        PeakMenuInteractionState.reportScrollbar(new PeakScrollbarRegion(ownerId, vertical,
                toScreen(absX, bar.trackX1(), absScale, scissorUnitFactor),
                toScreen(absY, bar.trackY1(), absScale, scissorUnitFactor),
                toScreen(absX, bar.trackX2(), absScale, scissorUnitFactor),
                toScreen(absY, bar.trackY2(), absScale, scissorUnitFactor),
                toScreen(absX, bar.thumbX1(), absScale, scissorUnitFactor),
                toScreen(absY, bar.thumbY1(), absScale, scissorUnitFactor),
                toScreen(absX, bar.thumbX2(), absScale, scissorUnitFactor),
                toScreen(absY, bar.thumbY2(), absScale, scissorUnitFactor),
                max, clipLeft, clipTop, clipRight, clipBottom));
    }

    private static int toScreen(int parentPosition, int local, float absScale, float scissorUnitFactor) {
        return Math.round((parentPosition + local * absScale) * scissorUnitFactor);
    }

    private static void renderFlowChildren(GuiGraphics graphics, Font font, PeakContainer<?> container,
                                           int contentX, int contentY, int contentWidth, int contentHeight,
                                           int absX, int absY, float absScale, float scissorUnitFactor,
                                           String documentId, int depth, ChildPainter painter) {
        boolean column = container.getDirection() == PeakLayoutDirection.COLUMN;

        List<PeakElement<?>> relevant = new ArrayList<>();
        List<PeakPixelSize> footprints = new ArrayList<>();
        int totalMain = 0;

        for (PeakElement<?> child : container.getChildren()) {
            if (isCollapsed(child) || child.getPositionMode() == PeakPositionMode.ABSOLUTE) continue;
            PeakPixelSize childFootprint = footprint(font, child);
            PeakInsets margin = child.getStyle().getMargin();
            int mainSize = column ? childFootprint.height() + margin.getTop() + margin.getBottom() : childFootprint.width() + margin.getLeft() + margin.getRight();
            relevant.add(child);
            footprints.add(childFootprint);
            totalMain += mainSize;
        }

        int available = column ? contentHeight : contentWidth;
        int leftover = Math.max(0, available - totalMain);
        int relevantCount = relevant.size();

        int startOffset = 0;
        int extraGap = 0;

        switch (container.getMainAxisAlign()) {
            case CENTER -> startOffset = leftover / 2;
            case END -> startOffset = leftover;
            case SPACE_BETWEEN -> {
                if (relevantCount > 1) extraGap = leftover / (relevantCount - 1);
                else startOffset = leftover / 2;
            }
            case SPACE_AROUND -> {
                if (relevantCount > 0) {
                    int edgeGap = leftover / (2 * relevantCount);
                    extraGap = edgeGap * 2;
                    startOffset = edgeGap;
                }
            }
            case START -> { }
        }

        int cursorMain = (column ? contentY : contentX) + startOffset;

        for (int i = 0; i < relevant.size(); i++) {
            PeakElement<?> child = relevant.get(i);
            PeakPixelSize childFootprint = footprints.get(i);
            PeakInsets margin = child.getStyle().getMargin();
            int x, y;

            if (column) {
                int crossAvailable = contentWidth - margin.getLeft() - margin.getRight();
                int crossOffset = switch (resolveContainerAlign(container.getAlignChildren())) {
                    case CENTER -> Math.max(0, (crossAvailable - childFootprint.width()) / 2);
                    case END -> Math.max(0, crossAvailable - childFootprint.width());
                    case START, AUTO -> 0;
                };
                x = contentX + margin.getLeft() + crossOffset + child.getPosition().getOffsetX();
                y = cursorMain + margin.getTop() + child.getPosition().getOffsetY();
                cursorMain = y + childFootprint.height() + margin.getBottom() + extraGap + (i < relevant.size() - 1 ? container.getGap() : 0);
            } else {
                int crossAvailable = contentHeight - margin.getTop() - margin.getBottom();
                int crossOffset = switch (resolveContainerAlign(container.getAlignChildren())) {
                    case CENTER -> Math.max(0, (crossAvailable - childFootprint.height()) / 2);
                    case END -> Math.max(0, crossAvailable - childFootprint.height());
                    case START, AUTO -> 0;
                };
                x = cursorMain + margin.getLeft() + child.getPosition().getOffsetX();
                y = contentY + margin.getTop() + crossOffset + child.getPosition().getOffsetY();
                cursorMain = x + childFootprint.width() + margin.getRight() + extraGap + (i < relevant.size() - 1 ? container.getGap() : 0);
            }

            if (painter == null) {
                renderChild(graphics, font, child, x, y, absX, absY, absScale, scissorUnitFactor, documentId, depth + 1);
            } else {
                painter.paint(child, i, relevant.size(), x, y);
            }
        }
    }

    static void renderTableChildren(GuiGraphics graphics, Font font, PeakTableLayoutPanel table,
                                            int contentX, int contentY, int availableWidth, int availableHeight,
                                            int absX, int absY, float absScale, float scissorUnitFactor,
                                    String documentId, int depth) {
        TableLayout layout = computeTableLayout(font, table, availableWidth, availableHeight);
        PeakTableGrid.Result grid = layout.grid();
        PeakInsets padding = table.getCellPadding();

        if (PeakDebugState.isEnabled()) {
            if (LAST_TABLES.size() > 256) LAST_TABLES.clear();
            LAST_TABLES.put(table.getId(), grid);
        }
        drawTableDecor(graphics, table.getDecor(), grid, contentX, contentY);

        int savedBaseWidth = percentBaseWidth;
        int savedBaseHeight = percentBaseHeight;
        try {
            for (int i = 0; i < layout.cells().size(); i++) {
                PeakTableGrid.Placement placement = grid.placements().get(i);
                PeakElement<?> cell = layout.cells().get(i);
                PeakInsets margin = cell.getStyle().getMargin();

                int cellWidth = grid.spanWidth(placement.column(), placement.columnSpan());
                int cellHeight = grid.spanHeight(placement.row(), placement.rowSpan());
                int innerWidth = Math.max(0, cellWidth - padding.getLeft() - padding.getRight() - margin.getLeft() - margin.getRight());
                int innerHeight = Math.max(0, cellHeight - padding.getTop() - padding.getBottom() - margin.getTop() - margin.getBottom());

                percentBaseWidth = innerWidth;
                percentBaseHeight = innerHeight;
                PeakPixelSize cellFootprint = footprint(font, cell);

                PeakTrack columnTrack = table.getColumns().get(Math.min(placement.column(), table.getColumnCount() - 1));
                PeakTrack rowTrack = placement.row() < table.getRows().size()
                        ? table.getRows().get(placement.row()) : table.getRowHeight();
                PeakAlign horizontal = PeakTableGrid.resolveAlign(cell.getAlignSelf(), columnTrack.getAlign(), table.getCellAlign());
                PeakAlign vertical = PeakTableGrid.resolveAlign(cell.getVerticalAlign(), rowTrack.getAlign(), table.getCellVerticalAlign());

                int x = contentX + grid.columnStart(placement.column()) + padding.getLeft() + margin.getLeft()
                        + PeakTableGrid.alignOffset(horizontal, innerWidth - cellFootprint.width()) + cell.getPosition().getOffsetX();
                int y = contentY + grid.rowStart(placement.row()) + padding.getTop() + margin.getTop()
                        + PeakTableGrid.alignOffset(vertical, innerHeight - cellFootprint.height()) + cell.getPosition().getOffsetY();
                renderChild(graphics, font, cell, x, y, absX, absY, absScale, scissorUnitFactor, documentId, depth + 1);
            }
        } finally {
            percentBaseWidth = savedBaseWidth;
            percentBaseHeight = savedBaseHeight;
        }
    }

    static void renderChild(GuiGraphics graphics, Font font, PeakElement<?> element,
                                    int localX, int localY,
                                    int parentAbsX, int parentAbsY, float parentAbsScale, float scissorUnitFactor,
                                    String documentId, int depth) {
        PeakRenderStats.countElement();
        PeakPixelSize local = measureLocal(font, element);
        int newAbsX = parentAbsX + Math.round(localX * parentAbsScale);
        int newAbsY = parentAbsY + Math.round(localY * parentAbsScale);
        float newAbsScale = parentAbsScale * element.getScale();

        if (activeProbe != null) {
            int screenX = Math.round(newAbsX * scissorUnitFactor);
            int screenY = Math.round(newAbsY * scissorUnitFactor);
            int screenWidth = Math.round(local.width() * newAbsScale * scissorUnitFactor);
            int screenHeight = Math.round(local.height() * newAbsScale * scissorUnitFactor);
            activeProbe.record(new PeakLayoutEntry(documentId, element.getId(), element.getClass().getSimpleName(),
                    screenWidth, screenHeight, screenX, screenY, depth,
                    element, -1, local.width(), local.height(), newAbsScale,
                    percentBaseWidth, percentBaseHeight, isInvisible(element),
                    clipLeft, clipTop, clipRight, clipBottom));
        }

        if (isInvisible(element)) return;

        boolean fading = element.getOpacity() < 0.999f;
        if (fading) {
            RenderSystem.setShaderColor(1f, 1f, 1f, element.getOpacity());
        }

        graphics.pose().pushPose();
        graphics.pose().translate(localX, localY, 0);
        if (element.getRotationDegrees() != 0f) {
            graphics.pose().translate(local.width() / 2f, local.height() / 2f, 0);
            graphics.pose().mulPose(new Quaternionf().rotateZ((float) Math.toRadians(element.getRotationDegrees())));
            graphics.pose().translate(-local.width() / 2f, -local.height() / 2f, 0);
        }
        graphics.pose().scale(element.getScale(), element.getScale(), 1f);

        PeakElementRenderer<PeakElement<?>> renderer = PeakElementRendererRegistry.get(element.getClass());
        if (renderer != null) {
            PeakRenderContext ctx = new PeakRenderContext(graphics, font, newAbsX, newAbsY, newAbsScale, scissorUnitFactor, documentId, depth);
            try {
                renderer.renderContent(ctx, element, local.width(), local.height());
            } catch (Exception e) {
                PeakLog.error("Failed to render element '" + element.getId() + "'", e);
            } finally {
                if (renderer.writesDepth()) RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
            }
        } else {
            PeakLog.warn("No renderer registered for element type '{}' (id: {})", element.getClass().getSimpleName(), element.getId());
        }

        graphics.pose().popPose();

        if (fading) {
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        }
    }

    private static boolean isInvisible(PeakElement<?> element) {
        return element.isHidden();
    }

    static int parentBaseWidth() {
        return percentBaseWidth;
    }

    static int parentBaseHeight() {
        return percentBaseHeight;
    }

    static <T> T withBases(int width, int height, Supplier<T> action) {
        int savedWidth = percentBaseWidth, savedHeight = percentBaseHeight;
        percentBaseWidth = width;
        percentBaseHeight = height;
        try {
            return action.get();
        } finally {
            percentBaseWidth = savedWidth;
            percentBaseHeight = savedHeight;
        }
    }

    static boolean isCollapsed(PeakElement<?> element) {
        if (isInvisible(element) && element.isCollapseWhenHidden()) return true;
        if (element instanceof PeakConditional conditional) {
            return !evaluateConditions(conditional);
        }
        if (element instanceof PeakAbstractProgress<?> progress) {
            PeakProgressValues values = PeakProgressValueResolver.resolve(progress);
            if (progress.isHideIfFull() && values.isFull()) return true;
            if (progress.isHideIfEmpty() && values.isEmpty()) return true;
        } else if (element instanceof PeakIconBar bar) {
            PeakProgressValues values = PeakProgressValueResolver.resolveIconBar(bar, Minecraft.getInstance());
            if (bar.isHideIfEmpty() && values.isEmpty()) return true;
            if (bar.isHideIfFull() && values.isFull()) return true;
        }
        return false;
    }

    private static boolean evaluateConditions(PeakConditional conditional) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return false;
        List<PeakCondition> conditions = conditional.getConditions();
        if (conditions.isEmpty()) return true;

        if (conditional.isRequireAll()) {
            for (PeakCondition condition : conditions) if (!evaluate(condition, mc)) return false;
            return true;
        } else {
            for (PeakCondition condition : conditions) if (evaluate(condition, mc)) return true;
            return false;
        }
    }

    private static boolean evaluate(PeakCondition condition, Minecraft mc) {
        return switch (condition.getType()) {
            case DIMENSION -> mc.level.dimension().location().toString().equals(condition.getStringValue());
            case GAMEMODE -> mc.gameMode != null && mc.gameMode.getPlayerMode().getName().equals(condition.getStringValue());
            case SNEAKING -> mc.player.isCrouching();
            case SPRINTING -> mc.player.isSprinting();
            case SWIMMING -> mc.player.isSwimming();
            case FLYING -> mc.player.getAbilities().flying;
            case RIDING -> mc.player.getVehicle() != null;
            case UNDERWATER -> mc.player.isEyeInFluid(FluidTags.WATER);
            case IS_DAY -> mc.level.isDay();
            case IS_NIGHT -> !mc.level.isDay();
            case HEALTH_BELOW -> mc.player.getHealth() < condition.getNumberValue();
            case HEALTH_ABOVE -> mc.player.getHealth() > condition.getNumberValue();
            case TARGETING_ENTITY -> mc.hitResult instanceof EntityHitResult;
            case TARGETING_BLOCK -> mc.hitResult instanceof BlockHitResult && mc.hitResult.getType() != HitResult.Type.MISS;
        };
    }

    private static PeakPixelSize measureLocal(Font font, PeakElement<?> element) {
        if (!PeakDebugState.isEnabled()) return measureLocalUntimed(font, element);
        PeakRenderStats.beginLayout();
        try {
            return measureLocalUntimed(font, element);
        } finally {
            PeakRenderStats.endLayout();
        }
    }

    private static PeakPixelSize measureLocalUntimed(Font font, PeakElement<?> element) {
        PeakElementRenderer<PeakElement<?>> renderer = PeakElementRendererRegistry.get(element.getClass());
        int width = 0, height = 0;

        if (renderer != null) {
            try {
                PeakPixelSize content = renderer.measureContent(font, element);
                width = content.width();
                height = content.height();
            } catch (Exception e) {
                PeakLog.error("Failed to measure element '" + element.getId() + "'", e);
            }
        } else {
            PeakLog.warn("No renderer registered for element type '{}' (id: {})", element.getClass().getSimpleName(), element.getId());
        }

        PeakSize size = element.getSize();
        int resolvedWidth = resolveDimension(size.getWidth(), percentBaseWidth);
        int resolvedHeight = resolveDimension(size.getHeight(), percentBaseHeight);
        boolean widthSet = resolvedWidth >= 0;
        boolean heightSet = resolvedHeight >= 0;
        if (widthSet) width = resolvedWidth;
        if (heightSet) height = resolvedHeight;

        if (element instanceof PeakImage image && image.getAspectRatio() > 0) {
            float ratio = image.getAspectRatio();
            if (widthSet && !heightSet) height = Math.round(width / ratio);
            else if (heightSet && !widthSet) width = Math.round(height * ratio);
        }

        return clampToConstraints(element, width, height);
    }

    static PeakPixelSize measureContainerContent(Font font, PeakContainer<?> container) {
        int savedBaseWidth = percentBaseWidth;
        int savedBaseHeight = percentBaseHeight;
        percentBaseWidth = -1;
        percentBaseHeight = -1;
        try {
            return measureContainerContentUnresolved(font, container);
        } finally {
            percentBaseWidth = savedBaseWidth;
            percentBaseHeight = savedBaseHeight;
        }
    }

    static int resolveDimension(PeakSize.PeakDimension dimension, int base) {
        return switch (dimension.mode()) {
            case PIXELS -> (int) dimension.value();
            case PERCENT -> base < 0 ? -1 : Math.round(base * dimension.value() / 100f);
            case AUTO -> -1;
        };
    }

    private static PeakPixelSize measureContainerContentUnresolved(Font font, PeakContainer<?> container) {
        if (container instanceof PeakTableLayoutPanel table) {
            PeakTableGrid.Result grid = computeTableLayout(font, table, -1, -1).grid();
            PeakInsets padding = table.getStyle().getPadding();
            return new PeakPixelSize(
                    grid.contentWidth() + padding.getLeft() + padding.getRight(),
                    grid.contentHeight() + padding.getTop() + padding.getBottom()
            );
        }
        if (container instanceof PeakFlow flow) return PeakFlowLayout.measure(font, flow);
        if (container instanceof PeakCarousel carousel) return measureCarousel(font, carousel);
        if (container instanceof PeakCycler cycler) return measureCycler(font, cycler);
        int w = 0, h = 0;
        int relevantCount = 0;
        for (PeakElement<?> child : container.getChildren()) {
            if (isCollapsed(child) || child.getPositionMode() == PeakPositionMode.ABSOLUTE) continue;
            PeakPixelSize childFootprint = footprint(font, child);
            PeakInsets margin = child.getStyle().getMargin();
            int marginWidth = margin.getLeft() + margin.getRight();
            int marginHeight = margin.getTop() + margin.getBottom();
            if (container.getDirection() == PeakLayoutDirection.COLUMN) {
                w = Math.max(w, childFootprint.width() + marginWidth);
                h += childFootprint.height() + marginHeight;
            } else {
                w += childFootprint.width() + marginWidth;
                h = Math.max(h, childFootprint.height() + marginHeight);
            }
            relevantCount++;
        }
        int totalGap = Math.max(0, relevantCount - 1) * container.getGap();
        if (container.getDirection() == PeakLayoutDirection.COLUMN) h += totalGap; else w += totalGap;

        PeakInsets padding = container.getStyle().getPadding();
        return new PeakPixelSize(w + padding.getLeft() + padding.getRight(), h + padding.getTop() + padding.getBottom());
    }

    private static PeakPixelSize measureCarousel(Font font, PeakCarousel carousel) {
        boolean horizontal = carousel.getMovement().isHorizontal();
        int main = 0, cross = 0, count = 0;
        for (PeakElement<?> child : carousel.getChildren()) {
            if (child.isHidden() || isCollapsed(child) || child.getPositionMode() == PeakPositionMode.ABSOLUTE) continue;
            PeakPixelSize childFootprint = footprint(font, child);
            PeakInsets margin = child.getStyle().getMargin();
            int childMain = horizontal ? childFootprint.width() + margin.getLeft() + margin.getRight()
                    : childFootprint.height() + margin.getTop() + margin.getBottom();
            int childCross = horizontal ? childFootprint.height() + margin.getTop() + margin.getBottom()
                    : childFootprint.width() + margin.getLeft() + margin.getRight();
            main += childMain;
            cross = Math.max(cross, childCross);
            count++;
        }
        main += Math.max(0, count - 1) * carousel.getGap();
        PeakInsets padding = carousel.getStyle().getPadding();
        int width = horizontal ? main : cross;
        int height = horizontal ? cross : main;
        return new PeakPixelSize(width + padding.getLeft() + padding.getRight(), height + padding.getTop() + padding.getBottom());
    }

    private static PeakPixelSize measureCycler(Font font, PeakCycler cycler) {
        int width = 0, height = 0;
        for (PeakElement<?> child : cycler.getChildren()) {
            if (child.isHidden() || isCollapsed(child) || child.getPositionMode() == PeakPositionMode.ABSOLUTE) continue;
            PeakPixelSize childFootprint = footprint(font, child);
            PeakInsets margin = child.getStyle().getMargin();
            width = Math.max(width, childFootprint.width() + margin.getLeft() + margin.getRight());
            height = Math.max(height, childFootprint.height() + margin.getTop() + margin.getBottom());
        }
        PeakInsets padding = cycler.getStyle().getPadding();
        return new PeakPixelSize(width + padding.getLeft() + padding.getRight(), height + padding.getTop() + padding.getBottom());
    }

    private static PeakPixelSize clampToConstraints(PeakElement<?> element, int width, int height) {
        PeakConstraints constraints = element.getConstraints();
        int minWidth = resolveDimension(constraints.getMinWidth(), percentBaseWidth);
        int maxWidth = resolveDimension(constraints.getMaxWidth(), percentBaseWidth);
        int minHeight = resolveDimension(constraints.getMinHeight(), percentBaseHeight);
        int maxHeight = resolveDimension(constraints.getMaxHeight(), percentBaseHeight);
        if (minWidth >= 0) width = Math.max(width, minWidth);
        if (maxWidth >= 0) width = Math.min(width, maxWidth);
        if (minHeight >= 0) height = Math.max(height, minHeight);
        if (maxHeight >= 0) height = Math.min(height, maxHeight);
        return new PeakPixelSize(width, height);
    }

    static PeakPixelSize footprint(Font font, PeakElement<?> element) {
        PeakPixelSize local = measureLocal(font, element);
        return local.scaledBy(element.getScale());
    }

    private static final Map<String, PeakTableGrid.Result> LAST_TABLES = new HashMap<>();

    public static PeakTableGrid.Result lastTableGrid(String tableId) {
        return LAST_TABLES.get(tableId);
    }

    private static void drawTableDecor(GuiGraphics graphics, PeakTableStyle decor, PeakTableGrid.Result grid,
                                       int originX, int originY) {
        if (!decor.isActive() || grid.rows() == 0) return;

        int headerRows = decor.getHeaderRows();
        for (int row = headerRows; row < grid.rows(); row++) {
            fillTableRow(graphics, grid, row, originX, originY,
                    (row - headerRows) % 2 == 0 ? decor.getStripeFirst() : decor.getStripeSecond());
        }
        for (int column = 0; column < grid.columns(); column++) {
            PeakTrackStyle style = decor.getColumnStyle(column);
            if (style != null && style.hasBackground()) fillTableColumn(graphics, grid, column, originX, originY, style.getBackground());
        }
        for (int row = 0; row < Math.min(headerRows, grid.rows()); row++) {
            fillTableRow(graphics, grid, row, originX, originY, decor.getHeaderBackground());
        }
        for (int row = 0; row < grid.rows(); row++) {
            PeakTrackStyle style = decor.getRowStyle(row);
            if (style != null && style.hasBackground()) fillTableRow(graphics, grid, row, originX, originY, style.getBackground());
        }

        PeakTableStyle.LineMode mode = decor.getLineMode();
        boolean globalRows = decor.hasLines() && (mode == PeakTableStyle.LineMode.ROWS || mode == PeakTableStyle.LineMode.BOTH);
        boolean globalColumns = decor.hasLines() && (mode == PeakTableStyle.LineMode.COLUMNS || mode == PeakTableStyle.LineMode.BOTH);
        int halfColumn = grid.columnSpacing() / 2;
        int halfRow = grid.rowSpacing() / 2;

        for (int boundary = 0; boundary < grid.rows() - 1; boundary++) {
            TableLine line = resolveTableLine(decor, decor.getRowStyle(boundary), globalRows);
            if (line == null) continue;
            int y = originY + grid.rowStart(boundary + 1) - halfRow - line.width() / 2;
            for (int column = 0; column < grid.columns(); column++) {
                if (!decor.isLinesAcrossSpans() && grid.rowBoundaryBlocked(boundary, column)) continue;
                int x1 = originX + grid.columnStart(column) - (column > 0 ? halfColumn : 0);
                int x2 = originX + grid.columnStart(column) + grid.columnWidths()[column]
                        + (column < grid.columns() - 1 ? grid.columnSpacing() - halfColumn : 0);
                PeakPaintRenderer.fill(graphics, line.paint(), x1, y, x2, y + line.width(),
                        originX, originY, originX + grid.contentWidth(), originY + grid.contentHeight());
            }
        }
        for (int boundary = 0; boundary < grid.columns() - 1; boundary++) {
            TableLine line = resolveTableLine(decor, decor.getColumnStyle(boundary), globalColumns);
            if (line == null) continue;
            int x = originX + grid.columnStart(boundary + 1) - halfColumn - line.width() / 2;
            for (int row = 0; row < grid.rows(); row++) {
                if (!decor.isLinesAcrossSpans() && grid.columnBoundaryBlocked(boundary, row)) continue;
                int y1 = originY + grid.rowStart(row) - (row > 0 ? halfRow : 0);
                int y2 = originY + grid.rowStart(row) + grid.rowHeights()[row]
                        + (row < grid.rows() - 1 ? grid.rowSpacing() - halfRow : 0);
                PeakPaintRenderer.fill(graphics, line.paint(), x, y1, x + line.width(), y2,
                        originX, originY, originX + grid.contentWidth(), originY + grid.contentHeight());
            }
        }
    }

    private static TableLine resolveTableLine(PeakTableStyle decor, PeakTrackStyle style, boolean globalEnabled) {
        int width;
        PeakPaint color;
        if (style != null && style.overridesLine()) {
            width = style.getLineWidth();
            color = style.getLineColor().isNone() ? decor.getLineColor() : style.getLineColor();
        } else if (globalEnabled) {
            width = decor.getLineWidth();
            color = decor.getLineColor();
        } else {
            return null;
        }
        if (width <= 0 || color.isNone()) return null;
        return new TableLine(width, color);
    }

    private static void fillTableRow(GuiGraphics graphics, PeakTableGrid.Result grid, int row, int originX, int originY, PeakPaint paint) {
        if (paint.isNone()) return;
        int halfRow = grid.rowSpacing() / 2;
        int top = originY + grid.rowStart(row) - (row > 0 ? halfRow : 0);
        int bottom = originY + grid.rowStart(row) + grid.rowHeights()[row]
                + (row < grid.rows() - 1 ? grid.rowSpacing() - halfRow : 0);
        PeakPaintRenderer.fill(graphics, paint, originX, top, originX + grid.contentWidth(), bottom);
    }

    private static void fillTableColumn(GuiGraphics graphics, PeakTableGrid.Result grid, int column, int originX, int originY, PeakPaint paint) {
        if (paint.isNone()) return;
        int halfColumn = grid.columnSpacing() / 2;
        int left = originX + grid.columnStart(column) - (column > 0 ? halfColumn : 0);
        int right = originX + grid.columnStart(column) + grid.columnWidths()[column]
                + (column < grid.columns() - 1 ? grid.columnSpacing() - halfColumn : 0);
        PeakPaintRenderer.fill(graphics, paint, left, originY, right, originY + grid.contentHeight());
    }

    private record TableLayout(List<PeakElement<?>> cells, PeakTableGrid.Result grid) {}
    private record TableLine(int width, PeakPaint paint) {}

    private static TableLayout computeTableLayout(Font font, PeakTableLayoutPanel table, int availableWidth, int availableHeight) {
        List<PeakElement<?>> relevant = new ArrayList<>();
        List<PeakTableGrid.Cell> cells = new ArrayList<>();
        PeakInsets padding = table.getCellPadding();
        int savedBaseWidth = percentBaseWidth;
        int savedBaseHeight = percentBaseHeight;
        percentBaseWidth = -1;
        percentBaseHeight = -1;
        try {
            for (PeakElement<?> child : table.getChildren()) {
                if (isCollapsed(child) || child.getPositionMode() == PeakPositionMode.ABSOLUTE) continue;
                PeakPixelSize childFootprint = footprint(font, child);
                PeakInsets margin = child.getStyle().getMargin();
                int width = childFootprint.width() + margin.getLeft() + margin.getRight() + padding.getLeft() + padding.getRight();
                int height = childFootprint.height() + margin.getTop() + margin.getBottom() + padding.getTop() + padding.getBottom();
                relevant.add(child);
                cells.add(new PeakTableGrid.Cell(child.getColumnSpan(), child.getRowSpan(), width, height));
            }
        } finally {
            percentBaseWidth = savedBaseWidth;
            percentBaseHeight = savedBaseHeight;
        }
        PeakTableGrid.Result grid = PeakTableGrid.solve(table.getColumns(), table.getRows(), table.getRowHeight(), cells,
                table.getColumnSpacing(), table.getRowSpacing(), availableWidth, availableHeight, table.getMinColumnWidth());
        return new TableLayout(relevant, grid);
    }

    private static PeakPoint resolveAbsolutePosition(PeakElement<?> element, int boxOriginX, int boxOriginY,
                                                 int boxWidth, int boxHeight, PeakPixelSize elementSize) {
        PeakPosition pos = element.getPosition();

        int baseX = switch (pos.getAnchor()) {
            case TOP_LEFT, CENTER_LEFT, BOTTOM_LEFT -> boxOriginX;
            case TOP_CENTER, CENTER, BOTTOM_CENTER -> boxOriginX + boxWidth / 2;
            case TOP_RIGHT, CENTER_RIGHT, BOTTOM_RIGHT -> boxOriginX + boxWidth;
        };
        int baseY = switch (pos.getAnchor()) {
            case TOP_LEFT, TOP_CENTER, TOP_RIGHT -> boxOriginY;
            case CENTER_LEFT, CENTER, CENTER_RIGHT -> boxOriginY + boxHeight / 2;
            case BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT -> boxOriginY + boxHeight;
        };

        int x = baseX + pos.getOffsetX();
        switch (resolveHorizontalAlign(element)) {
            case CENTER -> x -= elementSize.width() / 2;
            case END -> x -= elementSize.width();
            case START, AUTO -> { }
        }

        int y = baseY + pos.getOffsetY();
        switch (resolveVerticalAlign(element)) {
            case CENTER -> y -= elementSize.height() / 2;
            case END -> y -= elementSize.height();
            case START, AUTO -> { }
        }

        return new PeakPoint(x, y);
    }

    private static PeakAlign resolveHorizontalAlign(PeakElement<?> element) {
        if (element.getAlignSelf() != PeakAlign.AUTO) return element.getAlignSelf();
        return switch (element.getPosition().getAnchor()) {
            case TOP_LEFT, CENTER_LEFT, BOTTOM_LEFT -> PeakAlign.START;
            case TOP_CENTER, CENTER, BOTTOM_CENTER -> PeakAlign.CENTER;
            case TOP_RIGHT, CENTER_RIGHT, BOTTOM_RIGHT -> PeakAlign.END;
        };
    }

    private static PeakAlign resolveVerticalAlign(PeakElement<?> element) {
        if (element.getVerticalAlign() != PeakAlign.AUTO) return element.getVerticalAlign();
        return switch (element.getPosition().getAnchor()) {
            case TOP_LEFT, TOP_CENTER, TOP_RIGHT -> PeakAlign.START;
            case CENTER_LEFT, CENTER, CENTER_RIGHT -> PeakAlign.CENTER;
            case BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT -> PeakAlign.END;
        };
    }

    static PeakAlign resolveContainerAlign(PeakAlign align) {
        return align == PeakAlign.AUTO ? PeakAlign.START : align;
    }

    static void blitNineSlice(GuiGraphics graphics, ResourceLocation texture,
                                      int x, int y, int width, int height,
                                      int texWidth, int texHeight, int corner) {
        blitNineSlice(graphics, texture, x, y, width, height, texWidth, texHeight, corner, false);
    }

    static void blitNineSlice(GuiGraphics graphics, ResourceLocation texture,
                                      int x, int y, int width, int height,
                                      int texWidth, int texHeight, int corner, boolean tile) {
        int cornerSize = Math.max(0, Math.min(corner, Math.min(texWidth, texHeight) / 2));
        int middleSourceWidth = Math.max(1, texWidth - cornerSize * 2);
        int middleSourceHeight = Math.max(1, texHeight - cornerSize * 2);
        int middleDestWidth = Math.max(0, width - cornerSize * 2);
        int middleDestHeight = Math.max(0, height - cornerSize * 2);

        PeakImmediateRender.blit(graphics, texture, x, y, 0, 0, cornerSize, cornerSize, texWidth, texHeight);
        PeakImmediateRender.blit(graphics, texture, x + width - cornerSize, y, texWidth - cornerSize, 0, cornerSize, cornerSize, texWidth, texHeight);
        PeakImmediateRender.blit(graphics, texture, x, y + height - cornerSize, 0, texHeight - cornerSize, cornerSize, cornerSize, texWidth, texHeight);
        PeakImmediateRender.blit(graphics, texture, x + width - cornerSize, y + height - cornerSize, texWidth - cornerSize, texHeight - cornerSize, cornerSize, cornerSize, texWidth, texHeight);

        if (middleDestWidth > 0) {
            blitRegion(graphics, texture, x + cornerSize, y, middleDestWidth, cornerSize, cornerSize, 0, middleSourceWidth, cornerSize, texWidth, texHeight, tile);
            blitRegion(graphics, texture, x + cornerSize, y + height - cornerSize, middleDestWidth, cornerSize, cornerSize, texHeight - cornerSize, middleSourceWidth, cornerSize, texWidth, texHeight, tile);
        }
        if (middleDestHeight > 0) {
            blitRegion(graphics, texture, x, y + cornerSize, cornerSize, middleDestHeight, 0, cornerSize, cornerSize, middleSourceHeight, texWidth, texHeight, tile);
            blitRegion(graphics, texture, x + width - cornerSize, y + cornerSize, cornerSize, middleDestHeight, texWidth - cornerSize, cornerSize, cornerSize, middleSourceHeight, texWidth, texHeight, tile);
        }

        if (middleDestWidth > 0 && middleDestHeight > 0) {
            blitRegion(graphics, texture, x + cornerSize, y + cornerSize, middleDestWidth, middleDestHeight, cornerSize, cornerSize, middleSourceWidth, middleSourceHeight, texWidth, texHeight, tile);
        }
    }

    private static final int MAX_TILES = 1024;

    private static void blitRegion(GuiGraphics graphics, ResourceLocation texture, int destX, int destY, int destWidth, int destHeight,
                                   int srcX, int srcY, int srcWidth, int srcHeight, int texWidth, int texHeight, boolean tile) {
        if (srcWidth <= 0 || srcHeight <= 0 || destWidth <= 0 || destHeight <= 0) return;
        long tiles = (long) PeakTileSpans.count(destWidth, srcWidth) * PeakTileSpans.count(destHeight, srcHeight);
        if (!tile || tiles > MAX_TILES) {
            PeakImmediateRender.blit(graphics, texture, destX, destY, destWidth, destHeight, srcX, srcY, srcWidth, srcHeight, texWidth, texHeight);
            return;
        }
        for (PeakTileSpans.Span row : PeakTileSpans.centered(destHeight, srcHeight)) {
            for (PeakTileSpans.Span column : PeakTileSpans.centered(destWidth, srcWidth)) {
                PeakImmediateRender.blit(graphics, texture, destX + column.destOffset(), destY + row.destOffset(), column.length(), row.length(),
                        srcX + column.srcOffset(), srcY + row.srcOffset(), column.length(), row.length(), texWidth, texHeight);
            }
        }
    }
}
