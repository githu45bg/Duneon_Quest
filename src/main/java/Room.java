public class Room {
    private RoomType type;
    private boolean explored;
    private boolean revealed;// used later for fog of war

    public Room(RoomType type) { this.type = type; }

    public RoomType getType()    { return type; }
    public boolean isWalkable()  { return type.isWalkable(); }
    public boolean isExplored()  { return explored; }
    public void markExplored()   { explored = true; }

    /** E.g. after picking up treasure the room becomes plain floor. */
    public boolean blocksSight() { return type == RoomType.WALL; }

    public boolean isRevealed()   {return revealed;}
    public void reveal()            {revealed=true;}

    public RoomType visibleType() {
        return (type.isHidden() && !revealed) ? RoomType.FLOOR : type;
    }



    public void setType(RoomType newType) {
        this.type = newType;
        this.revealed=false;
    }
}