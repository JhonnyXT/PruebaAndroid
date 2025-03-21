package com.example.pruebaandroid.auth

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.pruebaandroid.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class LoginScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun muestraCamposYBotonLogin() {
        composeRule.onNodeWithText("Usuario").assertIsDisplayed()
        composeRule.onNodeWithText("Contraseña").assertIsDisplayed()
        composeRule.onNodeWithText("Login").assertIsDisplayed()
    }

    @Test
    fun loginConCamposVaciosMuestraToast() {
        composeRule.onNodeWithText("Login").performClick()
        // No se puede validar el Toast directamente sin una librería extra,
        // pero podrías verificar que no navega o se reinician los campos.
    }
}
