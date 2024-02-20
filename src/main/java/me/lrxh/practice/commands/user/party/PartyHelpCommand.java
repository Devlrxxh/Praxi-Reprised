package me.lrxh.practice.commands.user.party;

import me.lrxh.practice.Locale;
import me.lrxh.practice.util.command.command.CommandMeta;
import org.bukkit.entity.Player;

@CommandMeta(label = {"p", "p help", "party", "party help"})
public class PartyHelpCommand {

    public void execute(Player player) {
        Locale.PARTY_HELP.formatLines(player).forEach(player::sendMessage);
    }

}
