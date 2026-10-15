import java.util.Scanner;

public interface Event {
    String getName();

    /** Applies the event and returns a message. The Scanner is for events that ask questions. */
    String trigger(Dungeon dungeon, Scanner in);
}
