package nico.farmingfellas.common.entity.fishing;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.world.World;
import nico.farmingfellas.common.entity.FellaVariant;
import nico.farmingfellas.common.entity.base.FellaGolemEntity;
import nico.farmingfellas.common.entity.base.goal.EmptyInventoryGoal;
import nico.farmingfellas.common.entity.farming.goal.harvest.*;
import nico.farmingfellas.common.item.ModItems;
import nico.farmingfellas.screen.custom.Generic3x2ContainerScreenHandler;
import org.jetbrains.annotations.Nullable;

public class FishingFellaEntity extends FellaGolemEntity {
    public FishingFellaEntity(EntityType<? extends PathAwareEntity> entityType, World world) {
        super(entityType, world, 6);
    }

    @Override
    protected void initGoals() {
        super.initGoals();

        this.goalSelector.add(2, new EmptyInventoryGoal(this));
    }


    @Override
    public @Nullable ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new Generic3x2ContainerScreenHandler(syncId, playerInventory, this);
    }

    @Override
    public ItemStack getPickBlockStack() {
        ItemStack stack = ModItems.FISHING_GOLEM_ITEM.getDefaultStack();
        if (this.hasCustomName()) stack.setCustomName(this.getCustomName());
        if (!this.getInventory().isEmpty()) Inventories.writeNbt(stack.getOrCreateSubNbt("Inventory"), this.getInventory().stacks);
        this.getZoneId().ifPresent(zoneId -> stack.getOrCreateNbt().putUuid("zone_id", zoneId));
        return stack;
    }

    @Override
    public FellaVariant getVariant() {
        return FellaVariant.FISHER;
    }
}
