
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Producer-Consumer Problem Implementation
 */
public class ProducerConsumerSolution {
    /**
     * Custom bounded queue with synchronization
     */
    static class BoundedQueue<T>{
        private final Queue<T> queue;
        private final int capacity;

        public BoundedQueue(int capacity){
            this.capacity = capacity;
            this.queue = new LinkedList<>();
        }

        /**
         * Add item to queue (used by Producer)
         * Waits if queue is full
         */
        public synchronized void enqueue(T item) throws InterruptedException {
            while(queue.size() == capacity){
                System.out.println("Queue is FULL. Producer waiting...");
                wait();
            }

            queue.offer(item);
            System.out.println("Produced: " + item + " | Queue size: " + queue.size());

            // Notify consumer that data is available
            notifyAll();
        }

        /**
         * Remove item from queue (used by consumer)
         * Waits if queue is empty
         */
        public synchronized T dequeue() throws InterruptedException{
            while(queue.isEmpty()){
                System.out.println("Queue is EMPTY. Consumer waiting...");
                wait();
            }

            T item = queue.poll();
            System.out.println("Consumed: " + item + " | Queue size: " + queue.size());

            notifyAll();
            return item;
        }

        public synchronized boolean isEmpty() {
            return queue.isEmpty();
        }

        public synchronized int size() {
            return queue.size();
        }
    }

    /**
     * Producer thread - reads from source container and puts into queue
     */
    static class Producer implements Runnable {
        private final List<Number> sourceContainer;
        private final BoundedQueue<Number> queue;
        private final AtomicInteger producedCount;

        public Producer(List<Number> sourceContainer, BoundedQueue<Number> queue, AtomicInteger producedCount){
            this.sourceContainer = sourceContainer;
            this.queue = queue;
            this.producedCount = producedCount;
        }

        @Override
        public void run(){
            try{
                for(Number num : sourceContainer){
                    queue.enqueue(num);
                    producedCount.incrementAndGet();

                    Thread.sleep(10);
                }
                System.out.println("\n>>> Producer finished! Produced " + producedCount.get() + " items <<<\n");

            } catch (InterruptedException e){
                Thread.currentThread().interrupt();
                System.err.println("Producer interrupted: " + e.getMessage());
            }
        }
    }

    /**
     *  Consumer thread - reads from queue and puts into destination container
     */

    static class Consumer implements Runnable {
        private final List<Number> destinationContainer;
        private final BoundedQueue<Number> queue;
        private final int expectedItems;
        private final AtomicInteger consumedCount;

        public Consumer(List<Number> destinationContainer, BoundedQueue<Number> queue, int expectedItems, AtomicInteger consumedCount){
            this.destinationContainer = destinationContainer;
            this.queue = queue;
            this.expectedItems = expectedItems;
            this.consumedCount = consumedCount;
        }

        @Override
        public void run(){
            try{
                while(consumedCount.get() < expectedItems) {
                    Number item = queue.dequeue();
                    destinationContainer.add(item);
                    consumedCount.incrementAndGet();

                    Thread.sleep(15);
                }

                System.out.println("\n>>> Consumer finished! Consumed " + consumedCount.get() + " items <<<\n");
            }catch (InterruptedException e){
                Thread.currentThread().interrupt();
                System.err.println("Consumer interrupted: " + e.getMessage());
            }
        }
    }

    /**
     * Main method - Sets up and runs the producer-consumer scenario
     */

    public static void main(String args[]){
        System.out.println("=== Producer-Consumer Problem Implementation ===\n");

        // Task 1 : Create source container with integers and doubles
        List<Number> sourceContainer = new ArrayList<>();

        // Populate with mix of integers and doubles
        for(int i = 1; i <= 10; i++){
            sourceContainer.add(i);
            sourceContainer.add(i * 1.5);
        }

        System.out.println("Source container created with " + sourceContainer.size() + " items");
        System.out.println("Source: " + sourceContainer + "\n");

        // Task 2: Create destination container with same capacity
        List<Number> destinationContainer = Collections.synchronizedList(new ArrayList<>());

        // Task 3: Create queue with half capacity of source container
        int queueCapacity = sourceContainer.size() / 2;
        BoundedQueue<Number> queue = new BoundedQueue<>(queueCapacity);
        System.out.println("Queue created with capacity: " + queueCapacity + "\n");

        // Shared counters for tracking progress
        AtomicInteger producedCount = new AtomicInteger(0);
        AtomicInteger consumedCount = new AtomicInteger(0);

        // Task 4 & 5: Create Producer and Consumer threads
        Thread producerThread = new Thread(
                new Producer(sourceContainer, queue, producedCount),
                "Producer-Thread"
        );

        Thread consumerThread = new Thread(
                new Consumer(destinationContainer, queue, sourceContainer.size(), consumedCount),
                "Consumer-Thread"
        );

        // Start both threads
        System.out.println("Starting Producer and Consumer threads...\n");
        System.out.println("-".repeat(50));

        producerThread.start();
        consumerThread.start();

        try{
            //Wait for both threads to complete
            producerThread.join();
            consumerThread.join();


            System.out.println("-".repeat(50));

            // Task 6: Test to confirm successful copy
            runVerificationTest(sourceContainer, destinationContainer);
        } catch (InterruptedException e){
            System.err.println("Main thread interrupted: " + e.getMessage());
        }
    }

    /**
     * Task 6: Verification test to confirm successful copy
     */

    private static void runVerificationTest(List<Number> source, List<Number> destination){
        System.out.println("=== VERIFICATION TEST ===\n");

        boolean testPassed = true;

        // Test 1: Check sizes
        System.out.println("Test 1 - Size Check");
        System.out.println(" Source size: " + source.size());
        System.out.println(" Destination size: " + destination.size());

        if(source.size() != destination.size()){
            System.out.println(" FAILED: Sizes don't match!");
            testPassed = false;
        } else {
            System.out.println(" PASSED: Sizes match!");
        }

        // Test 2: Check all elements are present(order might differ due to threading)
        System.out.println("\n Test 2 - Content Check:");

        //Sort both lists for comparison
        List<Number> sortedSource = new ArrayList<>(source);
        List<Number> sortedDestination = new ArrayList<>(destination);

        sortedSource.sort((a,b) -> Double.compare(a.doubleValue(), b.doubleValue()));
        sortedDestination.sort((a,b) -> Double.compare(a.doubleValue(), b.doubleValue()));

        boolean contentMatches = true;
        for(int i = 0; i < sortedSource.size(); i++){
            if(!sortedSource.get(i).equals(sortedDestination.get(i))) {
                contentMatches = false;
                System.out.println(" Mismatch at index " + i + ": " +
                        sortedSource.get(i) + " != " + sortedDestination.get(i));
            }
        }

        if(contentMatches) {
            System.out.println(" PASSED: All elements successfully copied!");
        } else {
            System.out.println(" FAILED: Elements don't match!");
            testPassed = false;
        }

        // Test 3: Type preservation check
        System.out.println("\nTest 3 - Type Preservation Check:");
        int sourceIntegers = 0, sourceDoubles = 0;
        int destIntegers = 0, destDoubles = 0;

        for(Number n : source){
            if(n instanceof Integer) sourceIntegers++;
            else if(n instanceof Double) sourceDoubles++;
        }

        for(Number n : destination){
            if(n instanceof Integer) destIntegers++;
            else if(n instanceof Double) destDoubles++;
        }

        System.out.println(" Source: " + sourceIntegers + " Integers, " + sourceDoubles + " Doubles");
        System.out.println(" Destination: " + destIntegers + " Integers, " + destDoubles + " Doubles");

        if(sourceIntegers == destIntegers && sourceDoubles == destDoubles){
            System.out.println(" PASSED: Types preserved correctly!");
        } else{
            System.out.println(" FAILED: Type counts don't match!");
            testPassed = false;
        }

        // Final result
        System.out.println("\n" + "=".repeat(30));
        if(testPassed){
            System.out.println(" ALL TESTS PASSED! ");
            System.out.println("Producer-Consumer implementation successful!");
        } else{
            System.out.println(" SOME TESTS FAILED");
        }
        System.out.println("=".repeat(30));

        // Display final containers
        System.out.println("\nFinal Containers:");
        System.out.println("Source        " + source);
        System.out.println("Destination : " + destination);
    }
}
