package com.hp.tinkers_kubejs_arsenal.kubejs.modifier;

import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventGroupWrapper;
import dev.latvian.mods.kubejs.event.EventHandler;
import dev.latvian.mods.kubejs.script.ScriptType;

public final class ArsenalToolEvents {
    public static final EventGroup GROUP = EventGroup.of("ArsenalToolEvents");
    public static final EventHandler STATS = GROUP.server("stats", () -> ToolStatsEventJS.class);

    private ArsenalToolEvents() {}

    public static void register() {
        GROUP.register();
    }

    public static EventGroupWrapper wrapper(ScriptType type) {
        return new EventGroupWrapper(type, GROUP);
    }
}
