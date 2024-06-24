package de.sakros.civilizationtntregen;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Particle;

public enum ParticleType {
	VANILLA,
	PRESET;
	
	public static ParticleType getParticleType(String particle) {
		for(Particle p : Particle.values()) {
			if(p.toString().equalsIgnoreCase(particle))
				return VANILLA.fromString(particle);
		}
		if(ParticlePresetManager.doesParticlePresetExist(particle))
			return PRESET.fromString(particle);
		return null;
	}
	private Object object;
	private ParticleType fromString(String particle) {
		if(this == VANILLA) {
			object = Particle.valueOf(particle.toUpperCase());
		} else if(this == PRESET) {
			object = ParticlePresetManager.getParticlePreset(particle);
		}
		return this;
	}
	public Particle getParticle() {
		if(object != null && object instanceof Particle) {
			return (Particle)object;
		}
		return null;
	}
	public ParticlePresetManager getParticlePreset() {
		if(object != null && object instanceof ParticlePresetManager) {
			return (ParticlePresetManager)object;
		}
		return null;
	}
	public void set(Object object) {
		if(object instanceof Particle || object instanceof ParticlePresetManager) {
			this.object = object;
		}
	}
	public String toParticleString() {
		if(object != null) {
			if(object instanceof Particle)
				return ((Particle)object).toString();
			else if(object instanceof ParticlePresetManager)
				return ((ParticlePresetManager)object).getName().toUpperCase();
			else
				return toString();
		} else 
			return toString();
	}
	public String toParticleStringFormatted() {
		if(object != null) {
			if(object instanceof Particle)
				return fancyDisplayName(((Particle)object).toString());
			else if(object instanceof ParticlePresetManager)
				return ((ParticlePresetManager)object).getFormattedName();
			else
				return toString();
		} else 
			return toString();
	}
	private String fancyDisplayName(String name) {
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
	
}
