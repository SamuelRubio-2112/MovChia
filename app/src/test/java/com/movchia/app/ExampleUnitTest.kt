package com.movchia.app

import com.movchia.app.data.model.AgeGroup
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Pruebas unitarias locales (se ejecutan en la máquina de desarrollo).
 */
class ExampleUnitTest {
    @Test
    fun ageGroup_isResolvedFromAge() {
        assertEquals(AgeGroup.KIDS, AgeGroup.fromAge(7))
        assertEquals(AgeGroup.ADULTS, AgeGroup.fromAge(30))
        assertEquals(AgeGroup.SENIORS, AgeGroup.fromAge(65))
    }
}
