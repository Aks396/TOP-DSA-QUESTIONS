package OOPS_Concepts;

import java.util.ArrayList;
import java.util.List;

/**
 * Question: Explain the Observer design pattern and write a clean implementation of it.
 */
public class ObserverPatternDemo {

    // 1. Observer Interface
    interface Observer {
        void update(String news);
    }

    // 2. Subject Interface
    interface Subject {
        void registerObserver(Observer observer);
        void removeObserver(Observer observer);
        void notifyObservers();
    }

    // 3. Concrete Subject (News Agency publishes news updates)
    static class NewsAgency implements Subject {
        private final List<Observer> observers = new ArrayList<>();
        private String latestNews;

        public void setNews(String news) {
            System.out.println("\nNews Agency received hot update: " + news);
            this.latestNews = news;
            notifyObservers(); // Automatically alert all registered observers
        }

        @Override
        public void registerObserver(Observer observer) {
            observers.add(observer);
        }

        @Override
        public void removeObserver(Observer observer) {
            observers.remove(observer);
        }

        @Override
        public void notifyObservers() {
            for (Observer observer : observers) {
                observer.update(latestNews);
            }
        }
    }

    // 4. Concrete Observer 1
    static class EmailSubscriber implements Observer {
        private String email;

        EmailSubscriber(String email) {
            this.email = email;
        }

        @Override
        public void update(String news) {
            System.out.println("Email Sent to [" + email + "] - BREAKING NEWS: " + news);
        }
    }

    // 5. Concrete Observer 2
    static class SMSSubscriber implements Observer {
        private String phone;

        SMSSubscriber(String phone) {
            this.phone = phone;
        }

        @Override
        public void update(String news) {
            System.out.println("SMS Sent to [" + phone + "] - BREAKING: " + news);
        }
    }

    public static void main(String[] args) {
        NewsAgency agency = new NewsAgency();

        Observer emailSub = new EmailSubscriber("john.doe@example.com");
        Observer smsSub = new SMSSubscriber("+19876543210");

        // Register subscribers
        agency.registerObserver(emailSub);
        agency.registerObserver(smsSub);

        // Publish updates
        agency.setNews("AI breakthroughs disrupt software engineering landscape.");

        // Deregister one subscriber
        System.out.println("\n--- Removing SMS Subscriber ---");
        agency.removeObserver(smsSub);

        // Publish new updates (only email should receive this)
        agency.setNews("Tech stock markets reach all-time high.");
    }
}
