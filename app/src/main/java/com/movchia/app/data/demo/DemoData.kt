package com.movchia.app.data.demo

import com.movchia.app.data.model.AgeGroup
import com.movchia.app.data.model.ContentType
import com.movchia.app.data.model.EducationalContent
import com.movchia.app.data.model.QuizQuestion
import com.movchia.app.data.model.ServicePoint
import com.movchia.app.data.model.TrafficCodeArticle

/**
 * Datos de demostración. Se usan en dos lugares:
 *  - Modo demo (solo debug): la app los sirve desde memoria, sin Firebase.
 *  - Sembrado (solo debug): se escriben en Firestore con estos mismos IDs.
 *
 * ATENCIÓN: textos, direcciones y teléfonos son ilustrativos. La Secretaría de
 * Movilidad debe validarlos antes de publicarlos como contenido real.
 */
object DemoData {

    private const val DAY_MS = 24L * 60 * 60 * 1000
    private val now = System.currentTimeMillis()

    // Videos públicos de prueba (cortos de Blender Foundation, licencia CC-BY).
    // Verificados con HTTP 200 el 2026-11-10; si dejan de servir, cambiarlos aquí.
    private const val SAMPLE_MP4 = "https://test-videos.co.uk/vids/bigbuckbunny/mp4/h264/360/Big_Buck_Bunny_360_10s_1MB.mp4"
    private const val SAMPLE_MP4_THUMB = "https://upload.wikimedia.org/wikipedia/commons/c/c5/Big_buck_bunny_poster_big.jpg"
    private const val SAMPLE_YOUTUBE = "https://www.youtube.com/watch?v=aqz-KE-bpKQ"
    private const val SAMPLE_MP4_2 = "https://download.blender.org/durian/trailer/sintel_trailer-480p.mp4"
    private const val SAMPLE_MP4_2_THUMB = "https://picsum.photos/seed/movchia-sintel/640/360"
    private const val SAMPLE_NEWS_THUMB = "https://picsum.photos/seed/movchia-ciclorruta/640/360"

    val contents: List<EducationalContent> = listOf(
        // ----- Niños -----
        EducationalContent(
            id = "demo-story-semaforo",
            title = "Don Semáforo nos enseña",
            description = "Había una vez un semáforo muy sabio en la esquina del parque de Chía. " +
                "Cada mañana saludaba a los niños que iban al colegio y les recordaba su secreto: " +
                "\"Rojo para parar, amarillo para esperar y verde para cruzar de la mano de mamá o papá\". " +
                "Un día, Tomás quiso cruzar corriendo cuando la luz estaba en rojo. Don Semáforo parpadeó " +
                "muy fuerte y Tomás se detuvo justo a tiempo. Desde entonces, Tomás siempre mira a los dos " +
                "lados, espera la luz verde y cruza por la cebra. ¡Y tú también puedes ser un peatón estrella!",
            type = ContentType.STORY,
            targetGroups = listOf(AgeGroup.KIDS),
            publishedAt = now - 1 * DAY_MS,
        ),
        EducationalContent(
            id = "demo-story-casco",
            title = "Lina y su casco de colores",
            description = "Lina recibió una bicicleta nueva, pero su abuela le dijo que antes de montarla " +
                "necesitaba algo muy importante: un casco. Lina eligió uno con estrellas de colores. " +
                "Al principio le parecía incómodo, pero cuando se cayó en la ciclorruta del parque, el casco " +
                "la protegió y solo se raspó la rodilla. Ahora Lina nunca pedalea sin su casco de estrellas.",
            type = ContentType.STORY,
            targetGroups = listOf(AgeGroup.KIDS),
            publishedAt = now - 3 * DAY_MS,
        ),
        EducationalContent(
            id = "demo-cartoon-casco",
            title = "Las Aventuras del Casco Mágico",
            description = "Dibujos animados: el Casco Mágico protege a los niños ciclistas de Chía. Video de prueba.",
            type = ContentType.CARTOON,
            mediaUrl = SAMPLE_MP4,
            thumbnailUrl = SAMPLE_MP4_THUMB,
            targetGroups = listOf(AgeGroup.KIDS),
            durationMinutes = 10,
            publishedAt = now - 2 * DAY_MS,
        ),
        EducationalContent(
            id = "demo-cartoon-cebra",
            title = "La cebra que cruza contigo",
            description = "Aprende con dibujos por qué el paso peatonal se llama cebra y cómo usarlo. Video de prueba en YouTube.",
            type = ContentType.CARTOON,
            mediaUrl = SAMPLE_YOUTUBE,
            targetGroups = listOf(AgeGroup.KIDS),
            durationMinutes = 10,
            publishedAt = now - 5 * DAY_MS,
        ),

        // ----- Jóvenes y Adultos -----
        EducationalContent(
            id = "demo-news-ciclorrutas",
            title = "Nuevas ciclorrutas habilitadas en el centro de Chía",
            description = "La Secretaría de Movilidad habilitó 3,2 kilómetros de ciclorruta entre la Avenida Pradilla " +
                "y el parque principal. El corredor cuenta con señalización horizontal nueva, bolardos de " +
                "protección y cruces semaforizados. Se invita a ciclistas y conductores a respetar la " +
                "separación de carriles y a usar casco y chaleco reflectivo en horas de poca luz.",
            type = ContentType.NEWS,
            thumbnailUrl = SAMPLE_NEWS_THUMB,
            targetGroups = listOf(AgeGroup.ADULTS),
            publishedAt = now - 1 * DAY_MS,
        ),
        EducationalContent(
            id = "demo-news-fotomultas",
            title = "Jornada de capacitación gratuita en normas de tránsito",
            description = "Este sábado, de 8:00 a.m. a 12:00 m., la Secretaría de Movilidad ofrecerá una jornada " +
                "gratuita de capacitación sobre el Código Nacional de Tránsito en la Casa de la Cultura. " +
                "Los asistentes recibirán certificado de participación. Inscripciones en las sedes de atención.",
            type = ContentType.NEWS,
            targetGroups = listOf(AgeGroup.ADULTS),
            publishedAt = now - 4 * DAY_MS,
        ),
        EducationalContent(
            id = "demo-campaign-peaton",
            title = "Campaña: Respeta al Peatón",
            description = "Campaña del mes. En Chía, el peatón siempre tiene prioridad en los pasos cebra. " +
                "Detente, haz contacto visual y deja cruzar. Video de prueba en YouTube.",
            type = ContentType.CAMPAIGN,
            mediaUrl = SAMPLE_YOUTUBE,
            targetGroups = listOf(AgeGroup.ADULTS, AgeGroup.SENIORS),
            durationMinutes = 10,
            publishedAt = now - 2 * DAY_MS,
        ),

        // ----- Adultos Mayores -----
        EducationalContent(
            id = "demo-video-cruzar",
            title = "Cómo cruzar una calle con seguridad",
            description = "Video explicativo: dónde pararse, cómo mirar a ambos lados y cuándo cruzar. Video de prueba.",
            type = ContentType.VIDEO,
            mediaUrl = SAMPLE_MP4_2,
            thumbnailUrl = SAMPLE_MP4_2_THUMB,
            targetGroups = listOf(AgeGroup.SENIORS, AgeGroup.ADULTS),
            durationMinutes = 1,
            publishedAt = now - 1 * DAY_MS,
        ),
        EducationalContent(
            id = "demo-faq-licencia",
            title = "¿Cómo renuevo mi licencia de conducción?",
            description = "Debe acudir a un Centro de Reconocimiento de Conductores (CRC) para el examen médico, " +
                "pagar los derechos del trámite y luego presentarse en la Secretaría de Movilidad con su cédula " +
                "y la licencia anterior. El trámite tarda aproximadamente una semana.",
            type = ContentType.FAQ,
            targetGroups = listOf(AgeGroup.SENIORS, AgeGroup.ADULTS),
            publishedAt = now - 2 * DAY_MS,
        ),
        EducationalContent(
            id = "demo-faq-comparendo",
            title = "¿Qué hago si me llega un comparendo?",
            description = "Tiene cinco días hábiles para pagar con descuento del 50 % o para solicitar audiencia " +
                "si no está de acuerdo. Puede consultarlo en el SIMIT con su número de cédula.",
            type = ContentType.FAQ,
            targetGroups = listOf(AgeGroup.SENIORS, AgeGroup.ADULTS),
            publishedAt = now - 3 * DAY_MS,
        ),
        EducationalContent(
            id = "demo-faq-emergencia",
            title = "¿A qué número llamo en una emergencia vial?",
            description = "Marque 123. Es gratuito desde cualquier teléfono y atiende policía, bomberos y ambulancia. " +
                "Indique el lugar exacto, qué ocurrió y si hay personas heridas.",
            type = ContentType.FAQ,
            targetGroups = listOf(AgeGroup.SENIORS),
            publishedAt = now - 4 * DAY_MS,
        ),
    )

    val trafficCodeArticles: List<TrafficCodeArticle> = listOf(
        TrafficCodeArticle(
            id = "demo-art-55",
            chapter = "Título III · Normas de comportamiento",
            articleNumber = "55",
            title = "Comportamiento del conductor, pasajero o peatón",
            body = "Toda persona que tome parte en el tránsito como conductor, pasajero o peatón, debe comportarse " +
                "en forma que no obstaculice, perjudique o ponga en riesgo a las demás y debe conocer y cumplir " +
                "las normas y señales de tránsito que le sean aplicables, así como obedecer las indicaciones " +
                "que les den las autoridades de tránsito.",
            simplifiedBody = "En la calle, todos (conductores, pasajeros y peatones) debemos cuidarnos unos a otros, " +
                "conocer las señales y hacer caso a los agentes de tránsito.",
            tags = listOf("peatón", "conductor", "comportamiento"),
            order = 1,
        ),
        TrafficCodeArticle(
            id = "demo-art-94",
            chapter = "Título III · Ciclistas y motociclistas",
            articleNumber = "94",
            title = "Normas generales para bicicletas, triciclos, motocicletas y similares",
            body = "Los conductores de bicicletas, triciclos, motocicletas, motociclos y mototriciclos deben transitar " +
                "por la derecha de las vías, a distancia no mayor de un metro de la acera u orilla, y nunca " +
                "utilizar las vías exclusivas para servicio público colectivo. No deben transitar sobre las " +
                "aceras ni en zonas destinadas al tránsito de peatones, ni sujetarse de otro vehículo.",
            simplifiedBody = "Si va en bicicleta o moto: vaya por la derecha, cerca del andén, nunca por el andén " +
                "ni agarrado de otro vehículo.",
            tags = listOf("bicicleta", "ciclista", "moto", "ciclorruta"),
            order = 2,
        ),
        TrafficCodeArticle(
            id = "demo-art-106",
            chapter = "Título III · Límites de velocidad",
            articleNumber = "106",
            title = "Límites de velocidad en vías urbanas y carreteras municipales",
            body = "En las vías urbanas las velocidades máximas y mínimas para vehículos de servicio público o " +
                "particular serán determinadas y debidamente señalizadas por la autoridad de tránsito competente " +
                "en el distrito o municipio respectivo. En ningún caso podrá sobrepasar los 50 kilómetros por hora. " +
                "La velocidad en zonas escolares y en zonas residenciales será hasta de 30 kilómetros por hora.",
            simplifiedBody = "Dentro del municipio nunca se puede ir a más de 50 km/h. Cerca de colegios y en barrios, " +
                "máximo 30 km/h.",
            tags = listOf("velocidad", "zona escolar", "50 km/h"),
            order = 3,
        ),
    )

    // Coordenadas aproximadas del centro de Chía. Verificar con la Secretaría.
    val servicePoints: List<ServicePoint> = listOf(
        ServicePoint(
            id = "demo-sede-principal",
            name = "Secretaría de Movilidad · Sede principal",
            address = "Carrera 11 No. 11-29, Chía (Alcaldía Municipal)",
            phone = "(601) 884 4444",
            schedule = "Lunes a viernes, 8:00 a.m. a 5:00 p.m.",
            latitude = 4.8617,
            longitude = -74.0583,
        ),
        ServicePoint(
            id = "demo-sede-tramites",
            name = "Punto de trámites de tránsito",
            address = "Calle 10 No. 9-50, Chía",
            phone = "(601) 884 4455",
            schedule = "Lunes a viernes, 7:30 a.m. a 4:30 p.m.",
            latitude = 4.8602,
            longitude = -74.0591,
        ),
    )

    val quizQuestions: List<QuizQuestion> = listOf(
        QuizQuestion(
            id = "demo-q-pare",
            question = "¿Qué debes hacer cuando ves una señal octagonal roja que dice PARE?",
            options = listOf("Reducir la velocidad", "Detenerte por completo", "Tocar la bocina", "Acelerar"),
            correctIndex = 1,
            explanation = "La señal de PARE obliga a detener el vehículo por completo antes de continuar.",
            targetGroups = listOf(AgeGroup.KIDS, AgeGroup.ADULTS),
            points = 10,
        ),
        QuizQuestion(
            id = "demo-q-amarillo",
            question = "El semáforo cambia a amarillo. ¿Qué significa?",
            options = listOf("Acelera para pasar", "Prepárate para detenerte", "Puedes cruzar caminando", "Nada"),
            correctIndex = 1,
            explanation = "La luz amarilla avisa que viene el rojo: hay que prepararse para detenerse.",
            targetGroups = listOf(AgeGroup.KIDS, AgeGroup.ADULTS),
            points = 10,
        ),
        QuizQuestion(
            id = "demo-q-cebra",
            question = "¿Por dónde debe cruzar un peatón la calle?",
            options = listOf("Por donde quiera", "Por el paso cebra o el puente peatonal", "Entre los carros", "Por la esquina sin mirar"),
            correctIndex = 1,
            explanation = "El paso cebra y los puentes peatonales son los lugares seguros para cruzar.",
            targetGroups = listOf(AgeGroup.KIDS, AgeGroup.ADULTS, AgeGroup.SENIORS),
            points = 10,
        ),
        QuizQuestion(
            id = "demo-q-velocidad",
            question = "¿Cuál es la velocidad máxima en una zona escolar?",
            options = listOf("30 km/h", "50 km/h", "60 km/h", "80 km/h"),
            correctIndex = 0,
            explanation = "En zonas escolares y residenciales el límite es 30 km/h (Art. 106, Ley 769 de 2002).",
            targetGroups = listOf(AgeGroup.ADULTS),
            points = 15,
        ),
    )
}
