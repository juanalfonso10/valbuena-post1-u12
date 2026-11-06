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
