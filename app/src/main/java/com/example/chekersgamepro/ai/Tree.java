package com.example.chekersgamepro.ai;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.chekersgamepro.data.move.Move;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

/**
 * Creates a new tree with dataGameBoard as the head, a move, a score, and children
 * dataGameBoard the head of the tree
 * move the move associated with the tree
 * score the score of the tree
 * children the children of the dataGameBoard
 */
public class Tree {

    private Object dataGameBoard;
    private Move move;
    private float score;
    private ArrayList<Tree> children;
    private Tree parent;


    public Tree(Object dataGameBoard, Move move, float score) {
        this.dataGameBoard = dataGameBoard;
        this.children = new ArrayList<>();
        this.score = score;
        this.move = move;
    }

    /**
     * @return the board of the tree
     */
    public Object getDataGameBoard() {
        return dataGameBoard;
    }

    /**
     * @return the move of the tree
     */
    public Move getMove() {
        return move;
    }

    /**
     * @return the score of the tree
     */
    public float getScore() {
        return score;
    }

    /**
     * @return the tree's children
     */
    public List<Tree> getChildList() {
        return children;
    }

    /**
     * @return the number of children the tree has
     */
    public int getChildrenSize() {
        return children.size();
    }

    /**
     * Changes the tree's score
     *
     * @param newVal the new score of the tree
     */
    public void setScore(float newVal) {
        score = newVal;
    }

    /**
     * The child at the given index
     *
     * @param index the chosen index
     * @return the child at the index
     */
    public @Nullable
    Tree getChild(int index) {
        return children.size() > 0 ? children.get(index) : null;
    }

    /**
     * Adds a child to the tree
     *
     * @param child the tree that will be added to the children
     */
    public void addChild(Tree child) {
        child.setParent(this);
        children.add(child);
    }

    public Tree setParent(Tree parent) {
        this.parent = parent;
        return this;
    }

    public Tree getParent() {
        return parent;
    }

    /**
     * Adds multiple children to the tree
     *
     * @param children the trees that will be added to the children
     */
    public void addChildren(Tree... children) {
        for (Tree child : children) {
            addChild(child);
        }
    }

    private String speicalTab = "|____";
    private String speicalTabSpace = "|    ";
    private String tab = "     ";
    private boolean firstTime = true;

    public String print(Tree root, int level) {
        StringBuilder br = new StringBuilder();
        if (firstTime){
            br.append("\n");
            firstTime = false;
        }
        for (int i = 0; i < level; i++) {
            if(i ==0 ){
                if (level == 1){
                    br.append(speicalTab);
                } else {
                    br.append(speicalTabSpace);
                }
            }else if (i + 1 == level){
                br.append(speicalTab);
            } else {
                br.append(speicalTabSpace);
            }
        }
        String dataRoot = root.toString();
        br
                .append(dataRoot)
                .append("\n");

//        Log.d("TEST_GAME", "dataRoot: " + br.toString() + dataRoot);
        for (Tree child : root.getChildList()) {
            br.append(print(child, level + 1));
        }
        return br.toString();
    }

    public void print() {
        firstTime = true;
        String print = print(this, 0);
        Log.d("TEST_GAME", "dataRoot: " + print);

    }

    @NonNull
    @Override
    public String toString() {

        return "score: " + score;
    }
}

