package me.lrxh.practice.profile.meta;

import lombok.Getter;
import lombok.Setter;
import me.lrxh.practice.kit.Kit;
import me.lrxh.practice.kit.KitLoadout;

public class ProfileKitEditorData {

    @Getter
    @Setter
    private boolean active;
    @Setter
    private boolean rename;
    @Getter
    @Setter
    private Kit selectedKit;
    @Getter
    @Setter
    private KitLoadout selectedKitLoadout;

    public boolean isRenaming() {
        return this.active && this.rename && this.selectedKit != null;
    }

}
