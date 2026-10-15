public enum PlayerClass {
    WARRIOR(30,15,3,1,0,8,"Power Strike"),
    MAGE   (18,11,0,2,20,6,"Fireball"),
    ROGUE  (22,13,1,3,0,6,"Backstab"),
    CLERIC (24,14,1,1,15,8,"Heal");

    private final int maxHP;
    private final int armorClass;
    private final int strMod;
    private final int dexMod;
    private final int maxMana;
    private final int weaponDie;
    private final String abilityName;

    PlayerClass(int maxHP, int armorClass, int strMod,
                int dexMod, int maxMana, int weaponDie, String abilityName){
        this.maxHP=maxHP;
        this.armorClass=armorClass;
        this.strMod=strMod;
        this.dexMod=dexMod;
        this.maxMana=maxMana;
        this.weaponDie=weaponDie;
        this.abilityName=abilityName;
    }
    public int getMaxHP(){
        return maxHP;}

    public int getArmorClass() {
        return armorClass;}

    public int getStrMod() {
        return strMod;}

    public int getDexMod() {
        return dexMod;}

    public int getMaxMana() {
        return maxMana;}

    public int getWeaponDie() {
        return weaponDie;}

    public String getAbilityName() {
        return abilityName;}

    public boolean canCast() {return maxMana>0;}

}
