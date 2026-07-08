package com.category.services;

import com.category.exceptions.CategoryException;
import com.category.models.Category;
import com.category.models.dtos.CategoryDetalleDTO;
import com.category.models.dtos.CategorySaveDTO;
import com.category.models.dtos.CategoryUpdateDescripcionDTO;
import com.category.models.dtos.CategoryUpdateNombreDTO;
import com.category.repositories.CategoryRepository;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category categoriaPrueba;
    private List<Category> categoriaList = new ArrayList<>();

    @BeforeEach
    public void setUp() {
        Faker faker = new Faker(Locale.of("es", "CL"));

        // Configuración de la Categoría de prueba (Entidad)
        this.categoriaPrueba = new Category();
        this.categoriaPrueba.setId(1L);
        this.categoriaPrueba.setNombreCategory("Aventura");
        this.categoriaPrueba.setDescripcion("Juegos de aventura y exploración");
        this.categoriaPrueba.setEstado("Active");
        this.categoriaList.add(this.categoriaPrueba);

        // Generar lista masiva de Categorías simuladas
        for (int i = 0; i < 5; i++) {
            Category cat = new Category();
            cat.setId((long) (i + 2));
            cat.setNombreCategory(faker.esports().game()); // Usamos faker de esports/juegos
            cat.setDescripcion(faker.lorem().sentence());
            cat.setEstado("Active");
            this.categoriaList.add(cat);
        }
    }

    @Test
    @DisplayName("Debe listar todas las categorías registradas mapeadas a DTO")
    public void shouldFindAll() {
        when(this.categoryRepository.findAll()).thenReturn(this.categoriaList);

        List<CategoryDetalleDTO> result = this.categoryService.findAll();

        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(6); // 1 de prueba + 5 del bucle
        assertThat(result.get(0).getNombreCategory()).isEqualTo("Aventura");
        verify(categoryRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Debe buscar una categoría por su ID y devolver un DTO")
    public void shouldFindById() {
        Long id = 1L;
        when(this.categoryRepository.findById(id)).thenReturn(Optional.of(this.categoriaPrueba));

        CategoryDetalleDTO result = this.categoryService.findById(id);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getNombreCategory()).isEqualTo("Aventura");
        assertThat(result.getEstado()).isEqualTo("Active");
        verify(categoryRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("Debe lanzar excepción al buscar una categoría por ID inexistente")
    public void shouldNotFindById() {
        Long id = 999L;
        when(this.categoryRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> this.categoryService.findById(id))
                .isInstanceOf(CategoryException.class)
                .hasMessage("Categoria con ID " + id + " no encontrada");

        verify(categoryRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("Debe guardar una nueva categoría exitosamente")
    public void shouldSaveCategory() {
        CategorySaveDTO saveDTO = new CategorySaveDTO();
        saveDTO.setNombreCategory("RPG");
        saveDTO.setDescripcion("Juegos de rol");
        saveDTO.setEstado("Active");

        when(this.categoryRepository.existsByNombreCategory(saveDTO.getNombreCategory())).thenReturn(false);
        when(this.categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
            Category catGuardada = invocation.getArgument(0);
            catGuardada.setId(10L); // Simulamos el ID generado por la base de datos
            return catGuardada;
        });

        CategoryDetalleDTO result = this.categoryService.save(saveDTO);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getNombreCategory()).isEqualTo("RPG");
        assertThat(result.getEstado()).isEqualTo("Active");

        verify(categoryRepository, times(1)).existsByNombreCategory(saveDTO.getNombreCategory());
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción al intentar guardar un nombre de categoría ya existente")
    public void shouldNotSaveCategoryWhenNameExists() {
        CategorySaveDTO saveDTO = new CategorySaveDTO();
        saveDTO.setNombreCategory("Aventura");

        when(this.categoryRepository.existsByNombreCategory(saveDTO.getNombreCategory())).thenReturn(true);

        assertThatThrownBy(() -> this.categoryService.save(saveDTO))
                .isInstanceOf(CategoryException.class)
                .hasMessage("Ya existe una categoria registrada con ese nombre");

        verify(categoryRepository, times(1)).existsByNombreCategory(saveDTO.getNombreCategory());
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    @DisplayName("Debe desactivar una categoría por su ID")
    public void shouldDesactiveById() {
        Long id = 1L;
        when(this.categoryRepository.findById(id)).thenReturn(Optional.of(this.categoriaPrueba));
        when(this.categoryRepository.save(any(Category.class))).thenAnswer(i -> i.getArgument(0));

        CategoryDetalleDTO result = this.categoryService.desactiveById(id);

        assertThat(result).isNotNull();
        assertThat(result.getEstado()).isEqualTo("Inactive");
        verify(categoryRepository, times(1)).findById(id);
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    @DisplayName("Debe actualizar el nombre de una categoría exitosamente")
    public void shouldUpdateNombre() {
        Long id = 1L;
        CategoryUpdateNombreDTO nombreDTO = new CategoryUpdateNombreDTO();
        nombreDTO.setNombreCategory("Acción");

        when(this.categoryRepository.findById(id)).thenReturn(Optional.of(this.categoriaPrueba));
        when(this.categoryRepository.save(any(Category.class))).thenAnswer(i -> i.getArgument(0));

        CategoryDetalleDTO result = this.categoryService.updateNombre(id, nombreDTO);

        assertThat(result).isNotNull();
        assertThat(result.getNombreCategory()).isEqualTo("Acción");
        verify(categoryRepository, times(1)).findById(id);
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción al actualizar nombre de categoría inexistente")
    public void shouldNotUpdateNombre() {
        Long id = 999L;
        CategoryUpdateNombreDTO nombreDTO = new CategoryUpdateNombreDTO();
        nombreDTO.setNombreCategory("Acción");

        when(this.categoryRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> this.categoryService.updateNombre(id, nombreDTO))
                .isInstanceOf(CategoryException.class)
                .hasMessage("Categoria no encontrada, no se puede actualizar el nombre");

        verify(categoryRepository, times(1)).findById(id);
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    @DisplayName("Debe actualizar la descripción de una categoría exitosamente")
    public void shouldUpdateDescripcion() {
        Long id = 1L;
        CategoryUpdateDescripcionDTO descripcionDTO = new CategoryUpdateDescripcionDTO();
        descripcionDTO.setDescripcion("Nueva descripción para los juegos de aventura");

        when(this.categoryRepository.findById(id)).thenReturn(Optional.of(this.categoriaPrueba));
        when(this.categoryRepository.save(any(Category.class))).thenAnswer(i -> i.getArgument(0));

        CategoryDetalleDTO result = this.categoryService.updateDescripcion(id, descripcionDTO);

        assertThat(result).isNotNull();
        assertThat(result.getDescripcion()).isEqualTo("Nueva descripción para los juegos de aventura");
        verify(categoryRepository, times(1)).findById(id);
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción al actualizar descripción de categoría inexistente")
    public void shouldNotUpdateDescripcion() {
        Long id = 999L;
        CategoryUpdateDescripcionDTO descripcionDTO = new CategoryUpdateDescripcionDTO();
        descripcionDTO.setDescripcion("Intento fallido");

        when(this.categoryRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> this.categoryService.updateDescripcion(id, descripcionDTO))
                .isInstanceOf(CategoryException.class)
                .hasMessage("Categoria no encontrada, no se puede actualizar la descripcion");

        verify(categoryRepository, times(1)).findById(id);
        verify(categoryRepository, never()).save(any(Category.class));
    }
}