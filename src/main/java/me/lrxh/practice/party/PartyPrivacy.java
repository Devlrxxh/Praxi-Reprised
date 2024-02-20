package me.lrxh.practice.party;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum PartyPrivacy {

    OPEN("Open"),
    CLOSED("Closed");

    private String readable;

}
