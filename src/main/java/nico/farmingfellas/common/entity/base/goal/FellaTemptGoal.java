package nico.farmingfellas.common.entity.base.goal;

import net.minecraft.entity.ai.goal.TemptGoal;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.Hand;
import nico.farmingfellas.common.entity.base.FellaGolemEntity;
import nico.farmingfellas.common.entity.base.GolemAnimationState;

public class FellaTemptGoal extends TemptGoal {

    private final FellaGolemEntity golem;
    public FellaTemptGoal(FellaGolemEntity entity, double speed) {
        super(entity, speed, Ingredient.ofItems(Items.COOKIE), false);
        this.golem = entity;
    }

    @Override
    public boolean canStart() {
        return super.canStart() && !this.golem.hasCookie();
    }

    @Override
    public void start() {
        super.start();

        this.golem.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
    }

    @Override
    public void stop() {
        super.stop();

        this.golem.jumpingMultiplier = 1f;
    }

    @Override
    public void tick() {
        this.mob.getLookControl().lookAt(this.closestPlayer, (float)(this.mob.getMaxHeadRotation() + 20), (float)this.mob.getMaxLookPitchChange());
        if (this.mob.squaredDistanceTo(this.closestPlayer) < 4.25) {
            this.mob.getJumpControl().setActive();
            this.golem.jumpingMultiplier = 0.5f;
            this.golem.setState(GolemAnimationState.BEGGING_COOKIE);
            this.mob.getNavigation().stop();
        } else {
            this.mob.getNavigation().startMovingTo(this.closestPlayer, this.speed);
            this.golem.jumpingMultiplier = 1f;
            this.golem.setState(GolemAnimationState.IDLE);
        }
    }
}
