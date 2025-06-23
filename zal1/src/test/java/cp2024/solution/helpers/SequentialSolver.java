package cp2024.solution.helpers;

import cp2024.circuit.*;

public class SequentialSolver{

    public static boolean recursiveSolve(CircuitNode n) throws InterruptedException {
        if (n.getType() == NodeType.LEAF)
            return ((LeafNode) n).getValue();

        CircuitNode[] args = n.getArgs();

        return switch (n.getType()) {
            case IF -> solveIF(args);
            case AND -> solveAND(args);
            case OR -> solveOR(args);
            case GT -> solveGT(args, ((ThresholdNode) n).getThreshold());
            case LT -> solveLT(args, ((ThresholdNode) n).getThreshold());
            case NOT -> solveNOT(args);
            default -> throw new RuntimeException("Illegal type " + n.getType());
        };
    }

    private static boolean solveNOT(CircuitNode[] args) throws InterruptedException {
        return !recursiveSolve(args[0]);
    }

    private static boolean solveLT(CircuitNode[] args, int threshold) throws InterruptedException{
        int gotTrue = 0;
        for (CircuitNode arg : args) {
            if (recursiveSolve(arg))
                gotTrue++;
        }
        return gotTrue < threshold;
    }

    private static boolean solveGT(CircuitNode[] args, int threshold) throws InterruptedException{
        int gotTrue = 0;
        for (CircuitNode arg : args) {
            if (recursiveSolve(arg))
                gotTrue++;
        }
        return gotTrue > threshold;
    }

    private static boolean solveOR(CircuitNode[] args)  throws InterruptedException{
        for (CircuitNode c : args) {
            if (recursiveSolve(c))
                return true;
        }
        return false;
    }

    private static boolean solveAND(CircuitNode[] args) throws InterruptedException{
        for (CircuitNode c : args) {
            if (!recursiveSolve(c))
                return false;
        }
        return true;
    }

    private static boolean solveIF(CircuitNode[] args)  throws InterruptedException{
        boolean b = recursiveSolve(args[0]);
        return b ? recursiveSolve(args[1]) : recursiveSolve(args[2]);
    }
}
