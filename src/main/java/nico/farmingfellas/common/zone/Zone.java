package nico.farmingfellas.common.zone;

import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import nico.farmingfellas.common.block.fertilizer_holder.FertilizerHolderBlock;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Zone {

    private final UUID zoneId;

    private BlockPos cornerA;
    private BlockPos cornerB;

    private long lastUpdateTime;

    private final Set<BlockPos> importantBlocks = new LinkedHashSet<>();

    public Zone(UUID zoneId) {
        this.zoneId = zoneId;
    }

    public static Zone createFromNbt(ServerWorld serverWorld, NbtCompound nbt) {
        if(nbt == null) {
            return ZoneSaveData.getOrCreateZone(serverWorld, UUID.randomUUID());
        }
        return fromNbt(nbt);
    }

    public void setCorners(@Nullable World world, BlockPos cornerA, BlockPos cornerB) {
        if (world != null) this.lastUpdateTime = world.getTime();
        this.cornerA = cornerA;
        this.cornerB = cornerB;
    }

    public void setCornerA(@Nullable World world, BlockPos cornerA) {
        if (world != null) this.lastUpdateTime = world.getTime();
        this.cornerA = cornerA;
    }

    public void setCornerB(@Nullable World world, BlockPos cornerB) {
        if (world != null) this.lastUpdateTime = world.getTime();
        this.cornerB = cornerB;
    }

    public Optional<BlockPos> getCornerA() {
        return Optional.ofNullable(cornerA);
    }

    public Optional<BlockPos> getCornerB() {
        return Optional.ofNullable(cornerB);
    }

    public UUID getZoneId() {
        return this.zoneId;
    }

    public long getLastUpdateTime() {
        return this.lastUpdateTime;
    }

    public void setLastUpdateTime(long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    @Override
    public int hashCode() {
        return zoneId.hashCode() * 31;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Zone zone && zone.zoneId.equals(this.zoneId);
    }

    public NbtCompound asNbt() {
        NbtCompound nbt = new NbtCompound();

        if (cornerA != null) nbt.putLong("corner_a", cornerA.asLong());
        if (cornerB != null) nbt.putLong("corner_b", cornerB.asLong());
        nbt.putLong("last_update_time", lastUpdateTime);
        nbt.putUuid("zone_id", zoneId);
        nbt.putLongArray("important_blocks", importantBlocks.stream().mapToLong(BlockPos::asLong).toArray());

        return nbt;
    }

    public static Zone fromNbt(NbtCompound nbt) {
        Zone zone = new Zone(nbt.getUuid("zone_id"));
        if (nbt.contains("corner_a")) zone.setCornerA(null, BlockPos.fromLong(nbt.getLong("corner_a")));
        if (nbt.contains("corner_b")) zone.setCornerB(null, BlockPos.fromLong(nbt.getLong("corner_b")));
        zone.lastUpdateTime = nbt.getLong("last_update_time");
        zone.importantBlocks.addAll(Arrays.stream(nbt.getLongArray("important_blocks")).mapToObj(BlockPos::fromLong).toList());

        return zone;
    }

    public void copyData(Zone otherZone) {
        this.cornerA = otherZone.cornerA;
        this.cornerB = otherZone.cornerB;
        this.lastUpdateTime = otherZone.lastUpdateTime;

        this.importantBlocks.clear();
        this.importantBlocks.addAll(otherZone.importantBlocks);
    }

    public boolean isInZone(BlockPos pos) {
        if (this.cornerA == null || this.cornerB == null || pos == null) return false;
        return new Box(this.cornerA, this.cornerB).contains(pos.toCenterPos());
    }

    public void forEach(Consumer<BlockPos> consumer) {
        getCornerA().ifPresent(a -> {
            getCornerB().ifPresent(b -> {
                int minX = Math.min(a.getX(), b.getX());
                int minY = Math.min(a.getY(), b.getY());
                int minZ = Math.min(a.getZ(), b.getZ());
                int maxX = Math.max(a.getX(), b.getX());
                int maxY = Math.max(a.getY(), b.getY());
                int maxZ = Math.max(a.getZ(), b.getZ());

                for (int x = minX; x <= maxX; x++) {
                    for (int y = minY; y <= maxY; y++) {
                        for (int z = minZ; z <= maxZ; z++) {
                            consumer.accept(new BlockPos(x, y, z));
                        }
                    }
                }
            });
        });
    }

    public Set<BlockPos> getImportantBlocks(World world) {
        return this.importantBlocks;
    }

    public List<BlockPos> getFertilizerHolder(World world) {
        return getImportantBlocks(world).stream().filter(pos -> world.getBlockState(pos).getBlock() instanceof FertilizerHolderBlock).toList();
    }

    public List<BlockPos> getChests(World world) {
        return getImportantBlocks(world).stream().filter(pos -> world.getBlockEntity(pos) instanceof ChestBlockEntity).toList();
    }

    public void addImportantBlock(BlockPos pos) {
        this.importantBlocks.add(pos);
    }

    public void removeImportantBlock(BlockPos pos) {
        this.importantBlocks.remove(pos);
    }
}
