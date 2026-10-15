/** Gives an item to an adjacent ally. A potion or scroll is used on them straight away. */
public class GiveAction implements Action {
    private final Adventurer giver;
    private final Adventurer target;
    private final Item item;

    public GiveAction(Adventurer giver, Adventurer target, Item item) {
        this.giver = giver;
        this.target = target;
        this.item = item;
    }

    @Override
    public String execute(Dungeon dungeon) {
        if (item instanceof Potion || item instanceof Scroll) return useOnAlly();
        if (!target.getInventory().add(item)) return target.getName() + "'s pack is full.";
        giver.getInventory().remove(item);
        return giver.getName() + " hands " + item.getName() + " to " + target.getName() + ".";
    }

    private String useOnAlly() {
        boolean wasDown = target.isUnconscious();
        String effect = item.use(target);
        giver.getInventory().remove(item);
        String revived = (wasDown && !target.isUnconscious())
                ? " " + target.getName() + " is back on their feet!" : "";
        return giver.getName() + " gives " + item.getName() + " to " + target.getName()
                + ". " + effect + revived;
    }
}
