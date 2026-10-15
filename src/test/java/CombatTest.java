import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Tests for the combat maths: attack rolls against armour class, damage, healing,
 * initiative, the monsters' damage dice, the revive countdown and temporary effects.
 */
class CombatTest {
    private static final int TRIALS = 2000;

    /** A punching bag with a fixed armour class, so attack results can be predicted. */
    private static class Dummy extends Character {
        Dummy(int armorClass, int hp) { super("Dummy", hp, armorClass); }

        @Override public int getAttackModifier() { return 0; }
        @Override public int rollDamage() { return 1; }
        @Override protected int getDexModifier() { return 0; }
    }

    @Test
    void lowArmourIsAlwaysHit() {
        // A goblin has +2 to attack, so its lowest roll is 1 + 2 = 3, which beats AC 2.
        Goblin goblin = new Goblin();
        Dummy target = new Dummy(2, 10);
        for (int i = 0; i < TRIALS; i++) assertTrue(goblin.attackRoll(target));
    }

    @Test
    void impossibleArmourIsNeverHit() {
        // The goblin's highest roll is 20 + 2 = 22, which cannot reach AC 23.
        Goblin goblin = new Goblin();
        Dummy target = new Dummy(23, 10);
        for (int i = 0; i < TRIALS; i++) assertFalse(goblin.attackRoll(target));
    }

    @Test
    void hitChanceMatchesTheMaths() {
        // Against AC 12 a goblin needs 10 or more on the d20 (10 + 2 = 12): 11 faces of 20 = 55%.
        // The accepted band (50% to 60%) is far wider than random noise over 20000 rolls.
        Goblin goblin = new Goblin();
        Dummy target = new Dummy(12, 10);
        int trials = 20000;
        int hits = 0;
        for (int i = 0; i < trials; i++) if (goblin.attackRoll(target)) hits++;
        double rate = (double) hits / trials;
        assertTrue(rate > 0.50 && rate < 0.60, "hit rate was " + rate);
    }

    @Test
    void damageNeverGoesBelowZeroHp() {
        Dummy target = new Dummy(10, 10);
        target.takeDamage(25);                       // far more than the 10 HP it has
        assertEquals(0, target.getHp());
        assertFalse(target.isAlive());
    }

    @Test
    void negativeDamageDoesNothing() {
        // Protects against a bug where "negative damage" would secretly heal.
        Dummy target = new Dummy(10, 10);
        target.takeDamage(-5);
        assertEquals(10, target.getHp());
    }

    @Test
    void healingStopsAtMaxHp() {
        Dummy target = new Dummy(10, 10);
        target.takeDamage(4);
        target.heal(100);                            // far more than needed
        assertEquals(10, target.getHp());
    }

    @Test
    void initiativeIsD20PlusDexterity() {
        // A Warrior has +1 DEX, so initiative runs from 1 + 1 = 2 up to 20 + 1 = 21.
        Adventurer warrior = new Adventurer("W", PlayerClass.WARRIOR);
        for (int i = 0; i < TRIALS; i++) {
            int roll = warrior.rollInitiative();
            assertTrue(roll >= 2 && roll <= 21, "initiative " + roll);
        }
    }

    @Test
    void monsterDamageStaysWithinItsDie() {
        // A goblin's weapon die is a d6.
        Goblin goblin = new Goblin();
        for (int i = 0; i < TRIALS; i++) {
            int damage = goblin.rollDamage();
            assertTrue(damage >= 1 && damage <= 6, "goblin damage " + damage);
        }
    }

    @Test
    void woundedOrcHitsHarder() {
        // Polymorphism: Orc overrides rollDamage(). Healthy: a d10 (1 to 10).
        // At half health or less it rages and adds 3, so 4 to 13.
        Orc orc = new Orc();
        for (int i = 0; i < TRIALS; i++) {
            int damage = orc.rollDamage();
            assertTrue(damage >= 1 && damage <= 10, "healthy orc " + damage);
        }
        orc.takeDamage(10);                          // 20 HP orc is now at exactly half
        for (int i = 0; i < TRIALS; i++) {
            int damage = orc.rollDamage();
            assertTrue(damage >= 4 && damage <= 13, "enraged orc " + damage);
        }
    }

    @Test
    void fallenAdventurerDiesAfterThreeRounds() {
        // Brief: unconscious at 0 HP, dead if not revived within 3 rounds.
        Adventurer a = new Adventurer("A", PlayerClass.MAGE);
        a.takeDamage(1000);
        assertTrue(a.isUnconscious());
        a.endOfRound();
        a.endOfRound();
        assertFalse(a.isDead());                     // two rounds: still alive
        a.endOfRound();
        assertTrue(a.isDead());                      // third round: gone
    }

    @Test
    void healingInTimeResetsTheCountdown() {
        // A potion or Cleric spell within the window saves the character, and the
        // next time they fall they get a fresh 3 rounds.
        Adventurer a = new Adventurer("A", PlayerClass.MAGE);
        a.takeDamage(1000);
        a.endOfRound();                              // 1 round used
        a.heal(5);                                   // rescued
        a.endOfRound();                              // conscious again: countdown resets
        a.takeDamage(1000);                          // falls again
        a.endOfRound();
        a.endOfRound();
        assertFalse(a.isDead());                     // only 2 rounds into a fresh countdown
    }

    @Test
    void armourBonusesDoNotStack() {
        // Warrior AC is 15. Swapping armor must replace the old bonus, not add to it.
        Adventurer a = new Adventurer("A", PlayerClass.WARRIOR);
        a.equipArmor(new Armor("Chain Mail", 2, 10));
        assertEquals(17, a.getArmorClass());         // 15 + 2
        a.equipArmor(new Armor("Plate", 3, 10));
        assertEquals(18, a.getArmorClass());         // 15 + 3, not 15 + 2 + 3
    }

    @Test
    void blessingAndCurseLastThreeRounds() {
        // Blessing: +2 AC for 3 rounds. Curse: -1 to all rolls for 3 rounds.
        Adventurer a = new Adventurer("A", PlayerClass.WARRIOR);
        a.getEffects().bless(3);
        a.getEffects().curse(3);
        assertEquals(17, a.getArmorClass());         // 15 + 2
        assertEquals(-1, a.getEffects().rollPenalty());
        a.endOfRound();
        a.endOfRound();
        assertEquals(17, a.getArmorClass());         // still active after 2 rounds
        a.endOfRound();
        assertEquals(15, a.getArmorClass());         // gone after the third
        assertEquals(0, a.getEffects().rollPenalty());
    }
}
