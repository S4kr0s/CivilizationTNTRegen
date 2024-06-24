package de.sakros.civilizationtntregen;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFertilizeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import de.sakros.civilizationtntregen.Explosion.ExplosionManager;
import de.sakros.civilizationtntregen.Inventory.InventoryManager;

public class PlayerJoinLeaveListener implements Listener {
	
	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		if(ConfigManager.showDiscordLink()) {
			if(player.isOp() || (player.hasPermission("tntregen.command.rconfig") && player.hasPermission("tntregen.command.rregen") && player.hasPermission("tntregen.command.rexplode"))) {
				player.sendMessage("�7[�cTnTRegen�7] �9Join this plugin's Discord Server to recieve update announcements, submit any bugs, ideas, or feedback, if you have any issues with the plugin, or get access to early builds. �n�lhttp://discord.gg/ucyZdQU");
			}
		}
		if(ConfigManager.isPlayerSettingsEnabled())
			new PlayerSettingsManager(player.getUniqueId()).updateFile();
	}
	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		if(ConfigManager.isPlayerSettingsEnabled())
			InventoryManager.unregisterInventory(event.getPlayer().getUniqueId());
	}
	@EventHandler
	public void onKick(PlayerKickEvent event) {
		if(ConfigManager.isPlayerSettingsEnabled())
			InventoryManager.unregisterInventory(event.getPlayer().getUniqueId());
	}
	
	@EventHandler
	public void blockDestroyed(BlockBreakEvent event) {
		ExplosionManager.removeDurability(event.getBlock().getLocation());
	}
	@EventHandler
	public void blockBurned(BlockBurnEvent event) {
		ExplosionManager.removeDurability(event.getBlock().getLocation());
	}
	@EventHandler
	public void blockFade(BlockFadeEvent event) {
		ExplosionManager.removeDurability(event.getBlock().getLocation());
	}
	@EventHandler
	public void blockFertilize(BlockFertilizeEvent event) {
		ExplosionManager.removeDurability(event.getBlock().getLocation());
	}
	@EventHandler
	public void blockForm(BlockFormEvent event) {
		ExplosionManager.removeDurability(event.getBlock().getLocation());
	}
	@EventHandler
	public void blockGrown(BlockGrowEvent event) {
		ExplosionManager.removeDurability(event.getBlock().getLocation());
	}
	@EventHandler
	public void blockPiston(BlockPistonExtendEvent event) {
		for(Block block : event.getBlocks())
			ExplosionManager.removeDurability(block.getLocation());
	}
	@EventHandler
	public void blockPiston(BlockPistonRetractEvent event) {
		for(Block block : event.getBlocks())
			ExplosionManager.removeDurability(block.getLocation());
	}
	@EventHandler
	public void blockLeaveDecay(LeavesDecayEvent event) {
		ExplosionManager.removeDurability(event.getBlock().getLocation());
	}
	@EventHandler
	public void blockForm(EntityBlockFormEvent event) {
		ExplosionManager.removeDurability(event.getBlock().getLocation());
	}

}
