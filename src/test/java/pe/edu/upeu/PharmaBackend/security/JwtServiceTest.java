package pe.edu.upeu.PharmaBackend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import pe.edu.upeu.PharmaBackend.entity.Rol;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String SECRETO =
            "w9Z6dP/dzykNH/uiI3Z/KGjBlP0H4Ji+Tuq4fm2cTNuLD4jWafu1gBL74V2SJEvy";

    private static final String OTRO_SECRETO =
            "q8Bx3mN0vRk7TzYp1LcW5aHs9JdFg2Ue4OiPlKjMnBvCxZaSdFgHjKlQwErTyUiO";

    private final JwtService jwtService = new JwtService(SECRETO, 60);

    private UserDetails vendedor() {
        return User.withUsername("vendedor")
                .password("no-importa")
                .authorities(PermisosPorRol.authorities(
                        List.of(new Rol(2L, "VENDEDOR", null))))
                .build();
    }

    @Test
    void generarYLeerToken() {
        String token = jwtService.generarToken(vendedor());
        Claims claims = jwtService.leerClaims(token);

        assertThat(claims.getSubject()).isEqualTo("vendedor");
        assertThat(jwtService.extraerUsername(token)).isEqualTo("vendedor");
    }

    @Test
    void tokenAlterado() {
        String token = jwtService.generarToken(vendedor());
        String alterado = token.substring(0, token.length()-2) + "AA";

        assertThatThrownBy(() -> jwtService.extraerUsername(alterado))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void tokenDeOtraClave() {
        String token = new JwtService(OTRO_SECRETO,60)
                .generarToken(vendedor());

        assertThatThrownBy(() -> jwtService.extraerUsername(token))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void tokenExpirado() {
        String token = new JwtService(SECRETO,-1)
                .generarToken(vendedor());

        assertThatThrownBy(() -> jwtService.extraerUsername(token))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void expiracionEnSegundos() {
        assertThat(jwtService.getExpiracionSegundos()).isEqualTo(3600);
    }
}