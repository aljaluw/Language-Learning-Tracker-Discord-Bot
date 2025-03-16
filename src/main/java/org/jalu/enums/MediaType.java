package org.jalu.enums;

public enum MediaType {
    READING("Reading",  new MediaUnit[]{MediaUnit.CHARACTERS, MediaUnit.PAGES, MediaUnit.CHAPTERS}),
    VISUAL_NOVEL("Visual Novel",  new MediaUnit[]{MediaUnit.CHARACTERS}),
    MOVIE("Movie",  new MediaUnit[]{MediaUnit.MINUTES}),
    TV_SHOW("TV Show",  new MediaUnit[]{MediaUnit.EPISODES}),
    GAME("Game",  new MediaUnit[]{MediaUnit.MINUTES}),
    LISTENING("Listening",  new MediaUnit[]{MediaUnit.MINUTES});

    private final String displayName;
    private final MediaUnit[] validUnits;


    MediaType(String displayName, MediaUnit[] validUnits) {
        this.displayName = displayName;
        this.validUnits = validUnits;
    }

    public MediaUnit[] getValidUnits() {
        return validUnits;
    }

    public String getDisplayName() {
        return displayName;
    }
}
