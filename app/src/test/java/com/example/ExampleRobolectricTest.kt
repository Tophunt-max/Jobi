package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.scheduler.DeadlineNotificationScheduler
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Sarkari Jobs", appName)
  }

  @Test
  fun `verify deadline scheduler date parsing and channel initialization`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    DeadlineNotificationScheduler.createNotificationChannel(context)

    val parsedTime = DeadlineNotificationScheduler.parseClosingDate("28-10-2026")
    assertNotNull(parsedTime)
    assertTrue(parsedTime!! > 0L)

    val remainingText = DeadlineNotificationScheduler.formatDaysRemainingText("28-10-2026")
    assertNotNull(remainingText)
  }
}
