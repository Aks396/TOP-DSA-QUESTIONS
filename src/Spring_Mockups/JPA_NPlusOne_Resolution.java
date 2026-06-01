package Spring_Mockups;

import java.util.ArrayList;
import java.util.List;

// Mock JPA / Hibernate annotations
@interface Entity {}
@interface Table { String name(); }
@interface Id {}
@interface GeneratedValue {}
@interface OneToMany { String mappedBy(); String fetch() default "LAZY"; }
@interface ManyToOne {}
@interface JoinColumn { String name(); }
@interface BatchSize { int size(); }
@interface EntityGraph { String attributePaths(); }

/**
 * Question: Explain the JPA N+1 query problem and show how to resolve it using EntityGraph, BatchSize, and JOIN FETCH.
 */
public class JPA_NPlusOne_Resolution {

    // 1. Mock Database Entities
    @Entity
    @Table(name = "orders")
    static class Order {
        @Id
        @GeneratedValue
        private Long id;
        private String customerName;

        // Problem Scenario: LAZY Loading items collection
        @OneToMany(mappedBy = "order", fetch = "LAZY")
        // Solution 3: BatchSize (limits fetching items in chunks instead of 1 by 1)
        @BatchSize(size = 20)
        private List<OrderItem> items = new ArrayList<>();

        public Long getId() { return id; }
        public String getCustomerName() { return customerName; }
        public List<OrderItem> getItems() { return items; }
    }

    @Entity
    @Table(name = "order_items")
    static class OrderItem {
        @Id
        @GeneratedValue
        private Long id;
        private String productName;
        private double price;

        @ManyToOne
        @JoinColumn(name = "order_id")
        private Order order;

        public Long getId() { return id; }
        public String getProductName() { return productName; }
        public double getPrice() { return price; }
    }

    // 2. Mock Repository interface demonstrating solutions
    interface OrderRepository {
        
        // --- 2.1 The N+1 Triggering Query ---
        // Query: SELECT o FROM Order o
        // Outcome: If there are 100 orders, calling order.getItems() on each triggers 100 extra queries. Total = 101 queries!
        List<Order> findAllOrders();

        // --- 2.2 Solution 1: JOIN FETCH ---
        // JPQL: SELECT o FROM Order o JOIN FETCH o.items
        // Outcome: Joins the tables and loads both Order and Items in a SINGLE database query.
        List<Order> findAllOrdersWithJoinFetch();

        // --- 2.3 Solution 2: EntityGraph ---
        // Annotation: @EntityGraph(attributePaths = "items")
        // Outcome: Instructs Hibernate to automatically perform a fetch join during query execution.
        @EntityGraph(attributePaths = "items")
        List<Order> findAllOrdersWithEntityGraph();
    }

    public static void main(String[] args) {
        System.out.println("JPA N+1 Problem Resolution Code Mockup Loaded.");
        System.out.println("Solutions mapped inside OrderRepository class:");
        System.out.println("1. JOIN FETCH JPQL approach");
        System.out.println("2. @EntityGraph annotation placement");
        System.out.println("3. @BatchSize(size = 20) collection throttling placement");
    }
}
