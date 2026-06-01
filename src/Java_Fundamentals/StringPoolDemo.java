package Java_Fundamentals;

/**
 * Question: Explain the String Constant Pool, the difference between == and equals(), and how intern() works.
 */
public class StringPoolDemo {

    public static void main(String[] args) {
        
        // 1. Literal strings go to String Constant Pool
        String s1 = "Java";
        String s2 = "Java";

        // 2. Created on the regular Heap (outside String Pool)
        String s3 = new String("Java");
        String s4 = new String("Java");

        System.out.println("--- 1. Reference Equality (==) ---");
        // Points to same reference in String Pool
        System.out.println("s1 == s2 (Literals): " + (s1 == s2)); // true
        // Points to different objects on regular Heap
        System.out.println("s3 == s4 (Heap objects): " + (s3 == s4)); // false
        // Literal vs Heap Object reference
        System.out.println("s1 == s3 (Literal vs Heap): " + (s1 == s3)); // false

        System.out.println("\n--- 2. Value Equality (equals()) ---");
        // All have the same content "Java"
        System.out.println("s1.equals(s2): " + s1.equals(s2)); // true
        System.out.println("s3.equals(s4): " + s3.equals(s4)); // true
        System.out.println("s1.equals(s3): " + s1.equals(s3)); // true

        System.out.println("\n--- 3. Understanding String.intern() ---");
        // intern() returns the canonical representation from the String Pool
        String s5 = s3.intern(); // Fetch pool reference for value "Java"

        System.out.println("s1 == s5 (Literal vs Interned reference): " + (s1 == s5)); // true
        System.out.println("s3 == s5 (Heap reference vs Interned reference): " + (s3 == s5)); // false

        // Proof of compile-time optimizations (string concatenation of literals)
        String s6 = "Ja" + "va"; // Calculated at compile-time, points to pool
        System.out.println("s1 == s6 (Literal vs concatenated literals): " + (s1 == s6)); // true
    }
}
