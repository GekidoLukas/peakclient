package net.gekidolukas.peakclient.ui;

import net.gekidolukas.peakclient.ui.patch.PeakAnimatedPatch;
import net.gekidolukas.peakclient.ui.patch.PeakBarChartPatch;
import net.gekidolukas.peakclient.ui.patch.PeakBasePatch;
import net.gekidolukas.peakclient.ui.patch.PeakBlockPatch;
import net.gekidolukas.peakclient.ui.patch.PeakButtonPatch;
import net.gekidolukas.peakclient.ui.patch.PeakCheckboxPatch;
import net.gekidolukas.peakclient.ui.patch.PeakFeedPatch;
import net.gekidolukas.peakclient.ui.patch.PeakChibiPlayerPatch;
import net.gekidolukas.peakclient.ui.patch.PeakColorPickerPatch;
import net.gekidolukas.peakclient.ui.patch.PeakCompassPatch;
import net.gekidolukas.peakclient.ui.patch.PeakCarouselPatch;
import net.gekidolukas.peakclient.ui.patch.PeakConditionalPatch;
import net.gekidolukas.peakclient.ui.patch.PeakCyclerPatch;
import net.gekidolukas.peakclient.ui.patch.PeakDivPatch;
import net.gekidolukas.peakclient.ui.patch.PeakDropdownPatch;
import net.gekidolukas.peakclient.ui.patch.PeakEntityPatch;
import net.gekidolukas.peakclient.ui.patch.PeakIconBarPatch;
import net.gekidolukas.peakclient.ui.patch.PeakImagePatch;
import net.gekidolukas.peakclient.ui.patch.PeakItemPatch;
import net.gekidolukas.peakclient.ui.patch.PeakItemSlotPatch;
import net.gekidolukas.peakclient.ui.patch.PeakLabelPatch;
import net.gekidolukas.peakclient.ui.patch.PeakMinimapPatch;
import net.gekidolukas.peakclient.ui.patch.PeakNumberFieldPatch;
import net.gekidolukas.peakclient.ui.patch.PeakPanelPatch;
import net.gekidolukas.peakclient.ui.patch.PeakPatch;
import net.gekidolukas.peakclient.ui.patch.PeakPieChartPatch;
import net.gekidolukas.peakclient.ui.patch.PeakPlayerIconPatch;
import net.gekidolukas.peakclient.ui.patch.PeakPopUpPatch;
import net.gekidolukas.peakclient.ui.patch.PeakProgressBarPatch;
import net.gekidolukas.peakclient.ui.patch.PeakRadialProgressPatch;
import net.gekidolukas.peakclient.ui.patch.PeakRadioGroupPatch;
import net.gekidolukas.peakclient.ui.patch.PeakRichTextBoxPatch;
import net.gekidolukas.peakclient.ui.patch.PeakSliderPatch;
import net.gekidolukas.peakclient.ui.patch.PeakSpinnerPatch;
import net.gekidolukas.peakclient.ui.patch.PeakFlowPatch;
import net.gekidolukas.peakclient.ui.patch.PeakWorldAnchorPatch;
import net.gekidolukas.peakclient.ui.patch.PeakReelPatch;
import net.gekidolukas.peakclient.ui.patch.PeakShapePatch;
import net.gekidolukas.peakclient.ui.patch.PeakSortedListPatch;
import net.gekidolukas.peakclient.ui.patch.PeakStaggerPatch;
import net.gekidolukas.peakclient.ui.patch.PeakTablePatch;
import net.gekidolukas.peakclient.ui.patch.PeakTextBoxPatch;
import net.gekidolukas.peakclient.ui.patch.PeakTimerPatch;
import net.gekidolukas.peakclient.ui.patch.PeakTooltipLabelPatch;

import java.util.function.Function;
import java.util.function.Supplier;

public final class PeakRef<E extends PeakElement<?>, P extends PeakBasePatch<?>> {
    private final String id;
    private final Class<E> elementClass;
    private final Supplier<P> patchFactory;

    private PeakRef(String id, Class<E> elementClass, Supplier<P> patchFactory) {
        this.id = id;
        this.elementClass = elementClass;
        this.patchFactory = patchFactory;
    }

    public static <E extends PeakElement<?>, P extends PeakBasePatch<?>> PeakRef<E, P> of(String id, Class<E> elementClass, Supplier<P> patchFactory) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("A reference needs a non-empty element id");
        return new PeakRef<>(PeakId.require(id, "Reference"), elementClass, patchFactory);
    }

    public static PeakRef<PeakLabel, PeakLabelPatch> label(String id) { return of(id, PeakLabel.class, PeakPatch::label); }
    public static PeakRef<PeakTimer, PeakTimerPatch> timer(String id) { return of(id, PeakTimer.class, PeakPatch::timer); }
    public static PeakRef<PeakImage, PeakImagePatch> image(String id) { return of(id, PeakImage.class, PeakPatch::image); }
    public static PeakRef<PeakItemElement, PeakItemPatch> item(String id) { return of(id, PeakItemElement.class, PeakPatch::item); }
    public static PeakRef<PeakEntityElement, PeakEntityPatch> entity(String id) { return of(id, PeakEntityElement.class, PeakPatch::entity); }
    public static PeakRef<PeakBlockElement, PeakBlockPatch> block(String id) { return of(id, PeakBlockElement.class, PeakPatch::block); }
    public static PeakRef<PeakProgressBar, PeakProgressBarPatch> progressBar(String id) { return of(id, PeakProgressBar.class, PeakPatch::progressBar); }
    public static PeakRef<PeakRadialProgress, PeakRadialProgressPatch> radialProgress(String id) {
        return of(id, PeakRadialProgress.class, PeakPatch::radialProgress);
    }
    public static PeakRef<PeakIconBar, PeakIconBarPatch> iconBar(String id) { return of(id, PeakIconBar.class, PeakPatch::iconBar); }
    public static PeakRef<PeakTooltipLabel, PeakTooltipLabelPatch> tooltipLabel(String id) { return of(id, PeakTooltipLabel.class, PeakPatch::tooltipLabel); }
    public static PeakRef<PeakCompass, PeakCompassPatch> compass(String id) { return of(id, PeakCompass.class, PeakPatch::compass); }
    public static PeakRef<PeakDiv, PeakDivPatch> div(String id) { return of(id, PeakDiv.class, PeakPatch::div); }
    public static PeakRef<PeakPanel, PeakPanelPatch> panel(String id) { return of(id, PeakPanel.class, PeakPatch::panel); }
    public static PeakRef<PeakButton, PeakButtonPatch> button(String id) { return of(id, PeakButton.class, PeakPatch::button); }
    public static PeakRef<PeakTextBox, PeakTextBoxPatch> textBox(String id) { return of(id, PeakTextBox.class, PeakPatch::textBox); }
    public static PeakRef<PeakRichTextBox, PeakRichTextBoxPatch> richTextBox(String id) { return of(id, PeakRichTextBox.class, PeakPatch::richTextBox); }
    public static PeakRef<PeakCheckbox, PeakCheckboxPatch> checkbox(String id) { return of(id, PeakCheckbox.class, PeakPatch::checkbox); }
    public static PeakRef<PeakSlider, PeakSliderPatch> slider(String id) { return of(id, PeakSlider.class, PeakPatch::slider); }
    public static PeakRef<PeakColorPicker, PeakColorPickerPatch> colorPicker(String id) { return of(id, PeakColorPicker.class, PeakPatch::colorPicker); }
    public static PeakRef<PeakRadioGroup, PeakRadioGroupPatch> radioGroup(String id) { return of(id, PeakRadioGroup.class, PeakPatch::radioGroup); }
    public static PeakRef<PeakNumberField, PeakNumberFieldPatch> numberField(String id) { return of(id, PeakNumberField.class, PeakPatch::numberField); }
    public static PeakRef<PeakDropdown, PeakDropdownPatch> dropdown(String id) { return of(id, PeakDropdown.class, PeakPatch::dropdown); }
    public static PeakRef<PeakItemSlot, PeakItemSlotPatch> itemSlot(String id) { return of(id, PeakItemSlot.class, PeakPatch::itemSlot); }
    public static PeakRef<PeakSpinner, PeakSpinnerPatch> spinner(String id) { return of(id, PeakSpinner.class, PeakPatch::spinner); }
    public static PeakRef<PeakTableLayoutPanel, PeakTablePatch> table(String id) { return of(id, PeakTableLayoutPanel.class, PeakPatch::table); }
    public static PeakRef<PeakPopUpContainer, PeakPopUpPatch> popup(String id) { return of(id, PeakPopUpContainer.class, PeakPatch::popup); }
    public static PeakRef<PeakConditional, PeakConditionalPatch> conditional(String id) { return of(id, PeakConditional.class, PeakPatch::conditional); }
    public static PeakRef<PeakCarousel, PeakCarouselPatch> carousel(String id) { return of(id, PeakCarousel.class, PeakPatch::carousel); }
    public static PeakRef<PeakCycler, PeakCyclerPatch> cycler(String id) { return of(id, PeakCycler.class, PeakPatch::cycler); }
    public static PeakRef<PeakAnimated, PeakAnimatedPatch> animated(String id) { return of(id, PeakAnimated.class, PeakPatch::animated); }
    public static PeakRef<PeakStagger, PeakStaggerPatch> stagger(String id) { return of(id, PeakStagger.class, PeakPatch::stagger); }
    public static PeakRef<PeakFeed, PeakFeedPatch> feed(String id) { return of(id, PeakFeed.class, PeakPatch::feed); }
    public static PeakRef<PeakSortedList, PeakSortedListPatch> sortedList(String id) { return of(id, PeakSortedList.class, PeakPatch::sortedList); }
    public static PeakRef<PeakReel, PeakReelPatch> reel(String id) { return of(id, PeakReel.class, PeakPatch::reel); }
    public static PeakRef<PeakFlow, PeakFlowPatch> flow(String id) { return of(id, PeakFlow.class, PeakPatch::flow); }
    public static PeakRef<PeakWorldAnchor, PeakWorldAnchorPatch> worldAnchor(String id) { return of(id, PeakWorldAnchor.class, PeakPatch::worldAnchor); }
    public static PeakRef<PeakShape, PeakShapePatch> shape(String id) { return of(id, PeakShape.class, PeakPatch::shape); }
    public static PeakRef<PeakMinimap, PeakMinimapPatch> minimap(String id) { return of(id, PeakMinimap.class, PeakPatch::minimap); }
    public static PeakRef<PeakChibiPlayer, PeakChibiPlayerPatch> chibiPlayer(String id) { return of(id, PeakChibiPlayer.class, PeakPatch::chibiPlayer); }
    public static PeakRef<PeakPlayerIcon, PeakPlayerIconPatch> playerIcon(String id) { return of(id, PeakPlayerIcon.class, PeakPatch::playerIcon); }
    public static PeakRef<PeakBarChart, PeakBarChartPatch> barChart(String id) { return of(id, PeakBarChart.class, PeakPatch::barChart); }
    public static PeakRef<PeakPieChart, PeakPieChartPatch> pieChart(String id) { return of(id, PeakPieChart.class, PeakPatch::pieChart); }

    public String id() { return id; }

    public Class<E> elementClass() { return elementClass; }

    public P newPatch() { return patchFactory.get(); }

    public E create(Function<String, E> factory) {
        E element = factory.apply(id);
        if (!id.equals(element.getId())) {
            throw new IllegalArgumentException("The element must be created with the id of the reference '" + id + "' but has '" + element.getId() + "'");
        }
        return element;
    }
}
