/** The special orders a Dungeon Master can give a monster. */
public final class MonsterCommands {
    private MonsterCommands() { }

    public static String perform(String name, Monster m, Dungeon d) throws CommandException {
        return switch (name.toLowerCase()) {
            case "act"  -> extraTurn(m, d);
            case "heal" -> heal(m);
            default -> throw new CommandException("Unknown monster action. Try: act, heal.");
        };
    }

    private static String extraTurn(Monster m, Dungeon d) {
        Action action = m.chooseAction(d, d.livingAdventurers());
        return "[DM] " + m.getName() + " takes an extra turn. " + action.execute(d);
    }

    private static String heal(Monster m) throws CommandException {
        if (m.getHp() == m.getMaxHp()) throw new CommandException("That monster is unhurt.");
        int amount = Dice.roll(1, 8) + 2;
        m.heal(amount);
        return "[DM] " + m.getName() + " regains " + amount + " HP.";
    }
}
