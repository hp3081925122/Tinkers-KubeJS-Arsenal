package com.hp.tinkers_kubejs_arsenal.mixin;

import com.hp.tinkers_kubejs_arsenal.kubejs.material.MaterialDataEventJS;
import com.hp.tinkers_kubejs_arsenal.kubejs.modifier.ArsenalModifierEvents;
import com.hp.tinkers_kubejs_arsenal.kubejs.modifier.ModifierDataEventJS;
import com.hp.tinkers_kubejs_arsenal.kubejs.material.ArsenalMaterialEvents;
import com.hp.tinkers_kubejs_arsenal.kubejs.material.MaterialTagEventJS;
import com.hp.tinkers_kubejs_arsenal.network.TinkerMaterialNetwork;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hp.tinkers_kubejs_arsenal.kubejs.tool.ArsenalToolBuilder;
import com.hp.tinkers_kubejs_arsenal.kubejs.tool.ArsenalPartBuilder;
import slimeknights.tconstruct.library.materials.MaterialRegistry;
import slimeknights.mantle.data.registry.IdAwareComponentRegistry;
import com.hp.tinkers_kubejs_arsenal.kubejs.tool.ToolDataEventJS;
import com.hp.tinkers_kubejs_arsenal.kubejs.modifier.ArsenalToolEvents;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
            ArsenalMaterialEvents.DATA.post(data);
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
            for (MaterialDataEventJS.MaterialDefinition extension : data.getExtensions().values()) {
                if (extension.isInvalid()) {
                    ConsoleJS.SERVER.error("Material extension " + extension.id() + " was not generated because it has errors");
                    continue;
                }
                if (extension.stats().size() > 0) {
                    JsonObject stats = new JsonObject();
                    stats.add("stats", extension.stats());
                    add(pack, extension.id(), "stats", stats);
                    added = true;
                }
                if (extension.hasTraits()) {
                    add(pack, extension.id(), "traits", extension.traits());
                    added = true;
                }
            }
            ModifierDataEventJS modifiers = new ModifierDataEventJS();
            ArsenalModifierEvents.DATA.post(modifiers);
            for (ModifierDataEventJS.ModifierDefinition modifier : modifiers.getModifiers().values()) {
                ResourceLocation id = modifier.id();
                if (modifier.isInvalid()) {
                    ConsoleJS.SERVER.error("Modifier " + id + " was not generated because its definition has errors");
                    continue;
                }
                pack.addJson(new ResourceLocation(id.getNamespace(), "tinkering/modifiers/" + id.getPath()), modifier.json());
                added = true;
            }
            ToolDataEventJS tools = new ToolDataEventJS();
            ArsenalToolEvents.DATA.post(tools);
            Map<String, JsonArray> itemTags = new LinkedHashMap<>();
            Map<ResourceLocation, List<ToolDataEventJS.ToolDefinitionJS>> armorSets = new LinkedHashMap<>();
            for (ArsenalPartBuilder part : ArsenalPartBuilder.PARTS.values()) {
                ResourceLocation id = part.id;
                if (MaterialRegistry.getInstance().getStatTypeLoader() instanceof IdAwareComponentRegistry<?> stats && !stats.getKeys().contains(part.getStatType())) {
                    ConsoleJS.SERVER.warn("Part " + id + " uses unknown stat type " + part.getStatType() + ", so no material can make it");
                }
                part.recipes().forEach((path, json) -> pack.addJson(new ResourceLocation(id.getNamespace(), "recipes/" + path), json));
                part.tags((tag, value) -> itemTags.computeIfAbsent(tag, key -> new JsonArray()).add(value));
                added = true;
            }
            for (ToolDataEventJS.ToolDefinitionJS tool : tools.getTools().values()) {
                ResourceLocation id = tool.id();
                if (tool.isInvalid()) {
                    ConsoleJS.SERVER.error("Tool " + id + " was not generated because its definition has errors");
                    continue;
                }
                pack.addJson(new ResourceLocation(id.getNamespace(), "tinkering/tool_definitions/" + id.getPath()), tool.definition());
                if (tool.builder().kind() == ArsenalToolBuilder.Kind.ARMOR) {
                    armorSets.computeIfAbsent(tool.builder().armorSet(), key -> new ArrayList<>()).add(tool);
                } else {
                    JsonObject layout;
                    try {
                        layout = tool.layout();
                    } catch (IllegalArgumentException e) {
                        ConsoleJS.SERVER.error(e.getMessage());
                        continue;
                    }
                    pack.addJson(new ResourceLocation(id.getNamespace(), "tinkering/station_layouts/" + id.getPath()), layout);
                }
                pack.addJson(new ResourceLocation(id.getNamespace(), "recipes/tools/building/" + id.getPath()), tool.recipe());
                tool.modifiableTags().forEach(tag -> itemTags.computeIfAbsent("tconstruct:modifiable/" + tag, key -> new JsonArray()).add(id.toString()));
                added = true;
            }
            for (Map.Entry<ResourceLocation, List<ToolDataEventJS.ToolDefinitionJS>> entry : armorSets.entrySet()) {
                ResourceLocation setId = entry.getKey();
                try {
                    pack.addJson(new ResourceLocation(setId.getNamespace(), "tinkering/station_layouts/" + setId.getPath() + "_armor"), ToolDataEventJS.armorLayout(setId, entry.getValue()));
                    added = true;
                } catch (IllegalArgumentException exception) {
                    ConsoleJS.SERVER.error(exception.getMessage());
                }
            }
            for (ResourceLocation id : ArsenalToolBuilder.TOOLS.keySet()) {
                if (!tools.getTools().containsKey(id)) {
                    ConsoleJS.SERVER.warn("Tool " + id + " is registered in startup_scripts but has no ArsenalToolEvents.data definition, so it has no stats and cannot be built");
                }
            }
            itemTags.forEach((tag, values) -> {
                JsonObject json = new JsonObject();
                json.addProperty("replace", false);
                json.add("values", values);
                ResourceLocation tagId = new ResourceLocation(tag);
                pack.addJson(new ResourceLocation(tagId.getNamespace(), "tags/items/" + tagId.getPath()), json);
            });
            TinkerMaterialNetwork.update(data.getMaterials(), modifiers.getModifiers());
            if (added && !ServerEvents.HIGH_DATA.hasListeners()) {
                this.tkja$pendingPack = pack;
            }
            MaterialTagEventJS tags = new MaterialTagEventJS();
            ArsenalMaterialEvents.TAGS.post(tags);
            for (var entry : tags.getTags().entrySet()) {
                pack.addJson(new ResourceLocation(entry.getKey().getNamespace(), "tinkering/tags/materials/" + entry.getKey().getPath()), entry.getValue().json());
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
