package chess;

import chess.abstracts.Engine;
import chess.implementation.AlphaBetaSearch;
import chess.implementation.BitBoard;
import chess.implementation.Eval;
import chess.implementation.SortMoves;
import chess.visual.MyFrame;

import static chess.constants.FiguresKt.CONTINUE;
import static chess.constants.FiguresKt.WHITE;

public class RunJava {

    public static long POSITIONS = 0;

    public static final String startPosition = "r1bqkb1r/ppp1nppp/2n5/2Np4/4p3/5N2/PPPPPPPP/R1BQKB1R b KQkq - 1 3";

    public static final boolean PLAY_WITH_COMPUTER = false;
    public static final int COMPUTER_PLAY_BY = WHITE;
    public static final int TURN = WHITE;
    public static final int THREADS = 10;

    public static void main(String[] args) throws InterruptedException {
        if (PLAY_WITH_COMPUTER)
            playWithComputer();
        else
            compVsComp();
    }

    private static void playWithComputer() throws InterruptedException {
        Engine engine = new Engine(Engine.Companion.fromFENtoBitBoard(startPosition), new AlphaBetaSearch(new Eval(), THREADS), WHITE);

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
        AlphaBetaSearch search = new AlphaBetaSearch(new Eval(), THREADS);
        Engine engine = new Engine(BitBoard.Positions.start(new SortMoves(), search), search, TURN);

        MyFrame frame = new MyFrame(null);

        while (true) {
            long time = System.currentTimeMillis();
            int move = engine.getAIMove();
            System.out.println(System.currentTimeMillis() - time + "    " + POSITIONS + "    " + (POSITIONS / (System.currentTimeMillis() - time)));
            engine.makeMove(move);
            frame.setPosition((BitBoard) engine.getPosition());
            while (System.currentTimeMillis() - time < 0)
                Thread.sleep(10);

            if (engine.getPosition().result() != CONTINUE || ((BitBoard) engine.getPosition()).isCheckMate(engine.getTurn())) {
                int a = engine.getPosition().result();
                System.out.println(a);
                return;
            }
        }
    }
}
