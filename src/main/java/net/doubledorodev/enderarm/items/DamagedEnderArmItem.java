package net.doubledorodev.enderarm.items;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;

import net.doubledorodev.enderarm.Enderarm;
import net.doubledorodev.enderarm.EnderarmConfig;

public class DamagedEnderArmItem extends Item
{
    public DamagedEnderArmItem(Properties properties)
    {
        super(properties);
    }

    @ParametersAreNonnullByDefault
    @Nonnull
    @Override
    public InteractionResult useOn(UseOnContext useContext)
    {
        ItemStack stackUsed = useContext.getItemInHand();
        Player player = useContext.getPlayer();

        Tag<Block> armActivatorBlocks = BlockTags.getAllTags().getTagOrEmpty(new ResourceLocation(Enderarm.MODID, "activates_broken_arm"));
        // Activate damaged arms into working proper arms.
        if (player != null)
            if (player.level.getBlockState(useContext.getClickedPos()).is(armActivatorBlocks))
            {
                ItemStack activatedArm = new ItemStack(ItemRegistry.ENDER_ARM.get());

                if (EnderarmConfig.GENERAL.randomActivationDurability.get())
                    activatedArm.setDamageValue((int) (EnderarmConfig.GENERAL.armDurability.get() * player.level.getRandom().nextDouble()));
                stackUsed.setCount(stackUsed.getCount() - 1);
                player.addItem(activatedArm);

                return InteractionResult.CONSUME;
            }
            else
            {
                player.displayClientMessage(new TranslatableComponent("chat.enderarm.item.broken.activate.tip"), true);
            }
        return InteractionResult.PASS;
    }
}
