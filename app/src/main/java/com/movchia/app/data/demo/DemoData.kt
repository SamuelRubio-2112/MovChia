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

    // Imágenes empaquetadas en app/src/main/assets/seed (sin costo de Storage).
    private const val ASSET_AGENTES_1 = "file:///android_asset/seed/agentes_transito_1.jpg"
    private const val ASSET_AGENTES_2 = "file:///android_asset/seed/agentes_transito_2.jpg"
    private const val ASSET_INFRACTOR_1 = "file:///android_asset/seed/cambio_infractor_1.jpg"

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
        // Noticias reales suministradas por la Secretaría de Movilidad (docs/material).
        EducationalContent(
            id = "news-traspaso-persona-indeterminada",
            title = "Aviso: 2.026 vehículos con traspaso a persona indeterminada deben regularizarse",
            description = "La Secretaría de Movilidad de Chía, en coordinación con la Unión Temporal Circulemos Chía, " +
                "informa que, tras el inventario ordenado por el Ministerio de Transporte (Circular Externa " +
                "20254000000627 del 24 de septiembre de 2025), se identificaron 2.026 vehículos (automóviles y " +
                "motocicletas) matriculados en Chía que llevan más de tres años en estado de \"traspaso a persona " +
                "indeterminada\".\n\n" +
                "Los interesados tienen un plazo de SEIS (6) MESES, contados desde la publicación del aviso, para " +
                "comparecer ante la Secretaría de Movilidad / Circulemos Chía y:\n" +
                "• Acreditar su calidad de propietario o adquirente del vehículo.\n" +
                "• Aportar los documentos exigidos por la normatividad vigente.\n" +
                "• Formalizar el traspaso ante el Organismo de Tránsito.\n\n" +
                "Vencido el plazo sin que se presente un interesado, la Secretaría podrá expedir la resolución que " +
                "ordene la suspensión del registro del vehículo. Un vehículo con registro suspendido no puede " +
                "circular y, de ser encontrado en vía, puede ser inmovilizado (art. 34, Ley 769 de 2002).\n\n" +
                "El listado completo de placas está publicado en la sede de la Secretaría (Diagonal 17 No. 6-108, " +
                "piso 1) y en www.chia-cundinamarca.gov.co.",
            type = ContentType.NEWS,
            mediaUrl = ASSET_AGENTES_1,
            thumbnailUrl = ASSET_AGENTES_1,
            targetGroups = listOf(AgeGroup.ADULTS),
            publishedAt = now - 1 * DAY_MS,
        ),
        EducationalContent(
            id = "news-patios-abandono",
            title = "Vehículos con más de un año en patios: 15 días hábiles antes de declararlos en abandono",
            description = "La Secretaría de Movilidad de Chía, como Organismo de Tránsito del municipio, publica el " +
                "listado de vehículos que permanecen en los patios oficiales con más de un año de inmovilización, " +
                "en cumplimiento de la Ley 1730 de 2014.\n\n" +
                "El propietario, poseedor o tercero con derecho (locatario, acreedor prendario o arrendatario) puede " +
                "acercarse a la Secretaría, en la Diagonal 17 No. 6-108, piso 1, para subsanar la causa de la " +
                "inmovilización y pagar lo adeudado por parqueadero y grúa.\n\n" +
                "Si dentro de los quince (15) días hábiles siguientes a la publicación no se presenta ningún " +
                "interesado, se declarará administrativamente el abandono de los vehículos y se procederá a su " +
                "enajenación.\n\n" +
                "Consulte el listado completo de placas en la sede de la Secretaría o en www.chia-cundinamarca.gov.co.",
            type = ContentType.NEWS,
            mediaUrl = ASSET_AGENTES_2,
            thumbnailUrl = ASSET_AGENTES_2,
            targetGroups = listOf(AgeGroup.ADULTS),
            publishedAt = now - 2 * DAY_MS,
        ),
        EducationalContent(
            id = "news-cambio-infractor-fotocomparendo",
            title = "Realiza el cambio de infractor de fotocomparendo en la Sede Electrónica",
            description = "La Sede Electrónica es el portal virtual que permite a los ciudadanos consultar la " +
                "información personal y vehicular registrada en el RUNT: infracciones de tránsito, licencias de " +
                "conducción, vehículos asociados y estado de trámites.\n\n" +
                "Una de sus funciones permite cambiar al infractor de un fotocomparendo reportado en el SIMIT cuando " +
                "la multa fue aplicada al propietario del vehículo pero la infracción la cometió otra persona. El " +
                "cambio debe ser consensuado entre ambas partes.\n\n" +
                "Paso a paso:\n" +
                "1. Actualización de datos: verifique que su correo, teléfono y dirección estén al día.\n" +
                "2. Registro en la Sede Electrónica: ambos infractores deben estar registrados.\n" +
                "3. Solicitud de cambio: el ciudadano la realiza desde la Sede Electrónica.\n" +
                "4. Aceptación del comparendo: el nuevo infractor ingresa y acepta el cambio.\n" +
                "5. Aprobación por parte del Tránsito: el Organismo valida la solicitud y la reporta al SIMIT.\n" +
                "6. Curso y pago: el nuevo infractor realiza el curso de normas de tránsito y paga la infracción.\n\n" +
                "Recomendaciones: no aplica para comparendos de SOAT o revisión técnico-mecánica; el comparendo debe " +
                "estar en el SIMIT en estado pendiente y dentro de los términos legales; el proceso es voluntario y " +
                "solo aplica si quien asume la infracción es una persona natural. Si el nuevo infractor no acepta o " +
                "el Tránsito no aprueba el cambio, el comparendo sigue asociado al infractor inicial.",
            type = ContentType.NEWS,
            mediaUrl = ASSET_INFRACTOR_1,
            thumbnailUrl = ASSET_INFRACTOR_1,
            targetGroups = listOf(AgeGroup.ADULTS, AgeGroup.SENIORS),
            publishedAt = now - 3 * DAY_MS,
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

    // Dirección, PBX y correo: pie de página de los avisos oficiales (docs/material).
    // Horario confirmado por el equipo. Enlaces y coordenadas: fichas de Google Maps.
    val servicePoints: List<ServicePoint> = listOf(
        ServicePoint(
            id = "sede-secretaria-movilidad",
            name = "Secretaría de Movilidad de Chía",
            address = "Diagonal 17 No. 6-108, piso 1, Chía (Cundinamarca)",
            phone = "(601) 884 4444 ext. 3505",
            schedule = "Lunes a viernes, 8:00 a.m. a 5:00 p.m.",
            email = "correspondencia.chia@datatools.com.co",
            latitude = 4.8638403,
            longitude = -74.0551414,
            mapsUrl = "https://maps.app.goo.gl/DvQy44QaVdPp25j87",
        ),
        ServicePoint(
            id = "sede-transito-transporte",
            name = "Secretaría de Tránsito y Transporte de Chía",
            address = "Diagonal 17 No. 6-108, Chía (Cundinamarca)",
            phone = "(601) 884 4444",
            schedule = "Lunes a viernes, 8:00 a.m. a 5:00 p.m.",
            latitude = 4.86381,
            longitude = -74.05509,
            mapsUrl = "https://maps.app.goo.gl/ZX4RVgGzzyCp81ng7",
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
