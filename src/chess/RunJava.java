package chess;

import chess.abstracts.Engine;
import chess.implementation.AlphaBetaSearch;
import chess.implementation.BitBoard;
import chess.implementation.Eval;
import chess.implementation.NullSort;
import chess.visual.MyFrame;

public class RunJava {
    public static void main(String[] args) throws InterruptedException {
        Engine engine = new Engine(BitBoard.Positions.start(new NullSort()), new AlphaBetaSearch(new Eval()), 0);

        MyFrame frame = new MyFrame((from, to) -> {
            boolean isLegal = ((BitBoard) engine.getPosition()).isMoveLegal(from, to, 1);
            if (isLegal) {
                engine.makeMove(from, to);
                return true;
            }
            return false;
        });
        frame.setPosition((BitBoard) engine.getPosition());
        while (true) {
            while (engine.getTurn() != 0)
                Thread.sleep(10);
            Move move = engine.getAIMove();
            engine.makeMove(move);
            frame.setPosition((BitBoard) engine.getPosition());
        }
    }
}
