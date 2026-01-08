package nico.farmingfellas.common.entity.base;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import nico.farmingfellas.common.entity.FellaVariant;
import nico.farmingfellas.common.entity.base.goal.FellaTemptGoal;

import java.util.function.Consumer;

public abstract class FellaGolemEntity extends PathAwareEntity implements Inventory, NamedScreenHandlerFactory {
    private static final TrackedData<Boolean> ARMS_IN_AIR = DataTracker.registerData(FellaGolemEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

    private final SimpleInventory inventory;

    public float jumpingMultiplier = 1;

    protected FellaGolemEntity(EntityType<? extends PathAwareEntity> entityType, World world, int inventorySize) {
        super(entityType, world);

        this.inventory = new SimpleInventory(inventorySize);
    }

    public static DefaultAttributeContainer.Builder createGolemAttributes() {
        return createLivingAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 8)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.25)
                .add(EntityAttributes.GENERIC_ARMOR, 0)
                .add(EntityAttributes.GENERIC_ARMOR_TOUGHNESS, 0)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 16.0)
                .add(EntityAttributes.GENERIC_ATTACK_KNOCKBACK, 0)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 0);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();

        this.dataTracker.startTracking(ARMS_IN_AIR, false);
    }

    @Override
    protected void initGoals() {
        super.initGoals();

        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(1, new FellaTemptGoal(this, 2));
    }

    @Override
    public void tick() {
        super.tick();

        if (getWorld() instanceof ServerWorld && getStackInHand(Hand.MAIN_HAND).isOf(Items.COOKIE)) {
            this.setArmsInAir(false);

            if (doActionAndWait(80, this, (cooldown) -> {
                if (random.nextBetween(1, 100) < 20) {
                    this.playSound(SoundEvents.ENTITY_GENERIC_EAT, 1f, 1 + random.nextBetween(1, 100) / 10f);
                }

                ItemStackParticleEffect stackParticleEffect = new ItemStackParticleEffect(ParticleTypes.ITEM, this.getStackInHand(Hand.MAIN_HAND));
                for (int i = 0; i < 2; ++i) {
                    double x = this.getEyePos().getX() + (double) MathHelper.nextBetween(random, -0.2F, 0.2F);
                    double y = this.getEyePos().getY();
                    double z = this.getEyePos().getZ() + (double) MathHelper.nextBetween(random, -0.2F, 0.2F);

                    ((ServerWorld) this.getWorld()).spawnParticles(
                            stackParticleEffect,
                            x, y, z, 1,
                            MathHelper.nextBetween(random, -0.2F, 0.2F),
                            MathHelper.nextBetween(random, -0.2F, 0.2F),
                            MathHelper.nextBetween(random, -0.2F, 0.2F),
                            0.05
                    );
                }
            })) return;

            setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
        }
    }

    @Override
    protected ActionResult interactMob(PlayerEntity player, Hand hand) {
        if (player.getStackInHand(hand).isOf(Items.COOKIE)) {
            this.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.COOKIE));
            player.getStackInHand(hand).decrement(1);
            return ActionResult.SUCCESS;
        }

        if (player.isSneaking()) {
            this.open(player);
            return ActionResult.SUCCESS;
        }

        return super.interactMob(player, hand);
    }

    //region // * Wait / Actions * //
    private int cooldown;

    public static boolean doActionAndWait(int maxCooldown, FellaGolemEntity golem, Consumer<Integer> run) {
        if (golem.cooldown < maxCooldown) {
            run.accept(golem.cooldown);

            golem.cooldown++;

            return true;
        }

        golem.cooldown = 0;
        return false;
    }

    public static boolean spawnBlockParticlesAndWait(int maxCooldown, FellaGolemEntity golem, BlockPos particlePos, Consumer<Integer> run) {
        return spawnBlockParticlesAndWait(maxCooldown, golem, particlePos, particlePos, run);
    }

    public static boolean spawnBlockParticlesAndWait(int maxCooldown, FellaGolemEntity golem, BlockPos particlePos, BlockPos spawnParticlesPos, Consumer<Integer> run) {
        return doActionAndWait(maxCooldown, golem, (cooldown) -> {
            Random random = golem.getWorld().getRandom();
            BlockState blockState = golem.getWorld().getBlockState(particlePos);

            for (int i = 0; i < 10; ++i) {
                double x = spawnParticlesPos.getX() + (double) MathHelper.nextBetween(random, -0.7F, 0.7F) + 0.5;
                double y = spawnParticlesPos.getY();
                double z = spawnParticlesPos.getZ() + (double) MathHelper.nextBetween(random, -0.7F, 0.7F) + 0.5;

                ((ServerWorld) golem.getWorld()).spawnParticles(
                        new BlockStateParticleEffect(ParticleTypes.BLOCK, blockState),
                        x, y, z, 1, 0, 0, 0, 1
                );
            }

            run.accept(cooldown);
        });
    }
    //endregion

    //region // * Utility * //
    public void lookAt(Vec3d position) {
        this.getLookControl().lookAt(position);
    }

    public boolean isPositionAccessible(BlockPos pos) {
        if (pos == null) return false;
        return true;
    }
    //endregion

    //region // * Tracked Data * //

    // Arms
    public void setArmsInAir(boolean value) {
        this.dataTracker.set(ARMS_IN_AIR, value);
    }

    public boolean getArmsInAir() {
        return this.dataTracker.get(ARMS_IN_AIR);
    }

    // Zone

    //endregion

    //region // * Saving / Loading * //
    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);

        nbt.put("inventory", this.inventory.toNbtList());
        nbt.putBoolean("arms_in_air", getArmsInAir());
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);

        this.inventory.readNbtList(nbt.getList("inventory", NbtElement.COMPOUND_TYPE));
        this.setArmsInAir(nbt.getBoolean("arms_in_air"));
    }
    //endregion

    //region // * Fields * //
    public boolean hasCookie() {
        return getStackInHand(Hand.MAIN_HAND).isOf(Items.COOKIE);
    }

    @Override
    public Arm getMainArm() {
        return Arm.RIGHT;
    }

    @Override
    public boolean cannotDespawn() {
        return true;
    }

    @Override
    public boolean isPersistent() {
        return true;
    }

    @Override
    protected float getJumpVelocity() {
        return super.getJumpVelocity() * jumpingMultiplier;
    }

    @Override
    public float getPathfindingPenalty(PathNodeType nodeType) {
        return switch (nodeType) {
            case BLOCKED, DAMAGE_FIRE, DAMAGE_CAUTIOUS -> -8;
            case WALKABLE, OPEN, DAMAGE_OTHER -> 8;
            default -> super.getPathfindingPenalty(nodeType);
        };
    }

    //endregion

    //region // * Inventory * //
    public ActionResult open(PlayerEntity player) {
        player.openHandledScreen(this);
        return !player.getWorld().isClient ? ActionResult.CONSUME : ActionResult.SUCCESS;
    }

    @Override
    public int size() {
        return this.inventory.size();
    }

    @Override
    public boolean isEmpty() {
        return inventory.isEmpty();
    }

    public boolean isFull() {
        for (ItemStack stack : inventory.stacks) {
            if (stack.isEmpty()) return false;
        }

        return true;
    }

    public boolean hasFreeSlot() {
        boolean freeSlot = false;

        for (ItemStack stack : inventory.stacks) {
            if (stack.isEmpty()) {
                freeSlot = true;
                break;
            }
        }

        return freeSlot;
    }

    @Override
    public ItemStack getStack(int slot) {
        return this.inventory.getStack(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        return this.inventory.removeStack(slot, amount);
    }

    @Override
    public ItemStack removeStack(int slot) {
        return this.inventory.removeStack(slot);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        this.inventory.setStack(slot, stack);
    }

    @Override
    public void markDirty() {
        this.inventory.markDirty();
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return this.inventory.canPlayerUse(player);
    }

    @Override
    public void clear() {
        this.inventory.clear();
    }

    public ItemStack insertStack(ItemStack stack) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack remainder = stack.copy();

        // 1. Try to merge into existing stacks
        for (int i = 0; i < this.inventory.size(); i++) {
            ItemStack slotStack = this.getStack(i);

            if (!slotStack.isEmpty()
                    && ItemStack.canCombine(slotStack, remainder)) {

                int max = Math.min(slotStack.getMaxCount(), this.getMaxCountPerStack());
                int space = max - slotStack.getCount();

                if (space > 0) {
                    int toMove = Math.min(space, remainder.getCount());
                    slotStack.increment(toMove);
                    remainder.decrement(toMove);

                    if (remainder.isEmpty()) {
                        return ItemStack.EMPTY;
                    }
                }
            }
        }

        // 2. Put into empty slots
        for (int i = 0; i < this.inventory.size(); i++) {
            ItemStack slotStack = this.getStack(i);

            if (slotStack.isEmpty()) {
                int toMove = Math.min(remainder.getCount(), remainder.getMaxCount());
                ItemStack newStack = remainder.copy();
                newStack.setCount(toMove);

                this.setStack(i, newStack);
                remainder.decrement(toMove);

                if (remainder.isEmpty()) {
                    return ItemStack.EMPTY;
                }
            }
        }

        // 3. Return leftovers
        return remainder;
    }
    //endregion

    //region // * Dropping * //
    @Override
    protected void dropLoot(DamageSource damageSource, boolean causedByPlayer) {
        this.spawnItemStack(this.getPickBlockStack());
        this.inventory.stacks.forEach(this::spawnItemStack);
    }

    private void spawnItemStack(ItemStack stack) {
        if (this.getWorld().isClient()) return;
        if (stack == null || stack.isEmpty() || stack.getItem().equals(Items.AIR)) return;

        ItemEntity itemEntity = new ItemEntity(EntityType.ITEM, this.getWorld());
        itemEntity.setStack(stack);
        itemEntity.setPosition(this.getPos());
        itemEntity.setVelocity(
                MathHelper.nextBetween(random, -0.5f, 0.5f) / 5f,
                MathHelper.nextBetween(random, 0.25f, 0.5f) / 2f,
                MathHelper.nextBetween(random, -0.5f, 0.5f) / 5f
        );
        this.getWorld().spawnEntity(itemEntity);
    }

    @Override
    protected void dropEquipment(DamageSource source, int lootingMultiplier, boolean allowDrops) {

    }
    //endregion

    //region // * Rendering * //
    @Environment(EnvType.CLIENT)
    private boolean inGui = false;

    @Environment(EnvType.CLIENT)
    public void setInGui(boolean inGui) {
        this.inGui = inGui;
    }

    @Environment(EnvType.CLIENT)
    public boolean isInGui() {
        return inGui;
    }
    //endregion

    @Override
    public abstract ItemStack getPickBlockStack();

    public abstract FellaVariant getVariant();
}
