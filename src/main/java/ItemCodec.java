/** Turns items into one line of text and back. Fields are separated by ';'. */
public final class ItemCodec {
    private ItemCodec() { }

    public static String encode(Item item) {
        String name = item.getName().replace(';', ',').replace('|', '/');
        if (item instanceof Potion p) return String.join(";", "POTION", name, String.valueOf(p.getHealAmount()));
        if (item instanceof Weapon w) {
            return String.join(";", "WEAPON", name, String.valueOf(w.getDamageDie()), String.valueOf(w.getValue()));
        }
        if (item instanceof Armor a) {
            return String.join(";", "ARMOR", name, String.valueOf(a.getAcBonus()), String.valueOf(a.getValue()));
        }
        if (item instanceof Scroll s) return String.join(";", "SCROLL", name, String.valueOf(s.getManaRestore()));
        return "KEY;" + name;
    }

    public static Item decode(String text) throws SaveException {
        String[] p = text.split(";", -1);
        try {
            return switch (p[0]) {
                case "POTION" -> new Potion(p[1], Integer.parseInt(p[2]));
                case "WEAPON" -> new Weapon(p[1], Integer.parseInt(p[2]), Integer.parseInt(p[3]));
                case "ARMOR"  -> new Armor(p[1], Integer.parseInt(p[2]), Integer.parseInt(p[3]));
                case "SCROLL" -> new Scroll(p[1], Integer.parseInt(p[2]));
                case "KEY"    -> new Key(p[1]);
                default -> throw new SaveException("Unknown item type '" + p[0] + "' in save file.");
            };
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            throw new SaveException("Damaged item entry in save file: " + text);
        }
    }
}
