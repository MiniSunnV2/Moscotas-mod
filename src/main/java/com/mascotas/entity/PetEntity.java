package com.mascotas.entity;

import com.mascotas.pet.PetBenefits;
import com.mascotas.pet.PetType;
import com.mascotas.pet.TamingRule;
import com.mascotas.registry.ModSounds;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.SitGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

/** Entidad única que representa a las 30 mascotas; el comportamiento cambia según su {@link PetType}. */
public class PetEntity extends TameableEntity implements GeoEntity {
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation SIT = RawAnimation.begin().thenLoop("sit");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final PetType petType;

    private int feedCount;
    private final List<String> fedItems = new ArrayList<>();
    private long lastRevive = -24000L;

    public PetEntity(EntityType<? extends PetEntity> type, World world, PetType petType) {
        super(type, world);
        this.petType = petType;
    }

    public static DefaultAttributeContainer.Builder createPetAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 14.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.25)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 20.0);
    }

    public static boolean canSpawnPet(EntityType<PetEntity> type, ServerWorldAccess world, SpawnReason reason,
                                      BlockPos pos, Random random) {
        BlockPos below = pos.down();
        return world.getBlockState(below).isSolidBlock(world, below);
    }

    public PetType getPetType() { return petType; }
    public long getLastRevive() { return lastRevive; }
    public void setLastRevive(long time) { this.lastRevive = time; }

    // ---------------------------------------------------------------- IA

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new SwimGoal(this));
        this.goalSelector.add(2, new SitGoal(this));
        this.goalSelector.add(4, new FollowOwnerGoal(this, 1.1, 10.0f, 2.0f, false));
        this.goalSelector.add(5, new WanderAroundFarGoal(this, 0.8));
        this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 6.0f));
        this.goalSelector.add(7, new LookAroundGoal(this));
    }

    @Override
    public void tick() {
        super.tick();
        if (!getWorld().isClient && isTamed() && getOwner() instanceof ServerPlayerEntity owner
                && squaredDistanceTo(owner) < 32.0 * 32.0) {
            PetBenefits.tick(this, owner);
        }
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return false;
    }

    @Nullable
    @Override
    public PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        return null;
    }

    // ---------------------------------------------------------------- Domesticación

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        World world = getWorld();

        if (isTamed()) {
            if (isOwner(player) && hand == Hand.MAIN_HAND) {
                if (!world.isClient) {
                    setSitting(!isSitting());
                    setInSittingPose(isSitting());
                    this.navigation.stop();
                    setTarget(null);
                }
                return ActionResult.success(world.isClient);
            }
            return super.interactMob(player, hand);
        }

        TamingRule rule = petType.rule();
        if (!rule.item().test(stack)) {
            if (!world.isClient && stack.isEmpty() && hand == Hand.MAIN_HAND) {
                player.sendMessage(Text.translatable("pet.mascotas.hint." + petType.id()), true);
            }
            return super.interactMob(player, hand);
        }
        if (world.isClient) return ActionResult.CONSUME;

        ServerWorld server = (ServerWorld) world;
        if (!rule.condition().test(this, player)) {
            player.sendMessage(Text.translatable("pet.mascotas.hint." + petType.id()), true);
            server.spawnParticles(ParticleTypes.SMOKE, getX(), getBodyY(0.8), getZ(), 6, 0.25, 0.25, 0.25, 0.01);
            return ActionResult.SUCCESS;
        }

        String itemId = Registries.ITEM.getId(stack.getItem()).toString();
        if (rule.distinct() && fedItems.contains(itemId)) {
            player.sendMessage(Text.translatable("pet.mascotas.already_tried"), true);
            return ActionResult.SUCCESS;
        }

        if (!player.getAbilities().creativeMode) stack.decrement(1);
        feedCount++;
        if (rule.distinct()) fedItems.add(itemId);
        server.spawnParticles(ParticleTypes.HEART, getX(), getBodyY(0.8), getZ(), 4, 0.3, 0.3, 0.3, 0.02);

        if (feedCount >= rule.feeds()) {
            setOwner(player);
            setTamed(true);
            this.navigation.stop();
            setTarget(null);
            setSitting(false);
            setInSittingPose(false);
            setHealth(getMaxHealth());
            server.spawnParticles(ParticleTypes.HAPPY_VILLAGER, getX(), getBodyY(0.8), getZ(), 12, 0.4, 0.4, 0.4, 0.05);
            player.sendMessage(Text.translatable("pet.mascotas.tamed", getDisplayName(),
                    Text.translatable("pet.mascotas.benefit." + petType.id())), false);
        } else {
            player.sendMessage(Text.translatable("pet.mascotas.progress", getDisplayName(), feedCount, rule.feeds()), true);
        }
        return ActionResult.SUCCESS;
    }

    // ---------------------------------------------------------------- Sonidos

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.get(petType, "ambient");
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.get(petType, "hurt");
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.get(petType, "death");
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        // Sin sonido de pasos de vainilla: las mascotas son pequeñas y ligeras.
    }

    // ---------------------------------------------------------------- NBT

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putInt("FeedCount", feedCount);
        nbt.putLong("LastRevive", lastRevive);
        NbtList list = new NbtList();
        for (String s : fedItems) list.add(NbtString.of(s));
        nbt.put("FedItems", list);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        feedCount = nbt.getInt("FeedCount");
        if (nbt.contains("LastRevive")) lastRevive = nbt.getLong("LastRevive");
        fedItems.clear();
        NbtList list = nbt.getList("FedItems", NbtElement.STRING_TYPE);
        for (int i = 0; i < list.size(); i++) fedItems.add(list.getString(i));
    }

    // ---------------------------------------------------------------- GeckoLib

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 4, this::animationPredicate));
    }

    private PlayState animationPredicate(AnimationState<PetEntity> state) {
        if (isInSittingPose()) return state.setAndContinue(SIT);
        if (state.isMoving()) return state.setAndContinue(WALK);
        return state.setAndContinue(IDLE);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
