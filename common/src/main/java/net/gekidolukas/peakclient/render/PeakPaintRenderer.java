package net.gekidolukas.peakclient.render;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.gekidolukas.peakclient.ui.PeakGradientDirection;
import net.gekidolukas.peakclient.ui.PeakLinearGradient;
import net.gekidolukas.peakclient.ui.PeakPaint;
import net.gekidolukas.peakclient.ui.PeakSolidColor;
import net.minecraft.client.gui.GuiGraphics;
import org.joml.Matrix4f;

public final class PeakPaintRenderer {
    private static final int MAX_STRIPS = 64;
    private static final int ARC_SEGMENTS = 8;
    private static final int MAX_GRID_CELLS = 40;

    private PeakPaintRenderer() {}

    public static boolean usesStrips(PeakPaint paint) {
        return paint instanceof PeakSolidColor || (paint instanceof PeakLinearGradient gradient && gradient.isAxisAligned());
    }

    public static boolean isHorizontal(PeakPaint paint) {
        return !(paint instanceof PeakLinearGradient gradient) || gradient.getDirection() != PeakGradientDirection.TOP_TO_BOTTOM;
    }

    public static int[] boundaryColors(PeakPaint paint, int length) {
        int segments = Math.max(1, Math.min(length, MAX_STRIPS));
        int[] colors = new int[segments + 1];
        for (int i = 0; i <= segments; i++) colors[i] = paint.colorAt((float) i / segments);
        return colors;
    }

    public static void fill(GuiGraphics graphics, PeakPaint paint, int x1, int y1, int x2, int y2) {
        fill(graphics, paint, x1, y1, x2, y2, x1, y1, x2, y2);
    }

    public static void fill(GuiGraphics graphics, PeakPaint paint, int x1, int y1, int x2, int y2,
                            int boxX1, int boxY1, int boxX2, int boxY2) {
        if (paint.isNone() || x2 <= x1 || y2 <= y1) return;
        if (paint instanceof PeakSolidColor solid) {
            PeakImmediateRender.fill(graphics, x1, y1, x2, y2, solid.argb());
            return;
        }
        if (!usesStrips(paint)) {
            fillGrid(graphics, paint, x1, y1, x2, y2, boxX1, boxY1, boxX2, boxY2);
            return;
        }
        Sampler source = new Sampler(paint, boxX1, boxY1, boxX2, boxY2);
        boolean horizontal = source.horizontal();
        int start = horizontal ? x1 : y1;
        int length = horizontal ? x2 - x1 : y2 - y1;
        int boxStart = horizontal ? boxX1 : boxY1;
        int boxLength = Math.max(1, horizontal ? boxX2 - boxX1 : boxY2 - boxY1);
        int segments = Math.max(1, Math.min(length, MAX_STRIPS));
        int[] colors = new int[segments + 1];
        for (int i = 0; i <= segments; i++) {
            float position = start + length * (float) i / segments;
            colors[i] = paint.colorAt((position - boxStart) / boxLength);
        }
        PeakImmediateRender.fillGradientStrips(graphics, x1, y1, x2, y2, horizontal, colors);
    }

    public static void fillRounded(GuiGraphics graphics, PeakPaint paint, int x1, int y1, int x2, int y2, int radius) {
        if (paint.isNone() || x2 <= x1 || y2 <= y1) return;
        if (radius <= 0) {
            fill(graphics, paint, x1, y1, x2, y2);
            return;
        }
        if (paint instanceof PeakSolidColor solid) {
            PeakRoundedRect.fillRounded(graphics, x1, y1, x2, y2, radius, solid.argb());
            return;
        }
        if (!usesStrips(paint)) {
            fillRoundedSampled(graphics, paint, x1, y1, x2, y2, radius, x1, y1, x2, y2);
            return;
        }
        boolean horizontal = isHorizontal(paint);
        int[] colors = boundaryColors(paint, horizontal ? x2 - x1 : y2 - y1);
        PeakRoundedRect.fillRoundedGradient(graphics, x1, y1, x2, y2, radius, false, horizontal, colors);
    }

    public static void fillRoundedWithBorder(GuiGraphics graphics, int x1, int y1, int x2, int y2, int outerRadius,
                                             int borderWidth, PeakPaint border, PeakPaint background) {
        int width = x2 - x1, height = y2 - y1;
        if (width <= 0 || height <= 0) return;
        int outer = Math.min(outerRadius, Math.min(width, height) / 2);
        int line = Math.min(borderWidth, Math.min(width, height) / 2);
        boolean hasBorder = line > 0 && !border.isNone();
        if (!hasBorder) {
            fillRounded(graphics, background, x1, y1, x2, y2, outer);
            return;
        }

        if (!background.isSolid() && !background.isNone()) {
            int inset = line;
            if (!usesStrips(background)) {
                int underlap = Math.max(0, inset - 1);
                fillRoundedSampled(graphics, background, x1 + underlap, y1 + underlap, x2 - underlap, y2 - underlap,
                        Math.max(0, outer - underlap), x1 + inset, y1 + inset, x2 - inset, y2 - inset);
                drawRing(graphics, x1, y1, x2, y2, outer, line, border, 0);
                return;
            }
            boolean horizontal = isHorizontal(background);
            int[] colors = boundaryColors(background, horizontal ? width - inset * 2 : height - inset * 2);
            PeakRoundedRect.fillRoundedGradient(graphics, x1 + inset, y1 + inset, x2 - inset, y2 - inset,
                    Math.max(0, outer - inset), true, horizontal, colors);
            drawRing(graphics, x1, y1, x2, y2, outer, line, border, 0);
            return;
        }

        int backgroundArgb = background instanceof PeakSolidColor solid ? solid.argb() : 0;
        if (border instanceof PeakSolidColor solid) {
            PeakRoundedRect.fillRoundedWithBorder(graphics, x1, y1, x2, y2, outer, line, solid.argb(), backgroundArgb);
        } else {
            drawRing(graphics, x1, y1, x2, y2, outer, line, border, backgroundArgb);
        }
    }

    public static void fillFeathered(GuiGraphics graphics, PeakPaint paint, int x1, int y1, int x2, int y2, int radius, int extent) {
        if (paint.isNone() || extent <= 0 || x2 <= x1 || y2 <= y1) return;
        if (paint instanceof PeakSolidColor solid) {
            PeakRoundedRect.fillFeathered(graphics, x1, y1, x2, y2, radius, extent, solid.argb());
            return;
        }
        Sampler source = new Sampler(paint, x1, y1, x2, y2);
        float cornerRadius = Math.max(0, Math.min(radius, Math.min(x2 - x1, y2 - y1) / 2f));

        Matrix4f matrix = graphics.pose().last().pose();
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);

        int segments = 8;
        int points = 4 * (segments + 1);
        float[] innerX = new float[points], innerY = new float[points];
        float[] outerX = new float[points], outerY = new float[points];
        PeakRoundedRect.buildOutline(x1, y1, x2, y2, cornerRadius, 0f, segments, innerX, innerY);

        float centerX = (x1 + x2) / 2f, centerY = (y1 + y2) / 2f;
        for (int i = 0; i < points; i++) {
            int next = (i + 1) % points;
            vertex(buffer, matrix, centerX, centerY, source, 1f);
            vertex(buffer, matrix, innerX[i], innerY[i], source, 1f);
            vertex(buffer, matrix, innerX[next], innerY[next], source, 1f);
        }

        for (int step = 0; step < extent; step++) {
            float innerAlpha = PeakRoundedRect.feather(step / (float) extent);
            float outerAlpha = PeakRoundedRect.feather((step + 1) / (float) extent);
            PeakRoundedRect.buildOutline(x1, y1, x2, y2, cornerRadius, step + 1, segments, outerX, outerY);
            for (int i = 0; i < points; i++) {
                int next = (i + 1) % points;
                vertex(buffer, matrix, innerX[i], innerY[i], source, innerAlpha);
                vertex(buffer, matrix, outerX[i], outerY[i], source, outerAlpha);
                vertex(buffer, matrix, outerX[next], outerY[next], source, outerAlpha);
                vertex(buffer, matrix, innerX[i], innerY[i], source, innerAlpha);
                vertex(buffer, matrix, outerX[next], outerY[next], source, outerAlpha);
                vertex(buffer, matrix, innerX[next], innerY[next], source, innerAlpha);
            }
            System.arraycopy(outerX, 0, innerX, 0, points);
            System.arraycopy(outerY, 0, innerY, 0, points);
        }

        PeakRoundedRect.upload(buffer);
    }

    public static void drawFrame(GuiGraphics graphics, PeakPaint paint, int width, int height, int lineWidth) {
        if (paint.isNone() || lineWidth <= 0) return;
        fill(graphics, paint, 0, 0, width, lineWidth, 0, 0, width, height);
        fill(graphics, paint, 0, height - lineWidth, width, height, 0, 0, width, height);
        fill(graphics, paint, 0, lineWidth, lineWidth, height - lineWidth, 0, 0, width, height);
        fill(graphics, paint, width - lineWidth, lineWidth, width, height - lineWidth, 0, 0, width, height);
    }

    private static void fillRoundedSampled(GuiGraphics graphics, PeakPaint paint, int x1, int y1, int x2, int y2, int radius,
                                           int boxX1, int boxY1, int boxX2, int boxY2) {
        int width = x2 - x1, height = y2 - y1;
        if (width <= 0 || height <= 0) return;
        int corner = Math.max(0, Math.min(radius, Math.min(width, height) / 2));
        Sampler source = new Sampler(paint, boxX1, boxY1, boxX2, boxY2);
        Matrix4f matrix = graphics.pose().last().pose();
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);

        addGrid(buffer, matrix, source, x1, y1 + corner, x2, y2 - corner);
        if (corner > 0) {
            addGrid(buffer, matrix, source, x1 + corner, y1, x2 - corner, y1 + corner);
            addGrid(buffer, matrix, source, x1 + corner, y2 - corner, x2 - corner, y2);
            addSampledCorner(buffer, matrix, source, x1 + corner, y1 + corner, corner, 180, 270);
            addSampledCorner(buffer, matrix, source, x2 - corner, y1 + corner, corner, 270, 360);
            addSampledCorner(buffer, matrix, source, x1 + corner, y2 - corner, corner, 90, 180);
            addSampledCorner(buffer, matrix, source, x2 - corner, y2 - corner, corner, 0, 90);
        }
        PeakRoundedRect.upload(buffer);
    }

    private static void addSampledCorner(BufferBuilder buffer, Matrix4f matrix, Sampler source, float cx, float cy, int radius,
                                         float fromDeg, float toDeg) {
        int segments = Math.max(8, Math.min(24, radius * 2));
        int rings = Math.max(1, Math.min(6, radius / 4));
        float half = PeakRoundedRect.featherHalf(matrix);
        float inner = radius - half;
        float outer = radius + half;
        for (int segment = 0; segment < segments; segment++) {
            float angle0 = (float) Math.toRadians(fromDeg + (toDeg - fromDeg) * segment / segments);
            float angle1 = (float) Math.toRadians(fromDeg + (toDeg - fromDeg) * (segment + 1) / segments);
            float cos0 = (float) Math.cos(angle0), sin0 = (float) Math.sin(angle0);
            float cos1 = (float) Math.cos(angle1), sin1 = (float) Math.sin(angle1);
            for (int ring = 0; ring < rings; ring++) {
                float from = inner * ring / rings, to = inner * (ring + 1) / rings;
                if (ring == 0) {
                    vertex(buffer, matrix, cx, cy, source, 1f);
                    vertex(buffer, matrix, cx + to * cos0, cy + to * sin0, source, 1f);
                    vertex(buffer, matrix, cx + to * cos1, cy + to * sin1, source, 1f);
                } else {
                    vertex(buffer, matrix, cx + from * cos0, cy + from * sin0, source, 1f);
                    vertex(buffer, matrix, cx + to * cos0, cy + to * sin0, source, 1f);
                    vertex(buffer, matrix, cx + to * cos1, cy + to * sin1, source, 1f);
                    vertex(buffer, matrix, cx + from * cos0, cy + from * sin0, source, 1f);
                    vertex(buffer, matrix, cx + to * cos1, cy + to * sin1, source, 1f);
                    vertex(buffer, matrix, cx + from * cos1, cy + from * sin1, source, 1f);
                }
            }
            vertex(buffer, matrix, cx + inner * cos0, cy + inner * sin0, source, 1f);
            vertex(buffer, matrix, cx + outer * cos0, cy + outer * sin0, source, 0f);
            vertex(buffer, matrix, cx + outer * cos1, cy + outer * sin1, source, 0f);
            vertex(buffer, matrix, cx + inner * cos0, cy + inner * sin0, source, 1f);
            vertex(buffer, matrix, cx + outer * cos1, cy + outer * sin1, source, 0f);
            vertex(buffer, matrix, cx + inner * cos1, cy + inner * sin1, source, 1f);
        }
    }

    private static void fillGrid(GuiGraphics graphics, PeakPaint paint, int x1, int y1, int x2, int y2,
                                 int boxX1, int boxY1, int boxX2, int boxY2) {
        Sampler source = new Sampler(paint, boxX1, boxY1, boxX2, boxY2);
        Matrix4f matrix = graphics.pose().last().pose();
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
        addGrid(buffer, matrix, source, x1, y1, x2, y2);
        PeakRoundedRect.upload(buffer);
    }

    private static void addGrid(BufferBuilder buffer, Matrix4f matrix, Sampler source, int x1, int y1, int x2, int y2) {
        if (x2 <= x1 || y2 <= y1) return;
        int columns = Math.max(1, Math.min(MAX_GRID_CELLS, (x2 - x1 + 1) / 2));
        int rows = Math.max(1, Math.min(MAX_GRID_CELLS, (y2 - y1 + 1) / 2));
        for (int row = 0; row < rows; row++) {
            int top = y1 + Math.round((float) row / rows * (y2 - y1));
            int bottom = y1 + Math.round((float) (row + 1) / rows * (y2 - y1));
            if (bottom <= top) continue;
            for (int column = 0; column < columns; column++) {
                int left = x1 + Math.round((float) column / columns * (x2 - x1));
                int right = x1 + Math.round((float) (column + 1) / columns * (x2 - x1));
                if (right <= left) continue;
                vertex(buffer, matrix, left, top, source, 1f);
                vertex(buffer, matrix, left, bottom, source, 1f);
                vertex(buffer, matrix, right, bottom, source, 1f);
                vertex(buffer, matrix, left, top, source, 1f);
                vertex(buffer, matrix, right, bottom, source, 1f);
                vertex(buffer, matrix, right, top, source, 1f);
            }
        }
    }

    private static void drawRing(GuiGraphics graphics, int x1, int y1, int x2, int y2, int outer, int line,
                                 PeakPaint border, int backgroundArgb) {
        Sampler source = new Sampler(border, x1, y1, x2, y2);
        Matrix4f matrix = graphics.pose().last().pose();
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);

        int inner = Math.max(0, outer - line);
        addQuad(buffer, matrix, x1 + outer, y1, x2 - outer, y1 + line, source);
        addQuad(buffer, matrix, x1 + outer, y2 - line, x2 - outer, y2, source);
        addQuad(buffer, matrix, x1, y1 + outer, x1 + line, y2 - outer, source);
        addQuad(buffer, matrix, x2 - line, y1 + outer, x2, y2 - outer, source);

        addQuarterRing(buffer, matrix, x1 + outer, y1 + outer, inner, outer, 180, 270, source);
        addQuarterRing(buffer, matrix, x2 - outer, y1 + outer, inner, outer, 270, 360, source);
        addQuarterRing(buffer, matrix, x1 + outer, y2 - outer, inner, outer, 90, 180, source);
        addQuarterRing(buffer, matrix, x2 - outer, y2 - outer, inner, outer, 0, 90, source);

        if (backgroundArgb != 0) {
            PeakRoundedRect.addFilledRoundedRegion(buffer, matrix, x1 + line, y1 + line, x2 - line, y2 - line, inner, backgroundArgb);
        }
        PeakRoundedRect.upload(buffer);
    }

    private static void addQuad(BufferBuilder buffer, Matrix4f matrix, int x1, int y1, int x2, int y2, Sampler source) {
        if (x2 <= x1 || y2 <= y1) return;
        if (source.constant()) {
            PeakRoundedRect.addQuad(buffer, matrix, x1, y1, x2, y2, source.at(x1, y1));
            return;
        }
        boolean horizontal = source.usesStrips() ? source.horizontal() : (x2 - x1) >= (y2 - y1);
        int start = horizontal ? x1 : y1;
        int length = horizontal ? x2 - x1 : y2 - y1;
        int strips = Math.max(1, Math.min(length, MAX_STRIPS));
        for (int strip = 0; strip < strips; strip++) {
            int from = start + Math.round((float) strip / strips * length);
            int to = start + Math.round((float) (strip + 1) / strips * length);
            if (to <= from) continue;
            int left = horizontal ? from : x1, right = horizontal ? to : x2;
            int top = horizontal ? y1 : from, bottom = horizontal ? y2 : to;
            vertex(buffer, matrix, left, top, source, 1f);
            vertex(buffer, matrix, left, bottom, source, 1f);
            vertex(buffer, matrix, right, bottom, source, 1f);
            vertex(buffer, matrix, left, top, source, 1f);
            vertex(buffer, matrix, right, bottom, source, 1f);
            vertex(buffer, matrix, right, top, source, 1f);
        }
    }

    private static void addQuarterRing(BufferBuilder buffer, Matrix4f matrix, float cx, float cy,
                                       float innerRadius, float outerRadius, float fromDeg, float toDeg, Sampler source) {
        for (int i = 0; i < ARC_SEGMENTS; i++) {
            float startAngle = (float) Math.toRadians(fromDeg + (toDeg - fromDeg) * i / ARC_SEGMENTS);
            float endAngle = (float) Math.toRadians(fromDeg + (toDeg - fromDeg) * (i + 1) / ARC_SEGMENTS);
            float outerStartX = cx + outerRadius * (float) Math.cos(startAngle), outerStartY = cy + outerRadius * (float) Math.sin(startAngle);
            float outerEndX = cx + outerRadius * (float) Math.cos(endAngle), outerEndY = cy + outerRadius * (float) Math.sin(endAngle);
            float innerStartX = cx + innerRadius * (float) Math.cos(startAngle), innerStartY = cy + innerRadius * (float) Math.sin(startAngle);
            float innerEndX = cx + innerRadius * (float) Math.cos(endAngle), innerEndY = cy + innerRadius * (float) Math.sin(endAngle);

            vertex(buffer, matrix, innerStartX, innerStartY, source, 1f);
            vertex(buffer, matrix, outerStartX, outerStartY, source, 1f);
            vertex(buffer, matrix, outerEndX, outerEndY, source, 1f);
            vertex(buffer, matrix, innerStartX, innerStartY, source, 1f);
            vertex(buffer, matrix, outerEndX, outerEndY, source, 1f);
            vertex(buffer, matrix, innerEndX, innerEndY, source, 1f);
        }
    }

    static void vertex(BufferBuilder buffer, Matrix4f matrix, float x, float y, Sampler source, float alphaFactor) {
        int argb = source.at(x, y);
        float alpha = ((argb >>> 24) & 0xFF) / 255f * alphaFactor;
        buffer.addVertex(matrix, x, y, 0).setColor(
                ((argb >> 16) & 0xFF) / 255f, ((argb >> 8) & 0xFF) / 255f, (argb & 0xFF) / 255f, alpha);
    }

    public static Sampler sampler(PeakPaint paint, int boxX1, int boxY1, int boxX2, int boxY2) {
        return new Sampler(paint, boxX1, boxY1, boxX2, boxY2);
    }

    public record Sampler(PeakPaint paint, int boxX1, int boxY1, int boxX2, int boxY2) {
        public void vertex(BufferBuilder buffer, Matrix4f matrix, float x, float y) {
            PeakPaintRenderer.vertex(buffer, matrix, x, y, this, 1f);
        }

        public boolean constant() {
            return paint.isSolid();
        }

        boolean horizontal() {
            return isHorizontal(paint);
        }

        boolean usesStrips() {
            return PeakPaintRenderer.usesStrips(paint);
        }

        public int at(float x, float y) {
            if (paint instanceof PeakSolidColor solid) return solid.argb();
            float width = Math.max(1, boxX2 - boxX1);
            float height = Math.max(1, boxY2 - boxY1);
            return paint.colorAt((x - boxX1) / width, (y - boxY1) / height, width / height);
        }
    }
}
