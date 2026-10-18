package net.gekidolukas.peakclient.ui;

public final class PeakBlockSource {
    public enum Kind { FIXED, TARGETED_BLOCK }

    private Kind kind;
    private String blockId;

    private PeakBlockSource() {}

    public static PeakBlockSource fixed(String blockId) {
        PeakBlockSource source = new PeakBlockSource();
        source.kind = Kind.FIXED; source.blockId = blockId;
        return source;
    }
    public static PeakBlockSource targetedBlock() {
        PeakBlockSource source = new PeakBlockSource(); source.kind = Kind.TARGETED_BLOCK; return source;
    }

    public Kind kind() { return kind; }
    public String blockId() { return blockId; }
}