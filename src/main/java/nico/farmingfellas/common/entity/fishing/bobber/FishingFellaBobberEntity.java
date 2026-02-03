package nico.farmingfellas.common.entity.fishing.bobber;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.*;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import nico.farmingfellas.common.entity.ModEntities;
import nico.farmingfellas.common.entity.fishing.FishingFellaEntity;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class FishingFellaBobberEntity extends ProjectileEntity {
    private final Random velocityRandom;
    private boolean caughtFish;
    private int outOfOpenWaterTicks;
    private static final int field_30665 = 10;
    private static final TrackedData<Integer> HOOK_ENTITY_ID = DataTracker.registerData(FishingFellaBobberEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Boolean> CAUGHT_FISH = DataTracker.registerData(FishingFellaBobberEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private int removalTimer;
    private int hookCountdown;
    private int waitCountdown;
    private int fishTravelCountdown;
    private float fishAngle;
    private boolean inOpenWater;
    @Nullable
    private Entity hookedEntity;
    private FishingFellaBobberEntity.State state;
    private final int luckOfTheSeaLevel;
    private final int lureLevel;

    private FishingFellaBobberEntity(EntityType<? extends FishingFellaBobberEntity> type, World world, int luckOfTheSeaLevel, int lureLevel) {
        super(type, world);
        this.velocityRandom = Random.create();
        this.inOpenWater = true;
        this.state = FishingFellaBobberEntity.State.FLYING;
        this.ignoreCameraFrustum = true;
        this.luckOfTheSeaLevel = Math.max(0, luckOfTheSeaLevel);
        this.lureLevel = Math.max(0, lureLevel);
    }

    public FishingFellaBobberEntity(EntityType<? extends FishingFellaBobberEntity> entityType, World world) {
        this(entityType, world, 0, 0);
    }

    public static FishingFellaBobberEntity create(Entity thrower, World world, int luckLevel, int lureLevel) {
        FishingFellaBobberEntity bobber =
                new FishingFellaBobberEntity(ModEntities.FISHING_BOBBER, world, luckLevel, lureLevel);

        bobber.setOwner(thrower);

        // Angles (degrees → radians)
        float pitch = thrower.getPitch();
        float yaw = thrower.getYaw();
        float yawRad = -yaw * MathHelper.RADIANS_PER_DEGREE - MathHelper.PI;
        float pitchRad = -pitch * MathHelper.RADIANS_PER_DEGREE;

        // Direction components
        float dirX = MathHelper.sin(yawRad);
        float dirZ = MathHelper.cos(yawRad);
        float dirYHorizontal = MathHelper.cos(pitchRad);
        float dirYVertical = MathHelper.sin(pitchRad);

        // Initial position
        double x = thrower.getX() - dirX * 0.15;
        double y = thrower.getEyeY();
        double z = thrower.getZ() - dirZ * 0.15;

        bobber.refreshPositionAndAngles(x, y, z, yaw, pitch);

        // Initial velocity
        Vec3d velocity = new Vec3d(
                -dirX,
                MathHelper.clamp(-(dirYVertical / dirYHorizontal), -3.0F, 3.0F),
                -dirZ
        );

        double length = velocity.length();
        double speed = 0.8 / length;

        velocity = velocity.multiply(
                speed + bobber.random.nextTriangular(0.25, 0.00103365),
                speed + bobber.random.nextTriangular(0.25, 0.00103365),
                speed + bobber.random.nextTriangular(0.25, 0.00103365)
        );

        bobber.setVelocity(velocity);

        // Rotation from velocity
        bobber.setYaw((float) MathHelper.atan2(velocity.x, velocity.z) * MathHelper.DEGREES_PER_RADIAN);
        bobber.setPitch((float) MathHelper.atan2(velocity.y, velocity.horizontalLength()) * MathHelper.DEGREES_PER_RADIAN);

        bobber.prevYaw = bobber.getYaw();
        bobber.prevPitch = bobber.getPitch();

        return bobber;
    }


    protected void initDataTracker() {
        this.getDataTracker().startTracking(HOOK_ENTITY_ID, 0);
        this.getDataTracker().startTracking(CAUGHT_FISH, false);
    }

    public void onTrackedDataSet(TrackedData<?> data) {
        if (HOOK_ENTITY_ID.equals(data)) {
            int i = (Integer) this.getDataTracker().get(HOOK_ENTITY_ID);
            this.hookedEntity = i > 0 ? this.getWorld().getEntityById(i - 1) : null;
        }

        if (CAUGHT_FISH.equals(data)) {
            this.caughtFish = (Boolean) this.getDataTracker().get(CAUGHT_FISH);
            if (this.caughtFish) {
                this.setVelocity(this.getVelocity().x, (double) (-0.4F * MathHelper.nextFloat(this.velocityRandom, 0.6F, 1.0F)), this.getVelocity().z);
            }
        }

        super.onTrackedDataSet(data);
    }

    public boolean shouldRender(double distance) {
        return distance < 4096.0;
    }

    public void updateTrackedPositionAndAngles(double x, double y, double z, float yaw, float pitch, int interpolationSteps, boolean interpolate) {
    }

    public void tick() {
        this.velocityRandom.setSeed(this.getUuid().getLeastSignificantBits() ^ this.getWorld().getTime());
        super.tick();
        FishingFellaEntity fishingGolem = this.getGolemOwner();
        if (fishingGolem == null) {
            this.discard();
        } else if (this.getWorld().isClient || !this.removeIfInvalid(fishingGolem)) {
            if (this.isOnGround()) {
                ++this.removalTimer;
                if (this.removalTimer >= 1200) {
                    this.discard();
                    return;
                }
            } else {
                this.removalTimer = 0;
            }

            float f = 0.0F;
            BlockPos blockPos = this.getBlockPos();
            FluidState fluidState = this.getWorld().getFluidState(blockPos);
            if (fluidState.isIn(FluidTags.WATER)) {
                f = fluidState.getHeight(this.getWorld(), blockPos);
            }

            boolean bl = f > 0.0F;
            if (this.state == FishingFellaBobberEntity.State.FLYING) {
                if (this.hookedEntity != null) {
                    this.setVelocity(Vec3d.ZERO);
                    this.state = FishingFellaBobberEntity.State.HOOKED_IN_ENTITY;
                    return;
                }

                if (bl) {
                    this.setVelocity(this.getVelocity().multiply(0.3, 0.2, 0.3));
                    this.state = FishingFellaBobberEntity.State.BOBBING;
                    return;
                }

                this.checkForCollision();
            } else {
                if (this.state == FishingFellaBobberEntity.State.HOOKED_IN_ENTITY) {
                    if (this.hookedEntity != null) {
                        if (!this.hookedEntity.isRemoved() && this.hookedEntity.getWorld().getRegistryKey() == this.getWorld().getRegistryKey()) {
                            this.setPosition(this.hookedEntity.getX(), this.hookedEntity.getBodyY(0.8), this.hookedEntity.getZ());
                        } else {
                            this.updateHookedEntityId((Entity) null);
                            this.state = FishingFellaBobberEntity.State.FLYING;
                        }
                    }

                    return;
                }

                if (this.state == FishingFellaBobberEntity.State.BOBBING) {
                    Vec3d vec3d = this.getVelocity();
                    double d = this.getY() + vec3d.y - (double) blockPos.getY() - (double) f;
                    if (Math.abs(d) < 0.01) {
                        d += Math.signum(d) * 0.1;
                    }

                    this.setVelocity(vec3d.x * 0.9, vec3d.y - d * (double) this.random.nextFloat() * 0.2, vec3d.z * 0.9);
                    if (this.hookCountdown <= 0 && this.fishTravelCountdown <= 0) {
                        this.inOpenWater = true;
                    } else {
                        this.inOpenWater = this.inOpenWater && this.outOfOpenWaterTicks < 10 && this.isOpenOrWaterAround(blockPos);
                    }

                    if (bl) {
                        this.outOfOpenWaterTicks = Math.max(0, this.outOfOpenWaterTicks - 1);
                        if (this.caughtFish) {
                            this.setVelocity(this.getVelocity().add(0.0, -0.1 * (double) this.velocityRandom.nextFloat() * (double) this.velocityRandom.nextFloat(), 0.0));
                        }

                        if (!this.getWorld().isClient) {
                            this.tickFishingLogic(blockPos);
                        }
                    } else {
                        this.outOfOpenWaterTicks = Math.min(10, this.outOfOpenWaterTicks + 1);
                    }
                }
            }

            if (!fluidState.isIn(FluidTags.WATER)) {
                this.setVelocity(this.getVelocity().add(0.0, -0.03, 0.0));
            }

            this.move(MovementType.SELF, this.getVelocity());
            this.updateRotation();
            if (this.state == FishingFellaBobberEntity.State.FLYING && (this.isOnGround() || this.horizontalCollision)) {
                this.setVelocity(Vec3d.ZERO);
            }

            double e = 0.92;
            this.setVelocity(this.getVelocity().multiply(e));
            this.refreshPosition();

            if(getPos().squaredDistanceTo(getGolemOwner().getPos()) <= 0.15) {
                this.kill();
            }
        }
    }

    private boolean removeIfInvalid(FishingFellaEntity golem) {
        ItemStack itemStack = golem.getMainHandStack();
        boolean bl = itemStack.isOf(Items.FISHING_ROD);
        if (!golem.isRemoved() && golem.isAlive() && bl && !(this.squaredDistanceTo(golem) > 1024.0)) {
            return false;
        } else {
            this.discard();
            return true;
        }
    }

    private void checkForCollision() {
        HitResult hitResult = ProjectileUtil.getCollision(this, this::canHit);
        this.onCollision(hitResult);
    }

    protected boolean canHit(Entity entity) {
        return super.canHit(entity) || entity.isAlive() && entity instanceof ItemEntity;
    }

    protected void onEntityHit(EntityHitResult entityHitResult) {
        super.onEntityHit(entityHitResult);
        if (!this.getWorld().isClient) {
            this.updateHookedEntityId(entityHitResult.getEntity());
        }

    }

    protected void onBlockHit(BlockHitResult blockHitResult) {
        super.onBlockHit(blockHitResult);
        this.setVelocity(this.getVelocity().normalize().multiply(blockHitResult.squaredDistanceTo(this)));
    }

    private void updateHookedEntityId(@Nullable Entity entity) {
        this.hookedEntity = entity;
        this.getDataTracker().set(HOOK_ENTITY_ID, entity == null ? 0 : entity.getId() + 1);
    }

    private void tickFishingLogic(BlockPos pos) {
        ServerWorld serverWorld = (ServerWorld) this.getWorld();
        int i = 1;
        BlockPos blockPos = pos.up();
        if (this.random.nextFloat() < 0.25F && this.getWorld().hasRain(blockPos)) {
            ++i;
        }

        if (this.random.nextFloat() < 0.5F && !this.getWorld().isSkyVisible(blockPos)) {
            --i;
        }

        if (this.hookCountdown > 0) {
            --this.hookCountdown;
            if (this.hookCountdown <= 0) {
                this.waitCountdown = 0;
                this.fishTravelCountdown = 0;
                this.getDataTracker().set(CAUGHT_FISH, false);
            }
        } else {
            float f;
            float g;
            float h;
            double d;
            double e;
            double j;
            BlockState blockState;
            if (this.fishTravelCountdown > 0) {
                this.fishTravelCountdown -= i;
                if (this.fishTravelCountdown > 0) {
                    this.fishAngle += (float) this.random.nextTriangular(0.0, 9.188);
                    f = this.fishAngle * 0.017453292F;
                    g = MathHelper.sin(f);
                    h = MathHelper.cos(f);
                    d = this.getX() + (double) (g * (float) this.fishTravelCountdown * 0.1F);
                    e = ((float) MathHelper.floor(this.getY()) + 1.0F);
                    j = this.getZ() + (double) (h * (float) this.fishTravelCountdown * 0.1F);
                    blockState = serverWorld.getBlockState(BlockPos.ofFloored(d, e - 1.0, j));
                    if (blockState.isOf(Blocks.WATER)) {
                        if (this.random.nextFloat() < 0.15F) {
                            serverWorld.spawnParticles(ParticleTypes.BUBBLE, d, e - 0.10000000149011612, j, 1, (double) g, 0.1, (double) h, 0.0);
                        }

                        float k = g * 0.04F;
                        float l = h * 0.04F;
                        serverWorld.spawnParticles(ParticleTypes.FISHING, d, e, j, 0, (double) l, 0.01, (double) (-k), 1.0);
                        serverWorld.spawnParticles(ParticleTypes.FISHING, d, e, j, 0, (double) (-l), 0.01, (double) k, 1.0);
                    }
                } else {
                    this.playSound(SoundEvents.ENTITY_FISHING_BOBBER_SPLASH, 0.25F, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.4F);
                    double m = this.getY() + 0.5;
                    serverWorld.spawnParticles(ParticleTypes.BUBBLE, this.getX(), m, this.getZ(), (int) (1.0F + this.getWidth() * 20.0F), (double) this.getWidth(), 0.0, (double) this.getWidth(), 0.20000000298023224);
                    serverWorld.spawnParticles(ParticleTypes.FISHING, this.getX(), m, this.getZ(), (int) (1.0F + this.getWidth() * 20.0F), (double) this.getWidth(), 0.0, (double) this.getWidth(), 0.20000000298023224);
                    this.hookCountdown = MathHelper.nextInt(this.random, 20, 40);
                    this.getDataTracker().set(CAUGHT_FISH, true);

                    use(getGolemOwner().getStackInHand(Hand.MAIN_HAND));
                }
            } else if (this.waitCountdown > 0) {
                this.waitCountdown -= i;
                f = 0.15F;
                if (this.waitCountdown < 20) {
                    f += (float) (20 - this.waitCountdown) * 0.05F;
                } else if (this.waitCountdown < 40) {
                    f += (float) (40 - this.waitCountdown) * 0.02F;
                } else if (this.waitCountdown < 60) {
                    f += (float) (60 - this.waitCountdown) * 0.01F;
                }

                if (this.random.nextFloat() < f) {
                    g = MathHelper.nextFloat(this.random, 0.0F, 360.0F) * 0.017453292F;
                    h = MathHelper.nextFloat(this.random, 25.0F, 60.0F);
                    d = this.getX() + (double) (MathHelper.sin(g) * h) * 0.1;
                    e = (double) ((float) MathHelper.floor(this.getY()) + 1.0F);
                    j = this.getZ() + (double) (MathHelper.cos(g) * h) * 0.1;
                    blockState = serverWorld.getBlockState(BlockPos.ofFloored(d, e - 1.0, j));
                    if (blockState.isOf(Blocks.WATER)) {
                        serverWorld.spawnParticles(ParticleTypes.SPLASH, d, e, j, 2 + this.random.nextInt(2), 0.10000000149011612, 0.0, 0.10000000149011612, 0.0);
                    }
                }

                if (this.waitCountdown <= 0) {
                    this.fishAngle = MathHelper.nextFloat(this.random, 0.0F, 360.0F);
                    this.fishTravelCountdown = MathHelper.nextInt(this.random, 20, 80);
                }
            } else {
                this.waitCountdown = MathHelper.nextInt(this.random, 100, 600);
                this.waitCountdown -= this.lureLevel * 20 * 5;
            }
        }

    }

    private boolean isOpenOrWaterAround(BlockPos pos) {
        FishingFellaBobberEntity.PositionType positionType = FishingFellaBobberEntity.PositionType.INVALID;

        for (int i = -1; i <= 2; ++i) {
            FishingFellaBobberEntity.PositionType positionType2 = this.getPositionType(pos.add(-2, i, -2), pos.add(2, i, 2));
            switch (positionType2) {
                case INVALID:
                    return false;
                case ABOVE_WATER:
                    if (positionType == FishingFellaBobberEntity.PositionType.INVALID) {
                        return false;
                    }
                    break;
                case INSIDE_WATER:
                    if (positionType == FishingFellaBobberEntity.PositionType.ABOVE_WATER) {
                        return false;
                    }
            }

            positionType = positionType2;
        }

        return true;
    }

    private FishingFellaBobberEntity.PositionType getPositionType(BlockPos start, BlockPos end) {
        return BlockPos.stream(start, end).map(this::getPositionType).reduce((positionType, positionType2) -> {
            return positionType == positionType2 ? positionType : FishingFellaBobberEntity.PositionType.INVALID;
        }).orElse(FishingFellaBobberEntity.PositionType.INVALID);
    }

    private FishingFellaBobberEntity.PositionType getPositionType(BlockPos pos) {
        BlockState blockState = this.getWorld().getBlockState(pos);
        if (!blockState.isAir() && !blockState.isOf(Blocks.LILY_PAD)) {
            FluidState fluidState = blockState.getFluidState();
            return fluidState.isIn(FluidTags.WATER) && fluidState.isStill() && blockState.getCollisionShape(this.getWorld(), pos).isEmpty() ? FishingFellaBobberEntity.PositionType.INSIDE_WATER : FishingFellaBobberEntity.PositionType.INVALID;
        } else {
            return FishingFellaBobberEntity.PositionType.ABOVE_WATER;
        }
    }

    public boolean isInOpenWater() {
        return this.inOpenWater;
    }

    public void writeCustomDataToNbt(NbtCompound nbt) {
    }

    public void readCustomDataFromNbt(NbtCompound nbt) {
    }

    public int use(ItemStack usedItem) {
        FishingFellaEntity fishingGolem = this.getGolemOwner();
        if (!this.getWorld().isClient && fishingGolem != null && !this.removeIfInvalid(fishingGolem)) {
            int i = 0;
            if (this.hookedEntity != null) {
                this.pullHookedEntity(this.hookedEntity);
                //Criteria.FISHING_ROD_HOOKED.trigger((ServerPlayerEntity)fishingGolem, usedItem, this, Collections.emptyList());
                this.getWorld().sendEntityStatus(this, EntityStatuses.PULL_HOOKED_ENTITY);
                i = this.hookedEntity instanceof ItemEntity ? 3 : 5;
            } else if (this.hookCountdown > 0) {
                LootContextParameterSet lootContextParameterSet =
                        (new LootContextParameterSet.Builder((ServerWorld) this.getWorld()))
                                .add(LootContextParameters.ORIGIN, this.getPos())
                                .add(LootContextParameters.TOOL, usedItem)
                                .add(LootContextParameters.THIS_ENTITY, this)
                                .luck((float) this.luckOfTheSeaLevel + fishingGolem.getLuck())
                                .build(LootContextTypes.FISHING);
                LootTable lootTable = this.getWorld().getServer().getLootManager().getLootTable(LootTables.FISHING_GAMEPLAY);
                List<ItemStack> list = lootTable.generateLoot(lootContextParameterSet);
                //Criteria.FISHING_ROD_HOOKED.trigger((ServerPlayerEntity)fishingGolem, usedItem, this, list);

                for (ItemStack itemStack : list) {
                    ItemEntity itemEntity = new ItemEntity(this.getWorld(), this.getX(), this.getY(), this.getZ(), itemStack);
                    double d = fishingGolem.getX() - this.getX();
                    double e = fishingGolem.getY() - this.getY();
                    double f = fishingGolem.getZ() - this.getZ();
                    double g = 0.1;
                    itemEntity.setVelocity(d * g, (e) * g + Math.sqrt(Math.sqrt(d * d + e * e + f * f)) * 0.08, f * g);
                    //this.getWorld().spawnEntity(itemEntity);
                    fishingGolem.getWorld().spawnEntity(new ExperienceOrbEntity(fishingGolem.getWorld(), fishingGolem.getX(), fishingGolem.getY() + 0.5, fishingGolem.getZ() + 0.5, this.random.nextInt(6) + 1));
                    if (itemStack.isIn(ItemTags.FISHES)) {
                        //fishingGolem.increaseStat(Stats.FISH_CAUGHT, 1);
                    }

                    fishingGolem.insertStack(itemStack);
                }

                i = 1;
            }

            if (this.isOnGround()) {
                i = 2;
            }

            this.discard();
            return i;
        } else {
            return 0;
        }
    }

    public void handleStatus(byte status) {
        if (status == EntityStatuses.PULL_HOOKED_ENTITY && this.getWorld().isClient && this.hookedEntity instanceof PlayerEntity && ((PlayerEntity) this.hookedEntity).isMainPlayer()) {
            this.pullHookedEntity(this.hookedEntity);
        }

        super.handleStatus(status);
    }

    protected void pullHookedEntity(Entity entity) {
        Entity entity2 = this.getOwner();
        if (entity2 != null) {
            Vec3d vec3d = (new Vec3d(entity2.getX() - this.getX(), entity2.getY() - this.getY(), entity2.getZ() - this.getZ())).multiply(0.1);
            entity.setVelocity(entity.getVelocity().add(vec3d));
        }
    }

    protected Entity.MoveEffect getMoveEffect() {
        return Entity.MoveEffect.NONE;
    }

    public void remove(Entity.RemovalReason reason) {
        this.setPlayerFishHook(null);
        super.remove(reason);
    }

    public void onRemoved() {
        this.setPlayerFishHook(null);
    }

    public void setOwner(@Nullable Entity entity) {
        super.setOwner(entity);
        this.setPlayerFishHook(this);
    }

    private void setPlayerFishHook(@Nullable FishingFellaBobberEntity fishingBobber) {
        FishingFellaEntity fishingGolem = this.getGolemOwner();
        if (fishingGolem != null) {
            fishingGolem.fishHook = fishingBobber;
        }

    }

    @Nullable
    public FishingFellaEntity getGolemOwner() {
        Entity entity = this.getOwner();
        return entity instanceof FishingFellaEntity ? (FishingFellaEntity) entity : null;
    }

    @Nullable
    public Entity getHookedEntity() {
        return this.hookedEntity;
    }

    public boolean canUsePortals() {
        return false;
    }

    public Packet<ClientPlayPacketListener> createSpawnPacket() {
        Entity entity = this.getOwner();
        return new EntitySpawnS2CPacket(this, entity == null ? this.getId() : entity.getId());
    }

    enum State {
        FLYING,
        HOOKED_IN_ENTITY,
        BOBBING;
    }

    private enum PositionType {
        ABOVE_WATER,
        INSIDE_WATER,
        INVALID;
    }
}