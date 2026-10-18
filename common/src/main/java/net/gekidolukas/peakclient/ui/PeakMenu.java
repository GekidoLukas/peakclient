package net.gekidolukas.peakclient.ui;

import java.util.function.Consumer;

public final class PeakMenu {
    private PeakMenu() {}

    public static PeakDocument create(String id) {
        return new PeakDocument(id).menu(PeakMenuSettings.create());
    }

    public static PeakDocument create(String id, Consumer<PeakMenuSettings> configure) {
        PeakMenuSettings settings = PeakMenuSettings.create();
        configure.accept(settings);
        return new PeakDocument(id).menu(settings);
    }
}
