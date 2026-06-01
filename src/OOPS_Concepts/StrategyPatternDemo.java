package OOPS_Concepts;

/**
 * Question: Explain the Strategy design pattern and write a clean implementation.
 */
public class StrategyPatternDemo {

    // 1. Strategy Interface
    interface PaymentStrategy {
        void pay(int amount);
    }

    // 2. Concrete Strategy 1: Credit Card
    static class CreditCardStrategy implements PaymentStrategy {
        private String cardHolder;
        private String cardNumber;

        CreditCardStrategy(String name, String num) {
            this.cardHolder = name;
            this.cardNumber = num;
        }

        @Override
        public void pay(int amount) {
            System.out.println("Processing Card Payment of Rs." + amount + " for " + cardHolder + " (Card Ending: " + cardNumber.substring(cardNumber.length() - 4) + ")");
        }
    }

    // 3. Concrete Strategy 2: UPI
    static class UPIStrategy implements PaymentStrategy {
        private String upiId;

        UPIStrategy(String upiId) {
            this.upiId = upiId;
        }

        @Override
        public void pay(int amount) {
            System.out.println("Processing UPI Payment of Rs." + amount + " via VPA: " + upiId);
        }
    }

    // 4. Context Class
    static class ShoppingCart {
        private int totalAmount = 0;

        public void addItem(String item, int price) {
            System.out.println("Added: " + item + " for Rs." + price);
            totalAmount += price;
        }

        // Dynamically choose strategy at runtime
        public void checkout(PaymentStrategy paymentMethod) {
            paymentMethod.pay(totalAmount);
            totalAmount = 0; // Reset cart
        }
    }

    public static void main(String[] args) {
        ShoppingCart cart = new ShoppingCart();
        cart.addItem("Mechanical Keyboard", 4500);
        cart.addItem("Ergonomic Mouse", 2500);

        System.out.println("\n--- Checkout Scenario 1: Credit Card Strategy ---");
        PaymentStrategy cardPayment = new CreditCardStrategy("Alice Smith", "1234567812349988");
        cart.checkout(cardPayment);

        System.out.println("\n--- Checkout Scenario 2: UPI Strategy ---");
        cart.addItem("Noise Cancelling Headphones", 12000);
        PaymentStrategy upiPayment = new UPIStrategy("alice@okaxis");
        cart.checkout(upiPayment);
    }
}
