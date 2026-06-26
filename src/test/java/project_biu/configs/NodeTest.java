package project_biu.configs;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NodeTest {

    @Test
    void acyclicChainHasNoCycle() {
        final Node a = new Node("A");
        final Node b = new Node("B");
        a.addOutEdge(b);

        assertFalse(a.hasCycles());
        assertFalse(b.hasCycles());
    }

    @Test
    void hasCyclesTest() {
        final Node a = new Node("A");
        final Node b = new Node("B");
        a.addOutEdge(b);
        b.addOutEdge(a);

        assertTrue(a.hasCycles());
        assertTrue(b.hasCycles());
    }

    @Test
    void selfCycle() {
        final Node a = new Node("A");
        a.addOutEdge(a);

        assertTrue(a.hasCycles());
    }

    @Test
    void longerLoopTest() {
        final Node a = new Node("A");
        final Node b = new Node("B");
        final Node c = new Node("C");
        a.addOutEdge(b);
        b.addOutEdge(c);
        c.addOutEdge(a);

        assertTrue(a.hasCycles());
        assertTrue(b.hasCycles());
        assertTrue(c.hasCycles());
    }
}
