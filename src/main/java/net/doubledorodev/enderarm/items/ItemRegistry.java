package net.doubledorodev.enderarm.items;

import java.util.Locale;
import java.util.function.Supplier;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.Rarity;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import net.doubledorodev.enderarm.Enderarm;

public class ItemRegistry
{
    public static final DeferredRegister<Item> ITEMS_DEFERRED = DeferredRegister.create(ForgeRegistries.ITEMS, Enderarm.MODID);

    //Items
    public static final RegistryObject<Item> ENDER_ARM = register("ender_arm", () -> new EnderArmItem(new Item.Properties().durability(5000).tab(ItemGroup.TAB_TOOLS).rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> DAMAGED_ENDER_ARM = register("damaged_ender_arm", () -> new DamagedEnderArmItem(new Item.Properties().tab(ItemGroup.TAB_TOOLS).rarity(Rarity.RARE)));

    private static <T extends Item> RegistryObject<T> register(String name, Supplier<T> itemSupplier)
    {
        final String actualName = name.toLowerCase(Locale.ROOT);
        return ITEMS_DEFERRED.register(actualName, itemSupplier);
    }

}
