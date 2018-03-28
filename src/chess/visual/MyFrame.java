package chess.visual;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;

import chess.implementation.BitBoard;


public class MyFrame extends JFrame {

    private JButton[][] buttons;
    private int[] lastClick = null;
    private MoveListener listener;

    public MyFrame(MoveListener listener) {
        buttons = new JButton[8][8];
        this.listener = listener;
        frameInitialise();
    }

    public void setListener(MoveListener listener) {
        this.listener = listener;
    }

    private void frameInitialise() {
        super.frameInit();

        setSize(640, 640);
        setResizable(false);

        final Dimension screenSize = getToolkit().getScreenSize();
        final int centerX = screenSize.width / 2, centerY = screenSize.height / 2;
        setLocation(centerX - 640 / 2, centerY - 640 / 2);

        setLayout(new GridLayout(8, 8));

        for (int y = 7; y >= 0; y--) {
            for (int x = 0; x < 8; x++) {
                final int x0 = x, y0 = y;

                buttons[x][y] = new JButton();
                add(buttons[x][y]);
                buttons[x][y].setAction(new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent actionEvent) {
                        if (lastClick == null)
                            lastClick = new int[]{x0, y0};
                        else {
                            if (listener.move(transform(lastClick), transform(x0, y0)))
                                lastClick = null;
                            else
                                lastClick = new int[]{x0, y0};
                        }
                    }
                });
            }
        }
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
    }

    public void setPosition(BitBoard position) {
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                String color;
                if ((x + y) % 2 == 0)
                    color = "_W";
                else
                    color = "_B";
                String[] symbols = {"K", "R", "N", "B", "Q", "P"};
                label: for (int c = 0; c < 2; c++) {
                    for (int i = 0; i < 6; i++) {
                        if ((position.getFigures()[c][i] & (1L << transform(x, y))) != 0) {
                            buttons[x][y].setIcon(new ImageIcon(
                                    "icons/" + (c == 0 ? "W" : "B") + symbols[i] + color + ".png"
                            ));
                            break label;
                        } else {
                            buttons[x][y].setIcon(new ImageIcon(
                                    "icons/" + color + ".png"
                            ));
                        }
                    }
                }
            }
        }
        sounds("sounds/move.wav");
    }

    static void sounds(String sound) {
        try {
            File soundFile = new File(sound);
            AudioInputStream ais = AudioSystem.getAudioInputStream(soundFile);
            Clip clip = AudioSystem.getClip();
            clip.open(ais);
            clip.setFramePosition(0);
            clip.start();
        } catch (IOException | UnsupportedAudioFileException | LineUnavailableException exc) {
            exc.printStackTrace();
        }
    }

    private int transform(int... cord) {
        return 8 * cord[1] + cord[0];
    }

    public interface MoveListener {

        boolean move(int from, int to);
    }
}
