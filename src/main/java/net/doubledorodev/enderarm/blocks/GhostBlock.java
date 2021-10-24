package net.doubledorodev.enderarm.blocks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;

import net.doubledorodev.enderarm.Utils;

/**
 * This class is essentially a fake block.
 * It's built to take in any data that could possibly exist on a block and
 * convert it into the replaced block states information when possible. This
 * allows the block to act, interact and be like it's original counterpart to
 * keep the illusion of the block being "reached" through.
 *
 * Due to some instances where the BlockEntity data can't be written in time for
 * the lookups to happen a default is added by checking for an Block.AIR return
 * from the state lookup.
 */
public class GhostBlock extends Block
{
    public static final BooleanProperty ENABLED = BlockStateProperties.ENABLED;

    // TODO: This whole class will not correctly update the stored state in the TE if anything is changed by a method. Can this be fixed?
    public GhostBlock(Properties properties)
    {
        super(properties);
        registerDefaultState(getStateDefinition().any().setValue(ENABLED, false));
    }

    /**
     * This should allow users to interact with the base block as if the block
     * was never replaced. Things like crafting tables will still do as they
     * should when someone is looking through it.
     */
    @ParametersAreNonnullByDefault
    @Nonnull
    @Override
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult rayTraceResult)
    {
        BlockState stateFromGhost = Utils.getNonNullStateFromGhost(world, pos);
        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();
        //TODO TEST ON SEVER, Doesn't work.
        if (Utils.getEnabledState(mainHand) || Utils.getEnabledState(offHand))
            return ActionResultType.PASS;
        return stateFromGhost.getBlock().use(stateFromGhost, world, pos, player, hand, rayTraceResult);
    }

    @ParametersAreNonnullByDefault
    @Override
    public boolean canBeReplaced(BlockState state, BlockItemUseContext useContext)
    {
        return false;
    }

    @ParametersAreNonnullByDefault
    @Nonnull
    @Override
    public VoxelShape getInteractionShape(BlockState state, IBlockReader world, BlockPos pos)
    {
        BlockState stateFromGhost = Utils.getNonNullStateFromGhost(world, pos);
        return stateFromGhost.getBlock().getInteractionShape(stateFromGhost, world, pos);
    }

    @ParametersAreNonnullByDefault
    @Override
    public boolean canSurvive(BlockState state, IWorldReader world, BlockPos pos)
    {
        BlockState stateFromGhost = Utils.getNonNullStateFromGhost(world, pos);
        if (stateFromGhost.is(Blocks.AIR.getBlock()))
            return true;
        return stateFromGhost.getBlock().canSurvive(stateFromGhost, world, pos);
    }

    @ParametersAreNonnullByDefault
    @Override
    public float getShadeBrightness(BlockState state, IBlockReader world, BlockPos pos)
    {
        return 1.0F;
    }

    /**
     * This handles the ability for players to reach through the block.
     * It requires a player to be holding an active arm to reach through otherwise
     * it will act as a soild object.
     */
    @ParametersAreNonnullByDefault
    @Nonnull
    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext selectionContext)
    {
        Entity entity = selectionContext.getEntity();

        if (entity instanceof PlayerEntity)
        {
            PlayerEntity player = (PlayerEntity) selectionContext.getEntity();
            ItemStack mainHand = player.getMainHandItem();
            ItemStack offHand = player.getOffhandItem();

            if (Utils.getEnabledState(mainHand) || Utils.getEnabledState(offHand))
                return VoxelShapes.empty();
        }
        return VoxelShapes.block();
    }

    @ParametersAreNonnullByDefault
    @Nonnull
    @Override
    public VoxelShape getCollisionShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext selectionContext)
    {
        BlockState stateFromGhost = Utils.getNonNullStateFromGhost(world, pos);
        return stateFromGhost.getBlock().getCollisionShape(stateFromGhost, world, pos, selectionContext);
    }

    @ParametersAreNonnullByDefault
    @Nonnull
    @Override
    public VoxelShape getVisualShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext selectionContext)
    {
        BlockState stateFromGhost = Utils.getNonNullStateFromGhost(world, pos);
        return stateFromGhost.getBlock().getVisualShape(stateFromGhost, world, pos, selectionContext);
    }

    @ParametersAreNonnullByDefault
    @Override
    public int getSignal(BlockState state, IBlockReader world, BlockPos pos, Direction direction)
    {
        BlockState stateFromGhost = Utils.getNonNullStateFromGhost(world, pos);
        return stateFromGhost.getBlock().getSignal(stateFromGhost, world, pos, direction);
    }

    // Redstone stuff.
    @ParametersAreNonnullByDefault
    @Override
    public int getDirectSignal(BlockState state, IBlockReader world, BlockPos pos, Direction direction)
    {
        BlockState stateFromGhost = Utils.getNonNullStateFromGhost(world, pos);
        return stateFromGhost.getBlock().getDirectSignal(stateFromGhost, world, pos, direction);
    }

    @Override
    public float getSlipperiness(BlockState state, IWorldReader world, BlockPos pos, @Nullable Entity entity)
    {
        BlockState stateFromGhost = Utils.getNonNullStateFromGhost(world, pos);
        return stateFromGhost.getBlock().getSlipperiness(stateFromGhost, world, pos, entity);
    }

    @Override
    public int getLightValue(BlockState state, IBlockReader world, BlockPos pos)
    {
        BlockState stateFromGhost = Utils.getNonNullStateFromGhost(world, pos);
        return stateFromGhost.getBlock().getLightValue(stateFromGhost, world, pos);
    }

    @Override
    public boolean isLadder(BlockState state, IWorldReader world, BlockPos pos, LivingEntity entity)
    {
        BlockState stateFromGhost = Utils.getNonNullStateFromGhost(world, pos);
        return stateFromGhost.getBlock().isLadder(stateFromGhost, world, pos, entity);
    }

    @Override
    public boolean makesOpenTrapdoorAboveClimbable(BlockState state, IWorldReader world, BlockPos pos, BlockState trapdoorState)
    {
        BlockState stateFromGhost = Utils.getNonNullStateFromGhost(world, pos);
        return stateFromGhost.getBlock().makesOpenTrapdoorAboveClimbable(stateFromGhost, world, pos, trapdoorState);
    }

    // Tile/Block entity stuff.
    @Override
    public boolean hasTileEntity(BlockState state)
    {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world)
    {
        return new GhostBlockEntity();
    }

    @Override
    public boolean canConnectRedstone(BlockState state, IBlockReader world, BlockPos pos, @Nullable Direction direction)
    {
        BlockState stateFromGhost = Utils.getNonNullStateFromGhost(world, pos);
        return stateFromGhost.getBlock().canConnectRedstone(stateFromGhost, world, pos, direction);
    }

    @Override
    public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player)
    {
        BlockState stateFromGhost = Utils.getNonNullStateFromGhost(world, pos);
        return stateFromGhost.getBlock().getPickBlock(stateFromGhost, target, world, pos, player);
    }

    @Override
    public boolean isFertile(BlockState state, IBlockReader world, BlockPos pos)
    {
        BlockState stateFromGhost = Utils.getNonNullStateFromGhost(world, pos);
        return stateFromGhost.getBlock().isFertile(stateFromGhost, world, pos);
    }

    @Override
    public boolean isConduitFrame(BlockState state, IWorldReader world, BlockPos pos, BlockPos conduit)
    {
        BlockState stateFromGhost = Utils.getNonNullStateFromGhost(world, pos);
        if (stateFromGhost.is(Blocks.AIR.getBlock()))
            return true;
        return stateFromGhost.getBlock().isConduitFrame(stateFromGhost, world, pos, conduit);
    }

    @Override
    public boolean isPortalFrame(BlockState state, IBlockReader world, BlockPos pos)
    {
        BlockState stateFromGhost = Utils.getNonNullStateFromGhost(world, pos);
        if (stateFromGhost.is(Blocks.AIR.getBlock()))
            return true;
        return stateFromGhost.getBlock().isPortalFrame(stateFromGhost, world, pos);
    }

    @Override
    public float getEnchantPowerBonus(BlockState state, IWorldReader world, BlockPos pos)
    {
        BlockState stateFromGhost = Utils.getNonNullStateFromGhost(world, pos);
        return stateFromGhost.getBlock().getEnchantPowerBonus(stateFromGhost, world, pos);
    }

    @Override
    public void onNeighborChange(BlockState state, IWorldReader world, BlockPos pos, BlockPos neighbor)
    {
        BlockState stateFromGhost = Utils.getNonNullStateFromGhost(world, pos);
        stateFromGhost.getBlock().onNeighborChange(stateFromGhost, world, pos, neighbor);
    }

    @Override
    public int getFlammability(BlockState state, IBlockReader world, BlockPos pos, Direction direction)
    {
        BlockState stateFromGhost = Utils.getNonNullStateFromGhost(world, pos);
        return stateFromGhost.getBlock().getFlammability(stateFromGhost, world, pos, direction);
    }

    @Override
    public boolean isFlammable(BlockState state, IBlockReader world, BlockPos pos, Direction direction)
    {
        BlockState stateFromGhost = Utils.getNonNullStateFromGhost(world, pos);
        if (stateFromGhost.is(Blocks.AIR.getBlock()) || !isFireSource(stateFromGhost, (IWorldReader) world, pos, direction))
            return true;
        return stateFromGhost.getBlock().isFlammable(stateFromGhost, world, pos, direction);
    }

    @Override
    public void catchFire(BlockState state, World world, BlockPos pos, @Nullable Direction direction, @Nullable LivingEntity igniter)
    {
        BlockState stateFromGhost = Utils.getNonNullStateFromGhost(world, pos);
        stateFromGhost.getBlock().catchFire(stateFromGhost, world, pos, direction, igniter);
    }

    // Fire Stuff
    @Override
    public int getFireSpreadSpeed(BlockState state, IBlockReader world, BlockPos pos, Direction direction)
    {
        BlockState stateFromGhost = Utils.getNonNullStateFromGhost(world, pos);
        return stateFromGhost.getBlock().getFlammability(stateFromGhost, world, pos, direction);
    }

    @Override
    public boolean isFireSource(BlockState state, IWorldReader world, BlockPos pos, Direction direction)
    {
        BlockState stateFromGhost = Utils.getNonNullStateFromGhost(world, pos);
        if (stateFromGhost.is(Blocks.AIR.getBlock()))
            return true;
        return stateFromGhost.getBlock().isFireSource(stateFromGhost, world, pos, direction);
    }

    @Override
    public boolean isScaffolding(BlockState state, IWorldReader world, BlockPos pos, LivingEntity entity)
    {
        BlockState stateFromGhost = Utils.getNonNullStateFromGhost(world, pos);
        return stateFromGhost.getBlock().isScaffolding(stateFromGhost, world, pos, entity);
    }

    @ParametersAreNonnullByDefault
    @Override
    public boolean propagatesSkylightDown(BlockState state, IBlockReader world, BlockPos pos)
    {
        BlockState stateFromGhost = Utils.getNonNullStateFromGhost(world, pos);
        return stateFromGhost.getBlock().propagatesSkylightDown(stateFromGhost, world, pos);
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> stateBuilder)
    {
        stateBuilder.add(ENABLED);
    }

    // Misc crap.
    @ParametersAreNonnullByDefault
    @Override
    public boolean canSustainPlant(BlockState state, IBlockReader world, BlockPos pos, Direction facing, IPlantable plantable)
    {
        BlockState stateFromGhost = Utils.getNonNullStateFromGhost(world, pos);
        if (stateFromGhost.is(Blocks.AIR.getBlock()))
            return true;
        return stateFromGhost.getBlock().canSustainPlant(stateFromGhost, world, pos, facing, plantable);
    }
}
