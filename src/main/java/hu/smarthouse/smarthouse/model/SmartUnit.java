package hu.smarthouse.smarthouse.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@AllArgsConstructor
@Accessors(chain = true)
@ToString
public abstract class SmartUnit {

    @NonNull
    private final String compID;

}