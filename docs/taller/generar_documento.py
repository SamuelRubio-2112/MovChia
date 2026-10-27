"""Genera el informe del taller a partir del documento base.

Uso: python generar_documento.py <base.docx> <salida.docx> <carpeta_evidencia>

El documento base puede ser el enunciado original o una versión anterior del
informe: en ese caso se quitan primero los bloques agregados (datos del equipo
y todo lo que va después de la tabla «Registro de pruebas»).
"""
import sys
import docx
from docx.shared import Inches, Pt, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH

SRC, OUT, EV = sys.argv[1], sys.argv[2], sys.argv[3]
d = docx.Document(SRC)
body = d.element.body
T1, T2 = d.tables[0], d.tables[1]

# --- Limpieza de una versión anterior del informe ---
PORTADA = ("Integrantes:", "Aplicación:", "Repositorio:", "Android moderno:", "Android antiguo:", "Estado:")
for p in list(d.paragraphs):
    if p.text.startswith(PORTADA):
        p._p.getparent().remove(p._p)
el = T2._tbl.getnext()
while el is not None and not el.tag.endswith("sectPr"):
    nxt = el.getnext(); body.remove(el); el = nxt


def para_after(anchor, text="", style=None, bold_prefix=None, color=None):
    new = d.add_paragraph(style=style)
    if bold_prefix:
        new.add_run(bold_prefix).bold = True
    r = new.add_run(text)
    if color:
        r.font.color.rgb = RGBColor(*color)
    anchor.addnext(new._p)
    return new._p


class Cursor:
    def __init__(self, anchor): self.a = anchor
    def p(self, text="", style=None, b=None, color=None):
        self.a = para_after(self.a, text, style, b, color)
    def h(self, text, lvl=2): self.p(text, f"Heading {lvl}")
    def images(self, header, pairs):
        t = d.add_table(rows=len(pairs) + 1, cols=2)
        t.style = T1.style
        for ci, cap in enumerate(header):
            r = t.cell(0, ci).paragraphs[0].add_run(cap); r.bold = True; r.font.size = Pt(9)
        for ri, (l, r_, cap) in enumerate(pairs, start=1):
            for ci, f in enumerate((l, r_)):
                cell = t.cell(ri, ci); p = cell.paragraphs[0]
                p.alignment = WD_ALIGN_PARAGRAPH.CENTER
                p.add_run().add_picture(f"{EV}/{f}", height=Inches(3.3))
                cr = cell.add_paragraph().add_run(cap); cr.font.size = Pt(8); cr.italic = True
        self.a.addnext(t._tbl)
        self.a = t._tbl
        self.p()


def fill(table, data):
    for ri, row in enumerate(data, start=1):
        for ci, val in enumerate(row, start=1):
            c = table.rows[ri].cells[ci]; c.text = ""
            c.paragraphs[0].add_run(val).font.size = Pt(9)


c = Cursor(d.paragraphs[1]._p)
c.p("Samuel Rubio, Stephanie Gurrero", b="Integrantes: ")
c.p("MovChia", b="Aplicación: ")
c.p("github.com/SamuelRubio-2112/MovChia, rama compat/android-8", b="Repositorio: ")
c.p("Android 13 (API 33), emulador", b="Android moderno: ")
c.p("Android 8.0 (API 26), emulador de gama baja (2 GB de RAM)", b="Android antiguo: ")

fill(T1, [
    ["33 / 37 / 37", "minSdk → 26; target y compile se conservan", "Con minSdk 33 el APK no instala en Android 8 (INSTALL_FAILED_OLDER_SDK). Bajar targetSdk no sirve para eso."],
    ["AGP 9.4.1 / Gradle 9.6.0 / JDK 25", "Se conservan; se activa R8 en release", "Compila sin problemas; minSdk no depende de AGP ni del JDK. R8 reduce el APK de 17,8 MB a 3,2 MB."],
    ["Compose BOM 2026.02.01", "Se conserva", "Pide minSdk 23 (confirmado: con minSdk 22 la compilación falla)."],
    ["Firebase BoM 34.0.0", "Se conserva", "Pide minSdk 23; compatible con Android 8."],
    ["activity-compose 1.13.0 / core-ktx", "Se conserva", "Piden minSdk 23."],
    ["F1 · Selección de edad", "Barra inferior ajustada al sistema", "Funciona en ambos y se guarda tras reiniciar. En Android 8 la barra del sistema tapaba los botones; corregido."],
    ["F2 · Lectura en voz (modo Mayores)", "Aviso si la voz no está disponible", "Funciona en ambos. En Android 8 la voz no venía instalada y se usaba por internet; ahora la app avisa si no puede leer."],
    ["F3 · Login admin (Firebase)", "Documentado como bloqueo", "La pantalla funciona en ambos, pero el google-services.json es de prueba: Firebase responde «API key not valid»."],
])
fill(T2, [
    ["OK", "APK original: falla. APK final: OK", "Mismo APK release en ambos (minSdk 26, 3,2 MB)."],
    ["OK (2,3–3,2 s)", "OK (0,7–1,0 s)", "Sin cierres inesperados (0 crashes). El emulador moderno es más pesado (1440p)."],
    ["OK", "OK", "Se guarda la edad y abre en «Mayores» tras reiniciar."],
    ["OK con y sin red", "OK con y sin red", "En Android 8 la voz se descargó tras el primer uso; ahora funciona sin internet."],
    ["Bloqueado", "Bloqueado", "Igual en ambos: error de Firebase por la configuración de prueba, no por Android."],
])

c = Cursor(T2._tbl)
c.p()
c.h("Evidencia de pantalla (mismo APK en ambos)")
c.images(("Android 13 (API 33)", "Android 8.0 (API 26)"), [
    ("moderno_01_inicio.png", "antiguo_01_inicio.png", "Inicio"),
    ("moderno_02_home_mayores.png", "antiguo_02_home_mayores.png", "Modo Mayores"),
    ("moderno_05_login_resultado.png", "antiguo_05_login_resultado.png", "Login admin"),
])
c.h("Antes y después de la corrección en Android 8")
c.images(("Antes", "Después"), [
    ("../antiguo_03_home_mayores.png", "antiguo_03_tts_sin_red.png", "Botones inferiores tapados → completos"),
])

c.h("Registro de errores")
c.p("Error al instalar en Android 8: «Requires newer sdk version #33». Causa: minSdk 33. Solución: minSdk 26.", b="E1. ")
c.p("Con minSdk 22 la compilación falla porque Compose exige 23. Esto confirma el mínimo real.", b="E2. ")
c.p("En Android 8 la barra del sistema tapaba los botones INICIO / EMERGENCIA. Causa: la barra inferior no dejaba espacio para la del sistema. Solución: navigationBarsPadding().", b="E3. ")
c.p("En Android 8 la voz en español no venía instalada y dependía de internet. Solución: detectar la voz y mostrar un aviso si no se puede leer.", b="E4. ")
c.p("Login admin: «API key not valid» (con red) y error de red (sin red). Causa: google-services.json de prueba. Queda como bloqueo.", b="E5. ")

c.h("Respuestas a las preguntas técnicas", 1)
for q, a in [
    ("1.", "El mínimo real es API 23, que es lo que piden Compose, Activity y Firebase. Lo comprobamos revisando las dependencias y compilando con minSdk 22, que falla. Usamos 26 porque es la versión asignada."),
    ("2.", "minSdk define desde qué versión se instala la app: lo bajamos a 26. compileSdk es el SDK con que se compila y targetSdk define el comportamiento esperado de la plataforma: los dos quedan en 37. AGP y las bibliotecas no se cambiaron porque ya soportan Android 8."),
    ("3.", "La lectura en voz y la barra de navegación se comportan distinto en Android 8. Mantuvimos un solo código que se adapta: el lector revisa si la voz está instalada o necesita internet y, si no puede leer, muestra un aviso; la barra inferior deja el espacio que ocupa la barra del sistema. No hizo falta preguntar por la versión de Android."),
    ("4.", "Lo más pesado en el equipo antiguo es la primera lectura en voz (unos 2 s, porque descarga la voz). El arranque tarda menos de 1 s. Como optimización activamos R8 en release: el APK pasó de 17,8 MB a 3,2 MB y siguió funcionando en ambos."),
    ("5.", "Bajar bibliotecas implica perder parches de seguridad y funciones, y más trabajo de mantenimiento. No fue necesario. Descartamos forzar la compatibilidad con overrideLibrary y bajar targetSdk."),
    ("6.", "El mismo APK release (mismo SHA-256) se instaló en Android 13 y Android 8. Las capturas y los registros de evidencia/final muestran el dispositivo, el arranque y las funciones, sin crashes. Las funciones 1 y 2 funcionan en ambos; la 3 llega a Firebase en ambos y falla igual por la configuración de prueba."),
]:
    c.p(a, b=q + " ")

c.h("Decisiones humanas")
c.p("Elegimos selección de edad (entrada a la app), lectura en voz (accesibilidad para adultos mayores) y login admin (mantenimiento del contenido).", b="A. ")
c.p("Elegimos bajar solo minSdk (cambio mínimo y seguro) y descartamos bajar las bibliotecas y targetSdk (se pierde seguridad y habría que reescribir código).", b="B. ")
c.p("En Android 8 la voz dependía de internet y la barra del sistema tapaba los botones inferiores. Las dos quedaron corregidas.", b="C. ")

c.h("Uso de IA")
c.p("Usamos Claude Code para revisar la configuración, proponer las correcciones, automatizar las pruebas en los emuladores e interpretar los logs. Aceptamos el cambio de minSdk, el ajuste de la barra inferior, el aviso del lector de voz y R8. Descartamos bajar targetSdk, forzar librerías y probar el login con credenciales reales.")

c.h("Limitación pendiente")
c.p("El login admin solo se puede completar con el google-services.json real del proyecto Firebase, que no teníamos. El bloqueo es de configuración: la app llega a Firebase en ambas versiones.")

c.h("Reflexión individual (borradores: cada integrante debe reescribirla)")
c.p("Aprendí que compilar no basta para demostrar compatibilidad. La app solo tenía un minSdk demasiado alto, y al bajarlo se instaló en Android 8, pero al probarla aparecieron diferencias que en Android 13 no se ven: la voz dependía de internet y la barra del sistema tapaba el botón de emergencia. Las corregimos con un solo código que se adapta al dispositivo, sin preguntar por la versión. También entendí la diferencia entre minSdk, targetSdk y compileSdk, y por qué no conviene bajar targetSdk ni las bibliotecas. Activar R8 redujo mucho el APK, algo útil en equipos con poco espacio.", b="Samuel Rubio: ")
c.p("Lo más útil fue comparar la app en dos versiones de Android. El APK original ni siquiera se instalaba en Android 8; con los cambios, el mismo APK funcionó en los dos. Al comparar las capturas vimos problemas reales para el usuario, como el botón de emergencia tapado y la voz que necesitaba internet, algo importante para un adulto mayor sin datos. Entendí que no era necesario bajar versiones de bibliotecas, porque ya soportaban Android 8, y que hacerlo habría traído riesgos de seguridad. Lo único que no pudimos cerrar fue el login, porque depende de la configuración real de Firebase.", b="Stephanie Gurrero: ")

d.save(OUT)
print("ok", OUT)
