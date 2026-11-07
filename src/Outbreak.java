import java.util.*;

/**
 * Mission 5. Dynamic programming on the tree of infections. Root the tree in person 0.
 * Input.Tree.neighbours() gives the neighbours of every person. The number of people is at most about 100, so a recursive DFS is fine here.
 */
public class Outbreak {

    /**
     * Choose people to test so that for every link (somebody infected somebody) at least one of the two is tested,
     * and the total cost of testing is as small as possible. cost[v] is the price of testing person v.
     * Returns tested[v] = true if person v is tested.
     *
     * State for every person v (looking only at v and everything below it): the smallest cost when v IS tested, and
     * the smallest cost when v is NOT tested. What must then be true about the children of v in each case?
     * Do not forget to rebuild the answer (who exactly is tested), not only the cost: the picture needs it.
     * Main calls this twice: with all costs equal to 1 (the line "Tests needed") and with the real costs ("Cheapest testing").
     */
    public static boolean[] cheapestTests(int n, int[][] nb, int[] cost) {
        boolean[] tested = new boolean[n];
        if (n == 0) return tested;

        int[] parent = new int[n];
        int[] order = bfsOrder(n, nb, parent);

        long[] yes = new long[n];   // минимум для поддерева v, если v проверен
        long[] no = new long[n];    // минимум для поддерева v, если v не проверен

        // 1) снизу вверх: сначала дети, потом родитель
        for (int idx = n - 1; idx >= 0; idx--) {
            int v = order[idx];
            long sumFree = 0;       // сумма min(yes, no) по детям
            long sumMust = 0;       // сумма yes по детям
            for (int c : nb[v]) {
                if (c == parent[v]) continue;          // родитель не ребёнок
                sumFree += Math.min(yes[c], no[c]);
                sumMust += yes[c];
            }
            yes[v] = cost[v] + sumFree;
            no[v] = sumMust;
        }

        // 2) сверху вниз: восстанавливаем, кто проверен
        tested[0] = yes[0] <= no[0];
        for (int idx = 1; idx < n; idx++) {
            int v = order[idx];
            int p = parent[v];
            if (!tested[p]) {
                tested[v] = true;                      // родитель не проверен: ребёнок обязан быть проверен
            } else {
                tested[v] = yes[v] <= no[v];           // родитель проверен: ребёнок выбирает дешевле
            }
        }
        return tested;
    }

    /**
     * The number of links on the longest chain of infections, that is the longest path in the tree (counted in links).
     * A path goes up from one person and down to another; for every person you need the two longest ways down.
     * Example: a line 0-1-2-3 has chain 3; a star with centre 0 and leaves 1, 2, 3 has chain 2.
     */
    public static int longestChain(int n, int[][] nb) {
        if (n == 0) return 0;

        int[] parent = new int[n];
        int[] order = bfsOrder(n, nb, parent);

        int[] top1 = new int[n];    // самая длинная ветка вниз от v (в рёбрах)
        int[] top2 = new int[n];    // вторая по длине ветка вниз от v
        int best = 0;

        for (int idx = n - 1; idx >= 0; idx--) {
            int v = order[idx];
            best = Math.max(best, top1[v] + top2[v]);  // лучший путь, у которого v самая верхняя точка

            if (parent[v] >= 0) {                      // передаём родителю свою ветку
                int p = parent[v];
                int len = top1[v] + 1;                 // +1 за ребро v-p
                if (len > top1[p]) {
                    top2[p] = top1[p];
                    top1[p] = len;
                } else if (len > top2[p]) {
                    top2[p] = len;
                }
            }
        }
        return best;
    }

    /** BFS от человека 0: возвращает порядок обхода и заполняет parent (у корня -1). */
    private static int[] bfsOrder(int n, int[][] nb, int[] parent) {
        int[] order = new int[n];
        boolean[] seen = new boolean[n];
        int head = 0, tail = 0;
        order[tail++] = 0;
        seen[0] = true;
        parent[0] = -1;
        while (head < tail) {
            int v = order[head++];
            for (int u : nb[v]) {
                if (!seen[u]) {
                    seen[u] = true;
                    parent[u] = v;
                    order[tail++] = u;
                }
            }
        }
        return order;
    }
}
