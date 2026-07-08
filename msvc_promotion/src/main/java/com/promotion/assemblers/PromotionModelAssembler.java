package com.promotion.assemblers;

import com.promotion.controllers.PromotionController;
import com.promotion.models.dtos.PromotionDetalleDTO;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class PromotionModelAssembler implements RepresentationModelAssembler<PromotionDetalleDTO, EntityModel<PromotionDetalleDTO>> {

    @Override
    public EntityModel<PromotionDetalleDTO> toModel(PromotionDetalleDTO promotion) {
        return EntityModel.of(
                promotion,
                linkTo(methodOn(PromotionController.class).findById(promotion.getId())).withSelfRel(),
                linkTo(methodOn(PromotionController.class).findAll()).withRel("promociones"),
                linkTo(methodOn(PromotionController.class).findCurrent()).withRel("current-promociones"),
                linkTo(methodOn(PromotionController.class).findByCodigo(promotion.getCodigo())).withRel("by-codigo"),
                linkTo(methodOn(PromotionController.class).desactiveById(promotion.getId())).withRel("desactive"),
                linkTo(methodOn(PromotionController.class).updateDate(promotion.getId(), null)).withRel("update-date"),
                linkTo(methodOn(PromotionController.class).aplicarPromocion(promotion.getCodigo(), null, null)).withRel("aplicar-promocion")
        );
    }
}