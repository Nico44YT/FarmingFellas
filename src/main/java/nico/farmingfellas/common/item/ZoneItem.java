package nico.farmingfellas.common.item;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import nico.farmingfellas.common.entity.base.ZoneHolderEntity;
import nico.farmingfellas.common.zone.Zone;
import nico.farmingfellas.common.zone.ZoneSaveData;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ZoneItem extends Item {

    private static final String ZONE_LAST_UPDATE_TIME = "last_update_time";
    private static final String ZONE_ID = "zone_id";
    private static final String ZONE_DATA = "zone_data";
    private static final String ZONE_DIRTY = "zone_dirty";

    public ZoneItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        if(entity instanceof ZoneHolderEntity zoneHolderEntity && user.isSneaking() && hand == Hand.MAIN_HAND) return zoneHolderEntity.assignZone(getZoneId(stack), stack, user);
        return super.useOnEntity(stack, user, entity, hand);
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, world, entity, slot, selected);

        if (!(world instanceof ServerWorld serverWorld)) return;

        UUID zoneId = getOrCreateZoneId(stack);
        Zone zone = ZoneSaveData.getOrCreateZone(serverWorld, zoneId);

        // Load stack data into zone
        if (stack.getSubNbt(ZONE_DATA) != null) {
            if(zone.getLastUpdateTime() < getLastUpdateTime(stack)) {
                NbtCompound nbtCompound = stack.getSubNbt(ZONE_DATA);
                assert nbtCompound != null;
                zone.copyData(Zone.fromNbt(nbtCompound));
            } else {
                stack.getSubNbt(ZONE_DATA).copyFrom(zone.asNbt());
            }
        }

        // Sync back if dirty
        if (isDirty(stack)) {
            ZoneSaveData.getZone(serverWorld, zoneId)
                    .ifPresent(saved -> saved.copyData(zone));

            stack.getOrCreateSubNbt(ZONE_DATA).copyFrom(zone.asNbt());
            setDirty(stack, false);
        }
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        ItemStack stack = context.getStack();

        if (!(world instanceof ServerWorld serverWorld)) {
            return super.useOnBlock(context);
        }

        UUID zoneId = getOrCreateZoneId(stack);
        Zone zone = ZoneSaveData.getOrCreateZone(serverWorld, zoneId);
        zone.setLastUpdateTime(world.getTime());

        if (zone.getCornerB().isEmpty()) {
            zone.setCornerB(world, pos);
        } else {
            zone.setCorners(world, pos, null);
        }

        stack.getOrCreateSubNbt(ZONE_DATA).copyFrom(zone.asNbt());
        setDirty(stack, true);


        return ActionResult.SUCCESS;
    }

    /* -------------------- Helpers -------------------- */

    private static UUID getOrCreateZoneId(ItemStack stack) {
        var nbt = stack.getOrCreateNbt();
        if (!nbt.containsUuid(ZONE_ID)) {
            nbt.putUuid(ZONE_ID, UUID.randomUUID());
        }
        return nbt.getUuid(ZONE_ID);
    }

    private static Optional<UUID> getZoneId(ItemStack stack) {
        var nbt = stack.getNbt();
        if(nbt == null) return Optional.empty();
        return Optional.ofNullable(nbt.getUuid(ZONE_ID));
    }

    private static boolean isDirty(ItemStack stack) {
        return stack.getOrCreateNbt().getBoolean(ZONE_DIRTY);
    }

    private static void setDirty(ItemStack stack, boolean dirty) {
        stack.getOrCreateNbt().putBoolean(ZONE_DIRTY, dirty);
    }

    private static void setLastUpdateTime(ItemStack stack, long lastUpdateTime) {
        stack.getOrCreateSubNbt(ZONE_DATA).putLong(ZONE_LAST_UPDATE_TIME, lastUpdateTime);
    }

    private static long getLastUpdateTime(ItemStack stack) {
        return stack.getOrCreateSubNbt(ZONE_DATA).getLong(ZONE_LAST_UPDATE_TIME);
    }

    public static Optional<BlockPos> getCornerA(ItemStack stack) {
        if(stack.getSubNbt(ZONE_DATA) == null || !stack.getSubNbt(ZONE_DATA).contains("corner_a")) return Optional.empty();
        return Optional.of(BlockPos.fromLong(stack.getSubNbt(ZONE_DATA).getLong("corner_a")));
    }

    public static Optional<BlockPos> getCornerB(ItemStack stack) {
        if(stack.getSubNbt(ZONE_DATA) == null || !stack.getSubNbt(ZONE_DATA).contains("corner_b")) return Optional.empty();
        return Optional.of(BlockPos.fromLong(stack.getSubNbt(ZONE_DATA).getLong("corner_b")));
    }

    @Override
    public boolean allowNbtUpdateAnimation(PlayerEntity player, Hand hand, ItemStack oldStack, ItemStack newStack) {
        return false;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);

        getCornerA(stack).ifPresent(blockPos -> {
            tooltip.add(Text.translatable("item.farming_fellas.zoning_map.from", blockPos.toShortString()));
        });

        getCornerB(stack).ifPresent(blockPos -> {
            tooltip.add(Text.translatable("item.farming_fellas.zoning_map.to", blockPos.toShortString()));
        });

        if(Screen.hasShiftDown()) {
            getCornerA(stack).ifPresent(cornerA -> {
                getCornerB(stack).ifPresent(cornerB -> {
                    int width = Math.abs(cornerA.getX() - cornerB.getX()) + 1;
                    int height = Math.abs(cornerA.getY() - cornerB.getY()) + 1;
                    int length = Math.abs(cornerA.getZ() - cornerB.getZ()) + 1;
                    int volume = width * height * length;

                    tooltip.add(Text.translatable("item.farming_fellas.zoning_map.zone_area", volume));
                });
            });

            getZoneId(stack).ifPresent(zoneId -> {
                tooltip.add(Text.translatable("item.farming_fellas.zoning_map.zone_id", zoneId.toString()));
            });
        } else {
            tooltip.add(Text.translatable("item.farming_fellas.zoning_map.more_info"));
        }
    }
}
