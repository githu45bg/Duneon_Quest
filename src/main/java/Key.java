public class Key extends Item {
    public Key(String name) {
        super(name, 5);
    }

    @Override
    public String use(Adventurer user) {
        return "Walk into a locked chest to use this key.";
    }
}