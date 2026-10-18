package net.gekidolukas.peakclient;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** Fails when rendering code draws through GuiGraphics instead of PeakImmediateRender.
 *  Raw fill/blit/drawString calls are deferred by GuiGraphics and break the draw order. */
class PeakRawDrawCallsTest {
    private static final Pattern RAW_CALL = Pattern.compile(
            "\\bgraphics(\\(\\))?\\.(fill|blit|drawString|drawCenteredString|hLine|vLine|fillGradient|blitSprite|renderOutline|renderTooltip)\\(");
    private static final Pattern RAW_TOOLTIP_UTIL = Pattern.compile("TooltipRenderUtil");
    private static final List<String> SCANNED_PACKAGES = List.of("render", "debug");

    private static Path sourceRoot() {
        for (String candidate : List.of("src/main/java/net/gekidolukas/peakclient", "common/src/main/java/net/gekidolukas/peakclient")) {
            Path path = Paths.get(candidate);
            if (Files.isDirectory(path)) return path;
        }
        throw new IllegalStateException("Source root not found from " + Paths.get("").toAbsolutePath());
    }

    @Test
    void renderingCodeDoesNotCallGuiGraphicsDrawMethodsDirectly() throws IOException {
        Path root = sourceRoot();
        List<String> violations = new ArrayList<>();
        for (String packageName : SCANNED_PACKAGES) {
            Path directory = root.resolve(packageName);
            if (!Files.isDirectory(directory)) continue;
            try (Stream<Path> files = Files.walk(directory)) {
                for (Path file : files.filter(p -> p.toString().endsWith(".java")).toList()) {
                    List<String> lines = Files.readAllLines(file);
                    for (int i = 0; i < lines.size(); i++) {
                        String line = lines.get(i);
                        if (RAW_CALL.matcher(line).find() || RAW_TOOLTIP_UTIL.matcher(line).find()) {
                            violations.add(root.relativize(file) + ":" + (i + 1) + " " + line.trim());
                        }
                    }
                }
            }
        }
        assertTrue(violations.isEmpty(), "Raw GuiGraphics draw calls found:\n" + String.join("\n", violations));
    }
}
