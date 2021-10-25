package net.doubledorodev.enderarm.blocks;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import net.doubledorodev.enderarm.Utils;

public class GhostBlockEntity extends BlockEntity
{
    BlockState parentBlock = Blocks.AIR.defaultBlockState();
    ArrayList<UUID> lookingPlayers = new ArrayList<>();

    public static void tick(Level level, BlockPos pos, BlockState state, GhostBlockEntity ghostBlockEntity)
    {
        if (level != null && !level.isClientSide())
        {
            List<Player> playersInRange = level.getEntitiesOfClass(Player.class, Utils.playerCheckAABB(pos));

            // Loop over all the players that can be in range of a ghost block, Skips extra checks if a player is removed at any point.
            for (Player player : playersInRange)
            {
                UUID playerID = player.getUUID();
                ItemStack mainHand = player.getMainHandItem();
                ItemStack offHand = player.getOffhandItem();

                // Check hands first for disabled arms. Both need to fail, method checks for valid arm item.
                if (!Utils.getEnabledState(mainHand) && !Utils.getEnabledState(offHand))
                {
                    ghostBlockEntity.lookingPlayers.remove(playerID);
                    continue;
                }

                CompoundTag mainNBT = mainHand.getTagElement("handData");
                CompoundTag offhandNBT = offHand.getTagElement("handData");

                // Next check for valid block links on the item NBT.
                if (mainNBT != null && !NbtUtils.readBlockPos(mainNBT.getCompound("activeTile")).equals(pos) ||
                        offhandNBT != null && !NbtUtils.readBlockPos(offhandNBT.getCompound("activeTile")).equals(pos))
                {
                    ghostBlockEntity.lookingPlayers.remove(playerID);
                    continue;
                }

                // Finally we throw a ray to be extra sure they didn't look off real quick and somehow mange to keep the block down.
                HitResult result = Utils.findCollidable(player);

                if (result.getType() == HitResult.Type.BLOCK)
                {
                    BlockHitResult blockTrace = (BlockHitResult) result;
                    BlockState stateAtTrace = level.getBlockState(blockTrace.getBlockPos());

                    if (stateAtTrace.getBlock() != BlockRegistry.GHOST_BLOCK.get())
                        ghostBlockEntity.lookingPlayers.remove(playerID);
                }
                else ghostBlockEntity.lookingPlayers.remove(playerID);
            }

            // Make sure to clear the block if nobody is in range or looking at it.
            if (ghostBlockEntity.lookingPlayers.size() == 0 || playersInRange.size() == 0)
            {
                level.setBlock(pos, ghostBlockEntity.parentBlock, 2);
            }
        }
    }

//    public GhostBlockEntity()
//    {
//        this(BlockRegistry.GHOST_BLOCK_ENTITY.get());
//    }

    public GhostBlockEntity(BlockPos pos, BlockState state)
    {
        super(BlockRegistry.GHOST_BLOCK_ENTITY.get(), pos, state);
    }

    public void addPlayerLooking(Player player)
    {
        if (!lookingPlayers.contains(player.getUUID()))
            lookingPlayers.add(player.getUUID());
    }

    public void removePlayerLooking(Player player)
    {
        lookingPlayers.remove(player.getUUID());
    }

    @Override
    @ParametersAreNonnullByDefault
    public void load(CompoundTag compoundNBT)
    {
        super.load(compoundNBT);

        parentBlock = NbtUtils.readBlockState(compoundNBT.getCompound("parentBlock"));
    }

    public ArrayList<UUID> getLookingPlayers()
    {
        return lookingPlayers;
    }

    public BlockState getParentBlock()
    {
        return parentBlock;
    }

    public void setParentBlock(BlockState parentBlock)
    {
        this.parentBlock = parentBlock;
    }

    @Override
    @Nonnull
    public CompoundTag save(CompoundTag compoundNBT)
    {
        compoundNBT.put("parentBlock", NbtUtils.writeBlockState(parentBlock));

        return super.save(compoundNBT);
    }

    @Nonnull
    @Override
    public BlockPos getBlockPos()
    {
        return super.getBlockPos();
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket()
    {
        return new ClientboundBlockEntityDataPacket(this.worldPosition, 13, this.getUpdateTag());
    }

    @Override
    @Nonnull
    public CompoundTag getUpdateTag()
    {
        return this.save(new CompoundTag());
    }

    /**
     * Called when you receive a TileEntityData packet for the location this
     * TileEntity is currently in. On the client, the NetworkManager will always
     * be the remote server. On the server, it will be whomever is responsible for
     * sending the packet.
     *
     * @param net The NetworkManager the packet originated from
     * @param pkt The data packet
     */
    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt)
    {
        deserializeNBT(pkt.getTag());
        setChanged();
    }
}