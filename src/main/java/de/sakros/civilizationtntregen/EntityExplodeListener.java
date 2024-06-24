package de.sakros.civilizationtntregen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Container;
import org.bukkit.block.ShulkerBox;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.Bisected.Half;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.Bed;
import org.bukkit.block.data.type.Bed.Part;
import org.bukkit.block.data.type.Piston;
import org.bukkit.block.data.type.PistonHead;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import de.sakros.civilizationtntregen.Explosion.BlockManager;
import de.sakros.civilizationtntregen.Explosion.ExplosionManager;
import de.sakros.civilizationtntregen.Explosion.ExplosionManager.ExplosionType;

public class EntityExplodeListener implements Listener {
	private static Material eMaterial = Material.AIR;
	private static HashMap<BlockManager, Integer> sortByValues(HashMap<BlockManager, Integer> map) { 
	       List<Entry<BlockManager, Integer>> list = new LinkedList<>(map.entrySet());
	       Collections.sort(list, new Comparator<Entry<BlockManager, Integer>>() {
	            public int compare(Entry<BlockManager, Integer> o1, Entry<BlockManager, Integer> o2) {
	            	return ((Comparable<Integer>) ((Map.Entry<BlockManager, Integer>) (o2)).getValue()).compareTo(((Map.Entry<BlockManager, Integer>) (o1)).getValue());
	            }
	       });
	       HashMap<BlockManager, Integer> sortedHashMap = new LinkedHashMap<>();
	       for (Iterator<Entry<BlockManager, Integer>> it = list.iterator(); it.hasNext();) {
	              Entry<BlockManager, Integer> entry = it.next();
	              sortedHashMap.put(entry.getKey(), entry.getValue());
	       } 
	       return sortedHashMap;
	}
	@EventHandler(priority=EventPriority.MONITOR)
	public void onExplode(EntityExplodeEvent event) {
		explode(event);
	}
	@EventHandler(priority=EventPriority.MONITOR)
	public void onExplode(BlockExplodeEvent event) {
		explode(event);
	}
	@EventHandler
	public void onInteractBed(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		if(event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			Block block = event.getClickedBlock();
			if(player.getWorld().getEnvironment() == Environment.NETHER || player.getWorld().getEnvironment() == Environment.THE_END) {
				if(block.getType().toString().contains("_BED")) {
					eMaterial = block.getType();
				}
			}
		}
	}
	
	public void explode(Event e) {
		Cancellable event = (Cancellable) e;
		ExplosionType explosionType;
		String subType;
		if(e instanceof EntityExplodeEvent) {
			explosionType = ExplosionType.ENTITY;
			subType = ((EntityExplodeEvent)e).getEntityType().toString().toLowerCase();
		} else if(e instanceof BlockExplodeEvent) {
			explosionType = ExplosionType.BLOCK;
			if(eMaterial != Material.AIR) {
				subType = eMaterial.toString().toLowerCase();
			} else
				subType = "default";
		} else {
			return;
		}
		Location eLoc = e instanceof EntityExplodeEvent ? ((EntityExplodeEvent)e).getLocation() : ((BlockExplodeEvent)e).getBlock().getLocation();
		List<Block> eBlockList = e instanceof EntityExplodeEvent ? ((EntityExplodeEvent)e).blockList() : ((BlockExplodeEvent)e).blockList();
		if(!event.isCancelled()) {
			ExplosionManager explosion = new ExplosionManager(explosionType, subType, eLoc);
			ConfigManager configManager = new ConfigManager(explosionType, subType);
			
			if(explosion.canRegenerate()) {
				int radius = configManager.getExplosionCheckRadius();
			    for (int x = radius * -1; x <= radius; x++)
			        for (int y = radius * -1; y <= radius; y++)
			          for (int z = radius * -1; z <= radius; z++) {
			        	  Block block = eLoc.getWorld().getBlockAt(eLoc.getBlockX() + x, eLoc.getBlockY() + y, eLoc.getBlockZ() + z);
			        	  if(block.getType() == Material.ENDER_CHEST || block.getType() == Material.OBSIDIAN || block.getType() == Material.ENCHANTING_TABLE || block.getType() == Material.ANVIL || block.getType() == Material.STRUCTURE_BLOCK || block.getType() == Material.END_PORTAL_FRAME || block.getType() == Material.END_PORTAL || block.getType() == Material.END_GATEWAY || block.getType() == Material.COMMAND_BLOCK || block.getType() == Material.CHAIN_COMMAND_BLOCK || block.getType() == Material.REPEATING_COMMAND_BLOCK || block.getType() == Material.BEDROCK || block.getType() == Material.BARRIER) {
							  eBlockList.add(block);
			        	  }
			          }

				if(configManager.isBlockDamageDisable()) {
					new ArrayList<Block>(eBlockList).forEach(block -> eBlockList.remove(block));
				} else {
					List<BlockManager> blocksMan = new ArrayList<>();
					for(Block block : eBlockList){
						if(Main.landsAddon.isClaimed(block.getLocation())
						|| block.getType() == Material.ENDER_CHEST || block.getType() == Material.OBSIDIAN || block.getType() == Material.ENCHANTING_TABLE || block.getType() == Material.ANVIL || block.getType() == Material.STRUCTURE_BLOCK || block.getType() == Material.END_PORTAL_FRAME || block.getType() == Material.END_PORTAL || block.getType() == Material.END_GATEWAY || block.getType() == Material.COMMAND_BLOCK || block.getType() == Material.CHAIN_COMMAND_BLOCK || block.getType() == Material.REPEATING_COMMAND_BLOCK || block.getType() == Material.BEDROCK || block.getType() == Material.BARRIER){
							blocksMan.add(new BlockManager(block.getState()));
						} else {
							block.breakNaturally();
						}
					}

					HashMap<BlockManager, Integer> newBlockMan = new HashMap<>();
					blocksMan.forEach(block -> newBlockMan.put(block, block.getLocation().getBlockY()));
					Map<BlockManager, Integer> map = sortByValues(newBlockMan);
					List<String> removedList = new ArrayList<>();
					//LinkedList<BlockManager> queueGroundAfter = new LinkedList<>();
					List<BlockManager> blockList = new ArrayList<>(map.keySet());
					for(Iterator<BlockManager> it = new ArrayList<>(blockList).iterator(); it.hasNext();) {
						BlockManager block = it.next();
						for(Material groundMat : BlockManager.getBlocksRequiredGround()) {
							if(block.getLocation().getBlock().getType().equals(groundMat)) {
								blockList.remove(block);
								if(groundMat == Material.CACTUS || groundMat == Material.SUGAR_CANE) {
									for(int i = 0; i <= BlockManager.getMaxRegenHeight(groundMat); i++) {
										if(block.getLocation().add(0, i, 0).getBlock().getType() == Material.CACTUS || block.getLocation().add(0, i, 0).getBlock().getType() == Material.SUGAR_CANE) {
											blockList.add(0, new BlockManager(block.getLocation().add(0, i, 0).getBlock().getState()));
										}
									}
								} else {
									blockList.add(0, new BlockManager(block.getLocation().getBlock().getState()));
								}
							}
							if(block.getLocation().add(0, 1, 0).getBlock().getType().equals(groundMat)) {
								if(getBlockFromLocation(blockList, block.getBlockAbove(1).getLocation()) != null)
									blockList.remove(getBlockFromLocation(blockList, block.getLocation().add(0, 1, 0)));
								if(!eBlockList.contains(block.getBlockAbove(1)))
									eBlockList.add(block.getBlockAbove(1));
								if(groundMat == Material.CACTUS || groundMat == Material.SUGAR_CANE) {
									for(int i = 1; i <= BlockManager.getMaxRegenHeight(groundMat); i++) {
										if(block.getLocation().add(0, i, 0).getBlock().getType() == Material.CACTUS || block.getLocation().add(0, i, 0).getBlock().getType() == Material.SUGAR_CANE) {
											blockList.add(0, new BlockManager(block.getLocation().add(0, i, 0).getBlock().getState()));
										}
									}
								} else {
									blockList.add(0, new BlockManager(block.getLocation().add(0, 1, 0).getBlock().getState()));
								}
							}
						}
					}
					for(BlockManager block : new ArrayList<>(blockList)) {
						if(!(block.getBlock().getBlockData() instanceof Bisected) && !(block.getBlock().getBlockData() instanceof Bed) && !(block.getBlock().getBlockData() instanceof PistonHead) && !(block.getBlock().getBlockData() instanceof Piston) && block.getBlock().getType() != Material.VINE) {
							boolean r = true;
							boolean r2 = true;
							r = preRegen(block, explosion);
							if(!r) {
								removedList.add(block.getBlock().getType().toString().toLowerCase());
								eBlockList.remove(block.getBlock());
							} else {
								if(block.getBlock().getType().toString().contains("SHULKER_BOX") || block.getBlock().getType() == Material.BEACON)
									block.getBlock().setType(Material.AIR);
							}
							if(block.getLocation().add(0, 1, 0).getBlock().getBlockData() instanceof Bisected) {
								for(int i = 1; i <= 2; i++) {
									if(getBlockFromLocation(blockList, block.getLocation().add(0, i, 0)) != null)
										r2 = preRegen(getBlockFromLocation(blockList, block.getLocation().add(0, i, 0)), explosion);
									else
										r2 = preRegen(new BlockManager(block.getLocation().add(0, i, 0).getBlock().getState()), explosion);
									
									if(r2)
										block.getLocation().add(0, i, 0).getBlock().setType(Material.AIR, false);
									else
										if(getBlockFromLocation(blockList, block.getLocation().add(0, i, 0)) != null) {
											removedList.add(block.getLocation().add(0, i, 0).getBlock().getType().toString().toLowerCase());
											eBlockList.remove(block.getLocation().add(0, i, 0).getBlock());
										}
								}
							}
							for(Material groundMat : BlockManager.getBlocksRequiredGround()) {
								if(block.getLocation().add(0, 1, 0).getBlock().getType().equals(groundMat)) {
									if(groundMat == Material.CACTUS || groundMat == Material.SUGAR_CANE) {
										for (int i = 1; i <= BlockManager.getMaxRegenHeight(groundMat); i++) {
											if(block.getLocation().add(0, i, 0).getBlock().getType() == Material.CACTUS || block.getLocation().add(0, i, 0).getBlock().getType() == Material.SUGAR_CANE) {
												if(getBlockFromLocation(blockList, block.getLocation().add(0, i, 0)) != null)
													r2 = preRegen(getBlockFromLocation(blockList, block.getLocation().add(0, i, 0)), explosion);
												else
													r2 = preRegen(new BlockManager(block.getLocation().add(0, i, 0).getBlock().getState()), explosion);
												if(r2)
													if(i == BlockManager.getMaxRegenHeight(groundMat))
														block.getLocation().add(0, i, 0).getBlock().setType(Material.AIR, true);
													else
														block.getLocation().add(0, i, 0).getBlock().setType(Material.AIR, false);
												else
													if(getBlockFromLocation(blockList, block.getLocation().add(0, i, 0)) != null)
														eBlockList.remove(block.getLocation().add(0, i, 0).getBlock());
											} else
												break;
										}
									} else {
										if(getBlockFromLocation(blockList, block.getLocation().add(0, 1, 0)) != null)
											r2 = preRegen(getBlockFromLocation(blockList, block.getLocation().add(0, 1, 0)), explosion);
										else
											r2 = preRegen(new BlockManager(block.getLocation().add(0, 1, 0).getBlock().getState()), explosion);
										if(r2)
											block.getLocation().add(0, 1, 0).getBlock().setType(Material.AIR);
										else
											if(getBlockFromLocation(blockList, block.getLocation().add(0, 1, 0)) != null)
												eBlockList.remove(block.getLocation().add(0, 1, 0).getBlock());
									}
								}	
							}
//							for(Material groundMat : BlockManager.getBlocksRequiredGround()) {
//								if(block.getLocation().getBlock().getType().equals(groundMat)) {
//									if(groundMat == Material.CACTUS || groundMat == Material.SUGAR_CANE) {
//										for(int i = 0; i <= BlockManager.getMaxRegenHeight(groundMat); i++) {
//											if(block.getLocation().add(0, i, 0).getBlock().getType() == Material.CACTUS || block.getLocation().add(0, i, 0).getBlock().getType() == Material.SUGAR_CANE) {
//												if(getBlockFromLocation(blockList, block.getLocation().add(0, i, 0)) != null) {
//													queueGroundAfter.add(getBlockFromLocation(blockList, block.getLocation().add(0, i, 0)));
//													blockList.remove(getBlockFromLocation(blockList, block.getLocation().add(0, i, 0)));
//												} else
//													queueGroundAfter.add(new BlockManager(block.getLocation().add(0, i, 0).getBlock().getState()));
//											}
//										}
//									} else {
//										queueGroundAfter.add(getBlockFromLocation(blockList, block.getLocation()));
//										blockList.remove(getBlockFromLocation(blockList, block.getLocation()));
//									}
//								}
//								if(block.getLocation().add(0, 1, 0).getBlock().getType().equals(groundMat)) {
//									if(groundMat == Material.CACTUS || groundMat == Material.SUGAR_CANE) {
//										for(int i = 1; i <= BlockManager.getMaxRegenHeight(groundMat); i++) {
//											if(block.getLocation().add(0, i, 0).getBlock().getType() == Material.CACTUS || block.getLocation().add(0, i, 0).getBlock().getType() == Material.SUGAR_CANE) {
//												if(getBlockFromLocation(blockList, block.getLocation().add(0, i, 0)) != null) {
//													queueGroundAfter.add(getBlockFromLocation(blockList, block.getLocation().add(0, i, 0)));
//													blockList.remove(getBlockFromLocation(blockList, block.getLocation().add(0, i, 0)));
//												} else
//													queueGroundAfter.add(new BlockManager(block.getLocation().add(0, i, 0).getBlock().getState()));
//											}
//										}
//									} else {
//										if(getBlockFromLocation(blockList, block.getLocation().add(0, 1, 0)) != null) {
//											queueGroundAfter.add(getBlockFromLocation(blockList, block.getLocation().add(0, 1, 0)));
//											blockList.remove(getBlockFromLocation(blockList, block.getLocation().add(0, 1, 0)));
//										} else if(getBlockFromLocation(queueGroundAfter, block.getLocation().add(0, 1, 0)) == null) {
//											queueGroundAfter.add(new BlockManager(block.getLocation().add(0, 1, 0).getBlock().getState()));
//											eBlockList.add(block.getLocation().add(0, 1, 0).getBlock());
//											//block.getLocation().add(0, 1, 0).getBlock().setType(Material.AIR);
//										}
//									}
//								}
//							}
							for(Material wallMat : BlockManager.getBlocksRequiredWall()) {
								for(BlockFace face : new BlockFace[] {BlockFace.EAST, BlockFace.NORTH, BlockFace.WEST, BlockFace.SOUTH}) {
									int x = 0;
									int z = 0;
									switch(face) {
									case EAST:
										x = 1; break;
									case NORTH:
										z = -1; break;
									case WEST:
										x = -1; break;
									case SOUTH:
										z = 1; break;
									default: break;
									}
									if(block.getLocation().add(x, 0, z).getBlock().getType().equals(wallMat)) {
										if(getBlockFromLocation(blockList, block.getLocation().add(x, 0, z)) != null)
											r2 = preRegen(getBlockFromLocation(blockList, block.getLocation().add(x, 0, z)), explosion);
										else
											r2 = preRegen(new BlockManager(block.getLocation().add(x, 0, z).getBlock().getState()), explosion);
										if(r2)
											block.getLocation().add(x, 0, z).getBlock().setType(Material.AIR, false);
										else
											if(getBlockFromLocation(blockList, block.getLocation().add(x, 0, z)) != null) {
												removedList.add(block.getLocation().add(x, 0, z).getBlock().getType().toString().toLowerCase());
												eBlockList.remove(block.getLocation().add(x, 0, z).getBlock());
											}
									}
								}
							}
						} else if(block.getBlock().getBlockData() instanceof Bisected) {
							Half half = ((Bisected)block.getBlock().getBlockData()).getHalf();
							boolean r = true;
							int y = 0;
							switch(half) {
							case BOTTOM:
								y = 1;
								break;
							case TOP:
								y = -1;
								break;
							}
							if(getBlockFromLocation(blockList, block.getLocation().add(0, y, 0)) != null)
								r = preRegen(getBlockFromLocation(blockList, block.getLocation().add(0, y, 0)), explosion);
							else
								r = preRegen(new BlockManager(block.getLocation().add(0, y, 0).getBlock().getState()), explosion);
							if(r)
								block.getLocation().add(0, y, 0).getBlock().setType(Material.AIR, false);
							else
								if(getBlockFromLocation(blockList, block.getLocation().add(0, y, 0)) != null) {
									removedList.add(block.getLocation().add(0, y, 0).getBlock().getType().toString().toLowerCase());
									eBlockList.remove(block.getLocation().add(0, y, 0).getBlock());
								}
							r = preRegen(block, explosion);
							if(r)
								block.getBlock().setType(Material.AIR, false);
							else {
								removedList.add(block.getLocation().getBlock().getType().toString().toLowerCase());
								eBlockList.remove(block.getLocation().getBlock());
							}
						} else if(block.getBlock().getBlockData() instanceof Bed) {
							Bed b = (Bed)block.getBlock().getBlockData();
							Part part = b.getPart();
							BlockFace face = b.getFacing();
							boolean r = true;
							int x = 0, z = 0;
							switch(face) {
							case NORTH:
								z = -1;
								break;
							case EAST:
								x = 1;
								break;
							case SOUTH:
								z = 1;
								break;
							case WEST:
								x = -1;
							default:
								break;
							}
							switch(part) {
							case HEAD:
								if(x != 0)
									x = x == 1 ? x = -1 : 1;
								if(z != 0)
									z = z == 1 ? z = -1 : 1;
								break;
							case FOOT:
								break;
							}
							if(getBlockFromLocation(blockList, block.getLocation().add(x, 0, z)) != null)
								r = preRegen(getBlockFromLocation(blockList, block.getLocation().add(x, 0, z)), explosion);
							else
								r = preRegen(new BlockManager(block.getLocation().add(x, 0, z).getBlock().getState()), explosion);
							if(r)
								block.getLocation().add(x, 0, z).getBlock().setType(Material.AIR, false);
							else
								if(getBlockFromLocation(blockList, block.getLocation().add(x, 0, z)) != null) {
									removedList.add(block.getLocation().add(x, 0, z).getBlock().getType().toString().toLowerCase());
									eBlockList.remove(block.getLocation().add(x, 0, z).getBlock());
								}
							r = preRegen(block, explosion);
							if(r)
								block.getBlock().setType(Material.AIR, false);
							else {
								removedList.add(block.getLocation().getBlock().getType().toString().toLowerCase());
								eBlockList.remove(block.getLocation().getBlock());
							}
						} else if(block.getBlock().getBlockData() instanceof PistonHead || block.getBlock().getBlockData() instanceof Piston) {
							BlockData data = block.getBlock().getBlockData();
							BlockFace face = ((Directional)data).getFacing();
							boolean p = preRegen(block, explosion);
							int x = 0;
							int y = 0;
							int z = 0;
							switch(face) {
							case EAST:
								x = -1;
								break;
							case WEST:
								x = 1;
								break;
							case NORTH:
								z = 1;
								break;
							case SOUTH:
								z = -1;
								break;
							case UP:
								y = -1;
								break;
							case DOWN:
								y = 1;
								break;
							default:
								break;
							}
							if(block.getBlock().getBlockData() instanceof Piston) {
								if(x != 0)
									x = x == 1 ? x = -1 : 1;
								if(y != 0)
									y = y == 1 ? y = -1 : 1;
								if(z != 0)
									z = z == 1 ? z = -1 : 1;
							}
							if(p) {
								if(data instanceof PistonHead) {
									if(block.getLocation().add(x, y, z).getBlock().getBlockData() instanceof Piston) {
										Piston piston = (Piston)block.getLocation().add(x, y, z).getBlock().getBlockData();
										piston.setExtended(false);
										block.getLocation().add(x, y, z).getBlock().setBlockData(piston);
										block.getBlock().setType(Material.AIR, false);
									}
								} else {
									if(block.getLocation().add(x, y, z).getBlock().getBlockData() instanceof PistonHead) {
										Piston piston = (Piston)block.getBlock().getBlockData();
										piston.setExtended(false);
										block.getBlock().setBlockData(piston);
										block.getLocation().add(x, y, z).getBlock().setType(Material.AIR, false);
									}
								}
							} else {
								removedList.add(block.getLocation().getBlock().getType().toString().toLowerCase());
								eBlockList.remove(block.getBlock());
							}
						}
					}
//					for(BlockManager block : queueGroundAfter) {
//						boolean r = true;
//						//boolean r2 = true;
//						r = preRegen(block, explosion, false, true);
//						if(r)
//							block.getLocation().getBlock().setType(Material.AIR);
//						else {
//							removedList.add(block.getBlock().getType().toString().toLowerCase());
//							eBlockList.remove(block.getBlock());
//						}
//					}
					if(e instanceof EntityExplodeEvent)
						((EntityExplodeEvent)e).setYield(Float.valueOf(0));
					else if(e instanceof BlockExplodeEvent)
						((BlockExplodeEvent)e).setYield(Float.valueOf(0));
					if(Main.getInstance().getCoreProtect() != null) {
						for(BlockManager block : explosion.getBlocks())
							Main.getInstance().getCoreProtect().logRemoval("#" + subType, block.getLocation(), block.getType(), block.getBlock().getBlockData());
					}
					explosion.regenerate(configManager.getRegenDelay(), configManager.getRegenPeriod(), configManager.isInstantRegenEnable());
				}			
			} else {
				if(!configManager.isRegenEnable()) {
					List<BlockManager> blocksMan = new ArrayList<>();
					for(Block block : eBlockList)
						blocksMan.add(new BlockManager(block.getState()));
					HashMap<BlockManager, Integer> newBlockMan = new HashMap<>();
					blocksMan.forEach(block -> newBlockMan.put(block, block.getLocation().getBlockY()));
					Map<BlockManager, Integer> map = sortByValues(newBlockMan);
					for(BlockManager block : map.keySet()) {
						if(BlockManager.isValidBlock(block.getBlock().getType())) {
							if(!block.allowExplosionDamage()) {
								preRegen(block, explosion);
								eBlockList.remove(block.getBlock());
							}
						}
					}
				}
			}
		}
		if(eMaterial != Material.AIR)
			eMaterial = Material.AIR;
	}
	private BlockManager getBlockFromLocation(List<BlockManager> set, Location location) {
		for(BlockManager block : set) {
			if(block.getLocation().equals(location))
				return block;
		}
		return null;
	}
	private boolean preRegen(BlockManager block, ExplosionManager explosion) {
		return preRegen(block, explosion, false, false);
	}
	private boolean preRegen(BlockManager block, ExplosionManager explosion, boolean ignoreDurability, boolean preappend) { 
		if(BlockManager.isValidBlock(block.getBlock().getType())) {
			if(ignoreDurability || (block.getDurability() == 0 || block.getDurability() == 1)) {
				if(block.allowExplosionDamage()) {
					if(block.allowRegen()) {
						if(block.getType() != Material.TNT && !(block.getBlock().getBlockData() instanceof PistonHead)) {
							if(block.getBlock().getBlockData() instanceof Piston) {
								Piston piston = (Piston)block.getBlock().getBlockData();
								piston.setExtended(false);
								block.getState().setBlockData(piston);
							}
							if(block.isContainer()) {
								if(block.saveItems() || block.getState() instanceof ShulkerBox)
									((Container)block.getState()).getInventory().clear();
								else
									((Container)block.getState()).getSnapshotInventory().clear();
							}
							if(block.allowReplace()) {
								if(block.replaceWith().data == block.getType().data) {
									BlockData data = Bukkit.createBlockData(block.replaceWith(), block.getBlock().getBlockData().getAsString().replace("minecraft:" + block.getBlock().getType().toString().toLowerCase(), ""));
									block.getState().setBlockData(data);
								} else
									block.getState().setType(block.replaceWith());
							}
							explosion.addBlock(block, preappend);
						}						
					} else {
						Random r = new Random();
						int random = r.nextInt(99);
						if(random <= block.dropChance() - 1)
							block.getBlock().breakNaturally();
					}	
					return true;
				} else {
					if(block.allowReplace()) {
						if(block.replaceWith().data == block.getBlock().getType().data) {
							BlockData newB = block.replaceWith().createBlockData(block.getBlock().getBlockData().getAsString().replace(block.getBlock().getType().getKey().toString(), ""));
							block.getBlock().setBlockData(newB);
						} else block.getBlock().setType(block.replaceWith());
					}
					return false;
				}
			} else if(block.getDurability() <= -1) {
				return false;
			} else {
				boolean status = ExplosionManager.adjustDurability(block.getLocation(), block.getDurability(), new ConfigManager(explosion.getExplosionType(), explosion.getSubType()).getBlockDamage());
				if(!status) {
					return preRegen(block, explosion, true, preappend);
				} else {
					return false;
				}
			}
		}
		return false;
	}
}
