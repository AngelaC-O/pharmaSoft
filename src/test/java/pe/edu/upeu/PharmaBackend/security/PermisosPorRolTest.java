package pe.edu.upeu.PharmaBackend.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import pe.edu.upeu.PharmaBackend.entity.Rol;
import pe.edu.upeu.PharmaBackend.enums.Permiso;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PermisosPorRolTest {

    private static final Rol ADMIN =
            new Rol(1L,"ADMIN",null);

    private static final Rol VENDEDOR =
            new Rol(2L,"VENDEDOR",null);

    private static final Rol ALMACENERO =
            new Rol(3L,"ALMACENERO",null);

    private List<String> authorities(Rol... roles) {
        return PermisosPorRol.authorities(List.of(roles))
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
    }

    @Test
    void adminTieneTodo() {
        assertThat(PermisosPorRol.de("ADMIN"))
                .containsExactlyInAnyOrder(Permiso.values());
    }

    @Test
    void vendedor() {
        assertThat(authorities(VENDEDOR))
                .contains("ROLE_VENDEDOR","VENTAS_REGISTRAR","CATALOGO_LEER")
                .doesNotContain("REPORTES_VER");
    }

    @Test
    void variosRoles() {
        assertThat(authorities(VENDEDOR,ALMACENERO))
                .contains("ROLE_ALMACENERO","ROLE_VENDEDOR",
                        "CATALOGO_ESCRIBIR","VENTAS_REGISTRAR")
                .doesNotHaveDuplicates();
    }

    @Test
    void rolDesconocido() {
        assertThat(authorities(new Rol(9L,"AUDITOR",null)))
                .containsExactly("ROLE_AUDITOR");
    }
}