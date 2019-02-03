package org.firstinspires.ftc.teamcode.scheduler;

import java.util.Iterator;
import java.util.LinkedHashMap;

public class RollingStatistics
{
    public final long timeInterval_ms;

    // Stores data collected, indexed by currentTimeMillis
    LinkedHashMap<Long, Double> data = new LinkedHashMap<Long, Double>()
    {
        @Override
        protected boolean removeEldestEntry(Entry<Long, Double> eldest)
        {
            long age_ms = System.currentTimeMillis() - eldest.getKey();
            return age_ms > timeInterval_ms;
        }

        ;
    };

    public RollingStatistics(long timeInterval_ms)
    {
        this.timeInterval_ms = timeInterval_ms;
    }


    public double getAverage()
    {
        double measurementTotal = 0;
        long count=0;
        Iterator<Long> loopIntervalHistory_iterator = data.keySet().iterator();
        while (loopIntervalHistory_iterator.hasNext())
        {
            Long time_ms = loopIntervalHistory_iterator.next();
            double measurement = data.get(time_ms);

            long age_ms  = System.currentTimeMillis() - time_ms;
            if (age_ms <= timeInterval_ms)
            {
                count++;
                measurementTotal += measurement;
            }
            else
                // The loopDurationHistory_ns is somewhat self-cleaning (one added, one removed)
                // but this cleans it more if old entries accumulate
                loopIntervalHistory_iterator.remove();
        }
        if (count == 0)
            return 0;

        double averageMeasurement = measurementTotal / count;

        return  averageMeasurement;
    }


    public void put(double dataItem)
    {
        data.put(System.currentTimeMillis(), dataItem);
    }
}

