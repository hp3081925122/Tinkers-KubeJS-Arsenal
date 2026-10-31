package com.hp.tinkers_kubejs_arsenal.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hp.tinkers_kubejs_arsenal.TinkersKubejsArsenal;
import com.hp.tinkers_kubejs_arsenal.kubejs.material.StatTypeEventJS;
import com.hp.tinkers_kubejs_arsenal.kubejs.tool.ArsenalPartBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import slimeknights.mantle.client.book.BookLoader;
import slimeknights.mantle.client.book.data.BookData;
import slimeknights.mantle.client.book.data.SectionData;
import slimeknights.mantle.client.book.repository.BookRepository;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.client.book.TinkerBook;
import slimeknights.tconstruct.library.client.book.content.material.SingleMaterialStatContent;
import slimeknights.tconstruct.library.client.book.sectiontransformer.materials.TierRangeMaterialSectionTransformer;
import slimeknights.tconstruct.library.materials.IMaterialRegistry;
import slimeknights.tconstruct.library.materials.MaterialRegistry;
import slimeknights.tconstruct.library.materials.definition.IMaterial;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;
import slimeknights.tconstruct.library.materials.stats.MaterialStatsId;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public final class ArsenalBookSections {
    private static final ResourceLocation PAGE_ID = new ResourceLocation(TinkersKubejsArsenal.MOD_ID, "script_material");
    private static final ResourceLocation LANGUAGE = new ResourceLocation(TinkersKubejsArsenal.MOD_ID, "book/script_materials/language.lang");

    private ArsenalBookSections() {}

    public static void register(FMLClientSetupEvent event) {
        if (StatTypeEventJS.TYPES.isEmpty()) {
            return;
        }
        event.enqueueWork(() -> {
            for (MaterialStatsId stat : StatTypeEventJS.TYPES.keySet()) {
                TierRangeMaterialSectionTransformer.registerMaterialType(stat, (variant, detailed) -> new Page(variant, detailed, stat));
            }
            add(TinkerBook.MATERIALS_AND_YOU, 0, 1, true, false);
            add(TinkerBook.PUNY_SMELTING, 2, 2, true, false);
            add(TinkerBook.MIGHTY_SMELTING, 3, 3, true, false);
            add(TinkerBook.ENCYCLOPEDIA, 0, Short.MAX_VALUE, false, true);
        });
    }

    private static void add(BookData book, int minTier, int maxTier, boolean excludeDistant, boolean detailed) {
        book.addRepository(new Repository(minTier, maxTier, excludeDistant, detailed));
    }

    private static String sectionName(MaterialStatsId stat) {
        return ("arsenal_" + stat.getNamespace() + "_" + stat.getPath()).replace('/', '_').replace('.', '_').toLowerCase();
    }

    private static class Repository extends BookRepository {
        private final int minTier;
        private final int maxTier;
        private final boolean excludeDistant;
        private final boolean detailed;

        private Repository(int minTier, int maxTier, boolean excludeDistant, boolean detailed) {
            this.minTier = minTier;
            this.maxTier = maxTier;
            this.excludeDistant = excludeDistant;
            this.detailed = detailed;
        }

        private boolean hasMaterial(MaterialStatsId stat) {
            IMaterialRegistry registry = MaterialRegistry.getInstance();
            for (IMaterial material : MaterialRegistry.getMaterials()) {
                int tier = material.getTier();
                if (tier < this.minTier || tier > this.maxTier) {
                    continue;
                }
                if (this.excludeDistant && registry.isInTag(material.getIdentifier(), TinkerTags.Materials.DISTANT)) {
                    continue;
                }
                if (registry.getMaterialStats(material.getIdentifier(), stat).isPresent()) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public List<SectionData> getSections() {
            JsonArray sections = new JsonArray();
            for (MaterialStatsId stat : StatTypeEventJS.TYPES.keySet()) {
                if (!this.hasMaterial(stat)) {
                    continue;
                }
                JsonObject tier = new JsonObject();
                tier.addProperty("min", this.minTier);
                tier.addProperty("max", this.maxTier);
                JsonObject materialTier = new JsonObject();
                materialTier.addProperty("type", stat.toString());
                materialTier.addProperty("detailed", this.detailed);
                materialTier.add("tier", tier);
                if (this.excludeDistant) {
                    JsonObject predicate = new JsonObject();
                    predicate.addProperty("type", "mantle:inverted");
                    predicate.addProperty("inverted_type", "mantle:tag");
                    predicate.addProperty("tag", TinkerTags.Materials.DISTANT.location().toString());
                    materialTier.add("predicate", predicate);
                }
                JsonObject extraData = new JsonObject();
                extraData.add("tconstruct:material_tier", materialTier);
                JsonObject icon = new JsonObject();
                icon.addProperty("item", ArsenalPartBuilder.PARTS.values().stream().filter(part -> stat.equals(part.getStatType())).findFirst().map(part -> part.id.toString()).orElse("tconstruct:materials_and_you"));
                JsonObject section = new JsonObject();
                section.addProperty("name", sectionName(stat));
                section.addProperty("data", "no-load");
                section.add("extraData", extraData);
                section.add("icon", icon);
                sections.add(section);
            }
            return new java.util.ArrayList<>(Arrays.asList(BookLoader.getGson().fromJson(sections, SectionData[].class)));
        }

        @Nullable
        @Override
        public ResourceLocation getResourceLocation(@Nullable String path, boolean safe) {
            if ("language.lang".equals(path)) {
                return LANGUAGE;
            }
            return safe ? new ResourceLocation("") : null;
        }

        @Override
        public Optional<Resource> getLocation(@Nullable ResourceLocation location) {
            if (!LANGUAGE.equals(location)) {
                return Optional.empty();
            }
            StringBuilder lang = new StringBuilder();
            for (MaterialStatsId stat : StatTypeEventJS.TYPES.keySet()) {
                lang.append(sectionName(stat)).append('=').append(I18n.get("stat." + stat.getNamespace() + "." + stat.getPath().replace('/', '.'))).append('\n');
            }
            byte[] bytes = lang.toString().getBytes(StandardCharsets.UTF_8);
            return Optional.of(new Resource(Minecraft.getInstance().getVanillaPackResources(), () -> new ByteArrayInputStream(bytes)));
        }

        @Override
        public String resourceToString(@Nullable Resource resource, boolean skipComments) {
            if (resource == null) {
                return "";
            }
            try (var stream = resource.open()) {
                return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                return "";
            }
        }
    }

    private static class Page extends SingleMaterialStatContent {
        private final transient MaterialStatsId stat;

        private Page(MaterialVariantId variant, boolean detailed, MaterialStatsId stat) {
            super(variant, detailed);
            this.stat = stat;
        }

        @Override
        public ResourceLocation getId() {
            return PAGE_ID;
        }

        @Override
        protected MaterialStatsId getStatType() {
            return this.stat;
        }

        @Override
        protected boolean hasPart() {
            return ArsenalPartBuilder.PARTS.values().stream().anyMatch(part -> this.stat.equals(part.getStatType()));
        }

        @Override
        protected String translationSuffix() {
            return this.stat.getPath().replace('/', '_');
        }
    }
}
