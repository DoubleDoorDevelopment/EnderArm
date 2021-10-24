package net.doubledorodev.enderarm.blocks;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;

import net.doubledorodev.enderarm.Utils;

public class GhostBlockEntity extends TileEntity implements ITickableTileEntity
{
    BlockState parentBlock = Blocks.AIR.defaultBlockState();
    ArrayList<UUID> lookingPlayers = new ArrayList<>();

    public GhostBlockEntity(TileEntityType<?> tileEntityTypeIn)
    {
        super(tileEntityTypeIn);
    }

    public GhostBlockEntity()
    {
        this(BlockRegistry.GHOST_BLOCK_ENTITY.get());
    }

    @Override
    public void tick()
    {
        if (level != null && !level.isClientSide())
        {
            List<PlayerEntity> playersInRange = level.getEntitiesOfClass(PlayerEntity.class, Utils.playerCheckAABB(worldPosition));

            // Loop over all the players that can be in range of a ghost block, Skips extra checks if a player is removed at any point.
            for (PlayerEntity player : playersInRange)
            {
                UUID playerID = player.getUUID();
                ItemStack mainHand = player.getMainHandItem();
                ItemStack offHand = player.getOffhandItem();

                // Check hands first for disabled arms. Both need to fail, method checks for valid arm item.
                if (!Utils.getEnabledState(mainHand) && !Utils.getEnabledState(offHand))
                {
                    lookingPlayers.remove(playerID);
                    continue;
                }

                CompoundNBT mainNBT = mainHand.getTagElement("handData");
                CompoundNBT offhandNBT = offHand.getTagElement("handData");

                // Next check for valid block links on the item NBT.
                if (mainNBT != null && !NBTUtil.readBlockPos(mainNBT.getCompound("activeTile")).equals(this.worldPosition) ||
                        offhandNBT != null && !NBTUtil.readBlockPos(offhandNBT.getCompound("activeTile")).equals(this.worldPosition))
                {
                    lookingPlayers.remove(playerID);
                    continue;
                }

                // Finally we throw a ray to be extra sure they didn't look off real quick and somehow mange to keep the block down.
                RayTraceResult result = Utils.findCollidable(player);

                if (result.getType() == RayTraceResult.Type.BLOCK)
                {
                    BlockRayTraceResult blockTrace = (BlockRayTraceResult) result;
                    BlockState stateAtTrace = level.getBlockState(blockTrace.getBlockPos());

                    if (stateAtTrace.getBlock() != BlockRegistry.GHOST_BLOCK.get())
                        lookingPlayers.remove(playerID);
                }
                else lookingPlayers.remove(playerID);
            }

            // Make sure to clear the block if nobody is in range or looking at it.
            if (lookingPlayers.size() == 0 || playersInRange.size() == 0)
            {
                level.setBlock(worldPosition, parentBlock, 2);
            }
        }
    }

    public void addPlayerLooking(PlayerEntity player)
    {
        if (!lookingPlayers.contains(player.getUUID()))
            lookingPlayers.add(player.getUUID());
    }

    public void removePlayerLooking(PlayerEntity player)
    {
        lookingPlayers.remove(player.getUUID());
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
    @ParametersAreNonnullByDefault
    public void load(BlockState state, CompoundNBT compoundNBT)
    {
        super.load(state, compoundNBT);

        parentBlock = NBTUtil.readBlockState(compoundNBT.getCompound("parentBlock"));
//        ListNBT listnbt = compoundNBT.getList("Players", 11);
//
//        for (net.minecraft.nbt.INBT inbt : listnbt)
//        {
//            lookingPlayers.add(NBTUtil.loadUUID(inbt));
//        }
    }

    @Override
    @Nonnull
    public CompoundNBT save(CompoundNBT compoundNBT)
    {
        compoundNBT.put("parentBlock", NBTUtil.writeBlockState(parentBlock));
//
//        ListNBT listnbt = new ListNBT();
//
//        for(UUID uuid : lookingPlayers) {
//            listnbt.add(NBTUtil.createUUID(uuid));
//        }
//
//        compoundNBT.put("Players", listnbt);

        return super.save(compoundNBT);
    }

    @Nullable
    @Override
    public SUpdateTileEntityPacket getUpdatePacket()
    {
        return new SUpdateTileEntityPacket(this.worldPosition, 3, this.getUpdateTag());
    }

    @Override
    @Nonnull
    public CompoundNBT getUpdateTag()
    {
        return this.save(new CompoundNBT());
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
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt)
    {
        deserializeNBT(pkt.getTag());
        setChanged();
    }
}