package hu.smarthouse.smarthouse.model;

import hu.smarthouse.smarthouse.core.Priority;
import lombok.*;
import lombok.experimental.Accessors;

import java.sql.Timestamp;

@Getter
@RequiredArgsConstructor
@Accessors(chain = true)
@ToString
public final class Event {

    @NonNull
    private final String type;
    @NonNull
    private final String message;
    @NonNull
    private final String compId;
    private final Priority priority;
    private final Timestamp timestamp = new Timestamp(System.currentTimeMillis());

    public Event(String type, String message, String compId) {
        this(type, message, compId, null);
    }

    public Event withPriority(Priority p) {
        return new Event(type, message, compId, p);
    }
}