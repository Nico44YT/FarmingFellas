package nico.farmingfellas.common.block.fertilizer_holder;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

public class FertilizerHolderBlock extends BlockWithEntity {
    public static final int MAX_LEVEL = 3;
    public static final IntProperty LEVEL = IntProperty.of("level", 0, MAX_LEVEL);

    private static final VoxelShape SHAPE = Stream.of(
            Block.createCuboidShape(0, 0, 0, 3, 16, 3),
            Block.createCuboidShape(13, 0, 0, 16, 16, 3),
            Block.createCuboidShape(13, 0, 13, 16, 16, 16),
            Block.createCuboidShape(0, 0, 13, 3, 16, 16),
            Block.createCuboidShape(3, 0, 0.5, 13, 3, 2.5),
            Block.createCuboidShape(3, 4, 0.5, 13, 7, 2.5),
            Block.createCuboidShape(3, 8, 0.5, 13, 11, 2.5),
            Block.createCuboidShape(3, 12, 0.5, 13, 15, 2.5),
            Block.createCuboidShape(3, 0, 13.5, 13, 3, 15.5),
            Block.createCuboidShape(3, 4, 13.5, 13, 7, 15.5),
            Block.createCuboidShape(3, 8, 13.5, 13, 11, 15.5),
            Block.createCuboidShape(3, 12, 13.5, 13, 15, 15.5),
            Block.createCuboidShape(0.5, 0, 3, 2.5, 3, 13),
            Block.createCuboidShape(0.5, 4, 3, 2.5, 7, 13),
            Block.createCuboidShape(0.5, 8, 3, 2.5, 11, 13),
            Block.createCuboidShape(0.5, 12, 3, 2.5, 15, 13),
            Block.createCuboidShape(13.5, 0, 3, 15.5, 3, 13),
            Block.createCuboidShape(13.5, 4, 3, 15.5, 7, 13),
            Block.createCuboidShape(13.5, 8, 3, 15.5, 11, 13),
            Block.createCuboidShape(13.5, 12, 3, 15.5, 15, 13),
            Block.createCuboidShape(2.5, 0, 2.5, 13.5, 1, 13.5)
    ).reduce((v1, v2) -> VoxelShapes.combineAndSimplify(v1, v2, BooleanBiFunction.OR)).get();

    public FertilizerHolderBlock(Settings settings) {
        super(settings);
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new FertilizerHolderBlockEntity(pos, state);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(
                builder.add(LEVEL)
        );
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }
}
