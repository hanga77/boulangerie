package lab.hang.boulangerie.services.impl;

import lab.hang.boulangerie.dto.ProductDTO;
import lab.hang.boulangerie.entity.Products;
import lab.hang.boulangerie.repository.ProductRepository;
import lab.hang.boulangerie.services.ProductsServices;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ProductsServicesImpl implements ProductsServices {

    private final ProductRepository productRepository;
    @Override
    public void SaveProducts(ProductDTO productDTO) {
        productDTO.setActive(true);
        Products products = mapToProduct(productDTO);
        productRepository.save(products);
    }

    @Override
    public List<ProductDTO> getAllProducts() {
        return productRepository.findAll(Sort.by(Sort.Direction.DESC,"dateCreated")).stream().map(this::mapToProductsDTO).collect(Collectors.toList());
    }

    @Override
    public void DeleteProducts(Long id) {
        Products products = productRepository.getReferenceById(id);
        products.setActive(false);
        productRepository.saveAndFlush(products);
    }

    @Override
    public ProductDTO UpdatedProduct(ProductDTO productDTO) {
        Products products = productRepository.findProductsByProductName(productDTO.getProductName()).orElseThrow();
        products.setProductName(productDTO.getProductName());
        products.setPrix(productDTO.getPrix());
        products.setActive(true);
        productRepository.saveAndFlush(products);
        return mapToProductsDTO(products);
    }

    @Override
    public ProductDTO GetProduct(ProductDTO productDTO) {
        return productRepository.findProductsByProductName(productDTO.getProductName()).map(this::mapToProductsDTO).get();
    }

    @Override
    public ProductDTO getProduct(String product) {
        return productRepository.findProductsByProductName(product).map(this::mapToProductsDTO).orElseThrow();
    }

    @Override
    public Products getProducts(String product) {
        return productRepository.findProductsByProductName(product).orElseThrow();
    }

    @Override
    public ProductDTO mapToProductsDTO(Products products){
        ProductDTO productDTO = new ProductDTO();
        productDTO.setProductID(products.getProductID());
        productDTO.setActive(products.isActive());
        productDTO.setDateCreated(products.getDateCreated());
        productDTO.setProductName(products.getProductName());
        productDTO.setPrix(products.getPrix());
        productDTO.setStandardProduction(products.getStandardProduction());
        return productDTO;
    }

    @Override
    public Products mapToProduct(ProductDTO productDTO){
        Products products = new Products();
        products.setActive(productDTO.isActive());
        products.setProductID(productDTO.getProductID());
        products.setDateCreated(productDTO.getDateCreated());
        products.setProductName(productDTO.getProductName());
        products.setPrix(productDTO.getPrix());
        products.setStandardProduction(productDTO.getStandardProduction());
        return products;
    }
}
