package com.example.chekersgamepro;

import java.util.ArrayList;
import java.util.List;

public class MinMaxNode {

    private static final int MAX_VALUE = Integer.MAX_VALUE;
    private static final int MIN_VALUE = Integer.MIN_VALUE;

    private Object value;
    private int score;
    private List<MinMaxNode> childList;
    private MinMaxNode parent;

    public MinMaxNode() {
        this(null, 0);
    }

    public MinMaxNode(Object value, int score) {
        super();
        this.value = value;
        this.score = score;
        this.childList = new ArrayList<>();
    }

    public void addChild(MinMaxNode child) {
        child.setParent(this);
        childList.add(child);
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public List<MinMaxNode> getChildList() {
        return childList;
    }

    public MinMaxNode getParent() {
        return parent;
    }

    public void setParent(MinMaxNode parent) {
        this.parent = parent;
    }

    public boolean hasChildren() {
        return ((this.childList == null) || (this.childList.isEmpty()));
    }

    @Override
    public String toString() {
        return value + " (" + score + ")";
    }
    public static void calcScore(MinMaxNode node, boolean isFirstChild, int level) {
        if (node == null)
            return;

        List<MinMaxNode> childList = node.getChildList();
        int childIndex = 0;
        boolean firstChild = true;
        boolean isPlayer = (level % 2 == 0);
        int min = MAX_VALUE;
        int max = MIN_VALUE;
        for (MinMaxNode child : childList) {
            calcScore(child, firstChild, level+1);
            firstChild = false;
            childIndex ++;
        }
// now deal with the node
        System.out.println(level + " - " + (isPlayer ? "Player:\t" : "Computer:\t") + node.toString());
        int childScore = node.getScore();
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
            int parentScore = node.getParent().getScore();
            if (isPlayer) {
                if (isFirstChild) {
                    parentScore = MAX_VALUE;
                }
                node.getParent().setScore(parentScore < min ? parentScore : min);

            } else {
                if (isFirstChild) {
                    parentScore = MIN_VALUE;
                }
                node.getParent().setScore(parentScore > max ? parentScore : max);
            }
        }


    }


    // ------------------ Test ----------------------------------------
// ------------------ Test ----------------------------------------
// ------------------ Test ----------------------------------------
// ------------------ Test ----------------------------------------
// ------------------ Test ----------------------------------------
// ------------------ Test ----------------------------------------
// ------------------ Test ----------------------------------------
// ------------------ Test ----------------------------------------
// ------------------ Test ----------------------------------------
    public static MinMaxNode buildDemoTree() {
        MinMaxNode root = new MinMaxNode("Root", 0);
        for (int i = 1; i <= 4; i++) {
            MinMaxNode child = new MinMaxNode(String.format("Child_%d", i), i);
            root.addChild(child);
            for (int j = 1; j <= 3; j++) {
                MinMaxNode child2 = new MinMaxNode(String.format("Child_%d_%d", i, j), i + j);
                child.addChild(child2);

                for (int k = 1; k <= 3; k++) {
                    MinMaxNode child3 = new MinMaxNode(String.format("Child_%d_%d_%d", i, j, k), i + j + k);
                    child2.addChild(child3);
                }
            }
        }
        return root;
    }

    public static MinMaxNode buildDemoTree2() {
        int x = 0;
        int[] score = {20, 100, -10, 9, -100, -100, -8, 8, 8, 100, 6, 100, -100, -100, -10, -5};
        MinMaxNode root = new MinMaxNode("Root", 0);
        for (int i = 1; i <= 2; i++) {
            MinMaxNode child = new MinMaxNode(String.format("Child_%d", i), 0);
            root.addChild(child);
            for (int j = 1; j <= 2; j++) {
                MinMaxNode child2 = new MinMaxNode(String.format("Child_%d_%d", i, j), 0);
                child.addChild(child2);

                for (int k = 1; k <= 2; k++) {
                    MinMaxNode child3 = new MinMaxNode(String.format("Child_%d_%d_%d", i, j, k), 0);
                    child2.addChild(child3);

                    for (int m = 1; m <= 2; m++) {
                        MinMaxNode child4 = new MinMaxNode(String.format("Child_%d_%d_%d_%d", i, j, k, m), score[x]);
                        child3.addChild(child4);
                        x++;
                    }
                }
            }
        }
        return root;
    }

    public static MinMaxNode buildDemoTree3() {
        int x = 0;
        int[] score = {2, 3, 4, 1, 5, 6, 2, 10};
        MinMaxNode root = new MinMaxNode("Root", 0);
        for (int i = 1; i <= 4; i++) {
            MinMaxNode child = new MinMaxNode(String.format("Child_%d", i), 0);
            root.addChild(child);
            for (int j = 1; j <= 2; j++) {
                MinMaxNode child2 = new MinMaxNode(String.format("Child_%d_%d", i, j), score[x]);
                child.addChild(child2);
                x ++;
            }
        }
        return root;
    }

    public static void printTree(MinMaxNode root, int level) {
        String spaceBefore = (level > 0 ? String.format("%" + level + "s", "\t").replace(' ', '\t') : "");
        String s = spaceBefore + root.toString();
        System.out.println(s);
        for (MinMaxNode child : root.getChildList()) {
            printTree(child, level + 1);
        }
    }

    public static void printTree() {
        MinMaxNode root = buildDemoTree();
        printTree(root, 0);
        System.out.println("--------------------------------------------");
        System.out.println("--------------------printPostOrder------------------------");
        TreePrinter.printPostOrder(root);

        System.out.println("--------------------------------------------");
        System.out.println("--------------------printInOrder------------------------");
        TreePrinter.printInOrder(root);

        System.out.println("--------------------------------------------");
        System.out.println("--------------------printPreOrder------------------------");
        TreePrinter.printPreOrder(root);
    }

    public static void main(String[] args) {
// MinMaxTree root = buildDemoTree2();
        MinMaxNode root = buildDemoTree3();
        printTree(root, 0);

        System.out.println("--------------------------------------------");
        calcScore(root, true, 0);
// System.out.println("--------------------------------------------");
// TreePrinter.printPostOrder(root);
    }

    // ==================================================================
// Inner Class
// ==================================================================
    public static class TreePrinter {
        /* Given a binary tree, print its nodes according to the "bottom-up" postorder traversal. */
        public static void printPostOrder(MinMaxNode node) {
            if (node == null)
                return;

            List<MinMaxNode> childList = node.getChildList();
            for (MinMaxNode child : childList) {
                printPostOrder(child);
            }

// now deal with the node
            System.out.println(node.toString());
        }

        /* Given a binary tree, print its nodes in inorder */
        public static void printInOrder(MinMaxNode node) {
            if (node == null)
                return;

            List<MinMaxNode> childList = node.getChildList();
            for (MinMaxNode child : childList) {
// now deal with the node
                System.out.println(node.toString());
                printInOrder(child);
                /* then print the data of node */
                System.out.println(node.toString());
            }

        }

        /* Given a binary tree, print its nodes in preorder */
        public static void printPreOrder(MinMaxNode node) {
            if (node == null)

                return;
            /* first print data of node */
            System.out.println(node.toString());
            List<MinMaxNode> childList = node.getChildList();
            for (MinMaxNode child : childList) {
                printPreOrder(child);
            }
        }
    }

}
