import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SoundTest {
    private short[] samples(Sound.Cue cue) {
        byte[] data = Sound.render(cue);
        short[] out = new short[data.length / 2];
        for (int i = 0; i < out.length; i++) {
            out[i] = (short) ((data[2 * i] & 0xFF) | (data[2 * i + 1] << 8));
        }
        return out;
    }

    @Test
    void everyCueRendersSomeSound() {
        for (Sound.Cue cue : Sound.Cue.values()) {
            short[] wave = samples(cue);
            assertTrue(wave.length > 0, cue + " is empty");
            boolean audible = false;
            for (short s : wave) if (s != 0) audible = true;
            assertTrue(audible, cue + " is silent");
        }
    }

    @Test
    void theStepTickHasTheRightLengthAndPitch() {
        short[] wave = samples(Sound.Cue.STEP);                 // 520 Hz for 35 ms at 22050 Hz
        assertEquals(771, wave.length);
        int crossings = 0;
        for (int i = 1; i < wave.length; i++) {
            if ((wave[i - 1] < 0) != (wave[i] < 0)) crossings++;
        }
        assertTrue(crossings >= 32 && crossings <= 40, "zero crossings: " + crossings);
    }

    @Test
    void soundsStartAndEndSoftlyAndStayUnderTheirVolume() {
        short[] wave = samples(Sound.Cue.STEP);                 // volume 0.20
        assertEquals(0, wave[0]);
        assertEquals(0, wave[wave.length - 1]);
        int peak = 0;
        for (short s : wave) peak = Math.max(peak, Math.abs(s));
        assertTrue(peak <= 0.20 * Short.MAX_VALUE + 1, "peak " + peak);
        assertTrue(peak > 0.15 * Short.MAX_VALUE, "too quiet: " + peak);
    }

    @Test
    void aTrapBuzzHasAGapInTheMiddle() {
        short[] wave = samples(Sound.Cue.TRAP);                 // tone, 30 ms pause, tone
        int start = 22050 * 120 / 1000;
        int pause = 22050 * 30 / 1000;
        for (int i = start; i < start + pause; i++) assertEquals(0, wave[i]);
    }

    @Test
    void soundCanBeSwitchedOffAndOn() {
        boolean before = Sound.isEnabled();
        Sound.setEnabled(false);
        assertFalse(Sound.isEnabled());
        Sound.play(Sound.Cue.KILL);                              // must do nothing, silently
        Sound.setEnabled(true);
        assertTrue(Sound.isEnabled());
        Sound.setEnabled(before);
    }
}
