package com.auth.services;

import com.auth.clients.UserClient;
import com.auth.exceptions.AuthException;
import com.auth.models.Auth;
import com.auth.models.Rol;
import com.auth.models.dtos.AuthDetalleDTO;
import com.auth.models.dtos.AuthLoginDTO;
import com.auth.models.dtos.AuthSaveDTO;
import com.auth.models.dtos.AuthUpdatePasswordDTO;
import com.auth.models.dtos.AuthUpdateRolDTO;
import com.auth.models.dtos.UserDTO;
import com.auth.repositories.AuthRepository;
import com.auth.repositories.RolRepository;
import com.auth.security.JwtService;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {
    @Mock
    private AuthRepository authRepository;
    @Mock
    private UserClient userClient;
    @Mock
    private RolRepository rolRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthServiceImpl authService;

    private Auth authPrueba;
    private Rol rolPrueba;
    private UserDTO userDtoPrueba;
    private List<Auth> authList = new ArrayList<>();

    @BeforeEach
    public void setUp() {
        Faker faker = new Faker(Locale.of("es", "CL"));

        // Configuración de Rol
        this.rolPrueba = new Rol("ROLE_USER");
        this.rolPrueba.setRolId(1L);

        // Configuración de Auth (Entidad)
        this.authPrueba = new Auth();
        this.authPrueba.setId(1L);
        this.authPrueba.setNombreCuenta("UsuarioPrueba");
        this.authPrueba.setEmail("prueba@gamehub.com");
        this.authPrueba.setPassword("encodedPassword123");
        this.authPrueba.setEstado("Active");
        this.authPrueba.setFechaCreacion(LocalDateTime.now());
        this.authPrueba.getRoles().add(rolPrueba);
        this.authList.add(authPrueba);

        // Configuración de UserDTO
        this.userDtoPrueba = new UserDTO();
        this.userDtoPrueba.setId(10L);
        this.userDtoPrueba.setNombreUser(faker.name().fullName());
        this.userDtoPrueba.setEmail(this.authPrueba.getEmail());
        this.userDtoPrueba.setTelefono(faker.phoneNumber().cellPhone());
        this.userDtoPrueba.setRol("ROLE_USER");
        this.userDtoPrueba.setEstado("Active");

        // Generar lista masiva de Auths
        for (int i = 0; i < 5; i++) {
            Auth auth = new Auth();
            auth.setId((long) (i + 2));
            auth.setNombreCuenta(faker.internet().username());
            auth.setEmail(faker.internet().emailAddress());
            auth.setPassword(faker.internet().password());
            auth.setEstado("Active");
            auth.getRoles().add(rolPrueba);
            authList.add(auth);
        }
    }

    @Test
    @DisplayName("Debe listar todas las cuentas registradas")
    public void shouldFindAll() {
        when(this.authRepository.findAll()).thenReturn(this.authList);
        when(this.userClient.getUserByEmail(anyString())).thenReturn(this.userDtoPrueba);

        List<AuthDetalleDTO> result = this.authService.findAll();

        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(6); // 1 de prueba + 5 del bucle
        verify(authRepository, times(1)).findAll();
        verify(userClient, times(6)).getUserByEmail(anyString());
    }

    @Test
    @DisplayName("Debe buscar una cuenta por su ID")
    public void shouldFindById() {
        Long id = 1L;
        when(this.authRepository.findById(id)).thenReturn(Optional.of(this.authPrueba));
        when(this.userClient.getUserByEmail(this.authPrueba.getEmail())).thenReturn(this.userDtoPrueba);

        AuthDetalleDTO result = this.authService.findById(id);

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("prueba@gamehub.com");
        assertThat(result.getUser()).isNotNull();
        verify(authRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("Debe lanzar excepción al buscar una cuenta por ID inexistente")
    public void shouldNotFindById() {
        Long id = 999L;
        when(this.authRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> this.authService.findById(id))
                .isInstanceOf(AuthException.class)
                .hasMessage("Cuenta con ID " + id + " no encontrada");

        verify(authRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("Debe guardar una nueva cuenta exitosamente")
    public void shouldSaveAuth() {
        AuthSaveDTO saveDTO = new AuthSaveDTO();
        saveDTO.setEmail("nuevo@gamehub.com");
        saveDTO.setPassword("rawPassword");
        saveDTO.setNombreCuenta("NuevoUsuario");
        saveDTO.setRol("ROLE_USER");

        when(this.authRepository.findByEmail(saveDTO.getEmail())).thenReturn(Optional.empty());
        when(this.rolRepository.findByNombre(saveDTO.getRol())).thenReturn(Optional.of(this.rolPrueba));
        when(this.passwordEncoder.encode(saveDTO.getPassword())).thenReturn("encodedPassword");
        when(this.authRepository.save(any(Auth.class))).thenAnswer(invocation -> {
            Auth authGuardado = invocation.getArgument(0);
            authGuardado.setId(2L); // Simulamos el ID autogenerado
            return authGuardado;
        });

        AuthDetalleDTO result = this.authService.save(saveDTO);

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("nuevo@gamehub.com");
        assertThat(result.getEstado()).isEqualTo("Active");
        assertThat(result.getRol()).isEqualTo("ROLE_USER");

        verify(authRepository, times(1)).findByEmail(saveDTO.getEmail());
        verify(rolRepository, times(1)).findByNombre(saveDTO.getRol());
        verify(passwordEncoder, times(1)).encode(saveDTO.getPassword());
        verify(authRepository, times(1)).save(any(Auth.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción al intentar guardar un email ya registrado")
    public void shouldNotSaveAuthWhenEmailExists() {
        AuthSaveDTO saveDTO = new AuthSaveDTO();
        saveDTO.setEmail("prueba@gamehub.com");

        when(this.authRepository.findByEmail(saveDTO.getEmail())).thenReturn(Optional.of(this.authPrueba));

        assertThatThrownBy(() -> this.authService.save(saveDTO))
                .isInstanceOf(AuthException.class)
                .hasMessage("Cuenta con Email " + saveDTO.getEmail() + " ya esta registrado");

        verify(authRepository, times(1)).findByEmail(saveDTO.getEmail());
        verify(authRepository, never()).save(any(Auth.class));
    }

    @Test
    @DisplayName("Debe desactivar una cuenta por su ID")
    public void shouldDesactiveById() {
        Long id = 1L;
        when(this.authRepository.findById(id)).thenReturn(Optional.of(this.authPrueba));
        when(this.authRepository.save(any(Auth.class))).thenAnswer(i -> i.getArgument(0));

        AuthDetalleDTO result = this.authService.desactiveById(id);

        assertThat(result).isNotNull();
        assertThat(result.getEstado()).isEqualTo("Inactive");
        verify(authRepository, times(1)).findById(id);
        verify(authRepository, times(1)).save(any(Auth.class));
    }

    @Test
    @DisplayName("Debe actualizar la contraseña exitosamente")
    public void shouldUpdatePassword() {
        Long id = 1L;
        AuthUpdatePasswordDTO passwordDTO = new AuthUpdatePasswordDTO();
        passwordDTO.setPassword("newRawPassword");

        when(this.authRepository.findById(id)).thenReturn(Optional.of(this.authPrueba));
        when(this.passwordEncoder.encode(passwordDTO.getPassword())).thenReturn("newEncodedPassword");
        when(this.authRepository.save(any(Auth.class))).thenAnswer(i -> i.getArgument(0));

        AuthDetalleDTO result = this.authService.updatePassword(id, passwordDTO);

        assertThat(result).isNotNull();
        verify(authRepository, times(1)).findById(id);
        verify(passwordEncoder, times(1)).encode(passwordDTO.getPassword());
        verify(authRepository, times(1)).save(any(Auth.class));
    }

    @Test
    @DisplayName("Debe actualizar el rol exitosamente")
    public void shouldUpdateRol() {
        Long id = 1L;
        AuthUpdateRolDTO rolDTO = new AuthUpdateRolDTO();
        rolDTO.setRol("ROLE_ADMIN");

        Rol nuevoRol = new Rol("ROLE_ADMIN");

        when(this.authRepository.findById(id)).thenReturn(Optional.of(this.authPrueba));
        when(this.rolRepository.findByNombre(rolDTO.getRol())).thenReturn(Optional.of(nuevoRol));
        when(this.authRepository.save(any(Auth.class))).thenAnswer(i -> i.getArgument(0));

        AuthDetalleDTO result = this.authService.updateRol(id, rolDTO);

        assertThat(result).isNotNull();
        assertThat(result.getRol()).isEqualTo("ROLE_ADMIN");
        verify(authRepository, times(1)).findById(id);
        verify(rolRepository, times(1)).findByNombre(rolDTO.getRol());
        verify(authRepository, times(1)).save(any(Auth.class));
    }

    @Test
    @DisplayName("Debe iniciar sesión exitosamente y generar token")
    public void shouldLoginSuccessfully() {
        AuthLoginDTO loginDTO = new AuthLoginDTO();
        loginDTO.setEmail("prueba@gamehub.com");
        loginDTO.setPassword("rawPassword");

        when(this.authRepository.findByEmail(loginDTO.getEmail())).thenReturn(Optional.of(this.authPrueba));
        // Simulamos que la contraseña hace match
        when(this.passwordEncoder.matches(loginDTO.getPassword(), this.authPrueba.getPassword())).thenReturn(true);
        when(this.jwtService.generarToken(this.authPrueba)).thenReturn("jwt-token-generado-123");

        AuthDetalleDTO result = this.authService.login(loginDTO);

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("prueba@gamehub.com");
        assertThat(result.getEstado()).isEqualTo("Active");
        verify(authRepository, times(1)).findByEmail(loginDTO.getEmail());
        verify(passwordEncoder, times(1)).matches(loginDTO.getPassword(), this.authPrueba.getPassword());
        verify(jwtService, times(1)).generarToken(this.authPrueba);
    }

    @Test
    @DisplayName("Debe lanzar excepción al iniciar sesión con contraseña incorrecta")
    public void shouldFailLoginWhenPasswordIncorrect() {
        AuthLoginDTO loginDTO = new AuthLoginDTO();
        loginDTO.setEmail("prueba@gamehub.com");
        loginDTO.setPassword("wrongPassword");

        when(this.authRepository.findByEmail(loginDTO.getEmail())).thenReturn(Optional.of(this.authPrueba));
        when(this.passwordEncoder.matches(loginDTO.getPassword(), this.authPrueba.getPassword())).thenReturn(false);

        assertThatThrownBy(() -> this.authService.login(loginDTO))
                .isInstanceOf(AuthException.class)
                .hasMessage("Contraseña invalida");

        verify(authRepository, times(1)).findByEmail(loginDTO.getEmail());
        verify(passwordEncoder, times(1)).matches(loginDTO.getPassword(), this.authPrueba.getPassword());
        verify(jwtService, never()).generarToken(any(Auth.class));
    }
}