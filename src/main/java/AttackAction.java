public class AttackAction implements Action {
    // one character attacks another
    private final Character attacker;
    private final Character target;
    // constructor= stores attacker that was provided by the object

    public AttackAction(Character attacker, Character target) {
        this.attacker = attacker;
        this.target = target;
    }

    @Override
    public String execute(Dungeon dungeon) {// execute method performs an attack
        // @overrid tells java= this method is implementing a method
        if (!target.isAlive()) {// checks whether the target is alive
            return target.getName() + " is already down.";//target dead
        }
        if (!isAdjacent()) {
            return target.getName() + " is out of reach.";
        }
        if (!attacker.attackRoll(target)) {// check whether the attack hits
            return attacker.getName() + " misses " + target.getName() + ".";
        }
        int damage = attacker.rollDamage();// if attack successfully hits, calculate how much damage they heal
        target.takeDamage(damage);// sends damage to the target
        return attacker.getName() + " hits " + target.getName()
                + " for " + damage + " damage!";// return stops the attack
        // returns the attack message
    }

    private boolean isAdjacent() {//check attack distance, whether the attacker is close enough to attack
        int dist = Math.abs(attacker.getX() - target.getX())//distance calculation/manhattan distance
                + Math.abs(attacker.getY() - target.getY());// maths.abs()=absolute value
        return dist == 1;
    }
}
