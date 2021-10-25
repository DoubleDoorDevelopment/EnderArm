package net.doubledorodev.enderarm.blocks;

import java.util.Locale;
import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.fmllegacy.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import net.doubledorodev.enderarm.Enderarm;

public class BlockRegistry
{
    public static final DeferredRegister<Block> BLOCK_DEFERRED = DeferredRegister.create(ForgeRegistries.BLOCKS, Enderarm.MODID);
    public static final DeferredRegister<BlockEntityType<?>> TILE_ENTITY_DEFERRED = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, Enderarm.MODID);

    // Blocks
    public static final RegistryObject<Block> GHOST_BLOCK = register("ghost_block", () -> new GhostBlock(
            BlockBehaviour.Properties.of(Material.GLASS)
                    .strength(-1.0F, 3600000.0F)
                    .noDrops()
                    .noOcclusion()
                    .isSuffocating(BlockRegistry::never)
                    .isViewBlocking(BlockRegistry::never)
    ));

    // Block Entities
    public static final RegistryObject<BlockEntityType<GhostBlockEntity>> GHOST_BLOCK_ENTITY = register("ghost_block", GhostBlockEntity::new, GHOST_BLOCK);

    private static <T extends BlockEntity> RegistryObject<BlockEntityType<T>> register(String name, BlockEntityType.BlockEntitySupplier<T> factory, Supplier<? extends Block> block)
    {
        return TILE_ENTITY_DEFERRED.register(name, () -> BlockEntityType.Builder.of(factory, block.get()).build(null));
    }

    private static <T extends Block> RegistryObject<T> register(String name, Supplier<T> blockSupplier)
    {
        final String actualName = name.toLowerCase(Locale.ROOT);
        return BLOCK_DEFERRED.register(actualName, blockSupplier);
    }

    private static boolean never(BlockState state, BlockGetter world, BlockPos pos)
    {
        return false;
    }
}
