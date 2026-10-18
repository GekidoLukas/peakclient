package net.gekidolukas.peakclient.server;

import net.gekidolukas.peakclient.PeakClientServer;
import net.gekidolukas.peakclient.PeakServerThread;
import net.gekidolukas.peakclient.api.PeakClient;
import net.gekidolukas.peakclient.api.PeakMenus;
import net.gekidolukas.peakclient.api.PeakVanillaHud;
import net.gekidolukas.peakclient.helper.PeakPlayers;
import net.gekidolukas.peakclient.helper.PeakTargetedTask;
import net.gekidolukas.peakclient.helper.PeakTimerSync;
import net.gekidolukas.peakclient.helper.PeakWaypointTracker;
import net.gekidolukas.peakclient.ui.PeakDocument;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.scores.PlayerTeam;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BooleanSupplier;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

public final class PeakAudience {
    public interface Listener {
        void onJoin(UUID playerId);
        void onLeave(UUID playerId);
    }

    private final Supplier<Collection<UUID>> source;
    private final Set<UUID> active = ConcurrentHashMap.newKeySet();
    private final List<Listener> listeners = new CopyOnWriteArrayList<>();
    private final List<PeakView> views = new CopyOnWriteArrayList<>();
    private final List<PeakScheduler.Handle> handles = new CopyOnWriteArrayList<>();
    private final Map<PeakTargetedTask, Listener> boundTasks = new ConcurrentHashMap<>();
    private final PeakAudienceValues values = new PeakAudienceValues(this);
    private volatile boolean closed = false;
    private PeakVanillaHud vanillaHud;

    private PeakAudience(Supplier<Collection<UUID>> source) {
        this.source = source;
        PeakAudiences.register(this);
        refresh();
    }

    public static PeakAudience fixed(Collection<UUID> playerIds) {
        Set<UUID> copy = new HashSet<>(playerIds);
        return new PeakAudience(() -> copy);
    }

    public static PeakAudience players(Collection<ServerPlayer> players) {
        return fixed(PeakPlayers.ids(players));
    }

    public static PeakAudience all() {
        return new PeakAudience(() -> {
            MinecraftServer server = PeakClientServer.server();
            return server == null ? List.of() : PeakPlayers.all(server);
        });
    }

    public static PeakAudience team(String teamName) {
        return new PeakAudience(() -> {
            MinecraftServer server = PeakClientServer.server();
            if (server == null) return List.of();
            PlayerTeam team = server.getScoreboard().getPlayerTeam(teamName);
            if (team == null) return List.of();
            List<UUID> ids = new ArrayList<>();
            for (String name : team.getPlayers()) {
                ServerPlayer player = server.getPlayerList().getPlayerByName(name);
                if (player != null) ids.add(player.getUUID());
            }
            return ids;
        });
    }

    public static PeakAudience dimension(ResourceKey<Level> dimension) {
        return new PeakAudience(() -> {
            MinecraftServer server = PeakClientServer.server();
            if (server == null) return List.of();
            List<UUID> ids = new ArrayList<>();
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                if (player.level().dimension().equals(dimension)) ids.add(player.getUUID());
            }
            return ids;
        });
    }

    public static PeakAudience dynamic(Supplier<Collection<UUID>> supplier) {
        return new PeakAudience(supplier);
    }

    public Collection<UUID> members() { return List.copyOf(active); }

    public boolean isClosed() { return closed; }

    public PeakAudienceValues values() { return values; }

    public synchronized PeakVanillaHud vanillaHud() {
        if (vanillaHud == null) {
            PeakVanillaHud hud = PeakVanillaHud.tracked(this::members);
            listeners.add(new Listener() {
                @Override public void onJoin(UUID playerId) { hud.replayTo(playerId); }
                @Override public void onLeave(UUID playerId) { hud.restoreFor(playerId); }
            });
            vanillaHud = hud;
        }
        return vanillaHud;
    }

    public void addListener(Listener listener) { listeners.add(listener); }

    public void removeListener(Listener listener) { listeners.remove(listener); }

    public PeakView open(PeakDocument document) {
        PeakView view = new PeakView(this, document);
        views.add(view);
        PeakServerThread.run(view::open);
        return view;
    }

    public PeakView menuView(PeakDocument menu) {
        if (!menu.isMenu()) {
            throw new IllegalArgumentException("Document '" + menu.getId() + "' is not a menu, create it with PeakMenu.create(...)");
        }
        PeakView view = new PeakView(this, menu, PeakViewTransport.MENU);
        views.add(view);
        PeakServerThread.run(view::open);
        return view;
    }

    public PeakMenus menus() {
        return new PeakMenus(this::members);
    }

    public <T extends PeakTargetedTask> T bind(T task) {
        task.keepAlive(true);
        task.addTargets(active);
        Listener listener = new Listener() {
            @Override public void onJoin(UUID playerId) { task.addTarget(playerId); }
            @Override public void onLeave(UUID playerId) { task.removeTarget(playerId); }
        };
        listeners.add(listener);
        boundTasks.put(task, listener);
        return task;
    }

    public PeakWaypointTracker waypointTracker(String waypointId, Entity trackedEntity) {
        return bind(PeakWaypointTracker.create(waypointId, trackedEntity, PeakClient.to(List.<UUID>of())));
    }

    public PeakWaypointTracker waypointTracker(String waypointId, UUID trackedEntityUuid) {
        return bind(PeakWaypointTracker.create(waypointId, trackedEntityUuid, PeakClient.to(List.<UUID>of())));
    }

    public PeakTimerSync timerSync(String documentId, String timerId, LongSupplier valueTicks, BooleanSupplier running) {
        return bind(PeakTimerSync.create(documentId, timerId, PeakClient.to(List.<UUID>of()), valueTicks, running));
    }

    public PeakScheduler.Handle after(int ticks, Runnable action) {
        PeakScheduler.Handle handle = PeakScheduler.after(ticks, action);
        handles.add(handle);
        return handle;
    }

    public PeakScheduler.Handle every(int ticks, Runnable action) {
        PeakScheduler.Handle handle = PeakScheduler.every(ticks, action);
        handles.add(handle);
        return handle;
    }

    public void refresh() {
        if (!PeakServerThread.isServerThread()) {
            PeakServerThread.run(this::refresh);
            return;
        }
        if (closed) return;
        Set<UUID> next = compute();
        List<UUID> left = new ArrayList<>();
        for (UUID id : active) if (!next.contains(id)) left.add(id);
        List<UUID> joined = new ArrayList<>();
        for (UUID id : next) if (!active.contains(id)) joined.add(id);

        active.removeAll(left);
        active.addAll(joined);

        for (UUID id : left) for (Listener listener : listeners) listener.onLeave(id);
        for (UUID id : joined) {
            values.onJoin(id);
            for (PeakView view : views) view.onJoin(id);
            for (Listener listener : listeners) listener.onJoin(id);
        }
    }

    public void close() {
        if (!PeakServerThread.isServerThread()) {
            PeakServerThread.run(this::close);
            return;
        }
        if (closed) return;
        PeakVanillaHud hud;
        synchronized (this) {
            hud = vanillaHud;
        }
        if (hud != null) hud.restoreAll();
        for (PeakView view : new ArrayList<>(views)) view.close();
        closed = true;
        for (PeakScheduler.Handle handle : handles) handle.cancel();
        for (PeakTargetedTask task : boundTasks.keySet()) {
            task.keepAlive(false);
            task.stop();
        }
        boundTasks.clear();
        listeners.clear();
        PeakAudiences.unregister(this);
    }

    void flushViews() {
        for (PeakView view : views) view.flush();
    }

    void removeView(PeakView view) {
        views.remove(view);
    }

    private Set<UUID> compute() {
        Set<UUID> result = new HashSet<>();
        for (UUID id : source.get()) {
            if (PeakClientServer.resolvePlayer(id) != null && PeakClients.hasClient(id)) result.add(id);
        }
        return result;
    }
}
