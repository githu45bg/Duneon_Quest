public class MoveAction implements Action {
    private final Character actor;
    private final Direction direction;

    public MoveAction(Character actor, Direction direction) {
        this.actor = actor;
        this.direction = direction;
    }

    @Override
    public String execute(Dungeon dungeon) {
        int nx = actor.getX() + direction.dx();
        int ny = actor.getY() + direction.dy();
        if (!dungeon.canEnter(nx, ny)) {
            return actor.getName() + " is blocked.";
        }
        actor.moveTo(nx, ny);
        return actor.getName() + " moves " + direction + ".";
    }
}
