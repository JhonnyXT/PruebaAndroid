package com.example.pruebaandroid.menu

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.example.pruebaandroid.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class MenuScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun muestraTextoAccesosRestantes() {
        composeRule.onNodeWithText("Accesos restantes").assertExists()
    }

    @Test
    fun botonObtenerArchivoExiste() {
        composeRule.onNodeWithText("Obtener archivo").assertIsDisplayed()
    }
}
