package com.mcmoddev.golems.proxies;

import com.mcmoddev.golems.blocks.BlockGolemHead;
import com.mcmoddev.golems.blocks.BlockUtilityGlow;
import com.mcmoddev.golems.blocks.BlockUtilityPower;
import com.mcmoddev.golems.items.ItemBedrockGolem;
import com.mcmoddev.golems.items.ItemGolemSpell;
import com.mcmoddev.golems.items.ItemInfoBook;
import com.mcmoddev.golems.main.ExtraGolems;
import com.mcmoddev.golems.main.GolemItems;
import com.mcmoddev.golems.util.ConsumerLootTables;
import com.mcmoddev.golems.util.GolemNames;
import com.mcmoddev.golems.util.config.GolemRegistrar;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.RegistryEvent;

public class ProxyCommon {
	
	static { registerLootTables(); }
	
	public void registerListeners() { }
	
	public void registerEntityRenders() { }
	
	public void registerEntities(final RegistryEvent.Register<EntityType<?>> event) {
		// Register Golem EntityEntries as well as building blocks
		GolemRegistrar.getContainers().forEach(container -> event.getRegistry().register(container.entityType));
	}

	public void registerItems(final RegistryEvent.Register<Item> event) {
		event.getRegistry().register(new ItemBlock(GolemItems.golemHead, new Item.Properties().group(ItemGroup.MISC)) {
			@Override
			@OnlyIn(Dist.CLIENT)
			public boolean hasEffect(final ItemStack stack) {
				return true;
			}
		}.setRegistryName(GolemItems.golemHead.getRegistryName()));
		ExtraGolems.LOGGER.info("RegistryEvents registering items");
		event.getRegistry()
			.register(new ItemBedrockGolem()
				.setRegistryName(ExtraGolems.MODID, "spawn_bedrock_golem"));

		event.getRegistry().register(new ItemGolemSpell()
			.setRegistryName(ExtraGolems.MODID, "golem_paper"));

		event.getRegistry().register(new ItemInfoBook()
			.setRegistryName(ExtraGolems.MODID, "info_book"));
	}

	public void registerBlocks(final RegistryEvent.Register<Block> event) {
		event.getRegistry().registerAll(
			new BlockGolemHead().setRegistryName(ExtraGolems.MODID, "golem_head"),
			new BlockUtilityGlow(Material.GLASS, 1.0F, BlockUtilityGlow.UPDATE_TICKS)
				.setRegistryName(ExtraGolems.MODID, "light_provider_full"),
			new BlockUtilityPower(15, BlockUtilityPower.UPDATE_TICKS)
				.setRegistryName(ExtraGolems.MODID, "power_provider_all"));
	}
	

	public static void registerLootTables() {
		// register Golem Loot Tables
		LootTableList.register(new ResourceLocation(ExtraGolems.MODID, "entities/_golem_base"));
		GolemNames.forEach(ConsumerLootTables.CONSUMER);
	}
}
