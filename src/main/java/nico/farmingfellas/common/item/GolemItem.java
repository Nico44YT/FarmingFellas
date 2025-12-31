package nico.farmingfellas.common.item;

import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import nico.farmingfellas.common.entity.base.FellaGolemEntity;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class GolemItem extends Item {

    private final Function<World, FellaGolemEntity> factory;

    public GolemItem(Settings settings, Function<World, FellaGolemEntity> factory) {
        super(settings);

        this.factory = factory;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        PlayerEntity player = context.getPlayer();
        BlockPos pos = context.getBlockPos();
        World world = context.getWorld();
        ItemStack stack = context.getStack();


        if ((world.getBlockState(pos.offset(context.getSide())).isAir() && world.getBlockState(pos.offset(context.getSide()).offset(Direction.UP)).isAir() && world.getBlockState(pos.offset(context.getSide()).offset(Direction.UP, 2)).isAir()) || (world.getBlockState(pos.offset(context.getSide())).getBlock().equals(Blocks.WATER) && world.getBlockState(pos.offset(context.getSide()).offset(Direction.UP)).getBlock().equals(Blocks.WATER) && world.getBlockState(pos.offset(context.getSide()).offset(Direction.UP, 2)).getBlock().equals(Blocks.WATER))) {
            assert player != null;
            FellaGolemEntity golem = factory.apply(world);

            golem.refreshPositionAndAngles(pos.offset(context.getSide()), player.getHeadYaw() + 180, 0);

            if (stack.hasCustomName()) golem.setCustomName(stack.getName());
            world.spawnEntity(golem);
            stack.decrement(1);
            context.getPlayer().swingHand(context.getHand());
            context.getPlayer().getItemCooldownManager().set(this, 1);
            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }

    public FellaGolemEntity createGolem(@Nullable World world) {
        return factory.apply(world);
    }
}
