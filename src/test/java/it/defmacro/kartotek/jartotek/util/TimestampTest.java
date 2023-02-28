package it.defmacro.kartotek.jartotek.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.Month;

import static org.junit.jupiter.api.Assertions.*;

class TimestampTest {

    @Test
    void deserialize() {
        String ts = "20221023115004";
        LocalDateTime res = Timestamp.deserialize(ts);
        assertEquals(res.getYear(), 2022);
        assertEquals(res.getMonthValue(), 10);
        assertEquals(res.getDayOfMonth(), 23);
        assertEquals(res.getHour(), 11);
        assertEquals(res.getMinute(), 50);
        assertEquals(res.getSecond(), 4);
    }

    @Test
    void serialize() {
        LocalDateTime dt = LocalDateTime.of(
                2022, Month.OCTOBER,
                23, 11, 50, 4
        );
        String res = Timestamp.serialize(dt);
        assertEquals(res, "20221023115004");
    }
}