package net.gekidolukas.peakclienttest.common.menus;

import net.gekidolukas.peakclient.api.PeakClient;
import net.gekidolukas.peakclient.ui.PeakAction;
import net.gekidolukas.peakclient.ui.PeakCheckbox;
import net.gekidolukas.peakclient.ui.PeakDocument;
import net.gekidolukas.peakclient.ui.PeakFocusStyle;
import net.gekidolukas.peakclient.ui.PeakPaint;
import net.gekidolukas.peakclient.ui.PeakGradientDirection;
import net.gekidolukas.peakclient.ui.PeakMenu;
import net.gekidolukas.peakclient.ui.PeakMenuSettings;
import net.gekidolukas.peakclient.ui.PeakObjectFit;
import net.gekidolukas.peakclient.ui.PeakRichTextBox;
import net.gekidolukas.peakclient.ui.PeakSlider;
import net.gekidolukas.peakclient.ui.PeakTextBox;
import net.gekidolukas.peakclient.ui.PeakTooltipStyle;
import net.gekidolukas.peakclienttest.common.PeakMenuChecks;
import net.gekidolukas.peakclienttest.common.PeakMenuTest;
import net.gekidolukas.peakclienttest.common.PeakMenuTestStep;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.function.Consumer;

import static net.gekidolukas.peakclienttest.common.PeakMenuTestKit.button;
import static net.gekidolukas.peakclienttest.common.PeakMenuTestKit.frame;

public final class ThemeMenuTest implements PeakMenuTest {
    private static final String MENU = "peaktest:menu/theme";
    private static final String PANORAMA = "minecraft:textures/gui/title/background/panorama_0.png";

    @Override public String name() { return "theme"; }
    @Override public String description() { return "Focus ring, tooltip frame, click and hover sounds and the backdrop per menu and per element"; }

    private static void send(ServerPlayer player, Consumer<PeakMenuSettings> theme, String title, PeakDocumentFiller filler) {
        PeakDocument menu = PeakMenu.create(MENU, settings -> {
            settings.title(title);
            theme.accept(settings);
        });
        filler.fill(menu);
        PeakClient.to(player).menus().sendAndOpen(menu);
    }

    private interface PeakDocumentFiller {
        void fill(PeakDocument menu);
    }

    private static void inputs(PeakDocument menu, String title) {
        menu.add(frame(title,
                PeakTextBox.create("theme:text").placeholder("Text box"),
                PeakRichTextBox.create("theme:rich", "").placeholder("Rich box").size(180, 30),
                PeakCheckbox.create("theme:check", "Checkbox"),
                PeakSlider.create("theme:slider", 0, 10, 5).step(1),
                button("theme:plain", "Button (menu theme)", PeakAction.send("plain")),
                button("theme:own", "Button (own focus)", PeakAction.send("own"))
                        .focusColor(0xFFFF5555).focusWidth(3)));
    }

    private static void tooltips(PeakDocument menu) {
        menu.add(frame("Tooltip frame",
                button("theme:tip-menu", "Menu tooltip style", PeakAction.send("plain"))
                        .tooltip("Menu style: dark blue, gold frame, wide padding"),
                button("theme:tip-own", "Own tooltip style", PeakAction.send("own"))
                        .tooltip("Own style: red frame, no shadow")
                        .tooltipStyle(PeakTooltipStyle.create().background(0xFF300000).border(0xFFFF3030)
                                .borderWidth(2).padding(4).textColor(0xFFAAAA).shadow(false)),
                button("theme:tip-wrap", "Wrapped tooltip", PeakAction.send("plain"))
                        .tooltip("This long tooltip is wrapped at the maximum width of the menu style so it takes several lines"),
                button("theme:tip-flat", "Flat tooltip", PeakAction.send("own"))
                        .tooltip("No frame at all")
                        .tooltipStyle(PeakTooltipStyle.create().border(0).borderWidth(0).background(0xE0000000))));
    }

    private static void sounds(PeakDocument menu) {
        menu.add(frame("Sounds",
                button("theme:snd-menu", "Menu click sound", PeakAction.send("menu-sound")),
                button("theme:snd-own", "Own click sound", PeakAction.send("own-sound"))
                        .clickSound("minecraft:entity.experience_orb.pickup", 1f, 1f),
                button("theme:snd-silent", "Silent button", PeakAction.send("silent-sound")).silent(true),
                button("theme:snd-hover", "Own hover sound", PeakAction.send("hover-sound"))
                        .hoverSound("minecraft:block.note_block.pling", 0.6f, 1.4f)));
    }

    @Override
    public List<PeakMenuTestStep> steps() {
        return List.of(
                PeakMenuTestStep.of("Focus ring color and width from the menu")
                        .setup(player -> send(player,
                                settings -> settings.focus(focus -> focus.color(0xFF55FF55).width(2)),
                                "Focus", menu -> inputs(menu, "Green focus ring")))
                        .check(PeakMenuChecks.navigated("The menu opens", MENU, MENU))
                        .check(PeakMenuChecks.manual("Click or Tab through every element: all rings are green and 2 px wide, except 'Button (own focus)' which is red and 3 px")),

                PeakMenuTestStep.of("The focus ring can be switched off")
                        .setup(player -> send(player,
                                settings -> settings.focus(PeakFocusStyle::none),
                                "No focus ring", menu -> inputs(menu, "No focus ring")))
                        .check(PeakMenuChecks.navigated("The menu opens", MENU))
                        .check(PeakMenuChecks.manual("Tab through the elements: the menu shows no ring, only 'Button (own focus)' still shows its red 3 px ring")),

                PeakMenuTestStep.of("Tooltip frame")
                        .setup(player -> send(player,
                                settings -> settings.tooltip(style -> style.background(0xFF0A0A30)
                                        .border(0xFFFFAA00).borderWidth(2).padding(6).maxWidth(110)),
                                "Tooltips", ThemeMenuTest::tooltips))
                        .check(PeakMenuChecks.navigated("The menu opens", MENU))
                        .check(PeakMenuChecks.manual("Hover each button: dark blue box with a gold 2 px frame, red frame without shadow on the second, text wrapped at about 110 px on the third, no frame at all on the fourth")),

                PeakMenuTestStep.of("Click sounds")
                        .setup(player -> send(player,
                                settings -> settings.clickSound("minecraft:block.note_block.bell", 1f, 1f),
                                "Sounds", ThemeMenuTest::sounds))
                        .check(PeakMenuChecks.navigated("The menu opens", MENU))
                        .check(PeakMenuChecks.action("Click 'Menu click sound' (bell)", "menu-sound", "theme:snd-menu"))
                        .check(PeakMenuChecks.action("Click 'Own click sound' (experience orb)", "own-sound", "theme:snd-own"))
                        .check(PeakMenuChecks.action("Click 'Silent button' (the action arrives)", "silent-sound", "theme:snd-silent"))
                        .check(PeakMenuChecks.manual("Menu button rang a bell, the own button played the orb pickup, the silent button was silent")),

                PeakMenuTestStep.of("Hover sounds")
                        .setup(player -> send(player,
                                settings -> settings.hoverSound("minecraft:ui.button.click", 0.4f, 0.8f).noClickSound(),
                                "Hover", ThemeMenuTest::sounds))
                        .check(PeakMenuChecks.navigated("The menu opens", MENU))
                        .check(PeakMenuChecks.manual("Moving onto a button plays a soft click once (not every frame); 'Own hover sound' plays a high pling instead; clicking plays nothing")),

                PeakMenuTestStep.of("Backdrop gradient")
                        .setup(player -> send(player,
                                settings -> settings.backdrop(PeakPaint.gradient(0xE0000030, 0xE0300000)
                                        .direction(PeakGradientDirection.TOP_TO_BOTTOM)),
                                "Gradient", menu -> inputs(menu, "Backdrop gradient")))
                        .check(PeakMenuChecks.navigated("The menu opens", MENU))
                        .check(PeakMenuChecks.manual("The screen fades from dark blue at the top to dark red at the bottom")),

                PeakMenuTestStep.of("Backdrop texture")
                        .setup(player -> send(player,
                                settings -> settings.backdropTexture(PANORAMA, 256, 256)
                                        .backdropFit(PeakObjectFit.COVER).noBackdrop(),
                                "Texture", menu -> inputs(menu, "Backdrop texture")))
                        .check(PeakMenuChecks.navigated("The menu opens", MENU))
                        .check(PeakMenuChecks.manual("The panorama image covers the whole screen without distortion; resize the window and it stays centered and covering")),

                PeakMenuTestStep.of("Backdrop texture contained")
                        .setup(player -> send(player,
                                settings -> settings.backdropTexture(PANORAMA, 256, 256)
                                        .backdropFit(PeakObjectFit.CONTAIN),
                                "Contain", menu -> inputs(menu, "Texture contained")))
                        .check(PeakMenuChecks.navigated("The menu opens", MENU))
                        .check(PeakMenuChecks.manual("The square image sits centered with the world visible on both sides, darkened by the backdrop color")),

                PeakMenuTestStep.of("Backdrop blur")
                        .setup(player -> send(player,
                                settings -> settings.backdropBlur(true),
                                "Blur", menu -> inputs(menu, "Backdrop blur")))
                        .check(PeakMenuChecks.navigated("The menu opens", MENU))
                        .check(PeakMenuChecks.manual("The world behind the menu is blurred (needs 'Menu Background Blur' above 0 in the video settings)")),

                PeakMenuTestStep.of("Backdrop panorama")
                        .setup(player -> send(player,
                                settings -> settings.vanillaPanorama().noBackdrop(),
                                "Panorama", menu -> inputs(menu, "Backdrop panorama")))
                        .check(PeakMenuChecks.navigated("The menu opens", MENU))
                        .check(PeakMenuChecks.manual("The title screen panorama turns slowly behind the menu and covers the whole screen; setting 'Panorama Scroll Speed' to 0 in the video settings freezes it")),

                PeakMenuTestStep.of("Panorama speed, pitch and overlay")
                        .setup(player -> send(player,
                                settings -> settings.backdropPanorama(PeakMenuSettings.VANILLA_PANORAMA, 4f)
                                        .backdropPanoramaPitch(-20f).backdropPanoramaOverlay(false).backdrop(
                                                PeakPaint.gradient(0x60000000, 0x00000000)
                                                        .direction(PeakGradientDirection.TOP_TO_BOTTOM)),
                                "Panorama 2", menu -> inputs(menu, "Fast, looking up")))
                        .check(PeakMenuChecks.navigated("The menu opens", MENU))
                        .check(PeakMenuChecks.manual("The panorama turns about four times as fast, looks upwards instead of slightly down, shows no overlay texture and has a soft dark fade at the top")),

                PeakMenuTestStep.of("A missing panorama is ignored")
                        .setup(player -> send(player,
                                settings -> settings.backdropPanorama("peaktest:textures/gui/does_not_exist"),
                                "Missing", menu -> inputs(menu, "Missing panorama")))
                        .check(PeakMenuChecks.navigated("The menu opens", MENU))
                        .check(PeakMenuChecks.manual("No panorama is drawn, the normal dark backdrop is shown and the client log has exactly one warning about the missing panorama texture"))
        );
    }
}
