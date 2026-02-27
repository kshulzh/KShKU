package io.github.kshulzh.problemgraph.it

import io.github.kshulzh.problemgraph.action.NodeActionImpl
import io.github.kshulzh.problemgraph.action.NodeActionMap
import io.github.kshulzh.problemgraph.context.ActionExecutor
import io.github.kshulzh.problemgraph.context.ProblemContext
import io.github.kshulzh.problemgraph.v1.tryGet
import io.github.kshulzh.problemgraph.v1.getValue
import kotlin.test.Test
import kotlin.test.assertEquals

class TwoNodeTests {
    @Test
    fun `test two map nodes`() {
        val action = ActionExecutor()
        val problemContext = ProblemContext(action, action)
        val node1 = NodeActionMap(problemContext) {
            return@NodeActionMap 0
        }

        val node2 = NodeActionMap(problemContext) {
            val node1res by tryGet {node1}
            return@NodeActionMap 1 + node1res
        }

        action.submit(node2)
        action.resolve()
        assertEquals(1, node2.result)
    }

    @Test
    fun `test two nodes`() {
        val action = ActionExecutor()
        val problemContext = ProblemContext(action, action)
        val node1 = NodeActionImpl(problemContext) {
            return@NodeActionImpl 0
        }

        val node2 = NodeActionImpl(problemContext) {
            val node1res by tryGet(node1)
            return@NodeActionImpl 1 + node1res
        }

        action.submit(node2)
        action.resolve()
        node2.exec()
        assertEquals(1, node2.result)
    }

    @Test
    fun `test two nodes with error`() {
        val action = ActionExecutor()
        val problemContext = ProblemContext(action, action)
        val node1 = NodeActionImpl<Int>(problemContext) {
            throw Exception()
        }

        val node2 = NodeActionImpl(problemContext) {
            val node1res by tryGet(node1)
            return@NodeActionImpl 1 + node1res
        }

        action.submit(node2)
        val res = action.resolve()
        assertEquals(1, res.queue.size)
    }

    @Test
    fun `test two nodes with first error`() {
        val action = ActionExecutor()
        val problemContext = ProblemContext(action, action)
        val node1 = NodeActionImpl(problemContext) {
            return@NodeActionImpl 1
        }

        val node2 = NodeActionImpl(problemContext) {
            val node1res by tryGet(node1)
            if (node1res == 1) throw Exception()
            return@NodeActionImpl 1 + node1res
        }

        action.submit(node2)
        val res = action.resolve()
        assertEquals(1, res.queue.size)
    }
}