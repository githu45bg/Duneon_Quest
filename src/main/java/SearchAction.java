public class SearchAction implements Action {
    private static final int RADIUS = 2;
    private static final int ROGUE_RADIUS = 3;

    private final Adventurer searcher;

    public SearchAction(Adventurer searcher) { this.searcher = searcher; }

    @Override
    public String execute(Dungeon d) {
        int radius = searcher.getPlayerClass() == PlayerClass.ROGUE ? ROGUE_RADIUS : RADIUS;
        StringBuilder found = new StringBuilder();
        for (int y = searcher.getY() - radius; y <= searcher.getY() + radius; y++) {
            for (int x = searcher.getX() - radius; x <= searcher.getX() + radius; x++) {
                inspect(d, x, y, radius, found);
            }
        }
        if (found.length() == 0) return searcher.getName() + " searches but finds nothing.";
        return found.toString().trim();
    }

    private void inspect(Dungeon d, int x, int y, int radius, StringBuilder found) {
        if (!d.inBounds(x, y)) return;
        if (!FieldOfView.canSee(d, searcher.getX(), searcher.getY(), x, y, radius)) return;
        Room room = d.getRoom(x, y);
        if (room.getType() == RoomType.TRAP) {
            room.setType(RoomType.FLOOR);
            found.append(searcher.getName()).append(" finds and disarms a trap! ");
        } else if (room.getType() == RoomType.SECRET && !room.isRevealed()) {
            room.reveal();
            found.append(searcher.getName()).append(" uncovers hidden treasure! ");
        }
    }
}