package net.doubledorodev.enderarm.client;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import net.doubledorodev.enderarm.blocks.BlockRegistry;

public class ClientRegistry
{
    public static void init()
    {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientRegistry::doClientStuff);
    }

    public static void doClientStuff(final FMLClientSetupEvent event)
    {
        BlockEntityRenderers.register(BlockRegistry.GHOST_BLOCK_ENTITY.get(), context -> new GhostBlockRender());
        ItemBlockRenderTypes.setRenderLayer(BlockRegistry.GHOST_BLOCK.get(), RenderType.translucent());
    }
}
