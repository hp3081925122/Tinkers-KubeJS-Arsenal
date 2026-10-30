package com.hp.tinkers_kubejs_arsenal.kubejs.modifier;

import com.hp.tinkers_kubejs_arsenal.TinkersKubejsArsenal;
import dev.latvian.mods.kubejs.script.ScriptType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.registries.RegisterEvent;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.data.loadable.record.SingletonLoader;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.armor.ModifyDamageModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.behavior.AttributesModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.build.ToolStatsModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeDamageModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeHitModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.mining.BlockBreakModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.ranged.ProjectileHitModifierHook;
import slimeknights.tconstruct.library.modifiers.modules.ModifierModule;
import slimeknights.tconstruct.library.module.HookProvider;
import slimeknights.tconstruct.library.module.ModuleHook;
import slimeknights.tconstruct.library.tools.context.EquipmentContext;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.context.ToolHarvestContext;
import slimeknights.tconstruct.library.tools.nbt.IToolContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;
import slimeknights.tconstruct.library.tools.nbt.ModifierNBT;
import slimeknights.tconstruct.library.tools.stat.ModifierStatsBuilder;

import java.util.List;
import java.util.function.BiConsumer;

public final class ArsenalEventsModule implements ModifierModule, MeleeDamageModifierHook, MeleeHitModifierHook, ProjectileHitModifierHook, BlockBreakModifierHook, ModifyDamageModifierHook, ToolStatsModifierHook, AttributesModifierHook {
    public static final ResourceLocation ID = new ResourceLocation(TinkersKubejsArsenal.MOD_ID, "kubejs_events");
    public static final ResourceLocation DAMAGE_ID = new ResourceLocation(TinkersKubejsArsenal.MOD_ID, "kubejs_damage_events");
    public static final ArsenalEventsModule INSTANCE = SingletonLoader.singleton(ArsenalEventsModule::new);
    public static final AfterArmor AFTER_ARMOR = SingletonLoader.singleton(AfterArmor::new);
    private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<ArsenalEventsModule>defaultHooks(ModifierHooks.MELEE_DAMAGE, ModifierHooks.MELEE_HIT, ModifierHooks.PROJECTILE_HIT, ModifierHooks.BLOCK_BREAK, ModifierHooks.MODIFY_HURT, ModifierHooks.TOOL_STATS, ModifierHooks.ATTRIBUTES);

    private final RecordLoadable<ArsenalEventsModule> loader;

    private ArsenalEventsModule(RecordLoadable<ArsenalEventsModule> loader) {
        this.loader = loader;
    }

    public static void register(RegisterEvent event) {
        if (event.getRegistryKey() == Registries.RECIPE_SERIALIZER) {
            ModifierModule.LOADER.register(ID, INSTANCE.getLoader());
            ModifierModule.LOADER.register(DAMAGE_ID, AFTER_ARMOR.getLoader());
        }
    }

    @Override
    public RecordLoadable<ArsenalEventsModule> getLoader() {
        return this.loader;
    }

    @Override
    public List<ModuleHook<?>> getDefaultHooks() {
        return DEFAULT_HOOKS;
    }

    @Override
    public float getMeleeDamage(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float baseDamage, float damage) {
        if (context.getLevel().isClientSide || !ArsenalModifierEvents.MELEE_DAMAGE.hasListeners(modifier.getId())) {
            return damage;
        }
        ModifierHookEvents.MeleeDamageEventJS event = new ModifierHookEvents.MeleeDamageEventJS(tool, modifier, context, damage);
        ArsenalModifierEvents.MELEE_DAMAGE.post(ScriptType.SERVER, modifier.getId(), event);
        return event.getDamage();
    }

    @Override
    public void afterMeleeHit(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float damageDealt) {
        if (!context.getLevel().isClientSide && ArsenalModifierEvents.AFTER_MELEE_HIT.hasListeners(modifier.getId())) {
            ArsenalModifierEvents.AFTER_MELEE_HIT.post(ScriptType.SERVER, modifier.getId(), new ModifierHookEvents.AttackEventJS(tool, modifier, context, damageDealt));
        }
    }

    @Override
    public boolean onProjectileHitEntity(ModifierNBT modifiers, ModDataNBT persistentData, ModifierEntry modifier, Projectile projectile, EntityHitResult hit, @Nullable LivingEntity attacker, @Nullable LivingEntity target, boolean notBlocked) {
        if (!projectile.level().isClientSide && ArsenalModifierEvents.PROJECTILE_HIT.hasListeners(modifier.getId())) {
            ArsenalModifierEvents.PROJECTILE_HIT.post(ScriptType.SERVER, modifier.getId(), new ModifierHookEvents.ProjectileHitEventJS(modifier, modifiers, projectile, hit.getEntity(), attacker, target, !notBlocked));
        }
        return false;
    }

    @Override
    public void afterBlockBreak(IToolStackView tool, ModifierEntry modifier, ToolHarvestContext context) {
        if (ArsenalModifierEvents.BLOCK_BREAK.hasListeners(modifier.getId())) {
            ArsenalModifierEvents.BLOCK_BREAK.post(ScriptType.SERVER, modifier.getId(), new ModifierHookEvents.BlockBreakEventJS(tool, modifier, context));
        }
    }

    @Override
    public float modifyDamageTaken(IToolStackView tool, ModifierEntry modifier, EquipmentContext context, EquipmentSlot slotType, DamageSource source, float amount, boolean isDirectDamage) {
        return post(ArsenalModifierEvents.HURT, tool, modifier, context, slotType, source, amount, isDirectDamage);
    }

    @Override
    public void addToolStats(IToolContext context, ModifierEntry modifier, ModifierStatsBuilder builder) {
        if (ArsenalModifierEvents.TOOL_STATS.hasListeners(modifier.getId())) {
            ArsenalModifierEvents.TOOL_STATS.post(ScriptType.SERVER, modifier.getId(), new ToolStatsEventJS(context, modifier, builder));
        }
    }

    @Override
    public void addAttributes(IToolStackView tool, ModifierEntry modifier, EquipmentSlot slot, BiConsumer<Attribute, AttributeModifier> consumer) {
        if (ArsenalModifierEvents.ATTRIBUTES.hasListeners(modifier.getId())) {
            ArsenalModifierEvents.ATTRIBUTES.post(ScriptType.SERVER, modifier.getId(), new ModifierHookEvents.AttributesEventJS(tool, modifier, slot, consumer));
        }
    }

    private static float post(dev.latvian.mods.kubejs.event.EventHandler handler, IToolStackView tool, ModifierEntry modifier, EquipmentContext context, EquipmentSlot slotType, DamageSource source, float amount, boolean isDirectDamage) {
        LivingEntity entity = context.getEntity();
        if (entity.level().isClientSide || !handler.hasListeners(modifier.getId())) {
            return amount;
        }
        ModifierHookEvents.DamageTakenEventJS event = new ModifierHookEvents.DamageTakenEventJS(tool, modifier, entity, slotType, source, amount, isDirectDamage);
        handler.post(ScriptType.SERVER, modifier.getId(), event);
        return event.getDamage();
    }

    public static final class AfterArmor implements ModifierModule, ModifyDamageModifierHook {
        private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<AfterArmor>defaultHooks(ModifierHooks.MODIFY_DAMAGE);
        private final RecordLoadable<AfterArmor> loader;

        private AfterArmor(RecordLoadable<AfterArmor> loader) {
            this.loader = loader;
        }

        @Override
        public RecordLoadable<AfterArmor> getLoader() {
            return this.loader;
        }

        @Override
        public List<ModuleHook<?>> getDefaultHooks() {
            return DEFAULT_HOOKS;
        }

        @Override
        public float modifyDamageTaken(IToolStackView tool, ModifierEntry modifier, EquipmentContext context, EquipmentSlot slotType, DamageSource source, float amount, boolean isDirectDamage) {
            return post(ArsenalModifierEvents.DAMAGE, tool, modifier, context, slotType, source, amount, isDirectDamage);
        }
    }
}
