import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class ShoppingDataAnalyzer {
    /**
     * Data model representing a single shopping record
     */
    static class ShoppingRecord{
        private final String invoiceNo;
        private final String customerId;
        private final String gender;
        private final int age;
        private final String category;
        private final int quantity;
        private final double price;
        private final String paymentMethod;
        private final String invoiceDate;
        private final String shoppingMall;

        public ShoppingRecord(String[] data){
            this.invoiceNo = data[0].trim();
            this.customerId = data[1].trim();
            this.gender = data[2].trim();
            this.age = Integer.parseInt(data[3].trim());
            this.category = data[4].trim();
            this.quantity = Integer.parseInt(data[5].trim());
            this.price = Double.parseDouble(data[6].trim());
            this.paymentMethod = data[7].trim();
            this.invoiceDate = data[8].trim();
            this.shoppingMall = data[9].trim();
        }

        // Getters
        public String getGender() {
            return gender;
        }

        public double getPrice() {
            return price;
        }

        public int getQuantity(){
            return quantity;
        }

        public String getPaymentMethod(){
            return paymentMethod;
        }

        public String getInvoiceDate() {
            return invoiceDate;
        }

        // Calculate total sale for this record (price * quantity)
        public double getTotalSale(){
            return price * quantity;
        }
    }

    public static void main(String args[]){

        String csvFile = "Level 2 Coding Challenge Assignment 1/assignment1/customer_shopping_data.csv";

        // Task 1 : Read the data from CSV file
        List<ShoppingRecord> records = readCSVData(csvFile);

        if(records.isEmpty()){
            System.out.println("No data found or error reading file");
            return;
        }

        System.out.println("Successfully loaded " + records.size() + " records\n");
        System.out.println("=" .repeat(50));

        // Task 2 : Count the population grouped by gender
        performTask2(records);

        // Task 3 : Find total sales grouped by gender
        performTask3(records);

        // Task 4 : Find most used payment method
        performTask4(records);

        // Task 5 : Find day with most sales
        performTask5(records);
    }

    /**
     * Task 1 : Read data from CSV file into the collection
     * Uses BufferedReader for effective file reading
     */

    private static List<ShoppingRecord> readCSVData(String fileName){
        List<ShoppingRecord> records = new ArrayList<>();
        int lineNumber = 0;
        int errorCount = 0;

        try(BufferedReader br = new BufferedReader(new FileReader(fileName))){
            String line;
            boolean isHeader = true;

            while((line = br.readLine()) != null){
                lineNumber++;

                // Skip header row
                if(isHeader){
                    isHeader = false;
                    continue;
                }

                String[] values = line.split(",");

                // Validate we have all required fields
                if(values.length < 10) {
                    System.err.println("Skipping incomplete row at line " + lineNumber);
                    errorCount++;
                    continue;
                }

                try{
                    ShoppingRecord record = new ShoppingRecord(values);
                    records.add(record);
                } catch (NumberFormatException e){
                    System.err.println("Error parsing numeric value at line " + lineNumber + ": " + e.getMessage());
                    errorCount++;
                }
            }

            if(errorCount > 0 ){
                System.out.println("Warning: " + errorCount + "rows had errors and were skipped");
            }
        } catch(IOException e){
            System.err.println("Error reading file: " + e.getMessage());
        }
        return records;
    }

    /**
     * Task 2 : Count the population grouped by gender
     * Uses Collectors.groupingBy with Collectors.counting()
     */
    private static void performTask2(List<ShoppingRecord> records){
        System.out.println("\n Task 2: Population count by Gender");
        System.out.println("-" .repeat(35));

        Map<String, Long> genderCount = records.stream()
                .collect(Collectors.groupingBy(
                        ShoppingRecord::getGender,
                        Collectors.counting()
                ));

        // Display results sorted by count (descending)
        genderCount.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .forEach(entry ->
                        System.out.printf("  %-10s: %,d records%n", entry.getKey(), entry.getValue())
                );
    }

    /**
     * Task 3 : Find total sales grouped by gender
     * Uses Collectors.summingDouble to aggregate sales
     */
    private static void performTask3(List<ShoppingRecord> records){
        System.out.println("\nTask 3: Total sales by Gender");
        System.out.println("-" .repeat(35));

        Map<String, Double> salesbyGender = records.stream()
                .collect(Collectors.groupingBy(
                        ShoppingRecord::getGender,
                        Collectors.summingDouble(ShoppingRecord::getTotalSale)
                ));

        // Display results sorted by sales (descending)
        salesbyGender.entrySet().stream()
                .sorted(Map.Entry.<String, Double> comparingByValue().reversed())
                .forEach(entry ->
                        System.out.printf("  %-10s: $%,15.2f%n", entry.getKey(), entry.getValue())
                );

        // Calculate total sales
        double totalSales = salesbyGender.values().stream()
                .mapToDouble(Double::doubleValue)
                .sum();

        System.out.printf("\n  %-10s: $%,15.2f%n", "TOTAL", totalSales);

    }

    /**
     *  Task 4: Find most used payment method
     *  Uses max() with Comparator Map.Entry
     */
    private static void performTask4(List<ShoppingRecord> records){
        System.out.println("\nTask 4: Payment Method Analysis");
        System.out.println("-".repeat(35));

        Map<String, Long> paymentMethodCount = records.stream()
                .collect(Collectors.groupingBy(
                        ShoppingRecord::getPaymentMethod,
                        Collectors.counting()
                ));

        Optional<Map.Entry<String, Long>> mostUsed = paymentMethodCount.entrySet().stream()
                .max(Map.Entry.comparingByValue());

        if(mostUsed.isPresent()) {
            System.out.printf("Most Used Payment Method: %s (%,d transactions)%n",
                    mostUsed.get().getKey(), mostUsed.get().getValue());
        }
    }

    /**
     * Task 5: Find day with the most sales
     * Groups by date and sums total sales
     */
    private static void performTask5(List<ShoppingRecord> records){
        System.out.println("\nTask 5: Day with Most Sales");
        System.out.println("-".repeat(35));

        Map<String, Double> salesByDate = records.stream()
                .collect(Collectors.groupingBy(
                        ShoppingRecord::getInvoiceDate,
                        Collectors.summingDouble(ShoppingRecord::getTotalSale)
                ));

        //Find the day with maximum sales
        Optional<Map.Entry<String, Double>> maxSalesDay = salesByDate.entrySet().stream()
                .max(Map.Entry.comparingByValue());

        if(maxSalesDay.isPresent()) {
            System.out.printf("Day with Highest Sales: %s%n",maxSalesDay.get().getKey());
            System.out.printf("Total Sales: $%,.2f%n", maxSalesDay.get().getValue());

            // Count transactions on that day
            long transactionCount = records.stream()
                    .filter(r -> r.getInvoiceDate().equals(maxSalesDay.get().getKey()))
                    .count();
            System.out.printf("Number of Transactions: %,d%n", transactionCount);
        }
    }
}
