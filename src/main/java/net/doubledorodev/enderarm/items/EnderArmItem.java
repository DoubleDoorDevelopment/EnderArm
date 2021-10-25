package net.doubledorodev.enderarm.items;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

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
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand)
    {
        ItemStack usedStack = player.getItemInHand(hand);

        // State toggle for on/off feature.
        if (player.isCrouching())
        {
            // Make sure we can turn off arms that are outside of durability.
            if (Utils.getEnabledState(usedStack) ||
                    usedStack.getDamageValue() > EnderarmConfig.GENERAL.durabilityConsumedPerBlock.get() || !usedStack.isDamaged())
            {
                CompoundTag toggleNBT = usedStack.getOrCreateTagElement("handData");

                toggleNBT.putBoolean("enabled", !Utils.getEnabledState(usedStack));

                return InteractionResultHolder.consume(usedStack);
            }
            else
            {
                player.displayClientMessage(new TranslatableComponent("chat.enderarm.item.insufficient.durability"), true);
                return InteractionResultHolder.pass(usedStack);
            }
        }

        return InteractionResultHolder.pass(usedStack);
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
        Tag<Item> armActivatorBlocks = ItemTags.getAllTags().getTagOrEmpty(new ResourceLocation(Enderarm.MODID, "repairs_arm_durability"));
        return armActivatorBlocks.contains(stack.getItem()) || super.isRepairable(stack);
    }
}
