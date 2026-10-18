package net.gekidolukas.peakclient;

import net.gekidolukas.peakclient.api.PeakClientApiHolder;
import net.gekidolukas.peakclient.api.PeakRemotePages;
import net.gekidolukas.peakclient.helper.PeakHud;
import net.gekidolukas.peakclient.helper.PeakTaskRegistry;
import net.gekidolukas.peakclient.server.PeakAudiences;
import net.gekidolukas.peakclient.server.PeakClientApiImpl;
import net.gekidolukas.peakclient.server.PeakClients;
import net.gekidolukas.peakclient.server.PeakMenuSessions;
import net.gekidolukas.peakclient.server.PeakPlayerStates;
import net.gekidolukas.peakclient.server.PeakRecipients;
import net.gekidolukas.peakclient.server.PeakSentDocuments;

import java.util.UUID;

public final class PeakClientMod {
    public static final String MOD_ID = "peakclient";



    /// TODO
    /// - Add JSON file loading for docs and for elements on server
    /// - Visibility Conditions (Like selected Hotbarslot)

    public static void init() {
        PeakClientApiHolder.INSTANCE = new PeakClientApiImpl();
    }

    public static void onPlayerDisconnected(UUID playerId) {
        PeakHud.clearPlayer(playerId);
        PeakTaskRegistry.stopAllForPlayer(playerId);
        PeakSentDocuments.forgetPlayer(playerId);
        PeakRecipients.forget(playerId);
        PeakMenuSessions.forget(playerId);
        PeakRemotePages.forgetPlayer(playerId);
        PeakClients.remove(playerId);
        PeakPlayerStates.markOffline(playerId);
        PeakAudiences.refreshAll();
    }
}
