package compiler.aot;

public class HelloWorldPrinter {

    public static final String MESSAGE = "Hello world";

    public static final String CLINIT_MESSAGE = "Hello <clinit> world";

    static {
        System.out.println(CLINIT_MESSAGE);
    }

    public static void main(String[] args) {
        print();
    }

    public static void print() {
        System.out.println(MESSAGE);
    }
}
