package nico.farmingfellas.common.block.fertilizer;

import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class FertilizerHolderBlock extends BlockWithEntity {
    public FertilizerHolderBlock(Settings settings) {
        super(settings);
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new FertilizerHolderBlockEntity(pos, state);
    }
}
