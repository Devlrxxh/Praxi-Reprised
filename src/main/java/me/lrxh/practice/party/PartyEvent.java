package me.lrxh.practice.party;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum PartyEvent {

    FFA("Party FFA"),
    SPLIT("Party Split");

    private final String name;
}
