import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public final class EventFactory {
    private static final List<Supplier<Event>> ALL =
            List.of(Ambush::new, CaveIn::new, Blessing::new, Curse::new, Merchant::new);

    private EventFactory() { }

    public static Event random() { return Dice.pick(ALL).get(); }

    public static Optional<Event> byName(String name) {
        String key = normalise(name);
        return ALL.stream().map(Supplier::get)
                .filter(e -> normalise(e.getName()).equals(key))
                .findFirst();
    }

    private static String normalise(String s) {
        return s.trim().toLowerCase().replace("-", "").replace(" ", "");
    }
}
