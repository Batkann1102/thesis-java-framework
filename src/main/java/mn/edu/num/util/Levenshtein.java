package mn.edu.num.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * "Did-you-mean" зорилгод зориулсан Levenshtein distance тооцоолох туслах класс.
 * <p>
 * Bean олдоогүй үед хамгийн ойролцоо нэрийг санал болгож DX-ийг сайжруулна.
 * Жишээ: хэрэглэгч "userServce" гэж бичсэн боловч бодит нэр "userService" бол
 * "userService" гэсэн санал гарна.
 */
public final class Levenshtein {

    private Levenshtein() {
    }

    public static int distance(String a, String b) {
        if (a == null || b == null) {
            throw new IllegalArgumentException("Levenshtein input null байж болохгүй");
        }
        int n = a.length();
        int m = b.length();
        if (n == 0) return m;
        if (m == 0) return n;

        int[] previous = new int[m + 1];
        int[] current = new int[m + 1];
        for (int j = 0; j <= m; j++) previous[j] = j;

        for (int i = 1; i <= n; i++) {
            current[0] = i;
            char ca = a.charAt(i - 1);
            for (int j = 1; j <= m; j++) {
                int cost = (ca == b.charAt(j - 1)) ? 0 : 1;
                current[j] = Math.min(Math.min(
                                current[j - 1] + 1,
                                previous[j] + 1),
                        previous[j - 1] + cost);
            }
            int[] tmp = previous;
            previous = current;
            current = tmp;
        }
        return previous[m];
    }

    /**
     * Шалгаж буй нэрэнд хамгийн ойр кандидатуудыг олно.
     *
     * @param target     хэрэглэгчийн оруулсан нэр
     * @param candidates бүртгэлтэй боломжит нэрс
     * @param maxDistance distance-ийн дээд хязгаар (-аас дээш ялгаатайг тоохгүй)
     * @return санал болгож болох нэрс. Хамгийн ойр дээр нь эхэлж байрласан.
     */
    public static List<String> suggest(String target, Collection<String> candidates, int maxDistance) {
        if (target == null || candidates == null || candidates.isEmpty()) {
            return List.of();
        }
        List<String[]> scored = new ArrayList<>();
        for (String candidate : candidates) {
            int d = distance(target.toLowerCase(), candidate.toLowerCase());
            if (d <= maxDistance) {
                scored.add(new String[]{candidate, String.valueOf(d)});
            }
        }
        scored.sort(Comparator.comparingInt(arr -> Integer.parseInt(arr[1])));
        List<String> result = new ArrayList<>();
        for (String[] entry : scored) {
            result.add(entry[0]);
        }
        return Collections.unmodifiableList(result);
    }

    public static Optional<String> closest(String target, Collection<String> candidates, int maxDistance) {
        List<String> suggestions = suggest(target, candidates, maxDistance);
        return suggestions.isEmpty() ? Optional.empty() : Optional.of(suggestions.get(0));
    }
}
