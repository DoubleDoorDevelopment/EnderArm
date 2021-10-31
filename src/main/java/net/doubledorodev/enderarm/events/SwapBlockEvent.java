package net.doubledorodev.enderarm.events;

import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
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

        Player player = event.player;
        Item enderArm = ItemRegistry.ENDER_ARM.get();

        // Needs to hold an arm to activate.
        if (player.isHolding(enderArm))
        {
            Level world = player.level;

            ItemStack mainHand = player.getMainHandItem();
            ItemStack offHand = player.getOffhandItem();
            // Handy way to do a primary hand biased item grab.
            // Will prevent the secondary held item from working regardless of
            // activation to keep control of the ability to turn on/off the main hand.
            ItemStack stackToUse = mainHand.getItem() == enderArm ? mainHand : offHand.getItem() == enderArm ? offHand : ItemStack.EMPTY;

            // Arm needs to be active to swap blocks.
            if (Utils.getEnabledState(stackToUse))
            {
                HitResult result = Utils.findCollidable(player);
                // Raytrace needs to hit a block.
                if (result.getType() == HitResult.Type.BLOCK)
                {
                    BlockHitResult blockTrace = (BlockHitResult) result;
                    BlockState stateAtTrace = world.getBlockState(blockTrace.getBlockPos());

                    // Stop blocks that shouldn't be looked through from being looked through.
                    // Defaults to bedrock as that's likely the worst offender. #TagsMakeItConfigurable!
                    // Hardcode out doors as they hard depend on door parts existing and cause dupes.
                    Tag<Block> dontReplaceBlocks = BlockTags.getAllTags().getTagOrEmpty(new ResourceLocation(Enderarm.MODID, "do_not_replace"));
                    if (dontReplaceBlocks.contains(stateAtTrace.getBlock()) || stateAtTrace.getBlock() instanceof DoorBlock)
                        return;

                    // Add any other players looking into the same block to the tracking list.
                    if (stateAtTrace.getBlock().equals(BlockRegistry.GHOST_BLOCK.get()))
                    {
                        GhostBlockEntity ghostBlockEntity = (GhostBlockEntity) world.getBlockEntity(blockTrace.getBlockPos());

                        if (ghostBlockEntity != null)
                        {
                            ghostBlockEntity.addPlayerLooking(player);
                            stackToUse.getOrCreateTagElement("handData").put("activeTile", NbtUtils.writeBlockPos(blockTrace.getBlockPos()));
                        }

                        // We don't need to set a block if one already exists, just jump out now even though the next check would fail the block entity check.
                        return;
                    }

                    // Make sure we aren't trying to convert replace a block entity or something already invisible.
                    if (!stateAtTrace.hasBlockEntity() && stateAtTrace.getRenderShape() != RenderShape.INVISIBLE)
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
                            stackToUse.getOrCreateTagElement("handData").put("activeTile", NbtUtils.writeBlockPos(blockTrace.getBlockPos()));
                        }
                    }
                }
            }
        }
    }
}
