import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Random;

/**
 * Procedural dungeons. A random walk with backtracking carves a maze of 7x7 rooms,
 * a few walls are knocked down to make loops, then loot and monsters are scattered.
 * Everything is reachable from the start, so every level can be finished.
 */
public final class LevelGenerator {
    private static final int CELLS = 7;
    private static final int SIZE = CELLS * 2 + 1;             // 15 x 15 characters
    private static final int LOOPS = 6;
    private static final int MIN_LOOT_DISTANCE = 4;
    private static final int MIN_MONSTER_DISTANCE = 6;
    private static final int BOSS_ROOM_FROM = SIZE - 6;         // the open room beside the exit
    private static final int[][] STEPS = {{2, 0}, {-2, 0}, {0, 2}, {0, -2}};

    private record Feature(char symbol, int count) { }

    private LevelGenerator() { }

    /** difficulty 1 = easy, 2 = medium, 3 = hard (adds the dragon). */
    public static List<String> generate(int difficulty, Random random) {
        char[][] grid = carveMaze(random);
        addLoops(grid, random);
        boolean bossRoom = difficulty >= 3;
        if (bossRoom) openBossRoom(grid);
        grid[1][1] = 'A';
        grid[SIZE - 2][SIZE - 2] = 'E';
        scatter(grid, plan(difficulty), bossRoom, random);
        List<String> rows = new ArrayList<>();
        for (char[] row : grid) rows.add(new String(row));
        return rows;
    }

    /** Writes generated1.txt, generated2.txt and generated3.txt into the folder. */
    public static void writeCampaign(Path folder, Random random) throws IOException {
        Files.createDirectories(folder);
        for (int level = 1; level <= 3; level++) {
            Files.write(folder.resolve("generated" + level + ".txt"), generate(level, random));
        }
    }

    // ---------- the maze ----------

    private static char[][] carveMaze(Random random) {
        char[][] grid = new char[SIZE][SIZE];
        for (char[] row : grid) Arrays.fill(row, '#');
        Deque<int[]> stack = new ArrayDeque<>();
        grid[1][1] = '.';
        stack.push(new int[] {1, 1});
        while (!stack.isEmpty()) {
            int[] cur = stack.peek();
            List<int[]> options = uncarved(grid, cur[0], cur[1]);
            if (options.isEmpty()) {
                stack.pop();
                continue;
            }
            int[] next = options.get(random.nextInt(options.size()));
            grid[(cur[1] + next[1]) / 2][(cur[0] + next[0]) / 2] = '.';
            grid[next[1]][next[0]] = '.';
            stack.push(next);
        }
        return grid;
    }

    private static List<int[]> uncarved(char[][] grid, int x, int y) {
        List<int[]> options = new ArrayList<>();
        for (int[] step : STEPS) {
            int nx = x + step[0];
            int ny = y + step[1];
            if (nx > 0 && nx < SIZE - 1 && ny > 0 && ny < SIZE - 1 && grid[ny][nx] == '#') {
                options.add(new int[] {nx, ny});
            }
        }
        return options;
    }

    private static void addLoops(char[][] grid, Random random) {
        List<int[]> walls = new ArrayList<>();
        for (int y = 1; y < SIZE - 1; y++) {
            for (int x = 1; x < SIZE - 1; x++) {
                boolean between = (x % 2 == 0) != (y % 2 == 0);
                if (grid[y][x] == '#' && between) walls.add(new int[] {x, y});
            }
        }
        Collections.shuffle(walls, random);
        for (int i = 0; i < Math.min(LOOPS, walls.size()); i++) {
            grid[walls.get(i)[1]][walls.get(i)[0]] = '.';
        }
    }

    private static void openBossRoom(char[][] grid) {
        for (int y = BOSS_ROOM_FROM; y < SIZE - 1; y++) {
            for (int x = BOSS_ROOM_FROM; x < SIZE - 1; x++) grid[y][x] = '.';
        }
    }

    // ---------- loot and monsters ----------

    private static List<Feature> plan(int difficulty) {
        List<Feature> plan = new ArrayList<>(List.of(
                new Feature('T', 2 + difficulty), new Feature('P', 2), new Feature('K', 1),
                new Feature('C', 1), new Feature('H', 1), new Feature('^', difficulty + 1)));
        switch (Math.max(1, Math.min(3, difficulty))) {
            case 1 -> plan.addAll(List.of(new Feature('G', 2), new Feature('S', 1)));
            case 2 -> plan.addAll(List.of(new Feature('G', 3), new Feature('S', 2), new Feature('O', 1)));
            default -> plan.addAll(List.of(new Feature('G', 2), new Feature('S', 2), new Feature('O', 2)));
        }
        return plan;
    }

    private static void scatter(char[][] grid, List<Feature> plan, boolean bossRoom, Random random) {
        int[][] distance = distancesFromStart(grid);
        List<int[]> cells = new ArrayList<>();
        for (int y = 1; y < SIZE - 1; y++) {
            for (int x = 1; x < SIZE - 1; x++) {
                boolean inBossRoom = bossRoom && x >= BOSS_ROOM_FROM && y >= BOSS_ROOM_FROM;
                if (grid[y][x] == '.' && distance[y][x] >= MIN_LOOT_DISTANCE && !inBossRoom) {
                    cells.add(new int[] {x, y});
                }
            }
        }
        Collections.shuffle(cells, random);
        placeAll(grid, plan, cells, distance);
        if (bossRoom) grid[SIZE - 4][SIZE - 4] = 'D';
    }

    private static void placeAll(char[][] grid, List<Feature> plan, List<int[]> cells, int[][] distance) {
        int next = 0;
        for (Feature feature : plan) {
            boolean monster = "GSO".indexOf(feature.symbol()) >= 0;
            int placed = 0;
            while (placed < feature.count() && next < cells.size()) {
                int[] c = cells.get(next++);
                if (monster && distance[c[1]][c[0]] < MIN_MONSTER_DISTANCE) continue;
                grid[c[1]][c[0]] = feature.symbol();
                placed++;
            }
        }
    }

    private static int[][] distancesFromStart(char[][] grid) {
        int[][] distance = new int[SIZE][SIZE];
        for (int[] row : distance) Arrays.fill(row, -1);
        Deque<int[]> queue = new ArrayDeque<>();
        distance[1][1] = 0;
        queue.add(new int[] {1, 1});
        while (!queue.isEmpty()) {
            int[] cur = queue.poll();
            for (int[] step : new int[][] {{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
                int nx = cur[0] + step[0];
                int ny = cur[1] + step[1];
                if (grid[ny][nx] != '#' && distance[ny][nx] < 0) {
                    distance[ny][nx] = distance[cur[1]][cur[0]] + 1;
                    queue.add(new int[] {nx, ny});
                }
            }
        }
        return distance;
    }
}