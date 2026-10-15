public class Weapon extends Item {
    private final int damageDie;

    public Weapon(String name, int damageDie, int value) {
        super(name, value);
        this.damageDie = damageDie;
    }

    public int getDamageDie() { return damageDie; }

    @Override
    public String use(Adventurer user) {
        user.equipWeapon(this);
        return user.getName() + " wields " + getName() + " (d" + damageDie + ").";
    }
}