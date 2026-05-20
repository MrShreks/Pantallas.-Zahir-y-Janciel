<div align="center">
  <img src="Pantallas/src/main/resources/com/example/pantallas/logoqueseria.png" alt="Quesería Santiaguero Logo" width="120"/>
  <h1 align="center">🧀 Fábrica de Queso RD</h1>
  <p align="center"><strong>Sistema de Gestión Integral para Quesería Santiaguero</strong></p>
  <p align="center">
    <img src="https://img.shields.io/badge/Java-24-ED8B00?style=flat&logo=openjdk&logoColor=white"/>
    <img src="https://img.shields.io/badge/JavaFX-21.0.6-007396?style=flat&logo=java&logoColor=white"/>
    <img src="https://img.shields.io/badge/Maven-3.8.5-C71A36?style=flat&logo=apache-maven&logoColor=white"/>
    <img src="https://img.shields.io/badge/SQL%20Server-2019-CC2927?style=flat&logo=microsoft-sql-server&logoColor=white"/>
    <img src="https://img.shields.io/badge/JasperReports-6.21.3-2C8EBB?style=flat"/>
    <img src="https://img.shields.io/badge/license-MIT-green"/>
  </p>
</div>

---

## 📋 Tabla de Contenido

- [1. Resumen Ejecutivo](#1-resumen-ejecutivo)
- [2. Análisis del Problema](#2-análisis-del-problema)
- [3. Propuesta de Solución](#3-propuesta-de-solución)
- [4. Arquitectura del Sistema](#4-arquitectura-del-sistema)
- [5. Módulos del Sistema](#5-módulos-del-sistema)
- [6. Stack Tecnológico](#6-stack-tecnológico)
- [7. Base de Datos](#7-base-de-datos)
- [8. Instalación y Configuración](#8-instalación-y-configuración)
- [9. Plan de Implementación](#9-plan-de-implementación)
- [10. Equipo](#10-equipo)

---

## 1. Resumen Ejecutivo

**Fábrica de Queso** es un sistema de gestión empresarial desarrollado en JavaFX. La aplicación automatiza y digitaliza la totalidad del ciclo operativo: desde la recepción de leche cruda, pasando por los procesos de producción y maduración, hasta la venta, distribución y generación de reportes gerenciales.

El proyecto se encuentra en etapa de desarrollo activo con una arquitectura modular que abarca **6 procesos de negocio**, **17 pantallas FXML**, **más de 50 clases Java** y una base de datos SQL Server con **más de 50 tablas**.

---

## 2. Análisis del Problema

### 2.1 Contexto Actual

Nuestra Fabrica de Queso opera actualmente sus procesos de forma manual o con herramientas ofimáticas básicas, lo que genera las siguientes problemáticas:

| Problema | Impacto |
|----------|---------|
| **Registro manual de producción** | Pérdida de trazabilidad en lotes, difícil control de calidad |
| **Inventario descentralizado** | Desabastecimiento o excedentes de materia prima |
| **Facturación manual** | Errores de cálculo, evasión fiscal involuntaria |
| **Sin control de roles** | Acceso irrestricto a información sensible |
| **Reportes inexistentes** | Toma de decisiones basada en corazonadas |
| **Distribución sin seguimiento** | Pérdida de productos perecederos en tránsito |

### 2.2 Requerimientos Identificados

- **Funcionales:** Gestión de compras, ventas POS, inventario kardex, producción con BOM, distribución con rutas, mantenimiento de equipos, dashboard ejecutivo, reportes JasperReports.
- **No funcionales:** Autenticación con RBAC, interfaz intuitiva, escalabilidad, disponibilidad local, soporte moneda DOP con ITBIS 18%/16%.

---

## 3. Propuesta de Solución

### 3.1 Visión

> *"Digitalizar y optimizar la operación completa de la Quesería Santiaguero mediante un sistema de escritorio moderno, seguro e intuitivo, que centralice la información y provea inteligencia de negocio para la toma de decisiones."*

### 3.2 Objetivos

| # | Objetivo | Indicador |
|---|----------|-----------|
| 1 | Automatizar el 100% de los procesos operativos | Módulos implementados / 6 |
| 2 | Reducir errores de facturación a < 1% | Tasa de error en ventas |
| 3 | Trazabilidad completa de lotes de producción | % de lotes con historia completa |
| 4 | Reportes gerenciales disponibles en < 1 min | Tiempo de generación de reportes |
| 5 | Control de acceso por roles | Usuarios con permisos asignados / total |

### 3.3 Valor Diferencial

- **Interfaz moderna** con JavaFX y BootstrapFX (animaciones, transiciones suaves)
- **Punto de venta inteligente** con cálculo automático de ITBIS (18% general / 16% reducido)
- **Dashboard en tiempo real** con KPIs del negocio
- **Reportes profesional**es con JasperReports (PDF exportable)
- **Arquitectura preparada** para migración a microservicios

---

## 4. Arquitectura del Sistema

### 4.3 Patrones de Diseño

| Patrón | Implementación |
|--------|---------------|
| **MVC** | FXML (View) + Controller (Control) + Model (dominio/) |
| **Singleton** | `FabricaBase` — única instancia de conexión a BD |
| **Factory** | `FabricaBase.abrirConexion()` — crea conexiones JDBC |
| **Repository** | Servicios (`ServicioInventario`, `ServicioVentas`) encapsulan acceso a datos |
| **Facade** | `ReporteHelper` — simplifica la generación de reportes |
| **Session** | `SessionManager` — mantiene contexto de usuario autenticado |

### 4.4 Manejo Modular (Java Platform Module System)

El proyecto utiliza **JPMS** (Java 24) con un módulo `com.example.pantallas` que declara explícitamente sus dependencias:

## 5. Módulos del Sistema

### 5.1 Módulo de Compras (`ProcesoDeCompras/`)

| Funcionalidad | Estado |
|--------------|--------|
| Órdenes de compra a proveedores | ✅ Implementado |
| Catálogo de proveedores (suplidores) | ✅ Implementado |
| Recepción de insumos con actualización de inventario | ✅ Implementado |
| Historial de compras con filtros | ✅ Implementado |
| Factores de conversión de unidades | ✅ Implementado |

**Clases principales:** `CompraController.java`, `MainCompra.java`, `OrdenCompra.java`

---

### 5.2 Módulo de Ventas (`ProcesoDeVenta/`)

| Funcionalidad | Estado |
|--------------|--------|
| Punto de venta (POS) | ✅ Implementado |
| Alternancia Libras/Kilogramos | ✅ Implementado |
| Cálculo automático de ITBIS (18%/16%) | ✅ Implementado |
| Facturación y cobros multi-método (efectivo/tarjeta/transferencia) | ✅ Implementado |
| Gestión de clientes | ✅ Implementado |
| Precios dinámicos por producto | ✅ Implementado |

**Clases principales:** `VentaController.java`, `PuntoVentaController.java`, `ServicioVentas.java`

---

### 5.3 Módulo de Inventario (`ProcesoDeInventario/`)

| Funcionalidad | Estado |
|--------------|--------|
| Control de stock por producto | ✅ Implementado |
| Movimientos kardex (entrada/salida/ajuste) | ✅ Implementado |
| Alertas de stock mínimo | ✅ Implementado |
| Detección inteligente de unidad de medida | ✅ Implementado |
| Dashboard de estadísticas de inventario | ✅ Implementado |

**Clases principales:** `InventarioController.java`, `ServicioInventario.java`, `MovimientoInventario.java`

---

### 5.4 Módulo de Producción (`ProcesoDeProduccion/`)

| Funcionalidad | Estado |
|--------------|--------|
| Recepción y registro de leche cruda | ✅ Implementado |
| Órdenes de producción con recetas (BOM) | ✅ Implementado |
| Consumo automático de materia prima | ✅ Implementado |
| Control de calidad (aprobación/rechazo) | ✅ Implementado |
| Seguimiento de lotes por lote | ✅ Implementado |

**Procesos productivos modelados:**

```
Recepción Leche → Pasteurización → Coagulación → Corte Cuajada
→ Fermentación → Prensado → Salado → Maduración (Cuevas)
→ Cheddarización → Corte/Empaque → Producto Terminado
```

**Clases principales:** `ProduccionController.java`, `LoteController.java`, `OrdenProduccion.java`

---

### 5.5 Módulo de Distribución (`ProcesoDeDistribucion/`)

| Funcionalidad | Estado |
|--------------|--------|
| Registro de despachos | ✅ Implementado |
| Gestión de flota de vehículos | ✅ Implementado |
| Confirmación de entregas | ✅ Implementado |
| Gestión de transportistas | ✅ Implementado |

**Clases principales:** `DistribucionController.java`, `Envios.java`

---

### 5.6 Módulo de Mantenimiento (`ProcesoDeMantenimiento/`)

| Funcionalidad | Estado |
|--------------|--------|
| Registro de equipos y maquinaria | ✅ Implementado |
| Mantenimiento preventivo y correctivo | ✅ Implementado |
| Historial de intervenciones | ✅ Implementado |
| Programación de mantenimientos | ✅ Implementado |

**Clases principales:** `MantenimientoController.java`, `Equipos.java`, `Historial_mantenimiento.java`

---

### 5.7 Dashboard y Reportes

| Funcionalidad | Estado |
|--------------|--------|
| Dashboard con KPIs en tiempo real | ✅ Implementado |
| Reportes JasperReports (PDF) | ✅ Implementado |
| Reporte de ejemplo (proveedores) | ✅ Implementado |
| Reportes personalizados por módulo | 🔄 Pendiente |

**Indicadores del Dashboard:**
- Valor total del inventario
- Producción activa (lotes en curso)
- Ventas del mes
- Alertas de stock bajo
- Últimos movimientos de inventario

---

### 5.8 Seguridad y Autenticación

| Funcionalidad | Estado |
|--------------|--------|
| Login con validación contra BD | ✅ Implementado |
| Roles de usuario (ADMIN, VENDEDOR, INVENTARIO, SUPERVISOR, CAJERO) | ✅ Implementado |
| Menú contextual por rol | ✅ Implementado |
| Sesión de usuario con `SessionManager` | ✅ Implementado |
| Hash de contraseñas (BCrypt) | ✅ Preparado |
| Auditoría de acceso | 🔄 Pendiente |

---

## 6. Stack Tecnológico

### 6.1 Lenguajes y Frameworks

| Tecnología | Versión | Propósito |
|------------|---------|-----------|
| Java | 24 | Lenguaje principal |
| JavaFX | 21.0.6 | UI/UX de escritorio |
| FXML | — | Definición de vistas |
| CSS3 | — | Estilos y temas visuales |
| Maven | 3.8.5 | Construcción y dependencias |

### 6.2 Base de Datos

| Componente | Versión |
|------------|---------|
| Microsoft SQL Server | 2019+ |
| JDBC Driver | 12.4.2.jre11 |
| Esquema actual | `fabricadequeso` (~60 tablas) |
| Esquema objetivo | `FabricaQuesoRD` (normalizado) |

### 6.3 Librerías y Dependencias

| Librería | Versión | Uso |
|----------|---------|-----|
| JasperReports | 6.21.3 | Reportes PDF |
| BootstrapFX | 0.4.0 | Estilos CSS modernos |
| FormsFX | 11.6.0 | Formularios avanzados |
| Jackson | 2.15.3 | Procesamiento JSON/XML |
| OpenPDF | 1.3.32 | Renderizado PDF |
| JUnit Jupiter | 5.12.1 | Pruebas unitarias |
| Commons Collections4 | 4.2 | Utilidades de colecciones |
| Commons BeanUtils | 1.9.4 | Reflexión de beans |
| JFreeChart | 1.0.19 | Gráficos y visualizaciones |

### 6.4 Herramientas de Desarrollo

| Herramienta | Uso |
|-------------|-----|
| IntelliJ IDEA | IDE principal |
| Scene Builder | Editor visual FXML |
| SQL Server Management Studio | Administración BD |
| JasperSoft Studio | Diseño de reportes .jrxml |
| Git + GitHub | Control de versiones |

---

## 7. Base de Datos

### 7.1 Esquema Actual (`fabricadequeso`)

La base de datos activa (`quesito.txt`) contiene más de **60 tablas** organizadas por proceso:

```
┌──────────────────────────────────────────────────┐
│              FABRICADEQUESO                       │
├──────────────────────────────────────────────────┤
│ SEGURIDAD                                        │
│  ├── Usuarios, Roles, Permisos, Rol_Permisos     │
├──────────────────────────────────────────────────┤
│ PRODUCCIÓN                                       │
│  ├── tbl_lotes_produccion, tbl_pasteurizacion,   │
│  ├── tbl_coagulacion, tbl_corte_cuajada,         │
│  ├── tbl_fermentacion, tbl_prensado,             │
│  ├── tbl_salado, tbl_maduracion,                 │
│  ├── tbl_cheddarizacion, tbl_corte_empaque       │
├──────────────────────────────────────────────────┤
│ INVENTARIO                                       │
│  ├── tbl_inventario_productos, tbl_kardex,       │
│  ├── tbl_movimientos_inventario, tbl_ajustes     │
├──────────────────────────────────────────────────┤
│ COMERCIAL                                        │
│  ├── tbl_clientes, tbl_ventas, tbl_detalle_venta,│
│  ├── tbl_facturas, tbl_productos                 │
├──────────────────────────────────────────────────┤
│ COMPRAS                                          │
│  ├── tbl_suplidores, tbl_ordenes_compra,         │
│  ├── tbl_insumos, tbl_factores_conversion        │
├──────────────────────────────────────────────────┤
│ DISTRIBUCIÓN                                     │
│  ├── tbl_envios, tbl_despachos, tbl_vehiculos,   │
│  ├── tbl_transportistas                          │
├──────────────────────────────────────────────────┤
│ MANTENIMIENTO                                    │
│  ├── tbl_equipos, tbl_mantenimiento,             │
│  ├── tbl_historial_mantenimiento                 │
└──────────────────────────────────────────────────┘
```

### 7.2 Esquema Refactorizado (`FabricaQuesoRD`)

Disponible en `ESQUEMA_BASE_DATOS.sql` — esquema normalizado con:
- 15+ tablas con claves foráneas y constraints
- RBAC completo (5 roles, 15+ permisos)
- Vistas SQL para reportes (`vista_productos_precios`, `vista_ventas_detalladas`)
- Stored procedure transaccional `sp_registrar_venta`
- Datos semilla (usuario admin, productos, clientes, proveedores)
- Índices de rendimiento

### 7.3 Conexión

| Parámetro | Valor |
|-----------|-------|
| Host | `localhost:1433` |
| Base de datos | `fabricadequeso` |
| Usuario | `sa` |
| Autenticación | SQL Server |
| SSL | `trustServerCertificate=true;encrypt=false` |

---

## 8. Instalación y Configuración

### 8.1 Prerrequisitos

- **Java JDK 24** (o superior)
- **Maven 3.8+** (wrapper incluido)
- **SQL Server 2019+** (local o remoto)
- **IntelliJ IDEA** (recomendado)

### 8.2 Configuración de Base de Datos

```sql
-- 1. Restaurar la base de datos desde el script
-- Ejecutar ESQUEMA_BASE_DATOS.sql en SSMS

-- 2. O usar la base de datos existente
-- La aplicación se conecta automáticamente a:
-- jdbc:sqlserver://localhost:1433;databaseName=fabricadequeso
```

### 8.3 Compilación y Ejecución

```bash
# Compilar el proyecto
cd Pantallas/
mvn clean compile

# Ejecutar la aplicación
mvn javafx:run

# Generar JAR ejecutable
mvn package
```

### 8.4 Configuración en IntelliJ IDEA

1. Abrir el proyecto desde `Pantalllas/`
2. Configurar JDK 24 en `File → Project Structure → SDK`
3. Agregar JavaFX SDK si es necesario:
   ```
   VM options: --module-path "ruta/javafx-sdk-21.0.6/lib" --add-modules javafx.controls,javafx.fxml
   ```
4. Ejecutar `Pantallas/src/main/java/com/example/pantallas/Launcher.java`

### 8.5 Credenciales por Defecto

| Usuario | Contraseña | Rol |
|---------|-----------|-----|
| `admin` | `admin123` | Administrador |
| `empleado` | `emp123` | Empleado |

---

## 9. Plan de Implementación

El proyecto se desarrolla en **9 sprints** con entregas incrementales:

| Sprint | Período | Enfoque |
|--------|---------|---------|
| **Sprint 1** | Sem 1-2 | Fundación: BD, Login, Seguridad, Navegación |
| **Sprint 2** | Sem 3-4 | Módulo de Producción |
| **Sprint 3** | Sem 5-6 | Módulo de Inventario |
| **Sprint 4** | Sem 7-8 | Módulo de Compras |
| **Sprint 5** | Sem 9-10 | Módulo de Ventas |
| **Sprint 6** | Sem 11-12 | Módulo de Distribución |
| **Sprint 7** | Sem 13-14 | Módulo de Mantenimiento |
| **Sprint 8** | Sem 15-16 | Dashboard y Reportes |
| **Sprint 9** | Sem 17-18 | Pruebas, Despliegue y Documentación |

> 📋 El detalle completo con issues asignados está disponible en los [Milestones](https://github.com/MrShreks/Pantallas.-Zahir-y-Janciel/milestones) del repositorio.

---

## 10. Equipo

| Rol | Nombre |
|-----|--------|
| **Desarrollador Full Stack** | Janciel Eusebio |
| **Desarrollador Full Stack** | Zahir Jiménez |

### Agradecimientos

- A la **Quesería Santiaguero** por proporcionar el contexto y requisitos del negocio.
- A **JasperSoft** por su potente motor de reportes.
- A la comunidad **OpenJFX** por el framework de interfaz gráfica.

---

<div align="center">
  <p>
    <strong>Quesería Santiaguero</strong> — Tradición y Calidad desde República Dominicana 🇩🇴
  </p>
  <p>
    <sub>© 2026 — Proyecto Académico</sub>
  </p>
</div>
