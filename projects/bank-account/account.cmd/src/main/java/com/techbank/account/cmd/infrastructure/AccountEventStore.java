package com.techbank.account.cmd.infrastructure;

import com.techbank.account.cmd.domain.AccountAggregate;
import com.techbank.account.cmd.domain.EventStoreRepository;
import com.techbank.cqrs.core.events.BaseEvent;
import com.techbank.cqrs.core.events.EventModel;
import com.techbank.cqrs.core.exceptions.AggregateNotFoundException;
import com.techbank.cqrs.core.exceptions.ConcurrencyException;
import com.techbank.cqrs.core.infrastructure.EventStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AccountEventStore implements EventStore {
    @Autowired
    private EventStoreRepository eventStoreRepository;

    @Override
    public void saveEvent(String aggregateId, Iterable<BaseEvent> events, int expectedVersion) {
      final List<EventModel> eventStream = eventStoreRepository.findByAggregateIdentifier(aggregateId);
      if (expectedVersion != -1 && eventStream.get(eventStream.size() - 1).getVersion() != expectedVersion) {
          throw new ConcurrencyException();
      }

      int version = expectedVersion;
      for (BaseEvent baseEvent : events) {
          version++;
          baseEvent.setVersion(version);
          final EventModel eventModel = EventModel.builder()
                  .timeStamp(new Date())
                  .aggregateIdentifier(aggregateId)
                  .aggregateType(AccountAggregate.class.getTypeName())
                  .version(version)
                  .eventType(baseEvent.getClass().getTypeName())
                  .eventData(baseEvent)
                  .build();

          final EventModel savedEvent = eventStoreRepository.save(eventModel);
          if (savedEvent != null) {
              // @todo: produce event to Kafka
          }
      }
    }

    @Override
    public List<BaseEvent> getEvents(String aggregateId) {
        final List<EventModel> eventStream = eventStoreRepository.findByAggregateIdentifier(aggregateId);
        if (eventStream == null || eventStream.isEmpty()) {
            throw new AggregateNotFoundException("Incorrect account ID provided!");
        }

        return eventStream.stream().map(EventModel::getEventData).collect(Collectors.toList());
    }
}
