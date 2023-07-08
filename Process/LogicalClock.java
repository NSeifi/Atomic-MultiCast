package Process;

import java.util.Random;
/**
 * @Author: Nasrin Seifi
 * Purpose: The class aims to provide a random logical clock for each Process.
 * Also, handle the clock incrementation and providing clock value
 */
public class LogicalClock {
    int logicalClockValue;

    public LogicalClock() {
        Random r = new Random();
        logicalClockValue = r.nextInt(10);
    }

    /**
     * The method increments the clock
     * Usage: After an event (receiving or sending) the method is called to increment the value
     */
    public synchronized void incrementValue() {
        logicalClockValue++;
    }

    /**
     * The method increments the clock
     * Usage: Set a process clock to maximum (based on its and other processes clock) to deliver message
     * the method is called to increment the value
     * @param valRecvd
     */
    public synchronized void incrementValue(int valRecvd) {
        if (valRecvd > logicalClockValue)
            logicalClockValue = valRecvd + 1;
        else
            logicalClockValue++;
    }

    /**
     * The method aims for accessing the current logical value
     * @return the logical clock value
     */
    public synchronized int getValue() {
        return logicalClockValue;
    }
}
