package com.ebp.openQuarterMaster.baseStation.data.pojos.history;

import com.ebp.openQuarterMaster.baseStation.data.pojos.PojoTest;
import com.ebp.openQuarterMaster.baseStation.data.pojos.history.Historied;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
public class HistoriedTest extends PojoTest<Historied> {
    @Override
    public Historied getBasicTestItem() {
        return new Historied() {};
    }

    @Test
    public void testUpdated() {
        Historied o = this.getBasicTestItem();

        assertTrue(o.getHistory().isEmpty());
        assertEquals(0, o.getHistory().size());

        Historied o2 = o.updated(
                HistoryEvent.builder()
                        .type(EventType.CREATE)
                        .userId(UUID.randomUUID())
                .build()
        );
        assertSame(o, o2);
        assertEquals(1, o.getHistory().size());

        o2 = o.updated(
                HistoryEvent.builder()
                        .type(EventType.UPDATE)
                        .userId(UUID.randomUUID())
                        .build()
        );

        assertTrue(o.getHistory().get(0).getTimestamp().isAfter(o.getHistory().get(1).getTimestamp()));
    }

    @Test
    public void testUpdatedCreate() {
        Historied o = this.getBasicTestItem();

        o.updated(HistoryEvent.builder()
                .type(EventType.CREATE)
                .userId(UUID.randomUUID())
                .build()
        );

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            o.updated(HistoryEvent.builder()
                    .type(EventType.CREATE)
                    .userId(UUID.randomUUID())
                    .build());
        });
    }

    @Test
    public void testUpdatedUpdateFirst() {
        Historied o = this.getBasicTestItem();

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            o.updated(HistoryEvent.builder()
                    .type(EventType.UPDATE)
                    .userId(UUID.randomUUID())
                    .build());
        });
    }

    @Test
    public void testGetLastUpdate() {
        Historied o = this.getBasicTestItem();

        o.updated(
                HistoryEvent.builder()
                        .type(EventType.CREATE)
                        .userId(UUID.randomUUID())
                        .build()
        );
        o.updated(
                HistoryEvent.builder()
                        .type(EventType.UPDATE)
                        .userId(UUID.randomUUID())
                        .build()
        );

        HistoryEvent event = o.getLastHistoryEvent();

        assertEquals(o.getHistory().get(0), event);
    }

    @Test
    public void testGetLastUpdateTime() {
        Historied o = this.getBasicTestItem();

        o.updated(
                HistoryEvent.builder()
                        .type(EventType.CREATE)
                        .userId(UUID.randomUUID())
                        .build()
        );
        o.updated(
                HistoryEvent.builder()
                        .type(EventType.UPDATE)
                        .userId(UUID.randomUUID())
                        .build()
        );

        ZonedDateTime eventTime = o.getLastHistoryEventTime();

        assertEquals(o.getHistory().get(0).getTimestamp(), eventTime);
    }


}
