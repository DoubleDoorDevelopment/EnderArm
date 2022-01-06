package net.doubledorodev.enderarm;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;

import net.doubledorodev.enderarm.blocks.BlockRegistry;
import net.doubledorodev.enderarm.blocks.GhostBlockEntity;
import net.doubledorodev.enderarm.items.ItemRegistry;

public class Utils
{
    private static final Random random = new Random();

    /**
     * Borrowed and modified from vanilla pickBlock raytrace for finding blocks that will
     * cause a collision trigger based off the players current view.
     *
     * @param player player to start the ray at.
     * @return RayTraceResult of the trace.
     */
    public static RayTraceResult findCollidable(PlayerEntity player)
    {
        double reach = EnderarmConfig.GENERAL.armReach.get();
        float offset = 0;
        Vector3d fromVector = player.getEyePosition(offset);
        Vector3d viewModVector = player.getViewVector(offset);
        Vector3d toVector = fromVector.add(viewModVector.x * reach, viewModVector.y * reach, viewModVector.z * reach);

        return player.level.clip(new RayTraceContext(fromVector, toVector, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, player));
    }

    /**
     * Helper method to create an AABB for bound checking players based off the
     * reach distance of the arm's effect.
     *
     * @param pos to center on.
     * @return AxisAlignedBB within arms reach.
     */
    public static AxisAlignedBB playerCheckAABB(BlockPos pos)
    {
        return new AxisAlignedBB(pos.getX() - EnderarmConfig.GENERAL.armReach.get(), pos.getY() - EnderarmConfig.GENERAL.armReach.get(), pos.getZ() - EnderarmConfig.GENERAL.armReach.get(),
                pos.getX() + EnderarmConfig.GENERAL.armReach.get() + 1, pos.getY() + EnderarmConfig.GENERAL.armReach.get() + 1, pos.getZ() + EnderarmConfig.GENERAL.armReach.get() + 1);
    }

    /**
     * Helper method to get the on/off state of the arm being used from the stack NBT.
     * Checks only for arms and will apply the tag if somehow it's missing.
     *
     * @param stack to retrieve the NBT from.
     * @return boolean active state.
     */
    public static boolean getEnabledState(ItemStack stack)
    {
        if (stack.getItem() == ItemRegistry.ENDER_ARM.get())
        {
            CompoundNBT toggleNBT = stack.getOrCreateTagElement("handData");

            return toggleNBT.getBoolean("enabled");
        }
        else return false;
    }


    /**
     * Helper method to handle applying damage to arms.
     * Requires the arm to be active to apply the damage.
     *
     * @param player that's doing the action.
     * @param stack  being damaged.
     * @param damage to apply.
     */
    public static void damageEnabledArmItem(PlayerEntity player, ItemStack stack, int damage)
    {
        if (Utils.getEnabledState(stack) && stack.getDamageValue() > damage)
            stack.hurtAndBreak(damage, player, (playerEntity) ->
                    playerEntity.broadcastBreakEvent(player.getUsedItemHand()));
        else
        {
            CompoundNBT toggleNBT = stack.getOrCreateTagElement("handData");

            toggleNBT.putBoolean("enabled", false);
            player.displayClientMessage(new TranslationTextComponent("chat.enderarm.item.insufficient.durability.use"), true);
        }
    }

    /**
     * Helper method to retrieve the data from the BlockEntity for what state the
     * ghost block is currently holding. As this is used in some places where info
     * is checked before the BE data is written it defaults to air blocks to allow
     * special handling.
     *
     * @param world server world the block is in.
     * @param pos   location of the block to get the block entity from.
     * @return the state store within the block entity.
     */
    public static BlockState getNonNullStateFromGhost(IBlockReader world, BlockPos pos)
    {
        Block blockAtPos = world.getBlockState(pos).getBlock();
        GhostBlockEntity blockEntity = null;

        // BE verification to fix https://github.com/DoubleDoorDevelopment/EnderArm/pull/8
        if (blockAtPos == BlockRegistry.GHOST_BLOCK.get())
            blockEntity = (GhostBlockEntity) world.getBlockEntity(pos);
        else return world.getBlockState(pos);

        if (blockEntity != null && blockEntity.getParentBlock() != null)
            return blockEntity.getParentBlock();
        else return Blocks.AIR.defaultBlockState();
    }

    /**
     * Helper method to give a random +/- for vector related things.
     * Specifically particle spawning.
     *
     * @return a double that is either positive or negative.
     */
    public static double plusMinusRandD()
    {
        return random.nextBoolean() ? random.nextDouble() : -random.nextDouble();
    }
}
