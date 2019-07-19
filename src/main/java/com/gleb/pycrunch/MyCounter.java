package com.gleb.pycrunch;

public class MyCounter {
    private int Count = 0;
    private static int CounterOfSingletons = 0;
    // Sets the maximum allowed number of opened projects.
    public final int MaxCount = 3;

    public MyCounter() {
        MyCounter.CounterOfSingletons++;

    }

    public int IncreaseCounter() {
        Count++;
        return (Count > MaxCount) ? -1 : Count;
    }

    public int DecreaseCounter() {
        Count--;
        return (Count > MaxCount) ? -1 : Count;
    }

}