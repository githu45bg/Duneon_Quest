# Dungeon Quest

A terminal text-based multiplayer dungeon crawler in Java 17+. One to four adventurers share one keyboard; a Dungeon Master (DM) runs a password-protected console.

## Compile and run
Run from the project folder: the game reads `levels/`, `config/`, `saves/` and `scores.txt` from there.

    javac -d out -sourcepath src/main/java src/main/java/Main.java
    java -cp out Main

Menu: **N** new game, **L** load a save, **S** high scores, **Q** quit.
The DM presses **M** on any turn. The password is `dm.password` in `config/dm.properties`.
(pass dragon123)

## Controls
`N S E W` move | `A` attack | `X` ability (Warrior Power Strike, Rogue Backstab)
`C` cast (Mage Fireball, Cleric Heal/revive) | `F` find traps and treasure | `R` rest
`U n` use item n | `G n p` give item n to adjacent player p (a potion revives a friend)
`I` inventory | `SAVE [name]` | `SOUND` on/off | `H` help | `Q` quit to menu
DM: `trap x y`, `spawn type`, `event [name]`, `command x y act|heal`, `narration text`,
`view`, `exit` (3 traps, 3 spawns, 3 events per game; `command` uses one event).

## Level symbols
`#` wall  `.` floor  `A` start  `E` exit  `T` treasure  `P` potion  `K` key  `C` locked chest
`^` hidden trap  `H` hidden treasure  `G` goblin  `S` skeleton  `O` orc  `D` dragon

## Class diagram
    Character (abstract)
     |- Adventurer --- PlayerClass (enum), Inventory, StatusEffects
     '- Monster (abstract) - Goblin, Skeleton, Orc, Dragon
    Item (abstract) - Potion, Weapon, Armor, Key, Scroll
    Action (interface) - Move, Attack, SpecialAttack, Cast, Search, Rest, Use, Give, Breath
    Event (interface) - Ambush, CaveIn, Blessing, Curse, Merchant
    Renderer (interface) - ConsoleRenderer
    Game --- Dungeon --- Room / RoomType        Main --- SetupScreen, DmConsole
    DungeonMaster, CommandParser, GameSaver (SaveWriter, SaveReader), ScoreBoard
    Utilities: Dice, LevelLoader, FieldOfView, Pathfinder, LootTable, Palette, Ansi

## Known bugs and limitations
A loaded game resumes at the start of a round, and initiative is re-rolled.
- Initiative is rolled once per level, not at the start of each fight.
- The dragon's breath cooldown is not saved.
- An adventurer who died on an earlier level is missing from later saves and the scoreboard.
- A save made in a random dungeon resumes into the main campaign afterwards.
- The DM acts on demand (`M`), not in a fixed step every round.
- Quitting with `Q` does not write to `scores.txt`; only finished games (won or lost) do.
- Sound needs an audio device. Without one, key events fall back to a system beep.
## Libraries
None. Java standard library only.