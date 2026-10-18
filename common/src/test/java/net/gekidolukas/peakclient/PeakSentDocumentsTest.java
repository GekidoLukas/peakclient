package net.gekidolukas.peakclient;

import net.gekidolukas.peakclient.server.PeakSentDocuments;
import net.gekidolukas.peakclient.ui.PeakDocument;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class PeakSentDocumentsTest {
    private final UUID first = UUID.randomUUID();
    private final UUID second = UUID.randomUUID();

    @AfterEach
    void clean() {
        PeakSentDocuments.clearAll();
    }

    @Test
    void remembersDocumentsPerPlayer() {
        PeakDocument document = PeakDocument.create("test:doc");
        PeakSentDocuments.rememberDocument(first, "test:doc", document);

        assertSame(document, PeakSentDocuments.knownDocument(List.of(first), "test:doc"));
        assertNull(PeakSentDocuments.knownDocument(List.of(second), "test:doc"));
    }

    @Test
    void usesTheFirstPlayerThatKnowsTheDocument() {
        PeakDocument document = PeakDocument.create("test:doc");
        PeakSentDocuments.rememberDocument(second, "test:doc", document);

        assertSame(document, PeakSentDocuments.knownDocument(List.of(first, second), "test:doc"));
    }

    @Test
    void menusAndDocumentsAreSeparate() {
        PeakDocument document = PeakDocument.create("test:same");
        PeakDocument menu = PeakDocument.create("test:same");
        PeakSentDocuments.rememberDocument(first, "test:same", document);
        PeakSentDocuments.rememberMenu(first, "test:same", menu);

        assertSame(document, PeakSentDocuments.knownDocument(List.of(first), "test:same"));
        assertSame(menu, PeakSentDocuments.knownMenu(List.of(first), "test:same"));

        PeakSentDocuments.forgetMenu(first, "test:same");
        assertNull(PeakSentDocuments.knownMenu(List.of(first), "test:same"));
        assertSame(document, PeakSentDocuments.knownDocument(List.of(first), "test:same"));
    }

    @Test
    void forgetMenusKeepsDocuments() {
        PeakDocument document = PeakDocument.create("test:doc");
        PeakSentDocuments.rememberDocument(first, "test:doc", document);
        PeakSentDocuments.rememberMenu(first, "test:a", PeakDocument.create("test:a"));
        PeakSentDocuments.rememberMenu(first, "test:b", PeakDocument.create("test:b"));
        PeakDocument otherMenu = PeakDocument.create("test:a");
        PeakSentDocuments.rememberMenu(second, "test:a", otherMenu);

        PeakSentDocuments.forgetMenus(first);

        assertNull(PeakSentDocuments.knownMenu(List.of(first), "test:a"));
        assertNull(PeakSentDocuments.knownMenu(List.of(first), "test:b"));
        assertSame(document, PeakSentDocuments.knownDocument(List.of(first), "test:doc"));
        assertSame(otherMenu, PeakSentDocuments.knownMenu(List.of(second), "test:a"));
        assertNull(PeakSentDocuments.knownMenu(List.of(second), "test:b"));
    }

    @Test
    void forgetPlayerRemovesOnlyThatPlayer() {
        PeakDocument kept = PeakDocument.create("test:doc");
        PeakSentDocuments.rememberDocument(first, "test:doc", PeakDocument.create("test:doc"));
        PeakSentDocuments.rememberMenu(first, "test:menu", PeakDocument.create("test:menu"));
        PeakSentDocuments.rememberDocument(second, "test:doc", kept);

        PeakSentDocuments.forgetPlayer(first);

        assertNull(PeakSentDocuments.knownDocument(List.of(first), "test:doc"));
        assertNull(PeakSentDocuments.knownMenu(List.of(first), "test:menu"));
        assertSame(kept, PeakSentDocuments.knownDocument(List.of(second), "test:doc"));
    }

    @Test
    void clearAllEmptiesTheCache() {
        PeakSentDocuments.rememberDocument(first, "test:doc", PeakDocument.create("test:doc"));
        PeakSentDocuments.rememberMenu(second, "test:menu", PeakDocument.create("test:menu"));

        PeakSentDocuments.clearAll();

        assertNull(PeakSentDocuments.knownDocument(List.of(first), "test:doc"));
        assertNull(PeakSentDocuments.knownMenu(List.of(second), "test:menu"));
    }
}
