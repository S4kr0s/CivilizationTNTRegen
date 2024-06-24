package de.sakros.civilizationtntregen;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Explosive;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.ExplosiveMinecart;

import de.sakros.civilizationtntregen.Explosion.BlockManager;
import de.sakros.civilizationtntregen.Explosion.ExplosionManager.ExplosionType;

public class ConfigManager {
	private static YamlConfiguration config = YamlConfiguration.loadConfiguration(new File(Main.getInstance().getDataFolder() + File.separator + "config.yml"));
	
	public static void reloadConfig() {
		config = YamlConfiguration.loadConfiguration(new File(Main.getInstance().getDataFolder() + File.separator + "config.yml"));
		updateConfig();
		BlockManager.blockSection = YamlConfiguration.loadConfiguration(new File(Main.getInstance().getDataFolder() + File.separator + "blocks.yml"));
	}
	public static void updateConfig() {
		if(!Main.getInstance().getDataFolder().exists()) {
			Main.getInstance().getDataFolder().mkdirs();
		}
		if(!new File(Main.getInstance().getDataFolder() + File.separator + "config.yml").exists())
			try {
				new File(Main.getInstance().getDataFolder() + File.separator + "config.yml").createNewFile();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		boolean csave = false;
		LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
		addType(ExplosionType.ENTITY, null, map);
		addType(ExplosionType.BLOCK, null, map);
		for(String key : new ArrayList<>(map.keySet())) {
			Object value = map.get(key);
			if(!config.contains(key)) {config.set(key, value); csave = true; map.remove(key);}
		}
		for(EntityType type : EntityType.values()) {
			if(type.getEntityClass() != null) {
				if(Explosive.class.isAssignableFrom(type.getEntityClass()) || ExplosiveMinecart.class.isAssignableFrom(type.getEntityClass()) || Creeper.class.isAssignableFrom(type.getEntityClass()) || EnderCrystal.class.isAssignableFrom(type.getEntityClass())) {
					addType(ExplosionType.ENTITY, type.toString(), map);
				}
			}
		}
		for(Material bed : Material.values()) {
			if(bed.toString().contains("_BED"))
				addType(ExplosionType.BLOCK, bed.toString(), map);
		}

		map.put("other.enablePlugin", true);
		map.put("other.forceBlockToRegen", false);
		map.put("other.griefPreventionPluginAllowExplosionRegen", false);
		map.put("other.gravity.shiftGravityUp", true);
		map.put("other.gravity.maxShiftGravityUp", 5);
		map.put("other.NoPermMsg", "&c[TnTRegen] You do not have permission to use this command!");
		map.put("other.enablePlayerSettings", false);
		map.put("other.showDiscordLink", true);
		for(String key : new ArrayList<>(map.keySet())) {
			Object value = map.get(key);
			if(!config.contains(key)) {config.set(key, value); csave = true; map.remove(key);}
		}	
		
		if(csave == true) {	
			try {
				config.save(new File(Main.getInstance().getDataFolder() + File.separator + "config.yml"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if(!new File(Main.getInstance().getDataFolder() + File.separator + "particles.yml").exists())
			Main.getInstance().saveResource("particles.yml", false);
		if(isPlayerSettingsEnabled()) {
			Main.getInstance().getServer().getLogger().log(Level.INFO, "------------------------------");
			Main.getInstance().getServer().getLogger().log(Level.INFO, "PlayerSettings is enabled.");
			Main.getInstance().getServer().getLogger().log(Level.INFO, "------------------------------");
			if(!new File(Main.getInstance().getDataFolder() + File.separator + "Players").exists())
				new File(Main.getInstance().getDataFolder() + File.separator + "Players").mkdir();
			for(Player player : Bukkit.getOnlinePlayers()) {
				new PlayerSettingsManager(player.getUniqueId()).updateFile();
			}
		}
	}
	public static boolean isPlayerSettingsEnabled() {
		return config.getBoolean("other.enablePlayerSettings");
	}
	private static void addType(ExplosionType type, String subType, LinkedHashMap<String, Object> map) {
		if(subType == null)
			subType = "default";
		else
			subType = subType.toLowerCase();
		ConfigurationSection section = config.getConfigurationSection(type.toString().toLowerCase() + ".default");
		map.put(type.toString().toLowerCase() + "." + subType + ".enable", (section != null && section.get("enable") != null) ? section.get("enable") : true);
		map.put(type.toString().toLowerCase() + "." + subType + ".delay", (section != null && section.get("delay") != null) ? section.get("delay") : 1200);
		map.put(type.toString().toLowerCase() + "." + subType + ".period", (section != null && section.get("period") != null) ? section.get("period") : 20);
		map.put(type.toString().toLowerCase() + "." + subType + ".instantRegen", (section != null && section.get("instantRegen") != null) ? section.get("instantRegen") : false);
		map.put(type.toString().toLowerCase() + "." + subType + ".disableBlockDamage", (section != null && section.get("disableBlockDamage") != null) ? section.get("disableBlockDamage") : false);
		map.put(type.toString().toLowerCase() + "." + subType + ".blockDamage", (section != null && section.get("blockDamage") != null) ? section.get("blockDamage") : 1);
		map.put(type.toString().toLowerCase() + "." + subType + ".checkRadius", (section != null && section.get("checkRadius") != null) ? section.get("checkRadius") : 5);
		map.put(type.toString().toLowerCase() + "." + subType + ".particles.blockRegen.particle", (section != null && section.get("particles.blockRegen.particle") != null) ? section.get("particles.blockRegen.particle") : Particle.HEART.toString().toLowerCase());
		map.put(type.toString().toLowerCase() + "." + subType + ".particles.blockRegen.enable", (section != null && section.get("particles.blockRegen.enable") != null) ? section.get("particles.blockRegen.enable") : true);
		map.put(type.toString().toLowerCase() + "." + subType + ".particles.blockToBeRegen.particle", (section != null && section.get("particles.blockToBeRegen.particle") != null) ? section.get("particles.blockToBeRegen.particle") : Particle.FLAME.toString().toLowerCase());
		map.put(type.toString().toLowerCase() + "." + subType + ".particles.blockToBeRegen.enable", (section != null && section.get("particles.blockToBeRegen.enable") != null) ? section.get("particles.blockToBeRegen.enable") : true);
		map.put(type.toString().toLowerCase() + "." + subType + ".sound.enable", (section != null && section.get("sound.enable") != null) ? section.get("sound.enable") : true);
		map.put(type.toString().toLowerCase() + "." + subType + ".sound.sound", (section != null && section.get("sound.sound") != null) ? section.get("sound.sound") : Sound.BLOCK_GRASS_PLACE.toString().toLowerCase());
		map.put(type.toString().toLowerCase() + "." + subType + ".sound.volume", (section != null && section.get("sound.volume") != null) ? section.get("sound.volume") : 1.0);
		map.put(type.toString().toLowerCase() + "." + subType + ".sound.pitch", (section != null && section.get("sound.pitch") != null) ? section.get("sound.pitch") : 2.0);
		if(subType.equals("default")) {
			map.put(type.toString().toLowerCase() + "." + subType + ".triggers.defaultWorld.minY", 0.0);
			map.put(type.toString().toLowerCase() + "." + subType + ".triggers.defaultWorld.maxY", 256.0);	
			if(!Bukkit.getWorlds().isEmpty()) {
				for(int i = 0; i < Bukkit.getWorlds().size(); i++) {
					World world = Bukkit.getWorlds().get(i);
					map.put(type.toString().toLowerCase() + "." + subType + ".triggers." + world.getName() + ".minY", 0.0);
					map.put(type.toString().toLowerCase() + "." + subType + ".triggers." + world.getName() + ".maxY", 256.0);
				}
			}
		} else {
			for(int i = 0; i < Bukkit.getWorlds().size(); i++) {
				World world = Bukkit.getWorlds().get(i);
				if(config.contains(type.toString().toLowerCase() + ".default.triggers." + world.getName() + ".minY"))
					map.put(type.toString().toLowerCase() + "." + subType + ".triggers." + world.getName() + ".minY", config.getDouble(type.toString().toLowerCase() + ".default.triggers." + world.getName() + ".minY"));
				else
					map.put(type.toString().toLowerCase() + "." + subType + ".triggers." + world.getName() + ".minY", config.getDouble(type.toString().toLowerCase() + ".default.triggers.defaultWorld.minY"));
				if(config.contains(type.toString().toLowerCase() + ".default.triggers." + world.getName() + ".maxY"))
					map.put(type.toString().toLowerCase() + "." + subType + ".triggers." + world.getName() + ".maxY", config.getDouble(type.toString().toLowerCase() + ".default.triggers." + world.getName() + ".maxY"));
				else
					map.put(type.toString().toLowerCase() + "." + subType + ".triggers." + world.getName() + ".maxY", config.getDouble(type.toString().toLowerCase() + ".default.triggers.defaultWorld.maxY"));
			}
		}
	}
	public static void addType(ExplosionType type, String subType) {
		subType = subType.toLowerCase();
		boolean csave = false;
		LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
		addType(type, subType, map);
		for(String key : new ArrayList<>(map.keySet())) {
			Object value = map.get(key);
			if(!config.contains(key)) {config.set(key, value); csave = true; map.remove(key);}
		}
		if(csave == true) {	
			try {
				config.save(new File(Main.getInstance().getDataFolder() + File.separator + "config.yml"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	public static LinkedList<EntityType> getSupportedEntities() {
		LinkedList<EntityType> types = new LinkedList<>();
		for(String s : config.getConfigurationSection("entity").getKeys(false))
			if(!s.equals("default"))
				try {
					types.add(EntityType.valueOf(s.toUpperCase()));
				} catch(IllegalArgumentException e) {}
		return types;
	}
	public static LinkedList<Material> getSupportedBlocks() {
		LinkedList<Material> types = new LinkedList<>();
		for(String s : config.getConfigurationSection("block").getKeys(false))
			if(!s.equals("default"))
				try {
					types.add(Material.valueOf(s.toUpperCase()));
				} catch(IllegalArgumentException e) {}
		return types;
	}
	public static boolean isPluginEnable() {
		return config.getBoolean("other.enablePlugin");
	}
	public static boolean containsExplosionTypeSubType(ExplosionType type, String subType) {
		return config.isConfigurationSection(type.toString().toLowerCase() + "." + subType.toLowerCase());
	}
	public static String getNoPermMessage() {
		return config.getString("other.NoPermMsg").replace("&", "�");
	}
	public static boolean getForceBlockToRegen() {
		return config.getBoolean("other.forceBlockToRegen");
	}
	public static boolean isShiftGravityUpEnable() {
		return config.getBoolean("other.gravity.shiftGravityUp");
	}
	public static int getMaxShiftGravityUp() {
		return config.getInt("other.gravity.maxShiftGravityUp");
	}
	public static boolean doesGriefPreventionPluginAllowExplosionRegen() {
		return config.getBoolean("other.griefPreventionPluginAllowExplosionRegen");
	}
	public static boolean showDiscordLink() {
		return config.getBoolean("other.showDiscordLink");
	}
	ExplosionType type;
	String subType;
	public ConfigManager(ExplosionType type, String subType) {
		this.type = type;
		this.subType = subType != null ? subType.toLowerCase() : null;
	}
	public ExplosionType getExplosionType() {
		return type;
	}
	public boolean isBlockDamageDisable() {
		return config.getBoolean(type.toString().toLowerCase() + "." + subType.toLowerCase() + ".disableBlockDamage");
	}
	public boolean isInstantRegenEnable() {
		return config.getBoolean(type.toString().toLowerCase() + "." + subType + ".instantRegen");
	}
	public boolean isRegenEnable() {
		return config.getBoolean(type.toString().toLowerCase() + "." + subType + ".enable");
	}
	public int getRegenPeriod() {
		return config.getInt(type.toString().toLowerCase() + "." + subType + ".period");
	}
	public int getRegenDelay() {
		return config.getInt(type.toString().toLowerCase() + "." + subType + ".delay");
	}
	public boolean isParticleBlockRegenEnable() {
		return config.getBoolean(type.toString().toLowerCase() + "." + subType + ".particles.blockRegen.enable");
	}
	public void setParticleBlockRegenEnable(boolean value) {
		config.set(type.toString().toLowerCase() + "." + subType + ".particles.blockRegen.enable", value);
		try {
			config.save(new File(Main.getInstance().getDataFolder() + File.separator + "config.yml"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void setParticleBlockToBeRegenEnable(boolean value) {
		config.set(type.toString().toLowerCase() + "." + subType + ".particles.blockToBeRegen.enable", value);
		try {
			config.save(new File(Main.getInstance().getDataFolder() + File.separator + "config.yml"));
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
			config.save(new File(Main.getInstance().getDataFolder() + File.separator + "config.yml"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void setParticleBlockRegen(Particle particle) {
		config.set(type.toString().toLowerCase() + "." + subType + ".particles.blockRegen.particle", particle.toString().toLowerCase());
		try {
			config.save(new File(Main.getInstance().getDataFolder() + File.separator + "config.yml"));
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
			config.save(new File(Main.getInstance().getDataFolder() + File.separator + "config.yml"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void setParticleBlockToBeRegen(Particle particle) {
		config.set(type.toString().toLowerCase() + "." + subType + ".particles.blockToBeRegen.particle", particle.toString().toLowerCase());
		try {
			config.save(new File(Main.getInstance().getDataFolder() + File.separator + "config.yml"));
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
			config.save(new File(Main.getInstance().getDataFolder() + File.separator + "config.yml"));
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
			config.save(new File(Main.getInstance().getDataFolder() + File.separator + "config.yml"));
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
			config.save(new File(Main.getInstance().getDataFolder() + File.separator + "config.yml"));
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
			config.save(new File(Main.getInstance().getDataFolder() + File.separator + "config.yml"));
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
	public Object getTrigger(String key) {
		return config.get(type.toString().toLowerCase() + "." + subType + ".triggers." + key);
	}
	public Set<String> getTriggerWorlds() {
		return config.getConfigurationSection(type.toString().toLowerCase() + "." + subType + ".triggers").getKeys(false);
	}
	public int getBlockDamage() {
		return config.getInt(type.toString().toLowerCase() + "." + subType + ".blockDamage");
	}
	public int getExplosionCheckRadius() {
		return config.getInt(type.toString().toLowerCase() + "." + subType + ".checkRadius");
	}
	
}
