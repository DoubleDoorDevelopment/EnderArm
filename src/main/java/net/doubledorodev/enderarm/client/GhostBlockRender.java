package net.doubledorodev.enderarm.client;

import java.util.List;
import java.util.Random;
import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.world.World;
import net.minecraftforge.client.model.data.EmptyModelData;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.doubledorodev.enderarm.EnderarmConfig;
import net.doubledorodev.enderarm.Utils;
import net.doubledorodev.enderarm.blocks.BlockRegistry;
import net.doubledorodev.enderarm.blocks.GhostBlock;
import net.doubledorodev.enderarm.blocks.GhostBlockEntity;
import net.doubledorodev.enderarm.items.ItemRegistry;

public class GhostBlockRender extends TileEntityRenderer<GhostBlockEntity>
{
    public GhostBlockRender(TileEntityRendererDispatcher rendererDispatcherIn)
    {
        super(rendererDispatcherIn);
        RenderType.create("ghost", DefaultVertexFormats.BLOCK, 7, 256,
                RenderType.State.builder()
                        .setAlphaState(new RenderState.AlphaState(0.5F))
                        .createCompositeState(true));

    }

    @ParametersAreNonnullByDefault
    @Override
    public void render(GhostBlockEntity tileEntityIn, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn)
    {
        BlockRendererDispatcher blockRender = Minecraft.getInstance().getBlockRenderer();
        BlockState parentBlockState = tileEntityIn.getParentBlock();
        World world = tileEntityIn.getLevel();

        if (world != null && parentBlockState != null)
        {
            // Debug stuff for handling of stray blocks if they ever appear somehow.
            if (EnderarmConfig.GENERAL.debug.get())
            {
                //TODO: I have no clue if this is even correct.
                blockRender.renderModel(Blocks.END_GATEWAY.defaultBlockState(), tileEntityIn.getBlockPos(), world, matrixStackIn, bufferIn.getBuffer(RenderType.translucent()),
                        true, new Random(), EmptyModelData.INSTANCE);

                world.addParticle(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE, true,
                        tileEntityIn.getBlockPos().getX() + 0.5D, tileEntityIn.getBlockPos().getY() + 0.5D, tileEntityIn.getBlockPos().getZ() + 0.5D,
                        Utils.plusMinusRandD() / 25, Utils.plusMinusRandD(), Utils.plusMinusRandD() / 25);
                return;
            }

            List<PlayerEntity> playersInRenderRange = world.getEntitiesOfClass(PlayerEntity.class, Utils.playerCheckAABB(tileEntityIn.getBlockPos()));

            // This shouldn't be possible but would rather account for it otherwise you get holes.
            if (playersInRenderRange.size() == 0)
            {
                //TODO: I have no clue if this is even correct.
                blockRender.renderModel(parentBlockState, tileEntityIn.getBlockPos(), world, matrixStackIn, bufferIn.getBuffer(RenderType.translucent()),
                        true, new Random(), EmptyModelData.INSTANCE);
                return;
            }

            ClientPlayerEntity player = Minecraft.getInstance().player;

            // Only render blocks for non-invisible blocks, for people holding arms that are active.
            if (player != null && parentBlockState.getRenderShape() != BlockRenderType.INVISIBLE &&
                    player.isHolding(ItemRegistry.ENDER_ARM.get()) &&
                    (Utils.getEnabledState(player.getMainHandItem()) || Utils.getEnabledState(player.getOffhandItem())))
            {
                // Only render for people holding an enabled arm.
                //TODO: Couldn't find someone to do what I wanted, Changed to a texture. Oh well.
                blockRender.renderModel(BlockRegistry.GHOST_BLOCK.get().defaultBlockState().setValue(GhostBlock.ENABLED, true), tileEntityIn.getBlockPos(), world, matrixStackIn, bufferIn.getBuffer(RenderType.translucent()),
                        true, new Random(), EmptyModelData.INSTANCE);

                world.addParticle(ParticleTypes.DRAGON_BREATH, true,
                        tileEntityIn.getBlockPos().getX() + 0.5D, tileEntityIn.getBlockPos().getY() + 0.5D, tileEntityIn.getBlockPos().getZ() + 0.5D,
                        Utils.plusMinusRandD() / 25, Utils.plusMinusRandD() / 25, Utils.plusMinusRandD() / 25);
            }
            else
            {
                //TODO: I have no clue if this is even correct.
                blockRender.renderModel(parentBlockState, tileEntityIn.getBlockPos(), world, matrixStackIn, bufferIn.getBuffer(RenderType.translucent()),
                        true, new Random(), EmptyModelData.INSTANCE);
            }
        }
    }
}
