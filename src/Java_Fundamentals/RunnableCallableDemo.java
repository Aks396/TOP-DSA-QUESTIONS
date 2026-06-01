package Java_Fundamentals;

import java.io.IOException;
import java.util.concurrent.*;

/**
 * Question: Explain the difference between Runnable and Callable. Show how they are handled inside an ExecutorService.
 */
public class RunnableCallableDemo {

    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(2);

        // 1. Runnable implementation: void return type, cannot throw checked exceptions
        Runnable runnableTask = new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(500);
                    System.out.println("Runnable Task executing in thread: " + Thread.currentThread().getName());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        };

        // 2. Callable implementation: returns value, can throw checked exceptions
        Callable<String> callableTask = new Callable<String>() {
            @Override
            public String call() throws IOException, InterruptedException {
                Thread.sleep(500);
                System.out.println("Callable Task executing in thread: " + Thread.currentThread().getName());
                // Simulated checked exception throwing if condition is met
                if (Math.random() < 0.1) {
                    throw new IOException("Simulated I/O Failure!");
                }
                return "Result from Callable!";
            }
        };

        System.out.println("Submitting tasks...");

        // Submit Runnable
        Future<?> runnableFuture = executor.submit(runnableTask);
        // Submit Callable
        Future<String> callableFuture = executor.submit(callableTask);

        try {
            // Runnable get() returns null on successful completion
            Object runnableResult = runnableFuture.get();
            System.out.println("Runnable get() result: " + runnableResult);

            // Callable get() returns the actual calculated result
            String callableResult = callableFuture.get();
            System.out.println("Callable get() result: " + callableResult);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            System.out.println("Caught exception thrown from within task: " + e.getCause().getMessage());
        }

        // Java 8+ Lambda syntax demonstration
        System.out.println("\n--- Java 8+ Lambda syntax ---");
        executor.submit(() -> System.out.println("Runnable Lambda running."));
        Future<Integer> lambdaCallable = executor.submit(() -> {
            System.out.println("Callable Lambda running.");
            return 42;
        });

        try {
            System.out.println("Lambda Callable result: " + lambdaCallable.get());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Shut down the executor service
        executor.shutdown();
    }
}
