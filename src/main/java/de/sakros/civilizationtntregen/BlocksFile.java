package de.sakros.civilizationtntregen;

import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.ShulkerBox;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

import de.sakros.civilizationtntregen.Explosion.BlockManager;

public class BlocksFile {

	public static void update() {
		File blockFile = new File(Main.getInstance().getDataFolder() + File.separator + "blocks.yml"); 
		YamlConfiguration config = YamlConfiguration.loadConfiguration(blockFile);
		if(!blockFile.exists()) {
			try {
				blockFile.createNewFile();
				Bukkit.getConsoleSender().sendMessage("Generating blocks.yml file.");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		boolean csave = false;
		if(!config.contains("default.doPreventDamage")) {config.set("default.doPreventDamage", false); csave = true;}
		if(!config.contains("default.durability")) {config.set("default.durability", 1);csave = true;}
		if(!config.contains("default.regen")) {config.set("default.regen", true); csave = true;}
		if(!config.contains("default.replace.doReplace")) {config.set("default.replace.doReplace", false); csave = true;}
		if(!config.contains("default.replace.replaceWith")) {config.set("default.replace.replaceWith", "default"); csave = true;}
		if(!config.contains("default.chance")) {config.set("default.chance", 30); csave = true;}
		for(Material material : Material.values()) {
			if(material.isBlock()) {
				if(!BlockManager.isValidBlock(material)) {
					if(config.contains(material.toString().toLowerCase())) {
						config.set(material.toString().toLowerCase(), null);
						csave = true;
					}
				} else {
					if(!config.contains(material.toString().toLowerCase() + ".doPreventDamage")) {config.set(material.toString().toLowerCase() + ".doPreventDamage", false); csave = true;}
					if(material == Material.ENDER_CHEST || material == Material.OBSIDIAN || material == Material.ENCHANTING_TABLE || material == Material.ANVIL || material == Material.STRUCTURE_BLOCK || material == Material.END_PORTAL_FRAME || material == Material.END_PORTAL || material == Material.END_GATEWAY || material == Material.COMMAND_BLOCK || material == Material.CHAIN_COMMAND_BLOCK || material == Material.REPEATING_COMMAND_BLOCK || material == Material.BEDROCK || material == Material.BARRIER) {
						if(!config.contains(material.toString().toLowerCase() + ".durability")) {config.set(material.toString().toLowerCase() + ".durability", -1); csave = true;}
					} else
						if(!config.contains(material.toString().toLowerCase() + ".durability")) {config.set(material.toString().toLowerCase() + ".durability", 1);csave = true;}
					if(!config.contains(material.toString().toLowerCase() + ".regen")) {config.set(material.toString().toLowerCase() + ".regen", true); csave = true;}
					ItemStack item = new ItemStack(material);
					if(item.getItemMeta() instanceof BlockStateMeta) {
						BlockStateMeta meta = (BlockStateMeta) item.getItemMeta();
						BlockState state = meta.getBlockState();
						if(state instanceof Container && !(state instanceof ShulkerBox) && !config.contains(material.toString().toLowerCase() + ".saveItems")) {
							config.set(material.toString().toLowerCase() + ".saveItems", true);
							csave = true;
						}
					}
					if(material == Material.CACTUS || material == Material.SUGAR_CANE) {
						if(!config.contains(material.toString().toLowerCase() + ".maxRegenHeight")) {config.set(material.toString().toLowerCase() + ".maxRegenHeight", 3); csave = true;}
					}
					if(!config.contains(material.toString().toLowerCase() + ".replace.doReplace")) {config.set(material.toString().toLowerCase() + ".replace.doReplace", false); csave = true;}
					if(!config.contains(material.toString().toLowerCase() + ".replace.replaceWith")) {config.set(material.toString().toLowerCase() + ".replace.replaceWith", material.toString().toLowerCase()); csave = true;}
					if(!config.contains(material.toString().toLowerCase() + ".chance")) {config.set(material.toString().toLowerCase() + ".chance", 30); csave = true;}
					
				}
				if(csave == true) {
					try {
						config.save(blockFile);
					} catch (IOException e) {
						e.printStackTrace();
					}										
				}
			}
		}
		BlockManager.blockSection = YamlConfiguration.loadConfiguration(new File(Main.getInstance().getDataFolder() + File.separator + "blocks.yml"));
	}
	public static boolean isLegalBlock(Material type) {
		return (type == Material.AIR || type == Material.WATER || type == Material.LAVA || type == Material.FIRE);
	}
}
