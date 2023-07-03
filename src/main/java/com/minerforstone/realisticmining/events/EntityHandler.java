package com.minerforstone.realisticmining.events;

import com.minerforstone.realisticmining.functions.Degrader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.LogicalSidedProvider;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.EntityLeaveWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static com.minerforstone.realisticmining.RealisticMiningMod.Tags.DROP_NORMAL;

public class EntityHandler {
    @SubscribeEvent
    public static void neutralizeFallingBlocks(EntityJoinWorldEvent event) {

        Entity entity = event.getEntity();
        if (entity.getClass().equals(FallingBlockEntity.class)) {

            BlockState state = ((FallingBlockEntity) entity).getBlockState();
            if (!event.getWorld().isClientSide && state.getBlock().defaultDestroyTime() > 0 && !state.is(DROP_NORMAL)) {

                ((FallingBlockEntity) entity).dropItem = false;
            }
        }
    }

    @SubscribeEvent
    public static void fallingBlockLanded(EntityLeaveWorldEvent event) {

        Entity entity = event.getEntity();
        if (entity.getClass().equals(FallingBlockEntity.class)) {

            BlockState state = ((FallingBlockEntity) entity).getBlockState();
            if (!event.getWorld().isClientSide && state.getBlock().defaultDestroyTime() > 0 && !state.is(DROP_NORMAL)) {

                LogicalSidedProvider.WORKQUEUE.get(LogicalSide.SERVER).tell(new TickTask(0, () -> {

                    BlockPos pos = new BlockPos(entity.getX(), entity.getY() + .95, entity.getZ());
                    ServerLevel level = (ServerLevel) event.getWorld();

                    if (state != level.getBlockState(pos)) {

                        Degrader.popResources(
                                level,
                                pos,
                                level.getBlockState(pos),
                                new ItemStack(RegistryObject.create(new ResourceLocation("minecraft:air"), ForgeRegistries.BLOCKS).get()),
                                Direction.UP);
                        Degrader.addBlock(
                                level,
                                pos,
                                state,
                                1);
                    }
                }));
            }
        }
    }
}