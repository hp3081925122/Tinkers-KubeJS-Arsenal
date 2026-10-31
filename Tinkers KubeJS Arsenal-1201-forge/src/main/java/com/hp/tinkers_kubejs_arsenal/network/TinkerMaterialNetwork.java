package com.hp.tinkers_kubejs_arsenal.network;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hp.tinkers_kubejs_arsenal.TinkersKubejsArsenal;
import com.hp.tinkers_kubejs_arsenal.kubejs.material.MaterialDataEventJS;
import com.hp.tinkers_kubejs_arsenal.kubejs.modifier.ModifierDataEventJS;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

public final class TinkerMaterialNetwork {
    private static final Logger LOGGER = LoggerFactory.getLogger("TKJA Material Network");
    private static final String VERSION = "2";
    private static final int MAX_PAYLOAD = 1_000_000;
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
        new ResourceLocation(TinkersKubejsArsenal.MOD_ID, "materials"),
        () -> VERSION,
        VERSION::equals,
        VERSION::equals
    );
    private static int packetId;
    private static volatile MaterialAssetsPacket latest = MaterialAssetsPacket.of(Map.of());

    static {
        CHANNEL.registerMessage(packetId++, MaterialAssetsPacket.class, MaterialAssetsPacket::encode, MaterialAssetsPacket::decode, MaterialAssetsPacket::handle);
    }

    private TinkerMaterialNetwork() {}

    public static void init() {
    }

    public static void update(Map<ResourceLocation, MaterialDataEventJS.MaterialDefinition> materials, Map<ResourceLocation, ModifierDataEventJS.ModifierDefinition> modifiers) {
        latest = MaterialAssetsPacket.from(materials, modifiers);
        LOGGER.debug("Prepared {} bytes of synced client resources", latest.data.length);
    }

    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        if (latest.data.length > MAX_PAYLOAD) {
            LOGGER.error("Synced material resources are {} bytes after compression, over the {} byte limit; client appearance was not sent", latest.data.length, MAX_PAYLOAD);
            return;
        }
        for (ServerPlayer player : event.getPlayers()) {
            CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), latest);
        }
    }

    public static final class MaterialAssetsPacket {
        private final byte[] data;

        private MaterialAssetsPacket(byte[] data) {
            this.data = data;
        }

        private static MaterialAssetsPacket of(Map<String, String> resources) {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            try (DataOutputStream out = new DataOutputStream(new DeflaterOutputStream(bytes))) {
                out.writeInt(resources.size());
                for (Map.Entry<String, String> entry : resources.entrySet()) {
                    writeString(out, entry.getKey());
                    writeString(out, entry.getValue());
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            return new MaterialAssetsPacket(bytes.toByteArray());
        }

        private static void writeString(DataOutputStream out, String value) throws IOException {
            byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
            out.writeInt(bytes.length);
            out.write(bytes);
        }

        private Map<String, String> resources() {
            Map<String, String> resources = new LinkedHashMap<>();
            try (DataInputStream in = new DataInputStream(new InflaterInputStream(new ByteArrayInputStream(this.data)))) {
                int size = in.readInt();
                for (int i = 0; i < size; i++) {
                    resources.put(readString(in), readString(in));
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            return resources;
        }

        private static String readString(DataInputStream in) throws IOException {
            return new String(in.readNBytes(in.readInt()), StandardCharsets.UTF_8);
        }

        private static MaterialAssetsPacket from(Map<ResourceLocation, MaterialDataEventJS.MaterialDefinition> materials, Map<ResourceLocation, ModifierDataEventJS.ModifierDefinition> modifiers) {
            Map<String, String> resources = new LinkedHashMap<>();
            Map<String, JsonObject> colors = new LinkedHashMap<>();
            Map<String, Map<String, JsonObject>> languages = new LinkedHashMap<>();
            Map<String, JsonObject> icons = new LinkedHashMap<>();
            for (MaterialDataEventJS.MaterialDefinition material : materials.values()) {
                if (material.isInvalid() || !material.hasAssets()) {
                    continue;
                }
                ResourceLocation id = material.id();
                if (!material.render().entrySet().isEmpty()) {
                    resources.put(new ResourceLocation(id.getNamespace(), "tinkering/materials/" + id.getPath() + ".json").toString(), material.render().toString());
                }
                if (material.textColor() != null) {
                    colors.computeIfAbsent(id.getNamespace(), key -> new JsonObject()).addProperty(material.translationKey(), material.textColor());
                }
                addLanguage(languages, id, material.translationKey(), material.names());
                addLanguage(languages, id, material.translationKey() + ".flavor", material.flavors());
                addLanguage(languages, id, material.translationKey() + ".encyclopedia", material.encyclopedia());
            }
            for (ModifierDataEventJS.ModifierDefinition modifier : modifiers.values()) {
                if (modifier.isInvalid()) {
                    continue;
                }
                ResourceLocation id = modifier.id();
                if (modifier.textColor() != null) {
                    colors.computeIfAbsent(id.getNamespace(), key -> new JsonObject()).addProperty(modifier.translationKey(), modifier.textColor());
                }
                addLanguage(languages, id, modifier.translationKey(), modifier.names());
                addLanguage(languages, id, modifier.translationKey() + ".flavor", modifier.flavors());
                addLanguage(languages, id, modifier.translationKey() + ".description", modifier.descriptions());
                if (!modifier.icons().isEmpty()) {
                    JsonArray sprites = new JsonArray();
                    modifier.icons().forEach(sprites::add);
                    icons.computeIfAbsent(id.getNamespace(), key -> new JsonObject()).add(id.toString(), sprites);
                }
            }
            icons.forEach((namespace, json) -> resources.put(new ResourceLocation(namespace, "tinkering/modifier_icons.json").toString(), json.toString()));
            colors.forEach((namespace, json) -> resources.put(new ResourceLocation(namespace, "mantle/colors.json").toString(), json.toString()));
            languages.forEach((namespace, values) -> values.forEach((language, json) -> resources.put(new ResourceLocation(namespace, "lang/" + language + ".json").toString(), json.toString())));
            return of(resources);
        }

        private static void addLanguage(Map<String, Map<String, JsonObject>> languages, ResourceLocation id, String key, Map<String, String> values) {
            values.forEach((language, text) -> languages.computeIfAbsent(id.getNamespace(), ignored -> new LinkedHashMap<>()).computeIfAbsent(language, ignored -> new JsonObject()).addProperty(key, text));
        }

        private static MaterialAssetsPacket decode(FriendlyByteBuf buffer) {
            return new MaterialAssetsPacket(buffer.readByteArray(MAX_PAYLOAD));
        }

        private void encode(FriendlyByteBuf buffer) {
            buffer.writeByteArray(this.data);
        }

        private static void handle(MaterialAssetsPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
            NetworkEvent.Context context = contextSupplier.get();
            context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> com.hp.tinkers_kubejs_arsenal.client.SyncedMaterialClient.accept(packet.resources())));
            context.setPacketHandled(true);
        }
    }
}
