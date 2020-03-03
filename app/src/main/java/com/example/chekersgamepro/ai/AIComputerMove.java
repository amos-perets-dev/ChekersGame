package com.example.chekersgamepro.ai;

import android.util.Log;

import com.example.chekersgamepro.data.data_game.DataGame;
import com.example.chekersgamepro.data.move.DataMove;
import com.example.chekersgamepro.data.move.Move;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

public class AIComputerMove {

    private int LEVEL_LIMIT = DataGame.DIFFICULT_LEVEL;

    public Move getMoveAI(DataGameBoard dataGameBoard, boolean isPlayerOnCurrently) {
        Tree mainTree = new Tree(dataGameBoard, null, dataGameBoard.getEvaluateBoard(isPlayerOnCurrently));
        Tree optionalMoves = createMovesAI(mainTree, isPlayerOnCurrently, 0);
        return pickMove(optionalMoves);
    }

//    public Move getMoveAI(DataGameBoard dataGameBoard, boolean isPlayerOnCurrently) {
//        Tree mainTree = new Tree(dataGameBoard, null, dataGameBoard.getEvaluateBoard(isPlayerOnCurrently));
//        Tree optionalMoves = createMovesAI(mainTree, isPlayerOnCurrently, 0);
//        calculateScore(optionalMoves, true, 0);
//
//        List<Tree> childList = optionalMoves.getChildList();
//        float max = Integer.MIN_VALUE;
//        Move bestMove = null;
//        for (Tree tree : childList){
//            float score = tree.getScore();
//            if (score > max){
//                max = score;
//                bestMove = new Move(tree.getMove().getStartPoint(), tree.getMove().getEndPoint());
//            }
//        }
//        return bestMove;
//    }

    private Tree createMovesAI(Tree root, boolean isPlayerOneCurrently, int level) {
        if (LEVEL_LIMIT == level)return root;

        List<DataMove> relevantMovesCellsStartFirst = new ArrayList<>(((DataGameBoard)root.getDataGameBoard()).createMovesByCellsStart(isPlayerOneCurrently));
        for (DataMove dataMove : relevantMovesCellsStartFirst) {
            DataGameBoard currGameBoard = ((DataGameBoard)root.getDataGameBoard()).getCopyBoard();

            currGameBoard.setMovePawnPath(dataMove, isPlayerOneCurrently);
            Move move = new Move(dataMove.getMove().getStartPoint(), dataMove.getMove().getEndPoint());
            float score = -999;
            //check if is the leafs layer
            if (LEVEL_LIMIT == (level + 1)){
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

    public void calculateScore(Tree node, boolean isFirstChild, int level) {
        if (node == null)
            return;

        List<Tree> childList = node.getChildList();
        int childIndex = 0;
        boolean firstChild = true;
        boolean isPlayer = (level % 2 == 0);
        float min = Integer.MAX_VALUE;
        float max = Integer.MIN_VALUE;
        for (Tree child : childList) {
            calculateScore(child, firstChild, level+1);
            firstChild = false;
            childIndex ++;
        }
        // now deal with the node
        float childScore = node.getScore();
        if (isPlayer) {
            if (childScore < min) {
                min = childScore;
            }
        } else {
            if (childScore > max) {
                max = childScore;
            }
        }

        if (node.getParent() != null) {
            float parentScore = node.getParent().getScore();
            if (isPlayer) {
                if (isFirstChild) {
                    parentScore = Integer.MAX_VALUE;
                }
                node.getParent().setScore(parentScore < min ? parentScore : min);

            } else {
                if (isFirstChild) {
                    parentScore = Integer.MIN_VALUE;
                }
                node.getParent().setScore(parentScore > max ? parentScore : max);
            }
        }


    }

    private @Nullable Move pickMove(Tree optionalMoves) {

        float max = -13;
        int index = 0;
        for (int i = 0; i < optionalMoves.getChildrenSize(); i++) {
            Tree firstLayer = optionalMoves.getChild(i);
            if (firstLayer == null)break;
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


