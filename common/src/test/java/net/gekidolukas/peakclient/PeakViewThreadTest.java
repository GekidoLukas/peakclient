package net.gekidolukas.peakclient;

import net.gekidolukas.peakclient.api.PeakClient;
import net.gekidolukas.peakclient.api.PeakClientApi;
import net.gekidolukas.peakclient.api.PeakClientApiHolder;
import net.gekidolukas.peakclient.server.PeakAudience;
import net.gekidolukas.peakclient.server.PeakView;
import net.gekidolukas.peakclient.ui.*;
import net.gekidolukas.peakclient.ui.patch.PeakPatch;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PeakViewThreadTest {
    private final Queue<Runnable> queue = new ArrayDeque<>();
    private boolean onServerThread;
    private RecordingPeakApi api;
    private PeakClientApi previous;
    private PeakAudience audience;

    private void drain() {
        boolean before = onServerThread;
        onServerThread = true;
        try {
            while (!queue.isEmpty()) queue.poll().run();
        } finally {
            onServerThread = before;
        }
    }

    @BeforeEach
    void install() {
        previous = PeakClientApiHolder.INSTANCE;
        api = new RecordingPeakApi();
        PeakClientApiHolder.INSTANCE = api;
        onServerThread = false;
        PeakServerThread.install(() -> onServerThread, queue::add);
        audience = PeakClient.audienceOfIds(List.of(UUID.randomUUID()));
        drain();
    }

    @AfterEach
    void restore() {
        audience.close();
        drain();
        PeakServerThread.reset();
        PeakClientApiHolder.INSTANCE = previous;
    }

    private PeakView open(PeakLabel label) {
        PeakView view = audience.open(PeakDocument.create("threaded").add(label));
        drain();
        return view;
    }

    @Test
    void operationsFromAnotherThreadAreQueuedAndApplyOnTheServerThread() {
        PeakLabel label = PeakLabel.create("l", "before");
        PeakView view = open(label);

        view.update(label, PeakPatch.label().text("after"));

        assertEquals("before", label.getText().getString());
        assertFalse(queue.isEmpty());
        drain();
        assertEquals("after", label.getText().getString());

        view.flush();
        drain();
        assertTrue(api.calls.stream().anyMatch(call -> call.name().equals("update:threaded")));
    }

    @Test
    void operationsKeepTheirOrder() {
        PeakLabel label = PeakLabel.create("l", "start");
        PeakView view = open(label);

        view.update("l", PeakPatch.label().text("first"));
        view.update("l", PeakPatch.label().text("second"));
        drain();

        assertEquals("second", label.getText().getString());
    }

    @Test
    void callsFromTheServerThreadRunImmediately() {
        PeakLabel label = PeakLabel.create("l", "before");
        PeakView view = open(label);

        onServerThread = true;
        view.update(label, PeakPatch.label().text("now"));

        assertEquals("now", label.getText().getString());
        assertTrue(queue.isEmpty());
    }

    @Test
    void patchesAreCopiedWhenTheyAreQueued() {
        PeakLabel label = PeakLabel.create("l", "before");
        PeakView view = open(label);

        var patch = PeakPatch.label().text("queued");
        view.update(label, patch);
        patch.text("changed afterwards");
        drain();

        assertEquals("queued", label.getText().getString());
    }

    @Test
    void typedReferencesPatchTheMatchingElement() {
        PeakLabel label = PeakLabel.create("l", "before");
        PeakView view = open(label);

        view.update(PeakRef.label("l"), patch -> patch.text("typed"));
        drain();

        assertEquals("typed", label.getText().getString());
    }

    @Test
    void referencesOfTheWrongTypeAreIgnored() {
        PeakLabel label = PeakLabel.create("l", "before");
        PeakView view = open(label);

        view.update(PeakRef.panel("l"), patch -> patch.background(0xFF0000));
        view.flush();
        drain();

        assertEquals("before", label.getText().getString());
        assertTrue(api.calls.stream().noneMatch(call -> call.name().equals("update:threaded")));
    }

    @Test
    void closingFromAnotherThreadClosesTheView() {
        PeakLabel label = PeakLabel.create("l", "before");
        PeakView view = open(label);

        view.close();
        assertFalse(view.isClosed());
        drain();
        assertTrue(view.isClosed());
    }
}
