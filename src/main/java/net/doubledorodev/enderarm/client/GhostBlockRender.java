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
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.EmptyModelData;

import com.mojang.blaze3d.vertex.PoseStack;
import net.doubledorodev.enderarm.EnderarmConfig;
import net.doubledorodev.enderarm.Utils;
import net.doubledorodev.enderarm.blocks.GhostBlockEntity;
import net.doubledorodev.enderarm.items.ItemRegistry;

public class GhostBlockRender implements BlockEntityRenderer<GhostBlockEntity>
{
    @ParametersAreNonnullByDefault
    @Override
    public void render(GhostBlockEntity tileEntityIn, float partialTicks, PoseStack poseStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn)
    {
        BlockRenderDispatcher blockRender = Minecraft.getInstance().getBlockRenderer();
        BlockState parentBlockState = tileEntityIn.getParentBlock();
        Level level = tileEntityIn.getLevel();
        BlockPos pos = tileEntityIn.getBlockPos();

        if (level != null && parentBlockState != null)
        {
            // Debug stuff for handling of stray blocks if they ever appear somehow.
            if (EnderarmConfig.GENERAL.debug.get())
            {
                renderParentBlock(level, blockRender, parentBlockState, pos, poseStack, bufferIn, combinedLightIn);

                level.addParticle(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE, true,
                        tileEntityIn.getBlockPos().getX() + 0.5D, tileEntityIn.getBlockPos().getY() + 0.5D, tileEntityIn.getBlockPos().getZ() + 0.5D,
                        Utils.plusMinusRandD() / 25, Utils.plusMinusRandD(), Utils.plusMinusRandD() / 25);
                return;
            }

            List<Player> playersInRenderRange = level.getEntitiesOfClass(Player.class, Utils.playerCheckAABB(tileEntityIn.getBlockPos()));

            // This shouldn't be possible but would rather account for it otherwise you get holes.
            if (playersInRenderRange.size() == 0)
            {
                renderParentBlock(level, blockRender, parentBlockState, pos, poseStack, bufferIn, combinedLightIn);
                return;
            }

            LocalPlayer player = Minecraft.getInstance().player;

            // Only render blocks for non-invisible blocks, for people holding arms that are active.
            if (player != null && parentBlockState.getRenderShape() != RenderShape.INVISIBLE &&
                    player.isHolding(ItemRegistry.ENDER_ARM.get()) &&
                    (Utils.getEnabledState(player.getMainHandItem()) || Utils.getEnabledState(player.getOffhandItem())))
            {
                // Only render for people holding an enabled arm.
                BlockTransparentRenderer.renderGhostBlock(level, tileEntityIn.getParentBlock(), pos, poseStack, combinedLightIn, OverlayTexture.NO_OVERLAY);

                level.addParticle(ParticleTypes.DRAGON_BREATH, true,
                        tileEntityIn.getBlockPos().getX() + 0.5D, tileEntityIn.getBlockPos().getY() + 0.5D, tileEntityIn.getBlockPos().getZ() + 0.5D,
                        Utils.plusMinusRandD() / 25, Utils.plusMinusRandD() / 25, Utils.plusMinusRandD() / 25);
            }
            else
            {
                renderParentBlock(level, blockRender, parentBlockState, pos, poseStack, bufferIn, combinedLightIn);
            }
        }
    }

    void renderParentBlock(Level level, BlockRenderDispatcher blockRender, BlockState parentBlockState, BlockPos pos, PoseStack poseStack, MultiBufferSource bufferSource, int combinedLight)
    {
        blockRender.getModelRenderer().tesselateBlock(level, blockRender.getBlockModel(parentBlockState), parentBlockState, pos, poseStack, bufferSource.getBuffer(RenderType.translucent()),
                true, new Random(), parentBlockState.getSeed(pos), combinedLight, EmptyModelData.INSTANCE);
    }

}
