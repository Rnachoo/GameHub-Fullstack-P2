package com.category.controllers;

import com.category.assemblers.CategoryModelAssembler;
import com.category.models.dtos.CategoryDetalleDTO;
import com.category.models.dtos.CategorySaveDTO;
import com.category.models.dtos.CategoryUpdateDescripcionDTO;
import com.category.models.dtos.CategoryUpdateNombreDTO;
import com.category.services.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/v1/categories")
@Validated
@Tag(name = "Categorias V1", description = "Metodos Crud para la gestión de categorias")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CategoryModelAssembler categoryModelAssembler;

    //Buscar Todas las categorias registradas
    @GetMapping
    @Operation(
            summary = "Listado de todas las categorías",
            description = "Devuelve una lista con las categorias registradas"
    )
    @ApiResponse(responseCode = "200", description = "Operación Exitosa")
    public ResponseEntity<CollectionModel<EntityModel<CategoryDetalleDTO>>> findAll(){
        List<EntityModel<CategoryDetalleDTO>> entityModels = this.categoryService.findAll()
                .stream()
                .map(categoryModelAssembler::toModel)
                .toList();
        CollectionModel<EntityModel<CategoryDetalleDTO>> collectionModel = CollectionModel.of(
                entityModels,
                linkTo(methodOn(CategoryController.class).findAll()).withSelfRel()
        );

        return ResponseEntity.status(HttpStatus.OK).body(collectionModel);
    }

    //Buscar Por ID
    @GetMapping("/{id}")
    @Operation(
            summary = "Busqueda de una categoria por ID",
            description = "Devuelve los detalles de una categoría especifica. Lanza una excepción si no la encuentra"
    )
    @ApiResponses(value={
            @ApiResponse(
                    responseCode = "200",
                    description = "Categoria encontrada",
                        content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategoryDetalleDTO.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Categoria no encontrada en la base de datos")
    })
    public ResponseEntity<EntityModel<CategoryDetalleDTO>> findById(
            @Parameter(description = "ID de la categoria a buscar", required = true, example = "1")
            @PathVariable Long id
    ){
        EntityModel<CategoryDetalleDTO> entityModel = this.categoryModelAssembler.toModel(categoryService.findById(id));
        return ResponseEntity.ok(entityModel);
    }

    //Guardar una categoria
    @PostMapping
    @Operation(summary = "Creacion de una categoria", description = "Registra una categoria nueva en el sistema")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Categoria a crear", required = true,
            content = @Content(schema = @Schema(implementation = CategorySaveDTO.class))
    )
    public ResponseEntity<EntityModel<CategoryDetalleDTO>> save(@Valid @RequestBody CategorySaveDTO categorySaveDTO){
        CategoryDetalleDTO categoryCreate = this.categoryService.save(categorySaveDTO);
        EntityModel<CategoryDetalleDTO> entityModel = this.categoryModelAssembler.toModel(categoryCreate);
        return ResponseEntity.status(HttpStatus.CREATED).body(entityModel);
    }

    //Desactivar (No eliminar) por el ID
    @PatchMapping("/{id}")
    @Operation(summary = "Desactivación de una categoria", description = "Desactiva una categoria registrada")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categoria desactivada con exito"),
            @ApiResponse(responseCode = "400", description = "Categoria no encontrada")
    })
    public ResponseEntity<EntityModel<CategoryDetalleDTO>> desactiveByID(
            @Parameter(description = "ID de la categoria a desactivar", required = true, example = "1")
            @PathVariable Long id
    ){
        CategoryDetalleDTO categoryDesactive = categoryService.desactiveById(id);
        EntityModel<CategoryDetalleDTO> entityModel = this.categoryModelAssembler.toModel(categoryDesactive);
        return ResponseEntity.ok(entityModel);
    }

    //Actualizar Nombre de unaCategoria
    @PatchMapping("/{id}/nombreCategory")
    @Operation(summary = "Actualizar el nombre de una categoria", description = "Actualiza solamente el nombre de la categoria encontrada con el id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Nombre de la categoria actualizada con exito"),
            @ApiResponse(responseCode = "400", description = "Categoria no encontrada")
    })
    public ResponseEntity <EntityModel<CategoryDetalleDTO>> updateNombre(
            @Parameter(description = "ID de la categoria", required = true, example = "1")
            @PathVariable Long id,
            @Valid @RequestBody CategoryUpdateNombreDTO nombreDTO
    ){
        CategoryDetalleDTO categoryUpdate = categoryService.updateNombre(id, nombreDTO);
        EntityModel<CategoryDetalleDTO> entityModel = this.categoryModelAssembler.toModel(categoryUpdate);
        return ResponseEntity.status(HttpStatus.OK).body(entityModel);
    }
    //Actualizar Descripción de unaCategoria
    @PatchMapping("/{id}/descripcion")
    @Operation(summary = "Actualizar la descripción de una categoria", description = "Actualiza solamente la descripción de la categoria encontrada con el id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Descripción de la categoria actualizada con exito"),
            @ApiResponse(responseCode = "400", description = "Categoria no encontrada")
    })
    public ResponseEntity <EntityModel<CategoryDetalleDTO>> updateDescripcion(
            @Parameter(description = "ID de la categoria", required = true, example = "1")
            @PathVariable Long id,
            @Valid @RequestBody CategoryUpdateDescripcionDTO descripcionDTO
    ){
        CategoryDetalleDTO categoryUpdate = categoryService.updateDescripcion(id, descripcionDTO);
        EntityModel<CategoryDetalleDTO> entityModel = this.categoryModelAssembler.toModel(categoryUpdate);
        return ResponseEntity.status(HttpStatus.OK).body(entityModel);
    }

}
