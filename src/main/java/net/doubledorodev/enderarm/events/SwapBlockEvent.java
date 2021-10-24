package net.doubledorodev.enderarm.events;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import net.doubledorodev.enderarm.Enderarm;
import net.doubledorodev.enderarm.EnderarmConfig;
import net.doubledorodev.enderarm.Utils;
import net.doubledorodev.enderarm.blocks.BlockRegistry;
import net.doubledorodev.enderarm.blocks.GhostBlockEntity;
import net.doubledorodev.enderarm.items.ItemRegistry;

public class SwapBlockEvent
{
    @SubscribeEvent
    public static void swapLookedAtBlock(TickEvent.PlayerTickEvent event)
    {
        if (event.player.level.isClientSide())
            return;

        PlayerEntity player = event.player;
        Item enderArm = ItemRegistry.ENDER_ARM.get();

        // Needs to hold an arm to activate.
        if (player.isHolding(enderArm))
        {
            World world = player.level;

            ItemStack mainHand = player.getMainHandItem();
            ItemStack offHand = player.getOffhandItem();
            // Handy way to do a primary hand biased item grab.
            // Will prevent the secondary held item from working regardless of
            // activation to keep control of the ability to turn on/off the main hand.
            ItemStack stackToUse = mainHand.getItem() == enderArm ? mainHand : offHand.getItem() == enderArm ? offHand : ItemStack.EMPTY;

            // Arm needs to be active to swap blocks.
            if (Utils.getEnabledState(stackToUse))
            {
                RayTraceResult result = Utils.findCollidable(player);
                // Raytrace needs to hit a block.
                if (result.getType() == RayTraceResult.Type.BLOCK)
                {
                    BlockRayTraceResult blockTrace = (BlockRayTraceResult) result;
                    BlockState stateAtTrace = world.getBlockState(blockTrace.getBlockPos());

                    // Stop blocks that shouldn't be looked through from being looked through.
                    // Defaults to bedrock as that's likely the worst offender. #TagsMakeItConfigurable!
                    ITag<Block> dontReplaceBlocks = BlockTags.getAllTags().getTagOrEmpty(new ResourceLocation(Enderarm.MODID, "do_not_replace"));
                    if (dontReplaceBlocks.contains(stateAtTrace.getBlock()))
                        return;

                    // Add any other players looking into the same block to the tracking list.
                    if (stateAtTrace.getBlock().is(BlockRegistry.GHOST_BLOCK.get()))
                    {
                        GhostBlockEntity ghostBlockEntity = (GhostBlockEntity) world.getBlockEntity(blockTrace.getBlockPos());

                        if (ghostBlockEntity != null)
                        {
                            ghostBlockEntity.addPlayerLooking(player);
                            stackToUse.getOrCreateTagElement("handData").put("activeTile", NBTUtil.writeBlockPos(blockTrace.getBlockPos()));
                        }

                        // We don't need to set a block if one already exists, just jump out now even though the next check would fail the block entity check.
                        return;
                    }

                    // Make sure we aren't trying to convert replace a block entity or something already invisible.
                    if (!stateAtTrace.hasTileEntity() && stateAtTrace.getRenderShape() != BlockRenderType.INVISIBLE)
                    {
                        // Set the block.
                        world.setBlock(blockTrace.getBlockPos(), BlockRegistry.GHOST_BLOCK.get().defaultBlockState(), 2);
                        Utils.damageEnabledArmItem(player, stackToUse, EnderarmConfig.GENERAL.durabilityConsumedPerBlock.get());

                        // Must get the block entity after it's placed or it won't exist yet.
                        GhostBlockEntity ghostBlockEntity = (GhostBlockEntity) world.getBlockEntity(blockTrace.getBlockPos());
                        // Update the underlying tile data for the first look.
                        if (ghostBlockEntity != null)
                        {
                            ghostBlockEntity.setParentBlock(stateAtTrace);
                            ghostBlockEntity.addPlayerLooking(player);
                            stackToUse.getOrCreateTagElement("handData").put("activeTile", NBTUtil.writeBlockPos(blockTrace.getBlockPos()));
                        }
                    }
                }
            }
        }
    }
}
