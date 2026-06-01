package Java_Fundamentals;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Question 4: Advanced Java Stream Operations (Java 7 vs Java 8+ Streams)
 */
public class StreamAdvanced {
    public static void main(String[] args) {
        List<Employee> employees = Arrays.asList(
                new Employee("John", "IT", 50000),
                new Employee("Jane", "IT", 60000),
                new Employee("Bob", "HR", 40000),
                new Employee("Alice", "HR", 45000),
                new Employee("Charlie", "Sales", 55000));

        System.out.println("--- Java 7 Imperative Approach ---");
        groupingByJava7(employees);
        averagingSalaryJava7(employees);
        partitioningByJava7(employees);

        System.out.println("\n--- Java 8+ Streams Approach ---");
        groupingByStream(employees);
        averagingSalaryStream(employees);
        partitioningByStream(employees);
    }

    /**
     * Java 7 Approach: Group employees by department
     */
    public static void groupingByJava7(List<Employee> employees) {
        Map<String, List<Employee>> byDept = new HashMap<String, List<Employee>>();
        for (Employee e : employees) {
            List<Employee> list = byDept.get(e.dept);
            if (list == null) {
                list = new ArrayList<Employee>();
                byDept.put(e.dept, list);
            }
            list.add(e);
        }
        System.out.print("By Dept (Java 7): ");
        for (Map.Entry<String, List<Employee>> entry : byDept.entrySet()) {
            System.out.print(entry.getKey() + "=[");
            for (int i = 0; i < entry.getValue().size(); i++) {
                System.out.print(entry.getValue().get(i).name + (i < entry.getValue().size() - 1 ? ", " : ""));
            }
            System.out.print("] ");
        }
        System.out.println();
    }

    /**
     * Java 7 Approach: Average salary by department
     */
    public static void averagingSalaryJava7(List<Employee> employees) {
        Map<String, List<Double>> salariesByDept = new HashMap<String, List<Double>>();
        for (Employee e : employees) {
            List<Double> salaries = salariesByDept.get(e.dept);
            if (salaries == null) {
                salaries = new ArrayList<Double>();
                salariesByDept.put(e.dept, salaries);
            }
            salaries.add(e.salary);
        }

        Map<String, Double> avgSalary = new HashMap<String, Double>();
        for (Map.Entry<String, List<Double>> entry : salariesByDept.entrySet()) {
            double sum = 0;
            for (double sal : entry.getValue()) {
                sum += sal;
            }
            avgSalary.put(entry.getKey(), sum / entry.getValue().size());
        }
        System.out.println("Avg Salary (Java 7): " + avgSalary);
    }

    /**
     * Java 7 Approach: Partition employees with salary > 50000
     */
    public static void partitioningByJava7(List<Employee> employees) {
        Map<Boolean, List<Employee>> partitioned = new HashMap<Boolean, List<Employee>>();
        partitioned.put(true, new ArrayList<Employee>());
        partitioned.put(false, new ArrayList<Employee>());

        for (Employee e : employees) {
            if (e.salary > 50000) {
                partitioned.get(true).add(e);
            } else {
                partitioned.get(false).add(e);
            }
        }
        System.out.println("High Paid Count (Java 7): " + partitioned.get(true).size());
    }

    /**
     * Java 8+ Stream: Group employees by department
     */
    public static void groupingByStream(List<Employee> employees) {
        Map<String, List<Employee>> byDept = employees.stream()
                .collect(Collectors.groupingBy(e -> e.dept));
        System.out.print("By Dept (Stream): ");
        byDept.forEach((dept, list) -> {
            String names = list.stream().map(e -> e.name).collect(Collectors.joining(", "));
            System.out.print(dept + "=[" + names + "] ");
        });
        System.out.println();
    }

    /**
     * Java 8+ Stream: Average salary by department
     */
    public static void averagingSalaryStream(List<Employee> employees) {
        Map<String, Double> avgSalary = employees.stream()
                .collect(Collectors.groupingBy(e -> e.dept, Collectors.averagingDouble(e -> e.salary)));
        System.out.println("Avg Salary (Stream): " + avgSalary);
    }

    /**
     * Java 8+ Stream: Partition employees with salary > 50000
     */
    public static void partitioningByStream(List<Employee> employees) {
        Map<Boolean, List<Employee>> highPaid = employees.stream()
                .collect(Collectors.partitioningBy(e -> e.salary > 50000));
        System.out.println("High Paid Count (Stream): " + highPaid.get(true).size());
    }

    static class Employee {
        String name, dept;
        double salary;

        Employee(String n, String d, double s) {
            name = n;
            dept = d;
            salary = s;
        }
    }
}
