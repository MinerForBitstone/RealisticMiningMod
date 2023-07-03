package com.minerforstone.realisticmining.functions;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.minerforstone.realisticmining.RealisticMiningMod.Tags.DROP_LOOT;
import static com.minerforstone.realisticmining.RealisticMiningMod.Tags.NO_FALL;

public class Degrader {
    private static boolean blockHasLootTable;
    public static void degrade(ServerLevel level, BlockPos pos, BlockState state, ServerPlayer player, ItemStack tool, @Nullable Direction face) {
        if (face == null) face = Direction.UP;
        int silkTouch = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, tool);

        addBlock(level, pos, getNextBlock(level, state), silkTouch);
        if (state.is(DROP_LOOT)) popResources(level, pos, state, tool, face);
        
        // Damage the tool used, if applicable
        if (tool.isDamageableItem() && blockHasLootTable) {
            tool.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(EquipmentSlot.MAINHAND));
        }
    }

    public static BlockState getNextBlock(ServerLevel level, BlockState state) {
        // Construct a generic lootContext
        LootContext lootContext = new LootContext.Builder(level).create(new LootContextParamSet.Builder().build());

        // Get the next block from the loot table; block degrades to self without a loot table
        String blockDescription = state.getBlock().getDescriptionId();
        LootTable lootTable = Objects.requireNonNull(level.getServer()).getLootTables().get(new ResourceLocation(IdParser.getModId(blockDescription), "degradations/" + IdParser.getId(blockDescription)));
        String outputId = blockDescription;
        try { outputId = lootTable.getRandomItems(lootContext).get(0).getDescriptionId(); blockHasLootTable = true; }
        catch (Exception ignored) { blockHasLootTable = false; }

        return RegistryObject.create(new ResourceLocation(IdParser.getModId(outputId), IdParser.getId(outputId)), ForgeRegistries.BLOCKS).get().defaultBlockState();
    }

    public static boolean addBlock(ServerLevel level, BlockPos pos, BlockState state, int silkTouch) {
        // Add a degraded block to the level
        if (isFree(level.getBlockState(pos.below())) && pos.getY() >= level.getMinBuildHeight() && !state.is(NO_FALL) && silkTouch == 0) {
            FallingBlockEntity.fall(level, pos, state);
            return true;
        } else {
            level.setBlock(pos, state, 0);
            level.markAndNotifyBlock(pos, (LevelChunk) level.getChunk(pos), state, level.getBlockState(pos), 3, 3);
            return false;

        }
    }

    public static void popResources(ServerLevel level, BlockPos pos, BlockState state, ItemStack tool, Direction face) {
        // Drop loot and xp
        ItemStack adjustedTool = tool.copy();

        Map<Enchantment, Integer> enchants = EnchantmentHelper.getEnchantments(adjustedTool);
        enchants.remove(Enchantments.SILK_TOUCH);
        EnchantmentHelper.setEnchantments(enchants, adjustedTool);

        LootContext.Builder dropLootContext = new LootContext.Builder(level)
                .withParameter(LootContextParams.TOOL, adjustedTool)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos));

        List<ItemStack> items = state.getDrops(dropLootContext);
        for (ItemStack item : items) Block.popResourceFromFace(level, pos, face, item);

        int expDrop = state.getExpDrop(level, pos, EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, tool), 0);
        popExperience(level, pos, face, expDrop);
    }

    private static void popExperience(ServerLevel level, BlockPos pos, Direction face, int amount) {
        if (level.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS) && !level.restoringBlockSnapshots) {
            ExperienceOrb.award(level, Vec3.atCenterOf(pos).add(new Vec3(
                    face.getStepX() * .55F,
                    face.getStepY() * .55F,
                    face.getStepZ() * .55F
            )), amount);
        }
    }

    private static boolean isFree(BlockState state) {
        Material material = state.getMaterial();
        return state.isAir() || state.is(BlockTags.FIRE) || material.isLiquid() || material.isReplaceable();
    }

    static class IdParser {
        private static String getType(String descriptionId) { return descriptionId.split("\\.", 3)[0]; }
        private static String getModId(String descriptionId) { return descriptionId.split("\\.", 3)[1]; }
        private static String getId(String descriptionId) { return descriptionId.split("\\.", 3)[2]; }
    }
}
