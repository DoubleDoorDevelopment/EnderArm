package net.doubledorodev.enderarm.items;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;

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
    public ActionResultType useOn(ItemUseContext useContext)
    {
        ItemStack stackUsed = useContext.getItemInHand();
        PlayerEntity player = useContext.getPlayer();

        ITag<Block> armActivatorBlocks = BlockTags.getAllTags().getTagOrEmpty(new ResourceLocation(Enderarm.MODID, "activates_broken_arm"));
        // Activate damaged arms into working proper arms.
        if (player != null)
            if (player.level.getBlockState(useContext.getClickedPos()).is(armActivatorBlocks))
            {
                ItemStack activatedArm = new ItemStack(ItemRegistry.ENDER_ARM.get());

                if (EnderarmConfig.GENERAL.randomActivationDurability.get())
                    activatedArm.setDamageValue((int) (EnderarmConfig.GENERAL.armDurability.get() * player.level.getRandom().nextDouble()));
                stackUsed.setCount(0);
                player.addItem(activatedArm);

                return ActionResultType.CONSUME;
            }
            else
            {
                player.displayClientMessage(new TranslationTextComponent("chat.enderarm.item.broken.activate.tip"), true);
            }
        return ActionResultType.PASS;
    }
}
