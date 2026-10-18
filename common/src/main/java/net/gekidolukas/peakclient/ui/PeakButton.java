package net.gekidolukas.peakclient.ui;

public final class PeakButton extends PeakContainer<PeakButton> {
    public static final int DEFAULT_BACKGROUND = 0xFF555555;
    public static final int DEFAULT_HOVER_BACKGROUND = 0xFF707070;
    public static final int DEFAULT_PRESSED_BACKGROUND = 0xFF3B3B3B;
    public static final int DEFAULT_DISABLED_BACKGROUND = 0xFF2B2B2B;

    public PeakButton() { this(PeakId.random()); }

    public PeakButton(String id) {
        super(id);
        direction = PeakLayoutDirection.ROW;
        alignChildren = PeakAlign.CENTER;
        mainAxisAlign = PeakMainAxisAlign.CENTER;
        style.background(DEFAULT_BACKGROUND).padding(4, 10, 4, 10).borderWidth(1).borderColor(0xFF000000);
        interaction = PeakInteraction.create()
                .hoverStyle(PeakStyle.create().background(DEFAULT_HOVER_BACKGROUND))
                .pressedStyle(PeakStyle.create().background(DEFAULT_PRESSED_BACKGROUND))
                .disabledStyle(PeakStyle.create().background(DEFAULT_DISABLED_BACKGROUND));
    }

    public static PeakButton create() { return new PeakButton(); }
    public static PeakButton create(String id) { return new PeakButton(id); }

    public static PeakButton text(String text) {
        PeakButton button = new PeakButton();
        return button.add(PeakLabel.create(button.getId() + "/label", text));
    }

    public static PeakButton text(String id, String text) {
        return new PeakButton(id).add(PeakLabel.create(id + "/label", text));
    }

    @Override
    public String elementType() { return "button"; }
}
