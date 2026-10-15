import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

/**
 * Tests for the procedural level generator (bonus goal). A fixed Random seed makes
 * each level repeatable, so these tests never fail by chance.
 */
class GeneratorTest {
    /** Helper: counts how many times a symbol appears in the level. */
    private int count(List<String> rows, char symbol) {
        int n = 0;
        for (String row : rows) for (char c : row.toCharArray()) if (c == symbol) n++;
        return n;
    }

    /** Helper: walks out from the start and checks that every open cell was reached. */
    private boolean everythingReachable(List<String> rows) {
        int size = rows.size();
        boolean[][] seen = new boolean[size][size];
        Deque<int[]> queue = new ArrayDeque<>();
        seen[1][1] = true;                           // the generator always starts at (1,1)
        queue.add(new int[] {1, 1});
        while (!queue.isEmpty()) {
            int[] cur = queue.poll();
            for (int[] step : new int[][] {{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
                int nx = cur[0] + step[0];
                int ny = cur[1] + step[1];
                if (rows.get(ny).charAt(nx) != '#' && !seen[ny][nx]) {
                    seen[ny][nx] = true;
                    queue.add(new int[] {nx, ny});
                }
            }
        }
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                if (rows.get(y).charAt(x) != '#' && !seen[y][x]) return false;
            }
        }
        return true;
    }

    @Test
    void everyGeneratedLevelIsValidAndSolvable() {
        // 100 different levels at each of the 3 difficulties = 300 levels checked.
        for (int difficulty = 1; difficulty <= 3; difficulty++) {
            for (long seed = 0; seed < 100; seed++) {
                List<String> rows = generate(difficulty, seed);
                String where = "difficulty " + difficulty + ", seed " + seed;
                assertEquals(15, rows.size());
                for (String row : rows) assertEquals(15, row.length());
                assertEquals(1, count(rows, 'A'), where);        // exactly one start
                assertEquals(1, count(rows, 'E'), where);        // exactly one exit
                assertTrue(everythingReachable(rows), "unreachable cell at " + where);
            }
        }
    }

    @Test
    void onlyTheHardestLevelHasADragon() {
        for (long seed = 0; seed < 50; seed++) {
            assertEquals(0, count(generate(1, seed), 'D'));
            assertEquals(0, count(generate(2, seed), 'D'));
            assertEquals(1, count(generate(3, seed), 'D'));
        }
    }

    @Test
    void harderLevelsHaveMoreMonsters() {
        int easy = monsters(generate(1, 5));
        int hard = monsters(generate(3, 5));
        assertTrue(hard > easy, "easy " + easy + ", hard " + hard);
    }

    @Test
    void sameSeedGivesTheSameLevel() {
        // Same seed in, same dungeon out: proves the generator is repeatable.
        assertEquals(generate(2, 42), generate(2, 42));
    }

    @Test
    void generatedLevelsLoadIntoADungeon() throws Exception {
        // The real LevelLoader must accept what the generator writes.
        Path file = Files.createTempFile("dq-gen", ".txt");
        file.toFile().deleteOnExit();
        Files.write(file, generate(3, 9));
        Dungeon d = LevelLoader.load(file);
        assertEquals(15, d.getWidth());
        assertTrue(d.getMonsters().size() >= 7);
    }

    private List<String> generate(int difficulty, long seed) {
        return LevelGenerator.generate(difficulty, new Random(seed));
    }

    private int monsters(List<String> rows) {
        return count(rows, 'G') + count(rows, 'S') + count(rows, 'O') + count(rows, 'D');
    }
}
