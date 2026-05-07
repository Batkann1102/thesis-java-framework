package mn.edu.num.unit;

import mn.edu.num.util.Levenshtein;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LevenshteinTest {

    @Test
    void distanceShouldBeZeroForEqualStrings() {
        assertEquals(0, Levenshtein.distance("abc", "abc"));
    }

    @Test
    void distanceShouldHandleInsertion() {
        assertEquals(1, Levenshtein.distance("abc", "abcd"));
    }

    @Test
    void distanceShouldHandleSubstitution() {
        assertEquals(1, Levenshtein.distance("abc", "abd"));
    }

    @Test
    void suggestShouldReturnClosest() {
        List<String> candidates = List.of("userService", "orderService", "emailService");
        List<String> suggestions = Levenshtein.suggest("userServic", candidates, 3);
        assertTrue(suggestions.contains("userService"));
    }

    @Test
    void suggestShouldReturnEmptyWhenAllTooDistant() {
        List<String> candidates = List.of("alpha", "beta");
        List<String> suggestions = Levenshtein.suggest("xyz", candidates, 1);
        assertTrue(suggestions.isEmpty());
    }
}
