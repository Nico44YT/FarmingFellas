package nico.farmingfellas.common.entity.lumberjack;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import nico.farmingfellas.common.entity.FellaVariant;
import nico.farmingfellas.common.entity.base.FellaGolemEntity;
import nico.farmingfellas.common.entity.base.goal.EmptyInventoryGoal;
import nico.farmingfellas.common.entity.farming.goal.harvest.CropBlockHarvestGoal;
import nico.farmingfellas.common.entity.lumberjack.goal.harvest.TreeHarvestGoal;
import nico.farmingfellas.common.item.ModItems;
import nico.farmingfellas.screen.custom.Generic3x2ContainerScreenHandler;
import org.jetbrains.annotations.Nullable;

public class LumberjackFellaEntity extends FellaGolemEntity {
    public LumberjackFellaEntity(EntityType<? extends PathAwareEntity> entityType, World world) {
        super(entityType, world, 6);
    }

    @Override
    protected void initGoals() {
        super.initGoals();

        this.goalSelector.add(1, new TreeHarvestGoal(this));
        this.goalSelector.add(2, new EmptyInventoryGoal(this));
    }

    @Override
    public void tick() {
        super.tick();

        this.setStackInHand(Hand.MAIN_HAND, Items.IRON_AXE.getDefaultStack());
    }

    @Override
    public @Nullable ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new Generic3x2ContainerScreenHandler(syncId, playerInventory, this);
    }

    @Override
    public ItemStack getPickBlockStack() {
        ItemStack stack = ModItems.LUMBERJACK_GOLEM_ITEM.getDefaultStack();
        if (this.hasCustomName()) stack.setCustomName(this.getCustomName());
        if (!this.getInventory().isEmpty())
            Inventories.writeNbt(stack.getOrCreateSubNbt("Inventory"), this.getInventory().stacks);
        this.getZoneId().ifPresent(zoneId -> stack.getOrCreateNbt().putUuid("zone_id", zoneId));
        return stack;
    }

    @Override
    public FellaVariant getVariant() {
        return FellaVariant.LUMBERJACK;
    }
}