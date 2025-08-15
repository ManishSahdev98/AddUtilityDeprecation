package sample

public class MathUtils {
    
    public static int factorial(int n) {
        if (n <= 1) return 1;
        return n * factorial(n - 1);
    }
    
    public static boolean isPrime(int n) {
        if (n < 2) return false;
        for (int i = 2; i <= Math.sqrt(n); i++) {
            if (n % i == 0) return false;
        }
        return true;
    }
/** Do not change without asking MODS Team*/
@Deprecated
    
    public double calculateTotal(int a, int b) {
        return a + b;
    }
}