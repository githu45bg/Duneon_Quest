import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Game {

    private static final int AGGRO_RADIUS = 5;

    // Core game components
    private final Dungeon dungeon;
    private final Renderer renderer;
    private final Scanner in;
    private final CommandParser parser = new CommandParser();

    // Game state
    private boolean running = true;
    private boolean victory;

    // Dungeon Master and campaign information
    private final DmConsole dmConsole;
    private final int levelIndex;

    // Optional custom input provider for graphical interfaces
    private final CommandInput commandInput;

    // Tracks monsters whose rewards have already been processed
    private final Map<Monster, Boolean> rewardedMonsters = new HashMap<>();

    // =========================
    // CONSTRUCTORS
    // =========================

    /**
     * Existing constructor.
     *
     * Preserves compatibility with Main.java and the
     * existing Swing and console interfaces.
     */
    public Game(
            Dungeon dungeon,
            Renderer renderer,
            Scanner in,
            DmConsole dmConsole,
            int levelIndex
    ) {
        this(
                dungeon,
                renderer,
                in,
                dmConsole,
                levelIndex,
                null
        );
    }

    /**
     * Constructor with a custom input provider.
     *
     * A graphical interface can supply a CommandInput
     * implementation to receive player commands.
     */
    public Game(
            Dungeon dungeon,
            Renderer renderer,
            Scanner in,
            DmConsole dmConsole,
            int levelIndex,
            CommandInput commandInput
    ) {
        this.dungeon = dungeon;
        this.renderer = renderer;
        this.in = in;
        this.dmConsole = dmConsole;
        this.levelIndex = levelIndex;
        this.commandInput = commandInput;
    }

    // =========================
    // LOCALIZATION
    // =========================

    /**
     * Retrieves a translated message from I18n.
     *
     * Example:
     * text("game.initiative", "Hero", 15)
     */
    private String text(String key, Object... args) {

        return MessageFormat.format(
                I18n.get(key),
                args
        );
    }

    // =========================
    // START GAME
    // =========================

    /**
     * Runs the game until the party wins, all adventurers
     * die, or the player quits.
     *
     * IMPORTANT:
     * When using JavaFX with blocking input, call this
     * method on a background thread, not the JavaFX thread.
     */
    public void run() {

        // Display the initial dungeon.
        refreshMap();

        // Determine the order in which adventurers act.
        List<Adventurer> order = initiativeOrder();

        // Main game loop.
        while (running && !victory && !allDead()) {

            // Each adventurer takes a turn.
            for (Adventurer a : order) {

                if (!running || victory || allDead()) {
                    break;
                }

                takeTurn(a);
            }

            // Monsters act after the adventurers.
            if (running && !victory && !allDead()) {

                monstersAct();

                // Apply end-of-round effects.
                endRound(order);

                // Refresh the dungeon.
                refreshMap();
            }
        }

        // Display the final dungeon state.
        refreshMap();

        // Announce the result.
        announceResult();
    }

    /**
     * Returns true when the party wins.
     */
    public boolean won() {
        return victory;
    }

    /**
     * Returns true when the player quits.
     */
    public boolean wasQuit() {
        return !running;
    }

    // =========================
    // MAP RENDERING
    // =========================

    /**
     * Requests the renderer to display the dungeon.
     */
    private void refreshMap() {

        FieldOfView.updateExplored(
                dungeon,
                FieldOfView.SIGHT_RADIUS
        );

        renderer.render(dungeon);
    }

    // =========================
    // INITIATIVE
    // =========================

    /**
     * Determines the adventurers' turn order.
     *
     * The adventurer with the highest initiative acts first.
     */
    private List<Adventurer> initiativeOrder() {

        Map<Adventurer, Integer> rolls = new HashMap<>();

        // Roll initiative for every adventurer.
        for (Adventurer a : dungeon.getAdventurers()) {

            int roll = a.rollInitiative();

            rolls.put(a, roll);

            renderer.showMessage(
                    text(
                            "game.initiative",
                            a.getName(),
                            roll
                    )
            );
        }

        // Copy the adventurers into a separate list.
        List<Adventurer> order =
                new ArrayList<>(dungeon.getAdventurers());

        // Sort by initiative, highest first.
        order.sort(
                Comparator.comparingInt(
                        (Adventurer a) -> rolls.get(a)
                ).reversed()
        );

        return order;
    }

    // =========================
    // PLAYER TURN
    // =========================

    /**
     * Processes one adventurer's turn.
     */
    private void takeTurn(Adventurer a) {

        // Skip dead or unconscious adventurers.
        if (a.isDead() || a.isUnconscious()) {
            return;
        }

        refreshMap();

        while (running && !victory) {

            // Request a command from the player.
            String line = prompt(a);

            // Null or Q means the player wants to quit.
            if (line == null || line.trim().equalsIgnoreCase("Q")) {
                running = false;
                return;
            }

            line = line.trim();

            // Ignore empty commands.
            if (line.isEmpty()) {
                continue;
            }

            // Process commands that do not consume a turn.
            if (handleFreeCommand(line, a)) {

                refreshMap();

                continue;
            }

            // Process an ordinary game action.
            if (tryAction(line, a)) {

                refreshMap();

                // The adventurer has completed their turn.
                return;
            }

            // Invalid commands do not consume the turn.
        }
    }

    // =========================
    // PLAYER INPUT
    // =========================

    /**
     * Obtains a command from the appropriate input system.
     *
     * Priority:
     * 1. Custom input provider, such as JavaFX.
     * 2. Existing Swing input.
     * 3. Console input.
     */
    private String prompt(Adventurer a) {

        // Use the supplied input provider when available.
        if (commandInput != null) {

            return commandInput.getCommand(a.getName());
        }

        // Preserve existing Swing input.
        if (renderer instanceof SwingRenderer swingRenderer) {

            return swingRenderer
                    .promptCommand(a.getName())
                    .orElse(null);
        }

        // Preserve existing console input.
        System.out.print(
                text("game.commandPrompt", a.getName()) + " > "
        );

        if (in != null && in.hasNextLine()) {

            return in.nextLine().trim();
        }

        return null;
    }

    // =========================
    // FREE COMMANDS
    // =========================

    /**
     * Handles commands that do not consume a turn.
     *
     * Returns true when the command has been handled.
     */
    private boolean handleFreeCommand(
            String line,
            Adventurer a
    ) {

        // Help command.
        if (line.equalsIgnoreCase("H")) {

            renderer.showMessage(HelpText.TEXT);

            return true;
        }

        // Inventory command.
        if (line.equalsIgnoreCase("I")) {

            renderer.showMessage(inventoryText(a));

            return true;
        }

        // Dungeon Master command.
        if (line.equalsIgnoreCase("M")) {

            dmConsole.open(dungeon);

            refreshMap();

            return true;
        }

        // Save game command.
        String[] words = line.split("\\s+", 2);

        if (words[0].equalsIgnoreCase("SAVE")) {

            String name =
                    words.length > 1 ? words[1] : "";

            String message = SaveCommand.run(
                    name,
                    levelIndex,
                    dungeon,
                    dmConsole.getDm()
            );

            renderer.showMessage(message);

            return true;
        }

        // Toggle sound.
        if (line.equalsIgnoreCase("SOUND")) {

            return toggleSound();
        }

        return false;
    }

    // =========================
    // INVENTORY
    // =========================

    /**
     * Creates a readable inventory description.
     */
    private String inventoryText(Adventurer a) {

        StringBuilder sb = new StringBuilder();

        // Display gold and experience.
        sb.append(text("game.gold"))
                .append(": ")
                .append(a.getGold())
                .append("  ")
                .append(text("game.xp"))
                .append(": ")
                .append(a.getXp());

        int slot = 1;

        // List the items.
        for (Item item : a.getInventory().list()) {

            sb.append("\n  ")
                    .append(slot++)
                    .append(". ")
                    .append(item.getName());
        }

        // Report an empty inventory.
        if (slot == 1) {

            sb.append("\n  ")
                    .append(text("game.emptyInventory"));
        }

        return sb.toString();
    }

    // =========================
    // SOUND
    // =========================

    /**
     * Enables or disables sound.
     */
    private boolean toggleSound() {

        Sound.setEnabled(!Sound.isEnabled());

        renderer.showMessage(
                text(
                        Sound.isEnabled()
                                ? "game.soundOn"
                                : "game.soundOff"
                )
        );

        return true;
    }

    // =========================
    // EXECUTE PLAYER ACTION
    // =========================

    /**
     * Parses and executes a player's action.
     *
     * Returns true if the action succeeds.
     */
    private boolean tryAction(
            String line,
            Adventurer a
    ) {

        try {

            // Convert the command into an Action.
            Action action = parser.parse(
                    line,
                    a,
                    dungeon
            );

            // Execute the action.
            String message = action.execute(dungeon);

            renderer.showMessage(message);

            // Play the associated sound.
            SoundEffects.cueFor(action, message)
                    .ifPresent(Sound::play);

            // Handle effects caused by the action.
            afterAction(action, a);

            // Update the dungeon display.
            refreshMap();

            return true;

        } catch (CommandException e) {

            // Invalid commands do not consume a turn.
            renderer.showMessage(e.getMessage());

            return false;
        }
    }

    // =========================
    // AFTER PLAYER ACTION
    // =========================

    /**
     * Processes tile interactions, exit conditions,
     * and monster rewards after an action.
     */
    private void afterAction(
            Action action,
            Adventurer a
    ) {

        // Handle movement.
        if (action instanceof MoveAction) {

            String msg = TileInteraction.onEnter(
                    a,
                    dungeon
            );

            if (!msg.isEmpty()) {

                renderer.showMessage(msg);
            }

            // Check whether the adventurer reached the exit.
            RoomType here = dungeon
                    .getRoom(a.getX(), a.getY())
                    .getType();

            if (here == RoomType.EXIT) {

                victory = true;
            }
        }

        // Award XP and loot for defeated monsters.
        awardKills(a);
    }

    // =========================
    // EXPERIENCE AND LOOT
    // =========================

    /**
     * Awards rewards for monsters defeated by this adventurer.
     *
     * The reward tracker prevents an already-defeated monster
     * from granting rewards repeatedly on later turns.
     */
    private void awardKills(Adventurer a) {

        for (Monster m : dungeon.getMonsters()) {

            // Ignore monsters whose rewards were processed.
            if (rewardedMonsters.containsKey(m)) {
                continue;
            }

            // Process a defeated monster.
            if (!m.isAlive()) {

                rewardedMonsters.put(m, true);

                // Award XP.
                int levelsGained = a.addXp(
                        m.getXpValue()
                );

                renderer.showMessage(
                        text(
                                "game.monsterSlain",
                                m.getName(),
                                a.getName(),
                                m.getXpValue()
                        )
                );

                Sound.play(Sound.Cue.KILL);

                // Announce level-ups.
                if (levelsGained > 0) {

                    announceLevelUp(a);
                }

                // Attempt to drop loot.
                dropLoot(a);
            }
        }

        // Remove defeated monsters.
        dungeon.removeDeadMonsters();
    }

    /**
     * Announces a character level-up.
     */
    private void announceLevelUp(Adventurer a) {

        renderer.showMessage(
                text(
                        "game.levelUp",
                        a.getName(),
                        a.getLevel(),
                        a.getMaxHp()
                )
        );

        Sound.play(Sound.Cue.LEVEL_UP);
    }

    /**
     * Attempts to generate loot and add it to the inventory.
     */
    private void dropLoot(Adventurer a) {

        LootTable.maybeDrop().ifPresent(item -> {

            if (a.getInventory().add(item)) {

                renderer.showMessage(
                        text(
                                "game.findsItem",
                                a.getName(),
                                item.getName()
                        )
                );
            }
        });
    }

    // =========================
    // MONSTER TURNS
    // =========================

    /**
     * Allows nearby monsters to act.
     */
    private void monstersAct() {

        // Use a copy to avoid modification problems.
        for (Monster m :
                new ArrayList<>(dungeon.getMonsters())) {

            // Ignore defeated monsters.
            if (!m.isAlive()) {
                continue;
            }

            // Determine whether the monster can detect the party.
            boolean awake = FieldOfView.partyCanSee(
                    dungeon,
                    m.getX(),
                    m.getY(),
                    AGGRO_RADIUS
            );

            if (!awake) {
                continue;
            }

            // Let the monster choose its action.
            Action action = m.chooseAction(
                    dungeon,
                    dungeon.getAdventurers()
            );

            // Rest actions do not need combat messages.
            if (!(action instanceof RestAction)) {

                String message = action.execute(dungeon);

                renderer.showMessage(message);
            }
        }

        refreshMap();
    }

    // =========================
    // END OF ROUND
    // =========================

    /**
     * Applies end-of-round effects to each adventurer.
     */
    private void endRound(List<Adventurer> order) {

        for (Adventurer a : order) {

            boolean wasDead = a.isDead();

            // Apply end-of-round effects.
            a.endOfRound();

            // Announce a new death.
            if (!wasDead && a.isDead()) {

                renderer.showMessage(
                        text(
                                "game.died",
                                a.getName()
                        )
                );

                Sound.play(Sound.Cue.DOWN);

            } else if (a.isUnconscious()) {

                renderer.showMessage(
                        text(
                                "game.unconscious",
                                a.getName(),
                                a.getReviveRoundsLeft()
                        )
                );
            }
        }

        refreshMap();
    }

    // =========================
    // GAME OVER CHECK
    // =========================

    /**
     * Returns true when every adventurer is dead.
     */
    private boolean allDead() {

        return dungeon.getAdventurers()
                .stream()
                .allMatch(Adventurer::isDead);
    }

    // =========================
    // GAME RESULT
    // =========================

    /**
     * Announces the result when gameplay ends.
     */
    private void announceResult() {

        if (victory) {

            renderer.showMessage(
                    text("game.partyVictory")
            );

            Sound.play(Sound.Cue.VICTORY);

        } else if (allDead()) {

            renderer.showMessage(
                    text("game.partyFallen")
            );

            Sound.play(Sound.Cue.DEFEAT);

        } else {

            renderer.showMessage(
                    text("game.ended")
            );
        }
    }
}