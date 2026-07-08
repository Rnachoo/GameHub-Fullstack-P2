package com.promotion.controllers;

import com.promotion.assemblers.PromotionModelAssembler;
import com.promotion.models.dtos.PromotionAplicarDescuentoDTO;
import com.promotion.models.dtos.PromotionDetalleDTO;
import com.promotion.models.dtos.PromotionSaveDTO;
import com.promotion.models.dtos.PromotionUpdateDateDTO;
import com.promotion.services.PromotionService;
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
@RequestMapping("/api/v1/promociones")
@Validated
@Tag(
        name = "Promociones V1",
        description = "Métodos CRUD para la gestión de promociones, cupones y descuentos"
)
public class PromotionController {

    @Autowired
    private PromotionService promotionService;

    @Autowired
    private PromotionModelAssembler promotionModelAssembler;


    //Listar todas las promociones
    @GetMapping
    @Operation(
            summary = "Listado de todas las promociones",
            description = "Devuelve todas las promociones registradas en el sistema"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Operación exitosa",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = PromotionDetalleDTO.class)
            )
    )
    public ResponseEntity<CollectionModel<EntityModel<PromotionDetalleDTO>>> findAll() {
        List<EntityModel<PromotionDetalleDTO>> entityModels = this.promotionService.findAll()
                .stream()
                .map(promotionModelAssembler::toModel)
                .toList();

        CollectionModel<EntityModel<PromotionDetalleDTO>> collectionModel = CollectionModel.of(entityModels);
        return ResponseEntity.status(HttpStatus.OK).body(collectionModel);
    }


    //Buscar Promocion activa
    @GetMapping("/current")
    @Operation(
            summary = "Listado de promociones vigentes",
            description = "Devuelve todas las promociones activas y vigentes según la fecha actual"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Promociones encontradas",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PromotionDetalleDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "No existen promociones vigentes"
            )
    })
    public ResponseEntity<CollectionModel<EntityModel<PromotionDetalleDTO>>> findCurrent() {
        List<EntityModel<PromotionDetalleDTO>> entityModels = this.promotionService.findCurrent()
                .stream()
                .map(promotionModelAssembler::toModel)
                .toList();

        CollectionModel<EntityModel<PromotionDetalleDTO>> collectionModel = CollectionModel.of(entityModels);
        return ResponseEntity.status(HttpStatus.OK).body(collectionModel);
    }


    //Buscar promocion por ID
    @GetMapping("/{id}")
    @Operation(
            summary = "Búsqueda de promoción por ID",
            description = "Devuelve los detalles de una promoción específica"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Promoción encontrada",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PromotionDetalleDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Promoción no encontrada"
            )
    })
    public ResponseEntity<EntityModel<PromotionDetalleDTO>> findById(
            @Parameter(
                    description = "ID de la promoción a buscar",
                    required = true,
                    example = "1"
            )
            @PathVariable Long id
    ) {
        EntityModel<PromotionDetalleDTO> entityModel = this.promotionModelAssembler.toModel(this.promotionService.findById(id));
        return ResponseEntity.status(HttpStatus.OK).body(entityModel);
    }


    //buscar Promocion por Codigo
    @GetMapping("/codigo/{codigo}")
    @Operation(
            summary = "Búsqueda de promoción por código",
            description = "Devuelve una promoción utilizando su código promocional"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Promoción encontrada",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PromotionDetalleDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Código promocional no encontrado"
            )
    })
    public ResponseEntity<EntityModel<PromotionDetalleDTO>> findByCodigo(
            @Parameter(
                    description = "Código de la promoción",
                    required = true,
                    example = "GAMEHUB20"
            )
            @PathVariable String codigo
    ) {
        EntityModel<PromotionDetalleDTO> entityModel = this.promotionModelAssembler.toModel(this.promotionService.findByCodigo(codigo));
        return ResponseEntity.status(HttpStatus.OK).body(entityModel);
    }


    //Registrar una nueva promoción
    @PostMapping
    @Operation(
            summary = "Crear promoción",
            description = "Registra una nueva promoción o cupón de descuento"
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Datos de la promoción a registrar",
            required = true,
            content = @Content(
                    schema = @Schema(
                            implementation = PromotionSaveDTO.class
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Promoción creada correctamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PromotionDetalleDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos inválidos"
            )
    })
    public ResponseEntity<EntityModel<PromotionDetalleDTO>> save(
            @Valid @RequestBody PromotionSaveDTO promotionSaveDTO
    ) {
        PromotionDetalleDTO promotionCreate = this.promotionService.save(promotionSaveDTO);
        EntityModel<PromotionDetalleDTO> entityModel = this.promotionModelAssembler.toModel(promotionCreate);
        return ResponseEntity.status(HttpStatus.CREATED).body(entityModel);
    }


    //Desactivar una promoción por el ID
    @PatchMapping("/{id}")
    @Operation(
            summary = "Desactivar promoción",
            description = "Desactiva una promoción registrada sin eliminarla físicamente"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Promoción desactivada correctamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PromotionDetalleDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Promoción no encontrada"
            )
    })
    public ResponseEntity<EntityModel<PromotionDetalleDTO>> desactiveById(
            @Parameter(
                    description = "ID de la promoción a desactivar",
                    required = true,
                    example = "1"
            )
            @PathVariable Long id
    ) {
        PromotionDetalleDTO promotion = this.promotionService.desactiveById(id);
        EntityModel<PromotionDetalleDTO> entityModel = this.promotionModelAssembler.toModel(promotion);
        return ResponseEntity.status(HttpStatus.OK).body(entityModel);
    }


    //Actualizar la fecha de una promoción
    @PatchMapping("/{id}/date")
    @Operation(
            summary = "Actualizar fechas de promoción",
            description = "Actualiza la fecha de inicio y fecha de término de una promoción"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Fechas actualizadas correctamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PromotionDetalleDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Promoción no encontrada"
            )
    })
    public ResponseEntity<EntityModel<PromotionDetalleDTO>> updateDate(
            @Parameter(
                    description = "ID de la promoción",
                    required = true,
                    example = "1"
            )
            @PathVariable Long id,
            @Valid @RequestBody PromotionUpdateDateDTO promotionUpdateDateDTO
    ) {
        PromotionDetalleDTO promotionUpdate = this.promotionService.updateDate(id, promotionUpdateDateDTO);
        EntityModel<PromotionDetalleDTO> entityModel = this.promotionModelAssembler.toModel(promotionUpdate);
        return ResponseEntity.status(HttpStatus.OK).body(entityModel);
    }


    //Aplicar uan promoción en una orden
    @PostMapping("/{codigo}/aplicar")
    @Operation(
            summary = "Aplicar promoción a una orden",
            description = "Valida y aplica un descuento utilizando un código promocional sobre el total de una orden"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Promoción aplicada correctamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PromotionDetalleDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Promoción inválida o no aplicable"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Promoción no encontrada"
            )
    })
    public ResponseEntity<EntityModel<PromotionDetalleDTO>> aplicarPromocion(
            @Parameter(
                    description = "Código promocional",
                    required = true,
                    example = "GAMEHUB20"
            )
            @PathVariable("codigo") String codigo,

            @RequestBody PromotionAplicarDescuentoDTO aplicarDescuentoDTO,

            @Parameter(
                    description = "Monto total de la orden antes del descuento",
                    required = true,
                    example = "150000"
            )
            @RequestParam("totalOrden") Double totalOrden
    ) {

        PromotionDetalleDTO promotionAplicada = this.promotionService.aplicarPromocion(codigo, aplicarDescuentoDTO, totalOrden);
        EntityModel<PromotionDetalleDTO> entityModel = this.promotionModelAssembler.toModel(promotionAplicada);
        return ResponseEntity.status(HttpStatus.OK).body(entityModel);
    }
}