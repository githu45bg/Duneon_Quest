public abstract class Item {
    private final String name;
    private final int value;   // gold value, counts toward treasure score

    protected Item(String name, int value) {
        this.name = name;
        this.value = value;
    }

    /** Applies the item and returns a message for the player. */
    public abstract String use(Adventurer user);

    public String getName() { return name; }
    public int getValue()   { return value; }

    @Override
    public String toString() { return name; }
}