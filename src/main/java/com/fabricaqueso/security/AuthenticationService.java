package com.fabricaqueso.security;

import com.fabricaqueso.model.entities.Usuario;
import com.fabricaqueso.model.enums.RolEnum;
import com.fabricaqueso.repository.interfaces.UsuarioRepository;
import com.fabricaqueso.util.SecurityUtil;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Servicio de autenticación y gestión de sesiones
 * Implementa el sistema RBAC para control de acceso
 */
public class AuthenticationService {
    
    private static AuthenticationService instance;
    private Usuario currentUser;
    private UsuarioRepository usuarioRepository;
    private Map<String, Boolean> sessionCache;
    
    private AuthenticationService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
        this.sessionCache = new HashMap<>();
    }
    
    /**
     * Obtiene la instancia singleton del servicio
     */
    public static synchronized AuthenticationService getInstance(UsuarioRepository usuarioRepository) {
        if (instance == null) {
            instance = new AuthenticationService(usuarioRepository);
        }
        return instance;
    }
    
    /**
     * Autentica un usuario y establece la sesión actual
     * @param nombreUsuario Nombre de usuario
     * @param contrasena Contraseña sin encriptar
     * @return Optional<Usuario> con el usuario autenticado o vacío
     */
    public Optional<Usuario> authenticate(String nombreUsuario, String contrasena) {
        try {
            // Encriptar contraseña para comparación
            String hashedPassword = SecurityUtil.hashPassword(contrasena);
            
            Optional<Usuario> usuarioOpt = usuarioRepository.findByNombreUsuario(nombreUsuario);
            
            if (usuarioOpt.isPresent()) {
                Usuario usuario = usuarioOpt.get();
                
                // Verificar contraseña y estado activo
                if (usuario.isActivo() && usuario.getContrasena().equals(hashedPassword)) {
                    this.currentUser = usuario;
                    
                    // Actualizar último acceso
                    usuarioRepository.actualizarUltimoAcceso(usuario.getIdUsuario());
                    
                    // Limpiar caché de permisos
                    sessionCache.clear();
                    
                    return Optional.of(usuario);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error en autenticación: " + e.getMessage());
        }
        
        return Optional.empty();
    }
    
    /**
     * Cierra la sesión actual
     */
    public void logout() {
        this.currentUser = null;
        this.sessionCache.clear();
    }
    
    /**
     * Obtiene el usuario actualmente autenticado
     */
    public Optional<Usuario> getCurrentUser() {
        return Optional.ofNullable(currentUser);
    }
    
    /**
     * Verifica si hay una sesión activa
     */
    public boolean isAuthenticated() {
        return currentUser != null && currentUser.isActivo();
    }
    
    /**
     * Verifica si el usuario actual tiene un rol específico
     */
    public boolean hasRole(RolEnum rol) {
        if (!isAuthenticated()) {
            return false;
        }
        
        return currentUser.getRol().getNombreRol().equalsIgnoreCase(rol.name());
    }
    
    /**
     * Verifica si el usuario actual tiene un permiso específico
     * Implementa caché para optimizar rendimiento
     */
    public boolean hasPermission(String permission) {
        if (!isAuthenticated()) {
            return false;
        }
        
        // Verificar caché primero
        String cacheKey = currentUser.getIdUsuario() + ":" + permission;
        if (sessionCache.containsKey(cacheKey)) {
            return sessionCache.get(cacheKey);
        }
        
        try {
            boolean hasPermission = usuarioRepository.hasPermission(currentUser.getIdUsuario(), permission);
            sessionCache.put(cacheKey, hasPermission);
            return hasPermission;
            
        } catch (SQLException e) {
            System.err.println("Error verificando permiso: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Verifica si el usuario puede acceder a un módulo específico
     */
    public boolean canAccessModule(String module) {
        if (!isAuthenticated()) {
            return false;
        }
        
        try {
            return usuarioRepository.hasModuleAccess(currentUser.getIdUsuario(), module);
            
        } catch (SQLException e) {
            System.err.println("Error verificando acceso a módulo: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Obtiene todos los permisos del usuario actual
     */
    public Map<String, Boolean> getAllPermissions() {
        Map<String, Boolean> permissions = new HashMap<>();
        
        if (!isAuthenticated()) {
            return permissions;
        }
        
        try {
            permissions = usuarioRepository.getAllPermissions(currentUser.getIdUsuario());
            
        } catch (SQLException e) {
            System.err.println("Error obteniendo permisos: " + e.getMessage());
        }
        
        return permissions;
    }
    
    /**
     * Verifica si el usuario actual es administrador
     */
    public boolean isAdmin() {
        return hasRole(RolEnum.ADMINISTRADOR);
    }
    
    /**
     * Cambia la contraseña del usuario actual
     */
    public boolean changePassword(String currentPassword, String newPassword) {
        if (!isAuthenticated()) {
            return false;
        }
        
        try {
            // Verificar contraseña actual
            String hashedCurrent = SecurityUtil.hashPassword(currentPassword);
            if (!currentUser.getContrasena().equals(hashedCurrent)) {
                return false;
            }
            
            // Actualizar contraseña
            String hashedNew = SecurityUtil.hashPassword(newPassword);
            boolean updated = usuarioRepository.updatePassword(currentUser.getIdUsuario(), hashedNew);
            
            if (updated) {
                currentUser.setContrasena(hashedNew);
            }
            
            return updated;
            
        } catch (SQLException e) {
            System.err.println("Error cambiando contraseña: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Refresca los datos del usuario actual desde la base de datos
     */
    public void refreshUserData() {
        if (!isAuthenticated()) {
            return;
        }
        
        try {
            Optional<Usuario> updatedUser = usuarioRepository.findById(currentUser.getIdUsuario());
            updatedUser.ifPresent(user -> this.currentUser = user);
            
            // Limpiar caché de permisos
            sessionCache.clear();
            
        } catch (SQLException e) {
            System.err.println("Error refrescando datos de usuario: " + e.getMessage());
        }
    }
    
    /**
     * Verifica si la sesión ha expirado (implementar timeout si es necesario)
     */
    public boolean isSessionValid() {
        return isAuthenticated(); // Implementar lógica de timeout si se requiere
    }
    
    /**
     * Obtiene información de auditoría para el usuario actual
     */
    public String getAuditInfo() {
        if (!isAuthenticated()) {
            return "No autenticado";
        }
        
        return String.format("Usuario: %s (ID: %d, Rol: %s)", 
                currentUser.getNombreUsuario(), 
                currentUser.getIdUsuario(), 
                currentUser.getRol().getNombreRol());
    }
}
