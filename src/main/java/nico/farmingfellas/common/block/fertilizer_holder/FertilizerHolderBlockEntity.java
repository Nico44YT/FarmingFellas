package nico.farmingfellas.common.block.fertilizer_holder;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import nico.farmingfellas.common.block.ModBlocks;

public class FertilizerHolderBlockEntity extends BlockEntity {

    public static final int MAX_FERTILIZERS = 64;
    public int currentFertilizers = 0;

    public FertilizerHolderBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.FERTILIZED_HOLDER_TYPE, pos, state);
    }

    public int tryFillHolder(int amount) {
        if (amount <= 0 || currentFertilizers >= MAX_FERTILIZERS) {
            return 0;
        }

        int space = MAX_FERTILIZERS - currentFertilizers;
        int inserted = Math.min(space, amount);

        currentFertilizers += inserted;

        var world = getWorld();
        var pos = getPos();

        int level = (int)Math.floor(
                currentFertilizers * ((FertilizerHolderBlock.MAX_LEVEL - 1) / (float) MAX_FERTILIZERS)
        ) + 1;

        world.setBlockState(
                pos,
                world.getBlockState(pos).with(FertilizerHolderBlock.LEVEL, level)
        );

        return inserted;
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);

        nbt.putInt("amount", currentFertilizers);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);

        this.currentFertilizers = nbt.getInt("amount");
    }
}
