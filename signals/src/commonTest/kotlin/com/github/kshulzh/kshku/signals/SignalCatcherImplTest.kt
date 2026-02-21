package com.github.kshulzh.kshku.signals

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SignalCatcherImplTest {

    @Test
    fun `handles matching signal in same thread`() {
        var calls = 0
        var payload: String? = null

        val catcher = SignalCatcherImpl().signal<String> {
            calls += 1
            payload = it
        }

        catcher.run {
            signal("hello")
        }

        assertEquals(1, calls)
        assertEquals("hello", payload)
    }

    @Test
    fun `delegates to parent when child has no handler`() {
        var parentCalls = 0
        var childCalls = 0

        val parent = SignalCatcherImpl().signal<Int> {
            parentCalls += it
        }
        val child = SignalCatcherImpl().signal<String> {
            // different type, should not be called in this test
            childCalls += 1
        }

        parent.run {
            child.run {
                // Emit Int; child has no Int handler, should delegate to parent
                signal(3)
            }
        }

        assertEquals(3, parentCalls)
        assertEquals(0, childCalls)
    }

    @Test
    fun `setup and delete push and pop context`() {
        fun current(): SignalCatcher? = SignalCatcher.signal

        assertNull(current(), "No catcher before setup")

        val parent = SignalCatcherImpl()
        parent.run {
            // parent is current
            assertEquals(parent, current())

            val child = SignalCatcherImpl()
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