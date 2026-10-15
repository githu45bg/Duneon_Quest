import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class Dungeon {
    private final int width;
    private final int height;
    private final Room[][] grid;
    private final List<Monster> monsters = new ArrayList<>();
    private final List<Adventurer> adventurers = new ArrayList<>();
    private int startX;
    private int startY;

    public Optional<Monster> adjacentMonster(Character c) {
        for (Monster m : monsters) {
            int dist = Math.abs(m.getX() - c.getX()) + Math.abs(m.getY() - c.getY());
            if (m.isAlive() && dist == 1) return Optional.of(m);
        }
        return Optional.empty();
    }
    public List<Adventurer> livingAdventurers() {
        return adventurers.stream().filter(a -> !a.isDead()).toList();
    }

    public Optional<int[]> find(RoomType type) {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (grid[y][x].getType() == type) return Optional.of(new int[] {x, y});
            }
        }
        return Optional.empty();
    }

    public Dungeon(int width, int height) {
        this.width = width;
        this.height = height;
        this.grid = new Room[height][width];          // grid[row][col]
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                grid[y][x] = new Room(RoomType.WALL);
            }
        }
    }

    public int getWidth()  { return width; }
    public int getHeight() { return height; }

    public boolean inBounds(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    public Room getRoom(int x, int y) { return grid[y][x]; }

    public void setRoom(int x, int y, RoomType type) {
        if (inBounds(x, y)) grid[y][x].setType(type);
    }

    /** True if the cell exists, is walkable, and nobody alive stands there. */
    public boolean canEnter(int x, int y) {
        return inBounds(x, y) && grid[y][x].isWalkable() && !isOccupied(x, y);
    }

    public boolean isOccupied(int x, int y) {
        return occupantAt(x, y) != null;
    }

    public Character occupantAt(int x, int y) {
        for (Monster m : monsters) {
            if (m.isAlive() && m.getX() == x && m.getY() == y) return m;
        }
        for (Adventurer a : adventurers) {
            if (!a.isDead() && a.getX() == x && a.getY() == y) return a;
        }
        return null;
    }

    public void addMonster(Monster m)       { monsters.add(m); }
    public void addAdventurer(Adventurer a) { adventurers.add(a); }
    public void removeDeadMonsters()        { monsters.removeIf(m -> !m.isAlive()); }

    public List<Monster> getMonsters() {
        return Collections.unmodifiableList(monsters);
    }
    public List<Adventurer> getAdventurers() {
        return Collections.unmodifiableList(adventurers);
    }

    public void setStart(int x, int y) { startX = x; startY = y; }
    public int getStartX() { return startX; }
    public int getStartY() { return startY; }
}
