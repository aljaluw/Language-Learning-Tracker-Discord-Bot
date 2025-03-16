package org.jalu.enums;

public enum MediaUnit {
    CHARACTERS("Characters"),
    PAGES("Pages"),
    CHAPTERS("Chapters"),
    MINUTES("Minutes"),
    EPISODES("Episodes");

    private final String displayName;

    MediaUnit(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
