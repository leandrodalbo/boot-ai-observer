package io.boot.ai.observer.collector.error;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

public abstract class AIObserverErrorCollector
{

    private static final int MAX_ERRORS         = 20;
    private static final int MAX_MESSAGE_LENGTH = 120;

    protected final ConcurrentLinkedDeque<String> errors = new ConcurrentLinkedDeque<>();

    public void track(Throwable ex) {
        if (errors.size() >= MAX_ERRORS) {
            errors.pollFirst();
        }
        errors.addLast(ex.getClass().getSimpleName() + ": " + truncate(ex.getMessage()));
    }

    public List<String> getAndReset() {
        List<String> snapshot = new ArrayList<>();
        String entry;
        while ((entry = errors.pollFirst()) != null) {
            snapshot.add(entry);
        }
        return Collections.unmodifiableList(snapshot);
    }

    private static String truncate(String exMsg) {
        if (exMsg == null) return "";
        return exMsg.length() <= MAX_MESSAGE_LENGTH ? exMsg : exMsg.substring(0, MAX_MESSAGE_LENGTH) + "…";
    }
}
