package Spring_Mockups;

// Mock Spring Annotations
@interface Service {}
@interface Transactional {}
@interface Autowired {}

/**
 * Question: Explain the @Transactional self-invocation issue in Spring Boot. Write a mockup to show why it occurs and how to resolve it.
 */
public class TransactionalSelfInvocation {

    // -----------------------------------------------------------------------
    // 1. Buggy Scenario: Bypassing the proxy due to self-invocation
    // -----------------------------------------------------------------------
    @Service
    static class BuggyOrderService {

        // Non-transactional method
        public void processCheckout() {
            System.out.println("Step 1: Starting order checkout...");
            
            // BUG: Invoking a transactional method within the same class using "this"
            // Because this is a direct reference call, it bypasses the Spring AOP Proxy.
            // As a result, NO database transaction is initiated for savePaymentData()!
            savePaymentData(); 
        }

        @Transactional
        public void savePaymentData() {
            System.out.println("Step 2: Saving payment data under database transaction (Will fail to open transaction!)");
        }
    }

    // -----------------------------------------------------------------------
    // 2. Refactored Solution: Extracting the transactional logic to a separate service bean
    // -----------------------------------------------------------------------
    @Service
    static class PaymentService {
        
        // Transaction is correctly initiated here when called via AOP Proxy
        @Transactional
        public void savePaymentData() {
            System.out.println("Step 2 (Fixed): Saving payment data under transaction (Successfully intercepted by AOP Proxy!)");
        }
    }

    @Service
    static class FixedOrderService {
        @Autowired
        private PaymentService paymentService; // Inject the payment service bean

        public void processCheckout() {
            System.out.println("Step 1 (Fixed): Starting order checkout...");
            
            // FIXED: Calling the transactional method on the injected proxy bean.
            // Spring will correctly intercept the call and open a JDBC transaction.
            paymentService.savePaymentData();
        }
    }

    public static void main(String[] args) {
        System.out.println("Spring AOP Proxy @Transactional Self-Invocation Mockup Loaded.");
        System.out.println("Issue: Direct method calls bypass Spring proxy wrappers.");
        System.out.println("Solution: Extract transactional behaviors into separate service classes (e.g. PaymentService).");
    }
}
