package nico.farmingfellas.common.item.zoning;

import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import nico.farmingfellas.common.data.Zone;
import nico.farmingfellas.common.data.ZoneManager;
import nico.farmingfellas.common.entity.base.FellaGolemEntity;
import nico.farmingfellas.common.item.SimpleItemModel;

import java.util.*;


public class ZoningMapItem extends Item implements SimpleItemModel {

    public static final String TAG_ZONE_DATA = "zoneData";
    public static final String TAG_ZONE_MODE = "zoningMode";

    public static final String TAG_ZONE_ID = "zoneId";
    public static final String TAG_ZONE_COLOR = "zoneColor";

    public static final String TAG_CORNER_A = "corner_a";
    public static final String TAG_CORNER_B = "corner_b";
    public static final String TAG_CHEST_LOCATIONS = "chest_positions";

    public static final String TAG_DIRTY = "dirty";

    public ZoningMapItem(Settings settings) {
        super(settings);
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (world.isClient()) return;

        NbtCompound root = stack.getOrCreateNbt();
        NbtCompound zoneNbt = stack.getOrCreateSubNbt(TAG_ZONE_DATA);
        ZoneManager zoneManager = ZoneManager.getInstance();

        // Initialize zoning mode once
        if (!root.contains(TAG_ZONE_MODE)) {
            root.putInt(TAG_ZONE_MODE, ZoningMode.AREA.asInt());
        }

        // Create zone if missing
        if (!zoneNbt.contains(TAG_ZONE_ID)) {
            Zone newZone = zoneManager.createNewZone(world);
            zoneNbt.copyFrom(newZone.toNbt());
            return;
        }

        if (root.contains(TAG_DIRTY) && root.getBoolean(TAG_DIRTY)) {
            Zone zone = zoneManager.getZone(zoneNbt.getInt(TAG_ZONE_ID));
            if (zone != null) {
                zone.readNbt(zoneNbt);
            }
            root.putBoolean(TAG_DIRTY, false);
        }
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        if(user.getWorld() instanceof ServerWorld && entity instanceof FellaGolemEntity golem) {
            golem.setZone(stack.getSubNbt(TAG_ZONE_DATA).getInt(TAG_ZONE_ID));
            return ActionResult.SUCCESS;
        }
        return super.useOnEntity(stack, user, entity, hand);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (world.isClient() || !user.isSneaking()) {
            return TypedActionResult.success(stack);
        }

        NbtCompound nbt = stack.getOrCreateNbt();
        ZoningMode[] modes = ZoningMode.values();

        int current = nbt.getInt(TAG_ZONE_MODE);
        int next = (current + 1) % modes.length;
        nbt.putInt(TAG_ZONE_MODE, next);

        MutableText text = Text.empty()
                .append(Text.literal(modes[(next - 1 + modes.length) % modes.length].getName())
                        .formatted(Formatting.DARK_GRAY))
                .append(Text.literal("   "))
                .append(Text.literal(modes[next].getName())
                        .formatted(Formatting.BOLD))
                .append(Text.literal("   "))
                .append(Text.literal(modes[(next + 1) % modes.length].getName())
                        .formatted(Formatting.DARK_GRAY));

        user.sendMessage(text, true);
        return TypedActionResult.success(stack);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        if (world.isClient()) return ActionResult.SUCCESS;

        ItemStack stack = context.getStack();
        BlockPos pos = context.getBlockPos();
        PlayerEntity player = context.getPlayer();

        NbtCompound root = stack.getOrCreateNbt();
        NbtCompound zoneData = stack.getOrCreateSubNbt(TAG_ZONE_DATA);
        ZoningMode mode = ZoningMode.fromInt(root.getInt(TAG_ZONE_MODE));

        if(mode == ZoningMode.AREA) {
            boolean hasA = zoneData.contains(TAG_CORNER_A);
            boolean hasB = zoneData.contains(TAG_CORNER_B);

            // If both corners are already set, restart zoning
            if (hasA && hasB) {
                zoneData.remove(TAG_CORNER_A);
                zoneData.remove(TAG_CORNER_B);
            }

            // Assign next corner
            if (!zoneData.contains(TAG_CORNER_A)) {
                zoneData.putLong(TAG_CORNER_A, pos.asLong());
            } else {
                zoneData.putLong(TAG_CORNER_B, pos.asLong());
                root.putBoolean(TAG_DIRTY, true);
            }

            return ActionResult.SUCCESS;
        }

        if(mode == ZoningMode.CHEST) {
            BlockState clickedState = world.getBlockState(pos);
            if(!(clickedState.getBlock() instanceof ChestBlock)) return ActionResult.PASS;

            Set<Long> chestLocations = new HashSet<>();
            for (long l : zoneData.getLongArray(TAG_CHEST_LOCATIONS)) {
                chestLocations.add(l);
            }

            if(chestLocations.contains(pos.asLong())) {
                player.sendMessage(Text.literal("Removed chest at " + pos.toShortString()));
                chestLocations.remove(pos.asLong());
            } else {
                player.sendMessage(Text.literal("Added chest at " + pos.toShortString()));
                chestLocations.add(pos.asLong());
            }

            root.putBoolean(TAG_DIRTY, true);
            zoneData.putLongArray(TAG_CHEST_LOCATIONS, chestLocations.stream().toList());
        }

        return ActionResult.SUCCESS;
    }

    public enum ZoningMode {
        AREA("Area", 0),
        CHEST("Chest", 1);

        private final String name;
        private final int id;

        ZoningMode(String name, int id) {
            this.name = name;
            this.id = id;
        }

        public int asInt() {
            return id;
        }

        public String getName() {
            return this.name;
        }

        public static ZoningMode fromInt(int id) {
            for (ZoningMode mode : values()) {
                if (mode.id == id) return mode;
            }
            return AREA;
        }
    }

    public static int getColor(ItemStack stack) {
        NbtCompound zoneData = stack.getSubNbt(TAG_ZONE_DATA);
        if (zoneData == null) return 0;
        return zoneData.getInt(TAG_ZONE_COLOR) | 0xAA000000;
    }

    public static Optional<BlockPos> getZonePos(String tag, ItemStack stack) {
        NbtCompound zoneData = stack.getSubNbt(TAG_ZONE_DATA);
        if (zoneData == null || !zoneData.contains(tag)) return Optional.empty();
        return Optional.of(BlockPos.fromLong(zoneData.getLong(tag)));
    }

    public static boolean isZonePosSet(String tag, ItemStack stack) {
        NbtCompound zoneData = stack.getSubNbt(TAG_ZONE_DATA);
        return zoneData != null && zoneData.contains(tag);
    }

    public static boolean isSameZone(ItemStack stack, BlockPos a, BlockPos b) {
        Optional<BlockPos> thisA = getZonePos(TAG_CORNER_A, stack);
        Optional<BlockPos> thisB = getZonePos(TAG_CORNER_B, stack);

        if (thisA.isEmpty() || thisB.isEmpty()) return false;

        return (thisA.get().equals(a) && thisB.get().equals(b))
                || (thisA.get().equals(b) && thisB.get().equals(a));
    }

    public static List<BlockPos> getChestPositions(ItemStack holdingStack) {
        return Arrays.stream(holdingStack.getSubNbt(TAG_ZONE_DATA).getLongArray(TAG_CHEST_LOCATIONS)).mapToObj(BlockPos::fromLong).toList();
    }
}