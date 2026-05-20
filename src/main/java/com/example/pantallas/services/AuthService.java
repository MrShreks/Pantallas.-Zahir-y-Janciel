package com.example.pantallas.services;

<<<<<<< HEAD
=======
import com.example.pantallas.config.DatabaseConfig;
>>>>>>> bb658e7 (Primer commit)
import com.example.pantallas.models.Usuario;
import com.example.pantallas.repositories.UsuarioRepository;
import com.example.pantallas.security.PasswordUtil;
import com.example.pantallas.security.SecurityUtil;
<<<<<<< HEAD
=======
import com.example.pantallas.utils.LoggerUtil;
>>>>>>> bb658e7 (Primer commit)

import java.util.Optional;

public class AuthService {
    private final UsuarioRepository usuarioRepository;

    public AuthService() {
        this.usuarioRepository = new UsuarioRepository();
    }

<<<<<<< HEAD
    public boolean login(String username, String password) {
        Optional<Usuario> userOpt = usuarioRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            Usuario user = userOpt.get();
            // Check plain password if no bcrypt is set yet, or check bcrypt
            if (PasswordUtil.checkPassword(password, user.getContrasena()) || user.getContrasena().equals(password)) {
                SecurityUtil.login(user.getId(), user.getNombreUsuario(), user.getRol());
                return true;
            }
        }
        return false;
=======
    public String login(String username, String password) {
        if (!DatabaseConfig.isInitialized()) {
            return "Error del sistema: " + DatabaseConfig.getInitError();
        }
        Optional<Usuario> userOpt = usuarioRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            String testConn = usuarioRepository.testConnection();
            if (!testConn.equals("OK")) {
                return "Error de conexión: " + testConn;
            }
            return "Usuario o contraseña incorrectos";
        }
        Usuario user = userOpt.get();
        String storedPass = user.getContrasena();
        boolean bcryptOk = PasswordUtil.checkPassword(password, storedPass);
        boolean plainOk = storedPass != null && storedPass.equals(password);
        LoggerUtil.info("Login debug - input pass: '" + password + "', stored pass: '" + storedPass + "', bcryptOk: " + bcryptOk + ", plainOk: " + plainOk + ", stored length: " + (storedPass != null ? storedPass.length() : 0));
        if (bcryptOk || plainOk) {
            SecurityUtil.login(user.getId(), user.getNombreUsuario(), user.getRol());
            return "OK";
        }
        return "Usuario o contraseña incorrectos";
>>>>>>> bb658e7 (Primer commit)
    }

    public void logout() {
        SecurityUtil.logout();
    }
}

