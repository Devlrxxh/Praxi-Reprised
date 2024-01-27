package me.funky.praxi.commands.user.settings;

import me.funky.praxi.Locale;
import me.funky.praxi.profile.Profile;
import me.funky.praxi.util.CC;
import me.funky.praxi.util.command.command.CommandMeta;
import org.bukkit.entity.Player;

@CommandMeta(label = {"togglescoreboard", "tsb"})
public class ToggleScoreboardCommand {

    public void execute(Player player) {
        Profile profile = Profile.getByUuid(player.getUniqueId());
        profile.getOptions().showScoreboard(!profile.getOptions().showScoreboard());

        if (profile.getOptions().showScoreboard()) {
            player.sendMessage(Locale.OPTIONS_SCOREBOARD_ENABLED.format());
            player.kickPlayer(CC.translate("&cRejoin to see changes"));
        } else {
            player.sendMessage(Locale.OPTIONS_SCOREBOARD_DISABLED.format());
            player.kickPlayer(CC.translate("&cRejoin to see changes"));
        }
    }

}
