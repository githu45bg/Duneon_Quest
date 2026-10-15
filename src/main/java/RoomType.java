public enum RoomType {
    WALL('#', false),
    FLOOR('.', true),
    TREASURE('T', true),
    POTION('P', true),
    KEY('K', true),
    CHEST('C', true),
    EXIT('E', true),
    TRAP('^', true,true),
    SECRET('H', true,true);

    private final char symbol;
    private final boolean walkable;
    private final boolean hidden;

    RoomType(char symbol, boolean walkable){
        this(symbol,walkable,false);
    }

    RoomType(char symbol, boolean walkable, boolean hidden) {
        this.symbol = symbol;
        this.walkable = walkable;
        this.hidden = hidden;
    }

    public char getSymbol()     { return symbol; }
    public boolean isWalkable() { return walkable; }
    public boolean isHidden()    {return hidden;}

    /** Returns null for symbols that aren't plain room types (A, G, S...). */
    public static RoomType fromSymbol(char c) {
        for (RoomType t : values()) {
            if (t.symbol == c) return t;
        }
        return null;
    }
}