import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

/**
 * Swing renderer for Dungeon Quest. The game rules remain in the existing
 * Dungeon/Game/Action classes; this class only displays state and collects input.
 */
public final class SwingRenderer implements Renderer {
    private static final int TILE_SIZE = 34;
    private static final int SIGHT_RADIUS = 5;

    private JFrame frame;
    private JPanel mapPanel;
    private JPanel statusPanel;
    private JTextArea log;
    private JLabel promptLabel;
    private Dungeon currentDungeon;

    public SwingRenderer() {
        runOnEdtAndWait(() -> {
            frame = new JFrame("Dungeon Quest");
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            frame.setLayout(new BorderLayout(8, 8));

            JLabel title = new JLabel("DUNGEON QUEST", SwingConstants.CENTER);
            title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
            title.setBorder(new EmptyBorder(10, 8, 4, 8));
            frame.add(title, BorderLayout.NORTH);

            mapPanel = new JPanel();
            mapPanel.setBorder(new EmptyBorder(8, 8, 8, 8));
            JScrollPane mapScroll = new JScrollPane(mapPanel);
            mapScroll.setPreferredSize(new Dimension(700, 560));
            frame.add(mapScroll, BorderLayout.CENTER);

            JPanel side = new JPanel(new BorderLayout(5, 5));
            side.setPreferredSize(new Dimension(270, 560));
            side.setBorder(new EmptyBorder(8, 0, 8, 8));
            JLabel partyTitle = new JLabel("PARTY STATUS");
            partyTitle.setFont(partyTitle.getFont().deriveFont(Font.BOLD, 14f));
            side.add(partyTitle, BorderLayout.NORTH);
            statusPanel = new JPanel();
            statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.Y_AXIS));
            side.add(statusPanel, BorderLayout.CENTER);
            log = new JTextArea(10, 24);
            log.setEditable(false);
            log.setLineWrap(true);
            log.setWrapStyleWord(true);
            log.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
            JScrollPane logScroll = new JScrollPane(log);
            logScroll.setBorder(BorderFactory.createTitledBorder("Adventure log"));
            side.add(logScroll, BorderLayout.SOUTH);
            frame.add(side, BorderLayout.EAST);

            promptLabel = new JLabel("Choose actions when prompted. Controls: N/S/E/W, A, C, X, F, R, U n, H, I, SAVE, Q");
            promptLabel.setBorder(new EmptyBorder(5, 10, 8, 10));
            frame.add(promptLabel, BorderLayout.SOUTH);

            frame.pack();
            frame.setMinimumSize(new Dimension(950, 650));
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    @Override
    public void render(Dungeon dungeon) {
        currentDungeon = dungeon;
        FieldOfView.updateExplored(dungeon, SIGHT_RADIUS);
        runOnEdtAndWait(() -> {
            mapPanel.removeAll();
            mapPanel.setLayout(new GridLayout(dungeon.getHeight(), dungeon.getWidth(), 1, 1));
            for (int y = 0; y < dungeon.getHeight(); y++) {
                for (int x = 0; x < dungeon.getWidth(); x++) {
                    mapPanel.add(tile(dungeon, x, y));
                }
            }
            statusPanel.removeAll();
            int n = 1;
            for (Adventurer a : dungeon.getAdventurers()) {
                String condition = a.isDead() ? "DEAD" : a.isUnconscious() ? "UNCONSCIOUS" : "ACTIVE";
                JLabel status = new JLabel("<html><b>" + n++ + ". " + escape(a.getName())
                        + " (" + a.getPlayerClass() + ")</b><br>HP: " + a.getHp() + "/" + a.getMaxHp()
                        + " &nbsp; MP: " + a.getMana() + "<br>Level: " + a.getLevel()
                        + " &nbsp; XP: " + a.getXp() + "<br>Gold: " + a.getGold()
                        + "<br>Status: " + condition + "</html>");
                status.setOpaque(true);
                status.setBackground(new Color(242, 244, 248));
                status.setBorder(new EmptyBorder(7, 7, 7, 7));
                status.setAlignmentX(Component.LEFT_ALIGNMENT);
                status.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
                statusPanel.add(status);
                statusPanel.add(Box.createVerticalStrut(6));
            }
            mapPanel.revalidate();
            mapPanel.repaint();
            statusPanel.revalidate();
            statusPanel.repaint();
            frame.setTitle("Dungeon Quest — " + dungeon.getWidth() + " × " + dungeon.getHeight());
        });
    }

    private JLabel tile(Dungeon d, int x, int y) {
        Room room = d.getRoom(x, y);
        boolean visible = FieldOfView.partyCanSee(d, x, y, SIGHT_RADIUS);
        String glyph;
        Color background;
        Color foreground;
        if (!visible && !room.isExplored()) {
            glyph = " ";
            background = new Color(24, 27, 34);
            foreground = background;
        } else {
            Character occupant = visible ? d.occupantAt(x, y) : null;
            if (occupant != null) {
                if (occupant instanceof Adventurer a) {
                    glyph = a.isUnconscious() ? "x" : Integer.toString(d.getAdventurers().indexOf(a) + 1);
                    foreground = new Color(40, 115, 220);
                } else {
                    glyph = occupant.getName().substring(0, 1).toUpperCase();
                    foreground = new Color(195, 55, 55);
                }
            } else {
                RoomType type = room.visibleType();
                glyph = String.valueOf(type.getSymbol());
                foreground = colorFor(type);
            }
            background = visible ? new Color(238, 238, 230) : new Color(185, 187, 190);
        }
        JLabel label = new JLabel(glyph, SwingConstants.CENTER);
        label.setFont(new Font(Font.MONOSPACED, Font.BOLD, 16));
        label.setForeground(foreground);
        label.setBackground(background);
        label.setOpaque(true);
        label.setPreferredSize(new Dimension(TILE_SIZE, TILE_SIZE));
        label.setBorder(BorderFactory.createLineBorder(new Color(90, 90, 90), 1));
        label.setToolTipText("x=" + x + ", y=" + y);
        return label;
    }

    private Color colorFor(RoomType type) {
        return switch (type) {
            case WALL -> new Color(85, 88, 95);
            case FLOOR -> new Color(75, 75, 75);
            case TREASURE, CHEST, SECRET -> new Color(190, 135, 20);
            case POTION -> new Color(150, 55, 190);
            case KEY -> new Color(20, 135, 165);
            case EXIT -> new Color(25, 145, 70);
            case TRAP -> new Color(190, 45, 45);
        };
    }

    @Override
    public void showMessage(String message) {
        runOnEdtAndWait(() -> {
            log.append(message + "\n");
            log.setCaretPosition(log.getDocument().getLength());
            promptLabel.setText(message.length() > 130 ? message.substring(0, 127) + "..." : message);
        });
    }

    public Optional<String> promptCommand(String playerName) {
        String command = JOptionPane.showInputDialog(
                frame,
                playerName + ", enter your command:\n"
                        + "Examples: MOVE NORTH, ATTACK, H, I, SAVE, Q",
                "Dungeon Quest",
                JOptionPane.PLAIN_MESSAGE
        );

        return Optional.ofNullable(command);
    }

    public void showInfo(String title, String message) {
        runOnEdtAndWait(() -> JOptionPane.showMessageDialog(frame, message, title, JOptionPane.INFORMATION_MESSAGE));
    }

    public String askText(String title, String prompt) {
        final String[] value = new String[1];
        runOnEdtAndWait(() -> value[0] = JOptionPane.showInputDialog(frame, prompt, title, JOptionPane.QUESTION_MESSAGE));
        return value[0] == null ? null : value[0].trim();
    }

    public int askChoice(String title, String prompt, String[] choices) {
        final int[] selected = {-1};
        runOnEdtAndWait(() -> selected[0] = JOptionPane.showOptionDialog(frame, prompt, title,
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, choices, choices[0]));
        return selected[0];
    }

    public void close() {
        runOnEdtAndWait(() -> frame.dispose());
    }

    private String escape(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private void runOnEdtAndWait(Runnable task) {
        if (SwingUtilities.isEventDispatchThread()) {
            task.run();
            return;
        }
        try {
            SwingUtilities.invokeAndWait(task);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (InvocationTargetException e) {
            throw new IllegalStateException("Swing UI update failed", e.getCause());
        }
    }
}
