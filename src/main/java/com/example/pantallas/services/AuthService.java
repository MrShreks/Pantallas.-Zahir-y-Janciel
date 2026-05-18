package com.example.pantallas.services;

import com.example.pantallas.models.Usuario;
import com.example.pantallas.repositories.UsuarioRepository;
import com.example.pantallas.security.PasswordUtil;
import com.example.pantallas.security.SecurityUtil;

import java.util.Optional;

public class AuthService {
    private final UsuarioRepository usuarioRepository;

    public AuthService() {
        this.usuarioRepository = new UsuarioRepository();
    }

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
    }

    public void logout() {
        SecurityUtil.logout();
    }
}

