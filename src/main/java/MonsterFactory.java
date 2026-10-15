import java.util.Optional;

public final class MonsterFactory {
    private MonsterFactory() { }

    public static Optional<Monster> create(String type) {
        return switch (type.trim().toLowerCase()) {
            case "goblin"   -> Optional.of(new Goblin());
            case "skeleton" -> Optional.of(new Skeleton());
            case "orc"      -> Optional.of(new Orc());
            case "dragon"   -> Optional.of(new Dragon());
            default         -> Optional.empty();
        };
    }
}
