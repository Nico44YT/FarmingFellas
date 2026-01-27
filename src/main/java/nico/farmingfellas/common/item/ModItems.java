package nico.farmingfellas.common.item;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import nico.farmingfellas.FarmingFellasMain;
import nico.farmingfellas.common.entity.ModEntities;
import nico.farmingfellas.common.entity.farming.FarmingFellaEntity;
import nico.farmingfellas.common.entity.lumberjack.LumberjackFellaEntity;
import nico.farmingfellas.common.entity.mining.MiningFellaEntity;
import nico.farmingfellas.common.item.custom.FertilizerItem;
import nico.farmingfellas.common.item.custom.GolemItem;
import nico.farmingfellas.common.item.custom.SaplingHolderKitItem;
import nico.farmingfellas.common.item.custom.ZoneItem;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

public class ModItems {
    public static Set<Item> items = new HashSet<>();
    public static final RegistryKey<ItemGroup> ITEM_GROUP = RegistryKey.of(RegistryKeys.ITEM_GROUP, FarmingFellasMain.id("item_group"));

    public static final Item BLANK_ZONING_MAP = register("blank_zoning_map", new Item.Settings().maxCount(1), ZoneItem::new);
    public static final Item FERTILIZER_ITEM = register("fertilizer", new Item.Settings(), FertilizerItem::new);
    public static final Item SAPLING_HOLDER_KIT = register("sapling_holder_kit", new Item.Settings().maxCount(16), SaplingHolderKitItem::new);

    public static final Item FARMING_GOLEM_ITEM = register("farming_golem", new Item.Settings(), $ -> new GolemItem($, world -> new FarmingFellaEntity(ModEntities.FARMING_GOLEM, world)));
    public static final Item LUMBERJACK_GOLEM_ITEM = register("lumberjack_golem", new Item.Settings(), $ -> new GolemItem($, world -> new LumberjackFellaEntity(ModEntities.LUMBERJACK_GOLEM, world)));
    public static final Item MINING_GOLEM_ITEM = register("mining_golem", new Item.Settings(), $ -> new GolemItem($, world -> new MiningFellaEntity(ModEntities.MINING_GOLEM, world)));

    public static void register() {
        Registry.register(Registries.ITEM_GROUP, ITEM_GROUP.getValue(), FabricItemGroup.builder()
                .displayName(Text.translatable("itemGroup.farming_fellas.name"))
                .icon(ModItems.FARMING_GOLEM_ITEM::getDefaultStack)
                .build()
        );

        ItemGroupEvents.modifyEntriesEvent(ITEM_GROUP).register(entries -> {
            entries.addAll(items.stream().sorted(Comparator.comparingInt(Item::getRawId)).map(Item::getDefaultStack).toList());
        });
    }

    public static <T extends Item> T register(String name, Item.Settings settings, Function<Item.Settings, T> factory) {
        T item = Registry.register(Registries.ITEM, FarmingFellasMain.id(name), factory.apply(settings));
        items.add(item);
        return item;
    }
}
