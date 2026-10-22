package net.gekidolukas.peakclient.ui.client;

import java.util.Map;
import java.util.Objects;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import net.gekidolukas.peakclient.debug.PeakDebugOverlayRenderer;
import net.gekidolukas.peakclient.debug.PeakDebugState;
import net.gekidolukas.peakclient.render.PeakLayoutEntry;
import net.gekidolukas.peakclient.render.PeakLayoutProbe;
import net.gekidolukas.peakclient.render.PeakPlaceholderResolver;
import net.gekidolukas.peakclient.render.PeakUiRenderer;
import net.gekidolukas.peakclient.ui.PeakDocument;
import net.gekidolukas.peakclient.ui.PeakDocumentModel;
import net.gekidolukas.peakclient.ui.PeakDropdown;
import net.gekidolukas.peakclient.ui.PeakElement;
import net.gekidolukas.peakclient.ui.PeakEvent;
import net.gekidolukas.peakclient.ui.PeakInteraction;
import net.gekidolukas.peakclient.ui.PeakMenuSettings;
import net.gekidolukas.peakclient.ui.PeakTheme;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.opengl.GL11;

import static com.mojang.blaze3d.platform.InputConstants.KEY_0;
import static com.mojang.blaze3d.platform.InputConstants.KEY_ESCAPE;
import static com.mojang.blaze3d.platform.InputConstants.KEY_F3;
import static com.mojang.blaze3d.platform.InputConstants.KEY_TAB;

public final class PeakMenuScreen extends Screen {
    private final PeakMenuContext context = new PeakMenuContext(this);
    private final PeakMenuChanges changes = new PeakMenuChanges();
    private final PeakMenuScrollbars scrollbars = new PeakMenuScrollbars(context);
    private final PeakInputControllers controllers = new PeakInputControllers(context, changes, new PeakTextHits(context));
    private final PeakMenuShortcuts shortcuts = new PeakMenuShortcuts();
    private final PeakMenuScrollInput scrollInput = new PeakMenuScrollInput(context);
    private final PeakMenuFocusNavigation focusNavigation = new PeakMenuFocusNavigation(context, controllers);
    private String lastHoverId;
    private boolean debugFromMenu;
    private EditBox inputGuard;

    public PeakMenuScreen() {
        super(Component.translatable("peakclient.menu.title"));
    }

    @Override
    public Component getTitle() {
        PeakMenuSettings settings = context.settings();
        String title = settings == null ? "" : settings.getTitle().getString();
        if (title.isEmpty()) return super.getTitle();
        return Component.literal(ChatFormatting.stripFormatting(PeakPlaceholderResolver.resolve(title)));
    }

    @Override
    public Component getNarrationMessage() {
        return getTitle();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        PeakDocument menu = PeakClientMenus.currentMenu();
        if (menu == null) return;
        if (!menu.getId().equals(context.shownMenuId())) {
            changes.flushAll(context.shownMenuId());
            context.shownMenuId(menu.getId());
            context.pressed(null);
            context.clearDrag();
            scrollbars.clear();
            context.probe(new PeakLayoutProbe());
        }

        RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);

        PeakMenuSettings settings = menu.getMenu();
        drawBackdrop(graphics, settings, partialTick);
        PeakColorPickerLinks.followTargets(menu.getId());

        PeakMenuDrag drag = context.activeDrag();
        String hoveredBar = null;
        if (!PeakDebugState.isEnabled() && (drag == null || !drag.keepsScrollbarHover())) {
            PeakScrollbarRegion region = scrollbars.at(mouseX, mouseY);
            if (region != null) hoveredBar = region.ownerId();
        }
        String draggedBar = drag == null ? null : drag.scrollbarOwner();
        PeakLayoutEntry hovered = PeakDebugState.isEnabled() || hoveredBar != null || draggedBar != null
                || PeakClientMenus.inputState(menu.getId()).openDropdownId() != null
                ? null : PeakMenuHits.interactiveAt(context.probe(), mouseX, mouseY);
        PeakLayoutProbe next = new PeakLayoutProbe();
        PeakMenuInteractionState.begin(hovered == null ? null : hovered.element(), context.pressed(),
                PeakClientMenus.scrollState(menu.getId()), PeakClientMenus.inputState(menu.getId()));
        PeakMenuInteractionState.scrollbarFocus(hoveredBar, draggedBar);
        PeakMenuInteractionState.useSettings(settings);
        if (drag != null) drag.beforeRender();
        try {
            PeakUiRenderer.renderMenu(graphics, minecraft, menu, next);
            scrollbars.update(PeakMenuInteractionState.scrollbars());
        } finally {
            PeakMenuInteractionState.end();
        }
        context.probe(next);

        PeakDropdown openDropdown = controllers.dropdowns().open();
        if (openDropdown != null) controllers.dropdowns().drawOverlay(graphics, openDropdown, mouseX, mouseY, settings);

        String hoveredId = hovered == null ? null : hovered.elementId();
        if (hoveredId != null && !hoveredId.equals(lastHoverId) && PeakMenuContext.isEnabled(hovered.element())
                && (drag == null || !drag.silencesHoverSound())) {
            PeakSounds.play(PeakTheme.hoverSound(settings, hovered.element().getInteraction()));
        }
        if (!Objects.equals(hoveredId, lastHoverId)) fireHover(lastHoverId, hovered);
        lastHoverId = hoveredId;

        if (hovered != null && (drag == null || !drag.hidesTooltip())) {
            PeakMenuTooltips.renderHovered(graphics, minecraft, settings, hovered.element(), mouseX, mouseY, width, height);
        }
        if (drag != null) drag.afterRender(graphics, mouseX, mouseY, width, height);

        if (PeakDebugState.isEnabled()) {
            PeakDebugState.useMenuProbe(context.probe());
            PeakDebugOverlayRenderer.renderForMenu(graphics, minecraft, mouseX, mouseY);
        }
    }

    private void fireHover(String previousId, PeakLayoutEntry current) {
        PeakDocumentModel model = PeakClientMenus.model(context.menuId());
        PeakElement<?> previous = previousId == null || model == null ? null : model.find(previousId);
        if (previous != null) context.fire(previous, PeakEvent.HOVER_LEAVE, Map.of());
        if (current != null) context.fire(current.element(), PeakEvent.HOVER_ENTER, Map.of());
    }

    private void fireFocus(String previousId, String currentId) {
        PeakDocumentModel model = PeakClientMenus.model(context.menuId());
        if (model == null) return;
        PeakElement<?> previous = previousId == null ? null : model.find(previousId);
        PeakElement<?> current = currentId == null ? null : model.find(currentId);
        if (previous != null) context.fire(previous, PeakEvent.BLUR, Map.of());
        if (current != null) context.fire(current, PeakEvent.FOCUS, Map.of());
    }

    private void drawBackdrop(GuiGraphics graphics, PeakMenuSettings settings, float partialTick) {
        if (settings == null) return;
        if (settings.isBackdropBlur()) renderBlurredBackground(partialTick);
        PeakMenuBackdrop.draw(graphics, settings, width, height, partialTick);
    }

    private boolean isDebugKey(int keyCode) {
        return keyCode == KEY_0
                && InputConstants.isKeyDown(minecraft.getWindow().getWindow(), KEY_F3);
    }

    private void toggleDebug() {
        boolean enable = !PeakDebugState.isEnabled();
        PeakDebugState.setEnabled(enable);
        debugFromMenu = enable;
        context.pressed(null);
        context.clearDrag();
    }

    @Override
    public void tick() {
        super.tick();
        String menuId = context.menuId();
        if (menuId == null) return;
        changes.tick(menuId, context.input().focusedId(), controllers.numbers()::commitOnBlur, this::fireFocus);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (PeakDebugState.isEnabled()) {
            PeakLayoutEntry selected = button == 0 ? PeakDebugState.resolveSelected((int) mouseX, (int) mouseY) : null;
            if (selected != null) {
                minecraft.keyboardHandler.setClipboard(selected.elementId());
                if (minecraft.player != null) {
                    minecraft.player.displayClientMessage(
                            Component.literal("§7[PeakClient Debug] Copied element ID: §f" + selected.elementId()), false);
                }
            }
            return true;
        }
        PeakDropdown openDropdown = controllers.dropdowns().open();
        if (openDropdown != null) {
            controllers.dropdowns().overlayClick(openDropdown, mouseX, mouseY, button);
            return true;
        }
        if (button == 1) return handleRightClick(mouseX, mouseY);
        if (button != 0) return true;

        PeakScrollbarRegion bar = scrollbars.at((int) mouseX, (int) mouseY);
        if (bar != null) {
            context.pressed(null);
            context.beginDrag(scrollbars.begin(bar, mouseX, mouseY));
            return true;
        }

        PeakMenuInputState input = context.input();
        PeakLayoutEntry hit = PeakMenuHits.interactiveAt(context.probe(), (int) mouseX, (int) mouseY);
        context.pressed(null);
        if (hit == null) {
            input.blur();
            return true;
        }
        PeakElement<?> element = hit.element();
        if (!PeakMenuContext.isEnabled(element)) {
            input.blur();
            return true;
        }
        controllers.of(element).pressed(element, hit, mouseX, mouseY);
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (PeakDebugState.isEnabled() || button != 0) return true;
        PeakMenuDrag drag = context.activeDrag();
        if (drag != null) drag.dragged(mouseX, mouseY);
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button != 0) return true;
        PeakMenuDrag drag = context.takeDrag();
        if (drag != null) {
            context.pressed(null);
            drag.released(mouseX, mouseY);
            return true;
        }

        PeakElement<?> element = context.takePressed();
        if (element == null) return true;

        PeakLayoutEntry hit = PeakMenuHits.interactiveAt(context.probe(), (int) mouseX, (int) mouseY);
        if (hit == null || hit.element() != element) return true;
        PeakInteraction interaction = element.getInteraction();
        if (interaction == null || !interaction.isEnabled()) return true;

        context.playClick(interaction);
        controllers.activate(element);
        if (context.doubleClickPressed()) context.fire(element, PeakEvent.DOUBLE_CLICK, Map.of());
        context.doubleClickPressed(false);
        return true;
    }

    private boolean handleRightClick(double mouseX, double mouseY) {
        PeakLayoutEntry hit = PeakMenuHits.interactiveAt(context.probe(), (int) mouseX, (int) mouseY);
        if (hit == null || !PeakMenuContext.isEnabled(hit.element())) return true;
        PeakInteraction interaction = hit.element().getInteraction();
        if (interaction == null || !hit.element().hasEvent(PeakEvent.RIGHT_CLICK)) return true;
        context.playClick(interaction);
        context.fire(hit.element(), PeakEvent.RIGHT_CLICK, Map.of());
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (PeakDebugState.isEnabled()) {
            PeakDebugState.scroll((int) mouseX, (int) mouseY, scrollY);
            return true;
        }
        PeakDocument menu = PeakClientMenus.currentMenu();
        if (menu == null) return true;
        PeakDropdown wheelDropdown = controllers.dropdowns().open();
        if (wheelDropdown != null) {
            controllers.dropdowns().overlayWheel(wheelDropdown, scrollY);
            return true;
        }
        PeakLayoutEntry wheelHit = PeakMenuHits.interactiveAt(context.probe(), (int) mouseX, (int) mouseY);
        if (wheelHit != null && PeakMenuContext.isEnabled(wheelHit.element())) {
            PeakElement<?> hitElement = wheelHit.element();
            if (controllers.of(hitElement).wheel(hitElement, scrollY)) return true;
        }
        scrollInput.wheel(menu, mouseX, mouseY, scrollY);
        return true;
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (PeakDebugState.isEnabled()) return true;
        PeakElement<?> focused = context.focusedElement();
        if (focused != null && PeakMenuContext.isEnabled(focused) && controllers.of(focused).typed(focused, codePoint)) {
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (isDebugKey(keyCode)) {
            toggleDebug();
            return true;
        }
        if (PeakDebugState.isEnabled()) {
            if (keyCode == KEY_ESCAPE) toggleDebug();
            return true;
        }

        PeakMenuInputState input = context.input();
        PeakElement<?> focused = context.focusedElement();
        if (keyCode == KEY_ESCAPE) {
            if (input.openDropdownId() != null) {
                input.closeDropdown();
                return true;
            }
            PeakMenuDrag drag = context.activeDrag();
            if (drag != null && drag.cancelledByEscape()) {
                context.takeDrag();
                return true;
            }
            if (focused != null && controllers.of(focused).escape(focused)) return true;
            PeakClientMenus.onEscape();
            return true;
        }
        PeakDropdown openForKeys = controllers.dropdowns().open();
        if (openForKeys != null) {
            if (keyCode == KEY_TAB) input.closeDropdown();
            else if (controllers.dropdowns().overlayKey(openForKeys, keyCode)) return true;
        }
        boolean typing = focused != null && PeakMenuContext.isEnabled(focused) && controllers.of(focused).isTyping();
        if ((!typing || hasControlDown() || hasAltDown()) && shortcuts.handle(keyCode)) return true;
        if (keyCode == KEY_TAB) {
            focusNavigation.move(hasShiftDown());
            return true;
        }
        if (focused == null || !PeakMenuContext.isEnabled(focused)) {
            if (scrollInput.key(keyCode)) return true;
            return super.keyPressed(keyCode, scanCode, modifiers);
        }

        if (controllers.of(focused).key(focused, keyCode)) return true;
        if (scrollInput.key(keyCode)) return true;
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        shortcuts.released(keyCode);
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public GuiEventListener getFocused() {
        if (!PeakDebugState.isEnabled()) {
            PeakElement<?> focused = context.focusedElement();
            if (focused != null && PeakMenuContext.isEnabled(focused) && controllers.of(focused).isTyping()) {
                if (inputGuard == null) {
                    inputGuard = new EditBox(Minecraft.getInstance().font, 0, 0, 0, 0, Component.empty());
                    inputGuard.setFocused(true);
                }
                return inputGuard;
            }
        }
        return super.getFocused();
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public boolean isPauseScreen() {
        PeakDocument menu = PeakClientMenus.currentMenu();
        return menu != null && menu.getMenu() != null && menu.getMenu().isPauseGame();
    }

    @Override
    public void removed() {
        changes.flushAll(context.menuId());
        super.removed();
        PeakDebugState.useMenuProbe(null);
        if (debugFromMenu) {
            PeakDebugState.setEnabled(false);
            debugFromMenu = false;
        }
        PeakClientMenus.onScreenRemoved();
    }
}
