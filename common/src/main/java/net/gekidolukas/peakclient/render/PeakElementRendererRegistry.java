package net.gekidolukas.peakclient.render;

import net.gekidolukas.peakclient.ui.PeakBarChart;
import net.gekidolukas.peakclient.ui.PeakBlockElement;
import net.gekidolukas.peakclient.ui.PeakButton;
import net.gekidolukas.peakclient.ui.PeakCheckbox;
import net.gekidolukas.peakclient.ui.PeakChibiPlayer;
import net.gekidolukas.peakclient.ui.PeakColorPicker;
import net.gekidolukas.peakclient.ui.PeakCompass;
import net.gekidolukas.peakclient.ui.PeakAnimated;
import net.gekidolukas.peakclient.ui.PeakCarousel;
import net.gekidolukas.peakclient.ui.PeakConditional;
import net.gekidolukas.peakclient.ui.PeakCycler;
import net.gekidolukas.peakclient.ui.PeakDiv;
import net.gekidolukas.peakclient.ui.PeakDocument;
import net.gekidolukas.peakclient.ui.PeakDropdown;
import net.gekidolukas.peakclient.ui.PeakElement;
import net.gekidolukas.peakclient.ui.PeakEntityElement;
import net.gekidolukas.peakclient.ui.PeakIconBar;
import net.gekidolukas.peakclient.ui.PeakImage;
import net.gekidolukas.peakclient.ui.PeakItemElement;
import net.gekidolukas.peakclient.ui.PeakItemSlot;
import net.gekidolukas.peakclient.ui.PeakLabel;
import net.gekidolukas.peakclient.ui.PeakMinimap;
import net.gekidolukas.peakclient.ui.PeakNumberField;
import net.gekidolukas.peakclient.ui.PeakPanel;
import net.gekidolukas.peakclient.ui.PeakPieChart;
import net.gekidolukas.peakclient.ui.PeakPlayerIcon;
import net.gekidolukas.peakclient.ui.PeakPopUpContainer;
import net.gekidolukas.peakclient.ui.PeakFeed;
import net.gekidolukas.peakclient.ui.PeakFlow;
import net.gekidolukas.peakclient.ui.PeakWorldAnchor;
import net.gekidolukas.peakclient.ui.PeakReel;
import net.gekidolukas.peakclient.ui.PeakShape;
import net.gekidolukas.peakclient.ui.PeakSortedList;
import net.gekidolukas.peakclient.ui.PeakStagger;
import net.gekidolukas.peakclient.ui.PeakProgressBar;
import net.gekidolukas.peakclient.ui.PeakRadialProgress;
import net.gekidolukas.peakclient.ui.PeakRadioGroup;
import net.gekidolukas.peakclient.ui.PeakRichTextBox;
import net.gekidolukas.peakclient.ui.PeakSlider;
import net.gekidolukas.peakclient.ui.PeakSpacer;
import net.gekidolukas.peakclient.ui.PeakSpinner;
import net.gekidolukas.peakclient.ui.PeakTableLayoutPanel;
import net.gekidolukas.peakclient.ui.PeakTextBox;
import net.gekidolukas.peakclient.ui.PeakTimer;
import net.gekidolukas.peakclient.ui.PeakTooltipLabel;

import java.util.HashMap;
import java.util.Map;

public final class PeakElementRendererRegistry {
    private static final Map<Class<?>, PeakElementRenderer<?>> RENDERERS = new HashMap<>();

    static {
        register(PeakLabel.class, new PeakLabelRenderer());
        register(PeakTimer.class, new PeakTimerRenderer());
        register(PeakImage.class, new PeakImageRenderer());
        register(PeakSpacer.class, new PeakSpacerRenderer());
        register(PeakItemElement.class, new PeakItemElementRenderer());
        register(PeakEntityElement.class, new PeakEntityElementRenderer());
        register(PeakBlockElement.class, new PeakBlockElementRenderer());
        register(PeakProgressBar.class, new PeakProgressBarRenderer());
        register(PeakRadialProgress.class, new PeakRadialProgressRenderer());
        register(PeakTooltipLabel.class, new PeakTooltipLabelRenderer());
        register(PeakIconBar.class, new PeakIconBarRenderer());
        register(PeakCompass.class, new PeakCompassRenderer());
        register(PeakDiv.class, new PeakDivRenderer());
        register(PeakPanel.class, new PeakPanelRenderer());
        register(PeakButton.class, new PeakButtonRenderer());
        register(PeakTextBox.class, new PeakTextBoxRenderer());
        register(PeakRichTextBox.class, new PeakRichTextBoxRenderer());
        register(PeakCheckbox.class, new PeakCheckboxRenderer());
        register(PeakSlider.class, new PeakSliderRenderer());
        register(PeakColorPicker.class, new PeakColorPickerRenderer());
        register(PeakRadioGroup.class, new PeakRadioGroupRenderer());
        register(PeakNumberField.class, new PeakNumberFieldRenderer());
        register(PeakDropdown.class, new PeakDropdownRenderer());
        register(PeakItemSlot.class, new PeakItemSlotRenderer());
        register(PeakSpinner.class, new PeakSpinnerRenderer());
        register(PeakTableLayoutPanel.class, new PeakTableLayoutPanelRenderer());
        register(PeakPopUpContainer.class, new PeakPopUpContainerRenderer());
        register(PeakConditional.class, new PeakConditionalRenderer());
        register(PeakCarousel.class, new PeakCarouselRenderer());
        register(PeakCycler.class, new PeakCyclerRenderer());
        register(PeakAnimated.class, new PeakAnimatedRenderer());
        register(PeakStagger.class, new PeakStaggerRenderer());
        register(PeakFeed.class, new PeakFeedRenderer());
        register(PeakSortedList.class, new PeakSortedListRenderer());
        register(PeakReel.class, new PeakReelRenderer());
        register(PeakFlow.class, new PeakFlowRenderer());
        register(PeakWorldAnchor.class, new PeakWorldAnchorRenderer());
        register(PeakShape.class, new PeakShapeRenderer());
        register(PeakDocument.class, new PeakDocumentRenderer());
        register(PeakMinimap.class, new PeakMinimapRenderer());
        register(PeakPlayerIcon.class, new PeakPlayerIconRenderer());
        register(PeakBarChart.class, new PeakBarChartRenderer());
        register(PeakPieChart.class, new PeakPieChartRenderer());
        register(PeakChibiPlayer.class, new PeakChibiPlayerRenderer());
    }

    private static <T extends PeakElement<?>> void register(Class<T> type, PeakElementRenderer<T> renderer) {
        RENDERERS.put(type, renderer);
    }

    @SuppressWarnings("unchecked")
    public static <T extends PeakElement<?>> PeakElementRenderer<T> get(Class<?> type) {
        return (PeakElementRenderer<T>) RENDERERS.get(type);
    }

    private PeakElementRendererRegistry() {}
}
