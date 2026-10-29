package com.example

import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import android.os.Looper
import java.util.concurrent.TimeUnit

/**
 * Cold start with an empty database (fresh install, nothing approved on the server yet).
 * Guards against crashes and main-thread hangs during the first frames.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AppStartupTest {

    @Test(timeout = 120_000)
    fun `cold start with empty data does not crash or hang`() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            repeat(20) {
                shadowOf(Looper.getMainLooper()).idleFor(250, TimeUnit.MILLISECONDS)
            }
            assertEquals(Lifecycle.State.RESUMED, scenario.state)
        }
    }
}
