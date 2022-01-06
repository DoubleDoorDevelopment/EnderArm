package net.doubledorodev.enderarm.client;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraftforge.fml.DeferredWorkQueue;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import net.doubledorodev.enderarm.blocks.BlockRegistry;

public class ClientRegistryArm
{
    public static void init()
    {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientRegistryArm::doClientStuff);
    }

    public static void doClientStuff(final FMLClientSetupEvent event)
    {
        ClientRegistry.bindTileEntityRenderer(BlockRegistry.GHOST_BLOCK_ENTITY.get(), GhostBlockRender::new);
        DeferredWorkQueue.runLater(() -> RenderTypeLookup.setRenderLayer(BlockRegistry.GHOST_BLOCK.get(), RenderType.translucent()));
    }
}
