package com.hp.tinkers_kubejs_arsenal;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import com.hp.tinkers_kubejs_arsenal.network.TinkerMaterialNetwork;
import com.hp.tinkers_kubejs_arsenal.kubejs.modifier.ArsenalEventsModule;
import com.hp.tinkers_kubejs_arsenal.kubejs.tool.ArsenalToolTabs;

@Mod(TinkersKubejsArsenal.MOD_ID)
public final class TinkersKubejsArsenal {
    public static final String MOD_ID = "tinkers_kubejs_arsenal";

    public TinkersKubejsArsenal(FMLJavaModLoadingContext context) {
        TinkerMaterialNetwork.init();
        MinecraftForge.EVENT_BUS.register(TinkerMaterialNetwork.class);
        context.getModEventBus().addListener(ArsenalEventsModule::register);
        context.getModEventBus().addListener(ArsenalToolTabs::buildContents);
        context.getModEventBus().addListener(com.hp.tinkers_kubejs_arsenal.kubejs.material.ScriptMaterialStats::register);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> context.getModEventBus().addListener(com.hp.tinkers_kubejs_arsenal.client.SyncedMaterialClient::addPackFinders));
    }
}
