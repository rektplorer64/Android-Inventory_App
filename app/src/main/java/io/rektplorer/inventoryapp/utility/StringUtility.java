package io.rektplorer.inventoryapp.utility;


import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

public class StringUtility{
    /**
     * Converts a long number into one of these manner
     * 1000 to 1k
     * 5821 to 5.8k
     * 10500 to 10k
     * 101800 to 101k
     * 2000000 to 2m
     * 7800000 to 7.8m
     * 92150000 to 92m
     * 123200000 to 123m
     *
     * @param value a long value
     *
     * @return formatted string
     */
    public static String shortenNumber(long value){
        final NavigableMap<Long, String> suffixes = new TreeMap<>();
        suffixes.put(1_000L, "k");
        suffixes.put(1_000_000L, "M");
        suffixes.put(1_000_000_000L, "G");
        suffixes.put(1_000_000_000_000L, "T");
        suffixes.put(1_000_000_000_000_000L, "P");
        suffixes.put(1_000_000_000_000_000_000L, "E");

        //Long.MIN_VALUE == -Long.MIN_VALUE so we need an adjustment here
        if(value == Long.MIN_VALUE){
            return shortenNumber(Long.MIN_VALUE + 1);
        }
        if(value < 0){
            return "-" + shortenNumber(-value);
        }
        if(value < 1000){
            return Long.toString(value); //deal with easy case
        }

        Map.Entry<Long, String> e = suffixes.floorEntry(value);
        Long divideBy = e.getKey();
        String suffix = e.getValue();

        long truncated = value / (divideBy / 10); //the number part of the output times 10
        boolean hasDecimal = truncated < 100 && (truncated / 10d) != (truncated / 10);
        return hasDecimal ? (truncated / 10d) + suffix : (truncated / 10) + suffix;
    }
}
