package com.hp.tinkers_kubejs_arsenal.kubejs.modifier;

import dev.latvian.mods.kubejs.event.EventJS;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.context.ToolHarvestContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ModifierNBT;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.UUID;
import java.util.function.BiConsumer;

public final class ModifierHookEvents {
    private ModifierHookEvents() {}

    public abstract static class ModifierEventJS extends EventJS {
        private final ModifierEntry modifier;

        protected ModifierEventJS(ModifierEntry modifier) {
            this.modifier = modifier;
        }

        public ModifierId getModifier() {
            return this.modifier.getId();
        }

        public int getLevel() {
            return this.modifier.getLevel();
        }

        public float getEffectiveLevel() {
            return this.modifier.getEffectiveLevel();
        }
    }

    public abstract static class ToolModifierEventJS extends ModifierEventJS {
        private final IToolStackView tool;

        protected ToolModifierEventJS(IToolStackView tool, ModifierEntry modifier) {
            super(modifier);
            this.tool = tool;
        }

        public IToolStackView getTool() {
            return this.tool;
        }
    }

    public static class AttackEventJS extends ToolModifierEventJS {
        private final ToolAttackContext context;
        protected float damage;

        public AttackEventJS(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float damage) {
            super(tool, modifier);
            this.context = context;
            this.damage = damage;
        }

        public LivingEntity getAttacker() {
            return this.context.getAttacker();
        }

        @Nullable
        public Player getPlayer() {
            return this.context.getPlayerAttacker();
        }

        public Entity getTarget() {
            return this.context.getTarget();
        }

        @Nullable
        public LivingEntity getLivingTarget() {
            return this.context.getLivingTarget();
        }

        public InteractionHand getHand() {
            return this.context.getHand();
        }

        public EquipmentSlot getSlot() {
            return this.context.getSlotType();
        }

        @Nullable
        public Projectile getProjectile() {
            return this.context.getProjectile();
        }

        public boolean isCritical() {
            return this.context.isCritical();
        }

        public boolean isFullyCharged() {
            return this.context.isFullyCharged();
        }

        public boolean isExtraAttack() {
            return this.context.isExtraAttack();
        }

        public float getBaseDamage() {
            return this.context.getBaseDamage();
        }

        public float getDamage() {
            return this.damage;
        }
    }

    public static class MeleeDamageEventJS extends AttackEventJS {
        public MeleeDamageEventJS(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float damage) {
            super(tool, modifier, context, damage);
        }

        public void setDamage(float damage) {
            this.damage = damage;
        }
    }

    public static class ProjectileHitEventJS extends ModifierEventJS {
        private final ModifierNBT modifiers;
        private final Projectile projectile;
        private final Entity hitEntity;
        private final LivingEntity attacker;
        private final LivingEntity target;
        private final boolean blocked;

        public ProjectileHitEventJS(ModifierEntry modifier, ModifierNBT modifiers, Projectile projectile, Entity hitEntity, @Nullable LivingEntity attacker, @Nullable LivingEntity target, boolean blocked) {
            super(modifier);
            this.modifiers = modifiers;
            this.projectile = projectile;
            this.hitEntity = hitEntity;
            this.attacker = attacker;
            this.target = target;
            this.blocked = blocked;
        }

        public ModifierNBT getModifiers() {
            return this.modifiers;
        }

        public Projectile getProjectile() {
            return this.projectile;
        }

        public Entity getHitEntity() {
            return this.hitEntity;
        }

        @Nullable
        public LivingEntity getAttacker() {
            return this.attacker;
        }

        @Nullable
        public LivingEntity getTarget() {
            return this.target;
        }

        public boolean isBlocked() {
            return this.blocked;
        }
    }

    public static class BlockBreakEventJS extends ToolModifierEventJS {
        private final ToolHarvestContext context;

        public BlockBreakEventJS(IToolStackView tool, ModifierEntry modifier, ToolHarvestContext context) {
            super(tool, modifier);
            this.context = context;
        }

        public ServerLevel getWorld() {
            return this.context.getWorld();
        }

        public LivingEntity getEntity() {
            return this.context.getLiving();
        }

        @Nullable
        public Player getPlayer() {
            return this.context.getPlayer();
        }

        public BlockPos getPos() {
            return this.context.getPos();
        }

        public BlockState getState() {
            return this.context.getState();
        }

        public boolean isAOE() {
            return this.context.isAOE();
        }

        public boolean canHarvest() {
            return this.context.canHarvest();
        }

        public boolean isEffective() {
            return this.context.isEffective();
        }
    }

    public static class DamageTakenEventJS extends ToolModifierEventJS {
        private final LivingEntity entity;
        private final EquipmentSlot slot;
        private final DamageSource source;
        private final boolean direct;
        private float damage;

        public DamageTakenEventJS(IToolStackView tool, ModifierEntry modifier, LivingEntity entity, EquipmentSlot slot, DamageSource source, float damage, boolean direct) {
            super(tool, modifier);
            this.entity = entity;
            this.slot = slot;
            this.source = source;
            this.damage = damage;
            this.direct = direct;
        }

        public LivingEntity getEntity() {
            return this.entity;
        }

        public EquipmentSlot getSlot() {
            return this.slot;
        }

        public DamageSource getSource() {
            return this.source;
        }

        public boolean isDirectDamage() {
            return this.direct;
        }

        public float getDamage() {
            return this.damage;
        }

        public void setDamage(float damage) {
            this.damage = damage;
        }
    }

    public static class AttributesEventJS extends ToolModifierEventJS {
        private final EquipmentSlot slot;
        private final BiConsumer<Attribute, AttributeModifier> consumer;
        private int count;

        public AttributesEventJS(IToolStackView tool, ModifierEntry modifier, EquipmentSlot slot, BiConsumer<Attribute, AttributeModifier> consumer) {
            super(tool, modifier);
            this.slot = slot;
            this.consumer = consumer;
        }

        public EquipmentSlot getSlot() {
            return this.slot;
        }

        public void add(String attribute, double amount, String attributeOperation) {
            ResourceLocation id = new ResourceLocation(attribute);
            Attribute value = ForgeRegistries.ATTRIBUTES.getValue(id);
            if (value == null) {
                throw new IllegalArgumentException("Unknown attribute " + attribute);
            }
            AttributeModifier.Operation op = switch (attributeOperation.toLowerCase(Locale.ROOT)) {
                case "addition" -> AttributeModifier.Operation.ADDITION;
                case "multiply_base" -> AttributeModifier.Operation.MULTIPLY_BASE;
                case "multiply_total" -> AttributeModifier.Operation.MULTIPLY_TOTAL;
                default -> throw new IllegalArgumentException("Invalid attribute operation '" + attributeOperation + "', expected addition, multiply_base or multiply_total");
            };
            String name = this.getModifier() + "." + id + "." + this.slot.getName() + "." + this.count++;
            this.consumer.accept(value, new AttributeModifier(UUID.nameUUIDFromBytes(name.getBytes(StandardCharsets.UTF_8)), name, amount, op));
        }
    }
}
