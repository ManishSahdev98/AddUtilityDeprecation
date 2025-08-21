package sample;

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

    public double calculateTotal(int a, int b) {
        CheckCmd checkCmd = new CheckCmd();
        a = checkCmd.execute(a, b);
        checkCmd.setCommand("calculateTotal");
        return add(a, b) + multiply(a, b);
    }
    
    public void processData(String data) {
        System.out.println("Processing: " + data);
    }
}