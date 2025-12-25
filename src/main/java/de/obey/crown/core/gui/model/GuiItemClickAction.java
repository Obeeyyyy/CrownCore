package de.obey.crown.core.gui.model;

/*
    Author: Obey
    Date: 23.12.2025
    Time: 10:10
    Project: CrownCore
*/

public record GuiItemClickAction(Type type, String value, boolean close) {

    public enum Type {
        NONE,
        COMMAND,
        CONSOLE_COMMAND,
        OPEN_GUI,
        CLOSE
    }

}
