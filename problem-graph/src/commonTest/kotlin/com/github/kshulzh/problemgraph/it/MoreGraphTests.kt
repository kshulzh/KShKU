package com.github.kshulzh.problemgraph.it

import com.github.kshulzh.problemgraph.action.Action
import com.github.kshulzh.problemgraph.action.NodeActionImpl
import com.github.kshulzh.problemgraph.action.NodeActionMap
import com.github.kshulzh.problemgraph.action.finished
import com.github.kshulzh.problemgraph.context.ActionExecutor
import com.github.kshulzh.problemgraph.context.ProblemContext
import com.github.kshulzh.problemgraph.v1.get
import com.github.kshulzh.problemgraph.v1.getValue
import com.github.kshulzh.problemgraph.v1.optional
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MoreGraphTests {
    @Test
    fun mixed_map_and_impl_dependencies() {
        val dispatcher = ActionExecutor()
        val ctx = ProblemContext(dispatcher, dispatcher)
        var mapExec = 0
        val producerMap = NodeActionMap(ctx) {
            mapExec++
            return@NodeActionMap 5
        }
        val midImpl = NodeActionImpl(ctx) {
            val a by get(producerMap)
            return@NodeActionImpl a * 2
        }
        val consumerImpl = NodeActionImpl(ctx) {
            val b by get(midImpl)
            return@NodeActionImpl b + 1
        }
        dispatcher.submit(consumerImpl)
        dispatcher.resolve()
        assertEquals(11, consumerImpl.result)
        assertEquals(1, mapExec, "Producer map should execute exactly once")
    }

    @Test
    fun duplicate_submission_should_not_double_execute() {
        val dispatcher = ActionExecutor()
        val ctx = ProblemContext(dispatcher, dispatcher)
        var execCount = 0
        val node = NodeActionImpl(ctx) {
            execCount++
            return@NodeActionImpl 42
        }
        dispatcher.submit(node)
        dispatcher.submit(node) // duplicate
        dispatcher.resolve()
        assertEquals(42, node.result)
        assertEquals(1, execCount)
    }

    @Test
    fun empty_and_singleton_list_dependencies() {
        val dispatcher = ActionExecutor()
        val ctx = ProblemContext(dispatcher, dispatcher)

        val emptyConsumer = NodeActionImpl(ctx) {
            val lst by get(emptyList<Action<Int>>())
            // get(emptyList()) should yield empty list; just check size
            val sized = lst.size
            return@NodeActionImpl sized
        }

        val singleProducer = NodeActionImpl(ctx) { 7 }
        val singleConsumer = NodeActionImpl(ctx) {
            val lst by get(listOf(singleProducer))
            return@NodeActionImpl lst.sum()
        }

        dispatcher.submit(singleConsumer)
        dispatcher.submit(emptyConsumer)
        dispatcher.resolve()
        assertEquals(0, emptyConsumer.result)
        assertEquals(7, singleConsumer.result)
    }

    @Test
    fun chain_failure_then_recovery_over_multiple_resolves() {
        val dispatcher = ActionExecutor()
        val ctx = ProblemContext(dispatcher, dispatcher)
        var switch = false

        val a = NodeActionImpl(ctx) {
            if (!switch) throw IllegalStateException("not ready")
            1
        }
        val b = NodeActionImpl(ctx) {
            val ar by get(a)
            ar + 1
        }
        val c = NodeActionImpl(ctx) {
            val br by get(b)
            br + 1
        }

        dispatcher.submit(c)
        val r1 = dispatcher.resolve()
        // nothing finished yet for c due to failure at a; queue should contain c
        assertTrue(r1.queue.isNotEmpty())

        val r2 = dispatcher.resolve()
        assertTrue(r2.queue.isEmpty())
        assertEquals(3, c.result)
    }

    @Test
    fun late_submission_of_required_node() {
        val dispatcher = ActionExecutor()
        val ctx = ProblemContext(dispatcher, dispatcher)

        lateinit var producer: NodeActionImpl<Int>
        val consumer = NodeActionImpl(ctx) {
            val x by get(producer)
            x * 3
        }

        dispatcher.submit(consumer)
        val r1 = dispatcher.resolve()
        assertTrue(r1.queue.isNotEmpty())

        producer = NodeActionImpl(ctx) { 4 }
        // Important: submit producer after first resolve
        dispatcher.submit(producer)
        val r2 = dispatcher.resolve()
        assertTrue(r2.queue.isEmpty())
        assertEquals(12, consumer.result)
    }

    @Test
    fun optional_subactions_only_once_per_key() {
        val dispatcher = ActionExecutor()
        val ctx = ProblemContext(dispatcher, dispatcher)
        val calls = mutableListOf<Int>()

        val node = NodeActionImpl(ctx) {
            optional("key") { calls.add(1) }
            optional("key") { calls.add(2) }
            0
        }

        dispatcher.submit(node)
        dispatcher.resolve()
        node.exec()
        // Only first optional action (for the same key) should be scheduled once
        assertEquals(listOf(1), calls)
    }

    @Test
    fun finished_extension_true_on_error() {
        val dispatcher = ActionExecutor()
        val ctx = ProblemContext(dispatcher, dispatcher)
        val node = NodeActionImpl<Int>(ctx) { throw IllegalArgumentException() }
        dispatcher.submit(node)
        val r = dispatcher.resolve()
        assertTrue(node.finished())
        assertTrue(r.queue.isNotEmpty())
    }
}
