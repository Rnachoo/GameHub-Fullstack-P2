package com.category.services;

import com.category.models.dtos.CategoryDetalleDTO;
import com.category.models.dtos.CategorySaveDTO;
import com.category.models.dtos.CategoryUpdateDescripcionDTO;
import com.category.models.dtos.CategoryUpdateNombreDTO;

import java.util.List;

public interface CategoryService {
    List<CategoryDetalleDTO> findAll();
    CategoryDetalleDTO findById(Long id);
    CategoryDetalleDTO save (CategorySaveDTO categorySaveDTO);
    CategoryDetalleDTO desactiveById(Long id);
    CategoryDetalleDTO updateNombre(Long id, CategoryUpdateNombreDTO nombreDTO);
    CategoryDetalleDTO updateDescripcion(Long id, CategoryUpdateDescripcionDTO descripcionDTO);

}
