package io.github.kshulzh.kshku.signals

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CoroutineSignalCatcherImplTest {

    @Test
    fun `handles matching signal in same coroutine`() = runBlocking {
        var calls = 0
        var payload: String? = null

        val catcher = CoroutineSignalCatcherImpl().signal<String> {
            calls += 1
            payload = it
        }

        catcher.run {
            // Emit from a child coroutine so hierarchical lookup works
            signalSuspend("hello")
        }

        assertEquals(1, calls)
        assertEquals("hello", payload)
    }

    @Test
    fun `delegates to parent when child has no handler`() = runBlocking {
        var parentCalls = 0
        var childCalls = 0

        val parent = CoroutineSignalCatcherImpl().signal<Int> {
            parentCalls += it
        }
        val child = CoroutineSignalCatcherImpl().signal<String> {
            // different type, should not be called in this test
            childCalls += 1
        }

        parent.run {
            // In parent context, launch a child coroutine and install child catcher

            child.run {
                // Emit Int; child has no Int handler, should delegate to parent
                signalSuspend(3)
            }

        }

        assertEquals(3, parentCalls)
        assertEquals(0, childCalls)
    }

    @Test
    fun `setup and delete push and pop context`() = runBlocking {
        suspend fun current(): CoroutineSignalCatcher? = CoroutineSignalCatcher.signal.getValue()

        assertNull(current(), "No catcher before setup")

        val parent = CoroutineSignalCatcherImpl()
        parent.run {
            // parent is current
            assertEquals(parent, current())

            val child = CoroutineSignalCatcherImpl()
            child.run {
                // child overrides current
                assertEquals(child, current())
            }
            // after child.delete(), parent restored
            assertEquals(parent, current())
        }

        // after parent.delete(), no catcher
        assertNull(current(), "No catcher after delete")
    }
}