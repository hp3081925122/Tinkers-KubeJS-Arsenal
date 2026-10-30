package com.hp.tinkers_kubejs_arsenal.mixin;

import dev.latvian.mods.kubejs.event.EventResult;
import dev.latvian.mods.kubejs.script.data.DataPackEventJS;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = DataPackEventJS.class, remap = false)
public interface DataPackEventJSInvoker {
    @Invoker("afterPosted")
    void tkja$afterPosted(EventResult result);
}
