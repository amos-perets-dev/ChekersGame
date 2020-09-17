package com.example.chekersgamepro.ai;

import android.util.Log;

import com.example.chekersgamepro.checkers.CheckersApplication;
import com.example.chekersgamepro.data.data_game.DataGame;
import com.example.chekersgamepro.data.move.DataMove;
import com.example.chekersgamepro.data.move.Move;
import com.google.firebase.database.core.utilities.TreeNode;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

public class AIComputerMove {

    private int LEVEL_LIMIT = DataGame.getInstance().getDifficultLevel();

    public Move getMoveAI(DataGameBoard dataGameBoard, boolean isPlayerOnCurrently) {
        Log.d("TEST_GAME", "getMoveAI LEVEL_LIMIT: " + LEVEL_LIMIT);

        Log.d("TEST_GAME", "start getMoveAI");
        Tree mainTree = new Tree(dataGameBoard, null, dataGameBoard.getEvaluateBoard(isPlayerOnCurrently));
        Tree optionalMoves = createMovesAI(mainTree, isPlayerOnCurrently, 0);

        optionalMoves.print();
        Log.d("TEST_GAME", "*************************************");
        Log.d("TEST_GAME", "*************************************");
        Log.d("TEST_GAME", "*************************************");

        Tree minimax = createMinimaxAlphaBeta(optionalMoves, true, LEVEL_LIMIT, Integer.MIN_VALUE, Integer.MAX_VALUE);
        minimax.print();
        Log.d("TEST_GAME", "*************************************");
        Log.d("TEST_GAME", "*************************************");

        List<Tree> childList = minimax.getChildList();
        float max = -999F;
        int index = 0;
        for (int i = 0; i < childList.size(); i++) {
            Tree child = childList.get(i);
            float score = child.getScore();
            Log.d("TEST_GAME", "score: " + score + ", max: " + max + ", index: " + index);

            if (score > max) {
                max = score;
                index = i;
            }
        }

        Move bestMove = null;
//        float max = -9999;


        Tree tree = childList.get(index);
        Log.d("TEST_GAME", "MAX: " + tree.getScore());

//        CheckersApplication.create().showToast("MAX CHOOSE: " + tree.getScore() + ", MAX: " + max);

        Move move = tree.getMove();
        return move;
    }

    private Tree createMovesAI(Tree root, boolean isPlayerOneCurrently, int level) {
        if (LEVEL_LIMIT == level) return root;

        List<DataMove> relevantMovesCellsStartFirst = new ArrayList<>(((DataGameBoard) root.getDataGameBoard()).createMovesByCellsStart(isPlayerOneCurrently));
        for (DataMove dataMove : relevantMovesCellsStartFirst) {
            DataGameBoard currGameBoard = ((DataGameBoard) root.getDataGameBoard()).getCopyBoard();

            currGameBoard.setMovePawnPath(dataMove, isPlayerOneCurrently);
            Move move = new Move(dataMove.getMove().getStartPoint(), dataMove.getMove().getEndPoint(), dataMove.getMove().getIdCell());
//            float score = isPlayerOneCurrently ?  Integer.MAX_VALUE    : Integer.MIN_VALUE ;
            float score = -999;
            //check if is the leafs layer
            if (LEVEL_LIMIT == (level + 1)) {
                score = currGameBoard.getEvaluateBoard(isPlayerOneCurrently);

            }
//            Log.d("TEST_GAME", "LEVEL: " + level + ", ONE COUNT: " + (currGameBoard.getPlayerOneCount() + currGameBoard.getPlayerOneKingsCount()) +
//                     " , TWO COUNT: " + (currGameBoard.getPlayerTwoCount() + currGameBoard.getPlayerTwoKingsCount()) +
//                    " , SCORE: " + score + ", FROM: " + move.getStartPoint() +", TO: " + move.getEndPoint());
            Tree currentLayer = new Tree(currGameBoard, move, score);

            root.addChild(currentLayer);
            createMovesAI(currentLayer, !isPlayerOneCurrently, level + 1);
        }

        return root;
    }

    private Move bestMove = null;
    private Tree eval;

    private Tree createMinimaxAlphaBeta(Tree root, boolean isPlayerOneCurrently, int level, double alpha, double beta) {
        if (level == 0) return root;

        if (isPlayerOneCurrently) {
//            Tree maxEval = new Tree(null, null, Integer.MIN_VALUE);
            for (int i = 0; i < root.getChildrenSize(); i++) {
                Tree child = root.getChild(i);
                eval = createMinimaxAlphaBeta(child, false, level - 1, alpha, beta);

                if (root.getScore() < eval.getScore() || root.getScore() == -999) {
//                    maxEval.setScore(eval.getScore());
                    root.setScore(eval.getScore());
                }
                if (alpha < eval.getScore()) {
                    alpha = eval.getScore();
                }
                if (beta <= alpha) {
                    Log.d("TEST_GAME", "MAX BREAK ");
                    break;
                }
            }
            return root;
        } else {
//            Tree minEval = new Tree(null, null, Integer.MAX_VALUE);
            for (Tree child : root.getChildList()) {
                eval = createMinimaxAlphaBeta(child, true, level - 1, alpha, beta);

                if (root.getScore() > eval.getScore() || root.getScore() == -999) {
//                    minEval.setScore(eval.getScore());
                    root.setScore(eval.getScore());
                }
                if (beta > eval.getScore()) {
                    beta = eval.getScore();
                }
                if (beta <= alpha) {
                    Log.d("TEST_GAME", "MIN BREAK ");
                    break;
                }
            }
            return root;
        }
    }

    private @Nullable
    Move pickMove(Tree optionalMoves) {

        float max = -13;
        int index = 0;
        for (int i = 0; i < optionalMoves.getChildrenSize(); i++) {
            Tree firstLayer = optionalMoves.getChild(i);
            if (firstLayer == null) break;
            float tMax = -13;
            // Find the max leaf
            for (Tree secondLayer : firstLayer.getChildList()) {
                float min = 13;
                for (Tree thirdLayer : secondLayer.getChildList()) {
                    if (thirdLayer.getScore() <= min) {
                        min = thirdLayer.getScore();
                    }
                }
                secondLayer.setScore(min);
                // Find the min on the third level
                if (secondLayer.getScore() >= tMax) {
                    tMax = secondLayer.getScore();
                }
            }
            firstLayer.setScore(tMax);
            // Find the max on the second layer and save the index
            if (firstLayer.getScore() >= max) {
                max = firstLayer.getScore();
                index = i;
            }
        }
        Tree child = optionalMoves.getChild(index);
        return child != null ? child.getMove() : null;
    }

}


//package com.example.chekersgamepro.ai;
//
//        import android.util.Log;
//
//        import com.example.chekersgamepro.data.data_game.DataGame;
//        import com.example.chekersgamepro.data.move.DataMove;
//        import com.example.chekersgamepro.data.move.Move;
//        import com.google.firebase.database.core.utilities.TreeNode;
//
//        import java.util.ArrayList;
//        import java.util.List;
//
//        import javax.annotation.Nullable;
//
//public class AIComputerMove {
//
//    private int LEVEL_LIMIT = DataGame.DIFFICULT_LEVEL;
//
//    public Move getMoveAI(DataGameBoard dataGameBoard, boolean isPlayerOnCurrently) {
//        Log.d("TEST_GAME", "start getMoveAI");
//        Tree mainTree = new Tree(dataGameBoard, null, dataGameBoard.getEvaluateBoard(isPlayerOnCurrently));
//        Tree optionalMoves = createMovesAI(mainTree, isPlayerOnCurrently, 0);
//        Move move = pickMove(optionalMoves);
//
//        Tree minimax = createMinimaxAlphaBeta(optionalMoves, true, LEVEL_LIMIT, Integer.MIN_VALUE, Integer.MAX_VALUE);
//
////        Move bestMove = null;
//        Move bestMove_ = null;
////        Move moveFromPath = getMoveFromPath(minimax).getMove();
//        while (minimax.getParent() != null) {
//            bestMove_ = minimax.getMove();
//            minimax = minimax.getParent();
//        }
////        Log.d("TEST_GAME", "1 end getMoveAI: " + moveFromPath.toString());
////        Log.d("TEST_GAME", "2 end getMoveAI: " + bestMove.toString());
//        Log.d("TEST_GAME", "3 end getMoveAI: " + bestMove_.toString());
//        Log.d("TEST_GAME", "end getMoveAI");
//
//        return bestMove_;
//    }
//
//    Move bestMove = null;
//
//    private Tree getMoveFromPath(Tree minimax) {
//        Tree parent = minimax.getParent();
//        if (parent != null) {
//            bestMove = parent.getMove();
//            getMoveFromPath(parent);
//        }
//
//        return minimax;
//
//    }
//
//    private Tree createMovesAI(Tree root, boolean isPlayerOneCurrently, int level) {
//        if (LEVEL_LIMIT == level) return root;
//
//        List<DataMove> relevantMovesCellsStartFirst = new ArrayList<>(((DataGameBoard) root.getDataGameBoard()).createMovesByCellsStart(isPlayerOneCurrently));
//        for (DataMove dataMove : relevantMovesCellsStartFirst) {
//            DataGameBoard currGameBoard = ((DataGameBoard) root.getDataGameBoard()).getCopyBoard();
//
//            currGameBoard.setMovePawnPath(dataMove, isPlayerOneCurrently);
//            Move move = new Move(dataMove.getMove().getStartPoint(), dataMove.getMove().getEndPoint());
//            float score = -999;
//            //check if is the leafs layer
//            if (LEVEL_LIMIT == (level + 1)) {
//                score = currGameBoard.getEvaluateBoard(isPlayerOneCurrently);
//            }
////            Log.d("TEST_GAME", "LEVEL: " + level + ", ONE COUNT: " + (currGameBoard.getPlayerOneCount() + currGameBoard.getPlayerOneKingsCount()) +
////                     " , TWO COUNT: " + (currGameBoard.getPlayerTwoCount() + currGameBoard.getPlayerTwoKingsCount()) +
////                    " , SCORE: " + score + ", FROM: " + move.getStartPoint() +", TO: " + move.getEndPoint());
//            Tree currentLayer = new Tree(currGameBoard, move, score);
//
//            root.addChild(currentLayer);
//            createMovesAI(currentLayer, !isPlayerOneCurrently, level + 1);
//        }
//
//        return root;
//    }
//
//    private Tree createMinimaxAlphaBeta(Tree root, boolean isPlayerOneCurrently, int level, double alpha, double beta) {
//        if (level == 0) return root;
//
//        Tree eval;
//        if (isPlayerOneCurrently) {
//            Tree maxEval = new Tree(null, null, Integer.MIN_VALUE);
//            for (int i = 0; i < root.getChildrenSize(); i++) {
//                Tree child = root.getChild(i);
//                eval = createMinimaxAlphaBeta(child, false, level - 1, alpha, beta);
//
//                if (maxEval.getScore() < eval.getScore()) {
//                    maxEval = eval;
//                    root.setScore(maxEval.getScore());
//                }
//                if (alpha < eval.getScore()) {
//                    alpha = eval.getScore();
//                }
//                if (beta <= alpha) {
//                    break;
//                }
//            }
//            return maxEval;
//        } else {
//            Tree minEval = new Tree(null, null, Integer.MAX_VALUE);
//            for (Tree child : root.getChildList()) {
//                eval = createMinimaxAlphaBeta(child, true, level - 1, alpha, beta);
//
//                if (minEval.getScore() > eval.getScore()) {
//                    minEval = eval;
//                    root.setScore(minEval.getScore());
//                }
//                if (beta > eval.getScore()) {
//                    beta = eval.getScore();
//                }
//                if (beta <= alpha) {
//                    break;
//                }
//            }
//            return minEval;
//        }
//    }
//
//    private @Nullable
//    Move pickMove(Tree optionalMoves) {
//
//        float max = -13;
//        int index = 0;
//        for (int i = 0; i < optionalMoves.getChildrenSize(); i++) {
//            Tree firstLayer = optionalMoves.getChild(i);
//            if (firstLayer == null) break;
//            float tMax = -13;
//            // Find the max leaf
//            for (Tree secondLayer : firstLayer.getChildList()) {
//                float min = 13;
//                for (Tree thirdLayer : secondLayer.getChildList()) {
//                    if (thirdLayer.getScore() <= min) {
//                        min = thirdLayer.getScore();
//                    }
//                }
//                secondLayer.setScore(min);
//                // Find the min on the third level
//                if (secondLayer.getScore() >= tMax) {
//                    tMax = secondLayer.getScore();
//                }
//            }
//            firstLayer.setScore(tMax);
//            // Find the max on the second layer and save the index
//            if (firstLayer.getScore() >= max) {
//                max = firstLayer.getScore();
//                index = i;
//            }
//        }
//        Tree child = optionalMoves.getChild(index);
//        return child != null ? child.getMove() : null;
//    }
//
//}
//
//
