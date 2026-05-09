---
name: Coverage setup — Kover + Robolectric in KMP
description: Kover 0.9.8 setup; 2026-05-09 fix — UI tests excluded from coverage causing 0%; 2026-05-09 testability refactor
type: project
---

Kover 0.9.8 configured in composeApp/build.gradle.kts with class exclusions for BuildConfig, *Res, *.generated.*.

**Why:** Codecov target ≥ 80%; UI tests (androidUnitTest) were previously excluded from coverage, causing 0% on all screen files.

**2026-05-09 testability refactor:**
- Extracted `ClockProvider` interface + `SystemClockProvider` impl (core/util)
- `PortfolioEntityMapper.toPortfolioCoinEntity()` now accepts `timestamp: Long` instead of calling `Clock.System.now()` directly
- `PortfolioRepositoryImpl` injects `ClockProvider` (4th ctor param, default = SystemClockProvider)
- `CoinsListViewModel`, `BuyViewModel`, `SellViewModel` now accept `CoroutineDispatcher` (default = Dispatchers.Default) with `flowOn(coroutineDispatcher)` on the state chain and `viewModelScope.launch(coroutineDispatcher)` on event handlers
- Koin module updated: `single<ClockProvider> { SystemClockProvider() }`
- New unit tests: TradeCoinMapperTest, ResultExtensionsTest, DataErrorToStringTest, CurrencyOffsetMappingTest
- Extended PortfolioRepositoryIntegrationTest: 9 scenarios (was 3)
- FakeClockProvider in commonTest for deterministic timestamp assertions

**How to apply:** When writing new tests for ViewModels, pass `coroutineDispatcher = StandardTestDispatcher(testScheduler)` to constructor. `Dispatchers.setMain` is still required for `viewModelScope`. For mapper tests that involve timestamps, use `FakeClockProvider`.
