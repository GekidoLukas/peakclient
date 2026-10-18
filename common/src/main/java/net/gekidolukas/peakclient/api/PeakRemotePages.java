package net.gekidolukas.peakclient.api;

import net.gekidolukas.peakclient.server.PeakMenuActionEvent;
import net.gekidolukas.peakclient.server.PeakMenuEvents;
import net.gekidolukas.peakclient.ui.PeakElement;
import net.gekidolukas.peakclient.ui.PeakId;
import net.gekidolukas.peakclient.ui.PeakUiOperation;
import net.gekidolukas.peakclient.ui.template.PeakPagedList;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public final class PeakRemotePages {
    @FunctionalInterface
    public interface Loader {
        List<? extends PeakElement<?>> load(ServerPlayer player, int page);
    }

    private static final Set<PeakRemotePages> ATTACHED = ConcurrentHashMap.newKeySet();

    private final String menuId;
    private final PeakPagedList list;
    private final Loader loader;
    private final Map<UUID, Map<Integer, List<String>>> loaded = new ConcurrentHashMap<>();
    private volatile boolean detached;

    private PeakRemotePages(String menuId, PeakPagedList list, Loader loader) {
        this.menuId = menuId;
        this.list = list;
        this.loader = loader;
    }

    public static PeakRemotePages attach(String menuId, PeakPagedList list, Loader loader) {
        PeakId.require(menuId, "Menu");
        Objects.requireNonNull(list, "list");
        Objects.requireNonNull(loader, "loader");
        if (!list.isRemote()) {
            throw new IllegalArgumentException("Paged list '" + list.id() + "' is not remote, create it with PeakPagedList.remote(...)");
        }
        for (PeakRemotePages previous : ATTACHED) {
            if (previous.menuId.equals(menuId) && previous.list.id().equals(list.id())) previous.detach();
        }
        PeakRemotePages pages = new PeakRemotePages(menuId, list, loader);
        ATTACHED.add(pages);
        PeakMenuEvents.set(menuId, PeakPagedList.loadActionId(list.id()), pages::onLoad);
        return pages;
    }

    public String menuId() {
        return menuId;
    }

    public String listId() {
        return list.id();
    }

    public int pageCount() {
        return list.pageCount();
    }

    public boolean isLoaded(UUID playerId, int page) {
        Map<Integer, List<String>> pages = loaded.get(playerId);
        return pages != null && pages.containsKey(page);
    }

    public PeakRemotePages sendFirstPage(ServerPlayer player) {
        loaded.remove(player.getUUID());
        send(player, loadOperations(player.getUUID(), 0, false, () -> loader.load(player, 0)));
        return this;
    }

    public PeakRemotePages sendFirstPage(Collection<ServerPlayer> players) {
        for (ServerPlayer player : List.copyOf(players)) sendFirstPage(player);
        return this;
    }

    public PeakRemotePages refresh(ServerPlayer player, int page) {
        int target = clamp(page);
        send(player, loadOperations(player.getUUID(), target, true, () -> loader.load(player, target)));
        return this;
    }

    public PeakRemotePages refresh(Collection<ServerPlayer> players, int page) {
        for (ServerPlayer player : List.copyOf(players)) refresh(player, page);
        return this;
    }

    public PeakRemotePages invalidate(ServerPlayer player, int page) {
        send(player, invalidateOperations(player.getUUID(), page));
        return this;
    }

    public PeakRemotePages invalidate(Collection<ServerPlayer> players, int page) {
        for (ServerPlayer player : List.copyOf(players)) invalidate(player, page);
        return this;
    }

    public PeakRemotePages invalidateAll(ServerPlayer player) {
        send(player, invalidateAllOperations(player.getUUID()));
        return this;
    }

    public PeakRemotePages invalidateAll(Collection<ServerPlayer> players) {
        for (ServerPlayer player : List.copyOf(players)) invalidateAll(player);
        return this;
    }

    public void detach() {
        if (detached) return;
        detached = true;
        ATTACHED.remove(this);
        loaded.clear();
        PeakMenuEvents.set(menuId, PeakPagedList.loadActionId(list.id()), event -> {});
    }

    private void onLoad(PeakMenuActionEvent event) {
        if (detached) return;
        String raw = event.arg(PeakPagedList.LOAD_PAGE_ARG);
        if (raw == null) return;
        int requested;
        try {
            requested = Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            return;
        }
        ServerPlayer player = event.player();
        int page = clamp(requested);
        send(player, loadOperations(player.getUUID(), page, false, () -> loader.load(player, page)));
    }

    int clamp(int page) {
        return Math.max(0, Math.min(page, list.pageCount() - 1));
    }

    List<PeakUiOperation> loadOperations(UUID playerId, int page, boolean force,
                                         Supplier<List<? extends PeakElement<?>>> source) {
        Map<Integer, List<String>> pages = loaded.computeIfAbsent(playerId, id -> new ConcurrentHashMap<>());
        if (!force && pages.containsKey(page)) return List.of();

        List<? extends PeakElement<?>> elements = source.get();
        List<PeakUiOperation> operations = new ArrayList<>();
        List<String> previous = pages.get(page);
        if (previous == null) {
            operations.add(new PeakUiOperation.Delete(PeakPagedList.loadingId(list.id(), page)));
        } else {
            for (String id : previous) operations.add(new PeakUiOperation.Delete(id));
        }
        List<String> ids = new ArrayList<>();
        String content = PeakPagedList.contentId(list.id(), page);
        if (elements != null) {
            for (PeakElement<?> element : elements) {
                operations.add(new PeakUiOperation.Add(content, element));
                ids.add(element.getId());
            }
        }
        pages.put(page, ids);
        return operations;
    }

    List<PeakUiOperation> invalidateOperations(UUID playerId, int page) {
        Map<Integer, List<String>> pages = loaded.get(playerId);
        if (pages == null) return List.of();
        List<String> ids = pages.remove(page);
        if (ids == null) return List.of();
        List<PeakUiOperation> operations = new ArrayList<>();
        appendReset(operations, page, ids);
        return operations;
    }

    List<PeakUiOperation> invalidateAllOperations(UUID playerId) {
        Map<Integer, List<String>> pages = loaded.remove(playerId);
        if (pages == null) return List.of();
        List<PeakUiOperation> operations = new ArrayList<>();
        for (Map.Entry<Integer, List<String>> entry : new TreeMap<>(pages).entrySet()) {
            appendReset(operations, entry.getKey(), entry.getValue());
        }
        return operations;
    }

    private void appendReset(List<PeakUiOperation> operations, int page, List<String> ids) {
        for (String id : ids) operations.add(new PeakUiOperation.Delete(id));
        operations.add(new PeakUiOperation.Add(PeakPagedList.contentId(list.id(), page), list.loadingElement(page)));
    }

    private void send(ServerPlayer player, List<PeakUiOperation> operations) {
        if (operations.isEmpty()) return;
        PeakClient.to(player).menus().update(menuId, operations);
    }

    static void onMenuSent(String menuId, Collection<UUID> playerIds) {
        for (PeakRemotePages pages : ATTACHED) {
            if (!pages.menuId.equals(menuId)) continue;
            for (UUID id : playerIds) pages.loaded.remove(id);
        }
    }

    public static void forgetPlayer(UUID playerId) {
        for (PeakRemotePages pages : ATTACHED) pages.loaded.remove(playerId);
    }

    public static void clearAll() {
        for (PeakRemotePages pages : ATTACHED) pages.loaded.clear();
    }
}
