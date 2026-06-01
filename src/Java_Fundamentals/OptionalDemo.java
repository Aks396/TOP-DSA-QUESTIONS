package Java_Fundamentals;

import java.util.Optional;

/**
 * Question: Explain Java 8 Optional and contrast its usage with legacy Java 7 null-checking patterns.
 */
public class OptionalDemo {

    static class User {
        String name;
        Address address;

        User(String name, Address address) {
            this.name = name;
            this.address = address;
        }

        public Address getAddress() {
            return address;
        }
    }

    static class Address {
        String city;

        Address(String city) {
            this.city = city;
        }

        public String getCity() {
            return city;
        }
    }

    // Java 7 Approach: Deep nested null checks
    public static String getCityNameJava7(User user) {
        if (user != null) {
            Address address = user.getAddress();
            if (address != null) {
                String city = address.getCity();
                if (city != null) {
                    return city;
                }
            }
        }
        return "Unknown City";
    }

    // Java 8+ Approach: Optional pipeline
    public static String getCityNameJava8(User user) {
        return Optional.ofNullable(user)
                .map(User::getAddress)
                .map(Address::getCity)
                .orElse("Unknown City");
    }

    public static void main(String[] args) {
        User userWithAddress = new User("Alice", new Address("San Francisco"));
        User userWithoutAddress = new User("Bob", null);
        User nullUser = null;

        System.out.println("--- Java 7 Nested Null Checking ---");
        System.out.println("Alice: " + getCityNameJava7(userWithAddress));
        System.out.println("Bob: " + getCityNameJava7(userWithoutAddress));
        System.out.println("Null User: " + getCityNameJava7(nullUser));

        System.out.println("\n--- Java 8+ Optional Pipelines ---");
        System.out.println("Alice: " + getCityNameJava8(userWithAddress));
        System.out.println("Bob: " + getCityNameJava8(userWithoutAddress));
        System.out.println("Null User: " + getCityNameJava8(nullUser));

        // Additional Optional patterns (ifPresent, filter)
        System.out.println("\n--- Additional Optional Operators ---");
        Optional<User> optionalUser = Optional.ofNullable(userWithAddress);
        optionalUser.ifPresent(u -> System.out.println("User is present: " + u.name));

        // Filtering using optional
        Optional<User> filtered = optionalUser.filter(u -> u.name.startsWith("A"));
        System.out.println("Filtered starting with 'A' present? " + filtered.isPresent());
    }
}
