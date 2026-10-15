import java.util.List;

public final class PartyPlacer {
    private PartyPlacer() { }

    public static void place(Dungeon d, List<Adventurer> party) {
        for (Adventurer a : party) {
            int[] spot = Pathfinder.nearestFree(d, d.getStartX(), d.getStartY())
                    .orElse(new int[] {d.getStartX(), d.getStartY()});
            a.moveTo(spot[0], spot[1]);
            d.addAdventurer(a);
        }
    }
}