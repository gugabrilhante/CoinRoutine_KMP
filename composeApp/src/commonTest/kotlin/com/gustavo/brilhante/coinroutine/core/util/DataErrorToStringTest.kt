package com.gustavo.brilhante.coinroutine.core.util

import coinroutine.composeapp.generated.resources.Res
import coinroutine.composeapp.generated.resources.error_disk_full
import coinroutine.composeapp.generated.resources.error_insufficient_balance
import coinroutine.composeapp.generated.resources.error_no_internet
import coinroutine.composeapp.generated.resources.error_request_timeout
import coinroutine.composeapp.generated.resources.error_serialization
import coinroutine.composeapp.generated.resources.error_too_many_requests
import coinroutine.composeapp.generated.resources.error_unknown
import com.gustavo.brilhante.coinroutine.core.domain.DataError
import kotlin.test.Test
import kotlin.test.assertEquals

class DataErrorToStringTest {

    // ── Local errors ─────────────────────────────────────────────────────────

    @Test
    fun `DISK_FULL maps to error_disk_full`() {
        assertEquals(Res.string.error_disk_full, DataError.Local.DISK_FULL.toUiText())
    }

    @Test
    fun `INSUFFICIENT_FUNDS maps to error_insufficient_balance`() {
        assertEquals(Res.string.error_insufficient_balance, DataError.Local.INSUFFICIENT_FUNDS.toUiText())
    }

    @Test
    fun `Local UNKNOWN maps to error_unknown`() {
        assertEquals(Res.string.error_unknown, DataError.Local.UNKNOWN.toUiText())
    }

    // ── Remote errors ────────────────────────────────────────────────────────

    @Test
    fun `REQUEST_TIMEOUT maps to error_request_timeout`() {
        assertEquals(Res.string.error_request_timeout, DataError.Remote.REQUEST_TIMEOUT.toUiText())
    }

    @Test
    fun `TOO_MANY_REQUESTS maps to error_too_many_requests`() {
        assertEquals(Res.string.error_too_many_requests, DataError.Remote.TOO_MANY_REQUESTS.toUiText())
    }

    @Test
    fun `NO_INTERNET maps to error_no_internet`() {
        assertEquals(Res.string.error_no_internet, DataError.Remote.NO_INTERNET.toUiText())
    }

    @Test
    fun `SERVER maps to error_unknown`() {
        assertEquals(Res.string.error_unknown, DataError.Remote.SERVER.toUiText())
    }

    @Test
    fun `SERIALIZATION maps to error_serialization`() {
        assertEquals(Res.string.error_serialization, DataError.Remote.SERIALIZATION.toUiText())
    }

    @Test
    fun `Remote UNKNOWN maps to error_unknown`() {
        assertEquals(Res.string.error_unknown, DataError.Remote.UNKNOWN.toUiText())
    }
}
