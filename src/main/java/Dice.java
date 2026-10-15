import java.util.Random;
import java.util.List;

public final class Dice {
    private static final Random RNG=new Random();
    // class that generates random numbers


    private Dice(){ }
    //private constructor
    //prevents objects from being created

    public static <T> T pick(List<T> items) {
        return items.get(RNG.nextInt(items.size()));
    }
    // method randomly chooses one item (warrior, mage, rogue, cleric)

    //rolls one or more dice
    public static int roll(int n, int sides){
        // rolls the dice/ 2 dice 6 sides each
        if(n<1 || sides<2){//validates
            throw new IllegalArgumentException(
                    "Invalid dice: "+n+"d"+sides);
        }
        //repeat dice rolling
        int total=0;
        for(int i=0; i<n; i++){
            total+=RNG.nextInt(sides)+1;
        }
        return total;

    }
    //rolls dice and adds a modifier
    public static int rollWithModifier(int n,int sides, int modifier){
        return roll(n,sides)+modifier;
    }

}
