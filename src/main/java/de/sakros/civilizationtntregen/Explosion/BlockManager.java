package de.sakros.civilizationtntregen.Explosion;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.ShulkerBox;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

import de.sakros.civilizationtntregen.Main;

public class BlockManager {
	private BlockState block;
	public static YamlConfiguration blockSection = YamlConfiguration.loadConfiguration(new File(Main.getInstance().getDataFolder() + File.separator + "blocks.yml"));
	private ConfigurationSection blockConfig;

	public BlockManager(BlockState block) {
		this.block = block;
		if(blockSection.getConfigurationSection(block.getType().toString().toLowerCase()) != null)
			this.blockConfig = blockSection.getConfigurationSection(block.getType().toString().toLowerCase());
		else {
			if(isValidBlock()) {
				Material material = block.getType();
				boolean csave = false;
				if(!blockSection.contains(material.toString().toLowerCase() + ".doPreventDamage")) {blockSection.set(material.toString().toLowerCase() + ".doPreventDamage", blockSection.getBoolean("default.doPreventDamage")); csave = true;}
				if(!blockSection.contains(material.toString().toLowerCase() + ".durability")) {blockSection.set(material.toString().toLowerCase() + ".durability", blockSection.getInt("default.durability"));csave = true;}
				if(!blockSection.contains(material.toString().toLowerCase() + ".regen")) {blockSection.set(material.toString().toLowerCase() + ".regen", blockSection.getBoolean("default.regen")); csave = true;}
				ItemStack item = new ItemStack(material);
				if(item.getItemMeta() instanceof BlockStateMeta) {
					BlockStateMeta meta = (BlockStateMeta) item.getItemMeta();
					BlockState state = meta.getBlockState();
					if(state instanceof Container && !(state instanceof ShulkerBox) && !blockSection.contains(material.toString().toLowerCase() + ".saveItems")) {
						blockSection.set(material.toString().toLowerCase() + ".saveItems", true);
						csave = true;
					}
				}
				if(!blockSection.contains(material.toString().toLowerCase() + ".replace.doReplace")) {blockSection.set(material.toString().toLowerCase() + ".replace.doReplace", blockSection.getBoolean("default.replace.doReplace")); csave = true;}
				if(!blockSection.contains(material.toString().toLowerCase() + ".replace.replaceWith")) {blockSection.set(material.toString().toLowerCase() + ".replace.replaceWith", material.toString().toLowerCase()); csave = true;}
				if(!blockSection.contains(material.toString().toLowerCase() + ".chance")) {blockSection.set(material.toString().toLowerCase() + ".chance", blockSection.getInt("default.chance")); csave = true;}
				if(csave == true) {
					try {
						blockSection.save(new File(Main.getInstance().getDataFolder() + File.separator + "blocks.yml"));
					} catch (IOException e) {
						e.printStackTrace();
					}										
				}
				this.blockConfig = blockSection.getConfigurationSection(block.getType().toString().toLowerCase());
				if(blockConfig == null) {
					this.blockConfig = blockSection.getConfigurationSection("default");
					Bukkit.getConsoleSender().sendMessage("[TnTRegen] Could not add missing block to blocks.yml: " + block.getType().toString().toLowerCase());
					Bukkit.getConsoleSender().sendMessage("[TnTRegen] Using default values for " + block.getType().toString().toLowerCase());
				} else
					Bukkit.getConsoleSender().sendMessage("[TnTRegen] Added missing block to blocks.yml: " + block.getType().toString().toLowerCase());
			}
		}
	}
	public Block getBlock() {
		return block.getBlock();
	}
	public BlockState getState() {
		return block;
	}
	public Block getBlockAbove(int distance) {
		return block.getLocation().clone().add(0, distance, 0).getBlock();
	}
	public Block getBlockBelow(int distance) {
		return block.getLocation().clone().add(0, -distance, 0).getBlock();
	}
	public Location getLocation() {
		return block.getLocation();
	}
	public Material getType() {
		return block.getType();
	}
	public boolean allowExplosionDamage() {
		if(blockConfig == null)
			return false;
		return blockConfig.getBoolean("doPreventDamage") ? false : true;
	}
	public boolean allowRegen() {
		return blockConfig.getBoolean("regen");
	}
	public boolean allowReplace() {
		return blockConfig.getBoolean("replace.doReplace");
	}
	public Material replaceWith() {
		if(blockConfig.getString("replace.replaceWith").toUpperCase().equals("DEFAULT"))
				return block.getType();
		return Material.valueOf(blockConfig.getString("replace.replaceWith").toUpperCase());
	}
	public int dropChance() {
		return blockConfig.getInt("chance");
	}
	public boolean saveItems() {
		if(!blockConfig.contains("saveItems"))
			return false;
		return blockConfig.getBoolean("saveItems");
	}
	public int getMaxRegenHeight() {
		return blockConfig.getInt("maxRegenHeight");
	}
	public int getDurability() {
		return blockConfig.getInt("durability");
	}
	public static int getMaxRegenHeight(Material material) {
		ConfigurationSection blockConfig = blockSection.getConfigurationSection(material.toString().toLowerCase());
		if(blockConfig != null)
			if(blockConfig.contains("maxRegenHeight"))
				return blockConfig.getInt("maxRegenHeight");
		return -1;
	}
	public boolean isContainer() {
		return (block instanceof Container /*&& !(block instanceof ShulkerBox)*/);
	}
	public boolean isValidBlock() {
		return isValidBlock(block.getType());
	}
	public static boolean isValidBlock(Material material) {
		if(!material.isBlock() || material == Material.AIR || material == Material.CAVE_AIR || material == Material.VOID_AIR || material == Material.TNT || material == Material.PISTON_HEAD)
			return false;
		return true;
	}
	public static List<Material> getBlocksRequiredGround() {
		List<Material> list = new ArrayList<>();
		list.add(Material.REDSTONE_WIRE);
		list.addAll(Tag.SAPLINGS.getValues());
		list.addAll(Tag.CARPETS.getValues());
		list.addAll(Tag.RAILS.getValues());
		list.addAll(Tag.WOODEN_PRESSURE_PLATES.getValues());
		list.add(Material.STONE_PRESSURE_PLATE);
		list.add(Material.LIGHT_WEIGHTED_PRESSURE_PLATE);
		list.add(Material.HEAVY_WEIGHTED_PRESSURE_PLATE);
		list.add(Material.GRASS);
		list.add(Material.FERN);
		list.add(Material.DEAD_BUSH);
		list.add(Material.SEA_PICKLE);
		for(Material mat : Tag.BANNERS.getValues()) {
			if(mat.toString().contains("BANNER") && !mat.toString().contains("WALL"))
				list.add(mat);
		}
		if(!Bukkit.getTag(Tag.REGISTRY_BLOCKS, NamespacedKey.minecraft("standing_signs"), Material.class).getValues().isEmpty())
			list.addAll(Bukkit.getTag(Tag.REGISTRY_BLOCKS, NamespacedKey.minecraft("standing_signs"), Material.class).getValues());
		else{
			list.add(Material.SPRUCE_SIGN);
			list.add(Material.ACACIA_SIGN);
			list.add(Material.BIRCH_SIGN);
			list.add(Material.OAK_SIGN);
			list.add(Material.JUNGLE_SIGN);
			list.add(Material.LEGACY_SIGN);
		}
		if(!Bukkit.getTag(Tag.REGISTRY_BLOCKS, NamespacedKey.minecraft("small_flowers"), Material.class).getValues().isEmpty())
			list.addAll(Bukkit.getTag(Tag.REGISTRY_BLOCKS, NamespacedKey.minecraft("small_flowers"), Material.class).getValues());
		else {
			list.add(Material.DANDELION);
			list.add(Material.POPPY);
			list.add(Material.BLUE_ORCHID);
			list.add(Material.ALLIUM);
			list.add(Material.AZURE_BLUET);
			list.add(Material.RED_TULIP);
			list.add(Material.ORANGE_TULIP);
			list.add(Material.WHITE_TULIP);
			list.add(Material.PINK_TULIP);
			list.add(Material.OXEYE_DAISY);
		}
		list.add(Material.BROWN_MUSHROOM);
		list.add(Material.RED_MUSHROOM);
		if(!Material.SNOW.hasGravity())
			list.add(Material.SNOW);
		list.add(Material.CACTUS);
		list.add(Material.SUGAR_CANE);
		for(Material mat : Tag.CORAL_BLOCKS.getValues()) {
			if(!mat.toString().contains("WALL"))
				list.add(mat);
		}
		list.addAll(Tag.CORAL_PLANTS.getValues());
		list.add(Material.TRIPWIRE);
		list.add(Material.REPEATER);
		list.add(Material.COMPARATOR);
		list.add(Material.TORCH);
		list.add(Material.REDSTONE_TORCH);
		list.addAll(Tag.BUTTONS.getValues());
		list.add(Material.ATTACHED_MELON_STEM);
		list.add(Material.MELON_STEM);
		list.add(Material.ATTACHED_PUMPKIN_STEM);
		list.add(Material.PUMPKIN_STEM);
		list.add(Material.BEETROOTS);
		list.add(Material.WHEAT);
		list.add(Material.CARROTS);
		list.add(Material.POTATOES);
		list.add(Material.LEVER);
		list.add(Material.NETHER_WART);
		return list;
	}
	public static List<Material> getBlocksRequiredWall() {
		List<Material> list = new ArrayList<>();
		list.addAll(Tag.BUTTONS.getValues());
		for(Material mat : Tag.BANNERS.getValues())
			if(mat.toString().contains("WALL_BANNER"))
				list.add(mat);
		list.add(Material.COCOA);
		for(Material mat : Tag.CORAL_BLOCKS.getValues()) {
			if(mat.toString().contains("WALL"))
				list.add(mat);
		}
		list.add(Material.LEVER);
		list.add(Material.WALL_TORCH);
		for(Material mat : Material.values())
			if(mat.toString().contains("WALL_SIGN"))
				list.add(mat);
		list.add(Material.REDSTONE_WALL_TORCH);
		list.add(Material.TRIPWIRE_HOOK);
		list.add(Material.LADDER);
		list.add(Material.VINE);
		return list;
	}
	
}
