package Java_Fundamentals;

import java.util.*;
import java.util.concurrent.*;

/**
 * Question: Explain the difference between fail-fast and fail-safe iterators. Show examples of both.
 */
public class FailFastSafeDemo {

    public static void main(String[] args) {
        
        System.out.println("--- 1. Fail-Fast Iterator (ArrayList) ---");
        List<String> list = new ArrayList<>();
        list.add("A");
        list.add("B");
        list.add("C");

        try {
            Iterator<String> it = list.iterator();
            while (it.hasNext()) {
                String val = it.next();
                System.out.println("Reading: " + val);
                // Structural modification during iteration triggers exception
                if (val.equals("A")) {
                    list.add("D"); 
                }
            }
        } catch (ConcurrentModificationException e) {
            System.out.println("Caught Expected: ConcurrentModificationException (Fail-Fast behavior)");
        }

        System.out.println("\n--- 2. Fail-Safe Iterator (CopyOnWriteArrayList) ---");
        List<String> failSafeList = new CopyOnWriteArrayList<>();
        failSafeList.add("A");
        failSafeList.add("B");
        failSafeList.add("C");

        Iterator<String> itSafe = failSafeList.iterator();
        while (itSafe.hasNext()) {
            String val = itSafe.next();
            System.out.println("Reading: " + val);
            // This is allowed because iteration occurs on a snapshot copy
            if (val.equals("A")) {
                failSafeList.add("D");
            }
        }
        System.out.println("Final list state: " + failSafeList);

        System.out.println("\n--- 3. Fail-Safe Iterator (ConcurrentHashMap) ---");
        Map<String, Integer> map = new ConcurrentHashMap<>();
        map.put("One", 1);
        map.put("Two", 2);

        Iterator<String> keyIterator = map.keySet().iterator();
        while (keyIterator.hasNext()) {
            String key = keyIterator.next();
            System.out.println("Reading Key: " + key + ", Value: " + map.get(key));
            // This does not throw exception in ConcurrentHashMap (weakly consistent iterator)
            if (key.equals("One")) {
                map.put("Three", 3);
            }
        }
        System.out.println("Final map state: " + map);
    }
}
