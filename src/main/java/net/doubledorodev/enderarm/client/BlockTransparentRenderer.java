package net.doubledorodev.enderarm.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.EmptyModelData;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.List;
import java.util.Random;

public class BlockTransparentRenderer {
    
    private static final Random random = new Random();

    public static void renderGhostBlock(Level level, BlockState state, BlockPos pos, PoseStack poseStack, int light, int overlay) {
        if (state.getRenderShape() == RenderShape.MODEL) {
            RenderSystem.enableBlend();
            BakedModel model = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getBlockModel(state);
            int tintColor = Minecraft.getInstance().getBlockRenderer().blockColors.getColor(state, level, pos, 0);
            float tintRed = ((tintColor >>> 16) & 0xFF) / 255f;
            float tintGreen = ((tintColor >>> 8) & 0xFF) / 255f;
            float tintBlue = ((tintColor) & 0xFF) / 255f;
            // Render into the crumbling buffer, so other block entity renderers can render behind this
            // without using fabulous graphics.
            VertexConsumer vertex = Minecraft.getInstance().renderBuffers().crumblingBufferSource().getBuffer(RenderType.translucent());
            for (Direction direction : Direction.values()) {
                random.setSeed(state.getSeed(pos));
                List<BakedQuad> list = model.getQuads(state, direction, random, EmptyModelData.INSTANCE);
                if (!list.isEmpty()) {
                    renderBlockQuad(poseStack.last(), vertex, list, tintRed, tintGreen, tintBlue, light, overlay);
                }
            }

            random.setSeed(state.getSeed(pos));
            List<BakedQuad> list = model.getQuads(state, null, random, EmptyModelData.INSTANCE);
            if (!list.isEmpty()) {
                renderBlockQuad(poseStack.last(), vertex, list, tintRed, tintGreen, tintBlue, light, overlay);
            }
            Minecraft.getInstance().renderBuffers().crumblingBufferSource().endBatch();
            RenderSystem.disableBlend();
        }
    }

    private static void renderBlockQuad(PoseStack.Pose pose, VertexConsumer vertex, List<BakedQuad> list, float tintRed, float tintGreen, float tintBlue, int light, int overlay) {
        for (BakedQuad quad : list) {
            if (quad.isTinted()) {
                putBulkDataWithAlpha(vertex, pose, quad, new float[]{1.0F, 1.0F, 1.0F, 1.0F}, tintRed, tintGreen, tintBlue, new int[]{light, light, light, light}, overlay, false);
            } else {
                putBulkDataWithAlpha(vertex, pose, quad, new float[]{1.0F, 1.0F, 1.0F, 1.0F}, 1, 1, 1, new int[]{light, light, light, light}, overlay, false);
            }
        }
    }

    // Copy of VertexConsumer#putBulkData but will always use 0.6 for alpha
    @SuppressWarnings("all")
    private static void putBulkDataWithAlpha(VertexConsumer vertex, PoseStack.Pose p_85996_, BakedQuad p_85997_, float[] p_85998_, float p_85999_, float p_86000_, float p_86001_, int[] p_86002_, int p_86003_, boolean p_86004_) {
        float[] afloat = new float[]{p_85998_[0], p_85998_[1], p_85998_[2], p_85998_[3]};
        int[] aint = new int[]{p_86002_[0], p_86002_[1], p_86002_[2], p_86002_[3]};
        int[] aint1 = p_85997_.getVertices();
        Vec3i vec3i = p_85997_.getDirection().getNormal();
        Vector3f vector3f = new Vector3f((float)vec3i.getX(), (float)vec3i.getY(), (float)vec3i.getZ());
        Matrix4f matrix4f = p_85996_.pose();
        vector3f.transform(p_85996_.normal());
        int i = 8;
        int j = aint1.length / 8;
        MemoryStack memorystack = MemoryStack.stackPush();

        try {
            ByteBuffer bytebuffer = memorystack.malloc(DefaultVertexFormat.BLOCK.getVertexSize());
            IntBuffer intbuffer = bytebuffer.asIntBuffer();

            for(int k = 0; k < j; ++k) {
                intbuffer.clear();
                intbuffer.put(aint1, k * 8, 8);
                float f = bytebuffer.getFloat(0);
                float f1 = bytebuffer.getFloat(4);
                float f2 = bytebuffer.getFloat(8);
                float f3;
                float f4;
                float f5;
                if (p_86004_) {
                    float f6 = (float)(bytebuffer.get(12) & 255) / 255.0F;
                    float f7 = (float)(bytebuffer.get(13) & 255) / 255.0F;
                    float f8 = (float)(bytebuffer.get(14) & 255) / 255.0F;
                    f3 = f6 * afloat[k] * p_85999_;
                    f4 = f7 * afloat[k] * p_86000_;
                    f5 = f8 * afloat[k] * p_86001_;
                } else {
                    f3 = afloat[k] * p_85999_;
                    f4 = afloat[k] * p_86000_;
                    f5 = afloat[k] * p_86001_;
                }

                int l = vertex.applyBakedLighting(p_86002_[k], bytebuffer);
                float f9 = bytebuffer.getFloat(16);
                float f10 = bytebuffer.getFloat(20);
                Vector4f vector4f = new Vector4f(f, f1, f2, 1.0F);
                vector4f.transform(matrix4f);
                vertex.applyBakedNormals(vector3f, bytebuffer, p_85996_.normal());
                vertex.vertex(vector4f.x(), vector4f.y(), vector4f.z(), f3, f4, f5, 0.6f, f9, f10, p_86003_, l, vector3f.x(), vector3f.y(), vector3f.z());
            }
        } catch (Throwable throwable1) {
            if (memorystack != null) {
                try {
                    memorystack.close();
                } catch (Throwable throwable) {
                    throwable1.addSuppressed(throwable);
                }
            }

            throw throwable1;
        }

        if (memorystack != null) {
            memorystack.close();
        }
    }
}
