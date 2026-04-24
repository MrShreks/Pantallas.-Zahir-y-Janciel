# Estructura Profesional del Proyecto - Fábrica de Queso RD

## 📁 Estructura de Carpetas Recomendada (MVC)

```
src/main/java/com/fabricaqueso/
├── config/
│   ├── DatabaseConfig.java           # Configuración de conexión a BD
│   ├── AppConfig.java                # Configuración de la aplicación
│   └── SecurityConfig.java           # Configuración de seguridad y RBAC
├── model/
│   ├── entities/                     # Entidades de base de datos
│   │   ├── Usuario.java
│   │   ├── Rol.java
│   │   ├── Permiso.java
│   │   ├── Cliente.java
│   │   ├── Producto.java
│   │   ├── Proveedor.java
│   │   ├── PrecioProveedor.java
│   │   ├── Venta.java
│   │   ├── DetalleVenta.java
│   │   └── Inventario.java
│   ├── dto/                          # Data Transfer Objects
│   │   ├── VentaDTO.java
│   │   ├── ProductoDTO.java
│   │   └── UsuarioDTO.java
│   └── enums/                        # Enumeraciones
│       ├── RolEnum.java
│       ├── MetodoPagoEnum.java
│       └── EstatusVentaEnum.java
├── repository/
│   ├── interfaces/                   # Interfaces de repositorio
│   │   ├── UsuarioRepository.java
│   │   ├── ProductoRepository.java
│   │   ├── VentaRepository.java
│   │   └── ProveedorRepository.java
│   └── implementations/              # Implementaciones JDBC
│       ├── UsuarioRepositoryImpl.java
│       ├── ProductoRepositoryImpl.java
│       └── VentaRepositoryImpl.java
├── service/
│   ├── interfaces/                   # Interfaces de lógica de negocio
│   │   ├── AuthService.java
│   │   ├── VentaService.java
│   │   ├── ProductoService.java
│   │   ├── PrecioService.java
│   │   ├── InventarioService.java
│   │   └── ReporteService.java
│   └── implementations/              # Implementaciones de servicios
│       ├── AuthServiceImpl.java
│       ├── VentaServiceImpl.java
│       ├── ProductoServiceImpl.java
│       ├── PrecioServiceImpl.java
│       └── InventarioServiceImpl.java
├── controller/
│   ├── auth/
│   │   ├── LoginController.java
│   │   └── SessionManager.java
│   ├── ventas/
│   │   ├── VentaController.java
│   │   ├── PuntoVentaController.java
│   │   └── CobroController.java
│   ├── inventario/
│   │   ├── InventarioController.java
│   │   └── StockController.java
│   ├── compras/
│   │   ├── CompraController.java
│   │   └── ProveedorController.java
│   ├── reportes/
│   │   └── ReporteController.java
│   └── common/
│       ├── BaseController.java
│       ├── NavigationController.java
│       └── AlertManager.java
├── util/
│   ├── DatabaseUtil.java
│   ├── CurrencyFormatter.java        # Formato para Pesos Dominicanos
│   ├── ITBISCalculator.java           # Cálculo de impuestos RD
│   ├── ValidationUtil.java
│   └── SecurityUtil.java
└── MainApplication.java               # Clase principal

src/main/resources/
├── css/
│   ├── global.css                    # Estilos globales unificados
│   ├── components/
│   │   ├── buttons.css
│   │   ├── forms.css
│   │   ├── tables.css
│   │   └── modals.css
│   └── themes/
│       ├── light-theme.css
│       └── dark-theme.css
├── fxml/
│   ├── auth/
│   │   └── login.fxml
│   ├── main/
│   │   ├── MenuPrincipal.fxml
│   │   └── Dashboard.fxml
│   ├── ventas/
│   │   ├── PuntoVenta.fxml
│   │   ├── Cobro.fxml
│   │   └── HistorialVentas.fxml
│   ├── inventario/
│   │   ├── GestionInventario.fxml
│   │   └── MovimientosStock.fxml
│   ├── compras/
│   │   ├── GestionCompras.fxml
│   │   └── GestionProveedores.fxml
│   └── common/
│       ├── Header.fxml
│       ├── Sidebar.fxml
│       └── ModalConfirmacion.fxml
├── images/
│   ├── icons/
│   └── logos/
└── properties/
    ├── database.properties
    ├── app.properties
    └── messages.properties          # Internacionalización
```

## 🎯 Principios de Arquitectura

1. **Separación de Responsabilidades**: Cada capa tiene una responsabilidad específica
2. **Inyección de Dependencias**: Los controllers reciben sus servicios por constructor
3. **Patrón Repository**: Abstracción del acceso a datos
4. **Patrón DTO**: Transferencia de datos entre capas
5. **Seguridad por Roles**: RBAC implementado a nivel de servicio y UI
6. **Configuración Centralizada**: Properties externos para conexión y configuración

## 🔐 Sistema de RBAC

### Roles Definidos:
- **ADMINISTRADOR**: Acceso completo a todos los módulos
- **VENDEDOR**: Solo módulo de ventas y consulta de inventario
- **INVENTARIO**: Gestión de stock y compras, sin acceso a ventas
- **SUPERVISOR**: Consulta de reportes y supervisión de operaciones
- **CAJERO**: Solo proceso de cobro y cierre de caja

### Permisos por Módulo:
- **VENTAS**: CREAR_VENTA, EDITAR_VENTA, ANULAR_VENTA, VER_REPORTES_VENTAS
- **INVENTARIO**: VER_STOCK, ACTUALIZAR_STOCK, CREAR_PRODUCTO, ELIMINAR_PRODUCTO
- **COMPRAS**: CREAR_COMPRA, EDITAR_COMPRA, GESTIONAR_PROVEEDORES
- **REPORTES**: VER_REPORTES_VENTAS, VER_REPORTES_INVENTARIO, EXPORTAR_DATOS
- **SISTEMA**: GESTIONAR_USUARIOS, CONFIGURAR_SISTEMA

## 💱 Localización República Dominicana

### Moneda:
- **Código**: DOP (Pesos Dominicanos)
- **Símbolo**: RD$
- **Formato**: RD$ 1,234.56

### Impuestos (ITBIS):
- **ITBIS General**: 18%
- **ITBIS Reducido**: 16% (productos específicos)
- **Exento**: 0% (productos básicos)

### Configuración Regional:
- **Idioma**: Español (República Dominicana)
- **Formato Fecha**: DD/MM/YYYY
- **Separador Decimal**: .
- **Separador Miles**: ,
