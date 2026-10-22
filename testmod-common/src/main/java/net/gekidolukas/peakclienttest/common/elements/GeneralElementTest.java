package net.gekidolukas.peakclienttest.common.elements;

import net.gekidolukas.peakclient.api.PeakClient;
import net.gekidolukas.peakclient.helper.PeakCustomValueWatcher;
import net.gekidolukas.peakclient.server.PeakValueKey;
import net.gekidolukas.peakclient.ui.*;
import net.gekidolukas.peakclient.ui.patch.PeakPatch;
import net.gekidolukas.peakclienttest.common.PeakElementTest;
import net.gekidolukas.peakclienttest.common.PeakElementTestStep;
import net.gekidolukas.peakclienttest.common.PeakTestTimeline;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class GeneralElementTest implements PeakElementTest {
    private static final String STEVE = "minecraft:textures/entity/player/wide/steve.png";
    private static final String ALEX = "minecraft:textures/entity/player/slim/alex.png";
    private static final int LIVE_DURATION_TICKS = 240;

    @Override public String name() { return "general"; }
    @Override public String description() { return "Features of every element: placeholders and bindings, visibility, layout, style, opacity, rotation, scale, draw order"; }

    private static PeakPanel row(int x, int y) {
        return PeakPanel.create().row().gap(14).background(PeakPaint.NONE).absolute(PeakAnchor.TOP_LEFT, x, y);
    }

    private static PeakPanel box(int width, int height, int color) {
        return PeakPanel.create().size(width, height).background(color);
    }

    private static PeakPanel box(int width, int height, PeakStyle style) {
        return PeakPanel.create().style(style).size(width, height);
    }

    private static PeakStyle rounded(int background) {
        return PeakStyle.create().background(background).borderRadius(10).borderWidth(3).borderColor(0xFFFF5555);
    }

    private static PeakElementTestStep step(String label, int ticks, Consumer<PeakDocument> content) {
        return PeakElementTestStep.of(label, ticks, (player, docId) -> {
            PeakDocument doc = PeakDocument.create(docId);
            content.accept(doc);
            PeakClient.to(player).sendUi(doc);
        });
    }

    private static PeakElementTestStep step(String label, Consumer<PeakDocument> content) {
        return step(label, 120, content);
    }

    private static void watchTemporarily(ServerPlayer player, String key, Supplier<String> supplier) {
        AtomicReference<PeakCustomValueWatcher<String>> holder = new AtomicReference<>();
        long start = player.level().getGameTime();
        holder.set(PeakCustomValueWatcher.watch(PeakValueKey.ofString("peaktest:" + key), PeakClient.to(player), () -> {
            if (player.level().getGameTime() - start > LIVE_DURATION_TICKS + 20) holder.get().stop();
            return supplier.get();
        }).intervalTicks(5));
    }

    private static PeakPanel layoutPanel() {
        return PeakPanel.create().absolute(PeakAnchor.CENTER, 0, 0).size(150, 80)
                .background(PeakPaint.rgba(30, 30, 40, 200)).padding(6);
    }

    @Override
    public List<PeakElementTestStep> steps() {
        List<PeakElementTestStep> steps = new ArrayList<>();

        steps.add(PeakElementTestStep.of("bindings 1: texture and UUID placeholders (icon, chibi, entity, composed path)", 120, (player, docId) -> {
            PeakClient.to(player).setValue("peaktest:skin", ALEX);
            PeakClient.to(player).setValue("peaktest:uuid", player.getUUID().toString());
            PeakClient.to(player).setValue("peaktest:model", "slim");
            PeakClient.to(player).setValue("peaktest:name", "alex");
            PeakDocument doc = PeakDocument.create(docId);
            PeakPanel first = row(20, 20);
            first.add(PeakPlayerIcon.ofTexture("{peaktest:skin}", true).view(PeakPlayerIconView.UPPER_BODY).size(48, 48));
            first.add(PeakChibiPlayer.ofTexture("{peaktest:skin}", true));
            first.add(PeakEntityElement.playerTexture("{peaktest:skin}", true).size(48, 64));
            doc.add(first);
            PeakPanel second = row(20, 140);
            second.add(PeakPlayerIcon.ofTexture("minecraft:textures/entity/player/{peaktest:model}/{peaktest:name}.png", true).size(48, 48));
            doc.add(second);
            PeakClient.to(player).sendUi(doc);
        }));
        steps.add(PeakElementTestStep.of("bindings 2: typed fields: opacity (left), color and radius (middle), hidden=false (right)", 120, (player, docId) -> {
            PeakClient.to(player).setValue("peaktest:opacity", "0.4");
            PeakClient.to(player).setValue("peaktest:hidden", "false");
            PeakClient.to(player).setValue("peaktest:color", "#55FF55");
            PeakClient.to(player).setValue("peaktest:radius", "14");
            PeakDocument doc = PeakDocument.create(docId);
            PeakPanel row = row(20, 20);
            row.add(box(80, 50, 0xFFFF5555).bind("opacity", "{peaktest:opacity}"));
            row.add(box(80, 50, 0xFFFF5555)
                    .bind("style.background", "{peaktest:color}")
                    .bind("style.borderRadius", "{peaktest:radius}"));
            row.add(box(80, 50, 0xFF5555FF).bind("hidden", "{peaktest:hidden}"));
            doc.add(row);
            PeakClient.to(player).sendUi(doc);
        }));
        steps.add(PeakElementTestStep.of("bindings 3: errors: missing keeps base 0.8, #fallback gives 0.3, invalid keeps base 0.6; icons: nothing, then steve", 120, (player, docId) -> {
            PeakClient.to(player).clearValues("peaktest");
            PeakClient.to(player).setValue("peaktest:broken", "not-a-number");
            PeakDocument doc = PeakDocument.create(docId);
            PeakPanel row = row(20, 20);
            row.add(box(80, 50, 0xFFFF5555).opacity(0.8f).bind("opacity", "{peaktest:missing}"));
            row.add(box(80, 50, 0xFFFF5555).bind("opacity", "{peaktest:missing#0.3}"));
            row.add(box(80, 50, 0xFFFF5555).opacity(0.6f).bind("opacity", "{peaktest:broken}"));
            row.add(PeakPlayerIcon.ofTexture("{peaktest:missing}").size(48, 48));
            row.add(PeakPlayerIcon.ofTexture("{peaktest:missing#" + STEVE + "}").size(48, 48));
            doc.add(row);
            PeakClient.to(player).sendUi(doc);
        }));
        steps.add(PeakElementTestStep.of("bindings 4: live change every 40 ticks without resending the document", LIVE_DURATION_TICKS, (player, docId) -> {
            PeakClient.to(player).setValue("peaktest:skin", STEVE);
            PeakClient.to(player).setValue("peaktest:color", "#FF5555");
            PeakDocument doc = PeakDocument.create(docId);
            PeakPanel row = row(20, 20);
            row.add(PeakPlayerIcon.ofTexture("{peaktest:skin}").view(PeakPlayerIconView.UPPER_BODY).size(64, 64));
            row.add(box(80, 50, 0xFF000000).bind("style.background", "{peaktest:color}"));
            doc.add(row);
            PeakClient.to(player).sendUi(doc);
            long start = player.level().getGameTime();
            watchTemporarily(player, "skin", () -> ((player.level().getGameTime() - start) / 40) % 2 == 0 ? STEVE : ALEX);
            watchTemporarily(player, "color", () -> ((player.level().getGameTime() - start) / 40) % 2 == 0 ? "#FF5555" : "#5555FF");
        }));

        steps.add(PeakElementTestStep.of("bindings 5: both boxes follow the value 0.4; after 2 s the left one is patched to 1.0 and leaves the binding, after 4 s the value becomes 0.15 and only the right box follows", 160, (player, docId) -> {
            PeakClient.to(player).setValue("peaktest:opacity", "0.4");
            PeakDocument doc = PeakDocument.create(docId);
            PeakPanel row = row(20, 20);
            row.add(PeakPanel.create("peaktest:general/bound-left").size(80, 50).background(0xFFFF5555).bind("opacity", "{peaktest:opacity}"));
            row.add(PeakPanel.create("peaktest:general/bound-right").size(80, 50).background(0xFF5555FF).bind("opacity", "{peaktest:opacity}"));
            doc.add(row);
            PeakClient.to(player).sendUi(doc);
            PeakTestTimeline.create(player)
                    .at(40, () -> PeakClient.to(player).updateElement(docId, "peaktest:general/bound-left", PeakPatch.panel().opacity(1f)))
                    .at(80, () -> PeakClient.to(player).setValue("peaktest:opacity", "0.15"))
                    .at(120, () -> {})
                    .start();
        }));

        steps.add(step("visibility: hidden with collapseWhenHidden=true (default), 'Below' moves up", 60, doc -> {
            PeakPanel panel = PeakPanel.create().absolute(PeakAnchor.CENTER, 0, 0);
            panel.add(PeakLabel.create("Above"), PeakLabel.create("Hidden line").hidden(true), PeakLabel.create("Below"));
            doc.add(panel);
        }));
        steps.add(step("visibility: hidden with collapseWhenHidden=false, 'Below' stays in place", 60, doc -> {
            PeakPanel panel = PeakPanel.create().absolute(PeakAnchor.CENTER, 0, 0);
            panel.add(PeakLabel.create("Above"), PeakLabel.create("Hidden line").hidden(true).collapseWhenHidden(false), PeakLabel.create("Below"));
            doc.add(panel);
        }));

        steps.add(step("layout: COLUMN direction (default)", 60, doc ->
                doc.add(layoutPanel().column().add(PeakLabel.create("Line 1"), PeakLabel.create("Line 2"), PeakLabel.create("Line 3")))));
        steps.add(step("layout: ROW direction", 60, doc ->
                doc.add(layoutPanel().row().add(PeakLabel.create("A"), PeakLabel.create("B"), PeakLabel.create("C")))));
        steps.add(step("layout: alignChildren CENTER", 60, doc ->
                doc.add(layoutPanel().alignChildren(PeakAlign.CENTER).add(PeakLabel.create("Short"), PeakLabel.create("A much longer line")))));
        steps.add(step("layout: alignChildren END", 60, doc ->
                doc.add(layoutPanel().alignChildren(PeakAlign.END).add(PeakLabel.create("Short"), PeakLabel.create("A much longer line")))));
        steps.add(step("layout: gap 10 between children", 60, doc ->
                doc.add(layoutPanel().gap(10).add(PeakLabel.create("Line 1"), PeakLabel.create("Line 2"), PeakLabel.create("Line 3")))));
        steps.add(step("layout: padding 2 (left) vs 12 (right) and margin 6 on the second label", 60, doc -> {
            PeakPanel row = row(20, 20);
            row.add(box(80, 40, 0xFF303050).padding(2).add(PeakLabel.create("Pad 2")));
            row.add(box(80, 40, 0xFF303050).padding(12).add(PeakLabel.create("Pad 12")));
            row.add(box(80, 40, 0xFF303050).add(PeakLabel.create("A"), PeakLabel.create("Margin 6").margin(6)));
            doc.add(row);
        }));
        steps.add(step("layout: alignSelf END on the second label in a COLUMN", 60, doc ->
                doc.add(layoutPanel().add(PeakLabel.create("Default"), PeakLabel.create("alignSelf END").alignSelf(PeakAlign.END)))));
        for (PeakMainAxisAlign align : PeakMainAxisAlign.values()) {
            steps.add(step("layout: mainAxisAlign " + align.name(), 60, doc -> {
                PeakPanel row = PeakPanel.create().row().mainAxisAlign(align).absolute(PeakAnchor.CENTER, 0, 0)
                        .size(200, 30).background(PeakPaint.rgba(0, 0, 0, 140)).border(1, PeakPaint.WHITE);
                row.add(PeakLabel.create("A"), PeakLabel.create("B"), PeakLabel.create("C"));
                doc.add(row);
            }));
        }
        steps.add(step("size: minSize 100x30 holds a small panel open, maxSize 60x20 cuts a large one", 60, doc -> {
            PeakPanel row = row(20, 20);
            row.add(box(20, 10, 0xFF55AA55).minSize(100, 30).add(PeakLabel.create("min")));
            row.add(box(200, 100, 0xFFAA5555).maxSize(60, 20).add(PeakLabel.create("max")));
            doc.add(row);
        }));

        steps.add(step("style: borderRadius with an opaque background (baseline)", 200, doc -> {
            PeakPanel row = row(20, 20);
            row.add(box(80, 50, rounded(0xFF203040)));
            doc.add(row);
        }));
        steps.add(step("style: borderRadius with a translucent background (left) vs. square translucent (right)", 200, doc -> {
            PeakPanel row = row(20, 20);
            row.add(box(80, 50, rounded(0x80203040)));
            row.add(box(80, 50, PeakStyle.create().background(0x80203040).borderWidth(3).borderColor(0xFFFF5555)));
            doc.add(row);
        }));
        steps.add(step("style: gradients, top row horizontal (rounded+border, square+border, 3 colors rounded), bottom row vertical", 200, doc -> {
            PeakPanel top = row(20, 20);
            top.add(box(80, 50, rounded(0xFF203040).background(PeakPaint.gradient(0xFF55FFFF, 0xFFFF55FF))));
            top.add(box(80, 50, PeakStyle.create().borderWidth(3).borderColor(0xFFFF5555)
                    .background(PeakPaint.gradient(0xFF55FFFF, 0xFFFF55FF))));
            top.add(box(80, 50, PeakStyle.create().borderRadius(14)
                    .background(PeakPaint.gradient(0xFFFF5555, 0xFFFFFF55, 0xFF55FF55))));
            doc.add(top);
            PeakPanel bottom = row(20, 100);
            bottom.add(box(80, 50, rounded(0xFF203040)
                    .background(PeakPaint.gradient(0xFF55FFFF, 0xFFFF55FF).direction(PeakGradientDirection.TOP_TO_BOTTOM))));
            bottom.add(box(80, 50, PeakStyle.create().borderWidth(3).borderColor(0xFFFF5555)
                    .background(PeakPaint.gradient(0xFF55FFFF, 0xFFFF55FF).direction(PeakGradientDirection.TOP_TO_BOTTOM))));
            bottom.add(box(80, 50, PeakStyle.create().borderRadius(14)
                    .background(PeakPaint.gradient(0xFFFF5555, 0xFFFFFF55, 0xFF55FF55).direction(PeakGradientDirection.TOP_TO_BOTTOM))));
            doc.add(bottom);
        }));
        steps.add(step("style: animated background gradient must loop without a jump", 200, doc -> {
            PeakPanel row = row(20, 20);
            row.add(box(160, 40, PeakStyle.create()
                    .background(PeakPaint.gradient(0xFFFF5555, 0xFFFFFF55, 0xFF55FF55).animated(40))));
            doc.add(row);
        }));
        steps.add(step("style: boxShadow blur 0 / 4 / 8, top square, bottom rounded", 200, doc -> {
            PeakPanel top = row(30, 30);
            PeakPanel bottom = row(30, 110);
            for (int blur : new int[] { 0, 4, 8 }) {
                top.add(box(60, 40, PeakStyle.create().background(0xFF4466AA).boxShadow(0x80000000, 3, 3, blur)));
                bottom.add(box(60, 40, PeakStyle.create().background(0xFF4466AA).borderRadius(10).boxShadow(0x80000000, 3, 3, blur)));
            }
            doc.add(top);
            doc.add(bottom);
        }));
        steps.add(step("style: border corners are seamless on square and rounded panels, thickness 1 / 3 / 6", 200, doc -> {
            PeakPanel top = row(30, 30);
            PeakPanel bottom = row(30, 110);
            for (int thickness : new int[] { 1, 3, 6 }) {
                top.add(box(60, 40, 0xFF203040).border(thickness, 0xFFFFAA00));
                bottom.add(box(60, 40, 0xFF203040).border(thickness, 0xFFFFAA00).borderRadius(10));
            }
            doc.add(top);
            doc.add(bottom);
        }));
        steps.add(step("style: overflow HIDDEN clips at the inner edge of the border (right) vs. visible (left)", 200, doc -> {
            PeakPanel row = row(30, 30).gap(50);
            for (PeakOverflow overflow : PeakOverflow.values()) {
                PeakPanel container = box(60, 60, 0xFF223322).border(3, 0xFF55FF55).overflow(overflow);
                container.add(box(90, 90, PeakPaint.RED.withAlpha(100 / 255f).argb()));
                row.add(container);
            }
            doc.add(row);
        }));
        steps.add(step("style: rotationDegrees 45 inside a tight overflow HIDDEN container (right) vs. unrotated (left)", 200, doc -> {
            PeakPanel row = row(30, 30);
            for (float degrees : new float[] { 0f, 45f }) {
                PeakPanel container = box(60, 60, PeakStyle.create().background(0xFF223322).borderWidth(1).borderColor(0xFF55FF55))
                        .overflow(PeakOverflow.HIDDEN);
                container.add(box(60, 60, 0xFFFFAA00).rotationDegrees(degrees));
                row.add(container);
            }
            doc.add(row);
            PeakPanel angles = row(30, 130);
            for (float degrees : new float[] { 0f, 15f, 90f }) {
                angles.add(box(40, 40, 0xFF55AAFF).rotationDegrees(degrees));
            }
            doc.add(angles);
        }));
        steps.add(step("style: scale 1 / 1.5 / 2 on a labeled panel", 120, doc -> {
            PeakPanel row = row(20, 20);
            for (float scale : new float[] { 1f, 1.5f, 2f }) {
                row.add(box(50, 24, 0xFF304060).scale(scale).add(PeakLabel.create("x" + scale)));
            }
            doc.add(row);
        }));

        steps.add(step("opacity 0.5 on panel, zombie, player copy, chibi, player icon, item, block (all half transparent)", 200, doc -> {
            PeakPanel backdrop = box(420, 110, 0xFFDDDDDD).row().gap(10).absolute(PeakAnchor.TOP_LEFT, 10, 10);
            backdrop.add(box(40, 60, 0xFFCC2222).opacity(0.5f),
                    PeakEntityElement.fixed("minecraft:zombie").size(40, 80).opacity(0.5f),
                    PeakEntityElement.player().copyMode(PeakEntityCopyMode.TYPE_AND_SKIN).size(40, 80).opacity(0.5f),
                    PeakChibiPlayer.clientPlayer().size(40, 60).opacity(0.5f),
                    PeakPlayerIcon.clientPlayer().view(PeakPlayerIconView.FULL_BODY).size(30, 60).opacity(0.5f),
                    PeakItemElement.fixed(new ItemStack(Items.DIAMOND_SWORD)).size(32, 32).opacity(0.5f),
                    PeakBlockElement.fixed("minecraft:diamond_block").size(32, 32).opacity(0.5f));
            doc.add(backdrop);
        }));
        steps.add(step("opacity 0.5 inside overflow HIDDEN (clipped at the border edge, half transparent, no visible buffer rectangle)", 200, doc -> {
            PeakPanel backdrop = box(420, 120, 0xFFDDDDDD).row().gap(10).absolute(PeakAnchor.TOP_LEFT, 10, 10);
            PeakElement<?>[] content = {
                    PeakEntityElement.fixed("minecraft:zombie").size(40, 80).opacity(0.5f),
                    PeakChibiPlayer.clientPlayer().size(40, 80).opacity(0.5f),
                    PeakEntityElement.player().copyMode(PeakEntityCopyMode.TYPE_AND_SKIN).size(40, 80).opacity(0.5f),
                    PeakBlockElement.fixed("minecraft:diamond_block").size(64, 64).opacity(0.5f)
            };
            for (PeakElement<?> element : content) {
                PeakPanel container = box(50, 50, 0xFF223322).border(1, 0xFF55FF55).overflow(PeakOverflow.HIDDEN);
                container.add(element);
                backdrop.add(container);
            }
            doc.add(backdrop);
        }));
        steps.add(step("opacity 0.5 on a container applies to its children together (left) vs. opacity on each child (right)", 120, doc -> {
            PeakPanel backdrop = box(300, 90, 0xFFDDDDDD).row().gap(20).padding(10).absolute(PeakAnchor.TOP_LEFT, 10, 10);
            PeakPanel together = box(100, 60, 0xFF2255AA).opacity(0.5f);
            together.add(box(60, 30, 0xFFAA2222));
            PeakPanel separate = box(100, 60, 0xFF2255AA);
            separate.add(box(60, 30, 0xFFAA2222).opacity(0.5f));
            backdrop.add(together, separate);
            doc.add(backdrop);
        }));

        steps.add(step("draw order: a label lies in front of a panel background", 60, doc -> {
            doc.add(PeakLabel.create("Visible text").color(0xFFFFFF).absolute(PeakAnchor.TOP_LEFT, 10, 10));
            doc.add(box(256, 64, 0xFF000000).absolute(PeakAnchor.TOP_LEFT, 10, 10));
        }));
        steps.add(step("draw order: an image lies above (left) or below (right) a panel, in the order of the document", 60, doc -> {
            doc.add(box(64, 64, 0xFFFF0000).absolute(PeakAnchor.TOP_LEFT, 10, 10));
            doc.add(PeakImage.create("minecraft:textures/item/diamond.png").absolute(PeakAnchor.TOP_LEFT, 24, 24).size(32, 32));
            doc.add(PeakImage.create("minecraft:textures/item/diamond.png").absolute(PeakAnchor.TOP_LEFT, 110, 24).size(32, 32));
            doc.add(box(64, 64, 0xFF0000FF).absolute(PeakAnchor.TOP_LEFT, 100, 10));
        }));
        steps.add(step("draw order: scaling FIXED places an element at the same screen position as the default scaling", 120, doc -> {
            doc.scaling(PeakScaling.FIXED);
            doc.add(PeakLabel.create("FIXED scaling document").absolute(PeakAnchor.TOP_CENTER, 0, 10));
        }));

        return steps;
    }
}
