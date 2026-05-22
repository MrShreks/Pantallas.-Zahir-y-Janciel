-- ===================================================================
-- ESQUEMA DE BASE DE DATOS - FÁBRICA DE QUESO REPÚBLICA DOMINICANA
-- ===================================================================
-- Base de datos: SQL Server
-- Localización: República Dominicana (DOP, ITBIS 18%/16%)

-- Crear base de datos
IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = 'FabricaQuesoRD')
BEGIN
    CREATE DATABASE FabricaQuesoRD;
END
GO

USE FabricaQuesoRD;
GO

-- ===================================================================
-- TABLAS DE SEGURIDAD Y USUARIOS (RBAC)
-- ===================================================================

-- Tabla de Roles
CREATE TABLE Roles (
    id_rol INT PRIMARY KEY IDENTITY(1,1),
    nombre_rol VARCHAR(50) NOT NULL UNIQUE,
    descripcion VARCHAR(200),
    activo BIT DEFAULT 1,
    fecha_creacion DATETIME DEFAULT GETDATE()
);

-- Tabla de Permisos
CREATE TABLE Permisos (
    id_permiso INT PRIMARY KEY IDENTITY(1,1),
    nombre_permiso VARCHAR(100) NOT NULL UNIQUE,
    descripcion VARCHAR(300),
    modulo VARCHAR(50) NOT NULL,
    fecha_creacion DATETIME DEFAULT GETDATE()
);

-- Tabla de Roles-Permisos (Muchos a Muchos)
CREATE TABLE Rol_Permisos (
    id_rol INT NOT NULL,
    id_permiso INT NOT NULL,
    PRIMARY KEY (id_rol, id_permiso),
    FOREIGN KEY (id_rol) REFERENCES Roles(id_rol) ON DELETE CASCADE,
    FOREIGN KEY (id_permiso) REFERENCES Permisos(id_permiso) ON DELETE CASCADE
);

-- Tabla de Usuarios
CREATE TABLE Usuarios (
    id_usuario INT PRIMARY KEY IDENTITY(1,1),
    nombre_usuario VARCHAR(50) NOT NULL UNIQUE,
    contrasena VARCHAR(255) NOT NULL, -- Encriptada
    nombre_completo VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    telefono VARCHAR(20),
    id_rol INT NOT NULL,
    activo BIT DEFAULT 1,
    fecha_creacion DATETIME DEFAULT GETDATE(),
    ultimo_acceso DATETIME,
    FOREIGN KEY (id_rol) REFERENCES Roles(id_rol)
);

-- ===================================================================
-- TABLAS DE PROVEEDORES Y COMPRAS
-- ===================================================================

-- Tabla de Proveedores
CREATE TABLE Proveedores (
    id_proveedor INT PRIMARY KEY IDENTITY(1,1),
    nombre_proveedor VARCHAR(100) NOT NULL,
    rnc VARCHAR(11), -- Cédula/RNC Dominicano
    telefono VARCHAR(20),
    email VARCHAR(100),
    direccion VARCHAR(300),
    contacto_principal VARCHAR(100),
    activo BIT DEFAULT 1,
    fecha_creacion DATETIME DEFAULT GETDATE()
);

-- Tabla de Productos
CREATE TABLE Productos (
    id_producto INT PRIMARY KEY IDENTITY(1,1),
    codigo_producto VARCHAR(20) NOT NULL UNIQUE,
    nombre_producto VARCHAR(100) NOT NULL,
    descripcion VARCHAR(500),
    categoria VARCHAR(50),
    unidad_medida VARCHAR(20) DEFAULT 'LIBRAS',
    stock_actual DECIMAL(10,2) DEFAULT 0,
    stock_minimo DECIMAL(10,2) DEFAULT 0,
    precio_venta_base DECIMAL(10,2) NOT NULL,
    itbis_aplicable BIT DEFAULT 1,
    porcentaje_itbis DECIMAL(5,2) DEFAULT 18.00, -- 18% o 16%
    activo BIT DEFAULT 1,
    fecha_creacion DATETIME DEFAULT GETDATE(),
    fecha_ultima_actualizacion DATETIME DEFAULT GETDATE()
);

-- Tabla de Precios por Proveedor
CREATE TABLE Precios_Proveedores (
    id_precio_proveedor INT PRIMARY KEY IDENTITY(1,1),
    id_producto INT NOT NULL,
    id_proveedor INT NOT NULL,
    precio_compra DECIMAL(10,2) NOT NULL,
    fecha_vigencia DATETIME DEFAULT GETDATE(),
    activo BIT DEFAULT 1,
    FOREIGN KEY (id_producto) REFERENCES Productos(id_producto),
    FOREIGN KEY (id_proveedor) REFERENCES Proveedores(id_proveedor),
    UNIQUE (id_producto, id_proveedor, fecha_vigencia)
);

-- Tabla de Compras
CREATE TABLE Compras (
    id_compra INT PRIMARY KEY IDENTITY(1,1),
    numero_factura VARCHAR(50) UNIQUE,
    id_proveedor INT NOT NULL,
    id_usuario INT NOT NULL,
    fecha_compra DATETIME DEFAULT GETDATE(),
    subtotal DECIMAL(12,2) NOT NULL,
    itbis_total DECIMAL(12,2) DEFAULT 0,
    total_compra DECIMAL(12,2) NOT NULL,
    metodo_pago VARCHAR(50),
    estatus VARCHAR(20) DEFAULT 'PENDIENTE', -- PENDIENTE, PAGADA, CANCELADA
    observaciones VARCHAR(500),
    FOREIGN KEY (id_proveedor) REFERENCES Proveedores(id_proveedor),
    FOREIGN KEY (id_usuario) REFERENCES Usuarios(id_usuario)
);

-- Tabla de Detalle de Compras
CREATE TABLE Detalle_Compras (
    id_detalle_compra INT PRIMARY KEY IDENTITY(1,1),
    id_compra INT NOT NULL,
    id_producto INT NOT NULL,
    cantidad DECIMAL(10,2) NOT NULL,
    precio_unitario DECIMAL(10,2) NOT NULL,
    itbis_unitario DECIMAL(5,2) DEFAULT 18.00,
    subtotal DECIMAL(12,2) NOT NULL,
    FOREIGN KEY (id_compra) REFERENCES Compras(id_compra) ON DELETE CASCADE,
    FOREIGN KEY (id_producto) REFERENCES Productos(id_producto)
);

-- ===================================================================
-- TABLAS DE CLIENTES Y VENTAS
-- ===================================================================

-- Tabla de Clientes
CREATE TABLE Clientes (
    id_cliente INT PRIMARY KEY IDENTITY(1,1),
    nombre_cliente VARCHAR(100) NOT NULL,
    rnc VARCHAR(11), -- Cédula/RNC Dominicano
    telefono VARCHAR(20),
    email VARCHAR(100),
    direccion VARCHAR(300),
    tipo_cliente VARCHAR(20) DEFAULT 'GENERAL', -- GENERAL, MAYORISTA, MINORISTA
    limite_credito DECIMAL(12,2) DEFAULT 0,
    activo BIT DEFAULT 1,
    fecha_creacion DATETIME DEFAULT GETDATE()
);

-- Tabla de Ventas
CREATE TABLE Ventas (
    id_venta INT PRIMARY KEY IDENTITY(1,1),
    numero_factura VARCHAR(50) UNIQUE,
    id_cliente INT NOT NULL,
    id_usuario INT NOT NULL,
    fecha_venta DATETIME DEFAULT GETDATE(),
    subtotal DECIMAL(12,2) NOT NULL,
    itbis_total DECIMAL(12,2) DEFAULT 0,
    total_venta DECIMAL(12,2) NOT NULL,
    monto_efectivo DECIMAL(12,2) DEFAULT 0,
    monto_tarjeta DECIMAL(12,2) DEFAULT 0,
    monto_transferencia DECIMAL(12,2) DEFAULT 0,
    metodo_pago VARCHAR(50), -- EFECTIVO, TARJETA, TRANSFERENCIA, MIXTO
    estatus VARCHAR(20) DEFAULT 'ACTIVA', -- ACTIVA, ANULADA, PENDIENTE
    observaciones VARCHAR(500),
    FOREIGN KEY (id_cliente) REFERENCES Clientes(id_cliente),
    FOREIGN KEY (id_usuario) REFERENCES Usuarios(id_usuario)
);

-- Tabla de Detalle de Ventas
CREATE TABLE Detalle_Ventas (
    id_detalle_venta INT PRIMARY KEY IDENTITY(1,1),
    id_venta INT NOT NULL,
    id_producto INT NOT NULL,
    cantidad DECIMAL(10,2) NOT NULL,
    precio_unitario DECIMAL(10,2) NOT NULL, -- Se arrastra desde Productos
    itbis_unitario DECIMAL(5,2) DEFAULT 18.00, -- Se arrastra desde Productos
    subtotal DECIMAL(12,2) NOT NULL,
    FOREIGN KEY (id_venta) REFERENCES Ventas(id_venta) ON DELETE CASCADE,
    FOREIGN KEY (id_producto) REFERENCES Productos(id_producto)
);

-- ===================================================================
-- TABLAS DE INVENTARIO
-- ===================================================================

-- Tabla de Movimientos de Inventario
CREATE TABLE Movimientos_Inventario (
    id_movimiento INT PRIMARY KEY IDENTITY(1,1),
    id_producto INT NOT NULL,
    tipo_movimiento VARCHAR(20) NOT NULL, -- ENTRADA, SALIDA, AJUSTE
    cantidad DECIMAL(10,2) NOT NULL,
    stock_antes DECIMAL(10,2) NOT NULL,
    stock_despues DECIMAL(10,2) NOT NULL,
    referencia_id INT, -- ID de compra, venta o ajuste
    referencia_tipo VARCHAR(20), -- COMPRA, VENTA, AJUSTE
    id_usuario INT NOT NULL,
    fecha_movimiento DATETIME DEFAULT GETDATE(),
    observaciones VARCHAR(300),
    FOREIGN KEY (id_producto) REFERENCES Productos(id_producto),
    FOREIGN KEY (id_usuario) REFERENCES Usuarios(id_usuario)
);

-- Tabla de Ajustes de Inventario
CREATE TABLE Ajustes_Inventario (
    id_ajuste INT PRIMARY KEY IDENTITY(1,1),
    id_producto INT NOT NULL,
    tipo_ajuste VARCHAR(20) NOT NULL, -- SUMA, RESTA
    cantidad DECIMAL(10,2) NOT NULL,
    motivo VARCHAR(200) NOT NULL,
    id_usuario_autoriza INT NOT NULL,
    id_usuario_realiza INT NOT NULL,
    fecha_ajuste DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (id_producto) REFERENCES Productos(id_producto),
    FOREIGN KEY (id_usuario_autoriza) REFERENCES Usuarios(id_usuario),
    FOREIGN KEY (id_usuario_realiza) REFERENCES Usuarios(id_usuario)
);

-- ===================================================================
-- DATOS INICIALES
-- ===================================================================

-- Insertar Roles
INSERT INTO Roles (nombre_rol, descripcion) VALUES
('ADMINISTRADOR', 'Acceso completo a todos los módulos del sistema'),
('VENDEDOR', 'Acceso a módulo de ventas y consulta de inventario'),
('INVENTARIO', 'Gestión de stock y compras, sin acceso a ventas'),
('SUPERVISOR', 'Consulta de reportes y supervisión de operaciones'),
('CAJERO', 'Solo proceso de cobro y cierre de caja');

-- Insertar Permisos
INSERT INTO Permisos (nombre_permiso, descripcion, modulo) VALUES
-- Ventas
('CREAR_VENTA', 'Crear nuevas ventas', 'VENTAS'),
('EDITAR_VENTA', 'Editar ventas existentes', 'VENTAS'),
('ANULAR_VENTA', 'Anular ventas', 'VENTAS'),
('VER_REPORTES_VENTAS', 'Ver reportes de ventas', 'VENTAS'),
-- Inventario
('VER_STOCK', 'Consultar existencias', 'INVENTARIO'),
('ACTUALIZAR_STOCK', 'Actualizar cantidades de stock', 'INVENTARIO'),
('CREAR_PRODUCTO', 'Crear nuevos productos', 'INVENTARIO'),
('ELIMINAR_PRODUCTO', 'Eliminar productos', 'INVENTARIO'),
-- Compras
('CREAR_COMPRA', 'Registrar compras', 'COMPRAS'),
('EDITAR_COMPRA', 'Editar compras', 'COMPRAS'),
('GESTIONAR_PROVEEDORES', 'Administrar proveedores', 'COMPRAS'),
-- Reportes
('VER_REPORTES_INVENTARIO', 'Ver reportes de inventario', 'REPORTES'),
('EXPORTAR_DATOS', 'Exportar datos a Excel/PDF', 'REPORTES'),
-- Sistema
('GESTIONAR_USUARIOS', 'Administrar usuarios del sistema', 'SISTEMA'),
('CONFIGURAR_SISTEMA', 'Configuración general del sistema', 'SISTEMA');

-- Asignar Permisos a Roles
-- Administrador tiene todos los permisos
INSERT INTO Rol_Permisos (id_rol, id_permiso)
SELECT 1, id_permiso FROM Permisos;

-- Vendedor
INSERT INTO Rol_Permisos (id_rol, id_permiso) VALUES
(2, 1), (2, 2), (2, 4), (2, 5);

-- Inventario
INSERT INTO Rol_Permisos (id_rol, id_permiso) VALUES
(3, 5), (3, 6), (3, 7), (3, 8), (3, 9), (3, 10), (3, 11);

-- Supervisor
INSERT INTO Rol_Permisos (id_rol, id_permiso) VALUES
(4, 4), (4, 12), (4, 13);

-- Cajero
INSERT INTO Rol_Permisos (id_rol, id_permiso) VALUES
(5, 1), (5, 4);

-- Insertar usuario administrador por defecto
-- Contraseña: admin123 (deberá ser encriptada en producción)
INSERT INTO Usuarios (nombre_usuario, contrasena, nombre_completo, email, id_rol) VALUES
('admin', 'admin123', 'Administrador del Sistema', 'admin@fabricaqueso.com', 1);

-- Insertar productos de ejemplo
INSERT INTO Productos (codigo_producto, nombre_producto, descripcion, categoria, stock_actual, stock_minimo, precio_venta_base, porcentaje_itbis) VALUES
('QUESO001', 'Queso Blanco', 'Queso fresco tipo campesino', 'LACTEOS', 100.00, 20.00, 150.00, 18.00),
('QUESO002', 'Queso Amarillo', 'Queso madurado tipo cheddar', 'LACTEOS', 75.50, 15.00, 180.00, 18.00),
('QUESO003', 'Queso Mozzarella', 'Queso para pizza', 'LACTEOS', 50.00, 10.00, 200.00, 18.00),
('QUESO004', 'Queso Crema', 'Queso untable', 'LACTEOS', 30.00, 8.00, 120.00, 16.00);

-- Insertar clientes de ejemplo
INSERT INTO Clientes (nombre_cliente, rnc, telefono, direccion, tipo_cliente) VALUES
('Cliente General', '12345678901', '809-555-1234', 'Santo Domingo, RD', 'GENERAL'),
('Distribuidora La Mejor', '98765432109', '809-555-5678', 'Santiago, RD', 'MAYORISTA'),
('Colmado El Vecino', '45678901234', '809-555-9012', 'La Vega, RD', 'MINORISTA');

-- Insertar proveedores de ejemplo
INSERT INTO Proveedores (nombre_proveedor, rnc, telefono, email, direccion, contacto_principal) VALUES
('Lechería del Norte', '11122233344', '809-555-1111', 'contacto@lecherianorte.com', 'Puerto Plata, RD', 'Juan Pérez'),
('Ganadería Central', '55566677788', '809-555-2222', 'info@ganaderiacentral.com', 'Santiago, RD', 'María González');

-- Insertar precios de proveedores
INSERT INTO Precios_Proveedores (id_producto, id_proveedor, precio_compra) VALUES
(1, 1, 80.00), -- Queso Blanco - Lechería del Norte
(2, 1, 95.00), -- Queso Amarillo - Lechería del Norte
(3, 2, 110.00), -- Queso Mozzarella - Ganadería Central
(4, 1, 65.00); -- Queso Crema - Lechería del Norte

-- ===================================================================
-- ÍNDICES PARA OPTIMIZACIÓN
-- ===================================================================

CREATE INDEX idx_productos_codigo ON Productos(codigo_producto);
CREATE INDEX idx_productos_nombre ON Productos(nombre_producto);
CREATE INDEX idx_ventas_fecha ON Ventas(fecha_venta);
CREATE INDEX idx_ventas_cliente ON Ventas(id_cliente);
CREATE INDEX idx_detalle_ventas_venta ON Detalle_Ventas(id_venta);
CREATE INDEX idx_detalle_ventas_producto ON Detalle_Ventas(id_producto);
CREATE INDEX idx_movimientos_producto ON Movimientos_Inventario(id_producto);
CREATE INDEX idx_movimientos_fecha ON Movimientos_Inventario(fecha_movimiento);
CREATE INDEX idx_usuarios_usuario ON Usuarios(nombre_usuario);

-- ===================================================================
-- VISTAS ÚTILES
-- ===================================================================

-- Vista de Productos con Precios
CREATE VIEW vista_productos_precios AS
SELECT 
    p.id_producto,
    p.codigo_producto,
    p.nombre_producto,
    p.descripcion,
    p.categoria,
    p.stock_actual,
    p.stock_minimo,
    p.precio_venta_base,
    p.porcentaje_itbis,
    CASE 
        WHEN p.stock_actual <= p.stock_minimo THEN 'CRÍTICO'
        WHEN p.stock_actual <= (p.stock_minimo * 2) THEN 'BAJO'
        ELSE 'NORMAL'
    END AS estatus_stock,
    ISNULL(pp.precio_compra, 0) AS ultimo_precio_compra
FROM Productos p
LEFT JOIN (
    SELECT id_producto, precio_compra,
        ROW_NUMBER() OVER (PARTITION BY id_producto ORDER BY fecha_vigencia DESC) as rn
    FROM Precios_Proveedores
    WHERE activo = 1
) pp ON p.id_producto = pp.id_producto AND pp.rn = 1
WHERE p.activo = 1;

-- Vista de Ventas Detalladas
CREATE VIEW vista_ventas_detalladas AS
SELECT 
    v.id_venta,
    v.numero_factura,
    v.fecha_venta,
    c.nombre_cliente,
    u.nombre_completo AS vendedor,
    v.subtotal,
    v.itbis_total,
    v.total_venta,
    v.metodo_pago,
    v.estatus,
    COUNT(dv.id_detalle_venta) AS cantidad_items
FROM Ventas v
INNER JOIN Clientes c ON v.id_cliente = c.id_cliente
INNER JOIN Usuarios u ON v.id_usuario = u.id_usuario
LEFT JOIN Detalle_Ventas dv ON v.id_venta = dv.id_venta
GROUP BY 
    v.id_venta, v.numero_factura, v.fecha_venta, c.nombre_cliente, 
    u.nombre_completo, v.subtotal, v.itbis_total, v.total_venta, 
    v.metodo_pago, v.estatus;

-- ===================================================================
-- PROCEDIMIENTOS ALMACENADOS
-- ===================================================================

-- Procedimiento para registrar venta y actualizar inventario
CREATE PROCEDURE sp_registrar_venta
    @id_cliente INT,
    @id_usuario INT,
    @metodo_pago VARCHAR(50),
    @detalle JSON -- Formato: [{"id_producto":1,"cantidad":10.5}]
AS
BEGIN
    DECLARE @id_venta INT;
    DECLARE @subtotal DECIMAL(12,2) = 0;
    DECLARE @itbis_total DECIMAL(12,2) = 0;
    DECLARE @total_venta DECIMAL(12,2) = 0;
    DECLARE @numero_factura VARCHAR(50);
    
    -- Generar número de factura
    SET @numero_factura = 'VTA-' + CONVERT(VARCHAR(8), GETDATE(), 112) + '-' + 
                         RIGHT('000000' + CAST(COALESCE((SELECT MAX(id_venta) FROM Ventas) + 1, 1) AS VARCHAR), 6);
    
    BEGIN TRANSACTION;
    
    BEGIN TRY
        -- Insertar venta
        INSERT INTO Ventas (numero_factura, id_cliente, id_usuario, metodo_pago, subtotal, itbis_total, total_venta)
        VALUES (@numero_factura, @id_cliente, @id_usuario, @metodo_pago, 0, 0, 0);
        
        SET @id_venta = SCOPE_IDENTITY();
        
        -- Procesar detalle
        -- (Aquí iría la lógica para procesar el JSON y actualizar inventario)
        
        COMMIT TRANSACTION;
        
        SELECT @id_venta AS id_venta, @numero_factura AS numero_factura, 'EXITO' AS resultado;
        
    END TRY
    BEGIN CATCH
        ROLLBACK TRANSACTION;
        SELECT 0 AS id_venta, '' AS numero_factura, 'ERROR: ' + ERROR_MESSAGE() AS resultado;
    END CATCH
END;

GO

PRINT 'Base de datos FabricaQuesoRD creada exitosamente';
PRINT 'Usuario administrador: admin / admin123';
PRINT 'Recuerde cambiar la contraseña por defecto en producción';
