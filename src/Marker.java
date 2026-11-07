import java.util.*;

/**
 * Missions 3 and 4. Find a word in a long text.
 */
public class Marker {

    /**
     * Mission 3a. The prefix function of p. pi[i] = the length of the longest proper prefix of p[0..i] that is
     * also a suffix of p[0..i] ("proper" means shorter than p[0..i] itself).
     * Example: p = "ACGAC" gives [0, 0, 0, 1, 2]. The last number is the "Border length" line.
     */
    public static int[] prefixFunction(String p) {
        int m = p.length();
        int[] pi = new int[m];   // pi[0] = 0 всегда: у строки из одной буквы нет собственной границы
        int k = 0;               // длина текущей границы

        for (int i = 1; i < m; i++) {
            // пока следующая буква не подходит, откатываемся к более короткой границе
            while (k > 0 && p.charAt(i) != p.charAt(k)) {
                k = pi[k - 1];
            }
            // если подошла, граница стала на 1 длиннее
            if (p.charAt(i) == p.charAt(k)) {
                k++;
            }
            pi[i] = k;
        }
        return pi;
    }

    /**
     * Mission 3b. KMP. All start positions (counting from 0, in increasing order) where p occurs in text.
     * Overlapping occurrences count: "ACG" occurs in "ACGACG" at 0 and 3, and "AA" in "AAA" at 0 and 1.
     * Use the prefix function: the text pointer never goes back. The number of positions is "Marker hits",
     * the first one is "First hit" (-1 when there is none).
     */
    public static int[] findAll(String text, String p) {
        int n = text.length();
        int m = p.length();
        if (m == 0 || m > n) return new int[0];

        int[] pi = prefixFunction(p);
        List<Integer> found = new ArrayList<>();
        int k = 0;               // сколько букв маркера сейчас совпало

        for (int i = 0; i < n; i++) {            // i только растёт: назад не идём
            while (k > 0 && text.charAt(i) != p.charAt(k)) {
                k = pi[k - 1];                    // откатываем k, а не i
            }
            if (text.charAt(i) == p.charAt(k)) {
                k++;
            }
            if (k == m) {                         // полное совпадение
                found.add(i - m + 1);             // позиция начала вхождения
                k = pi[m - 1];                    // не 0: следующее вхождение может перекрываться
            }
        }

        int[] result = new int[found.size()];
        for (int i = 0; i < result.length; i++) result[i] = found.get(i);
        return result;
    }

    /**
     * Mission 4. Rabin-Karp. The number of positions i in a such that the word of k letters starting at i
     * (a[i..i+k-1]) also occurs somewhere in b. Count positions, not different words. This is the "Shared words" line.
     * The idea: give every window of k letters a number (a rolling hash, updated in O(1) when the window moves by one),
     * look the number up among the windows of b, and when it is found COMPARE THE LETTERS, because two different words
     * can have the same hash.
     * Example: a = "ACGTAC", b = "TTACGT", k = 4: the words of a are ACGT, CGTA, GTAC; only ACGT is in b, so the answer is 1.
     */
    public static int sharedWords(String a, String b, int k) {
        if (k <= 0 || a.length() < k || b.length() < k) return 0;

        long[] hashA = rollingHashes(a, k);   // hashA[i] = хеш окна A[i..i+k-1]
        long[] hashB = rollingHashes(b, k);   // hashB[j] = хеш окна B[j..j+k-1]

        // хеш -> позиции окон B с таким хешем
        Map<Long, List<Integer>> windowsOfB = new HashMap<>();
        for (int j = 0; j < hashB.length; j++) {
            windowsOfB.computeIfAbsent(hashB[j], x -> new ArrayList<>()).add(j);
        }

        int count = 0;
        for (int i = 0; i < hashA.length; i++) {
            List<Integer> candidates = windowsOfB.get(hashA[i]);
            if (candidates == null) continue;               // такого хеша нет в B: слова точно нет

            for (int j : candidates) {
                if (sameLetters(a, i, b, j, k)) {           // хеши равны: проверяем сами буквы
                    count++;                                // позиция i считается один раз
                    break;
                }
            }
        }
        return count;
    }

    private static final long BASE = 257;
    private static final long MOD = 1_000_000_007L;

    private static int code(char c) {
        return switch (c) {
            case 'A' -> 1;
            case 'C' -> 2;
            case 'G' -> 3;
            case 'T' -> 4;
            default -> throw new IllegalArgumentException("Unexpected letter: " + c);
        };
    }

    /** Хеши всех окон длины k строки s: первый считаем напрямую, остальные сдвигом за O(1). */
    private static long[] rollingHashes(String s, int k) {
        int count = s.length() - k + 1;
        long[] hashes = new long[count];

        long pow = 1;                                       // BASE^(k-1) mod MOD
        for (int t = 1; t < k; t++) pow = pow * BASE % MOD;

        long h = 0;
        for (int t = 0; t < k; t++) {                       // хеш первого окна
            h = (h * BASE + code(s.charAt(t))) % MOD;
        }
        hashes[0] = h;

        for (int i = 1; i < count; i++) {
            // 1) убрать старую первую букву (+ MOD, чтобы число не стало отрицательным)
            h = (h - code(s.charAt(i - 1)) * pow % MOD + MOD) % MOD;
            // 2) сдвинуть и добавить новую последнюю букву
            h = (h * BASE + code(s.charAt(i + k - 1))) % MOD;
            hashes[i] = h;
        }
        return hashes;
    }

    /** Сравнивает k букв A[i..] и B[j..] по одной. */
    private static boolean sameLetters(String a, int i, String b, int j, int k) {
        for (int t = 0; t < k; t++) {
            if (a.charAt(i + t) != b.charAt(j + t)) return false;
        }
        return true;
    }
}
