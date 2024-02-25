package com.kqp.inventorytabs.tabs.tab;

import java.util.Objects;

import com.kqp.inventorytabs.init.InventoryTabs;
import com.kqp.inventorytabs.tabs.provider.BlockTabProvider;
import com.kqp.inventorytabs.util.BlockUtil;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Generic tab for blocks.
 */
public class SimpleBlockTab extends Tab {
    public final Identifier blockId;
    public final BlockPos blockPos;

    public SimpleBlockTab(Identifier blockId, BlockPos blockPos) {
        super(new ItemStack(MinecraftClient.getInstance().player.method_48926().getBlockState(blockPos).getBlock()));
        this.blockId = blockId;
        this.blockPos = blockPos;
    }

    @Override
    public void open() {
        MinecraftClient client = MinecraftClient.getInstance();
        BlockHitResult hitResult;

        if (InventoryTabs.getConfig().doSightChecksFlag) {
            hitResult = BlockUtil.getLineOfSight(blockPos, client.player, 5D);
        } else {
            hitResult = new BlockHitResult(Vec3d.ofCenter(blockPos), Direction.EAST, blockPos, false);
        }

        if (hitResult != null) {
            if (InventoryTabs.getConfig().rotatePlayer) {
                MinecraftClient.getInstance().player.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES,
                        Vec3d.ofCenter(blockPos));
            }

            MinecraftClient.getInstance().interactionManager.interactBlock(client.player,
                    Hand.MAIN_HAND, hitResult);
        }
    }

    @Override
    public boolean shouldBeRemoved() {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;

        if (!Registries.BLOCK.getId(player.method_48926().getBlockState(blockPos).getBlock()).equals(blockId)) {
            return true;
        }

        if (InventoryTabs.getConfig().doSightChecksFlag) {
            if (BlockUtil.getLineOfSight(blockPos, player, 5D) == null) {
                return true;
            } else {
                return !BlockUtil.inRange(blockPos, player, 5D);
            }
        }
        Vec3d playerHead = player.getPos().add(0D, player.getEyeHeight(player.getPose()), 0D);

        return Vec3d.ofCenter(blockPos).subtract(playerHead).lengthSquared() > BlockTabProvider.SEARCH_DISTANCE
                * BlockTabProvider.SEARCH_DISTANCE;

    }

    @Override
    public Text getHoverText() {
        World world = MinecraftClient.getInstance().player.method_48926();

        BlockEntity blockEntity = world.getBlockEntity(blockPos);

        if (blockEntity != null) {
            NbtCompound tag = new NbtCompound();
            blockEntity.writeNbt(tag); // had to use an accesswidener for this in 1.18

            if (tag.contains("CustomName", 8)) {
                return Text.Serialization.fromJson(tag.getString("CustomName"));
            }
        }

        return Text.translatable(world.getBlockState(blockPos).getBlock().getTranslationKey());

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SimpleBlockTab tab = (SimpleBlockTab) o;
        return Objects.equals(blockPos, tab.blockPos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(blockPos);
    }
}
