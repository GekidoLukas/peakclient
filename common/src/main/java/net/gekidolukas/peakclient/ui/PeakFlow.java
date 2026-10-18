package net.gekidolukas.peakclient.ui;

public final class PeakFlow extends PeakContainer<PeakFlow> {
    private int lineGap = -1;
    private PeakMainAxisAlign lineAlign = PeakMainAxisAlign.START;
    private boolean reverse = false;
    private int maxLines = 0;
    private int wrapSize = 0;

    public PeakFlow() { this(PeakId.random()); }

    public PeakFlow(String id) {
        super(id);
        this.direction = PeakLayoutDirection.ROW;
    }

    public static PeakFlow create() { return new PeakFlow(); }
    public static PeakFlow create(String id) { return new PeakFlow(id); }
    public static PeakSpacer lineBreak() { return PeakSpacer.lineBreak(); }

    public int getLineGap() { return lineGap < 0 ? gap : lineGap; }
    public PeakFlow lineGap(int gap) { this.lineGap = Math.max(-1, gap); return this; }
    public PeakFlow spacing(int gap) { this.gap = Math.max(0, gap); this.lineGap = Math.max(0, gap); return this; }

    public PeakMainAxisAlign getLineAlign() { return lineAlign == null ? PeakMainAxisAlign.START : lineAlign; }
    public PeakFlow lineAlign(PeakMainAxisAlign align) { this.lineAlign = align; return this; }

    public boolean isReverse() { return reverse; }
    public PeakFlow reverse(boolean reverse) { this.reverse = reverse; return this; }

    public int getMaxLines() { return Math.max(0, maxLines); }
    public PeakFlow maxLines(int lines) { this.maxLines = Math.max(0, lines); return this; }

    public int getWrapSize() { return Math.max(0, wrapSize); }
    public PeakFlow wrapSize(int size) { this.wrapSize = Math.max(0, size); return this; }

    @Override
    public boolean scrollsVertically() { return direction == PeakLayoutDirection.ROW; }

    @Override
    public String elementType() { return "flow"; }
}
