package com.github.kshulzh.problemgraph.it

import com.github.kshulzh.problemgraph.action.NodeActionImpl
import com.github.kshulzh.problemgraph.action.NodeActionMap
import com.github.kshulzh.problemgraph.context.ActionExecutor
import com.github.kshulzh.problemgraph.context.ProblemContext
import com.github.kshulzh.problemgraph.v1.optional
import kotlin.test.Test
import kotlin.test.assertEquals

class SingleNodeTests {
    @Test
    fun `one node map success`() {
        val action = ActionExecutor()
        val problemContext = ProblemContext(action, action)
        val node = NodeActionMap(problemContext) {
            return@NodeActionMap 0
        }

        action.submit(node)
        action.resolve()
        assertEquals(0, node.result)
    }

    @Test
    fun `one node success`() {
        val action = ActionExecutor()
        val problemContext = ProblemContext(action, action)
        val node = NodeActionImpl(problemContext) {
            return@NodeActionImpl 1
        }

        action.submit(node)
        action.resolve()
        assertEquals(1, node.result)
    }

    @Test
    fun `one node fail`() {
        val action = ActionExecutor()
        val problemContext = ProblemContext(action, action)
        val node = NodeActionImpl<Int>(problemContext) {
            throw Exception()
        }

        action.submit(node)
        val res = action.resolve()
        assertEquals(1, res.queue.size)
    }

    @Test
    fun `one node map fail`() {
        val action = ActionExecutor()
        val problemContext = ProblemContext(action, action)
        val node = NodeActionImpl<Int>(problemContext) {
            throw Exception()
        }

        action.submit(node)
        val res = action.resolve()
        assertEquals(1, res.queue.size)
    }

    @Test
    fun `one node with optional`() {
        val action = ActionExecutor()
        val problemContext = ProblemContext(action, action)
        var mutableInt = 1
        val node = NodeActionImpl(problemContext) {
            this.optional {
                mutableInt +=1
            }
            return@NodeActionImpl 1
        }

        action.submit(node)
        node.exec()
        action.resolve()
        node.exec()
        assertEquals(2, mutableInt)
    }
}