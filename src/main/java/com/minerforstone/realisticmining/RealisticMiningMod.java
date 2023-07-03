package com.minerforstone.realisticmining;

import com.minerforstone.realisticmining.events.BreakBlockHandler;
import com.minerforstone.realisticmining.events.EntityHandler;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

import java.util.stream.Collectors;

@Mod("realisticmining")
public class RealisticMiningMod {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static class Tags {
        public static final TagKey<Block> DROP_NORMAL = ForgeRegistries.BLOCKS.tags().createTagKey(new ResourceLocation("realisticmining:drop_normal"));
        public static final TagKey<Block> NO_FALL = ForgeRegistries.BLOCKS.tags().createTagKey(new ResourceLocation("realisticmining:no_fall"));
        public static final TagKey<Block> DROP_LOOT = ForgeRegistries.BLOCKS.tags().createTagKey(new ResourceLocation("realisticmining:drop_loot"));
    }

    public RealisticMiningMod() {
        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        // Register the enqueueIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
        // Register the processIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);

        MinecraftForge.EVENT_BUS.register(BreakBlockHandler.class);
        MinecraftForge.EVENT_BUS.register(EntityHandler.class);
    }

    private void setup(final FMLCommonSetupEvent event)
    {
        // some preinit code
        LOGGER.info("HELLO FROM PREINIT");
        LOGGER.info("DIRT BLOCK >> {}", Blocks.DIRT.getRegistryName());
    }

    private void enqueueIMC(final InterModEnqueueEvent event)
    {
        // Some example code to dispatch IMC to another mod
        InterModComms.sendTo("examplemod", "helloworld", () -> { LOGGER.info("Hello world from the MDK"); return "Hello world";});
    }

    private void processIMC(final InterModProcessEvent event)
    {
        // Some example code to receive and process InterModComms from other mods
        LOGGER.info("Got IMC {}", event.getIMCStream().
                map(m->m.messageSupplier().get()).
                collect(Collectors.toList()));
    }
}
