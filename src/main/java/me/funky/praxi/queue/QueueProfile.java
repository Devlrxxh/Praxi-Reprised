package me.funky.praxi.queue;

import java.util.UUID;

import me.funky.praxi.Locale;
import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@Data
public class QueueProfile {

	private final Queue queue;
	private final UUID playerUuid;
	private int elo;
	private int range = 25;
	private long start = System.currentTimeMillis();
	private int ticked;

	public QueueProfile(Queue queue, UUID playerUuid) {
		this.queue = queue;
		this.playerUuid = playerUuid;
	}

	public void tickRange() {
		ticked++;

		if (ticked % 6 == 0) {
			range += 3;

			if (ticked >= 50) {
				ticked = 0;

				if (queue.isRanked()) {
					Player player = Bukkit.getPlayer(playerUuid);

					if (player != null) {
						player.sendMessage(Locale.QUEUE_RANGE_INCREMENT.format(queue.getQueueName(), getMinRange(),
								getMaxRange()));
					}
				}
			}
		}
	}

	public boolean isInRange(int elo) {
		return elo >= (this.elo - this.range) && elo <= (this.elo + this.range);
	}

	public long getPassed() {
		return System.currentTimeMillis() - this.start;
	}

	public int getMinRange() {
		int min = this.elo - this.range;

		return min < 0 ? 0 : min;
	}

	public int getMaxRange() {
		int max = this.elo + this.range;

		return max > 2500 ? 2500 : max;
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof QueueProfile && ((QueueProfile) o).getPlayerUuid().equals(this.playerUuid);
	}

}
