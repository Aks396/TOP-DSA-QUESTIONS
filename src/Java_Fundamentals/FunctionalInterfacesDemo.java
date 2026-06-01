package Java_Fundamentals;

import java.util.function.*;

/**
 * Question: Explain Lambdas, Functional Interfaces, and Method References in Java 8+.
 */
public class FunctionalInterfacesDemo {

    // Custom Functional Interface
    @FunctionalInterface
    interface MathOperation {
        int operate(int a, int b);
    }

    public static void main(String[] args) {
        
        System.out.println("--- 1. Custom Functional Interface Implementation ---");

        // Java 7 style: Anonymous Class
        MathOperation addJava7 = new MathOperation() {
            @Override
            public int operate(int a, int b) {
                return a + b;
            }
        };
        System.out.println("Java 7 Addition: " + addJava7.operate(10, 5));

        // Java 8+ style: Lambda expression
        MathOperation addJava8 = (a, b) -> a + b;
        System.out.println("Java 8+ Addition: " + addJava8.operate(10, 5));

        System.out.println("\n--- 2. Built-in Functional Interfaces ---");

        // Predicate: takes T, returns boolean
        Predicate<String> isLongWord = s -> s.length() > 5;
        System.out.println("Is 'Lambda' long? " + isLongWord.test("Lambda")); // true

        // Function: takes T, returns R
        Function<String, Integer> stringLength = s -> s.length();
        System.out.println("Length of 'Antigravity': " + stringLength.apply("Antigravity")); // 12

        // Consumer: takes T, returns void
        Consumer<String> printer = s -> System.out.println("Consumer printing: " + s);
        printer.accept("Hello World");

        // Supplier: takes nothing, returns T
        Supplier<Double> randomValue = () -> Math.random();
        System.out.println("Random Value from Supplier: " + randomValue.get());

        System.out.println("\n--- 3. Method References (::) ---");

        // Static Method Reference: ClassName::staticMethodName
        Function<Double, Double> sqrtLambda = d -> Math.sqrt(d);
        Function<Double, Double> sqrtMethodRef = Math::sqrt;
        System.out.println("Sqrt Method Ref (16.0): " + sqrtMethodRef.apply(16.0));

        // Instance Method Reference of a Particular Object: instance::methodName
        String message = "hello";
        Supplier<String> upperLambda = () -> message.toUpperCase();
        Supplier<String> upperMethodRef = message::toUpperCase;
        System.out.println("Upper Method Ref: " + upperMethodRef.get());
    }
}
