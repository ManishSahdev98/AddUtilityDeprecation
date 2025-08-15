package sample

public class Calculator {
    
    public int add(int a, int b) {
        return a + b;
    }
    
    public int subtract(int a, int b) {
        return a - b;
    }
    
    public int multiply(int a, int b) {
        return a * b;
    }
    
    public int divide(int a, int b) {
        if (b == 0) {
            throw new ArithmeticException("Division by zero");
        }
        return a / b;
    }
/** Do not change without asking MODS Team*/
@Deprecated
    
    public double calculateTotal(int a, int b) {
        return add(a, b) + multiply(a, b);
    }
/** Do not change without asking MODS Team*/
@Deprecated
    
    public void processData(String data) {
        System.out.println("Processing: " + data);
    }
}