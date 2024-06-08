package com.minerforstone.realisticmining.events;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.vehicle.Minecart;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

// TODO: Figure out how to add Carry On as a proper dependency because this is not going to work

public class MinecartInteractionHandler {
    @SubscribeEvent
    public static void rightClick(PlayerInteractEvent.EntityInteract event) {
        if (event.getTarget().getClass().equals(Minecart.class)) {
            if (event.getPlayer().getMainHandItem().getItem().equals(RegistryObject.create(new ResourceLocation("carryon:tile_item"), ForgeRegistries.ITEMS).get())) {
                event.setCanceled(true);

                // Store the block ID being held into the Minecart
                Tag blockId = event.getPlayer().getMainHandItem().getTag().get("block");

                CompoundTag minecartNBT = event.getTarget().serializeNBT();
                CompoundTag displayNBT = minecartNBT.getCompound("DisplayState");

                displayNBT.put("Name", blockId);

                minecartNBT.put("DisplayState", displayNBT);
                minecartNBT.putInt("DisplayOffset", 3);
                minecartNBT.putBoolean("CustomDisplayTile", true);

                event.getTarget().deserializeNBT(minecartNBT);

                // TODO: Remove the item from the inventory
                event.getPlayer().startUsingItem(event.getHand());
                event.getPlayer().releaseUsingItem();

            }
            else if (event.getPlayer().getMainHandItem().getItem().equals(RegistryObject.create(new ResourceLocation("minecraft:air"), ForgeRegistries.ITEMS).get())) {
                event.setCanceled(true);

                CompoundTag minecartNBT = event.getTarget().serializeNBT();
                minecartNBT.putBoolean("CustomDisplayTile", false);
                event.getTarget().deserializeNBT(minecartNBT);

                String blockId = minecartNBT.getCompound("DisplayState").getString("Name");

                ItemStack pickupItem = new ItemStack(RegistryObject.create(new ResourceLocation("carryon:tile_item"), ForgeRegistries.ITEMS).get());

                CompoundTag tileDef = new CompoundTag();
                // TODO: Add necessary NBT data to Tile Item

                pickupItem.deserializeNBT(tileDef);

                event.getPlayer().setItemSlot(EquipmentSlot.MAINHAND, pickupItem);
            }
        }
    }
}
