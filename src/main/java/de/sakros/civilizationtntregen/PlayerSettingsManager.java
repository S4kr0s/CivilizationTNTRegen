package de.sakros.civilizationtntregen;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;

import de.sakros.civilizationtntregen.Explosion.ExplosionManager.ExplosionType;
import de.sakros.civilizationtntregen.Inventory.InventoryManager.TypeCommand;

public class PlayerSettingsManager {
	private UUID uuid;
	private File file;
	private YamlConfiguration config;
	
	public PlayerSettingsManager(UUID uuid) {
		this.uuid = uuid;
		file = new File(Main.getInstance().getDataFolder() + File.separator + "Players" + File.separator + uuid.toString() + ".yml");
		config = YamlConfiguration.loadConfiguration(file);
	}
	public UUID getUUID() {
		return uuid;
	}
	public void updateFile() {
		if(!hasFile()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		boolean csave = false;
		LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
		ArrayList<String> list = new ArrayList<>();
		for(EntityType type : ConfigManager.getSupportedEntities())
			list.add("entity." + type.toString().toLowerCase());
		for(Material material : ConfigManager.getSupportedBlocks())
			list.add("block." + material.toString().toLowerCase());
		for(String type : list) {
			ConfigManager configManager = new ConfigManager(ExplosionType.valueOf(type.split("\\.")[0].toUpperCase()), type.split("\\.")[1]);
			map.put(type + ".particles.blockRegen.particle", configManager.getParticleBlockRegen().toParticleString().toLowerCase());
			map.put(type + ".particles.blockRegen.enable", configManager.isParticleBlockRegenEnable());
			map.put(type + ".particles.blockToBeRegen.particle", configManager.getParticleBlockToBeRegen().toParticleString().toLowerCase());
			map.put(type + ".particles.blockToBeRegen.enable", configManager.isParticleBlockToBeRegenEnable());
			map.put(type + ".sound.enable", configManager.isSoundEnable());
			map.put(type + ".sound.sound", configManager.getSound().toString().toLowerCase());
			map.put(type + ".sound.volume", configManager.getSoundVolume());
			map.put(type + ".sound.pitch", configManager.getSoundPitch());
		}
		for(String key : new ArrayList<>(map.keySet())) {
			Object value = map.get(key);
			if(!config.contains(key)) {config.set(key, value); csave = true;}
			if(key.contains(".particle") && (config.getString(key).equalsIgnoreCase(ParticleType.VANILLA.toString().toLowerCase())) || config.getString(key).equalsIgnoreCase(ParticleType.PRESET.toString().toLowerCase())) {config.set(key, value); csave = true;}
			map.remove(key);
		}	
		
		if(csave == true) {	
			try {
				config.save(file);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	private boolean hasFile() {
		return file.exists();
	}
	public List<EntityType> getConfigurableEntities(TypeCommand command) {
		List<EntityType> list = new ArrayList<>();
		if(Bukkit.getOfflinePlayer(uuid).isOnline()) {
			for(EntityType type : ConfigManager.getSupportedEntities()) {
				if(Bukkit.getPlayer(uuid).hasPermission("tntregen.command.r" + command.toString().toLowerCase() + ".entity." + type.toString().toLowerCase())) {
					list.add(type);
				} else {
					if(command == TypeCommand.PARTICLE) {
						for(Particle particle : Particle.values()) {
							if(particle.getDataType() == Void.class) {
								if(Bukkit.getPlayer(uuid).hasPermission("tntregen.command.r" + command.toString().toLowerCase() + ".entity." + type.toString().toLowerCase() + ".particle." + particle.toString().toLowerCase())) {
									list.add(type);
									break;
								}
							}
						}	
					} else if(command == TypeCommand.SOUND) {
						for(Sound sound: Sound.values()) {
							if(Bukkit.getPlayer(uuid).hasPermission("tntregen.command.r" + command.toString().toLowerCase() + ".entity." + type.toString().toLowerCase() + ".sound." + sound.toString().toLowerCase())) {
								list.add(type);
								break;
							}
						}
					}
				}
			}
		}
		return list;
	}
	public List<Material> getConfigurableBlock(TypeCommand command) {
		List<Material> list = new ArrayList<>();
		if(Bukkit.getOfflinePlayer(uuid).isOnline()) {
			for(Material material : ConfigManager.getSupportedBlocks()) {
				if(Bukkit.getPlayer(uuid).hasPermission("tntregen.command.r" + command.toString().toLowerCase() + ".block." + material.toString().toLowerCase())) {
					list.add(material);
				} else {
					if(command == TypeCommand.PARTICLE) {
						for(Particle particle : Particle.values()) {
							if(particle.getDataType() == Void.class) {
								if(Bukkit.getPlayer(uuid).hasPermission("tntregen.command.r" + command.toString().toLowerCase() + ".entity." + material.toString().toLowerCase() + ".particle." + particle.toString().toLowerCase())) {
									list.add(material);
									break;
								}
							}
						}	
					} else if(command == TypeCommand.SOUND) {
						for(Sound sound: Sound.values()) {
							if(Bukkit.getPlayer(uuid).hasPermission("tntregen.command.r" + command.toString().toLowerCase() + ".entity." + material.toString().toLowerCase() + ".sound." + sound.toString().toLowerCase())) {
								list.add(material);
								break;
							}
						}
					}
				}
			}
		}
		return list;
	}
	public PlayerSettings getPlayerSettings(ExplosionType type, String subType) {
		return new PlayerSettings(type, subType);
	}
	
	public class PlayerSettings {
		ExplosionType type;
		String subType;
		public PlayerSettings(ExplosionType type, String subType) {
			this.type = type;
			this.subType = subType != null ? subType.toLowerCase() : null;
		}
		public boolean isParticleBlockRegenEnable() {
			return config.getBoolean(type.toString().toLowerCase() + "." + subType + ".particles.blockRegen.enable");
		}
		public void setParticleBlockRegenEnable(boolean value) {
			config.set(type.toString().toLowerCase() + "." + subType + ".particles.blockRegen.enable", value);
			try {
				config.save(file);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		public void setParticleBlockToBeRegenEnable(boolean value) {
			config.set(type.toString().toLowerCase() + "." + subType + ".particles.blockToBeRegen.enable", value);
			try {
				config.save(file);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		public boolean isParticleBlockToBeRegenEnable() {
			return config.getBoolean(type.toString().toLowerCase() + "." + subType + ".particles.blockToBeRegen.enable");
		}
		public ParticleType getParticleBlockRegen() {
			if(subType == null)
				return ParticleType.getParticleType(config.getString(type.toString().toLowerCase() + ".default.particles.blockRegen.particle").toUpperCase());
			return ParticleType.getParticleType(config.getString(type.toString().toLowerCase() + "." + subType + ".particles.blockRegen.particle").toUpperCase());
		}
		public void setParticleBlockRegen(ParticleType particle) {
			config.set(type.toString().toLowerCase() + "." + subType + ".particles.blockRegen.particle", particle.toParticleString().replace(" ", "_").toLowerCase());
			try {
				config.save(file);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		public void setParticleBlockRegen(Particle particle) {
			config.set(type.toString().toLowerCase() + "." + subType + ".particles.blockRegen.particle", particle.toString().toLowerCase());
			try {
				config.save(file);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		public ParticleType getParticleBlockToBeRegen() {
			if(subType == null)
				return ParticleType.getParticleType(config.getString(type.toString().toLowerCase() + ".default.particles.blockToBeRegen.particle").toUpperCase());
			return ParticleType.getParticleType(config.getString(type.toString().toLowerCase() + "." + subType + ".particles.blockToBeRegen.particle").toUpperCase());
			
		}
		public void setParticleBlockToBeRegen(ParticleType particle) {
			config.set(type.toString().toLowerCase() + "." + subType + ".particles.blockToBeRegen.particle", particle.toParticleString().replace(" ", "_").toLowerCase());
			try {
				config.save(file);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		public void setParticleBlockToBeRegen(Particle particle) {
			config.set(type.toString().toLowerCase() + "." + subType + ".particles.blockToBeRegen.particle", particle.toString().toLowerCase());
			try {
				config.save(file);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		public boolean isSoundEnable() {
			return config.getBoolean(type.toString().toLowerCase() + "." + subType + ".sound.enable");
		}
		public void setSoundEnable(boolean value) {
			config.set(type.toString().toLowerCase() + "." + subType + ".sound.enable", value);
			try {
				config.save(file);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		public Sound getSound() {
			return Sound.valueOf(config.getString(type.toString().toLowerCase() + "." + subType + ".sound.sound").toUpperCase());	
		}
		public void setSound(Sound sound) {
			config.set(type.toString().toLowerCase() + "." + subType + ".sound.sound", sound.toString().toLowerCase());
			try {
				config.save(file);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		public float getSoundVolume() {
			return Float.valueOf(config.getString(type.toString().toLowerCase() + "." + subType + ".sound.volume"));
		}
		public void setSoundVolume(float value) {
			config.set(type.toString().toLowerCase() + "." + subType + ".sound.volume", value);
			try {
				config.save(file);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		public float getSoundPitch() {
			return Float.valueOf(config.getString(type.toString().toLowerCase() + "." + subType + ".sound.pitch"));
		}
		public void setSoundPitch(float value) {
			config.set(type.toString().toLowerCase() + "." + subType + ".sound.pitch", value);
			try {
				config.save(file);
			} catch (IOException e) {
				e.printStackTrace();
			}	
		}
	}

	
}
