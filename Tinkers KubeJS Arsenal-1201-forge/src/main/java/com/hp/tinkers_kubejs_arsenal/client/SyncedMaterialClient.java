package com.hp.tinkers_kubejs_arsenal.client;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.AbstractPackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class SyncedMaterialClient {
    private static final Logger LOGGER = LoggerFactory.getLogger("TKJA Synced Material Client");
    private static final String PACK_ID = "tkja_server_materials";
    private static final Gson GSON = new Gson();
    private static final Path CACHE = FMLPaths.GAMEDIR.get().resolve("local/tinkers_kubejs_arsenal/synced_materials.json");
    private static volatile Map<String, String> resources = load();

    private SyncedMaterialClient() {}

    public static void addPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() == PackType.CLIENT_RESOURCES) {
            event.addRepositorySource(consumer -> consumer.accept(createPack()));
        }
    }

    public static void accept(Map<String, String> values) {
        Map<String, String> next = Map.copyOf(values);
        if (next.equals(resources)) {
            LOGGER.debug("Synced client resources unchanged ({} entries), skipping resource reload", next.size());
            return;
        }
        LOGGER.debug("Received {} changed synced client resources, reloading resource packs", next.size());
        resources = next;
        save(values);
        Minecraft.getInstance().reloadResourcePacks();
    }

    private static Map<String, String> load() {
        if (!Files.isRegularFile(CACHE)) {
            return Map.of();
        }
        try (Reader reader = Files.newBufferedReader(CACHE, StandardCharsets.UTF_8)) {
            Map<String, String> values = GSON.fromJson(reader, new TypeToken<LinkedHashMap<String, String>>() {}.getType());
            return values == null ? Map.of() : Map.copyOf(values);
        } catch (Exception e) {
            LOGGER.warn("Failed to read synced material cache {}", CACHE, e);
            return Map.of();
        }
    }

    private static void save(Map<String, String> values) {
        try {
            Files.createDirectories(CACHE.getParent());
            try (Writer writer = Files.newBufferedWriter(CACHE, StandardCharsets.UTF_8)) {
                GSON.toJson(new LinkedHashMap<>(values), writer);
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to write synced material cache {}", CACHE, e);
        }
    }

    private static Pack createPack() {
        Pack.Info info = new Pack.Info(Component.literal("TKJA server materials"), 15, FeatureFlagSet.of());
        return Pack.create(PACK_ID, Component.literal("TKJA server materials"), true, ignored -> new SyncedPackResources(PACK_ID, resources), info, PackType.CLIENT_RESOURCES, Pack.Position.TOP, false, PackSource.SERVER);
    }

    private static final class SyncedPackResources extends AbstractPackResources {
        private final Map<String, String> values;

        private SyncedPackResources(String name, Map<String, String> values) {
            super(name, false);
            this.values = values;
        }

        @Override
        public IoSupplier<InputStream> getRootResource(String... path) {
            return null;
        }

        @Override
        public IoSupplier<InputStream> getResource(PackType type, ResourceLocation id) {
            if (type != PackType.CLIENT_RESOURCES) {
                return null;
            }
            String value = this.values.get(id.toString());
            return value == null ? null : () -> new ByteArrayInputStream(value.getBytes(StandardCharsets.UTF_8));
        }

        @Override
        public void listResources(PackType type, String namespace, String prefix, PackResources.ResourceOutput output) {
            if (type != PackType.CLIENT_RESOURCES) {
                return;
            }
            this.values.forEach((key, value) -> {
                ResourceLocation id = ResourceLocation.tryParse(key);
                if (id != null && id.getNamespace().equals(namespace) && id.getPath().startsWith(prefix)) {
                    output.accept(id, () -> new ByteArrayInputStream(value.getBytes(StandardCharsets.UTF_8)));
                }
            });
        }

        @Override
        public Set<String> getNamespaces(PackType type) {
            if (type != PackType.CLIENT_RESOURCES) {
                return Set.of();
            }
            return this.values.keySet().stream().map(ResourceLocation::tryParse).filter(java.util.Objects::nonNull).map(ResourceLocation::getNamespace).collect(java.util.stream.Collectors.toUnmodifiableSet());
        }

        @Override
        public void close() {
        }
    }
}
