package chess;

import chess.abstracts.Engine;
import chess.implementation.AlphaBetaSearch;
import chess.implementation.BitBoard;
import chess.implementation.Eval;
import chess.implementation.SortMoves;
import chess.visual.MyFrame;

import static chess.constants.FiguresKt.BLACK;

public class RunJava {

    private static final int COMPUTER_PLAY_BY = BLACK;

    public static void main(String[] args) throws InterruptedException {
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
            Move move = engine.getAIMove();
            engine.makeMove(move);
            frame.setPosition((BitBoard) engine.getPosition());
        }
    }
}
