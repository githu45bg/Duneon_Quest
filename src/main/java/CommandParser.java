import java.util.List;
import java.util.Optional;

public class CommandParser {

    public Action parse(String line, Adventurer a, Dungeon d) throws CommandException {
        String[] parts = line.trim().split("\\s+");
        String cmd = parts[0].toUpperCase();

        Optional<Direction> dir = Direction.parse(cmd);
        if (dir.isPresent()) return move(a, dir.get(), d);

        return switch (cmd) {
            case "A" -> attack(a, d);
            case "U" -> use(a, parts);
            case "R" -> new RestAction(a);
            case "C" -> cast(a, d);
            case "X" -> ability(a, d);
            case "F" -> new SearchAction(a);
            default -> throw new CommandException(
                    "Unknown command '" + parts[0] + "'. Type H for help.");
        };
    }

    private Action move(Adventurer a, Direction dir, Dungeon d) throws CommandException {
        if (!d.canEnter(a.getX() + dir.dx(), a.getY() + dir.dy())) {
            throw new CommandException("Something blocks the way.");
        }
        return new MoveAction(a, dir);
    }

    private Action attack(Adventurer a, Dungeon d) throws CommandException {
        for (Monster m : d.getMonsters()) {
            int dist = Math.abs(m.getX() - a.getX()) + Math.abs(m.getY() - a.getY());
            if (m.isAlive() && dist == 1) return new AttackAction(a, m);
        }
        throw new CommandException("There is no monster next to you.");
    }
    private Action cast(Adventurer a, Dungeon d) throws CommandException {
        PlayerClass pc = a.getPlayerClass();
        if (!pc.canCast()) {
            throw new CommandException("Your class can't cast spells. Try X for your ability.");
        }
        if (a.getMana() < CastAction.manaCost(pc)) {
            throw new CommandException("Not enough mana (need "
                    + CastAction.manaCost(pc) + ", have " + a.getMana() + ").");
        }
        if (!CastAction.hasTarget(a, d)) {
            throw new CommandException("There is no valid target for your spell.");
        }
        return new CastAction(a);
    }

    private Action ability(Adventurer a, Dungeon d) throws CommandException {
        if (a.getPlayerClass().canCast()) {
            throw new CommandException("Mages and Clerics use C to cast.");
        }
        if (!a.abilityReady()) {
            throw new CommandException(a.getPlayerClass().getAbilityName()
                    + " recharges in " + a.getAbilityCooldown() + " more round(s).");
        }
        Monster target = d.adjacentMonster(a)
                .orElseThrow(() -> new CommandException("There is no monster next to you."));
        return new SpecialAttackAction(a, target);
    }

    private Action use(Adventurer a, String[] parts) throws CommandException {
        if (parts.length < 2) throw new CommandException("Usage: U <item number>");
        try {
            int slot = Integer.parseInt(parts[1]) - 1;
            if (a.getInventory().get(slot).isEmpty()) {
                throw new CommandException("You have no item in slot " + parts[1] + ".");
            }
            return new UseAction(a, slot);
        } catch (NumberFormatException e) {
            throw new CommandException("The item number must be a number.");
        }
    }
    private Action give(Adventurer a, String[] parts, Dungeon d) throws CommandException {
        if (parts.length < 3) throw new CommandException("Usage: G <item number> <player number>");
        Item item = a.getInventory().get(number(parts[1]) - 1)
                .orElseThrow(() -> new CommandException("You have no item in slot " + parts[1] + "."));
        Adventurer target = ally(a, number(parts[2]), d);
        boolean used = item instanceof Potion || item instanceof Scroll;
        if (!used && (item == a.getWeapon() || item == a.getArmor())) {
            throw new CommandException("That is equipped, so you cannot give it away.");
        }
        if (!used && target.getInventory().isFull()) {
            throw new CommandException(target.getName() + "'s pack is full.");
        }
        return new GiveAction(a, target, item);
    }

    private Adventurer ally(Adventurer a, int number, Dungeon d) throws CommandException {
        List<Adventurer> party = d.getAdventurers();
        if (number < 1 || number > party.size()) {
            throw new CommandException("There is no player " + number + ".");
        }
        Adventurer target = party.get(number - 1);
        if (target == a) throw new CommandException("Use U to use your own items.");
        if (target.isDead()) throw new CommandException(target.getName() + " is beyond help.");
        int dist = Math.abs(a.getX() - target.getX()) + Math.abs(a.getY() - target.getY());
        if (dist != 1) throw new CommandException(target.getName() + " must be standing next to you.");
        return target;
    }

    private int number(String text) throws CommandException {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            throw new CommandException("'" + text + "' is not a number.");
        }
    }
}
