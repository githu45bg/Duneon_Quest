import java.util.Scanner;

public class Merchant implements Event {
    private static final int PRICE = 30;

    @Override public String getName() { return "Merchant"; }

    @Override
    public String trigger(Dungeon d, Scanner in) {
        System.out.println("A wandering merchant appears! Healing Potions: "
                + PRICE + " gold each.");
        for (Adventurer a : d.livingAdventurers()) {
            if (a.getGold() < PRICE) {
                System.out.println(a.getName() + " has only " + a.getGold() + " gold.");
            } else {
                offer(a, in);
            }
        }
        return "The merchant packs up and wanders off.";
    }

    private void offer(Adventurer a, Scanner in) {
        System.out.print(a.getName() + " has " + a.getGold() + " gold. Buy a potion? (Y/N): ");
        String answer = in.hasNextLine() ? in.nextLine().trim() : "N";
        if (!answer.equalsIgnoreCase("Y")) return;
        if (!a.getInventory().add(new Potion("Healing Potion", 10))) {
            System.out.println(a.getName() + "'s pack is full.");
            return;
        }
        a.spendGold(PRICE);
        System.out.println(a.getName() + " buys a Healing Potion.");
    }
}