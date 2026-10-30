package com.hp.tinkers_kubejs_arsenal.mixin;

import com.hp.tinkers_kubejs_arsenal.client.GeneratedMaterialSpriteSource;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.atlas.SpriteResourceLoader;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(SpriteResourceLoader.class)
public abstract class SpriteResourceLoaderMixin {
    @Shadow @Final private List<SpriteSource> sources;

    @Inject(method = "load", at = @At("RETURN"))
    private static void tkja$appendMaterialSource(ResourceManager manager, ResourceLocation atlas, CallbackInfoReturnable<SpriteResourceLoader> callback) {
        if (new ResourceLocation("minecraft", "blocks").equals(atlas)) {
            SpriteResourceLoaderMixin loader = (SpriteResourceLoaderMixin) (Object) callback.getReturnValue();
            loader.sources.add(new GeneratedMaterialSpriteSource());
        }
    }
}
