package com.product.assemblers;

import com.product.controllers.ProductController;
import com.product.models.dtos.ProductResponseDTO;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class ProductModelAssembler implements RepresentationModelAssembler<ProductResponseDTO, EntityModel<ProductResponseDTO>> {

    @Override
    public EntityModel<ProductResponseDTO> toModel(ProductResponseDTO product) {
        return EntityModel.of(
                product,
                linkTo(methodOn(ProductController.class).findById(product.getId())).withSelfRel(),
                linkTo(methodOn(ProductController.class).findAll()).withRel("productos"),
                linkTo(methodOn(ProductController.class).deactivate(product.getId())).withRel("deactivate"),
                linkTo(methodOn(ProductController.class).update(product.getId(), null)).withRel("update")
        );
    }
}