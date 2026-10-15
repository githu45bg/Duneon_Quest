public final class HelpText {
    private HelpText() { }

    public static final String TEXT = """
            ----- HELP -----
            N / S / E / W : move
            M             : open the Dungeon Master console (password needed;) 
            A             : attack an adjacent Monster
            U <n>         : use item number n (see I)
            R             : rest (+1 HP)
            C             : cast your spell (Mage: Fireball, Cleric: Heal)
            X             : class ability (Warrior: Power Strike, Rogue: Backstab)
            F             : find hidden traps and treasure nearby
            I             : show your inventory
            G <n> <p>     : give item n to the adjacent player p (a potion revives a fallen friend)
            SOUND         : turn the beeps on or off
            H             : show this help
            SAVE [name]   : save the game to saves/<name>.txt (default name: quicksave)
            Q             : quit the game
            Walk onto T, P, K and C to collectxp. Reach E to escape!
            """;
}
