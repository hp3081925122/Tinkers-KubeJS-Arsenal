package com.hp.tinkers_kubejs_arsenal.kubejs.modifier;

import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventGroupWrapper;
import dev.latvian.mods.kubejs.event.EventHandler;
import dev.latvian.mods.kubejs.event.Extra;
import dev.latvian.mods.kubejs.script.ScriptType;

public final class ArsenalModifierEvents {
    public static final EventGroup GROUP = EventGroup.of("ArsenalModifierEvents");
    public static final EventHandler DATA = GROUP.server("data", () -> ModifierDataEventJS.class);
    public static final EventHandler MELEE_DAMAGE = GROUP.server("meleeDamage", () -> ModifierHookEvents.MeleeDamageEventJS.class).extra(Extra.ID);
    public static final EventHandler AFTER_MELEE_HIT = GROUP.server("afterMeleeHit", () -> ModifierHookEvents.AttackEventJS.class).extra(Extra.ID);
    public static final EventHandler PROJECTILE_HIT = GROUP.server("projectileHit", () -> ModifierHookEvents.ProjectileHitEventJS.class).extra(Extra.ID);
    public static final EventHandler BLOCK_BREAK = GROUP.server("blockBreak", () -> ModifierHookEvents.BlockBreakEventJS.class).extra(Extra.ID);
    public static final EventHandler HURT = GROUP.server("hurt", () -> ModifierHookEvents.DamageTakenEventJS.class).extra(Extra.ID);
    public static final EventHandler DAMAGE = GROUP.server("damage", () -> ModifierHookEvents.DamageTakenEventJS.class).extra(Extra.ID);
    public static final EventHandler TOOL_STATS = GROUP.server("toolStats", () -> ToolStatsEventJS.class).extra(Extra.ID);
    public static final EventHandler ATTRIBUTES = GROUP.server("attributes", () -> ModifierHookEvents.AttributesEventJS.class).extra(Extra.ID);

    private ArsenalModifierEvents() {}

    public static void register() {
        GROUP.register();
    }

    public static EventGroupWrapper wrapper(ScriptType type) {
        return new EventGroupWrapper(type, GROUP);
    }
}
