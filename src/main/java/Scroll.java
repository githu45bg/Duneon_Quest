public class Scroll extends Item {
    private final int manaRestore;

    public Scroll(String name, int manaRestore) {
        super(name, 15);
        this.manaRestore = manaRestore;
    }

    @Override
    public String use(Adventurer user) {
        user.restoreMana(manaRestore);
        return user.getName() + " reads " + getName()
                + " and regains " + manaRestore + " mana.";
    }
    public int getManaRestore() { return manaRestore; }
}
