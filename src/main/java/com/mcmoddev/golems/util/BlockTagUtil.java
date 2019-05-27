package com.mcmoddev.golems.util;

import com.mcmoddev.golems.main.ExtraGolems;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;

public class BlockTagUtil {
	
	
	public static Tag<Block> TAG_CONCRETE;
	public static Tag<Block> TAG_SANDSTONE ;
	public static Tag<Block> TAG_RED_SANDSTONE;
	public static Tag<Block> TAG_PRISMARINE;
	public static Tag<Block> TAG_STAINED_GLASS;
	public static Tag<Block> TAG_TERRACOTTA;
	public static Tag<Block> TAG_QUARTZ;
	
	public static void loadTags() {
		TAG_CONCRETE = getTag(new ResourceLocation(ExtraGolems.MODID, "concrete"));
		TAG_SANDSTONE = getTag(new ResourceLocation(ExtraGolems.MODID, "sandstone"));
		TAG_RED_SANDSTONE = getTag(new ResourceLocation(ExtraGolems.MODID, "red_sandstone"));
		TAG_PRISMARINE = getTag(new ResourceLocation(ExtraGolems.MODID, "prismarine"));
		TAG_STAINED_GLASS = getTag(new ResourceLocation(ExtraGolems.MODID, "stained_glass"));
		TAG_TERRACOTTA = getTag(new ResourceLocation(ExtraGolems.MODID, "colored_terracotta"));
		TAG_QUARTZ = getTag(new ResourceLocation(ExtraGolems.MODID, "quartz"));
		
		// debug
		if(TAG_CONCRETE != null) {
			ExtraGolems.LOGGER.info("Loaded tag for Concrete:\n{ ");
			TAG_CONCRETE.getAllElements().stream()
				.map(e -> e.getRegistryName().toString()).forEach(e -> System.out.print(e + "\n"));
			System.out.print(" }\n");
		} else {
			ExtraGolems.LOGGER.info("TAG_CONCRETE is null!");
		}
	}
	
	public static Tag<Block> getTag(ResourceLocation path) {
		return BlockTags.getCollection().getOrCreate(path);
	}
}
