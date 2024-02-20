package me.lrxh.practice.profile.option;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.lrxh.practice.util.BaseEvent;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Getter
public class OptionsOpenedEvent extends BaseEvent {

    private final Player player;
    private final List<ProfileOptionButton> buttons = new ArrayList<>();

}
