package nico.farmingfellas.common.entity.base;

import net.minecraft.util.StringIdentifiable;

public enum GolemAnimationState implements StringIdentifiable {
    IDLE("idle"),
    WORKING("working"),
    CHEST("chest"),
    BEGGING_COOKIE("begging_cookie"),
    EATING_COOKIE("eating_cookie"),
    GUI("gui");

    private final String id;
    GolemAnimationState(String id) {
        this.id = id;
    }

    @Override
    public String asString() {
        return id;
    }

    @Override
    public String toString() {
        return asString();
    }
}
