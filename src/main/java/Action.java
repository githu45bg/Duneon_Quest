public interface Action {
    /** Performs the action and returns a message to show the table.
     * rule for action happening, allows separate action classes*/
    String execute(Dungeon dungeon);
}