package de.sakros.civilizationtntregen;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import de.sakros.civilizationtntregen.Explosion.ExplosionManager;
import com.google.common.collect.Iterables;

public class ParticlePresetManager {
	private static YamlConfiguration config = YamlConfiguration.loadConfiguration(new File(Main.getInstance().getDataFolder() + File.separator + "particles.yml"));
	
	public static void reloadConfig() {
		config = YamlConfiguration.loadConfiguration(new File(Main.getInstance().getDataFolder() + File.separator + "particles.yml"));
	}
	public static boolean doesParticlePresetExist(String name) {
		return config.isConfigurationSection(name.toLowerCase());
	}
	public static List<ParticlePresetManager> getPresetParticles() {
		List<ParticlePresetManager> list = new ArrayList<>();
		for(String key : config.getKeys(false)) {
			list.add(new ParticlePresetManager(key));
		}
		return list;
	}
	public static ParticlePresetManager getParticlePreset(String name) {
		if(config.isConfigurationSection(name.toLowerCase()))
			return new ParticlePresetManager(name);
		return null;
	}
	private String name;
	private ParticlePresetManager(String name) {
		this.name = name.toLowerCase();
	}
	public String getName() {
		return name;
	}
	public String getFormattedName() {
		return config.getString(name + ".displayName");
	}
	public void spawnParticles(ParticleRegenType regenType, Player player, World world, Location location, ExplosionManager explosion) {
		spawnParticles(regenType, player, world, location.getX(), location.getY(), location.getZ(), explosion);
	}
	public void spawnParticles(ParticleRegenType regenType, Player player, World world, double x, double y, double z, ExplosionManager explosion) {
		Location location = new Location(world, x, y, z);
		if(hasRegenType(regenType)) {
			for(ParticleData particle : getParticles(regenType)) {
				Location loc = null;
				if(particle.getSpawnOnType() == SpawnOnType.BLOCKS_LOCATION)
					loc = location.clone();
				else if(particle.getSpawnOnType() == SpawnOnType.EXPLOSION_LOCATION)
					loc = explosion.getLocation().clone();
				else if(particle.getSpawnOnType() == SpawnOnType.BLOCK_NEXT_LOCATION)
					loc = Iterables.getLast(explosion.getBlocks()).getLocation();
				loc = new Location(loc.getWorld(), loc.getBlockX() + 0.5, loc.getBlockY(), loc.getBlockZ() + 0.5);
				if(player == null)
					world.spawnParticle(particle.getParticle(), loc, particle.getCount(), particle.getOffsetX(), particle.getOffsetY(), particle.getOffsetZ(), particle.getExtra(), particle.getData());
				else
					player.spawnParticle(particle.getParticle(), loc, particle.getCount(), particle.getOffsetX(), particle.getOffsetY(), particle.getOffsetZ(), particle.getExtra(), particle.getData());
			}
		}
	}
	public boolean hasRegenType(ParticleRegenType regenType) {
		return config.isConfigurationSection(name + "." + regenType.toAltName());
	}
	public List<ParticleData> getParticles(ParticleRegenType regenType) {
		List<ParticleData> particles = new ArrayList<>();
		ConfigurationSection section = config.getConfigurationSection(name + "." + regenType.toAltName() + ".particles");
		Map<String, Object> map = section.getValues(false);
		for(String key : map.keySet()) {
			if(map.get(key) == null) {
				try {
					particles.add(new ParticleData(Particle.valueOf(key.toUpperCase())));
				} catch(IllegalArgumentException e) {};
			} else {
				int amount = NumberUtils.toInt(section.getString(key + ".amount"), 1);
				double offsetX = NumberUtils.toDouble(section.getString(key + ".offset.x"), 0.0);
				double offsetY = NumberUtils.toDouble(section.getString(key + ".offset.y"), 0.0);
				double offsetZ = NumberUtils.toDouble(section.getString(key + ".offset.z"), 0.0);
				double extra = NumberUtils.toDouble(section.getString(key + ".extra"), 0.0);
				Class<?> data = null;
				try {
					for(int i = 0; i < (section.getInt(key + ".count") <= 0 ? 1 : section.getInt(key + ".count")); i++) {
						ParticleData particle = new ParticleData(Particle.valueOf(key.toUpperCase()), amount, offsetX, offsetY, offsetZ, extra, data);
						particle.setCount(section.getInt(key + ".count"));
						if(section.contains(key + ".spawnOn"))
							particle.setSpawnOnType(SpawnOnType.valueOf(section.getString(key + ".spawnOn").toUpperCase().replace("NEXT", "BLOCK_NEXT") + "_LOCATION"));
						if(particle.getParticle().getDataType() == DustOptions.class) {
							Color color = Color.BLACK;
							float size = 1f;
							if(section.contains(key + ".option.color")) {
								String c = section.getString(key + ".option.color");
								if(c.contains(",")) {
									int red = 0, green = 0, blue = 0;
									for(int ii = 0; ii < c.split(",", 3).length; ii++) {
										String s = c.split(",", 3)[ii];
										try {
											int v = Integer.parseInt(s);
											switch(ii) {
											case 0:
												red = v;
											case 1:
												green = v;
											case 2:
												blue = v;
											}
										} catch(NumberFormatException e) {}
									}
									color = Color.fromRGB(red, green, blue);
								} else if(NumberUtils.isDigits(c)) {
									color = Color.fromRGB(Integer.parseInt(c));
								} else {
									try {
										color = (Color)Color.class.getField(c.toUpperCase()).get(null);
									} catch(IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {}
								}	
							}
							if(section.contains(key + ".option.size")) {
								size = NumberUtils.toFloat(section.getString(key + ".option.size"));
							}
							DustOptions dust = new DustOptions(color, size);
							particle.setData(dust);
						}
						particles.add(particle);		
					}
				} catch(IllegalArgumentException e) {}
			}
		}
		return particles;
	}
	public ParticleType toParticleType() {
		return ParticleType.getParticleType(getName());
	}
	public enum SpawnOnType {
		EXPLOSION_LOCATION,
		BLOCKS_LOCATION,
		BLOCK_NEXT_LOCATION;
	}
	public enum ParticleRegenType {
		POST("blockRegen"),
		PRE("blockToBeRegen");
		
		private String altName;
		private ParticleRegenType(String altName) {
			this.altName = altName;
		}
		public String toAltName() {
			return altName;
		}
	}
	public class ParticleData {
		private Particle particle;
		private int amount = 1;
		private double offsetX = 0;
		private double offsetY = 0;
		private double offsetZ = 0;
		private double extra = 0.0;
		private Object data = null;
		private int count = 1;
		private SpawnOnType spawnType = SpawnOnType.BLOCKS_LOCATION;
		private ParticleData(Particle particle) {
			this.particle = particle;
		}
		private ParticleData(Particle particle, int amount) {
			this(particle);
			this.amount = amount;
		}
		private ParticleData(Particle particle, int amount, Class<?> data) {
			this(particle, amount);
			this.data = data;
		}
		private ParticleData(Particle particle, int amount, double offsetX, double offsetY, double offsetZ) {
			this(particle, amount);
			this.offsetX = offsetX;
			this.offsetY = offsetY;
			this.offsetZ = offsetZ;
		}
		private ParticleData(Particle particle, int amount, double offsetX, double offsetY, double offsetZ, double extra) {
			this(particle, amount, offsetX, offsetY, offsetZ);
			this.extra = extra;
		}
		private ParticleData(Particle particle, int amount, double offsetX, double offsetY, double offsetZ, Class<?> data) {
			this(particle, amount, offsetX, offsetY, offsetZ);
			this.data = data;
		}
		private ParticleData(Particle particle, int amount, double offsetX, double offsetY, double offsetZ, double extra, Class<?> data) {
			this(particle, amount, offsetX, offsetY, offsetZ, extra);
			this.data = data;
		}
		public Particle getParticle() {
			return particle;
		}
		public int getAmount() {
			return amount;
		}
		public int setAmount(int amount) {
			this.amount = amount;
			return this.amount;
		}
		public double getOffsetX() {
			return offsetX;
		}
		public double setOffsetX(double offset) {
			this.offsetX = offset;
			return offsetX;
		}
		public double getOffsetY() {
			return offsetY;
		}
		public double setOffsetY(double offset) {
			this.offsetY = offset;
			return offsetY;
		}
		public double getOffsetZ() {
			return offsetZ;
		}
		public double setOffsetZ(double offset) {
			this.offsetZ = offset;
			return offsetZ;
		}
		public double getExtra() {
			return extra;
		}
		public double setExtra(double extra) {
			this.extra = extra;
			return extra;
		}
		public Object getData() {
			return data;
		}
		public Object setData(Object data) {
			this.data = data;
			return this.data;
		}
		public int getCount() {
			return count;
		}
		public int setCount(int count) {
			this.count = count;
			return this.count;
		}
		public SpawnOnType getSpawnOnType() {
			return spawnType;
			//return SpawnOnType.valueOf(config.getString(name + ".particles." + getParticle().toString().toLowerCase() + ".spawnOn").toUpperCase() + "_LOCATION");
		}
		public SpawnOnType setSpawnOnType(SpawnOnType spawnType) {
			this.spawnType = spawnType;
			return this.spawnType;
		}
	}
}
