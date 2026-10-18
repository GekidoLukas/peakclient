package net.gekidolukas.peakclient.fabric;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.gekidolukas.peakclient.PeakClientMod;
import net.gekidolukas.peakclient.PeakClientServer;
import net.gekidolukas.peakclient.network.PeakUiDocumentPayload;
import net.gekidolukas.peakclient.network.PeakUiUpdatePayload;

public final class PeakClientModFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        PeakClientMod.init();
        PeakClientFabricBootstrap.registerCommon();
    }
}
