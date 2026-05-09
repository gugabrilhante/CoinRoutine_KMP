package com.gustavo.brilhante.coinroutine.core.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ResultExtensionsTest {

    // ── map ──────────────────────────────────────────────────────────────────

    @Test
    fun `map transforms Success data`() {
        val result: Result<Int, DataError.Remote> = Result.Success(5)
        val mapped = result.map { it * 2 }
        val success = assertIs<Result.Success<Int>>(mapped)
        assertEquals(10, success.data)
    }

    @Test
    fun `map propagates Error unchanged`() {
        val result: Result<Int, DataError.Remote> = Result.Error(DataError.Remote.NO_INTERNET)
        val mapped = result.map { it * 2 }
        val error = assertIs<Result.Error<DataError.Remote>>(mapped)
        assertEquals(DataError.Remote.NO_INTERNET, error.error)
    }

    @Test
    fun `map can change the type of the wrapped value`() {
        val result: Result<Int, DataError.Remote> = Result.Success(42)
        val mapped = result.map { it.toString() }
        val success = assertIs<Result.Success<String>>(mapped)
        assertEquals("42", success.data)
    }

    // ── onSuccess ────────────────────────────────────────────────────────────

    @Test
    fun `onSuccess executes action for Success`() {
        var called = false
        val result: Result<Int, DataError.Remote> = Result.Success(1)
        result.onSuccess { called = true }
        assertTrue(called)
    }

    @Test
    fun `onSuccess does not execute action for Error`() {
        var called = false
        val result: Result<Int, DataError.Remote> = Result.Error(DataError.Remote.SERVER)
        result.onSuccess { called = true }
        assertFalse(called)
    }

    @Test
    fun `onSuccess returns the original result`() {
        val original: Result<Int, DataError.Remote> = Result.Success(7)
        val returned = original.onSuccess { }
        assertIs<Result.Success<Int>>(returned)
        assertEquals(7, (returned as Result.Success).data)
    }

    // ── onError ──────────────────────────────────────────────────────────────

    @Test
    fun `onError executes action for Error`() {
        var captured: DataError? = null
        val result: Result<Int, DataError.Remote> = Result.Error(DataError.Remote.REQUEST_TIMEOUT)
        result.onError { captured = it }
        assertEquals(DataError.Remote.REQUEST_TIMEOUT, captured)
    }

    @Test
    fun `onError does not execute action for Success`() {
        var called = false
        val result: Result<Int, DataError.Remote> = Result.Success(0)
        result.onError { called = true }
        assertFalse(called)
    }

    @Test
    fun `onError returns the original result`() {
        val original: Result<Int, DataError.Remote> = Result.Error(DataError.Remote.UNKNOWN)
        val returned = original.onError { }
        assertIs<Result.Error<DataError.Remote>>(returned)
    }

    // ── asEmptyDataResult ────────────────────────────────────────────────────

    @Test
    fun `asEmptyDataResult converts Success to Success of Unit`() {
        val result: Result<Int, DataError.Remote> = Result.Success(99)
        val empty = result.asEmptyDataResult()
        val success = assertIs<Result.Success<Unit>>(empty)
        assertEquals(Unit, success.data)
    }

    @Test
    fun `asEmptyDataResult propagates Error`() {
        val result: Result<Int, DataError.Remote> = Result.Error(DataError.Remote.SERIALIZATION)
        val empty = result.asEmptyDataResult()
        val error = assertIs<Result.Error<DataError.Remote>>(empty)
        assertEquals(DataError.Remote.SERIALIZATION, error.error)
    }
}
