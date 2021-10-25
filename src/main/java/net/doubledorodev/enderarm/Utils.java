package net.doubledorodev.enderarm;

import java.util.Random;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

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
    public static HitResult findCollidable(Player player)
    {
        double reach = EnderarmConfig.GENERAL.armReach.get();
        float offset = 0;
        Vec3 fromVector = player.getEyePosition(offset);
        Vec3 viewModVector = player.getViewVector(offset);
        Vec3 toVector = fromVector.add(viewModVector.x * reach, viewModVector.y * reach, viewModVector.z * reach);

        return player.level.clip(new ClipContext(fromVector, toVector, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
    }

    /**
     * Helper method to create an AABB for bound checking players based off the
     * reach distance of the arm's effect.
     *
     * @param pos to center on.
     * @return AxisAlignedBB within arms reach.
     */
    public static AABB playerCheckAABB(BlockPos pos)
    {
        return new AABB(pos.getX() - EnderarmConfig.GENERAL.armReach.get(), pos.getY() - EnderarmConfig.GENERAL.armReach.get(), pos.getZ() - EnderarmConfig.GENERAL.armReach.get(),
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
            CompoundTag toggleNBT = stack.getOrCreateTagElement("handData");

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
    public static void damageEnabledArmItem(Player player, ItemStack stack, int damage)
    {
        if (Utils.getEnabledState(stack) && stack.getDamageValue() > damage)
            stack.hurtAndBreak(damage, player, (playerEntity) ->
                    playerEntity.broadcastBreakEvent(player.getUsedItemHand()));
        else
        {
            CompoundTag toggleNBT = stack.getOrCreateTagElement("handData");

            toggleNBT.putBoolean("enabled", false);
            player.displayClientMessage(new TranslatableComponent("chat.enderarm.item.insufficient.durability.use"), true);
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
    public static BlockState getNonNullStateFromGhost(BlockGetter world, BlockPos pos)
    {
        GhostBlockEntity blockEntity = (GhostBlockEntity) world.getBlockEntity(pos);

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
