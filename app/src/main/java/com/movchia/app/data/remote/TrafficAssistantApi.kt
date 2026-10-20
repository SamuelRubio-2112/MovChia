package com.movchia.app.data.remote

import com.movchia.app.data.model.AssistantMessage
import kotlinx.coroutines.delay

/**
 * Contrato del Asistente de IA sobre el Código Nacional de Tránsito.
 * La implementación real llamará a un backend (Cloud Function, Vertex AI,
 * u otro proveedor de LLM) que responda con base en la Ley 769 de 2002.
 */
interface TrafficAssistantApi {
    suspend fun ask(question: String, history: List<AssistantMessage>): String
}

/**
 * Implementación de desarrollo: responde con un mensaje fijo sin llamar a
 * ningún servicio externo. Sustituir por la implementación real cuando el
 * endpoint esté disponible (ver [com.movchia.app.utils.Constants.AI_ASSISTANT_ENDPOINT]).
 */
class PlaceholderTrafficAssistantApi : TrafficAssistantApi {
    override suspend fun ask(question: String, history: List<AssistantMessage>): String {
        delay(600) // Simula latencia de red para probar estados de carga.
        return "Soy el asistente de MovChia sobre el Código Nacional de Tránsito. " +
            "Aún no estoy conectado al servicio de IA, pero pronto podré responder: " +
            "\"$question\". Mientras tanto, consulta el Manual de Tránsito en esta misma pantalla."
    }
}
