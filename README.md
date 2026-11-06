# Sistema de Solicitudes de Reembolso de Gastos Corporativos

Proyecto integrador de la Unidad 12 de Patrones de Diseño de Software. El área de Finanzas
reemplaza una hoja de cálculo compartida por un sistema propio: los empleados registran
solicitudes de reembolso (categoría, monto, fecha y documentos soporte), el sistema las
valida, calcula el monto aprobado y avisa a los interesados cuando cambian de estado.

## Diseño Propuesto

Esta sección se escribió antes de implementar cualquier patrón (primer commit del
repositorio). Para cada requisito se identificó qué varía, qué patrón lo resuelve y por qué
ese y no otro, aplicando el test de explicabilidad de la Guía (sección 1.2): si la razón no
cabe en una oración, el patrón probablemente sobra.

| Requisito | Qué varía (punto de variabilidad) | Patrón | Justificación en una oración |
|---|---|---|---|
| **RF1** Validación en varias etapas | El conjunto de verificaciones que debe pasar una solicitud, que hoy son tres y Finanzas ya anunció que crecerá | **Chain of Responsibility** | Cada verificación es un eslabón independiente que aprueba y delega al siguiente o rechaza y corta la cadena, así que una verificación nueva es un eslabón nuevo sin tocar los existentes. |
| **RF2** Cálculo por categoría | La fórmula del monto aprobado, distinta para cada categoría (viaje, representación, capacitación) y con categorías nuevas en camino | **Strategy** | Cada categoría tiene una fórmula intercambiable que se elige una sola vez por el valor de la categoría, así que una categoría nueva es una clase nueva sin modificar el cálculo de las demás. |
| **RF3** Notificación a varios interesados | Quiénes se enteran de un cambio de estado y cómo reacciona cada uno (empleado, Contabilidad, auditoría, y pronto un panel de métricas) | **Observer** | El servicio publica el cambio de estado a una lista de observadores que no conoce en detalle, y cada uno decide su reacción, así que un interesado nuevo es un observador más. |

**Por qué no otros patrones para el mismo requisito:**

- **RF1 no es Strategy.** No se elige *una* verificación: deben pasar *todas*, en secuencia,
  y la primera que falla decide el resultado. Esa es la forma de Chain of Responsibility.
- **RF2 no es State.** La categoría de una solicitud no cambia con el tiempo ni tiene
  transiciones; es un valor discreto que selecciona una fórmula. Tampoco es Chain of
  Responsibility, porque exactamente una fórmula aplica y no importa el orden.
- **RF3 no es Chain of Responsibility.** Ante un cambio de estado no basta con que *un*
  interesado lo maneje: deben enterarse *todos*, cada uno a su manera.

**Detalles de diseño previstos:**

- La cadena de RF1 tendrá un eslabón inicial que verifique que la solicitud trae los datos
  obligatorios (empleado, categoría, monto positivo y fecha), para que ningún validador
  posterior reciba valores nulos.
- La clase base de la cadena usará un Template Method interno (`validar` fija el recorrido
  y cada eslabón implementa solo `validarPropio`). No es un requisito de diseño aparte, sino
  la forma de no repetir la lógica de delegación en cada eslabón.
- El dominio (`SolicitudReembolso`, `CategoriaGasto`, `EstadoSolicitud`) no dependerá de
  Spring ni de JPA. Los patrones vivirán en `procesamiento/` y solo el servicio de
  aplicación conocerá la persistencia.

## Arquitectura resultante

```
com.empresa.reembolsos
├── dominio/             SolicitudReembolso, CategoriaGasto, EstadoSolicitud   (Java puro: sin Spring ni JPA)
├── procesamiento/
│   ├── validacion/      Chain of Responsibility: ValidadorSolicitud + 4 eslabones, CadenaValidadores
│   ├── calculo/         Strategy: CalculadoraReembolso + 3 calculadoras, SelectorCalculadora
│   └── notificacion/    Observer: ObservadorCambioEstado + 3 notificadores
├── aplicacion/          SolicitudReembolsoService (coordina los patrones), excepciones de aplicación
├── infraestructura/     RepositorioSolicitudesJpa (Spring Data JPA)
└── adaptadores/rest/    SolicitudReembolsoController (POST /api/solicitudes, PUT /api/solicitudes/{id}/estado)
src/main/resources/META-INF/orm.xml   mapeo JPA de SolicitudReembolso
```

Las dependencias van en un solo sentido: `adaptadores → aplicacion → procesamiento → dominio`, y
`aplicacion → infraestructura → dominio`. `procesamiento` nunca toca la persistencia:
`ValidadorDuplicados` recibe la lista de solicitudes pendientes ya cargada por el servicio.

**El dominio sin JPA y el repositorio con JPA.** El enunciado pide dos cosas que a primera vista
chocan: que el modelo de dominio no importe nada de Spring ni de JPA, y que
`RepositorioSolicitudesJpa` extienda `JpaRepository<SolicitudReembolso, Long>`, lo que exige que
`SolicitudReembolso` sea una entidad. Se resolvió con el mapeo XML estándar de JPA:
`META-INF/orm.xml` declara la entidad, su tabla, el id autogenerado y los enums como texto, y
`spring.jpa.mapping-resources` lo carga. La clase de dominio queda como Java puro y el
repositorio sigue siendo un `JpaRepository` directo, sin puertos ni mappers.

## Pruebas (Parte 1)

`./mvnw test` ejecuta 37 pruebas:

| Clase de prueba | Casos | Qué cubre |
|---|---|---|
| `ValidadorDatosCompletosTest` | 3 | Solicitud completa, campos faltantes nombrados en el mensaje, monto cero |
| `ValidadorDocumentosSoporteTest` | 2 | Con y sin documentos (mensaje claro) |
| `ValidadorMontoMaximoTest` | 4 | Justo en el tope, sobre el tope, tope de viaje por días, viaje sin días |
| `ValidadorDuplicadosTest` | 3 | Mismo mes y categoría, mismo mes de otro año, otra categoría |
| `CadenaValidadoresTest` | 3 | Todo aprobado, primera falla según el orden, corte antes de datos nulos |
| `CalculadoraViajeTest` / `CalculadoraRepresentacionTest` / `CalculadoraCapacitacionTest` | 2 c/u | Fórmula de la categoría y su tope |
| `SelectorCalculadoraTest` | 3 | Selección por categoría, categoría sin calculadora, categoría duplicada |
| `NotificadoresTest` | 4 | Contabilidad solo ante APROBADA; empleado y auditoría ante cualquier cambio |
| `SolicitudReembolsoServiceTest` | 4 | Crear calcula y no sobrescribe; inválida no guarda; cambio de estado notifica a todos; inexistente no notifica |
| `FlujoSolicitudReembolsoIntegracionTest` (`@SpringBootTest`) | 5 | API REST real con H2: las 3 categorías, notificación según el estado, 400 sin documentos, 400 por duplicado, 400 por tope, 404 |

## Decisiones de diseño (Parte 1)

**Decisión de diseño 1 — orden de la cadena: datos completos → documentos → monto →
duplicados.** El criterio es costo creciente, porque el primer rechazo corta la cadena. Las
verificaciones que solo miran la propia solicitud son constantes y baratas (comparar nulos, un
booleano, un tope). `ValidadorDuplicados` es la única que recorre la lista de solicitudes
pendientes del empleado, así que va al final: solo se ejecuta para las solicitudes que ya pasaron
todo lo demás.

- **Lo que se gana:** una solicitud sin documentos o fuera de tope se rechaza sin recorrer
  ninguna lista. Además, `ValidadorDatosCompletos` va primero por una razón más fuerte que el
  costo: los demás eslabones asumen categoría, monto y fecha no nulos, y ponerlo al inicio
  garantiza que nunca reciban un nulo.
- **Lo que se pierde:** el empleado ve un solo motivo a la vez. Una solicitud duplicada y sin
  documentos se rechaza primero por documentos; al corregirlo, recién se entera de que estaba
  duplicada. Con cualquier orden pasaría lo mismo con alguna combinación, porque la cadena corta
  en el primer rechazo.
- **Matiz honesto:** el ahorro es en la comparación, no en la consulta. El servicio carga las
  pendientes del empleado antes de recorrer la cadena, así que esa consulta a la base de datos
  se paga siempre. Diferirla exigiría pasar un `Supplier` a la cadena; no se hizo porque cada
  empleado tiene pocas solicitudes pendientes (ver "Qué se descartó y por qué").

**Decisión de diseño 2 — selección de la calculadora con un mapa construido desde
`List<CalculadoraReembolso>`, no con un `switch`.** RF2 exige agregar categorías sin modificar el
cálculo existente. Con el mapa, una categoría nueva es una clase nueva anotada con `@Component`:
Spring la inyecta en la lista y `SelectorCalculadora` la registra sola, sin tocar una línea de
las calculadoras ni del selector. Con un `switch`, cada categoría nueva obligaría a editar el
método de selección, que es justamente la clase de cambio que RF2 quiere evitar. El mapa además
falla al arrancar si dos calculadoras declaran la misma categoría (`IllegalStateException`), en
lugar de elegir una en silencio.

Siendo precisos con el alcance: una categoría nueva ("bienestar") también requiere agregar la
constante a `CategoriaGasto` y su tope a `ValidadorMontoMaximo`. Ninguno de los dos cambios toca
el cálculo de las categorías existentes, que es lo que pide RF2. Si se olvida el tope, el
validador rechaza la solicitud con "No hay un tope configurado para la categoria BIENESTAR" en
lugar de fallar con un error interno.

## Ajustes respecto al código del enunciado

- **Tope de VIAJE por días en `ValidadorMontoMaximo`.** El enunciado comparaba cualquier viaje
  contra 150 000 fijos, pero RF2 define el tope de viáticos como diario × días. Así, un viaje de
  3 días por 300 000 se rechazaba aunque su tope según RF2 es 450 000. Ahora el límite de VIAJE
  es 150 000 × días, el mismo que aplica `CalculadoraViaje`; los de las otras categorías siguen
  fijos.
- **Período de `ValidadorDuplicados` con mes y año.** Comparaba solo el mes, así que una
  solicitud pendiente de enero de 2025 bloqueaba una de enero de 2026.
- **`ValidadorDatosCompletos` como primer eslabón.** Sin él, una solicitud sin categoría o sin
  fecha terminaba en un `NullPointerException` y un error 500. Agregarlo no exigió modificar
  ninguna verificación existente, que es justamente lo que RF1 promete.
- **Respuestas de error claras.** El controlador traduce `SolicitudInvalidaException` a 400 y
  `SolicitudNoEncontradaException` a 404, con `{"error": "<motivo>"}`. Sin esto, Spring Boot
  responde 500 y oculta el mensaje.
- **`crear` ignora el `id` del cuerpo.** Si el cliente enviaba un `id` existente, `save` habría
  sobrescrito esa solicitud.
- **Dominio sin JPA mediante `orm.xml`** (ver arriba), y **Spring Boot 3.5.16**, el último 3.x;
  Spring Initializr ya solo ofrece la línea 4.
