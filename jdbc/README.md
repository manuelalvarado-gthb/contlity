# Contlity JDBC

Driver JDBC de solo lectura para Contlity. Funciona en DBeaver, DataGrip, Tableau (Otras bases de
datos – JDBC), SQuirreL, sqlline y cualquier programa Java 11+. No tiene dependencias.

Como los drivers de Snowflake o Athena, habla con Contlity por HTTPS: no hay puertos que abrir. Cada
consulta corre en el motor de Contlity con los permisos, la cartera de clientes y los créditos del
usuario de la API key.

## Descarga

`contlity-jdbc.jar` del [último release](https://github.com/manuelalvarado-gthb/contlity/releases/latest/download/contlity-jdbc.jar).

```
java -jar contlity-jdbc.jar      # muestra la versión
```

## Conexión

| Campo | Valor |
|---|---|
| Clase del driver | `cloud.techmx.contlity.jdbc.ContlityDriver` |
| URL | `jdbc:contlity:///despacho` (o `jdbc:contlity:///despacho?schema=ventas`) |
| Usuario | cualquiera |
| Contraseña | una API key con el alcance **SQL** (`ctly_live_...`) |

Parámetros opcionales de la URL: `schema` (esquema en uso), `pageSize` (filas por página, 100–100000,
por omisión 10000) y `timeout` (segundos por consulta, por omisión 120).

## Qué verás

- **Catálogos** = bases de datos de la cuenta; **esquemas** = esquemas de cada base.
- Las vistas del núcleo (comprobantes, conceptos, impuestos, …) están en el esquema `contlity` de cada
  base: `contlity.comprobantes`, `despacho.contlity.comprobantes` o solo `comprobantes`.
- Los resultados grandes llegan por páginas mientras los recorres; la memoria no crece con el tamaño
  del resultado.

## Límites

Solo lectura (SELECT, WITH, TABLE, VALUES, EXPLAIN). Las consultas cuentan para el límite por hora y
consumen créditos como en Worksheets; explorar tablas y columnas no cuesta. Guía completa:
https://contlity.tech-mx.cloud/docs/sdk
