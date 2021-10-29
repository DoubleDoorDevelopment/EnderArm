package net.doubledorodev.enderarm.events;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemKilledByPlayerCondition;
import net.minecraft.world.level.storage.loot.providers.number.BinomialDistributionGenerator;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
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
            armPool.setRolls(BinomialDistributionGenerator.binomial(1, EnderarmConfig.GENERAL.brokenArmChance.get().floatValue()))
                    .when(LootItemKilledByPlayerCondition.killedByPlayer());
            if (EnderarmConfig.GENERAL.dropUseableArms.get())
                armPool.add(LootItem.lootTableItem(ItemRegistry.ENDER_ARM.get()).apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 1))));
            else
                armPool.add(LootItem.lootTableItem(ItemRegistry.DAMAGED_ENDER_ARM.get()).apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 1))));

            event.getTable().addPool(armPool.build());
        }
    }
}
