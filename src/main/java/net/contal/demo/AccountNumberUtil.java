package net.contal.demo;
import java.util.Random;

public abstract class AccountNumberUtil {


    /**
     * @return random integer between 100000000 and 999999999 (inclusive)
     */
    public static int generateAccountNumber(){
        Random random = new Random();
        // Generates a random number between 100000000 and 999999999 (inclusive)
        return 100000000 + random.nextInt(900000000);
    }

}
