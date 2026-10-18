package net.gekidolukas.peakclient.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class PeakTrack {
    public enum Kind { AUTO, PIXELS, PERCENT, FILL }

    private Kind kind = Kind.AUTO;
    private float value = 0f;
    private int min = 0;
    private int max = 0;
    private PeakAlign align = PeakAlign.AUTO;

    public static PeakTrack auto() { return new PeakTrack(); }

    public static PeakTrack px(int pixels) {
        PeakTrack track = new PeakTrack();
        track.kind = Kind.PIXELS;
        track.value = Math.max(0, pixels);
        return track;
    }

    public static PeakTrack percent(float percent) {
        PeakTrack track = new PeakTrack();
        track.kind = Kind.PERCENT;
        track.value = Math.max(0f, percent);
        return track;
    }

    public static PeakTrack fill() { return fill(1f); }

    public static PeakTrack fill(float weight) {
        PeakTrack track = new PeakTrack();
        track.kind = Kind.FILL;
        track.value = weight > 0f ? weight : 1f;
        return track;
    }

    public static List<PeakTrack> parse(String specification) {
        List<PeakTrack> result = new ArrayList<>();
        if (specification == null) return result;
        for (String token : specification.trim().split("\\s+")) {
            if (token.isEmpty()) continue;
            result.add(parseToken(token));
        }
        return result;
    }

    private static PeakTrack parseToken(String token) {
        String lower = token.toLowerCase(Locale.ROOT);
        try {
            if (lower.equals("auto")) return auto();
            if (lower.equals("fill") || lower.equals("fr")) return fill();
            if (lower.endsWith("fr")) return fill(Float.parseFloat(lower.substring(0, lower.length() - 2)));
            if (lower.endsWith("%")) return percent(Float.parseFloat(lower.substring(0, lower.length() - 1)));
            if (lower.endsWith("px")) return px(Math.round(Float.parseFloat(lower.substring(0, lower.length() - 2))));
            return px(Math.round(Float.parseFloat(lower)));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid track '" + token
                    + "', expected auto, 60, 60px, 25%, fill or 2fr");
        }
    }

    public Kind getKind() { return kind == null ? Kind.AUTO : kind; }
    public float getValue() { return value; }
    public int getMin() { return Math.max(0, min); }
    public int getMax() { return Math.max(0, max); }
    public PeakAlign getAlign() { return align == null ? PeakAlign.AUTO : align; }

    public PeakTrack min(int min) { this.min = Math.max(0, min); return this; }
    public PeakTrack max(int max) { this.max = Math.max(0, max); return this; }
    public PeakTrack align(PeakAlign align) { this.align = align == null ? PeakAlign.AUTO : align; return this; }
    public PeakTrack alignStart() { return align(PeakAlign.START); }
    public PeakTrack alignCenter() { return align(PeakAlign.CENTER); }
    public PeakTrack alignEnd() { return align(PeakAlign.END); }
}
