package nico.farmingfellas.common.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
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

        HitResult hitResult = context.getPlayer().raycast(6, 0, false);
        if (hitResult.getType() == HitResult.Type.BLOCK) {
            assert player != null;
            FellaGolemEntity golem = factory.apply(world);

            golem.setPosition(hitResult.getPos());

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

    @Override
    public Text getName(ItemStack stack) {
        return Text.translatable(super.getName(stack).getString().replace("item.", "entity."));
    }
}
