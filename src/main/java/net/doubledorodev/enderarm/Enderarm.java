package net.doubledorodev.enderarm;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DeferredWorkQueue;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import net.doubledorodev.enderarm.blocks.BlockRegistry;
import net.doubledorodev.enderarm.client.GhostBlockRender;
import net.doubledorodev.enderarm.events.SwapBlockEvent;
import net.doubledorodev.enderarm.items.ItemRegistry;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("enderarm")
public class Enderarm
{
    //TODO: Item needs front face remade and an animation done for it.
    //TODO: How to animate an item based off NBT?
    //TODO: Make repair system more better.
    //TODO: Find a better render layer(?) that works better than the current one as lots of stuff is lost.

    //TODO: Fix break protection.

    //TODO: Interacting with a base block doesn't work right. Crafting table opens and instantly closes.
    //TODO: Fix the Item render in the GUI.
    //TODO: Fix fire getting yeeted.
    //TODO: Doors break...
    //TODO: Walls/Fences don't connect.

    public static final String MODID = "enderarm";

    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();

    public Enderarm()
    {
        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register config.
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, EnderarmConfig.spec);

        // Register the doClientStuff method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(SwapBlockEvent.class);

        ItemRegistry.ITEMS_DEFERRED.register(modEventBus);
        BlockRegistry.BLOCK_DEFERRED.register(modEventBus);
        BlockRegistry.TILE_ENTITY_DEFERRED.register(modEventBus);
    }

    private void doClientStuff(final FMLClientSetupEvent event)
    {
        ClientRegistry.bindTileEntityRenderer(BlockRegistry.GHOST_BLOCK_ENTITY.get(), GhostBlockRender::new);
        DeferredWorkQueue.runLater(() -> RenderTypeLookup.setRenderLayer(BlockRegistry.GHOST_BLOCK.get(), RenderType.translucent()));
    }
}
