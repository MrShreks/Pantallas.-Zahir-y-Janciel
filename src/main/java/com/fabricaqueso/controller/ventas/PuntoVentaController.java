package com.fabricaqueso.controller.ventas;

import com.fabricaqueso.model.dto.VentaDTO;
import com.fabricaqueso.model.entities.Producto;
import com.fabricaqueso.model.entities.Cliente;
import com.fabricaqueso.security.AuthenticationService;
import com.fabricaqueso.service.interfaces.VentaService;
import com.fabricaqueso.service.interfaces.ProductoService;
import com.fabricaqueso.util.ITBISCalculator;
import com.fabricaqueso.util.CurrencyFormatter;
import com.fabricaqueso.controller.common.BaseController;
import com.fabricaqueso.controller.common.AlertManager;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.GridPane;
import javafx.scene.input.KeyEvent;
import javafx.event.ActionEvent;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

/**
 * Controlador principal del Punto de Venta (POS)
 * Implementa cálculo automático de ITBIS para República Dominicana
 * Gestión de precios preestablecidos y control de acceso por roles
 */
public class PuntoVentaController extends BaseController {

    // Componentes de navegación
    @FXML private VBox viewVentas, viewCobro, viewHistorial;
    @FXML private Button btnNavVentas, btnNavHistorial;
    
    // Componentes del formulario de ventas
    @FXML private ComboBox<Cliente> cbCliente;
    @FXML private ComboBox<Producto> cbProducto;
    @FXML private TextField txtCantidad, txtPrecioUnitario, txtSubtotal;
    @FXML private TextField txtEfectivoRecibido, txtCambio;
    @FXML private ComboBox<String> cbMetodoPago;
    @FXML private Label lblSubtotal, lblITBIS, lblTotal, lblTotalCobro;
    
    // Tabla de items de venta
    @FXML private TableView<ItemVenta> tablaVentas;
    @FXML private TableColumn<ItemVenta, String> colProducto;
    @FXML private TableColumn<ItemVenta, Double> colCantidad;
    @FXML private TableColumn<ItemVenta, BigDecimal> colPrecioUnitario;
    @FXML private TableColumn<ItemVenta, BigDecimal> colSubtotal;
    @FXML private TableColumn<ItemVenta, BigDecimal> colITBIS;
    @FXML private TableColumn<ItemVenta, BigDecimal> colTotal;
    
    // Servicios
    private VentaService ventaService;
    private ProductoService productoService;
    private AuthenticationService authService;
    private ITBISCalculator itbisCalculator;
    
    // Datos
    private ObservableList<ItemVenta> itemsVenta = FXCollections.observableArrayList();
    private ObservableList<Cliente> clientes = FXCollections.observableArrayList();
    private ObservableList<Producto> productos = FXCollections.observableArrayList();
    
    // Constantes para RD
    private static final String MONEDA = "DOP";
    private static final BigDecimal ITBIS_GENERAL = new BigDecimal("0.18");
    private static final BigDecimal ITBIS_REDUCIDO = new BigDecimal("0.16");
    
    @FXML
    public void initialize() {
        super.initialize();
        
        // Validar permisos de acceso
        if (!authService.hasPermission("CREAR_VENTA")) {
            AlertManager.showError("Acceso Denegado", "No tienes permisos para acceder al módulo de ventas.");
            return;
        }
        
        inicializarComponentes();
        configurarTabla();
        cargarDatosIniciales();
        configurarListeners();
    }
    
    /**
     * Inyección de dependencias (llamado desde el factory)
     */
    public void setServices(VentaService ventaService, ProductoService productoService, 
                          AuthenticationService authService) {
        this.ventaService = ventaService;
        this.productoService = productoService;
        this.authService = authService;
        this.itbisCalculator = new ITBISCalculator();
    }
    
    private void inicializarComponentes() {
        // Configurar métodos de pago
        cbMetodoPago.setItems(FXCollections.observableArrayList(
            "EFECTIVO", "TARJETA", "TRANSFERENCIA", "MIXTO"
        ));
        
        // Configurar campos de solo lectura
        txtPrecioUnitario.setEditable(false);
        txtSubtotal.setEditable(false);
        txtCambio.setEditable(false);
        
        // Formato de moneda para campos
        configurarFormatoMoneda();
    }
    
    private void configurarTabla() {
        colProducto.setCellValueFactory(new PropertyValueFactory<>("nombreProducto"));
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colPrecioUnitario.setCellValueFactory(new PropertyValueFactory<>("precioUnitario"));
        colSubtotal.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
        colITBIS.setCellValueFactory(new PropertyValueFactory<>("montoITBIS"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("totalConITBIS"));
        
        // Formato de moneda en columnas
        colPrecioUnitario.setCellFactory(column -> new TableCell<ItemVenta, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : CurrencyFormatter.formatDOP(item));
            }
        });
        
        colSubtotal.setCellFactory(column -> new TableCell<ItemVenta, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : CurrencyFormatter.formatDOP(item));
            }
        });
        
        colITBIS.setCellFactory(column -> new TableCell<ItemVenta, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : CurrencyFormatter.formatDOP(item));
            }
        });
        
        colTotal.setCellFactory(column -> new TableCell<ItemVenta, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : CurrencyFormatter.formatDOP(item));
            }
        });
        
        tablaVentas.setItems(itemsVenta);
        
        // Listener para selección de items
        tablaVentas.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    cargarItemParaEdicion(newSelection);
                }
            });
    }
    
    private void cargarDatosIniciales() {
        try {
            // Cargar clientes activos
            clientes.addAll(productoService.getClientesActivos());
            cbCliente.setItems(clientes);
            
            // Cargar productos con stock
            productos.addAll(productoService.getProductosConStock());
            cbProducto.setItems(productos);
            
        } catch (Exception e) {
            AlertManager.showError("Error de Carga", "No se pudieron cargar los datos iniciales: " + e.getMessage());
        }
    }
    
    private void configurarListeners() {
        // Listener para selección de producto - carga precio automáticamente
        cbProducto.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                txtPrecioUnitario.setText(CurrencyFormatter.formatDOP(newVal.getPrecioVentaBase()));
                calcularSubtotal();
            }
        });
        
        // Listener para cantidad - recalcula automáticamente
        txtCantidad.addEventFilter(KeyEvent.KEY_TYPED, event -> {
            if (!event.getCharacter().matches("[0-9.]")) {
                event.consume();
            }
        });
        
        txtCantidad.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.isEmpty()) {
                calcularSubtotal();
            }
        });
        
        // Listener para método de pago - efectivo/cambio
        cbMetodoPago.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if ("EFECTIVO".equals(newVal)) {
                txtEfectivoRecibido.setDisable(false);
                txtEfectivoRecibido.requestFocus();
            } else {
                txtEfectivoRecibido.setDisable(true);
                txtEfectivoRecibido.clear();
                txtCambio.clear();
            }
        });
        
        // Listener para efectivo recibido - calcula cambio
        txtEfectivoRecibido.textProperty().addListener((obs, oldVal, newVal) -> {
            calcularCambio();
        });
    }
    
    private void configurarFormatoMoneda() {
        // Configurar formato para campos de moneda
        txtSubtotal.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.isEmpty() && !newVal.startsWith("RD$")) {
                try {
                    BigDecimal valor = new BigDecimal(newVal.replace(",", "").replace("RD$", "").trim());
                    txtSubtotal.setText(CurrencyFormatter.formatDOP(valor));
                } catch (NumberFormatException e) {
                    txtSubtotal.setText("RD$ 0.00");
                }
            }
        });
    }
    
    @FXML
    private void calcularSubtotal() {
        try {
            if (cbProducto.getValue() != null && !txtCantidad.getText().isEmpty()) {
                Producto producto = cbProducto.getValue();
                BigDecimal cantidad = new BigDecimal(txtCantidad.getText());
                BigDecimal precio = producto.getPrecioVentaBase();
                
                // Calcular subtotal
                BigDecimal subtotal = precio.multiply(cantidad);
                
                // Calcular ITBIS según tipo de producto
                BigDecimal porcentajeITBIS = producto.isItbisAplicable() ? 
                    producto.getPorcentajeItbis().divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP) : 
                    BigDecimal.ZERO;
                BigDecimal montoITBIS = subtotal.multiply(porcentajeITBIS);
                
                // Total con ITBIS
                BigDecimal total = subtotal.add(montoITBIS);
                
                txtSubtotal.setText(CurrencyFormatter.formatDOP(subtotal));
                
                // Guardar valores para uso posterior
                ItemVenta itemActual = new ItemVenta();
                itemActual.setSubtotal(subtotal);
                itemActual.setMontoITBIS(montoITBIS);
                itemActual.setTotalConITBIS(total);
                
            } else {
                txtSubtotal.setText("RD$ 0.00");
            }
        } catch (NumberFormatException e) {
            txtSubtotal.setText("RD$ 0.00");
        }
    }
    
    @FXML
    private void añadirItem() {
        if (!validarItem()) {
            return;
        }
        
        try {
            Producto producto = cbProducto.getValue();
            BigDecimal cantidad = new BigDecimal(txtCantidad.getText());
            BigDecimal precio = producto.getPrecioVentaBase();
            
            // Verificar stock disponible
            if (!productoService.verificarStock(producto.getIdProducto(), cantidad)) {
                AlertManager.showWarning("Stock Insuficiente", 
                    "Solo hay " + producto.getStockActual() + " libras disponibles de " + producto.getNombreProducto());
                return;
            }
            
            // Crear item de venta
            ItemVenta item = new ItemVenta();
            item.setIdProducto(producto.getIdProducto());
            item.setNombreProducto(producto.getNombreProducto());
            item.setCantidad(cantidad.doubleValue());
            item.setPrecioUnitario(precio);
            
            // Calcular valores
            BigDecimal subtotal = precio.multiply(cantidad);
            BigDecimal porcentajeITBIS = producto.isItbisAplicable() ? 
                producto.getPorcentajeItbis().divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP) : 
                BigDecimal.ZERO;
            BigDecimal montoITBIS = subtotal.multiply(porcentajeITBIS);
            BigDecimal total = subtotal.add(montoITBIS);
            
            item.setSubtotal(subtotal);
            item.setMontoITBIS(montoITBIS);
            item.setTotalConITBIS(total);
            item.setPorcentajeITBIS(producto.getPorcentajeItbis());
            
            // Verificar si el producto ya existe en la tabla
            Optional<ItemVenta> existente = itemsVenta.stream()
                .filter(i -> i.getIdProducto() == producto.getIdProducto())
                .findFirst();
            
            if (existente.isPresent()) {
                // Actualizar cantidad y valores
                ItemVenta itemExistente = existente.get();
                BigDecimal nuevaCantidad = itemExistente.getCantidad().add(cantidad);
                itemExistente.setCantidad(nuevaCantidad.doubleValue());
                
                // Recalcular valores
                BigDecimal nuevoSubtotal = precio.multiply(nuevaCantidad);
                BigDecimal nuevoITBIS = nuevoSubtotal.multiply(porcentajeITBIS);
                BigDecimal nuevoTotal = nuevoSubtotal.add(nuevoITBIS);
                
                itemExistente.setSubtotal(nuevoSubtotal);
                itemExistente.setMontoITBIS(nuevoITBIS);
                itemExistente.setTotalConITBIS(nuevoTotal);
                
                tablaVentas.refresh();
            } else {
                itemsVenta.add(item);
            }
            
            actualizarTotales();
            limpiarCamposItem();
            
        } catch (Exception e) {
            AlertManager.showError("Error", "No se pudo añadir el item: " + e.getMessage());
        }
    }
    
    @FXML
    private void eliminarItem() {
        ItemVenta seleccionado = tablaVentas.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            itemsVenta.remove(seleccionado);
            actualizarTotales();
            limpiarCamposItem();
        }
    }
    
    @FXML
    private void limpiarCamposItem() {
        cbProducto.setValue(null);
        txtCantidad.clear();
        txtPrecioUnitario.clear();
        txtSubtotal.setText("RD$ 0.00");
        tablaVentas.getSelectionModel().clearSelection();
    }
    
    private void cargarItemParaEdicion(ItemVenta item) {
        cbProducto.setValue(productos.stream()
            .filter(p -> p.getIdProducto() == item.getIdProducto())
            .findFirst()
            .orElse(null));
        txtCantidad.setText(String.valueOf(item.getCantidad()));
        txtPrecioUnitario.setText(CurrencyFormatter.formatDOP(item.getPrecioUnitario()));
        txtSubtotal.setText(CurrencyFormatter.formatDOP(item.getSubtotal()));
    }
    
    private void actualizarTotales() {
        BigDecimal subtotalTotal = itemsVenta.stream()
            .map(ItemVenta::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal itbisTotal = itemsVenta.stream()
            .map(ItemVenta::getMontoITBIS)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalGeneral = itemsVenta.stream()
            .map(ItemVenta::getTotalConITBIS)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        lblSubtotal.setText(CurrencyFormatter.formatDOP(subtotalTotal));
        lblITBIS.setText(CurrencyFormatter.formatDOP(itbisTotal));
        lblTotal.setText(CurrencyFormatter.formatDOP(totalGeneral));
        lblTotalCobro.setText(CurrencyFormatter.formatDOP(totalGeneral));
    }
    
    private void calcularCambio() {
        try {
            if (!txtEfectivoRecibido.getText().isEmpty() && !lblTotalCobro.getText().isEmpty()) {
                BigDecimal efectivo = new BigDecimal(txtEfectivoRecibido.getText().replace(",", "").replace("RD$", "").trim());
                BigDecimal total = new BigDecimal(lblTotalCobro.getText().replace(",", "").replace("RD$", "").trim());
                
                if (efectivo.compareTo(total) >= 0) {
                    BigDecimal cambio = efectivo.subtract(total);
                    txtCambio.setText(CurrencyFormatter.formatDOP(cambio));
                } else {
                    txtCambio.setText("RD$ 0.00");
                }
            }
        } catch (NumberFormatException e) {
            txtCambio.setText("RD$ 0.00");
        }
    }
    
    @FXML
    private void irACobro() {
        if (!validarVenta()) {
            return;
        }
        
        alternarVista(viewCobro, null);
    }
    
    @FXML
    private void finalizarVenta() {
        if (!validarCobro()) {
            return;
        }
        
        try {
            // Crear DTO de venta
            VentaDTO ventaDTO = new VentaDTO();
            ventaDTO.setIdCliente(cbCliente.getValue().getIdCliente());
            ventaDTO.setIdUsuario(authService.getCurrentUser().get().getIdUsuario());
            ventaDTO.setMetodoPago(cbMetodoPago.getValue());
            
            // Convertir items de venta
            ventaDTO.setItems(itemsVenta.stream()
                .map(this::convertirItemVentaADTO)
                .collect(java.util.stream.Collectors.toList()));
            
            // Configurar montos según método de pago
            BigDecimal totalVenta = new BigDecimal(lblTotalCobro.getText().replace(",", "").replace("RD$", "").trim());
            ventaDTO.setTotalVenta(totalVenta);
            
            if ("EFECTIVO".equals(cbMetodoPago.getValue())) {
                BigDecimal efectivo = new BigDecimal(txtEfectivoRecibido.getText().replace(",", "").replace("RD$", "").trim());
                ventaDTO.setMontoEfectivo(efectivo);
                ventaDTO.setMontoTarjeta(BigDecimal.ZERO);
                ventaDTO.setMontoTransferencia(BigDecimal.ZERO);
            } else if ("TARJETA".equals(cbMetodoPago.getValue())) {
                ventaDTO.setMontoEfectivo(BigDecimal.ZERO);
                ventaDTO.setMontoTarjeta(totalVenta);
                ventaDTO.setMontoTransferencia(BigDecimal.ZERO);
            } else if ("TRANSFERENCIA".equals(cbMetodoPago.getValue())) {
                ventaDTO.setMontoEfectivo(BigDecimal.ZERO);
                ventaDTO.setMontoTarjeta(BigDecimal.ZERO);
                ventaDTO.setMontoTransferencia(totalVenta);
            }
            
            // Procesar venta
            boolean exito = ventaService.procesarVenta(ventaDTO);
            
            if (exito) {
                AlertManager.showSuccess("Venta Exitosa", "La venta se ha procesado correctamente.");
                limpiarVenta();
                mostrarVentas();
            } else {
                AlertManager.showError("Error", "No se pudo procesar la venta. Intente nuevamente.");
            }
            
        } catch (Exception e) {
            AlertManager.showError("Error", "Error al procesar la venta: " + e.getMessage());
        }
    }
    
    private boolean validarItem() {
        if (cbProducto.getValue() == null) {
            AlertManager.showWarning("Validación", "Debe seleccionar un producto.");
            return false;
        }
        
        if (txtCantidad.getText().isEmpty()) {
            AlertManager.showWarning("Validación", "Debe ingresar una cantidad.");
            return false;
        }
        
        try {
            BigDecimal cantidad = new BigDecimal(txtCantidad.getText());
            if (cantidad.compareTo(BigDecimal.ZERO) <= 0) {
                AlertManager.showWarning("Validación", "La cantidad debe ser mayor a cero.");
                return false;
            }
        } catch (NumberFormatException e) {
            AlertManager.showWarning("Validación", "La cantidad ingresada no es válida.");
            return false;
        }
        
        return true;
    }
    
    private boolean validarVenta() {
        if (cbCliente.getValue() == null) {
            AlertManager.showWarning("Validación", "Debe seleccionar un cliente.");
            return false;
        }
        
        if (itemsVenta.isEmpty()) {
            AlertManager.showWarning("Validación", "Debe agregar al menos un producto a la venta.");
            return false;
        }
        
        return true;
    }
    
    private boolean validarCobro() {
        if (cbMetodoPago.getValue() == null) {
            AlertManager.showWarning("Validación", "Debe seleccionar un método de pago.");
            return false;
        }
        
        if ("EFECTIVO".equals(cbMetodoPago.getValue())) {
            if (txtEfectivoRecibido.getText().isEmpty()) {
                AlertManager.showWarning("Validación", "Debe ingresar el efectivo recibido.");
                return false;
            }
            
            try {
                BigDecimal efectivo = new BigDecimal(txtEfectivoRecibido.getText().replace(",", "").replace("RD$", "").trim());
                BigDecimal total = new BigDecimal(lblTotalCobro.getText().replace(",", "").replace("RD$", "").trim());
                
                if (efectivo.compareTo(total) < 0) {
                    AlertManager.showWarning("Validación", "El efectivo recibido es insuficiente.");
                    return false;
                }
            } catch (NumberFormatException e) {
                AlertManager.showWarning("Validación", "El monto de efectivo no es válido.");
                return false;
            }
        }
        
        return true;
    }
    
    private void limpiarVenta() {
        itemsVenta.clear();
        cbCliente.setValue(null);
        cbMetodoPago.setValue(null);
        txtEfectivoRecibido.clear();
        txtCambio.clear();
        actualizarTotales();
    }
    
    private void alternarVista(VBox vista, Button boton) {
        // Ocultar todas las vistas
        viewVentas.setVisible(false);
        viewVentas.setManaged(false);
        viewCobro.setVisible(false);
        viewCobro.setManaged(false);
        viewHistorial.setVisible(false);
        viewHistorial.setManaged(false);
        
        // Mostrar vista seleccionada
        vista.setVisible(true);
        vista.setManaged(true);
        
        // Actualizar botones de navegación
        if (boton != null) {
            btnNavVentas.getStyleClass().remove("nav-button-active");
            btnNavHistorial.getStyleClass().remove("nav-button-active");
            boton.getStyleClass().add("nav-button-active");
        }
    }
    
    @FXML
    private void mostrarVentas() {
        alternarVista(viewVentas, btnNavVentas);
    }
    
    @FXML
    private void mostrarHistorial() {
        if (!authService.hasPermission("VER_REPORTES_VENTAS")) {
            AlertManager.showError("Acceso Denegado", "No tienes permisos para ver el historial de ventas.");
            return;
        }
        alternarVista(viewHistorial, btnNavHistorial);
    }
    
    @FXML
    private void cancelarVenta() {
        if (!itemsVenta.isEmpty()) {
            boolean confirmar = AlertManager.showConfirmation("Cancelar Venta", 
                "¿Está seguro que desea cancelar la venta actual? Se perderán todos los datos.");
            
            if (confirmar) {
                limpiarVenta();
                mostrarVentas();
            }
        } else {
            mostrarVentas();
        }
    }
    
    // Métodos de conversión
    private VentaDTO.ItemVentaDTO convertirItemVentaADTO(ItemVenta item) {
        VentaDTO.ItemVentaDTO dto = new VentaDTO.ItemVentaDTO();
        dto.setIdProducto(item.getIdProducto());
        dto.setCantidad(item.getCantidad());
        dto.setPrecioUnitario(item.getPrecioUnitario());
        dto.setPorcentajeITBIS(item.getPorcentajeITBIS());
        return dto;
    }
    
    // Clase interna para representar items de venta en la tabla
    public static class ItemVenta {
        private int idProducto;
        private String nombreProducto;
        private double cantidad;
        private BigDecimal precioUnitario;
        private BigDecimal subtotal;
        private BigDecimal montoITBIS;
        private BigDecimal totalConITBIS;
        private BigDecimal porcentajeITBIS;
        
        // Getters y setters
        public int getIdProducto() { return idProducto; }
        public void setIdProducto(int idProducto) { this.idProducto = idProducto; }
        
        public String getNombreProducto() { return nombreProducto; }
        public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }
        
        public Double getCantidad() { return cantidad; }
        public void setCantidad(Double cantidad) { this.cantidad = cantidad; }
        
        public BigDecimal getCantidadAsBigDecimal() { return BigDecimal.valueOf(cantidad); }
        
        public BigDecimal getPrecioUnitario() { return precioUnitario; }
        public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }
        
        public BigDecimal getSubtotal() { return subtotal; }
        public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
        
        public BigDecimal getMontoITBIS() { return montoITBIS; }
        public void setMontoITBIS(BigDecimal montoITBIS) { this.montoITBIS = montoITBIS; }
        
        public BigDecimal getTotalConITBIS() { return totalConITBIS; }
        public void setTotalConITBIS(BigDecimal totalConITBIS) { this.totalConITBIS = totalConITBIS; }
        
        public BigDecimal getPorcentajeITBIS() { return porcentajeITBIS; }
        public void setPorcentajeITBIS(BigDecimal porcentajeITBIS) { this.porcentajeITBIS = porcentajeITBIS; }
    }
}
