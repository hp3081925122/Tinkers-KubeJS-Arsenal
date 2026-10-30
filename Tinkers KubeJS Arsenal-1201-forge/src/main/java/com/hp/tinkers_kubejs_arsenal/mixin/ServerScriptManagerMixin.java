package com.hp.tinkers_kubejs_arsenal.mixin;

import com.hp.tinkers_kubejs_arsenal.kubejs.material.MaterialDataEventJS;
import com.hp.tinkers_kubejs_arsenal.kubejs.material.TinkerMaterialEvents;
import com.google.gson.JsonObject;
import dev.latvian.mods.kubejs.bindings.event.ServerEvents;
import dev.latvian.mods.kubejs.event.EventJS;
import dev.latvian.mods.kubejs.event.EventResult;
import dev.latvian.mods.kubejs.script.data.DataPackEventJS;
import dev.latvian.mods.kubejs.server.ServerScriptManager;
import dev.latvian.mods.kubejs.util.ConsoleJS;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ServerScriptManager.class, remap = false)
public abstract class ServerScriptManagerMixin {
    @Unique
    private DataPackEventJS tkja$pendingPack;

    @ModifyArg(method = "wrapResourceManager", at = @At(value = "INVOKE", target = "Ldev/latvian/mods/kubejs/event/EventHandler;post(Ldev/latvian/mods/kubejs/script/ScriptTypeHolder;Ldev/latvian/mods/kubejs/event/EventJS;)Ldev/latvian/mods/kubejs/event/EventResult;", ordinal = 1), index = 1)
    private EventJS tkja$materials(EventJS event) {
        this.tkja$pendingPack = null;
        if (event instanceof DataPackEventJS pack) {
            MaterialDataEventJS data = new MaterialDataEventJS();
            TinkerMaterialEvents.DATA.post(data);
            boolean added = false;
            for (MaterialDataEventJS.MaterialDefinition material : data.getMaterials().values()) {
                ResourceLocation id = material.id();
                if (material.isInvalid()) {
                    ConsoleJS.SERVER.error("Material " + id + " was not generated because its definition has errors");
                    continue;
                }
                if (material.stats().size() == 0) {
                    ConsoleJS.SERVER.warn("Material " + id + " has no stats, so no tool parts can be made from it");
                }
                add(pack, id, "definition", material.definition());
                if (material.stats().size() > 0) {
                    JsonObject stats = new JsonObject();
                    stats.add("stats", material.stats());
                    add(pack, id, "stats", stats);
                }
                if (material.hasTraits()) {
                    add(pack, id, "traits", material.traits());
                }
                added = true;
            }
            if (added && !ServerEvents.HIGH_DATA.hasListeners()) {
                this.tkja$pendingPack = pack;
            }
        }
        return event;
    }

    @Inject(method = "wrapResourceManager", at = @At(value = "INVOKE", target = "Ldev/latvian/mods/kubejs/event/EventHandler;post(Ldev/latvian/mods/kubejs/script/ScriptTypeHolder;Ldev/latvian/mods/kubejs/event/EventJS;)Ldev/latvian/mods/kubejs/event/EventResult;", ordinal = 1, shift = At.Shift.AFTER))
    private void tkja$registerPack(CloseableResourceManager original, CallbackInfoReturnable<?> cir) {
        if (this.tkja$pendingPack != null) {
            ((DataPackEventJSInvoker) this.tkja$pendingPack).tkja$afterPosted(EventResult.PASS);
            this.tkja$pendingPack = null;
        }
    }

    @Unique
    private static void add(DataPackEventJS pack, ResourceLocation id, String folder, JsonObject json) {
        pack.addJson(new ResourceLocation(id.getNamespace(), "tinkering/materials/" + folder + "/" + id.getPath()), json);
    }
}
