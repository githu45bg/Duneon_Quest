/** The in-game "SAVE [name]" command. */
public final class SaveCommand {
    private SaveCommand() { }

    public static String run(String argument, int levelIndex, Dungeon d, DungeonMaster dm) {
        String name = argument.isBlank() ? "quicksave" : argument.trim();
        try {
            GameSaver.save(name, levelIndex, d, dm);
            return "Game saved to saves/" + name + ".txt";
        } catch (SaveException e) {
            return e.getMessage();
        }
    }
}