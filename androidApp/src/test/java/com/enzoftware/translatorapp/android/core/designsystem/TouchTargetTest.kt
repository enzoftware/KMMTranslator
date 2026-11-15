package com.enzoftware.translatorapp.android.core.designsystem

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Compose expands the hit area of small pointer-input nodes to the 48dp minimum touch target, so
 * compact chips (copy, clear, idioms, swap) stay easy to tap without growing their visual size.
 */
@RunWith(AndroidJUnit4::class)
class TouchTargetTest {
    @get:Rule val rule = createComposeRule()

    @Test
    fun smallCozyClickable_acceptsTapsWithinA48dpTarget() {
        var clicks = 0
        rule.setContent {
            Box(Modifier.padding(40.dp)) {
                Box(Modifier.testTag("chip").cozyClickable { clicks++ }.size(20.dp))
            }
        }
        // 12dp above the 20dp chip's top edge: outside its bounds, inside a centred 48dp target.
        rule.onNodeWithTag("chip").performTouchInput {
            click(Offset(center.x, -12.dp.toPx()))
        }
        assertThat(clicks).isEqualTo(1)
    }

    @Test
    fun smallCozyClickable_ignoresTapsOutsideThe48dpTarget() {
        var clicks = 0
        rule.setContent {
            Box(Modifier.padding(40.dp)) {
                Box(Modifier.testTag("chip").cozyClickable { clicks++ }.size(20.dp))
            }
        }
        // 20dp above the edge is beyond the (48 - 20) / 2 = 14dp expansion.
        rule.onNodeWithTag("chip").performTouchInput {
            click(Offset(center.x, -20.dp.toPx()))
        }
        assertThat(clicks).isEqualTo(0)
    }
}
