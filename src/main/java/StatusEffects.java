public class StatusEffects {
    private static final int BLESS_AC = 2;
    private static final int CURSE_PENALTY = -1;

    private int blessedRounds;
    private int cursedRounds;

    public void bless(int rounds) { blessedRounds = Math.max(blessedRounds, rounds); }
    public void curse(int rounds) { cursedRounds = Math.max(cursedRounds, rounds); }

    public int getBlessedRounds() { return blessedRounds; }
    public int getCursedRounds()  { return cursedRounds; }

    public int acBonus()      { return blessedRounds > 0 ? BLESS_AC : 0; }
    public int rollPenalty()  { return cursedRounds > 0 ? CURSE_PENALTY : 0; }
    public boolean isBlessed() { return blessedRounds > 0; }
    public boolean isCursed()  { return cursedRounds > 0; }

    public void tick() {
        if (blessedRounds > 0) blessedRounds--;
        if (cursedRounds > 0) cursedRounds--;
    }
}
