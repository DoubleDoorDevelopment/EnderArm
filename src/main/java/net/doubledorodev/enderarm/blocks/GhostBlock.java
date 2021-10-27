package net.doubledorodev.enderarm.blocks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.IPlantable;

import net.doubledorodev.enderarm.Utils;
import net.doubledorodev.enderarm.items.ItemRegistry;

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
public class GhostBlock extends BaseEntityBlock
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
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult rayTraceResult)
    {
        BlockState stateFromGhost = Utils.getNonNullStateFromGhost(world, pos);
        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();
        //TODO TEST ON SEVER, Doesn't work.
        if (Utils.getEnabledState(mainHand) || Utils.getEnabledState(offHand))
            return InteractionResult.PASS;
        return stateFromGhost.getBlock().use(stateFromGhost, world, pos, player, hand, rayTraceResult);
    }

    @ParametersAreNonnullByDefault
    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext placeContext)
    {
        return false;
    }

    @ParametersAreNonnullByDefault
    @Nonnull
    @Override
    public VoxelShape getInteractionShape(BlockState state, BlockGetter world, BlockPos pos)
    {
        BlockState stateFromGhost = Utils.getNonNullStateFromGhost(world, pos);
        return stateFromGhost.getBlock().getInteractionShape(stateFromGhost, world, pos);
    }

    @ParametersAreNonnullByDefault
    @Override
    public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos)
    {
        BlockState stateFromGhost = Utils.getNonNullStateFromGhost(world, pos);
        if (stateFromGhost.is(Blocks.AIR))
            return true;
        return stateFromGhost.getBlock().canSurvive(stateFromGhost, world, pos);
    }

    @ParametersAreNonnullByDefault
    @Override
    public float getShadeBrightness(BlockState state, BlockGetter world, BlockPos pos)
    {
        return 1.0F;
    }

    /**
     * This handles the ability for players to reach through the block.
     * It requires a player to be holding an active arm to reach through otherwise
     * it will act as a solid object.
     */
    @ParametersAreNonnullByDefault
    @Nonnull
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext collisionContext)
    {
//        CollisionContext entityCollide = CollisionContext.of()
//        Entity entity = collisionContext.;

        //TODO: See if I can somehow get the item state?
        if (collisionContext.isHoldingItem(ItemRegistry.ENDER_ARM.get()))
            return Shapes.empty();
        else return Shapes.block();
//
//        if (entity instanceof Player)
//        {
//            collisionContext.isHoldingItem()
//            Player player = (Player) collisionContext.getEntity();
//            ItemStack mainHand = player.getMainHandItem();
//            ItemStack offHand = player.getOffhandItem();
//
//            if (Utils.getEnabledState(mainHand) || Utils.getEnabledState(offHand))
//                return VoxelShapes.empty();
//        }
//        return VoxelShapes.block();
    }

    @Override
    public RenderShape getRenderShape(BlockState p_49232_)
    {
        return super.getRenderShape(p_49232_);
    }

    @ParametersAreNonnullByDefault
    @Nonnull
    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext selectionContext)
    {
        BlockState stateFromGhost = Utils.getNonNullStateFromGhost(world, pos);
        return stateFromGhost.getBlock().getCollisionShape(stateFromGhost, world, pos, selectionContext);
    }

    @ParametersAreNonnullByDefault
    @Nonnull
    @Override
    public VoxelShape getVisualShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext selectionContext)
    {
        BlockState stateFromGhost = Utils.getNonNullStateFromGhost(world, pos);
        return stateFromGhost.getBlock().getVisualShape(stateFromGhost, world, pos, selectionContext);
    }

    @ParametersAreNonnullByDefault
    @Override
    public int getSignal(BlockState state, BlockGetter world, BlockPos pos, Direction direction)
    {
        BlockState stateFromGhost = Utils.getNonNullStateFromGhost(world, pos);
        return stateFromGhost.getBlock().getSignal(stateFromGhost, world, pos, direction);
    }

    // Redstone stuff.
    @ParametersAreNonnullByDefault
    @Override
    public int getDirectSignal(BlockState state, BlockGetter world, BlockPos pos, Direction direction)
    {
        BlockState stateFromGhost = Utils.getNonNullStateFromGhost(world, pos);
        return stateFromGhost.getBlock().getDirectSignal(stateFromGhost, world, pos, direction);
    }

    @Override
    public float getFriction(BlockState state, LevelReader world, BlockPos pos, @Nullable Entity entity)
    {
        BlockState stateFromGhost = Utils.getNonNullStateFromGhost(world, pos);
        return stateFromGhost.getBlock().getFriction(stateFromGhost, world, pos, entity);
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter world, BlockPos pos)
    {
        BlockState realState = world.getBlockState(pos);
        if (realState.getBlock() instanceof GhostBlock) {
            BlockState stateFromGhost = Utils.getNonNullStateFromGhost(world, pos);
            return stateFromGhost.getBlock().getLightEmission(stateFromGhost, world, pos);
        } else {
            // See https://discord.com/channels/176780432371744769/353436942387642379/903019802161975316
            return realState.getLightEmission(world, pos);
        }
    }

    @Override
    public boolean isLadder(BlockState state, LevelReader world, BlockPos pos, LivingEntity entity)
    {
        BlockState stateFromGhost = Utils.getNonNullStateFromGhost(world, pos);
        return stateFromGhost.getBlock().isLadder(stateFromGhost, world, pos, entity);
    }

    @Override
    public boolean makesOpenTrapdoorAboveClimbable(BlockState state, LevelReader world, BlockPos pos, BlockState trapdoorState)
    {
        BlockState stateFromGhost = Utils.getNonNullStateFromGhost(world, pos);
        return stateFromGhost.getBlock().makesOpenTrapdoorAboveClimbable(stateFromGhost, world, pos, trapdoorState);
    }

    @Override
    public ItemStack getPickBlock(BlockState state, HitResult target, BlockGetter world, BlockPos pos, Player player)
    {
        BlockState stateFromGhost = Utils.getNonNullStateFromGhost(world, pos);
        return stateFromGhost.getBlock().getPickBlock(stateFromGhost, target, world, pos, player);
    }

    @Override
    public boolean isFertile(BlockState state, BlockGetter world, BlockPos pos)
    {
        BlockState stateFromGhost = Utils.getNonNullStateFromGhost(world, pos);
        return stateFromGhost.getBlock().isFertile(stateFromGhost, world, pos);
    }

    @Override
    public boolean isConduitFrame(BlockState state, LevelReader world, BlockPos pos, BlockPos conduit)
    {
        BlockState stateFromGhost = Utils.getNonNullStateFromGhost(world, pos);
        if (stateFromGhost.is(Blocks.AIR))
            return true;
        return stateFromGhost.getBlock().isConduitFrame(stateFromGhost, world, pos, conduit);
    }

    @Override
    public boolean isPortalFrame(BlockState state, BlockGetter world, BlockPos pos)
    {
        BlockState stateFromGhost = Utils.getNonNullStateFromGhost(world, pos);
        if (stateFromGhost.is(Blocks.AIR))
            return true;
        return stateFromGhost.getBlock().isPortalFrame(stateFromGhost, world, pos);
    }

    @Override
    public float getEnchantPowerBonus(BlockState state, LevelReader world, BlockPos pos)
    {
        BlockState stateFromGhost = Utils.getNonNullStateFromGhost(world, pos);
        return stateFromGhost.getBlock().getEnchantPowerBonus(stateFromGhost, world, pos);
    }

    @Override
    public void onNeighborChange(BlockState state, LevelReader world, BlockPos pos, BlockPos neighbor)
    {
        BlockState stateFromGhost = Utils.getNonNullStateFromGhost(world, pos);
        stateFromGhost.getBlock().onNeighborChange(stateFromGhost, world, pos, neighbor);
    }

    @Override
    public int getFlammability(BlockState state, BlockGetter world, BlockPos pos, Direction direction)
    {
        BlockState stateFromGhost = Utils.getNonNullStateFromGhost(world, pos);
        return stateFromGhost.getBlock().getFlammability(stateFromGhost, world, pos, direction);
    }

    @Override
    public boolean isFlammable(BlockState state, BlockGetter world, BlockPos pos, Direction direction)
    {
        BlockState stateFromGhost = Utils.getNonNullStateFromGhost(world, pos);
        if (stateFromGhost.is(Blocks.AIR) || !isFireSource(stateFromGhost, (LevelReader) world, pos, direction))
            return true;
        return stateFromGhost.getBlock().isFlammable(stateFromGhost, world, pos, direction);
    }

    @Override
    public void catchFire(BlockState state, Level world, BlockPos pos, @Nullable Direction direction, @Nullable LivingEntity igniter)
    {
        BlockState stateFromGhost = Utils.getNonNullStateFromGhost(world, pos);
        stateFromGhost.getBlock().catchFire(stateFromGhost, world, pos, direction, igniter);
    }

    // Fire Stuff
    @Override
    public int getFireSpreadSpeed(BlockState state, BlockGetter world, BlockPos pos, Direction direction)
    {
        BlockState stateFromGhost = Utils.getNonNullStateFromGhost(world, pos);
        return stateFromGhost.getBlock().getFlammability(stateFromGhost, world, pos, direction);
    }

    @Override
    public boolean isFireSource(BlockState state, LevelReader world, BlockPos pos, Direction direction)
    {
        BlockState stateFromGhost = Utils.getNonNullStateFromGhost(world, pos);
        if (stateFromGhost.is(Blocks.AIR))
            return true;
        return stateFromGhost.getBlock().isFireSource(stateFromGhost, world, pos, direction);
    }

    @Override
    public boolean isScaffolding(BlockState state, LevelReader world, BlockPos pos, LivingEntity entity)
    {
        BlockState stateFromGhost = Utils.getNonNullStateFromGhost(world, pos);
        return stateFromGhost.getBlock().isScaffolding(stateFromGhost, world, pos, entity);
    }

    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter world, BlockPos pos, @Nullable Direction direction)
    {
        BlockState stateFromGhost = Utils.getNonNullStateFromGhost(world, pos);
        return stateFromGhost.getBlock().canConnectRedstone(stateFromGhost, world, pos, direction);
    }

    @ParametersAreNonnullByDefault
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new GhostBlockEntity(pos, state);
    }

    @Nullable
    @ParametersAreNonnullByDefault
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType)
    {
        return createTickerHelper(blockEntityType, BlockRegistry.GHOST_BLOCK_ENTITY.get(), GhostBlockEntity::tick);
    }

    @ParametersAreNonnullByDefault
    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter world, BlockPos pos)
    {
        BlockState stateFromGhost = Utils.getNonNullStateFromGhost(world, pos);
        return stateFromGhost.getBlock().propagatesSkylightDown(stateFromGhost, world, pos);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> stateBuilder)
    {
        stateBuilder.add(ENABLED);
    }

    // Misc crap.
    @ParametersAreNonnullByDefault
    @Override
    public boolean canSustainPlant(BlockState state, BlockGetter world, BlockPos pos, Direction facing, IPlantable plantable)
    {
        BlockState stateFromGhost = Utils.getNonNullStateFromGhost(world, pos);
        if (stateFromGhost.is(Blocks.AIR))
            return true;
        return stateFromGhost.getBlock().canSustainPlant(stateFromGhost, world, pos, facing, plantable);
    }
}
