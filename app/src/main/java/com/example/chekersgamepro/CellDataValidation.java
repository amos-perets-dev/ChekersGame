package com.example.chekersgamepro;

public class CellDataValidation {

    private boolean isNode;
    private boolean isLeaf;
    private boolean isRight;
    private boolean isLeft;

    public CellDataValidation(boolean isNode, boolean isLeaf, boolean isRight, boolean isLeft) {
        this.isNode = isNode;
        this.isLeaf = isLeaf;
        this.isRight = isRight;
        this.isLeft = isLeft;
    }

    public CellDataValidation() { }

    public boolean isNode() {
        return isLeft && isRight;
    }

    public CellDataValidation setNode(boolean node) {
        isNode = node;
        return this;
    }

    public boolean isLeaf() {
        return isLeaf;
    }

    public CellDataValidation setLeaf(boolean leaf) {
        isLeaf = leaf;
        return this;
    }

    public boolean isRight() {
        return isRight;
    }

    public CellDataValidation setRight(boolean right) {
        isRight = right;
        return this;
    }

    public boolean isLeft() {
        return isLeft;
    }

    public CellDataValidation setLeft(boolean left) {
        isLeft = left;
        return this;
    }
}
