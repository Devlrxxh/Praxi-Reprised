package me.lrxh.practice.setting;


import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import org.bukkit.entity.Player;

@CommandAlias("profilesettings")
@Description("Profile Settings Command.")
public class ProfileSettingsCommand extends BaseCommand {
    @Default
    public void open(Player player) {
        new ProfileSettingsMenu().openMenu(player);
    }
}
 