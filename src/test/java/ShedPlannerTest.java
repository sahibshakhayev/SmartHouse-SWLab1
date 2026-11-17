import hu.smarthouse.smarthouse.core.ShedPlanner;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ShedPlannerTest {
    @Test
    void tiersAreShedInOrder() {
        Set<String> shed = new HashSet<>();
        assertEquals(ShedPlanner.TIERS.get(0), ShedPlanner.nextStep(shed::contains, l -> true));

        shed.addAll(ShedPlanner.TIERS.get(0));
        assertEquals(ShedPlanner.TIERS.get(1), ShedPlanner.nextStep(shed::contains, l -> true));

        shed.addAll(ShedPlanner.TIERS.get(1));
        assertEquals(List.of("LIVING_ROOM_LINE"), ShedPlanner.nextStep(shed::contains, l -> true));

        shed.add("LIVING_ROOM_LINE");
        assertEquals(List.of("KITCHEN_LINE"), ShedPlanner.nextStep(shed::contains, l -> true));
    }

    @Test
    void skipsLinesThatAreAlreadyOff() {
        Set<String> shed = new HashSet<>(ShedPlanner.TIERS.get(0));
        shed.addAll(ShedPlanner.TIERS.get(1));
        assertEquals(List.of("BATHROOM_LINE"), ShedPlanner.nextStep(shed::contains, l -> l.equals("BATHROOM_LINE")));
    }
}
