package lab.hang.boulangerie.services;

import lab.hang.boulangerie.dto.ProductDTO;
import lab.hang.boulangerie.entity.Products;

import java.util.List;

public interface ProductsServices {

    void SaveProducts(ProductDTO productDTO);
    List<ProductDTO> getAllProducts();
    void DeleteProducts(Long id);
    ProductDTO UpdatedProduct(ProductDTO productDTO);
    ProductDTO GetProduct(ProductDTO productDTO);

    ProductDTO getProduct(String product);

    Products getProducts(String product);

    ProductDTO mapToProductsDTO(Products products);

    Products mapToProduct(ProductDTO productDTO);
}
