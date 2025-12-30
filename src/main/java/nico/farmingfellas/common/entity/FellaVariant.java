package nico.farmingfellas.common.entity;

import net.minecraft.util.StringIdentifiable;

public enum FellaVariant implements StringIdentifiable {
    FARMER("farmer"),
    LUMBERJACK("lumberjack"),
    FISHER("fisher");

    private final String id;
    FellaVariant(String id) {
        this.id = id;
    }

    @Override
    public String asString() {
        return id;
    }
}
