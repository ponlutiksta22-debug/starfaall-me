package dev.starfall.starfallnuke.registry;

import dev.starfall.starfallnuke.StarfallNuke;
import dev.starfall.starfallnuke.item.StarfallCoreItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, StarfallNuke.MOD_ID);

    public static final RegistryObject<Item> STARFALL_CORE = ITEMS.register(
            "starfall_core",
            () -> new StarfallCoreItem(new Item.Properties().stacksTo(16))
    );
}
