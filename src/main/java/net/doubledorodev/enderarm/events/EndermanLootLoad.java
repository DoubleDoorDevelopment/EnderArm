package net.doubledorodev.enderarm.events;

import net.minecraft.loot.BinomialRange;
import net.minecraft.loot.ConstantRange;
import net.minecraft.loot.ItemLootEntry;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.conditions.KilledByPlayer;
import net.minecraft.loot.functions.SetCount;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import net.doubledorodev.enderarm.EnderarmConfig;
import net.doubledorodev.enderarm.items.ItemRegistry;

public class EndermanLootLoad
{
    @SubscribeEvent
    public static void modifyEnderDrops(LootTableLoadEvent event)
    {
        ResourceLocation enderTableLoc = new ResourceLocation("minecraft:entities/enderman");
        if (!EnderarmConfig.GENERAL.disableLootEdit.get() && event.getTable().getLootTableId().equals(enderTableLoc))
        {
            LootPool.Builder armPool = new LootPool.Builder();
            armPool.setRolls(BinomialRange.binomial(1, EnderarmConfig.GENERAL.brokenArmChance.get().floatValue()))
                    .when(KilledByPlayer.killedByPlayer());
            if (EnderarmConfig.GENERAL.dropUseableArms.get())
                armPool.add(ItemLootEntry.lootTableItem(ItemRegistry.ENDER_ARM.get()).apply(SetCount.setCount(ConstantRange.exactly(1))));
            else
                armPool.add(ItemLootEntry.lootTableItem(ItemRegistry.DAMAGED_ENDER_ARM.get()).apply(SetCount.setCount(ConstantRange.exactly(1))));

            event.getTable().addPool(armPool.build());
        }
    }
}
