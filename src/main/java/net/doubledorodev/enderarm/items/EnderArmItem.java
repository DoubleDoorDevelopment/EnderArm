package net.doubledorodev.enderarm.items;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import net.doubledorodev.enderarm.Enderarm;
import net.doubledorodev.enderarm.EnderarmConfig;
import net.doubledorodev.enderarm.Utils;

public class EnderArmItem extends Item
{
    public EnderArmItem(Properties properties)
    {
        super(properties);
    }

    @ParametersAreNonnullByDefault
    @Nonnull
    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand)
    {
        ItemStack usedStack = player.getItemInHand(hand);

        // State toggle for on/off feature.
        if (player.isCrouching())
        {
            // Make sure we can turn off arms that are outside of durability.
            if (Utils.getEnabledState(usedStack) ||
                    usedStack.getDamageValue() > EnderarmConfig.GENERAL.durabilityConsumedPerBlock.get() || !usedStack.isDamaged())
            {
                CompoundNBT toggleNBT = usedStack.getOrCreateTagElement("handData");

                toggleNBT.putBoolean("enabled", !Utils.getEnabledState(usedStack));

                return ActionResult.consume(usedStack);
            }
            else
            {
                player.displayClientMessage(new TranslationTextComponent("chat.enderarm.item.insufficient.durability"), true);
                return ActionResult.pass(usedStack);
            }
        }

        return ActionResult.pass(usedStack);
    }

    @ParametersAreNonnullByDefault
    @Override
    public boolean isFoil(ItemStack stack)
    {
        return Utils.getEnabledState(stack);
    }

    @ParametersAreNonnullByDefault
    @Override
    public boolean isRepairable(ItemStack stack)
    {
        ITag<Item> armActivatorBlocks = ItemTags.getAllTags().getTagOrEmpty(new ResourceLocation(Enderarm.MODID, "repairs_arm_durability"));
        return armActivatorBlocks.contains(stack.getItem()) || super.isRepairable(stack);
    }
}
