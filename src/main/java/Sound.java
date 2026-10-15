import java.awt.AWTError;
import java.awt.Toolkit;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

/**
 * Sound effects built from generated tones, so no audio files are needed.
 * Sounds play on a background thread and never slow the game down.
 * With no audio device, the important cues fall back to the terminal beep.
 */
public final class Sound {
    private record Tone(int hz, int millis) { }

    /** Each cue is a short run of tones. A pitch of 0 is a pause. */
    public enum Cue {
        STEP(0.20, new Tone(520, 35)),
        HIT(0.50, new Tone(300, 60), new Tone(200, 90)),
        MISS(0.30, new Tone(900, 40), new Tone(500, 60)),
        SPELL(0.40, new Tone(400, 60), new Tone(600, 60), new Tone(800, 80)),
        DISARM(0.40, new Tone(880, 80), new Tone(1320, 140)),
        KILL(0.50, new Tone(330, 80), new Tone(440, 130)),
        TRAP(0.50, new Tone(160, 120), new Tone(0, 30), new Tone(160, 180)),
        DOWN(0.50, new Tone(300, 150), new Tone(200, 150), new Tone(120, 300)),
        VICTORY(0.45, new Tone(523, 140), new Tone(659, 140), new Tone(784, 140),
                new Tone(1047, 320)),
        DEFEAT(0.45, new Tone(392, 200), new Tone(330, 200), new Tone(262, 200),
                new Tone(196, 420)),
        LEVEL_UP(0.45, new Tone(523, 90), new Tone(659, 90), new Tone(784, 160));

        private final double volume;
        private final Tone[] tones;

        Cue(double volume, Tone... tones) {
            this.volume = volume;
            this.tones = tones;
        }
    }

    private static final int SAMPLE_RATE = 22050;
    private static final int FADE_SAMPLES = SAMPLE_RATE / 200;      // 5 ms soft edges stop clicks
    private static final int BUFFER_BYTES = 4096;
    private static final AudioFormat FORMAT = new AudioFormat(SAMPLE_RATE, 16, 1, true, false);

    private static final ExecutorService PLAYER = Executors.newSingleThreadExecutor(task -> {
        Thread thread = new Thread(task, "sound");
        thread.setDaemon(true);
        return thread;
    });

    private static volatile boolean enabled = true;
    private static SourceDataLine line;          // only ever touched by the sound thread
    private static boolean audioBroken;          // only ever touched by the sound thread

    private Sound() { }

    public static void setEnabled(boolean on) { enabled = on; }

    public static boolean isEnabled() { return enabled; }

    /** Queues the sound and returns straight away. */
    public static void play(Cue cue) {
        if (enabled) PLAYER.execute(() -> playNow(cue));
    }

    /** The raw 16-bit mono samples for a cue. */
    static byte[] render(Cue cue) {
        int total = 0;
        for (Tone tone : cue.tones) total += samples(tone);
        byte[] data = new byte[total * 2];
        int at = 0;
        for (Tone tone : cue.tones) {
            int count = samples(tone);
            for (int i = 0; i < count; i++) {
                short value = sample(tone, i, count, cue.volume);
                data[at++] = (byte) (value & 0xFF);
                data[at++] = (byte) ((value >> 8) & 0xFF);
            }
        }
        return data;
    }

    private static int samples(Tone tone) {
        return SAMPLE_RATE * tone.millis() / 1000;
    }

    private static short sample(Tone tone, int index, int count, double volume) {
        double wave = tone.hz() == 0 ? 0 : Math.sin(2 * Math.PI * tone.hz() * index / SAMPLE_RATE);
        double fade = Math.min(1.0, Math.min(index, count - 1 - index) / (double) FADE_SAMPLES);
        return (short) (wave * fade * volume * Short.MAX_VALUE);
    }

    // ---------- playback, on the sound thread ----------

    private static void playNow(Cue cue) {
        if (!audioBroken) {
            try {
                if (line == null) openLine();
                byte[] data = render(cue);
                line.write(data, 0, data.length);
                return;
            } catch (LineUnavailableException | RuntimeException e) {
                audioBroken = true;
            }
        }
        fallbackBeep(cue);
    }

    private static void openLine() throws LineUnavailableException {
        SourceDataLine opened = AudioSystem.getSourceDataLine(FORMAT);
        opened.open(FORMAT, BUFFER_BYTES);
        opened.start();
        line = opened;
    }

    private static void fallbackBeep(Cue cue) {
        if (cue == Cue.STEP || cue == Cue.MISS) return;       // too frequent for a bell
        try {
            Toolkit.getDefaultToolkit().beep();
        } catch (RuntimeException | AWTError e) {
            // no display and no audio device: stay silent
        }
    }
    public static void testSound() {
        play(Cue.VICTORY);
    }
}