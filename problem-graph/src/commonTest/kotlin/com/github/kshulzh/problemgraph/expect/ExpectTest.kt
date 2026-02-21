package com.github.kshulzh.problemgraph.expect

import com.github.kshulzh.problemgraph.action.NodeActionImpl
import com.github.kshulzh.problemgraph.context.ActionExecutor
import com.github.kshulzh.problemgraph.context.ProblemContext
import com.github.kshulzh.problemgraph.v1.get
import com.github.kshulzh.problemgraph.v1.getValue
import kotlin.test.Test
import kotlin.test.assertEquals

class ExpectTest {
    @Test
    fun `2 nodes expects valid result from one`() {
        val action = ActionExecutor()
        val problemContext = ProblemContext(action, action)
        val node1 = NodeActionImpl(problemContext) {
            var res = 1
            expects.values.forEach {
                if (it is DividedExpect && res%it.num !=0) res *= it.num
            }
            return@NodeActionImpl res
        }

        val node2 = NodeActionImpl(problemContext) {
            val node1res by get(node1,setOf(DividedExpect(2)))

            return@NodeActionImpl node1res
        }

        val node3 = NodeActionImpl(problemContext) {
            val node1res by get(node1,setOf(DividedExpect(3)))

            return@NodeActionImpl node1res
        }
        action.submit(node3)
        action.submit(node2)
        action.resolve()

        assertEquals(6, node3.result)
    }

    class DividedExpect(
        val num: Int,
    ) : Expect<Int> {
        override fun expected(value: Int): Throwable? = if (value%num == 0) null else IllegalArgumentException(
            "Expected $num to divide $value"
        )
    }
}