package com.hp.tinkers_kubejs_arsenal.kubejs.material;

import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventGroupWrapper;

public final class TinkerMaterialEvents {
    public static final EventGroup GROUP = EventGroup.of("TinkerMaterialEvents");
    public static final dev.latvian.mods.kubejs.event.EventHandler DATA = GROUP.server("data", () -> MaterialDataEventJS.class);
    public static final dev.latvian.mods.kubejs.event.EventHandler ASSETS = GROUP.client("assets", () -> MaterialAssetEventJS.class);
    private TinkerMaterialEvents() {}
    public static void register() { GROUP.register(); }
    public static EventGroupWrapper wrapper(dev.latvian.mods.kubejs.script.ScriptType type) { return new EventGroupWrapper(type, GROUP); }
}
