package com.dw.backend.doablewellbeingbackend.business;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class MockitoInlineSmokeTest {

    static final class FinalThing {
        String hi() { return "real"; }
    }

    @Test
    void canMockFinalClassWithInline() {
        FinalThing t = mock(FinalThing.class);
        when(t.hi()).thenReturn("mocked");
        assertEquals("mocked", t.hi());
    }
}
