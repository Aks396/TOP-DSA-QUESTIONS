package OOPS_Concepts;

/**
 * Question: How do you implement a thread-safe Singleton? Show Double-Checked Locking and Enum Singleton.
 */
public class SingletonDemo {

    // 1. Double-Checked Locking (Lazy Initialization)
    static class LazySingleton {
        // volatile is critical: ensures write to instance is complete before reference is published
        private static volatile LazySingleton instance;

        private LazySingleton() {
            // Protect against reflection instantiation
            if (instance != null) {
                throw new IllegalStateException("Already instantiated!");
            }
        }

        public static LazySingleton getInstance() {
            if (instance == null) {                         // 1st Check (Performance helper)
                synchronized (LazySingleton.class) {        // Lock acquisition
                    if (instance == null) {                 // 2nd Check (Actual thread safety verification)
                        instance = new LazySingleton();
                    }
                }
            }
            return instance;
        }

        public void printMsg() {
            System.out.println("LazySingleton HashCode: " + this.hashCode());
        }
    }

    // 2. Enum Singleton (Eager Initialization - Reflection, Serialization, and Thread Safe automatically)
    enum EnumSingleton {
        INSTANCE;

        public void printMsg() {
            System.out.println("EnumSingleton HashCode: " + this.hashCode());
        }
    }

    public static void main(String[] args) {
        System.out.println("--- 1. Testing Double-Checked Locking (Lazy) Singleton ---");
        LazySingleton lazy1 = LazySingleton.getInstance();
        LazySingleton lazy2 = LazySingleton.getInstance();

        lazy1.printMsg();
        lazy2.printMsg();
        System.out.println("Are both Lazy instances equal? " + (lazy1 == lazy2)); // true

        System.out.println("\n--- 2. Testing Enum Singleton ---");
        EnumSingleton enum1 = EnumSingleton.INSTANCE;
        EnumSingleton enum2 = EnumSingleton.INSTANCE;

        enum1.printMsg();
        enum2.printMsg();
        System.out.println("Are both Enum instances equal? " + (enum1 == enum2)); // true
    }
}
