import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * Tests which sound goes with which action. Only the CHOICE of sound is tested
 * (SoundEffects.cueFor), so nothing is played and the tests stay silent.
 */
class SoundEffectsTest {
    private static final int TRIALS = 300;

    /** A punching bag with a fixed armour class, so hits and misses can be forced. */
    private static class Dummy extends Character {
        Dummy(int armorClass) { super("Dummy", 500, armorClass); }

        @Override public int getAttackModifier() { return 0; }
        @Override public int rollDamage() { return 1; }
        @Override protected int getDexModifier() { return 0; }
    }

    /** Helper: a small open room with a wall border. */
    private Dungeon room() {
        Dungeon d = new Dungeon(9, 5);
        for (int y = 1; y < 4; y++) {
            for (int x = 1; x < 8; x++) d.setRoom(x, y, RoomType.FLOOR);
        }
        return d;
    }

    /** Helper: runs the real action, then asks which sound its result deserves. */
    private Optional<Sound.Cue> cue(Action action, Dungeon d) {
        return SoundEffects.cueFor(action, action.execute(d));
    }

    @Test
    void everyStepMakesAStepSound() {
        Dungeon d = room();
        Adventurer a = new Adventurer("A", PlayerClass.WARRIOR, 1, 1);
        d.addAdventurer(a);
        assertEquals(Optional.of(Sound.Cue.STEP), cue(new MoveAction(a, Direction.E), d));
    }

    @Test
    void aSwingThatHitsAndOneThatMissesSoundDifferent() {
        Dungeon d = room();
        Adventurer a = new Adventurer("A", PlayerClass.WARRIOR, 1, 1);
        Dummy easy = new Dummy(1);                            // armour so low it is always hit
        Dummy hard = new Dummy(99);                           // armour so high it is never hit
        easy.moveTo(2, 1);
        hard.moveTo(2, 1);
        assertEquals(Optional.of(Sound.Cue.HIT), cue(new AttackAction(a, easy), d));
        assertEquals(Optional.of(Sound.Cue.MISS), cue(new AttackAction(a, hard), d));
    }

    @Test
    void classAbilitiesSoundLikeAttacksToo() {
        // The special attack can hit or miss at random, so run it many times and check
        // that each outcome got the matching sound, and that both outcomes happened.
        Dungeon d = room();
        Adventurer a = new Adventurer("A", PlayerClass.ROGUE, 1, 1);
        int hits = 0;
        int misses = 0;
        for (int i = 0; i < TRIALS; i++) {
            Goblin goblin = new Goblin();
            goblin.moveTo(2, 1);
            String message = new SpecialAttackAction(a, goblin).execute(d);
            Optional<Sound.Cue> cue = SoundEffects.cueFor(new SpecialAttackAction(a, goblin), message);
            if (message.contains("misses")) {
                assertEquals(Optional.of(Sound.Cue.MISS), cue);
                misses++;
            } else {
                assertEquals(Optional.of(Sound.Cue.HIT), cue);
                hits++;
            }
        }
        assertTrue(hits > 0 && misses > 0, "hits " + hits + ", misses " + misses);
    }

    @Test
    void aFireballMakesASpellSoundButAHealDoesNot() {
        Dungeon d = room();
        Adventurer mage = new Adventurer("M", PlayerClass.MAGE, 1, 1);
        Adventurer cleric = new Adventurer("C", PlayerClass.CLERIC, 5, 3);
        Adventurer ally = new Adventurer("Ally", PlayerClass.WARRIOR, 6, 3);
        Goblin goblin = new Goblin();
        goblin.moveTo(3, 1);                                  // 2 cells from the mage
        d.addAdventurer(mage);
        d.addAdventurer(cleric);
        d.addAdventurer(ally);
        d.addMonster(goblin);
        ally.takeDamage(10);                                  // the cleric has someone to heal
        assertEquals(Optional.of(Sound.Cue.SPELL), cue(new CastAction(mage), d));
        assertEquals(Optional.empty(), cue(new CastAction(cleric), d));
    }

    @Test
    void disarmingATrapChimesButAnEmptySearchIsSilent() {
        Dungeon d = room();
        Adventurer rogue = new Adventurer("R", PlayerClass.ROGUE, 2, 2);
        d.addAdventurer(rogue);
        assertEquals(Optional.of(Sound.Cue.DISARM), cueAfterTrap(rogue, d));
        assertEquals(Optional.empty(), cue(new SearchAction(rogue), d));   // trap already gone
    }

    private Optional<Sound.Cue> cueAfterTrap(Adventurer rogue, Dungeon d) {
        d.setRoom(3, 2, RoomType.TRAP);
        return cue(new SearchAction(rogue), d);
    }

    @Test
    void restingAndOtherActionsAreSilent() {
        Dungeon d = room();
        Adventurer a = new Adventurer("A", PlayerClass.WARRIOR, 1, 1);
        assertEquals(Optional.empty(), cue(new RestAction(a), d));
    }
}