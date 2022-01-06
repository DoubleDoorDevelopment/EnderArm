package net.doubledorodev.enderarm.blocks;

import java.util.Locale;
import java.util.function.Supplier;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import net.doubledorodev.enderarm.Enderarm;

public class BlockRegistry
{
    public static final DeferredRegister<Block> BLOCK_DEFERRED = DeferredRegister.create(ForgeRegistries.BLOCKS, Enderarm.MODID);
    public static final DeferredRegister<TileEntityType<?>> TILE_ENTITY_DEFERRED = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, Enderarm.MODID);

    // Blocks
    public static final RegistryObject<Block> GHOST_BLOCK = register("ghost_block", () -> new GhostBlock(
            AbstractBlock.Properties.of(Material.BARRIER)
                    .strength(-1.0F, 3600000.0F)
                    .noDrops()
                    .noOcclusion()
                    .isSuffocating(BlockRegistry::never)
                    .isViewBlocking(BlockRegistry::never)
                    .isRedstoneConductor(BlockRegistry::always)
    ));

    // Block Entities
    public static final RegistryObject<TileEntityType<GhostBlockEntity>> GHOST_BLOCK_ENTITY = register("ghost_block", GhostBlockEntity::new, GHOST_BLOCK);

    private static <T extends TileEntity> RegistryObject<TileEntityType<T>> register(String name, Supplier<T> factory, Supplier<? extends Block> block)
    {
        return TILE_ENTITY_DEFERRED.register(name, () -> TileEntityType.Builder.of(factory, block.get()).build(null));
    }

    private static <T extends Block> RegistryObject<T> register(String name, Supplier<T> blockSupplier)
    {
        final String actualName = name.toLowerCase(Locale.ROOT);
        return BLOCK_DEFERRED.register(actualName, blockSupplier);
    }

    private static boolean never(BlockState state, IBlockReader world, BlockPos pos)
    {
        return false;
    }

    private static boolean always(BlockState state, IBlockReader world, BlockPos pos)
    {
        return true;
    }
}
