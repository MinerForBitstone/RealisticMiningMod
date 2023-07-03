package com.minerforstone.realisticmining.events;

import com.minerforstone.realisticmining.functions.Degrader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.*;

import static com.minerforstone.realisticmining.RealisticMiningMod.Tags.DROP_NORMAL;

public class BreakBlockHandler {
    private final static Map<ServerPlayer, Direction> mineFaces = new HashMap<>();
    private final static Map<ServerPlayer, Boolean> isMining = new HashMap<>();

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void hitBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getSide() == LogicalSide.SERVER) {
            mineFaces.put((ServerPlayer) event.getPlayer(), event.getFace());
            isMining.put((ServerPlayer) event.getPlayer(), true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void touchBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getSide() == LogicalSide.SERVER) {
            isMining.put((ServerPlayer) event.getPlayer(), false);
        }
    }
    
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void breakBlock(BlockEvent.BreakEvent event) {
        if (!event.getState().is(DROP_NORMAL) && !event.getPlayer().getAbilities().instabuild && event.getState().getBlock().defaultDestroyTime() > 0 && isMining.getOrDefault((ServerPlayer) event.getPlayer(), true)) {
            event.setCanceled(true); // Don't break the block!
            Degrader.degrade((ServerLevel) event.getWorld(), event.getPos(), event.getState(), (ServerPlayer) event.getPlayer(), event.getPlayer().getMainHandItem(), mineFaces.getOrDefault((ServerPlayer) event.getPlayer(), Direction.UP));
        }
    }

    @SubscribeEvent
    public static void explode(ExplosionEvent.Detonate event) {
        List<BlockPos> blocks = event.getAffectedBlocks();
        Random random = event.getWorld().getRandom();
        for (Iterator<BlockPos> iterator = blocks.iterator(); iterator.hasNext();) {
            BlockPos pos = iterator.next();
            if (event.getWorld().getBlockState(pos).getBlock().defaultDestroyTime() > 0) {
                if (random.nextDouble() < .2) {
                    Degrader.addBlock(
                            (ServerLevel) event.getWorld(),
                            pos,
                            Degrader.getNextBlock((ServerLevel) event.getWorld(), event.getWorld().getBlockState(pos)),
                            0);
                } else {
                    Degrader.addBlock(
                            (ServerLevel) event.getWorld(),
                            pos,
                            RegistryObject.create(new ResourceLocation("minecraft:air"), ForgeRegistries.BLOCKS).get().defaultBlockState(),
                            1);
                }
                iterator.remove();
            }
        }
    }
}
