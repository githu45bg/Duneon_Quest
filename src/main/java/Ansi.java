public final class Ansi {
    //utility class for adding colors and text formatting to java console output
    /*  public- class can be accessed from another class
    final- the class cannot be inherited/ extended by another class
     */
    public static final String RESET   = "\u001B[0m";
    public static final String RED     = "\u001B[31m";
    public static final String GREEN   = "\u001B[32m";
    public static final String YELLOW  = "\u001B[33m";
    public static final String MAGENTA = "\u001B[35m";
    public static final String CYAN    = "\u001B[36m";
    public static final String WHITE   = "\u001B[37m";
    public static final String BRIGHT  = "\u001B[1;37m";
    public static final String DARK    = "\u001B[38;5;240m";

    private Ansi() { }

    public static String paint(String colour, String text) {
        return colour + text + RESET;
    }
}
