package com.product.controllers;

import com.product.assemblers.ProductModelAssembler;
import com.product.models.dtos.ProductRequestDTO;
import com.product.models.dtos.ProductResponseDTO;
import com.product.services.ProductService;
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

@RestController
@RequestMapping("/api/v1/productos")
@Validated
@Tag(
        name = "Productos V1",
        description = "Métodos CRUD para la gestión del catálogo de productos de GameHub Store"
)
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductModelAssembler productModelAssembler;


    //Listar todos los productos
    @GetMapping
    @Operation(
            summary = "Listado de productos",
            description = "Devuelve todos los productos registrados en el catálogo"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Operación exitosa",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ProductResponseDTO.class)
            )
    )
    public ResponseEntity<CollectionModel<EntityModel<ProductResponseDTO>>> findAll() {
        List<EntityModel<ProductResponseDTO>> entityModels = this.productService.getAllProducts()
                .stream()
                .map(productModelAssembler::toModel)
                .toList();

        CollectionModel<EntityModel<ProductResponseDTO>> collectionModel = CollectionModel.of(entityModels);
        return ResponseEntity.status(HttpStatus.OK).body(collectionModel);
    }


    //Buscar Producto por el ID
    @GetMapping("/{id}")
    @Operation(
            summary = "Búsqueda de producto por ID",
            description = "Devuelve los detalles de un producto específico"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Producto encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProductResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Producto no encontrado"
            )
    })
    public ResponseEntity<EntityModel<ProductResponseDTO>> findById(
            @Parameter(
                    description = "ID del producto a buscar",
                    required = true,
                    example = "1"
            )
            @PathVariable Long id
    ) {
        EntityModel<ProductResponseDTO> entityModel = this.productModelAssembler.toModel(this.productService.getProductById(id));
        return ResponseEntity.status(HttpStatus.OK).body(entityModel);
    }


    //Registrar nuevo Producto
    @PostMapping
    @Operation(
            summary = "Crear producto",
            description = "Registra un nuevo producto en el catálogo"
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Datos del producto a registrar",
            required = true,
            content = @Content(
                    schema = @Schema(
                            implementation = ProductRequestDTO.class
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Producto creado correctamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProductResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos inválidos"
            )
    })
    public ResponseEntity<EntityModel<ProductResponseDTO>> save(
            @Valid @RequestBody ProductRequestDTO requestDTO
    ) {
        ProductResponseDTO productCreate = this.productService.createProduct(requestDTO);
        EntityModel<ProductResponseDTO> entityModel = this.productModelAssembler.toModel(productCreate);
        return ResponseEntity.status(HttpStatus.CREATED).body(entityModel);
    }


    //Desactivar un producto
    @PatchMapping("/{id}")
    @Operation(
            summary = "Desactivar producto",
            description = "Desactiva un producto sin eliminarlo físicamente de la base de datos"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Producto desactivado correctamente"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Producto no encontrado"
            )
    })
    public ResponseEntity<Void> deactivate(
            @Parameter(
                    description = "ID del producto a desactivar",
                    required = true,
                    example = "1"
            )
            @PathVariable Long id
    ) {
        this.productService.deactivateProduct(id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }


    //Actualizar un Producto
    @PatchMapping("/{id}/update")
    @Operation(
            summary = "Actualizar producto",
            description = "Actualiza la información comercial y técnica de un producto registrado"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Producto actualizado correctamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProductResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Producto no encontrado"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos inválidos"
            )
    })
    public ResponseEntity<EntityModel<ProductResponseDTO>> update(
            @Parameter(
                    description = "ID del producto a actualizar",
                    required = true,
                    example = "1"
            )
            @PathVariable Long id,
            @Valid @RequestBody ProductRequestDTO requestDTO
    ) {
        ProductResponseDTO productUpdate = this.productService.updateProduct(id, requestDTO);
        EntityModel<ProductResponseDTO> entityModel = this.productModelAssembler.toModel(productUpdate);
        return ResponseEntity.status(HttpStatus.OK).body(entityModel);
    }
}