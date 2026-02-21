package com.github.kshulzh.problemgraph.it

import com.github.kshulzh.problemgraph.action.NodeActionImpl
import com.github.kshulzh.problemgraph.context.ActionExecutor
import com.github.kshulzh.problemgraph.context.ProblemContext
import com.github.kshulzh.problemgraph.v1.get
import com.github.kshulzh.problemgraph.v1.getValue
import com.github.kshulzh.problemgraph.v1.optional
import kotlin.test.Test
import kotlin.test.assertEquals

class ThreeNodeTests {
    @Test
    fun `three nodes`() {
        val action = ActionExecutor()
        val problemContext = ProblemContext(action, action)
        var counter = 0
        val node1 = NodeActionImpl(problemContext) {
            counter++
            return@NodeActionImpl 1
        }

        val node2 = NodeActionImpl(problemContext) {
            val node1res by get(node1)
            return@NodeActionImpl 1 + node1res
        }

        val node3 = NodeActionImpl(problemContext) {
            val node1res by get(node1)
            return@NodeActionImpl 1 + node1res
        }
        action.submit(node2)
        action.submit(node3)
        action.resolve()

        assertEquals(node2.result, node3.result)
        assertEquals(1, counter)
    }

    @Test
    fun `2 linked nodes and one hidden dependency`() {
        val action = ActionExecutor()
        val problemContext = ProblemContext(action, action)
        val list = mutableListOf< String>()
        val node1 = NodeActionImpl(problemContext) {
            list.add("123")
            return@NodeActionImpl 1
        }

        val node2 = NodeActionImpl(problemContext) {
            if (list.isEmpty()) throw Exception()
            return@NodeActionImpl 1
        }

        val node3 = NodeActionImpl(problemContext) {
            val node1res by get(node2)
            return@NodeActionImpl 1 + node1res
        }
        action.submit(node3)
        action.submit(node1)
        action.resolve()

        assertEquals(2, node3.result)
    }

    @Test
    fun `3 independent nodes`() {
        val action = ActionExecutor()
        val problemContext = ProblemContext(action, action)
        val set = mutableSetOf< String>()
        val node1 = NodeActionImpl(problemContext) {
            set.add("123")
            return@NodeActionImpl 1
        }

        val node2 = NodeActionImpl(problemContext) {
            if (set.isEmpty()) throw Exception()
            set.add("456")
            return@NodeActionImpl 1
        }

        val node3 = NodeActionImpl(problemContext) {
            if (set.size != 2) throw Exception()
            return@NodeActionImpl 1
        }
        action.submit(node3)
        action.submit(node2)
        action.submit(node1)
        action.resolve()

        assertEquals(1, node3.result)
    }

    @Test
    fun `1 node with list`() {
        val action = ActionExecutor()
        val problemContext = ProblemContext(action, action)
        val node1 = NodeActionImpl(problemContext) {
            return@NodeActionImpl 1
        }

        val node2 = NodeActionImpl(problemContext) {
            return@NodeActionImpl 2
        }

        val node3 = NodeActionImpl(problemContext) {
            val ll by get(listOf(node1,node2))
            return@NodeActionImpl ll.sum()
        }
        action.submit(node3)
        action.resolve()

        assertEquals(3, node3.result)
    }

    @Test
    fun `1 node with list with retry`() {
        val action = ActionExecutor()
        val problemContext = ProblemContext(action, action)
        val list = mutableListOf<Int>()
        val node1 = NodeActionImpl(problemContext) {
            if (list.isEmpty()) throw Exception()
            return@NodeActionImpl 1
        }

        val node2 = NodeActionImpl(problemContext) {
            list.add(2)
            return@NodeActionImpl 2
        }

        val node3 = NodeActionImpl(problemContext) {
            val ll by get(listOf(node1,node2))
            return@NodeActionImpl ll.sum()
        }
        action.submit(node3)
        action.resolve()

        assertEquals(3, node3.result)
    }

    @Test
    fun `1 node with sun node and optional with retry`() {
        val action = ActionExecutor()
        val problemContext = ProblemContext(action, action)
        val list = mutableListOf<Int>()

        val node2 = NodeActionImpl(problemContext) {
            return@NodeActionImpl 2
        }

        val node3 = NodeActionImpl(problemContext) {
            val ll by get(node2)
            optional {
                list.add(2)
            }
            return@NodeActionImpl ll
        }
        action.submit(node3)
        action.resolve()
        node3.exec()

        assertEquals(1, list.size)
    }
}