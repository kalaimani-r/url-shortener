package com.example.urlshortener.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class Base62Test {

    @Test
    void roundTrip() {
        for (long id : new long[] {0, 1, 61, 62, 3844, 9_999_999}) {
            String encoded = Base62.encode(id);
            assertEquals(id, Base62.decode(encoded));
        }
    }

    @Test
    void knownValues() {
        assertEquals("0", Base62.encode(0));
        assertEquals("1", Base62.encode(1));
        assertEquals("10", Base62.encode(62));
    }
}
