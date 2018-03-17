package com.codingchili.realm.instance.context;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author Robin Duda
 * <p>
 * a ring-based queue here where all tickers in a segment
 * gets executed and rescheduled by head+tick. Avoids having to loop over
 * all tickers on the context. The segment count must be less than the maximum
 * tick delay. Each tick is only scheduled once at most. Tickers must be removed
 * from head after tickers have been processed.
 */
public class RingBufferScheduler<E extends Supplier<Integer>> {
    private List<Queue<E>> segments = new ArrayList<>();
    private AtomicInteger head = new AtomicInteger(0);

    /**
     * @param segmentCount the number of segments that may contain items.
     */
    public RingBufferScheduler(int segmentCount) {
        for (int i = 0; i < segmentCount; i++) {
            segments.add(new LinkedBlockingQueue<>());
        }
    }

    /**
     * Process the items in the next segment and move the head forward by one.
     *
     * @param consumer to consume items in the next segment.
     */
    public void forNext(Consumer<E> consumer) {
        forNext(consumer, 1);
    }

    /**
     * Process the items in the next segments and move the head forward.
     *
     * @param consumer to consume items in the next segments.
     * @param count    the number of segments to process.
     */
    public void forNext(Consumer<E> consumer, int count) {
        final int step = count;
        int current = head.getAndUpdate(ptr -> (ptr + step) % segments.size());

        while (count > 0) {
            Queue<E> segment = segments.get(current);
            segment.forEach(item -> {
                schedule(item, item.get());
                consumer.accept(item);
            });
            segments.get(current).clear();

            if ((++current) == segments.size()) {
                current = 0;
            }
            count--;
        }
    }

    /**
     * Adds an item to a segment with the offset received from the item as a Supplier.
     *
     * @param item the item to schedule.
     */
    public void schedule(E item) {
        schedule(item, item.get());
    }

    /**
     * Adds an item to a segment with the offset specified. When the item is rescheduled
     * the value of the Supplier will be used.
     *
     * @param item  the item to schedule.
     * @param delay the delay as specified in number of segments as an offset to the current head.
     */
    public void schedule(E item, int delay) {
        if (delay >= segments.size()) {
            throw new IllegalArgumentException("Delay cannot be larger than segment count.");
        }
        if (delay > 0) {
            int segment = (head.get() + delay - 1) % segments.size();
            segments.get(segment).add(item);
        } else {
            throw new IllegalArgumentException("Delay must be greater than 0.");
        }
    }

    public static void main(String[] args) {
        AtomicInteger times = new AtomicInteger(0);
        RingBufferScheduler<Supplier<Integer>> scheduler = new RingBufferScheduler<>(600);

        for (int i = 601; i < 10000; i++) {
            final int index = ((i + 1) % 599) + 1;

            scheduler.schedule(() -> {
                times.incrementAndGet();
                return index;
            });
        }

        System.out.println("=== start measure ===");
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            scheduler.forNext((x) -> {
                //System.out.println("x = " + x + " t = " + times.get());
            });
        }
        System.out.println("called: " + times.get());
        System.out.println("time: " + (System.currentTimeMillis() - start) + " ms.");
        System.out.println("=== end measure ===");
    }
}
