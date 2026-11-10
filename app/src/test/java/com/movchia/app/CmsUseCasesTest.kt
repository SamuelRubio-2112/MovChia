package com.movchia.app

import com.movchia.app.data.model.AgeGroup
import com.movchia.app.data.model.ContentType
import com.movchia.app.data.model.EducationalContent
import com.movchia.app.data.model.Forum
import com.movchia.app.data.model.StaffRole
import com.movchia.app.domain.usecases.SaveContentUseCase
import com.movchia.app.domain.usecases.SaveForumUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CmsUseCasesTest {

    private val validNews = EducationalContent(
        title = "Nueva señalización en la Avenida Pradilla",
        description = "La Secretaría instaló 20 señales nuevas.",
        type = ContentType.NEWS,
        targetGroups = listOf(AgeGroup.ADULTS),
    )

    @Test
    fun `contenido válido pasa la validación`() {
        assertNull(SaveContentUseCase.validate(validNews))
    }

    @Test
    fun `título corto o sin grupos de edad se rechaza`() {
        assertNotNull(SaveContentUseCase.validate(validNews.copy(title = "ab")))
        assertNotNull(SaveContentUseCase.validate(validNews.copy(targetGroups = emptyList())))
    }

    @Test
    fun `video y dibujos exigen enlace de medio`() {
        val video = validNews.copy(type = ContentType.VIDEO, mediaUrl = "")
        assertTrue(SaveContentUseCase.validate(video)!!.contains("enlace"))
        assertNull(SaveContentUseCase.validate(video.copy(mediaUrl = "https://www.youtube.com/watch?v=abc")))
    }

    @Test
    fun `noticia sin texto ni medio se rechaza, con imagen de assets se acepta`() {
        assertNotNull(SaveContentUseCase.validate(validNews.copy(description = "")))
        assertNull(
            SaveContentUseCase.validate(
                validNews.copy(description = "", mediaUrl = "file:///android_asset/seed/agentes_transito_1.jpg"),
            ),
        )
    }

    @Test
    fun `enlaces que no son http ni assets se rechazan`() {
        assertNotNull(SaveContentUseCase.validate(validNews.copy(mediaUrl = "javascript:alert(1)")))
        assertNotNull(SaveContentUseCase.validate(validNews.copy(thumbnailUrl = "C:\\fotos\\a.jpg")))
    }

    @Test
    fun `foro válido e inválido`() {
        assertNull(SaveForumUseCase.validate(Forum(title = "Ciclorrutas", description = "¿Qué mejorar?")))
        assertNotNull(SaveForumUseCase.validate(Forum(title = "ab")))
        assertNotNull(SaveForumUseCase.validate(Forum(title = "Peatones", description = "x".repeat(201))))
    }

    @Test
    fun `rol se lee de Firestore sin importar mayúsculas`() {
        assertEquals(StaffRole.GESTOR, StaffRole.fromFirestore("gestor"))
        assertEquals(StaffRole.ROOT, StaffRole.fromFirestore(" Root "))
        assertNull(StaffRole.fromFirestore("ciudadano"))
        assertNull(StaffRole.fromFirestore(null))
    }
}
