public final class TileInteraction {
    private TileInteraction() { }

    /** Called after an adventurer moves. Returns a message, or "" if nothing happened. */
    public static String onEnter(Adventurer a, Dungeon d) {
        Room room = d.getRoom(a.getX(), a.getY());
        return switch (room.getType()) {
            case TREASURE -> takeTreasure(a, room);
            case POTION   -> pickUp(a, room, new Potion("Healing Potion", 10));
            case KEY      -> pickUp(a, room, new Key("Iron Key"));
            case CHEST    -> openChest(a, room);
            case TRAP     -> springTrap(a, room);
            case SECRET   -> takeSecret(a, room);
            default       -> "";
        };
    }

    private static String takeTreasure(Adventurer a, Room room) {
        a.addGold(50);
        room.setType(RoomType.FLOOR);
        return a.getName() + " finds 50 gold!";
    }

    private static String pickUp(Adventurer a, Room room, Item item) {
        if (!a.getInventory().add(item)) return "Your pack is full.";
        room.setType(RoomType.FLOOR);
        return a.getName() + " picks up " + item.getName() + ".";
    }
    private static String springTrap(Adventurer a, Room room) {
        int damage = Dice.roll(1, 6) + 1;
        a.takeDamage(damage);
        room.setType(RoomType.FLOOR);
        Sound.play(Sound.Cue.TRAP);
        return a.getName() + " triggers a trap and takes " + damage + " damage!";
    }

    private static String takeSecret(Adventurer a, Room room) {
        a.addGold(75);
        room.setType(RoomType.FLOOR);
        return a.getName() + " claims the hidden treasure: 75 gold!";
    }

    private static String openChest(Adventurer a, Room room) {
        return a.getInventory().findFirst(Key.class).map(key -> {
            a.getInventory().remove(key);
            Item loot = LootTable.randomGear();
            a.getInventory().add(loot);
            a.addGold(50);
            room.setType(RoomType.FLOOR);
            return a.getName() + " unlocks the chest: 50 gold and " + loot.getName() + "!";
        }).orElse("The chest is locked. You need a key.");
    }
}
