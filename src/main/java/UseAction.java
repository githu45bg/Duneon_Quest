public class UseAction implements Action {
    private final Adventurer user;
    private final int index;

    public UseAction(Adventurer user, int index) {
        this.user = user;
        this.index = index;
    }

    @Override
    public String execute(Dungeon dungeon) {
        return user.getInventory().get(index)
                .map(item -> {
                    String msg = item.use(user);
                    if (!(item instanceof Weapon || item instanceof Armor)) {
                        user.getInventory().remove(item);   // consumables vanish
                    }
                    return msg;
                })
                .orElse("There is no item in that slot.");
    }
}