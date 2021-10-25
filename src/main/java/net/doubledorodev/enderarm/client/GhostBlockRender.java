package net.doubledorodev.enderarm.client;

import java.util.List;
import java.util.Random;
import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.EmptyModelData;

import com.mojang.blaze3d.vertex.PoseStack;
import net.doubledorodev.enderarm.EnderarmConfig;
import net.doubledorodev.enderarm.Utils;
import net.doubledorodev.enderarm.blocks.BlockRegistry;
import net.doubledorodev.enderarm.blocks.GhostBlock;
import net.doubledorodev.enderarm.blocks.GhostBlockEntity;
import net.doubledorodev.enderarm.items.ItemRegistry;

public class GhostBlockRender implements BlockEntityRenderer<GhostBlockEntity>
{
//    public GhostBlockRender(BlockEntityRendererProvider.Context context)
//    {
////        RenderType.create("ghost", DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS, )
////        RenderType.create("ghost", DefaultVertexFormats.BLOCK, 7, 256,
////                RenderType.State.builder()
////                        .setAlphaState(new RenderState.AlphaState(0.5F))
////                        .createCompositeState(true));
//
//    }

    @ParametersAreNonnullByDefault
    @Override
    public void render(GhostBlockEntity tileEntityIn, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn)
    {
        BlockRenderDispatcher blockRender = Minecraft.getInstance().getBlockRenderer();
        BlockState parentBlockState = tileEntityIn.getParentBlock();
        Level world = tileEntityIn.getLevel();

        if (world != null && parentBlockState != null)
        {
            // Debug stuff for handling of stray blocks if they ever appear somehow.
            if (EnderarmConfig.GENERAL.debug.get())
            {
                //TODO: I have no clue if this is even correct.
                blockRender.renderBatched(Blocks.END_GATEWAY.defaultBlockState(), tileEntityIn.getBlockPos(), world, matrixStackIn, bufferIn.getBuffer(RenderType.translucent()),
                        true, new Random(), EmptyModelData.INSTANCE);

                world.addParticle(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE, true,
                        tileEntityIn.getBlockPos().getX() + 0.5D, tileEntityIn.getBlockPos().getY() + 0.5D, tileEntityIn.getBlockPos().getZ() + 0.5D,
                        Utils.plusMinusRandD() / 25, Utils.plusMinusRandD(), Utils.plusMinusRandD() / 25);
                return;
            }

            List<Player> playersInRenderRange = world.getEntitiesOfClass(Player.class, Utils.playerCheckAABB(tileEntityIn.getBlockPos()));

            // This shouldn't be possible but would rather account for it otherwise you get holes.
            if (playersInRenderRange.size() == 0)
            {
                //TODO: I have no clue if this is even correct.
                blockRender.renderBatched(parentBlockState, tileEntityIn.getBlockPos(), world, matrixStackIn, bufferIn.getBuffer(RenderType.translucent()),
                        true, new Random(), EmptyModelData.INSTANCE);
                return;
            }

            LocalPlayer player = Minecraft.getInstance().player;

            // Only render blocks for non-invisible blocks, for people holding arms that are active.
            if (player != null && parentBlockState.getRenderShape() != RenderShape.INVISIBLE &&
                    player.isHolding(ItemRegistry.ENDER_ARM.get()) &&
                    (Utils.getEnabledState(player.getMainHandItem()) || Utils.getEnabledState(player.getOffhandItem())))
            {
                // Only render for people holding an enabled arm.
                //TODO: Couldn't find someone to do what I wanted, Changed to a texture. Oh well.
                blockRender.renderBatched(BlockRegistry.GHOST_BLOCK.get().defaultBlockState().setValue(GhostBlock.ENABLED, true), tileEntityIn.getBlockPos(), world, matrixStackIn, bufferIn.getBuffer(RenderType.translucent()),
                        true, new Random(), EmptyModelData.INSTANCE);

                world.addParticle(ParticleTypes.DRAGON_BREATH, true,
                        tileEntityIn.getBlockPos().getX() + 0.5D, tileEntityIn.getBlockPos().getY() + 0.5D, tileEntityIn.getBlockPos().getZ() + 0.5D,
                        Utils.plusMinusRandD() / 25, Utils.plusMinusRandD() / 25, Utils.plusMinusRandD() / 25);
            }
            else
            {
                //TODO: I have no clue if this is even correct.
                blockRender.renderBatched(parentBlockState, tileEntityIn.getBlockPos(), world, matrixStackIn, bufferIn.getBuffer(RenderType.translucent()),
                        true, new Random(), EmptyModelData.INSTANCE);
            }
        }
    }
}
