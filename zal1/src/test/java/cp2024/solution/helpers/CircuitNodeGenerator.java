package cp2024.solution.helpers;

import cp2024.circuit.CircuitNode;
import cp2024.circuit.NodeType;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Random;

import static cp2024.circuit.NodeType.GT;
import static cp2024.circuit.NodeType.LT;
import static cp2024.circuit.NodeType.NOT;

public class CircuitNodeGenerator {
    private final Random random = new Random();

    private final double SLEEPY_LEAF_PROBABILITY = 0.5;
    private final int MAX_SLEEP_DURATION = 5000;
    private final int MAX_ARGS_COUNT = 5;

    // Generates a CircuitNode tree with `k` leaf nodes, optionally including sleepy
    // nodes.
    public CircuitNode generate(int k, boolean includeSleepy) {
        if (k <= 0) {
            throw new IllegalArgumentException("Number of nodes (k) must be greater than 0.");
        }

        ArrayList<CircuitNode> nodes = new ArrayList<>(k);

        final double sleepyProp = includeSleepy ? SLEEPY_LEAF_PROBABILITY : 0;
        for (int i = 0; i < k; i++) {
            nodes.add(generateLeaf(sleepyProp));
        }

        while (nodes.size() > 1) {
            nodes.add(generateNode(nodes));
        }

        return nodes.get(0);
    }

    // Generates an array of 'length' CircuitNodes with set values based on the
    // binary representation of 'index'.
    // Least significant bit of 'index' corresponds to the first node in the array.
    public static CircuitNode[] generateCombinations(int index, int length) throws InterruptedException {
        assert (length >= 0);

        CircuitNode[] nodes = new CircuitNode[length];
        for (int i = length - 1; i >= 0; i--) {
            boolean nodeValue = (index & 1) == 1;
            nodes[i] = CircuitNode.mk(nodeValue);

            index >>= 1;
        }

        return nodes;
    }

    private CircuitNode generateLeaf(double sleepyProp) {
        if (random.nextDouble() < sleepyProp) {
            return CircuitNode.mk(random.nextBoolean(), Duration.ofMillis(random.nextInt(MAX_SLEEP_DURATION)));
        } else {
            return CircuitNode.mk(random.nextBoolean());
        }
    }

    private CircuitNode generateNode(ArrayList<CircuitNode> nodes) {
        int argsCount = Math.min(MAX_ARGS_COUNT, random.nextInt(nodes.size()) + 1);
        CircuitNode[] args = new CircuitNode[argsCount];
        for (int i = 0; i < argsCount; i++) {
            args[i] = nodes.remove(random.nextInt(nodes.size()));
        }

        NodeType type = getRandomNodeType(argsCount);

        if (type == GT || type == LT) {
            final int threshold = random.nextInt(argsCount + 2);
            return CircuitNode.mk(type, threshold, args);
        } else if (type == NOT) {
            return CircuitNode.mk(type, args[0]);
        } else {
            return CircuitNode.mk(type, args);
        }
    }

    private final NodeType[] NODE_TYPES = { NodeType.AND, NodeType.OR, NodeType.GT, NodeType.LT, NodeType.NOT };

    private NodeType getRandomNodeType(int argsCount) {
        final int possibleTypesEnd = argsCount != 1 ? NODE_TYPES.length - 1 : NODE_TYPES.length;
        final int possibleTypesStart = argsCount == 1 ? 2 : 0;
        return NODE_TYPES[random.nextInt(possibleTypesStart, possibleTypesEnd)];
    }
}
