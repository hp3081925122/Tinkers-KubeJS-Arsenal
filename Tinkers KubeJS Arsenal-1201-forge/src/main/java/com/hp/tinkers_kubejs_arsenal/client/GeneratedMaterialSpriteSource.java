package com.hp.tinkers_kubejs_arsenal.client;

import com.hp.tinkers_kubejs_arsenal.kubejs.tool.ArsenalToolBuilder;
import com.hp.tinkers_kubejs_arsenal.kubejs.material.StatTypeEventJS;
import com.hp.tinkers_kubejs_arsenal.kubejs.tool.ArsenalPartBuilder;
import net.minecraftforge.registries.ForgeRegistries;
import slimeknights.tconstruct.library.tools.part.IToolPart;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.SpriteSources;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import slimeknights.tconstruct.library.client.materials.MaterialGeneratorInfo;
import slimeknights.tconstruct.library.client.data.material.MaterialPartTextureGenerator;
import slimeknights.tconstruct.library.client.data.spritetransformer.GreyToSpriteTransformer;
import slimeknights.tconstruct.library.materials.stats.MaterialStatsId;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GeneratedMaterialSpriteSource implements SpriteSource {
    private static final Logger LOGGER = LoggerFactory.getLogger("TKJA Material Sprite Source");
    private static final ResourceLocation GENERATOR_CONFIG = new ResourceLocation("tconstruct", "tinkering/generator_part_textures.json");
    private static final String MATERIAL_FOLDER = "tinkering/materials";

    @Override
    public void run(ResourceManager manager, Output output) {
        GreyToSpriteTransformer.init();
        MaterialPartTextureGenerator.runCallbacks(null, manager);
        List<Part> parts = this.loadParts(manager);
        if (parts.isEmpty()) {
            return;
        }
        Map<ResourceLocation, Resource> resources = manager.listResources(MATERIAL_FOLDER, id -> id.getPath().endsWith(".json"));
        int generated = 0;
        for (Map.Entry<ResourceLocation, Resource> entry : resources.entrySet()) {
            generated += this.generateMaterial(manager, output, parts, entry.getKey(), entry.getValue());
        }
        if (generated > 0) {
            LOGGER.debug("Generated {} material part sprites", generated);
        }
    }

    @Override
    public net.minecraft.client.renderer.texture.atlas.SpriteSourceType type() {
        return SpriteSources.SINGLE_FILE;
    }

    private List<Part> loadParts(ResourceManager manager) {
        Resource resource = manager.getResource(GENERATOR_CONFIG).orElse(null);
        try {
            if (resource == null) {
                return List.of();
            }
            JsonObject root;
            try (var reader = resource.openAsReader()) {
                root = JsonParser.parseReader(reader).getAsJsonObject();
            }
            JsonArray jsonParts = root.getAsJsonArray("parts");
            if (jsonParts == null) {
                return List.of();
            }
            List<Part> parts = new ArrayList<>();
            for (JsonElement element : jsonParts) {
                JsonObject json = element.getAsJsonObject();
                ResourceLocation path = new ResourceLocation(json.get("path").getAsString());
                ResourceLocation stat = new ResourceLocation(json.get("stat_type").getAsString());
                boolean allowAnimated = !json.has("allow_animated") || json.get("allow_animated").getAsBoolean();
                boolean skipVariants = json.has("skip_variants") && json.get("skip_variants").getAsBoolean();
                parts.add(new Part(path, new MaterialStatsId(stat.getNamespace(), stat.getPath()), allowAnimated, skipVariants));
            }
            for (ArsenalToolBuilder tool : ArsenalToolBuilder.TOOLS.values()) {
                for (ArsenalToolBuilder.ToolPart part : tool.parts()) {
                    if (!(ForgeRegistries.ITEMS.getValue(part.item()) instanceof IToolPart toolPart)) {
                        LOGGER.warn("Tool {} part '{}' uses {}, which is not a Tinkers tool part; no material textures generated", tool.id, part.name(), part.item());
                        continue;
                    }
                    parts.add(new Part(part.texture(), toolPart.getStatType(), true, false));
                    if (tool.isBroken(part.name())) {
                        parts.add(new Part(part.texture().withSuffix("_broken"), toolPart.getStatType(), true, false));
                    }
                }
            }
            for (ArsenalPartBuilder part : ArsenalPartBuilder.PARTS.values()) {
                if (part.getStatType() != null) {
                    parts.add(new Part(part.getPartTexture(), part.getStatType(), true, false));
                }
            }
            return parts;
        } catch (Exception exception) {
            LOGGER.warn("Failed to load Tinkers Construct generator_part_textures.json", exception);
            return List.of();
        }
    }

    private int generateMaterial(ResourceManager manager, Output output, List<Part> parts, ResourceLocation file, Resource resource) {
        int generated = 0;
        try {
            JsonObject json;
            try (var reader = resource.openAsReader()) {
                json = JsonParser.parseReader(reader).getAsJsonObject();
            }
            JsonObject generatorJson = json.getAsJsonObject("generator");
            if (generatorJson == null) {
                return 0;
            }
            MaterialGeneratorInfo generator = MaterialGeneratorInfo.LOADABLE.deserialize(generatorJson);
            ResourceLocation materialTexture = this.materialTexture(file, json);
            String textureSuffix = this.textureSuffix(materialTexture);
            Set<MaterialStatsId> supportedStats = this.supportedStats(generatorJson);
            for (Part part : parts) {
                if ((generator.isVariant() && part.skipVariants()) || (!generatorJson.has("ignore_material_stats") || !generatorJson.get("ignore_material_stats").getAsBoolean()) && !supportedStats.contains(part.statType()) && !(StatTypeEventJS.TYPES.containsKey(part.statType()) && supportedStats.contains(StatTypeEventJS.TYPES.get(part.statType()).textureStat()))) {
                    continue;
                }
                NativeImage source = this.loadSource(manager, part.path(), json);
                if (source == null) {
                    continue;
                }
                NativeImage transformed = null;
                try {
                    transformed = generator.getTransformer().transformCopy(source, part.allowAnimated());
                    JsonObject metadataJson = part.allowAnimated() ? generator.getTransformer().animationMeta(source) : null;
                    AnimationMetadataSection metadata = metadataJson == null ? AnimationMetadataSection.EMPTY : AnimationMetadataSection.SERIALIZER.fromJson(metadataJson);
                    ResourceLocation outputId = part.path().withSuffix("_" + textureSuffix);
                    if (this.hasTexture(manager, outputId)) {
                        continue;
                    }
                    output.add(outputId, new ImageSupplier(outputId, transformed, metadata));
                    generated++;
                    transformed = null;
                } finally {
                    source.close();
                    if (transformed != null) {
                        transformed.close();
                    }
                }
            }
        } catch (Exception exception) {
            LOGGER.warn("Failed to generate material sprites for {}", file, exception);
        }
        return generated;
    }

    private NativeImage loadSource(ResourceManager manager, ResourceLocation part, JsonObject material) {
        JsonArray fallbacks = material.has("fallbacks") ? material.getAsJsonArray("fallbacks") : new JsonArray();
        for (JsonElement fallback : fallbacks) {
            NativeImage image = this.readImage(manager, part.withSuffix("_" + fallback.getAsString()));
            if (image != null) {
                return image;
            }
        }
        return this.readImage(manager, part);
    }

    private NativeImage readImage(ResourceManager manager, ResourceLocation id) {
        ResourceLocation texture = new ResourceLocation(id.getNamespace(), "textures/" + id.getPath() + ".png");
        Resource resource = manager.getResource(texture).orElse(null);
        try {
            if (resource == null) {
                return null;
            }
            try (var input = resource.open()) {
                return NativeImage.read(input);
            }
        } catch (IOException exception) {
            return null;
        }
    }

    private boolean hasTexture(ResourceManager manager, ResourceLocation id) {
        return manager.getResource(new ResourceLocation(id.getNamespace(), "textures/" + id.getPath() + ".png")).isPresent();
    }

    private ResourceLocation materialTexture(ResourceLocation file, JsonObject material) {
        if (material.has("texture")) {
            return new ResourceLocation(material.get("texture").getAsString());
        }
        String path = file.getPath();
        String prefix = MATERIAL_FOLDER + "/";
        if (path.startsWith(prefix)) {
            path = path.substring(prefix.length());
        }
        if (path.endsWith(".json")) {
            path = path.substring(0, path.length() - 5);
        }
        int split = path.lastIndexOf('/');
        if (split >= 0) {
            path = path.substring(0, split) + "_" + path.substring(split + 1);
        }
        return new ResourceLocation(file.getNamespace(), path);
    }

    private String textureSuffix(ResourceLocation texture) {
        return "minecraft".equals(texture.getNamespace()) ? texture.getPath() : texture.getNamespace() + "_" + texture.getPath();
    }

    private Set<MaterialStatsId> supportedStats(JsonObject generator) {
        Set<MaterialStatsId> stats = new HashSet<>();
        JsonArray values = generator.getAsJsonArray("supported_stats");
        if (values != null) {
            for (JsonElement value : values) {
                ResourceLocation id = new ResourceLocation(value.getAsString().contains(":") ? value.getAsString() : "tconstruct:" + value.getAsString());
                stats.add(new MaterialStatsId(id.getNamespace(), id.getPath()));
            }
        }
        return stats;
    }

    private record Part(ResourceLocation path, MaterialStatsId statType, boolean allowAnimated, boolean skipVariants) {}

    private static final class ImageSupplier implements SpriteSupplier {
        private final ResourceLocation id;
        private NativeImage image;
        private final AnimationMetadataSection metadata;

        private ImageSupplier(ResourceLocation id, NativeImage image, AnimationMetadataSection metadata) {
            this.id = id;
            this.image = image;
            this.metadata = metadata;
        }

        @Override
        public SpriteContents get() {
            NativeImage current = this.image;
            this.image = null;
            if (current == null) {
                return null;
            }
            return new SpriteContents(this.id, metadata.calculateFrameSize(current.getWidth(), current.getHeight()), current, this.metadata);
        }

        @Override
        public void discard() {
            if (this.image != null) {
                this.image.close();
                this.image = null;
            }
        }
    }
}
