package com.kqp.inventorytabs.tabs.provider;

import com.kqp.inventorytabs.tabs.tab.SimpleBlockTab;
import com.kqp.inventorytabs.tabs.tab.Tab;
import net.minecraft.block.Block;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.*;

/**
 * Provides tabs for blocks that should only have one tab at a time (e.g. Crafting Tables).
 **/
public class UniqueTabProvider extends BlockTabProvider {
    private final Set<Identifier> uniqueBlocks = new HashSet<>();

    @Override
    public void addAvailableTabs(ClientPlayerEntity player, List<Tab> tabs) {
        super.addAvailableTabs(player, tabs);
        Set<Identifier> tabsToRemove = new HashSet<>();
        List<SimpleBlockTab> craftingTableTabs = tabs.stream().filter(tab -> tab instanceof SimpleBlockTab).map(tab -> (SimpleBlockTab) tab)
                .filter(tab -> uniqueBlocks.contains(tab.blockId)).toList();

        for (SimpleBlockTab tab : craftingTableTabs) {
            if (!tabsToRemove.add(tab.blockId)) {
                tabs.remove(tab);
            }
        }
    }

    public void addUniqueBlock(Block block) {
        uniqueBlocks.add(Registries.BLOCK.getId(block));
    }

    public void addUniqueBlock(Identifier blockId) {
        uniqueBlocks.add(blockId);
    }

    public void removeUniqueBlockId(Identifier blockId) {
        uniqueBlocks.remove(blockId);
    }
    @Override
    public boolean matches(World world, BlockPos pos) {
        return uniqueBlocks.contains(Registries.BLOCK.getId(world.getBlockState(pos).getBlock()));
    }

    @Override
    public Tab createTab(World world, BlockPos pos) {
        return new SimpleBlockTab(Registries.BLOCK.getId(world.getBlockState(pos).getBlock()), pos);
    }
}
