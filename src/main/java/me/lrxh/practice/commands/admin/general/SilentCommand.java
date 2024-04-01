package me.lrxh.practice.commands.admin.general;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import me.lrxh.practice.Locale;
import me.lrxh.practice.Practice;
import me.lrxh.practice.profile.Profile;
import me.lrxh.practice.util.CC;
import me.lrxh.practice.util.LocationUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.io.IOException;


@CommandAlias("silent")
@CommandPermission("practice.admin.silent")
@Description("Silent Command for staff.")
public class SilentCommand extends BaseCommand {

    @Default
    public void silent(Player player) {
        Profile profile = Profile.getProfiles().get(player.getUniqueId());
        profile.setSilent(!profile.isSilent());
        player.sendMessage(profile.isSilent() ? Locale.SILENT_ENABLED.format(player) : Locale.SILENT_DISABLED.format(player));
    }
}
