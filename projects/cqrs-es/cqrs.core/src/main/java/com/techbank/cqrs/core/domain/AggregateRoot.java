package com.techbank.cqrs.core.domain;

import com.techbank.cqrs.core.events.BaseEvent;
import org.springframework.boot.logging.LogLevel;

import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class AggregateRoot {
    protected String id;
    private int version = -1;

    private final List<BaseEvent> changes = new ArrayList<>();
    private final Logger logger = Logger.getLogger(AggregateRoot.class.getName());

    public String getId() {
        return this.id;
    }

    public void setVersion(final int version) {
        this.version = version;
    }

    public int getVersion() {
        return this.version;
    }

    public List<BaseEvent> getUncommittedChanges() {
        return this.changes;
    }

    public void markChangesAsCommitted() {
        this.changes.clear();
    }

    protected void applyChange(final BaseEvent event, Boolean isNewEvent) {
        try {
            final Method method = getClass().getDeclaredMethod("apply", event.getClass());
            method.setAccessible(true);
            method.invoke(this, event);
        } catch (final NoSuchMethodException e) {
            logger.log(Level.WARNING, MessageFormat.format("The apply method was not found in the aggregate for {0}", event.getClass().getName()));
        } catch (final Exception e) {
            logger.log(Level.SEVERE, "Error applying event to aggregate.");
        } finally {
            if (isNewEvent) {
                changes.add(event);
            }
        }
    }

    public void raiseEvent(final BaseEvent event) {
        applyChange(event, Boolean.TRUE);
    }

    public void replayEvents(final Iterable<BaseEvent> events) {
        events.forEach(event -> applyChange(event, Boolean.FALSE));
    }

}
