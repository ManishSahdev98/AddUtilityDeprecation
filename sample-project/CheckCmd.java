package sample;

public class CheckCmd {
    private String command;
    public int execute(int a, int b) {
        return 2 * a + 3 * b;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }
}