package de.sakros.civilizationtntregen.Inventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import de.sakros.civilizationtntregen.ConfigManager;
import de.sakros.civilizationtntregen.ParticlePresetManager;
import de.sakros.civilizationtntregen.ParticleType;
import de.sakros.civilizationtntregen.PlayerSettingsManager;
import de.sakros.civilizationtntregen.PlayerSettingsManager.PlayerSettings;
import de.sakros.civilizationtntregen.Explosion.ExplosionManager.ExplosionType;

public class InventoryManager {
//	private static HashMap<String, Inventory> categories = new HashMap<>();
//	private static HashMap<String, Inventory> subCategories = new HashMap<>();
//	private static HashMap<String, Inventory> subCatParticles = new HashMap<>();
//	private static HashMap<String, Inventory> subCatSounds = new HashMap<>();
	private static HashMap<UUID, HashMap<String, Inventory>> playerCategories = new HashMap<>();
	private static HashMap<UUID, HashMap<String, Inventory>> playerSubCategories = new HashMap<>();
	private static HashMap<UUID, HashMap<ParticleType, HashMap<String, Inventory>>> playerSubCatParticles = new HashMap<>();
	private static HashMap<UUID, HashMap<String, Inventory>> playerSubCatSounds = new HashMap<>();

	private static int maxParticlePage = 0;
	private static int maxSoundPage = 0;
	public static void updateInventories(UUID uuid) {
		HashMap<String, Inventory> pCategories;
		HashMap<String, Inventory> pSubCategories;
		HashMap<ParticleType, HashMap<String, Inventory>> pSubCatParticles;
		HashMap<String, Inventory> pSubCatSounds;
		if(!playerCategories.containsKey(uuid))
			pCategories = new HashMap<>();
		else
			pCategories = playerCategories.get(uuid);
		if(!playerSubCategories.containsKey(uuid))
			pSubCategories = new HashMap<>();
		else
			pSubCategories = playerSubCategories.get(uuid);
		if(!playerSubCatParticles.containsKey(uuid))
			pSubCatParticles = new HashMap<>();
		else
			pSubCatParticles = playerSubCatParticles.get(uuid);
		if(!playerSubCatSounds.containsKey(uuid))
			pSubCatSounds = new HashMap<>();
		else
			pSubCatSounds = playerSubCatSounds.get(uuid);
		HashMap<Inventory, String> invs = new HashMap<>();
		for(String s : pCategories.keySet())
			invs.put(pCategories.get(s), s);
		for(String s : pSubCategories.keySet())
			invs.put(pSubCategories.get(s), s);
		for(HashMap<String, Inventory> i : pSubCatParticles.values())
			for(String s : i.keySet())
				invs.put(i.get(s), s);
		for(String s : pSubCatSounds.keySet())
			invs.put(pSubCatSounds.get(s), s);
		for(Inventory inventory : invs.keySet())
			inventory.clear();
		PlayerSettingsManager playerManager = null;
		if(ConfigManager.isPlayerSettingsEnabled() && Bukkit.getPlayer(uuid) != null)
			playerManager = new PlayerSettingsManager(uuid);
		int pLength = Particle.values().length;
		for(Particle p : Particle.values()) {
			if(p.getDataType() != Void.class)
				pLength--;
		}
		maxParticlePage = Integer.parseInt(String.valueOf((pLength / 45d) + 1d).split("\\.")[0]);
		if(String.valueOf((pLength / 45d) + 1d).split("\\.")[1].equals("0"))
			maxParticlePage--;
		maxSoundPage = Integer.parseInt(String.valueOf((Sound.values().length / 45d) + 1d).split("\\.")[0]);
		if(String.valueOf((Sound.values().length / 45d) + 1d).split("\\.")[1].equals("0"))
			maxSoundPage--;
		for(int i = 0; i < 2; i++) {
			String cat = i == 0 ? "Particle" : "Sound";
			String title = cat + " - Select Category" + (playerManager == null ? " §6§l✯" : "");
			Inventory category;
			if(pCategories.containsKey(title.toLowerCase().replace(" §6§l✯", ""))) {
				category = pCategories.get(title.toLowerCase().replace(" §6§l✯", ""));
			} else
				category = Bukkit.createInventory(null, InventoryType.HOPPER, title);
			TypeCommand command = TypeCommand.valueOf(cat.toUpperCase());
			if(playerManager == null || !playerManager.getConfigurableEntities(command).isEmpty())
				category.setItem(1, new ItemCreator(Material.TNT).setDisplayName("§cEntities").create());
			if(playerManager == null || !playerManager.getConfigurableBlock(command).isEmpty())
				category.setItem(3, new ItemCreator(Material.WHITE_BED).setDisplayName("§aBlocks").create());
			pCategories.putIfAbsent(title.toLowerCase().replace(" §6§l✯", ""), category);
		}
		for(int i = 0; i < 4; i++) {
			ExplosionType type = i%2 == 0 ? ExplosionType.ENTITY : ExplosionType.BLOCK;
			TypeCommand command = i <= 1 ? TypeCommand.PARTICLE : TypeCommand.SOUND;
			String cat = (type == ExplosionType.ENTITY ? "§c" : "§a") + StringUtils.capitalize(command.toString().toLowerCase());
			double listSize;
			if(playerManager != null)
				listSize = i%2 == 0 ? playerManager.getConfigurableEntities(command).size() / 9d : playerManager.getConfigurableBlock(command).size() / 9d;
			else
				listSize = i%2 == 0 ? ConfigManager.getSupportedEntities().size() / 9d : ConfigManager.getSupportedBlocks().size() / 9d;
			int subCatSize = Float.parseFloat(String.valueOf(listSize).split("\\.")[1]) == 0 ? Integer.parseInt(String.valueOf(listSize).split("\\.")[0]) : Integer.parseInt(String.valueOf(listSize).split("\\.")[0]) + 1;
			if(subCatSize == 0)
				subCatSize = 1;
			String subTitle = cat + " - Select " + type.toString().toLowerCase() + (playerManager == null ? " §6§l✯" : "");
			Inventory subCategory;
			if(pSubCategories.containsKey(subTitle.toLowerCase().replace(" §6§l✯", ""))) {
				subCategory = pSubCategories.get(subTitle.toLowerCase().replace(" §6§l✯", ""));
			} else
				subCategory = Bukkit.createInventory(null, subCatSize*9, subTitle);
			if(type == ExplosionType.ENTITY) {
				for(EntityType entity : playerManager != null ? playerManager.getConfigurableEntities(command) : ConfigManager.getSupportedEntities()) {
					switch(entity) {
					case FIREBALL:
					case SMALL_FIREBALL:
					case DRAGON_FIREBALL:
						subCategory.addItem(new ItemCreator(Material.FIRE_CHARGE).setDisplayName("§a" + ItemCreator.fancyDisplayName(entity.toString())).create());
						break;
					case WITHER_SKULL:
						subCategory.addItem(new ItemCreator(Material.WITHER_SKELETON_SKULL).setDisplayName("§a" + ItemCreator.fancyDisplayName(entity.toString())).create());
						break;
					case PRIMED_TNT:
						subCategory.addItem(new ItemCreator(Material.TNT).setDisplayName("§a" + ItemCreator.fancyDisplayName(entity.toString())).create());
						break;
					case MINECART_TNT:
						subCategory.addItem(new ItemCreator(Material.TNT_MINECART).setDisplayName("§a" + ItemCreator.fancyDisplayName(entity.toString())).create());
						break;
					case CREEPER:
						subCategory.addItem(new ItemCreator(Material.CREEPER_HEAD).setDisplayName("§a" + ItemCreator.fancyDisplayName(entity.toString())).create());
						break;
					case ENDER_CRYSTAL:
						subCategory.addItem(new ItemCreator(Material.END_CRYSTAL).setDisplayName("§a" + ItemCreator.fancyDisplayName(entity.toString())).create());		
						break;
					default:
						subCategory.addItem(new ItemCreator(Material.FIREWORK_STAR).setDisplayName("§a" + ItemCreator.fancyDisplayName(entity.toString())).create());
						break;
					}
				}
			}
			if(type == ExplosionType.BLOCK) {
				for(Material material : playerManager != null ? playerManager.getConfigurableBlock(command) : ConfigManager.getSupportedBlocks()) {
					subCategory.addItem(new ItemCreator(material).setDisplayName("§a" + ItemCreator.fancyDisplayName(material.toString())).create());
				}
			}
			ArrayList<Object> objects = new ArrayList<>();
			objects.addAll(ConfigManager.getSupportedEntities());
			objects.addAll(ConfigManager.getSupportedBlocks());
			for(Object object : objects) {
				ConfigManager configManager = new ConfigManager(type, object.toString());
				if((type == ExplosionType.ENTITY && ConfigManager.getSupportedEntities().contains(object)) || (type == ExplosionType.BLOCK && ConfigManager.getSupportedBlocks().contains(object))) {
					PlayerSettings settings = null;
					if(playerManager != null)
						settings = playerManager.getPlayerSettings(type, object.toString());
					Player player = Bukkit.getPlayer(uuid);
					if(command == TypeCommand.PARTICLE) {
						HashMap<String, Inventory> map = null;
						for(ParticleType pType : ParticleType.values()) {
							if(pSubCatParticles.containsKey(pType))
								map = pSubCatParticles.get(pType);
							else
								map = new HashMap<>();
							int partLength = 0;
							if(pType == ParticleType.VANILLA) {
								partLength = Particle.values().length;
								for(Particle p : Particle.values()) {
									if(p.getDataType() != Void.class)
										partLength--;
									else if(playerManager != null && !player.hasPermission("tntregen.command.rparticle." + type.toString().toLowerCase() + "." + object.toString().toLowerCase() + ".particle." + p.toString().toLowerCase()))
										partLength--;
								}
							} else if(pType == ParticleType.PRESET) {
								partLength = ParticlePresetManager.getPresetParticles().size();
								for(ParticlePresetManager pm : ParticlePresetManager.getPresetParticles()) {
									if(playerManager != null && !player.hasPermission("tntregen.command.rparticle." + type.toString().toLowerCase() + "." + object.toString().toLowerCase() + ".particle." + pm.getName().toLowerCase()))
										partLength--;
								}
							}
							int pMaxParticlePage = Integer.parseInt(String.valueOf((partLength / 45d) + 1d).split("\\.")[0]);
							if(String.valueOf((partLength / 45d) + 1d).split("\\.")[1].equals("0"))
								pMaxParticlePage--;
							if(pMaxParticlePage == 0)
								pMaxParticlePage = 1;
							for(int p = 0, c = 0; p < pMaxParticlePage; p++) {
								String pageTitle = cat + " [" + (p+1) + "] | " + ItemCreator.fancyDisplayName(object.toString()).replace(" ", "_") + (playerManager == null ? " §6§l✯" : "");
								Inventory page;
								if(pSubCatParticles.containsKey(pType) && pSubCatParticles.get(pType).containsKey(pageTitle.toLowerCase().replace(" §6§l✯", ""))) {
									page = pSubCatParticles.get(pType).get(pageTitle.toLowerCase().replace(" §6§l✯", ""));
								} else
									page = Bukkit.createInventory(null, 9*6, pageTitle.replace("_", " "));
								ItemStack fws = new ItemCreator(Material.FIREWORK_STAR).setDisplayName(" ").create();
								for(int ps : new int[] {46, 47, 51, 52})
									page.setItem(ps, new ItemCreator(Material.PAPER).setDisplayName(" ").create());
								if(p == 0)
									page.setItem(45, fws);
								else
									page.setItem(45, new ItemCreator(Material.EMERALD).setDisplayName("§a§lPrevious Page").create());
								if(playerManager == null || player.hasPermission("tntregen.command.rparticle." + type.toString().toLowerCase() + "." + object.toString().toLowerCase() + ".presets")) {
									if(pType == ParticleType.VANILLA) {
										page.setItem(49, new ItemCreator(Material.ENDER_PEARL).setDisplayName("§aView Presets").create());
									} else if(pType == ParticleType.PRESET) {										
										page.setItem(49, new ItemCreator(Material.ENDER_EYE).setDisplayName("§aView Particles").create());
									}
								} else
									page.setItem(49, new ItemCreator(Material.PAPER).setDisplayName(" ").create());
								if(p == pMaxParticlePage-1)
									page.setItem(53, fws);
								else
									page.setItem(53, new ItemCreator(Material.EMERALD).setDisplayName("§a§lNext Page").create());
								String[] compassLore;
								if(playerManager != null)
									compassLore = new String[] {"§7blockRegen: §a§l" + playerManager.getPlayerSettings(type, object.toString()).getParticleBlockRegen().toParticleString(), "§7blockToBeRegen: §b§l" + playerManager.getPlayerSettings(type, object.toString()).getParticleBlockToBeRegen().toParticleString()};
								else
									compassLore = new String[] {"§7blockRegen: §a§l" + configManager.getParticleBlockRegen().toParticleString(), "§eLeft click to " + (configManager.isParticleBlockRegenEnable() ? "§cdisable" : "§aenable"), "§7blockToBeRegen: §b§l" + configManager.getParticleBlockToBeRegen().toParticleString(), "§eRight click to " + (configManager.isParticleBlockToBeRegenEnable() ? "§cdisable" : "§aenable")};
								page.setItem(48, new ItemCreator(Material.COMPASS).setDisplayName("§aParticles").setLore(compassLore).create());
								page.setItem(50, new ItemCreator(Material.CLOCK).setDisplayName("§a" + ItemCreator.fancyDisplayName(type.toString())).setLore(new String[] {"§7" + ItemCreator.fancyDisplayName(type.toString()) + ": " + cat.substring(0, 2) + "§l" + ItemCreator.fancyDisplayName(object.toString()), "§eClick to change selected " + type.toString().toLowerCase()}).create());
								for(int part = c; part < 45*(p+1); part++) {
									c++;
									ParticleType particle = ParticleType.valueOf(pType.toString());
									if(pType == ParticleType.PRESET) {
										if(c > ParticlePresetManager.getPresetParticles().size())
											break;
										ParticlePresetManager preset = ParticlePresetManager.getPresetParticles().get(c-1);
										particle.set(preset);
										
									} else if(pType == ParticleType.VANILLA) {
										if(c > Particle.values().length-1)
											break;
										particle.set(Particle.values()[c]);
										if(particle.getParticle().getDataType() != Void.class) {
											part--;
											continue;
										}
									}
									String particleString = particle.toParticleString();
									String fParticleString = particle.toParticleStringFormatted();
									String pBR = playerManager == null ? configManager.getParticleBlockRegen().toParticleString() : settings.getParticleBlockRegen().toParticleString();
									String pBtR = playerManager == null ? configManager.getParticleBlockToBeRegen().toParticleString() : settings.getParticleBlockToBeRegen().toParticleString();
									boolean isEqualToBR = particleString.equals(pBR);
									boolean isEqualToBtR = particleString.equals(pBtR);
									if(playerManager == null || player.hasPermission("tntregen.command.rparticle." + type.toString().toLowerCase() + "." + object.toString().toLowerCase() + ".particle." + particleString)) {
										if(isEqualToBR && isEqualToBtR) {
											page.addItem(new ItemCreator(Material.MAGENTA_DYE).setDisplayName("§d" + fParticleString).setLore(new String[] {"§eCurrently set for blockRegen", "§eCurrently set for blockToBeRegen"}).create());
										} else if(isEqualToBR) {
											page.addItem(new ItemCreator(Material.LIME_DYE).setDisplayName("§a" + fParticleString).setLore(new String[] {"§eCurrently set for blockRegen", "§eRight click to set blockToBeRegen"}).create());
										} else if(isEqualToBtR) {
											page.addItem(new ItemCreator(Material.LIGHT_BLUE_DYE).setDisplayName("§b" + fParticleString).setLore(new String[] {"§eLeft click to set blockRegen", "§eCurrently set for blockToBeRegen"}).create());
										} else {
											page.addItem(new ItemCreator(Material.GRAY_DYE).setDisplayName("§7" + fParticleString).setLore(new String[] {"§eLeft click to set blockRegen", "§eRight click to set blockToBeRegen"}).create());
										}
									}
								}
								map.put(pageTitle.toLowerCase().replace(" §6§l✯", ""), page);
							}
							pSubCatParticles.put(pType, map);
						}
					} else if(command == TypeCommand.SOUND) {
						int soundLength = Sound.values().length;
						for(Sound s : Sound.values()) {
							if(playerManager != null && !player.hasPermission("tntregen.command.rsound." + type.toString().toLowerCase() + "." + object.toString().toLowerCase() + ".sound." + s.toString().toLowerCase()))
								soundLength--;
						}
						int pMaxSoundPage = Integer.parseInt(String.valueOf((soundLength / 45d) + 1d).split("\\.")[0]);
						if(String.valueOf((soundLength / 45d) + 1d).split("\\.")[1].equals("0"))
							pMaxSoundPage--;
						if(pMaxSoundPage == 0)
							pMaxSoundPage = 1;
						for(int s = 0, c = 0; s < pMaxSoundPage; s++) {
							String pageTitle = cat + " [" + (s+1) + "] | " + ItemCreator.fancyDisplayName(object.toString()).replace(" ", "_");
							Inventory page;
							if(pSubCatSounds.containsKey(pageTitle.toLowerCase().replace(" §6§l✯", ""))) {
								page = pSubCatSounds.get(pageTitle.toLowerCase().replace(" §6§l✯", ""));
							} else
								page = Bukkit.createInventory(null, 9*6, cat + " [" + (s+1) + "] | " + ItemCreator.fancyDisplayName(object.toString())  + (playerManager == null ? " §6§l✯" : ""));
							ItemStack fws = new ItemCreator(Material.FIREWORK_STAR).setDisplayName(" ").create();
							for(int ps : new int[] {46, 47, 49, 51, 52})
								page.setItem(ps, new ItemCreator(Material.PAPER).setDisplayName(" ").create());
							if(s == 0)
								page.setItem(45, fws);
							else
								page.setItem(45, new ItemCreator(Material.EMERALD).setDisplayName("§a§lPrevious Page").create());
							if(s == pMaxSoundPage-1)
								page.setItem(53, fws);
							else
								page.setItem(53, new ItemCreator(Material.EMERALD).setDisplayName("§a§lNext Page").create());
							String[] compassLore;
							if(playerManager != null)
								compassLore = new String[] {"§7Sound: §a§l" + ItemCreator.fancyDisplayName(playerManager.getPlayerSettings(type, object.toString()).getSound().toString())};
							else
								compassLore = new String[] {"§7Sound: §a§l" + ItemCreator.fancyDisplayName(configManager.getSound().toString()), "§eMiddle click to " + (configManager.isSoundEnable() ? "§cdisable" : "§aenable"), "§7Volume: §l" + configManager.getSoundVolume(), "§eLeft click to increase volume", "§eRight click to decrease volume", "§7Pitch: §l" + configManager.getSoundPitch(), "§eShift + Left click to increase pitch", "§eShift + Right click to decrease pitch"};					
							page.setItem(48, new ItemCreator(Material.COMPASS).setDisplayName("§aSound").setLore(compassLore).create());
							page.setItem(50, new ItemCreator(Material.CLOCK).setDisplayName("§a" + ItemCreator.fancyDisplayName(type.toString())).setLore(new String[] {"§7" + ItemCreator.fancyDisplayName(type.toString()) + ": " + cat.substring(0, 2) + "§l" + ItemCreator.fancyDisplayName(object.toString()), "§eClick to change selected " + type.toString().toLowerCase()}).create());
							for(int part = c; part < 45*(s+1); part++) {
								c++;
								if(part > Sound.values().length-1)
									break;
								Sound sound = Sound.values()[part];
								ItemCreator item;
								String soundCat = sound.toString().split("_")[0];
								switch(soundCat) {
								case "AMBIENT":
									item = new ItemCreator(Material.MUSIC_DISC_13);
									break;
								case "BLOCK":
									item = new ItemCreator(Material.MUSIC_DISC_BLOCKS);
									break;
								case "ENCHANT":
									item = new ItemCreator(Material.MUSIC_DISC_WAIT);
									break;
								case "ENTITY":
									item = new ItemCreator(Material.MUSIC_DISC_CAT);
									break;
								case "ITEM":
									item = new ItemCreator(Material.MUSIC_DISC_MALL);
									break;
								case "MUSIC":
									item = new ItemCreator(Material.MUSIC_DISC_MELLOHI);
									break;
								case "UI":
									item = new ItemCreator(Material.MUSIC_DISC_STRAD);
									break;
								case "WEATHER":
									item = new ItemCreator(Material.MUSIC_DISC_STAL);
									break;
								default:
									item = new ItemCreator(Material.MUSIC_DISC_11);
									break;
								}
								if(playerManager != null && player.hasPermission("tntregen.command.rsound." + type.toString().toLowerCase() + "." + object.toString().toLowerCase() + ".sound." + sound.toString().toLowerCase())) {
									if(sound.equals(playerManager.getPlayerSettings(type, object.toString().toLowerCase()).getSound())) {
										item = new ItemCreator(Material.JUKEBOX);
										item.setDisplayName("§d" + ItemCreator.fancyDisplayName(sound.toString())).setLore(new String[] {"§eCurrently set"});
									} else {
										item.setDisplayName("§7" + ItemCreator.fancyDisplayName(sound.toString())).setLore(new String[] {"§eClick to set sound"});
									}
									page.addItem(item.create());
								} else if(playerManager == null) {
									if(sound.equals(configManager.getSound())) {
										item = new ItemCreator(Material.JUKEBOX);
										item.setDisplayName("§d" + ItemCreator.fancyDisplayName(sound.toString())).setLore(new String[] {"§eCurrently set"});
									} else {
										item.setDisplayName("§7" + ItemCreator.fancyDisplayName(sound.toString())).setLore(new String[] {"§eClick to set sound"});
									}
									page.addItem(item.create());
								}
							}
							pSubCatSounds.put(pageTitle.toLowerCase().replace(" §6§l✯", ""), page);
						}
					}	
				}
			}
			pSubCategories.putIfAbsent(subTitle.toLowerCase().replace(" §6§l✯", ""), subCategory);
		}
		for(Inventory inv : invs.keySet()) {
			String s = invs.get(inv);
			for(int i = 2; i > (ChatColor.stripColor(s.split(" ")[0]).equals("Particle") ? maxParticlePage : maxSoundPage); i++) {
				if(s.contains(" [" + i + "] | ")) {
					if(inv.getContents().length <= 9)
						invs.remove(inv);
				}
			}
		}
		playerCategories.putIfAbsent(uuid, pCategories);
		playerSubCategories.putIfAbsent(uuid, pSubCategories);
		playerSubCatParticles.putIfAbsent(uuid, pSubCatParticles);
		playerSubCatSounds.putIfAbsent(uuid, pSubCatSounds);
	}
	public static void unregisterInventories() {
		playerCategories.clear();
		playerSubCategories.clear();
		playerSubCatParticles.clear();
		playerSubCatSounds.clear();
	}
	public static void unregisterInventory(UUID uuid) {
		playerCategories.remove(uuid);
		playerSubCategories.remove(uuid);
		playerSubCatParticles.remove(uuid);
		playerSubCatSounds.remove(uuid);
	}
	public static boolean hasInventories(UUID uuid) {
		if(!playerCategories.containsKey(uuid) || !playerSubCategories.containsKey(uuid) || !playerSubCatParticles.containsKey(uuid) || !playerSubCatSounds.containsKey(uuid))
			return false;
		return true;
	}
	public static int getMaxPage(TypeCommand type) {
		return type == TypeCommand.PARTICLE ? maxParticlePage : maxSoundPage;
	}
	public static Inventory getInventoryCategory(UUID uuid, TypeCommand type) {
		return playerCategories.get(uuid).get(type.toString().toLowerCase() + " - select category");
	}
	public static Inventory getSubCategory(UUID uuid, TypeCommand type, ExplosionType explosionType) {
		ChatColor color = explosionType == ExplosionType.ENTITY ? ChatColor.RED : ChatColor.GREEN;
		return playerSubCategories.get(uuid).get(color + type.toString().toLowerCase() + " - select " + explosionType.toString().toLowerCase());
	}
	public static Inventory getSubCatType(UUID uuid, TypeCommand type, ParticleType pType, ExplosionType explosionType, Object object, int page) {
		HashMap<String, Inventory> maps = new HashMap<>();
		maps.putAll(playerSubCatParticles.get(uuid).get(pType == null ? ParticleType.VANILLA : pType));
		maps.putAll(playerSubCatSounds.get(uuid));
		ChatColor color = explosionType == ExplosionType.ENTITY ? ChatColor.RED : ChatColor.GREEN;
		return maps.get(color + type.toString().toLowerCase() + " [" + page + "] | " + object.toString().toLowerCase());
	}
	public enum TypeCommand {
		PARTICLE,
		SOUND;
	}
	private static class ItemCreator {
		private ItemStack item;
		private ItemMeta itemMeta;
		
		public ItemCreator(Material material) {
			this.item = new ItemStack(material);
			this.itemMeta = item.getItemMeta();
		}
		
		public ItemCreator setDisplayName(String name) {
			itemMeta.setDisplayName(name);
			item.setItemMeta(itemMeta);
			return this;
		}
		public static String fancyDisplayName(String name) {
			name = ChatColor.stripColor(name.replace(" ", "_"));
			String newName = "";
			if(name.contains("_")) {
				for(String string : name.split("_")) {
					if(!newName.equals(""))
						newName = newName + " ";
					newName = newName + StringUtils.capitalize(string.toLowerCase()).replace("Tnt", "TnT");
				}
			} else
				newName = StringUtils.capitalize(name.toLowerCase());
			return newName;
		}
		public ItemCreator setLore(String[] lore) {
			List<String> list = new ArrayList<>();
			for(String s : lore)
				list.add(s);
			itemMeta.setLore(list);
			item.setItemMeta(itemMeta);
			return this;
		}
		public ItemStack create() {
			return item;
		}
	}
	public static class InventoryManagerListener implements Listener {
		@EventHandler
		public void onInventory(InventoryClickEvent event) {
			if(event.getWhoClicked() instanceof Player) {
				InventoryView inventory = event.getView();
				int slot = event.getSlot();
				ClickType click = event.getClick();
				ItemStack item = event.getCurrentItem();
				Player he = (Player)event.getWhoClicked();
				PlayerSettingsManager playerManager = null;
				TypeCommand typeCommand = null;
				ExplosionType explosionType = null;
				Object object = null;
				Inventory rInventory = null;
				int page = 0;	
				if((inventory.getTitle().contains("Particle") || inventory.getTitle().contains("Sound"))) {
					event.setCancelled(true);
					if(inventory.getTopInventory().equals(event.getClickedInventory()) && (item != null && item.hasItemMeta())) {
						String itemDisplayNameEnumFixed = ChatColor.stripColor(item.getItemMeta().getDisplayName()).replace(" ", "_").toUpperCase();
						if(!inventory.getTitle().contains("✯"))
							playerManager = new PlayerSettingsManager(he.getUniqueId());
						Player player = playerManager != null ? (Player)event.getWhoClicked() : null;
						UUID uuid = player != null ? player.getUniqueId() : null;
						typeCommand = TypeCommand.valueOf(ChatColor.stripColor(inventory.getTitle().split(" ")[0]).toUpperCase());
						rInventory = getInventoryCategory(uuid, typeCommand);
						if(inventory.getTitle().contains("§c") || inventory.getTitle().contains("§a")) {
							explosionType = inventory.getTitle().substring(0, 2).equals("§c") ? ExplosionType.ENTITY : ExplosionType.BLOCK;
							rInventory = getSubCategory(uuid, typeCommand, explosionType);
							if(inventory.getTitle().contains(" | ")) {
								String o = ChatColor.stripColor(inventory.getTitle().split(" \\| ")[1].toUpperCase()).replace(" ✯", "").replace(" ", "_");
								try {
									object = EntityType.valueOf(o);
								} catch(IllegalArgumentException e) {
									object = Material.valueOf(o);
								}
								page = Integer.parseInt(inventory.getTitle().split("\\[")[1].split("\\]")[0]);
								ParticleType pType = null;
								if(typeCommand == TypeCommand.PARTICLE) {
									if(inventory.getItem(49).getItemMeta().getDisplayName().equals("§aView Presets"))
										pType = ParticleType.VANILLA;
									else if(inventory.getItem(49).getItemMeta().getDisplayName().equals("§aView Particles"))
										pType = ParticleType.PRESET;
								}
								rInventory = getSubCatType(uuid, typeCommand, pType, explosionType, object, page);
							}
						}
						if(typeCommand != null) {
							if(explosionType == null) {
								he.openInventory(getSubCategory(uuid, typeCommand, ExplosionType.valueOf(item.getItemMeta().getDisplayName().substring(2, item.getItemMeta().getDisplayName().length()).replace("Entities", "ENTITY").replace("Blocks", "BLOCK"))));
							} else {
								if(object == null) {
									try {
										object = EntityType.valueOf(itemDisplayNameEnumFixed);
									} catch(IllegalArgumentException e) {
										object = Material.valueOf(itemDisplayNameEnumFixed);
									}
									he.openInventory(getSubCatType(uuid, typeCommand, null, explosionType, object, 1));
								} else {
									ConfigManager configManager = new ConfigManager(explosionType, object.toString());
									PlayerSettings settings = playerManager != null ? playerManager.getPlayerSettings(explosionType, object.toString()) : null;
									if(slot >= 0 && slot <= 44) {
										if(typeCommand == TypeCommand.PARTICLE) {
											ParticleType pType = null;
											if(inventory.getItem(49).getItemMeta().getDisplayName().equals("§aView Presets")) {
												pType = ParticleType.VANILLA;
												pType.set(Particle.valueOf(itemDisplayNameEnumFixed));
											} else if(inventory.getItem(49).getItemMeta().getDisplayName().equals("§aView Particles")) {
												pType = ParticleType.PRESET;
												ParticlePresetManager particle = ParticlePresetManager.getParticlePreset(itemDisplayNameEnumFixed);
												pType.set(particle);
											}
											if(item.getType() == Material.GRAY_DYE) {
												for(ParticleType pT : ParticleType.values()) {
													for(String t : playerSubCatParticles.get(uuid).get(pT).keySet()) {
														Inventory inv = playerSubCatParticles.get(uuid).get(pT).get(t);
														if(t.substring(0, 10).equalsIgnoreCase(inventory.getTitle().substring(0, 10)) && t.split(" \\| ")[1].equalsIgnoreCase(object.toString())) {
															for(int c = 0; c < inv.getContents().length; c++) {
																ItemStack i = inv.getContents()[c];
																if(i != null && i.hasItemMeta()) {
																	String name = ChatColor.stripColor(i.getItemMeta().getDisplayName().replace(" ", "_"));
																	if(i.getType() == Material.MAGENTA_DYE) {
																		if(click == ClickType.LEFT)
																			inv.setItem(c, new ItemCreator(Material.LIGHT_BLUE_DYE).setDisplayName("§b" + ItemCreator.fancyDisplayName(name)).setLore(new String[] {"§eLeft click to set blockRegen", "§eCurrently set for blockToBeRegen"}).create());
																		else if(click == ClickType.RIGHT)
																			inv.setItem(c, new ItemCreator(Material.LIME_DYE).setDisplayName("§a" + ItemCreator.fancyDisplayName(name)).setLore(new String[] {"§eCurrently set for blockRegen", "§eRight click to set blockToBeRegen"}).create());
																	} else if(click == ClickType.LEFT) {
																		if(i.getType() == Material.LIME_DYE)
																			inv.setItem(c, new ItemCreator(Material.GRAY_DYE).setDisplayName("§7" + ItemCreator.fancyDisplayName(name)).setLore(new String[] {"§eLeft click to set blockRegen", "§eRight click to set blockToBeRegen"}).create());
																	} else if(click == ClickType.RIGHT) {
																		if(i.getType() == Material.LIGHT_BLUE_DYE)
																			inv.setItem(c, new ItemCreator(Material.GRAY_DYE).setDisplayName("§7" + ItemCreator.fancyDisplayName(name)).setLore(new String[] {"§eLeft click to set blockRegen", "§eRight click to set blockToBeRegen"}).create());
																	}
																}
															}
														}
													}
												}
												if(click == ClickType.LEFT) {
													rInventory.setItem(slot, new ItemCreator(Material.LIME_DYE).setDisplayName("§a" + pType.toParticleStringFormatted()).setLore(new String[] {"§eCurrently set for blockRegen", "§eRight click to set blockToBeRegen"}).create());
													if(playerManager != null)
														settings.setParticleBlockRegen(pType);
													else
														configManager.setParticleBlockRegen(pType);
												} else if(click == ClickType.RIGHT) {
													rInventory.setItem(slot, new ItemCreator(Material.LIGHT_BLUE_DYE).setDisplayName("§b" + pType.toParticleStringFormatted()).setLore(new String[] {"§eLeft click to set blockRegen", "§eCurrently set for blockToBeRegen"}).create());
													if(playerManager != null)
														settings.setParticleBlockToBeRegen(pType);
													else
														configManager.setParticleBlockToBeRegen(pType);
												}
											} else if(item.getType() == Material.LIME_DYE) {
												if(click == ClickType.RIGHT) {
													for(ParticleType pT : ParticleType.values()) {
														for(String t : playerSubCatParticles.get(uuid).get(pT).keySet()) {
															Inventory inv = playerSubCatParticles.get(uuid).get(pT).get(t);
															if(inventory.getTitle().substring(0, 10).equalsIgnoreCase(t.substring(0, 10)) && t.split(" \\| ")[1].equalsIgnoreCase(object.toString())) {
																for(int c = 0; c < inv.getContents().length; c++) {
																	ItemStack i = inv.getContents()[c];
																	if(i != null) {
																		if(i.getType() == Material.LIGHT_BLUE_DYE)
																			inv.setItem(c, new ItemCreator(Material.GRAY_DYE).setDisplayName("§7" + ItemCreator.fancyDisplayName(ChatColor.stripColor(i.getItemMeta().getDisplayName().replace(" ", "_")))).setLore(new String[] {"§eLeft click to set blockRegen", "§eRight click to set blockToBeRegen"}).create());
																	}
																}
															}
														}		
													}
													rInventory.setItem(slot, new ItemCreator(Material.MAGENTA_DYE).setDisplayName("§d" + pType.toParticleStringFormatted()).setLore(new String[] {"§eCurrently set for blockRegen", "§eCurrently set for blockToBeRegen"}).create());
													if(playerManager != null)
														settings.setParticleBlockToBeRegen(pType);
													else
														configManager.setParticleBlockToBeRegen(pType);
												}
											} else if(item.getType() == Material.LIGHT_BLUE_DYE) {
												if(click == ClickType.LEFT) {
													for(ParticleType pT : ParticleType.values()) {
														for(String t : playerSubCatParticles.get(uuid).get(pT).keySet()) {
															Inventory inv = playerSubCatParticles.get(uuid).get(pT).get(t);
															if(inventory.getTitle().substring(0, 10).equalsIgnoreCase(t.substring(0, 10)) && t.split(" \\| ")[1].equalsIgnoreCase(object.toString())) {
																for(int c = 0; c < inv.getContents().length; c++) {
																	ItemStack i = inv.getContents()[c];
																	if(i != null) {
																		if(i.getType() == Material.LIME_DYE)
																			inv.setItem(c, new ItemCreator(Material.GRAY_DYE).setDisplayName("§7" + ItemCreator.fancyDisplayName(ChatColor.stripColor(i.getItemMeta().getDisplayName().replace(" ", "_")))).setLore(new String[] {"§eLeft click to set blockRegen", "§eRight click to set blockToBeRegen"}).create());
																	}
																}	
															}
														}
													}
													rInventory.setItem(slot, new ItemCreator(Material.MAGENTA_DYE).setDisplayName("§d" + pType.toParticleStringFormatted()).setLore(new String[] {"§eCurrently set for blockRegen", "§eCurrently set for blockToBeRegen"}).create());
													if(playerManager != null)
														settings.setParticleBlockRegen(pType);
													else
														configManager.setParticleBlockRegen(pType);
												}
											}
											for(String t : playerSubCatParticles.get(uuid).get(pType).keySet()) {
												Inventory inv = playerSubCatParticles.get(uuid).get(pType).get(t);
												if(inventory.getTitle().substring(0, 10).equalsIgnoreCase(t.substring(0, 10)) && t.split(" \\| ")[1].equalsIgnoreCase(object.toString())) {
													String[] compassLore;
													if(playerManager != null)
														compassLore = new String[] {"§7blockRegen: §a§l" + settings.getParticleBlockRegen().toParticleString(), "§7blockToBeRegen: §b§l" + settings.getParticleBlockToBeRegen().toParticleString()};
													else
														compassLore = new String[] {"§7blockRegen: §a§l" + configManager.getParticleBlockRegen().toParticleString(), "§eLeft click to " + (configManager.isParticleBlockRegenEnable() ? "§cdisable" : "§aenable"), "§7blockToBeRegen: §b§l" + configManager.getParticleBlockToBeRegen().toParticleString(), "§eRight click to " + (configManager.isParticleBlockToBeRegenEnable() ? "§cdisable" : "§aenable")};
													inv.setItem(48, new ItemCreator(Material.COMPASS).setDisplayName("§aParticles").setLore(compassLore).create());
												}
											}
										} else {
											Sound sound = Sound.valueOf(itemDisplayNameEnumFixed);
											if(item.getType().toString().contains("MUSIC_DISC_")) {
												for(String t : playerSubCatSounds.get(uuid).keySet()) {
													Inventory inv = playerSubCatSounds.get(uuid).get(t);
													if(inventory.getTitle().substring(0, 8).equalsIgnoreCase(t.substring(0, 8)) && t.split(" \\| ")[1].equalsIgnoreCase(object.toString())) {
														for(int c = 0; c < inv.getContents().length; c++) {
															ItemStack i = inv.getContents()[c];
															if(i != null) {
																if(i.getType() == Material.JUKEBOX) {
																	ItemCreator ii;
																	String soundCat = ChatColor.stripColor(i.getItemMeta().getDisplayName().split(" ")[0]).toUpperCase();
																	switch(soundCat) {
																	case "AMBIENT":
																		ii = new ItemCreator(Material.MUSIC_DISC_13);
																		break;
																	case "BLOCK":
																		ii = new ItemCreator(Material.MUSIC_DISC_BLOCKS);
																		break;
																	case "ENCHANT":
																		ii = new ItemCreator(Material.MUSIC_DISC_WAIT);
																		break;
																	case "ENTITY":
																		ii = new ItemCreator(Material.MUSIC_DISC_CAT);
																		break;
																	case "ITEM":
																		ii = new ItemCreator(Material.MUSIC_DISC_MALL);
																		break;
																	case "MUSIC":
																		ii = new ItemCreator(Material.MUSIC_DISC_MELLOHI);
																		break;
																	case "UI":
																		ii = new ItemCreator(Material.MUSIC_DISC_STRAD);
																		break;
																	case "WEATHER":
																		ii = new ItemCreator(Material.MUSIC_DISC_STAL);
																		break;
																	default:
																		ii = new ItemCreator(Material.MUSIC_DISC_11);
																		break;
																	}
																	ii.setDisplayName("§7" + ChatColor.stripColor(ItemCreator.fancyDisplayName(i.getItemMeta().getDisplayName())));
																	inv.setItem(c, ii.create());
																}
															}
														}	
													}
												}
												rInventory.setItem(slot, new ItemCreator(Material.JUKEBOX).setDisplayName("§d" + ItemCreator.fancyDisplayName(sound.toString())).setLore(new String[] {"§eCurrently set"}).create());
												if(playerManager != null)
													settings.setSound(sound);
												else
													configManager.setSound(sound);
											}
											for(String t : playerSubCatSounds.get(uuid).keySet()) {
												Inventory inv = playerSubCatSounds.get(uuid).get(t);
												if(inventory.getTitle().substring(0, 8).equalsIgnoreCase(t.substring(0, 8)) && t.split(" \\| ")[1].equalsIgnoreCase(object.toString())) {
													String[] compassLore;
													if(playerManager != null)
														compassLore = new String[] {"§7Sound: §a§l" + ItemCreator.fancyDisplayName(playerManager.getPlayerSettings(explosionType, object.toString()).getSound().toString())};
													else
														compassLore = new String[] {"§7Sound: §a§l" + ItemCreator.fancyDisplayName(configManager.getSound().toString()), "§eMiddle click to " + (configManager.isSoundEnable() ? "§cdisable" : "§aenable"), "§7Volume: §l" + configManager.getSoundVolume(), "§eLeft click to increase volume", "§eRight click to decrease volume", "§7Pitch: §l" + configManager.getSoundPitch(), "§eShift + Left click to increase pitch", "§eShift + Right click to decrease pitch"};	
													inv.setItem(48, new ItemCreator(Material.COMPASS).setDisplayName("§aSound").setLore(compassLore).create());
												}
											}
											if(playerManager != null)
												he.playSound(he.getLocation(), sound, settings.getSoundVolume(), settings.getSoundPitch());
											else
												he.playSound(he.getLocation(), sound, configManager.getSoundVolume(), configManager.getSoundPitch());
										}
									} else {
										if(item != null) {
											if(item.getItemMeta().getDisplayName().equals("§aView Presets")) {
												he.openInventory(getSubCatType(uuid, typeCommand, ParticleType.PRESET, explosionType, object, 1));
											} else if(item.getItemMeta().getDisplayName().equals("§aView Particles")) {
												he.openInventory(getSubCatType(uuid, typeCommand, ParticleType.VANILLA, explosionType, object, 1));
											} else if(item.getItemMeta().getDisplayName().equals("§a§lPrevious Page")) {
												he.openInventory(getSubCatType(uuid, typeCommand, null, explosionType, object, page - 1));
											} else if(item.getItemMeta().getDisplayName().equals("§a§lNext Page")) {
												he.openInventory(getSubCatType(uuid, typeCommand, null, explosionType, object, page + 1));
											} else if(item.getItemMeta().getDisplayName().equals("§aParticles")) {
												ParticleType pType = ParticleType.VANILLA;
												if(inventory.getTopInventory().getItem(49).getItemMeta().getDisplayName().equals("§aView Particles")) {
													pType = ParticleType.PRESET;
												}
												if(playerManager == null) {
													if(click == ClickType.LEFT) {
														configManager.setParticleBlockRegenEnable(configManager.isParticleBlockRegenEnable() ? false : true);
														for(String t : playerSubCatParticles.get(uuid).get(pType).keySet()) {
															Inventory inv = playerSubCatParticles.get(uuid).get(pType).get(t);
															if(inventory.getTitle().substring(0, 10).equalsIgnoreCase(t.substring(0, 10)) && t.split(" \\| ")[1].equalsIgnoreCase(object.toString())) {
																inv.setItem(slot, new ItemCreator(Material.COMPASS).setDisplayName("§aParticles").setLore(new String[] {"§7blockRegen: §a§l" + configManager.getParticleBlockRegen(), "§eLeft click to " + (configManager.isParticleBlockRegenEnable() ? "§cdisable" : "§aenable"), "§7blockToBeRegen: §b§l" + configManager.getParticleBlockToBeRegen(), "§eRight click to " + (configManager.isParticleBlockToBeRegenEnable() ? "§cdisable" : "§aenable")}).create());
															}
														}
													} else if(click == ClickType.RIGHT) {
														configManager.setParticleBlockToBeRegenEnable(configManager.isParticleBlockToBeRegenEnable() ? false : true);
														for(String t : playerSubCatParticles.get(uuid).get(pType).keySet()) {
															Inventory inv = playerSubCatParticles.get(uuid).get(pType).get(t);
															if(inventory.getTitle().substring(0, 10).equalsIgnoreCase(t.substring(0, 10)) && t.split(" \\| ")[1].equalsIgnoreCase(object.toString())) {
																inv.setItem(slot, new ItemCreator(Material.COMPASS).setDisplayName("§aParticles").setLore(new String[] {"§7blockRegen: §a§l" + configManager.getParticleBlockRegen(), "§eLeft click to " + (configManager.isParticleBlockRegenEnable() ? "§cdisable" : "§aenable"), "§7blockToBeRegen: §b§l" + configManager.getParticleBlockToBeRegen(), "§eRight click to " + (configManager.isParticleBlockToBeRegenEnable() ? "§cdisable" : "§aenable")}).create());
															}
														}
													}
												}
											} else if(item.getItemMeta().getDisplayName().equals("§aEntity") || item.getItemMeta().getDisplayName().equals("§aBlock")) {
												he.openInventory(getSubCategory(uuid, typeCommand, explosionType));
											} else if(item.getItemMeta().getDisplayName().equals("§aSound")) {
												if(playerManager == null) {
													int v0 = Integer.parseInt(String.valueOf(configManager.getSoundVolume()).split("\\.")[0]);
													int v1 = Integer.parseInt(String.valueOf(configManager.getSoundVolume()).split("\\.")[1]);
													int p0 = Integer.parseInt(String.valueOf(configManager.getSoundPitch()).split("\\.")[0]);
													int p1 = Integer.parseInt(String.valueOf(configManager.getSoundPitch()).split("\\.")[1]);
													if(click == ClickType.MIDDLE) {
														configManager.setSoundEnable(configManager.isSoundEnable() ? false : true);
													} else if(click == ClickType.LEFT) {
														configManager.setSoundVolume(v1 == 9 ? Float.parseFloat((v0 + 1) + ".0") : Float.parseFloat(v0 + "." + (v1 + 1)));
													} else if(click == ClickType.RIGHT) {
														configManager.setSoundVolume(configManager.getSoundVolume() > 0.0 ? v1 == 0 ? Float.parseFloat((v0 - 1) + ".9") : Float.parseFloat(v0 + "." + (v1 - 1)) : configManager.getSoundVolume());
													} else if(click == ClickType.SHIFT_LEFT) {
														configManager.setSoundPitch(p1 == 9 ? Float.parseFloat((p0 + 1) + ".0") : Float.parseFloat(p0 + "." + (p1 + 1)));
													} else if(click == ClickType.SHIFT_RIGHT) {
														configManager.setSoundPitch(configManager.getSoundPitch() > 0.0 ? p1 == 0 ? Float.parseFloat((p0 - 1) + ".9") : Float.parseFloat(p0 + "." + (p1 - 1)) : configManager.getSoundPitch());
													}
													for(String t : playerSubCatSounds.get(uuid).keySet()) {
														Inventory inv = playerSubCatSounds.get(uuid).get(t);
														if(inventory.getTitle().substring(0, 10).equalsIgnoreCase(t.substring(0, 10)) && t.split(" \\| ")[1].equalsIgnoreCase(object.toString())) {
															inv.setItem(slot, new ItemCreator(Material.COMPASS).setDisplayName("§aSound").setLore(new String[] {"§7Sound: §a§l" + configManager.getSound(), "§eMiddle click to " + (configManager.isSoundEnable() ? "§cdisable" : "§aenable"), "§7Volume: §l" + configManager.getSoundVolume(), "§eLeft click to increase volume", "§eRight click to decrease volume", "§7Pitch: §l" + configManager.getSoundPitch(), "§eShift + Left click to increase pitch", "§eShift + Right click to decrease pitch"}).create());
															
														}
													}
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
		
	}

}
