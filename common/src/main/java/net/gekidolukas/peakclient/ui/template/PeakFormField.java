package net.gekidolukas.peakclient.ui.template;

import net.gekidolukas.peakclient.ui.PeakPaint;
import net.gekidolukas.peakclient.ui.PeakElement;
import net.gekidolukas.peakclient.ui.PeakId;
import net.gekidolukas.peakclient.ui.PeakLabel;
import net.gekidolukas.peakclient.ui.PeakPanel;
import net.gekidolukas.peakclient.ui.PeakUiOperation;
import net.gekidolukas.peakclient.ui.patch.PeakPatch;

import java.util.List;

public final class PeakFormField {
    public static final String REQUIRED_MESSAGE = "This field is required";

    private final String id;
    private final String label;
    private PeakElement<?> input;
    private boolean required;
    private String hint = "";
    private int gap = 3;
    private int width = -1;
    private PeakPaint labelColor = PeakPaint.solid(0xAAAAAA);
    private String requiredMessage = REQUIRED_MESSAGE;

    private PeakFormField(String id, String label) {
        this.id = PeakId.require(id, "Form field");
        this.label = label == null ? "" : label;
    }

    public static PeakFormField create(String id, String label) {
        return new PeakFormField(id, label);
    }

    public static String errorId(String id) { return id + "/error"; }
    public static String labelId(String id) { return id + "/label"; }
    public static String hintId(String id) { return id + "/hint"; }

    public PeakFormField input(PeakElement<?> element) {
        this.input = element;
        return this;
    }

    public PeakFormField required(boolean required) {
        this.required = required;
        return this;
    }

    public PeakFormField hint(String hint) {
        this.hint = hint == null ? "" : hint;
        return this;
    }

    public PeakFormField gap(int gap) {
        this.gap = Math.max(0, gap);
        return this;
    }

    public PeakFormField width(int width) {
        this.width = width;
        return this;
    }

    public PeakFormField labelColor(PeakPaint paint) {
        this.labelColor = paint;
        return this;
    }

    public PeakFormField labelColor(int argb) {
        return labelColor(PeakPaint.solid(argb));
    }

    public PeakFormField requiredMessage(String message) {
        this.requiredMessage = message == null || message.isEmpty() ? REQUIRED_MESSAGE : message;
        return this;
    }

    public PeakPanel build() {
        if (input == null) throw new IllegalStateException("Form field '" + id + "' has no input element");
        PeakPanel root = PeakPanel.create(id).column().gap(gap).padding(0).background(0);
        if (width > 0) root.width(width);

        String shownLabel = required ? label + " §c*" : label;
        root.add(PeakLabel.create(labelId(id), shownLabel).color(labelColor));

        input.required(required);
        input.errorElement(errorId(id));
        root.add(input);

        if (!hint.isEmpty()) root.add(PeakLabel.create(hintId(id), hint).color(0x777777));
        PeakLabel error = PeakLabel.create(errorId(id), requiredMessage).color(0xFF5555);
        error.hidden(true);
        root.add(error);
        return root;
    }

    public static List<PeakUiOperation> errorOperations(String fieldId, String inputId, String message) {
        boolean failed = message != null && !message.isEmpty();
        PeakUiOperation input = new PeakUiOperation.Update(inputId,
                PeakPatch.textBox().field("interaction.invalid", failed).build());
        var label = PeakPatch.label();
        if (failed) label.text(message);
        label.hidden(!failed);
        PeakUiOperation error = new PeakUiOperation.Update(errorId(fieldId), label.build());
        return List.of(input, error);
    }
}
