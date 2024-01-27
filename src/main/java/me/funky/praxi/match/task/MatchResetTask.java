package me.funky.praxi.match.task;

import com.boydti.fawe.util.EditSessionBuilder;
import com.boydti.fawe.util.TaskManager;
import me.funky.praxi.match.Match;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import lombok.AllArgsConstructor;
import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.scheduler.BukkitRunnable;

@AllArgsConstructor
public class MatchResetTask extends BukkitRunnable {

	private Match match;

	@Override
	public void run() {
		if (match.getKit().getGameRules().isBuild() && match.getPlacedBlocks().size() > 0) {
			TaskManager.IMP.async(() -> {
				EditSession editSession = new EditSessionBuilder(match.getArena().getSpawnA().getWorld().getName())
						.fastmode(true)
						.allowedRegionsEverywhere()
						.autoQueue(false)
						.limitUnlimited()
						.build();

				for (Location location : match.getPlacedBlocks()) {
					try {
						editSession.setBlock(
								new Vector((double) location.getBlockX(), (double) location.getBlockY(),
										location.getZ()
								), new BaseBlock(0));
					} catch (Exception ex) {
					}
				}

				editSession.flushQueue();

				TaskManager.IMP.task(() -> {
					match.getPlacedBlocks().clear();
					match.getArena().setActive(false);
					cancel();
				});
			});
		} else if (match.getKit().getGameRules().isBuild() && match.getChangedBlocks().size() > 0) {
			TaskManager.IMP.async(() -> {
				EditSession editSession = new EditSessionBuilder(match.getArena().getSpawnA().getWorld().getName())
						.fastmode(true)
						.allowedRegionsEverywhere()
						.autoQueue(false)
						.limitUnlimited()
						.build();

				for (BlockState blockState : match.getChangedBlocks()) {
					try {
						editSession.setBlock(
								new Vector(blockState.getLocation().getBlockX(), blockState.getLocation().getBlockY(),
										blockState.getLocation().getZ()
								), new BaseBlock(blockState.getTypeId(), blockState.getRawData()));
					} catch (Exception ex) {
					}
				}

				editSession.flushQueue();

				TaskManager.IMP.task(() -> {
					if (match.getKit().getGameRules().isBuild()) {
						match.getChangedBlocks().clear();
						match.getArena().setActive(false);
					}

					cancel();
				});
			});
		} else {
			cancel();
		}
	}

}
