package com.watsonllc.chestlock.commands;

public enum ToggleState {
    ON,
    OFF,
    TOGGLE;

    public static ToggleState parse(String value) {
        if (value == null || value.isEmpty())
            return TOGGLE;

        switch (value.toLowerCase()) {
        case "on":
            return ON;
        case "off":
            return OFF;
        case "toggle":
            return TOGGLE;
        default:
            return null;
        }
    }
}
