package game;

public enum HandValueType {
    
	ROYAL_FLUSH("a Royal Flush", 9),
    STRAIGHT_FLUSH("a Straight Flush", 8),
    FOUR_OF_A_KIND("Four of a Kind", 7),
    FULL_HOUSE("a Full House", 6),
    FLUSH("a Flush", 5),
    STRAIGHT("a Straight", 4),
    THREE_OF_A_KIND("Three of a Kind", 3),
    TWO_PAIRS("Two Pairs", 2),
    ONE_PAIR("One Pair", 1),
    HIGH_CARD("a High Card", 0);
    
    private String description;

    private int value;
    
    HandValueType(String description, int value) {
        this.description = description;
        this.value = value;
    }
    
    public String getDescription() {
        return description;
    }
    
    public int getValue() {
        return value;
    }
    
}