package chess;

import chess.abstracts.Engine;
import chess.implementation.AlphaBetaSearch;
import chess.implementation.BitBoard;
import chess.implementation.Eval;
import chess.implementation.SortMoves;
import chess.visual.MyFrame;

import static chess.constants.FiguresKt.BLACK;
import static chess.constants.FiguresKt.CONTINUE;
import static chess.constants.FiguresKt.WHITE;

public class RunJava {

    public static long ALL_TIME = 0;

    private static final boolean PLAY_WITH_COMPUTER = false;
    private static final int COMPUTER_PLAY_BY = WHITE;

    public static void main(String[] args) throws InterruptedException {
        if (PLAY_WITH_COMPUTER)
            playWithComputer();
        else
            compVsComp();
    }

    private static void playWithComputer() throws InterruptedException {
        Engine engine = new Engine(BitBoard.Positions.start(new SortMoves()), new AlphaBetaSearch(new Eval()), 0);

        MyFrame frame = new MyFrame(null);
        frame.setListener((from, to) -> {
            boolean isLegal = ((BitBoard) engine.getPosition()).isMoveLegal(from, to, 1 - COMPUTER_PLAY_BY);
            if (isLegal) {
                engine.makeMove(from, to);
                frame.setPosition((BitBoard) engine.getPosition());
                return true;
            }
            return false;
        });
        frame.setPosition((BitBoard) engine.getPosition());
        while (true) {
            while (engine.getTurn() != COMPUTER_PLAY_BY)
                Thread.sleep(10);
            int move = engine.getAIMove();
            engine.makeMove(move);
            frame.setPosition((BitBoard) engine.getPosition());
        }
    }

    private static void compVsComp() throws InterruptedException {
        Engine engine = new Engine(BitBoard.Positions.start(new SortMoves()), new AlphaBetaSearch(new Eval()), 0);

        MyFrame frame = new MyFrame(null);

        while (true) {
            long time = System.currentTimeMillis();
            int move = engine.getAIMove();
            System.out.println(System.currentTimeMillis() - time + "    " + ALL_TIME);
            engine.makeMove(move);
            frame.setPosition((BitBoard) engine.getPosition());
            while (System.currentTimeMillis() - time < 1500)
                Thread.sleep(10);

            if (engine.getPosition().result() != CONTINUE || ((BitBoard) engine.getPosition()).isCheckMate(engine.getTurn())) {
                int a = engine.getPosition().result();
                return;
            }
            return;
        }
    }
}
