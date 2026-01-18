package nico.farmingfellas.common.world;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.world.GameRules;
import nico.farmingfellas.FarmingFellasMain;

public class ModGamerules {
    public static final GameRules.Key<GameRules.BooleanRule> TRAMPLE_FARMLAND = register("trample_farmland", GameRules.Category.MISC, GameRuleFactory.createBooleanRule(false));

    public static void register() {

    }

    private static <T extends GameRules.Rule<T>> GameRules.Key<T> register(String name, GameRules.Category category, GameRules.Type<T> type) {
        return GameRuleRegistry.register(FarmingFellasMain.MOD_ID + ":" + name, category, type);
    }
}
