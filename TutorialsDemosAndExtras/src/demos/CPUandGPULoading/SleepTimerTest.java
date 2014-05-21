package demos.CPUandGPULoading;

import java.text.DecimalFormat;

/**
 * Display various timing performance measurements
 */
public class SleepTimerTest {

    public static DecimalFormat df;

    public static void main(String[] args) {
        df = new DecimalFormat("#.00");

        // Run a total of 4 timer accuracy tests (for different batch sizes)
        timerAccuracyTest(500);
        timerAccuracyTest(1000);
        timerAccuracyTest(1500);
        timerAccuracyTest(2000);

        // Run a number of different sleep tests. Note the tests are
        // repeated as a means of demonstrating the variability between
        // different calls to sleep for the same amount of time.
        for (int idx = 0; idx < 5; idx++)
            sleepTest(50);
        
        for (int idx = 0; idx < 5; idx++)
            sleepTest(10);

        for (int idx = 0; idx < 5; idx++)
            sleepTest(5);

        for (int idx = 0; idx < 5; idx++)
            sleepTest(1);
    }

    private static void timerAccuracyTest(int batchSize) {
        // Record the time now using the 'new' and 'old' means
        long timeStartOld = System.currentTimeMillis();
        long timeStartNew = System.nanoTime();

        // Do a pointless exercise to waste some time
        int counter = 0;
        for (int idx = 1; idx < batchSize; idx++) {
            for (int idx2 = 1; idx2 < batchSize; idx2++) {
                if (idx / idx2 == 2) {
                    counter++;
                }
            }
        }
        // Record the time now using the 'new' and 'old' means
        long timeEndOld = System.currentTimeMillis();
        long timeEndNew = System.nanoTime();

        System.out.println("Batch size = " + batchSize + "\t currentTimeMillis() = " 
                + (timeEndOld - timeStartOld) + "ms \t nanoTime() = " 
                + ((timeEndNew - timeStartNew) / 1000000L));
    }

    private static void sleepTest(int delay) {
        // Record tHe start time
        long timeStart = System.nanoTime();

        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
        }

        // Measure how long we were asleep and work out the % error
        double timeDiff = ((double) (System.nanoTime() - timeStart)) / (1000000L);
        double err = ((delay - timeDiff) / timeDiff) * 100;

        System.out.println("Attempt sleep = " + delay + "ms. \tActual sleep = " 
                + df.format(timeDiff) + "ms. \tError = " + df.format(err) + " %");
    }
}