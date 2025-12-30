package nico.farmingfellas.common.entity.farming;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.world.World;
import nico.farmingfellas.common.entity.FellaVariant;
import nico.farmingfellas.common.entity.base.FellaGolemEntity;
import nico.farmingfellas.common.entity.base.goal.EmptyInventoryGoal;
import nico.farmingfellas.common.entity.farming.goal.harvest.*;
import nico.farmingfellas.screen.custom.Generic3x2ContainerScreenHandler;
import org.jetbrains.annotations.Nullable;

public class FarmingFellaEntity extends FellaGolemEntity {
    public FarmingFellaEntity(EntityType<? extends PathAwareEntity> entityType, World world) {
        super(entityType, world, 6);
    }

    @Override
    protected void initGoals() {
        super.initGoals();

        this.goalSelector.add(1, new CropBlockHarvestGoal(this));
        this.goalSelector.add(1, new StemBlockHarvestGoal(this));

        this.goalSelector.add(1, new SweetBerryHarvestGoal(this));
        this.goalSelector.add(1, new CocoaBeansHarvestGoal(this));
        this.goalSelector.add(1, new NetherWartHarvestGoal(this));
        this.goalSelector.add(1, new GlowBerryHarvestGoal(this));

        this.goalSelector.add(2, new EmptyInventoryGoal(this));
    }


    @Override
    public @Nullable ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new Generic3x2ContainerScreenHandler(syncId, playerInventory, this);
    }

    @Override
    public ItemStack getPickBlockStack() {
        return ItemStack.EMPTY;
    }

    @Override
    public FellaVariant getVariant() {
        return FellaVariant.FARMER;
    }
}
