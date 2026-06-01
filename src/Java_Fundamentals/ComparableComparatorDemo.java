package Java_Fundamentals;

import java.util.*;

/**
 * Question: Explain Comparable vs Comparator and show how to implement both in Java 7 and Java 8+.
 */
public class ComparableComparatorDemo {

    // Student implements Comparable for natural ordering (by id)
    static class Student implements Comparable<Student> {
        int id;
        String name;
        int age;

        Student(int id, String name, int age) {
            this.id = id;
            this.name = name;
            this.age = age;
        }

        // Natural order: sort by ID ascending
        @Override
        public int compareTo(Student other) {
            return this.id - other.id;
        }

        @Override
        public String toString() {
            return "Student{id=" + id + ", name='" + name + "', age=" + age + "}";
        }
    }

    public static void main(String[] args) {
        List<Student> students = Arrays.asList(
                new Student(3, "John", 22),
                new Student(1, "Jane", 20),
                new Student(2, "Bob", 21)
        );

        System.out.println("Original List: " + students);

        // 1. Natural sorting using Comparable (Comparable works automatically with Collections.sort)
        List<Student> naturalSorted = new ArrayList<>(students);
        Collections.sort(naturalSorted);
        System.out.println("Natural Sorted (by id): " + naturalSorted);

        // 2. Custom sorting using Comparator (Sort by Name)
        System.out.println("\n--- Sorting by Name ---");

        // Java 7 style: Anonymous class
        List<Student> nameSortedJava7 = new ArrayList<>(students);
        Collections.sort(nameSortedJava7, new Comparator<Student>() {
            @Override
            public int compare(Student s1, Student s2) {
                return s1.name.compareTo(s2.name);
            }
        });
        System.out.println("Java 7 (Anonymous Class): " + nameSortedJava7);

        // Java 8+ style: Lambda expression
        List<Student> nameSortedJava8 = new ArrayList<>(students);
        Collections.sort(nameSortedJava8, (s1, s2) -> s1.name.compareTo(s2.name));
        System.out.println("Java 8+ (Lambda): " + nameSortedJava8);

        // 3. Custom sorting using Comparator (Sort by Age)
        System.out.println("\n--- Sorting by Age ---");

        // Java 7 style
        List<Student> ageSortedJava7 = new ArrayList<>(students);
        Collections.sort(ageSortedJava7, new Comparator<Student>() {
            @Override
            public int compare(Student s1, Student s2) {
                return s1.age - s2.age;
            }
        });
        System.out.println("Java 7 (Anonymous Class): " + ageSortedJava7);

        // Java 8+ style: Comparator.comparing method reference
        List<Student> ageSortedJava8 = new ArrayList<>(students);
        ageSortedJava8.sort(Comparator.comparingInt(s -> s.age));
        System.out.println("Java 8+ (Comparator.comparingInt): " + ageSortedJava8);
    }
}
