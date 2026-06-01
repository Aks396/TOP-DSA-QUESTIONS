package OOPS_Concepts;

/**
 * Question: Explain the Builder design pattern. Implement a thread-safe / clean Builder demo.
 */
public class BuilderPatternDemo {

    static class Computer {
        // Required parameters
        private final String HDD;
        private final String RAM;

        // Optional parameters
        private final boolean isGraphicsCardEnabled;
        private final boolean isBluetoothEnabled;

        public String getHDD() { return HDD; }
        public String getRAM() { return RAM; }
        public boolean isGraphicsCardEnabled() { return isGraphicsCardEnabled; }
        public boolean isBluetoothEnabled() { return isBluetoothEnabled; }

        // Private constructor so objects can only be created via the Builder
        private Computer(Builder builder) {
            this.HDD = builder.HDD;
            this.RAM = builder.RAM;
            this.isGraphicsCardEnabled = builder.isGraphicsCardEnabled;
            this.isBluetoothEnabled = builder.isBluetoothEnabled;
        }

        @Override
        public String toString() {
            return "Computer{HDD='" + HDD + "', RAM='" + RAM + 
                   "', GPU=" + isGraphicsCardEnabled + 
                   ", Bluetooth=" + isBluetoothEnabled + "}";
        }

        // Builder Class
        public static class Builder {
            // Required parameters
            private final String HDD;
            private final String RAM;

            // Optional parameters - initialized to default values
            private boolean isGraphicsCardEnabled = false;
            private boolean isBluetoothEnabled = false;

            // Constructor for required parameters
            public Builder(String hdd, String ram) {
                this.HDD = hdd;
                this.RAM = ram;
            }

            // Setter-like methods returning Builder reference for chaining
            public Builder setGraphicsCardEnabled(boolean isGraphicsCardEnabled) {
                this.isGraphicsCardEnabled = isGraphicsCardEnabled;
                return this;
            }

            public Builder setBluetoothEnabled(boolean isBluetoothEnabled) {
                this.isBluetoothEnabled = isBluetoothEnabled;
                return this;
            }

            // Build method to instantiate actual object
            public Computer build() {
                return new Computer(this);
            }
        }
    }

    public static void main(String[] args) {
        // Constructing a high-end gaming PC with graphics and bluetooth enabled
        Computer gamingPC = new Computer.Builder("2TB SSD", "32GB DDR5")
                .setGraphicsCardEnabled(true)
                .setBluetoothEnabled(true)
                .build();

        // Constructing a basic office PC with defaults
        Computer officePC = new Computer.Builder("500GB HDD", "8GB DDR4")
                .build();

        System.out.println("Gaming Configuration: " + gamingPC);
        System.out.println("Office Configuration: " + officePC);
    }
}
