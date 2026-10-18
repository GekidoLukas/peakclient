package net.gekidolukas.peakclienttest.common;

import java.util.List;

public interface PeakElementTest {
    String name();
    String description();
    List<PeakElementTestStep> steps();

    default String documentId() {
        return "peaktest:element/" + name();
    }
}
