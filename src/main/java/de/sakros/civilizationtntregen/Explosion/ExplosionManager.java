package de.sakros.civilizationtntregen.Explosion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.Bisected.Half;
import org.bukkit.block.data.MultipleFacing;
import org.bukkit.block.data.type.Bed;
import org.bukkit.block.data.type.Bed.Part;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import de.sakros.civilizationtntregen.BlocksFile;
import de.sakros.civilizationtntregen.ConfigManager;
import de.sakros.civilizationtntregen.Main;
import de.sakros.civilizationtntregen.ParticlePresetManager;
import de.sakros.civilizationtntregen.ParticlePresetManager.ParticleRegenType;
import de.sakros.civilizationtntregen.ParticleType;
import de.sakros.civilizationtntregen.PlayerSettingsManager;
import de.sakros.civilizationtntregen.PlayerSettingsManager.PlayerSettings;
import de.sakros.civilizationtntregen.Inventory.InventoryManager.TypeCommand;

import net.coreprotect.CoreProtectAPI;

public class ExplosionManager {

	private String subType;
	private Location location;
	private ExplosionType explosionType;
	private List<BlockManager> blocks = new ArrayList<>();
	static LinkedList<ExplosionManager> explosions = new LinkedList<>();
	static ConfigManager configManager;
	static HashMap<Location, Integer> blocksDurability = new HashMap<>();
	public ExplosionManager(ExplosionType type, String subType, Location location) {
		this.explosionType = type;
		this.subType = subType.toLowerCase();
		this.location = location;
		explosions.add(this);
		configManager = new ConfigManager(type, subType);
	}
	public void addBlocks(List<BlockManager> blocks) {
		for(BlockManager block : blocks)
			addBlock(block);
	}
	public void addBlock(BlockManager block) {
		addBlock(block, false);
	}
	public void addBlock(BlockManager block, boolean preappend) {
		if(block.getType() == Material.AIR || block.getType() == Material.CAVE_AIR) 
			return;
		if(blocks.isEmpty()) {
			this.blocks.add(block);
			return;
		}
		for(BlockManager blocks : blocks) {
			if(blocks.getLocation().equals(block.getLocation()))
				return;
		}
		if(preappend)
			this.blocks.add(0, block);
		else
			this.blocks.add(block);
	}
	public static List<ExplosionManager> getExplosions() {
		return explosions;
	}
	public static List<ExplosionManager> getExplosions(EntityType type) {
		List<ExplosionManager> list = new ArrayList<>();
		for(ExplosionManager explosion : new ArrayList<>(explosions)) {
			if(explosion.getSubType() != null)
				if(explosion.getSubType().equals(type.toString().toLowerCase()))
					list.add(explosion);
		}
		return list;
	}
	public static List<ExplosionManager> getExplosions(List<EntityType> types) {
		if(types == null)
			return getExplosions();
		List<ExplosionManager> list = new ArrayList<>();
		for(ExplosionManager explosion : new ArrayList<>(getExplosions())) {
			for(EntityType type : types)
				if(explosion.getSubType() != null)
					if(explosion.getSubType().equals(type.toString().toLowerCase()))
						list.add(explosion);
		}
		return list;

	}
	public static List<ExplosionManager> getExplosions(List<EntityType> types, Location location, double radius) {
		if(types == null)
			return getExplosions(location, radius);
		if(location == null || radius == -1)
			return getExplosions(types);
		List<ExplosionManager> list = new ArrayList<>();
		for(ExplosionManager explosion : new ArrayList<>(getExplosions(location, radius))) {
			for(EntityType type : types)
				if(explosion.getSubType() != null)
					if(explosion.getSubType().equals(type.toString().toLowerCase())) {
						list.add(explosion);
					}
		}
		return list;
	}
	public static List<ExplosionManager> getExplosions(Location location, double radius) {
		if(location == null || radius == -1)
			return getExplosions();
		List<ExplosionManager> list = new ArrayList<>();
		for(ExplosionManager explosion : new ArrayList<>(explosions)) {
			if(explosion.getLocation().getWorld().equals(location.getWorld())) {
				if(radius >= location.distance(explosion.getLocation())) {
					list.add(explosion);
				}
			}
		}
		return list;
	}
	public static boolean adjustDurability(Location location, int max, int amount) {
		if(!blocksDurability.containsKey(location)) {
			blocksDurability.put(location, max - amount);
		} else {
			blocksDurability.put(location, blocksDurability.get(location) - amount);
		}
		if(blocksDurability.get(location) <= 0) {
			blocksDurability.remove(location);
			return false;
		}
		return true;
	}
	public static void removeDurability(Location location) {
		if(blocksDurability.containsKey(location))
			blocksDurability.remove(location);
	}
	public static void clearBlocksDurability() {
		blocksDurability.clear();
	}
	public List<BlockManager> getBlocks() {
		return blocks;
	}
	public Location getLocation() {
		return location;
	}
	public String getSubType() {
		return subType;
	}
	public ExplosionType getExplosionType() {
		return explosionType;
	}
	public BlockManager getBlockFromLocation(Location location) {
		for(BlockManager block : blocks) {
			if(block.getLocation().equals(location))
				return block;
		}
		return null;
	}
	public static void regenerateAll() {
		for(ExplosionManager explosion : new ArrayList<>(explosions)) {
			explosion.regenerate();
		}
	}
	public static void regenerateAll(EntityType type) {
		for(ExplosionManager explosion : new ArrayList<>(getExplosions(type))) {
			explosion.regenerate();
		}
	}
	public static void regenerateAll(List<EntityType> types) {
		for(ExplosionManager explosion : new ArrayList<>(getExplosions(types, null, -1))) {
			explosion.regenerate();
		}
	}
	public static void regenerateAll(List<EntityType> types, Location location, double radius) {
		for(ExplosionManager explosion : new ArrayList<>(getExplosions(types, location, radius)))
			explosion.regenerate();
	}
	public void regenerate() {
		for(BlockManager block : new ArrayList<>(blocks)) {
			rrun(block);
		}
		explosions.remove(this);
	}
	public void regenerate(int delay, int period, boolean instant) {
		new BukkitRunnable() {
			@Override
			public void run() {
				if(blocks.isEmpty())
					this.cancel();
				else {
					if(configManager.isParticleBlockToBeRegenEnable()) {
						Random r = new Random();
						BlockManager b = blocks.get(r.nextInt(blocks.size()));
						if(ConfigManager.isPlayerSettingsEnabled()) {
							for(Player player : Bukkit.getOnlinePlayers()) {
								PlayerSettingsManager playerManager = new PlayerSettingsManager(player.getUniqueId());
								PlayerSettings settings = playerManager.getPlayerSettings(getExplosionType(), getSubType());
								if(player.hasPermission("tntregen.command.rparticle." + getExplosionType().toString().toLowerCase() + "." + getSubType().toLowerCase())
										|| ((getExplosionType() == ExplosionType.ENTITY && !playerManager.getConfigurableEntities(TypeCommand.PARTICLE).isEmpty()))
										|| (getExplosionType() == ExplosionType.BLOCK && !playerManager.getConfigurableEntities(TypeCommand.PARTICLE).isEmpty())) {
									if(settings.isParticleBlockToBeRegenEnable()) {
										if(settings.getParticleBlockToBeRegen() == ParticleType.VANILLA) {
											player.spawnParticle(settings.getParticleBlockToBeRegen().getParticle(), b.getLocation(), 5, 0.5, 0.5, 0.5, 0);
										} else if(settings.getParticleBlockToBeRegen() == ParticleType.PRESET) {
											ParticlePresetManager pManager = settings.getParticleBlockToBeRegen().getParticlePreset();
											pManager.spawnParticles(ParticleRegenType.PRE, player, b.getLocation().getWorld(), b.getLocation(), ExplosionManager.this);
										}
									}
								} else {
									if(configManager.getParticleBlockToBeRegen() == ParticleType.VANILLA) {
										player.spawnParticle(configManager.getParticleBlockToBeRegen().getParticle(), b.getLocation(), 5, 0.5, 0.5, 0.5, 0);
									} else if(configManager.getParticleBlockToBeRegen() == ParticleType.PRESET) {
										ParticlePresetManager pManager = configManager.getParticleBlockToBeRegen().getParticlePreset();
										pManager.spawnParticles(ParticleRegenType.PRE, player, b.getLocation().getWorld(), b.getLocation(), ExplosionManager.this);
									}
								}
							}	
						} else {
							if(configManager.getParticleBlockToBeRegen() == ParticleType.VANILLA) {
								b.getLocation().getWorld().spawnParticle(configManager.getParticleBlockToBeRegen().getParticle(), b.getLocation(), 5, 0.5, 0.5, 0.5, 0);
							} else if(configManager.getParticleBlockToBeRegen() == ParticleType.PRESET) {
								ParticlePresetManager pManager = configManager.getParticleBlockToBeRegen().getParticlePreset();
								pManager.spawnParticles(ParticleRegenType.PRE, null, b.getLocation().getWorld(), b.getLocation(), ExplosionManager.this);
							}
						}
					}
				}
			}
		}.runTaskTimer(Main.getInstance(), 0, 1);
		if(instant) {
			ExplosionManager exp = this;
			new BukkitRunnable() {

				@Override
				public void run() {
					for(BlockManager block : new ArrayList<BlockManager>(blocks)) {
						rrun(block);
					}
					explosions.remove(exp);
				}
				
			}.runTaskLater(Main.getInstance(), delay);
		} else {
			new BukkitRunnable() {
				
				@Override
				public void run() {
					if(blocks.isEmpty()) {
						explosions.remove(ExplosionManager.this);
						this.cancel();
						return;
					}
					BlockManager block = blocks.get(blocks.size() - 1);
					for(Material ground : BlockManager.getBlocksRequiredGround()) {
						if(block.getType().equals(ground)) {
							if(!block.getBlockBelow(1).getType().isSolid()) {
								blocks.remove(block);
								blocks.add(0, block);
								for(BlockManager b : blocks) {
									if(b.getLocation().getBlockX() == block.getLocation().getBlockX() && b.getLocation().getBlockY() == (block.getLocation().getBlockY()-1) && b.getLocation().getBlockZ() == b.getLocation().getBlockZ())
									block = b;
								}
							}
						}
					}
					rrun(block);
				}
			}.runTaskTimer(Main.getInstance(), delay, period);
		}
	}
	public void rrun(BlockManager block) {
		if(block.getType() == Material.VINE) {
			boolean hasSupport = false;
			for(BlockFace face : ((MultipleFacing)block.getState().getBlockData()).getFaces()) {
				int x = 0;
				int z = 0;
				int y = 0;
				switch(face) {
				case EAST:
					x = 1; break;
				case NORTH:
					z = -1; break;
				case WEST:
					x = -1; break;
				case SOUTH:
					z = 1; break;
				case UP:
					y = 1;
				default: break;
				}
				if(getBlockFromLocation(block.getLocation().add(x, y, z)) != null) {
					if(getBlockFromLocation(block.getLocation().add(x, y, z)).getState().getType().isSolid()) {
						hasSupport = true;
					}
				}
			}
			if(!hasSupport) {
				blocks.remove(block);
				return;
			}
		}
		if(block.getBlock().getType().hasGravity() || BlocksFile.isLegalBlock(block.getLocation().getBlock().getType())) {
			regen(block);
		} else {
			if(ConfigManager.getForceBlockToRegen()) {
				if(block.isContainer()) {
					Container container = (Container)block.getState();
					for(ItemStack item : container.getInventory().getContents())
						if(item != null)
							block.getLocation().getWorld().dropItemNaturally(block.getLocation(), item);
					container.getInventory().clear();
				}
				block.getLocation().getWorld().dropItemNaturally(block.getLocation(), new ItemStack(block.getType()));
				regen(block);
			} else {
				if(block.getBlock().getType() != Material.AIR) {
					BlockState save = block.getBlock().getState();
					if(save instanceof Container) {
						Container container = (Container)block.getBlock().getState();
						container.getInventory().clear();
					}
					block.getBlock().setType(Material.AIR, false);
					regen(block);
					block.getBlock().breakNaturally();
					save.update(true);
				} else {
					regen(block);					
				}
			}
		}
	}
	private void regen(BlockManager block) {
		if(block.getBlock().getType().hasGravity()) {
			if(ConfigManager.isShiftGravityUpEnable()) {
				for(int i = ConfigManager.getMaxShiftGravityUp(); i > 0; i--) {
					if(block.getBlockAbove(i).getType().hasGravity()) {
						if(BlocksFile.isLegalBlock(block.getBlockAbove(i+1).getType())) {
							block.getBlockAbove(i+1).setBlockData(block.getBlockAbove(i).getBlockData());
						}
					}
				}
				block.getBlockAbove(1).setBlockData(block.getBlock().getBlockData());
			}
		}
		block.getState().update(true);
		CoreProtectAPI coreProtect = Main.getInstance().getCoreProtect();
		if(coreProtect != null)
			coreProtect.logPlacement("#tntregen", block.getLocation(), block.getType(), block.getBlock().getBlockData());
		blocks.remove(block);
		if(configManager.isParticleBlockRegenEnable()) {
			if(ConfigManager.isPlayerSettingsEnabled()) {
				for(Player player : Bukkit.getOnlinePlayers()) {
					PlayerSettingsManager playerManager = new PlayerSettingsManager(player.getUniqueId());
					PlayerSettings settings = playerManager.getPlayerSettings(getExplosionType(), getSubType());
					if(player.hasPermission("tntregen.command.rparticle." + getExplosionType().toString().toLowerCase() + "." + getSubType().toLowerCase())
						|| ((getExplosionType() == ExplosionType.ENTITY && !playerManager.getConfigurableEntities(TypeCommand.PARTICLE).isEmpty()))
						|| (getExplosionType() == ExplosionType.BLOCK && !playerManager.getConfigurableEntities(TypeCommand.PARTICLE).isEmpty())) {
						if(settings.isParticleBlockRegenEnable()) {
							if(settings.getParticleBlockRegen() == ParticleType.VANILLA) {
								player.spawnParticle(settings.getParticleBlockRegen().getParticle(), block.getLocation(), 3, 1, 1, 1);
							} else if(settings.getParticleBlockRegen() == ParticleType.PRESET) {
								ParticlePresetManager pManager = settings.getParticleBlockRegen().getParticlePreset();
								pManager.spawnParticles(ParticleRegenType.POST, player, block.getLocation().getWorld(), block.getLocation(), ExplosionManager.this);
							}
						}
					} else {
						if(configManager.getParticleBlockRegen() == ParticleType.VANILLA) {
							player.spawnParticle(configManager.getParticleBlockRegen().getParticle(), block.getLocation(), 3, 1, 1, 1);
						} else if(configManager.getParticleBlockRegen() == ParticleType.PRESET) {
							ParticlePresetManager pManager = configManager.getParticleBlockRegen().getParticlePreset();
							pManager.spawnParticles(ParticleRegenType.POST, player, block.getLocation().getWorld(), block.getLocation(), ExplosionManager.this);
						}
					}
				}	
			} else {
				if(configManager.getParticleBlockRegen() == ParticleType.VANILLA)
					block.getLocation().getWorld().spawnParticle(configManager.getParticleBlockRegen().getParticle(), block.getLocation(), 3, 1, 1, 1);
				else if(configManager.getParticleBlockRegen() == ParticleType.PRESET) {
					ParticlePresetManager pManager = configManager.getParticleBlockRegen().getParticlePreset();
					pManager.spawnParticles(ParticleRegenType.POST, null, block.getLocation().getWorld(), block.getLocation(), ExplosionManager.this);
				}
			}
		}
		if(configManager.isSoundEnable()) {
			if(ConfigManager.isPlayerSettingsEnabled()) {
				for(Player player : Bukkit.getOnlinePlayers()) {
					PlayerSettingsManager playerManager = new PlayerSettingsManager(player.getUniqueId());
					PlayerSettings settings = playerManager.getPlayerSettings(getExplosionType(), getSubType());
					if(player.hasPermission("tntregen.command.rsound." + getExplosionType().toString().toLowerCase() + "." + getSubType().toLowerCase())
							|| ((getExplosionType() == ExplosionType.ENTITY && !playerManager.getConfigurableEntities(TypeCommand.SOUND).isEmpty()))
							|| (getExplosionType() == ExplosionType.BLOCK && !playerManager.getConfigurableEntities(TypeCommand.SOUND).isEmpty())) {
						if(settings.isSoundEnable())
							player.playSound(block.getLocation(), settings.getSound(), settings.getSoundVolume(), settings.getSoundPitch());
					} else
						player.playSound(block.getLocation(), configManager.getSound(), configManager.getSoundVolume(), configManager.getSoundPitch());
				}
			} else
				block.getLocation().getWorld().playSound(block.getLocation(), configManager.getSound(), configManager.getSoundVolume(), configManager.getSoundPitch());
		}
		if(block.getBlock().getBlockData() instanceof Bisected) {
			Bisected b = (Bisected)block.getState().getBlockData();
			if(b.getHalf() == Half.TOP) {
				if(getBlockFromLocation(block.getLocation().add(0, -1, 0)) != null)
						regen(getBlockFromLocation(block.getLocation().add(0, -1, 0)));
			} else {
				if(getBlockFromLocation(block.getLocation().add(0, 1, 0)) != null)
					regen(getBlockFromLocation(block.getLocation().add(0, 1, 0)));
			}
		} else if(block.getBlock().getBlockData() instanceof Bed) {
			Bed b = (Bed)block.getBlock().getBlockData();
			if(b.getPart() == Part.FOOT) {
				if(b.getFacing() == BlockFace.EAST)
					if(getBlockFromLocation(block.getLocation().add(1, 0, 0)) != null)
						regen(getBlockFromLocation(block.getLocation().add(1, 0, 0)));
				else if(b.getFacing() == BlockFace.WEST)
					if(getBlockFromLocation(block.getLocation().add(-1, 0, 0)) != null)
						regen(getBlockFromLocation(block.getLocation().add(-1, 0, 0)));
				else if(b.getFacing() == BlockFace.SOUTH)
					if(getBlockFromLocation(block.getLocation().add(0, 0, 1)) != null)
						regen(getBlockFromLocation(block.getLocation().add(0, 0, 1)));
				else if(b.getFacing() == BlockFace.NORTH)
					if(getBlockFromLocation(block.getLocation().add(0, 0, -1)) != null)
						regen(getBlockFromLocation(block.getLocation().add(0, 0, -1)));
			} else {
				if(b.getFacing() == BlockFace.EAST)
					if(getBlockFromLocation(block.getLocation().add(-1, 0, 0)) != null)
						regen(getBlockFromLocation(block.getLocation().add(-1, 0, 0)));
				else if(b.getFacing() == BlockFace.WEST)
					if(getBlockFromLocation(block.getLocation().add(1, 0, 0)) != null)
						regen(getBlockFromLocation(block.getLocation().add(1, 0, 0)));
				else if(b.getFacing() == BlockFace.SOUTH)
					if(getBlockFromLocation(block.getLocation().add(0, 0, -1)) != null)
						regen(getBlockFromLocation(block.getLocation().add(0, 0, -1)));
				else if(b.getFacing() == BlockFace.NORTH)
					if(getBlockFromLocation(block.getLocation().add(0, 0, 1)) != null)
						regen(getBlockFromLocation(block.getLocation().add(0, 0, 1)));
			}
		}
	}
	public boolean canRegenerate() {
		if(subType != null && !subType.equalsIgnoreCase("air")) {
			if(!ConfigManager.containsExplosionTypeSubType(explosionType, subType)) {
				Bukkit.getConsoleSender().sendMessage(StringUtils.capitalize(explosionType.toString().toLowerCase()) + "Type " + subType + " is not in the config. Adding support for this " + explosionType.toString().toLowerCase() + ".");
				ConfigManager.addType(explosionType, subType);
			}
		}
		if(configManager.isRegenEnable()) {
			for(String worlds : configManager.getTriggerWorlds()) {
				if(location.getWorld().getName().equals(worlds)) {
					if(location.getY() >= (double)configManager.getTrigger(worlds + ".minY") && location.getY() <= (double)configManager.getTrigger(worlds + ".maxY")) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public static enum ExplosionType {
		ENTITY,
		BLOCK;
	}
	
}
