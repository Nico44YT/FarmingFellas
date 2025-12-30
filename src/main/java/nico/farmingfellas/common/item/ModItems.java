package nico.farmingfellas.common.item;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import nico.farmingfellas.FarmingFellasMain;
import nico.farmingfellas.common.item.zoning.ZoningMapItem;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ModItems {

    public static final Item BLANK_ZONING_MAP = register("blank_zoning_map", new Item.Settings().maxCount(1), ZoningMapItem::new);

    public static void register() {
        //Registry.register(Registries.ITEM_GROUP, ITEM_GROUP.getValue(), FabricItemGroup.builder()
        //        .displayName(Text.translatable("itemGroup.dark_arts.name"))
        //        .icon(ModItems.RUBY_INFUSED_NETHERITE::getDefaultStack)
        //        .build()
        //);
    }

    public static List<Item> items;
    public static <T extends Item> T register(String name, Item.Settings settings, Function<Item.Settings, T> factory) {
        T item = Registry.register(Registries.ITEM, FarmingFellasMain.id(name), factory.apply(settings));
        if (items == null) items = new ArrayList<>();
        items.add(item);
        return item;
    }
}
