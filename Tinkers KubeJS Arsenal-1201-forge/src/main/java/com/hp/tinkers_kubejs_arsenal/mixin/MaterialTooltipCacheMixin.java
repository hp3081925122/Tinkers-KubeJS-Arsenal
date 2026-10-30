package com.hp.tinkers_kubejs_arsenal.mixin;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import slimeknights.tconstruct.library.client.materials.MaterialTooltipCache;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;

@Mixin(value = MaterialTooltipCache.class, remap = false)
public abstract class MaterialTooltipCacheMixin {
    @Shadow @Final @Mutable private static Map<MaterialVariantId, String> KEY_CACHE;
    @Shadow @Final @Mutable private static Map<MaterialVariantId, TextColor> COLOR_CACHE;
    @Shadow @Final @Mutable private static Map<MaterialVariantId, Component> DISPLAY_NAME_CACHE;
    @Shadow @Final @Mutable private static Map<MaterialVariantId, Component> COLORED_DISPLAY_NAME_CACHE;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void tkja$concurrentCaches(CallbackInfo ci) {
        KEY_CACHE = new ConcurrentHashMap<>();
        COLOR_CACHE = new ConcurrentHashMap<>();
        DISPLAY_NAME_CACHE = new ConcurrentHashMap<>();
        COLORED_DISPLAY_NAME_CACHE = new ConcurrentHashMap<>();
    }
}
