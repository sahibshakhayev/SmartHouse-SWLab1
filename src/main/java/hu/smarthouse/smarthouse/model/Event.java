package hu.smarthouse.smarthouse.model;

import lombok.*;
import lombok.experimental.Accessors;

@Getter
@AllArgsConstructor
@Accessors(chain = true)
@ToString
public final class Event {

    @NonNull
    private final String type;
    @NonNull
    private final String message;
    @NonNull
    private final String compID;

}