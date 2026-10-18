package net.gekidolukas.peakclient.ui;

import java.util.UUID;

public final class PeakId {
    private PeakId() {}

    public static String random() {
        return UUID.randomUUID().toString();
    }

    public static boolean isValid(String id) {
        if (id == null) return false;
        int first = id.indexOf(':');
        return first < 0 || id.indexOf(':', first + 1) < 0;
    }

    public static String require(String id, String kind) {
        if (id == null) {
            throw new IllegalArgumentException(kind + " id must not be null");
        }
        if (!isValid(id)) {
            throw new IllegalArgumentException(kind + " id '" + id + "' contains more than one ':'. "
                    + "Use a single ':' after the namespace and '/' for everything else, e.g. 'mygame:shop/title'");
        }
        return id;
    }

    public static String child(String parentId, String suffix) {
        return parentId + "/" + suffix;
    }
}
