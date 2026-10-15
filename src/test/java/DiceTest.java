import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Tests for Dice, the static helper that rolls every die in the game.
 * Dice is random, so each test rolls many times and checks the RULES
 * (range, coverage, errors) and not one particular result.
 */
class DiceTest {
    // Enough rolls that a bug would almost certainly show up.
    private static final int TRIALS = 2000;

    @Test
    void singleDieStaysInRange() {
        // A d6 must only ever give 1 to 6: never 0, never 7.
        for (int i = 0; i < TRIALS; i++) {
            int roll = Dice.roll(1, 6);
            assertTrue(roll >= 1 && roll <= 6, "d6 rolled " + roll);
        }
    }

    @Test
    void everyFaceComesUp() {
        // Guards against a die that is biased or can never roll one face.
        boolean[] seen = new boolean[7];
        for (int i = 0; i < TRIALS; i++) seen[Dice.roll(1, 6)] = true;
        for (int face = 1; face <= 6; face++) {
            assertTrue(seen[face], "face " + face + " never rolled");
        }
    }

    @Test
    void severalDiceAddUp() {
        // 3d4: the lowest total is 3 (all ones), the highest is 12 (all fours).
        for (int i = 0; i < TRIALS; i++) {
            int roll = Dice.roll(3, 4);
            assertTrue(roll >= 3 && roll <= 12, "3d4 rolled " + roll);
        }
    }

    @Test
    void modifierShiftsTheWholeRange() {
        // d20 + 5 gives 6 to 25. d20 - 3 gives -2 to 17 (negative modifiers work too).
        for (int i = 0; i < TRIALS; i++) {
            int up = Dice.rollWithModifier(1, 20, 5);
            int down = Dice.rollWithModifier(1, 20, -3);
            assertTrue(up >= 6 && up <= 25, "d20+5 rolled " + up);
            assertTrue(down >= -2 && down <= 17, "d20-3 rolled " + down);
        }
    }

    @Test
    void impossibleDiceAreRejected() {
        // Zero dice, negative dice, or a one-sided die make no sense: Dice must refuse.
        assertThrows(IllegalArgumentException.class, () -> Dice.roll(0, 6));
        assertThrows(IllegalArgumentException.class, () -> Dice.roll(-1, 6));
        assertThrows(IllegalArgumentException.class, () -> Dice.roll(2, 1));
    }
}
