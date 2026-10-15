import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/** What chests and slain monsters can drop. */
public final class LootTable {
    private static final int DROP_CHANCE_SIDES = 4;      // a 1 on a d4: one kill in four

    private static final List<Supplier<Item>> GEAR = List.of(
            () -> new Weapon("Steel Sword", 8, 40),
            () -> new Weapon("War Axe", 10, 60),
            () -> new Armor("Chain Mail", 2, 50),
            () -> new Armor("Plate Armor", 3, 80),
            () -> new Scroll("Mana Scroll", 8));

    private LootTable() { }

    public static Item randomGear() {
        return Dice.pick(GEAR).get();
    }

    public static Optional<Item> maybeDrop() {
        return Dice.roll(1, DROP_CHANCE_SIDES) == 1 ? Optional.of(randomGear()) : Optional.empty();
    }
}
