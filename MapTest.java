import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Tests for the map: bounds, walls, who may stand where, line of sight,
 * and loading levels from text files (including broken ones).
 */
class MapTest {
    /** Helper: a dungeon with a wall border and open floor inside. */
    private Dungeon room(int width, int height) {
        Dungeon d = new Dungeon(width, height);
        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) d.setRoom(x, y, RoomType.FLOOR);
        }
        return d;
    }

    /** Helper: writes a level to a temporary file that is deleted when the tests end. */
    private Path levelFile(String... rows) throws IOException {
        Path file = Files.createTempFile("dq-level", ".txt");
        file.toFile().deleteOnExit();
        Files.write(file, List.of(rows));
        return file;
    }

    @Test
    void boundsIncludeCornersAndExcludeEverythingOutside() {
        // A 15x15 map has cells 0..14. Test the corners and one step past each edge.
        Dungeon d = new Dungeon(15, 15);
        assertTrue(d.inBounds(0, 0));
        assertTrue(d.inBounds(14, 14));
        assertFalse(d.inBounds(-1, 0));
        assertFalse(d.inBounds(0, -1));
        assertFalse(d.inBounds(15, 0));
        assertFalse(d.inBounds(0, 15));
    }

    @Test
    void newDungeonIsSolidWall() {
        Dungeon d = new Dungeon(5, 5);
        assertEquals(RoomType.WALL, d.getRoom(2, 2).getType());
        assertFalse(d.canEnter(2, 2));
    }

    @Test
    void cannotEnterWallsOrOutsideTheMap() {
        Dungeon d = room(5, 5);
        assertFalse(d.canEnter(0, 0));               // border wall
        assertFalse(d.canEnter(-1, 2));              // off the left edge
        assertFalse(d.canEnter(2, 9));               // far below the map
        assertTrue(d.canEnter(2, 2));                // open floor
    }

    @Test
    void occupiedCellsAreBlockedUntilTheOccupantDies() {
        Dungeon d = room(5, 5);
        d.addAdventurer(new Adventurer("A", PlayerClass.WARRIOR, 2, 2));
        assertFalse(d.canEnter(2, 2));               // an adventurer stands here
        Goblin goblin = new Goblin();
        goblin.moveTo(3, 2);
        d.addMonster(goblin);
        assertFalse(d.canEnter(3, 2));               // a living monster blocks the cell
        goblin.takeDamage(100);
        assertTrue(d.canEnter(3, 2));                // a dead one no longer does
    }

    @Test
    void settingARoomOutsideTheMapIsIgnored() {
        // Must not throw: setRoom checks the bounds itself.
        Dungeon d = room(5, 5);
        d.setRoom(99, 99, RoomType.TREASURE);
        d.setRoom(-1, -1, RoomType.TREASURE);
        assertEquals(RoomType.FLOOR, d.getRoom(2, 2).getType());
    }

    @Test
    void movingIntoAWallIsRefusedAndMovingIntoFloorWorks() {
        Dungeon d = room(5, 5);
        Adventurer a = new Adventurer("A", PlayerClass.WARRIOR, 1, 1);
        d.addAdventurer(a);
        new MoveAction(a, Direction.N).execute(d);   // wall above
        new MoveAction(a, Direction.W).execute(d);   // wall to the left
        assertEquals(1, a.getX());
        assertEquals(1, a.getY());                   // did not move
        new MoveAction(a, Direction.E).execute(d);   // open floor
        assertEquals(2, a.getX());
    }

    @Test
    void wallsBlockSightAndRadiusLimitsIt() {
        Dungeon d = room(11, 3);
        assertTrue(FieldOfView.canSee(d, 1, 1, 4, 1, 5));    // 3 cells away, radius 5
        assertFalse(FieldOfView.canSee(d, 1, 1, 8, 1, 5));   // 7 cells away: too far
        d.setRoom(4, 1, RoomType.WALL);
        assertFalse(FieldOfView.canSee(d, 1, 1, 6, 1, 8));   // wall in the way
        assertTrue(FieldOfView.canSee(d, 1, 1, 4, 1, 8));    // the wall itself is visible
    }

    @Test
    void loaderReadsASmallLevel() throws Exception {
        // # wall, A start, G goblin, E exit.
        Dungeon d = LevelLoader.load(levelFile("#####", "#AGE#", "#####"));
        assertEquals(5, d.getWidth());
        assertEquals(3, d.getHeight());
        assertEquals(1, d.getStartX());
        assertEquals(1, d.getStartY());
        assertEquals(1, d.getMonsters().size());
    }

    @Test
    void loaderRejectsBrokenLevels() {
        // Every kind of bad file must give a LevelLoadException, never a crash.
        assertThrows(LevelLoadException.class,
                () -> LevelLoader.load(levelFile("#####", "#AE#", "#####")));    // ragged row
        assertThrows(LevelLoadException.class,
                () -> LevelLoader.load(levelFile("#####", "#AZE#", "#####")));   // unknown symbol
        assertThrows(LevelLoadException.class,
                () -> LevelLoader.load(levelFile("#####", "#A..#", "#####")));   // no exit
        assertThrows(LevelLoadException.class,
                () -> LevelLoader.load(levelFile("#####", "#..E#", "#####")));   // no start
        assertThrows(LevelLoadException.class,
                () -> LevelLoader.load(levelFile("   ")));                       // empty file
        assertThrows(LevelLoadException.class,
                () -> LevelLoader.load(Path.of("no_such_level.txt")));           // missing file
    }

    @Test
    void shippedLevelsAreBigEnough() throws Exception {
        // Brief: the main levels must be at least 15 x 15. Reads the real files in levels/.
        for (String name : List.of("level1.txt", "level2.txt", "level3.txt")) {
            Dungeon d = LevelLoader.load(Path.of("levels", name));
            assertTrue(d.getWidth() >= 15 && d.getHeight() >= 15, name + " is smaller than 15x15");
        }
    }
}
