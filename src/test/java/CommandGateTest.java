import hu.smarthouse.smarthouse.core.CommandGate;
import hu.smarthouse.smarthouse.core.Priority;
import hu.smarthouse.smarthouse.model.Event;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CommandGateTest {
    @Test
    void shedLockBlocksAutomationButNotUser() {
        CommandGate gate = new CommandGate();
        long now = 1_000;
        gate.shedLock("KITCHEN_PLUG", now + 60_000);

        Event automation = new Event("TURN_ON", "x", "KITCHEN_PLUG", Priority.AUTOMATION);
        Event user = new Event("TURN_ON", "x", "KITCHEN_PLUG", Priority.USER_OVERRIDE);

        assertFalse(gate.allow(automation, now));
        assertTrue(gate.allow(user, now));
    }
}
