import java.util.Optional;

/** Chooses the sound for what just happened. Pure logic, so tests can check it silently. */
public final class SoundEffects {
    private SoundEffects() { }

    public static Optional<Sound.Cue> cueFor(Action action, String message) {
        if (action instanceof MoveAction) return Optional.of(Sound.Cue.STEP);
        if (action instanceof AttackAction || action instanceof SpecialAttackAction) {
            return attackCue(message);
        }
        if (action instanceof CastAction && message.contains("hurls")) {
            return Optional.of(Sound.Cue.SPELL);
        }
        if (action instanceof SearchAction && message.contains("disarms")) {
            return Optional.of(Sound.Cue.DISARM);
        }
        return Optional.empty();
    }

    private static Optional<Sound.Cue> attackCue(String message) {
        if (message.contains("misses")) return Optional.of(Sound.Cue.MISS);
        if (message.contains(" hits ") || message.contains(" lands ")) {
            return Optional.of(Sound.Cue.HIT);
        }
        return Optional.empty();
    }
}
