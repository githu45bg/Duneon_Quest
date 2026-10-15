public class RestAction implements Action {
    private final Character actor;

    public RestAction(Character actor) { this.actor = actor; }

    @Override
    public String execute(Dungeon dungeon) {
        actor.heal(1);
        return actor.getName() + " rests.";
    }
}
