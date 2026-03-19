package com.codebugs.productservice.service;

import com.codebugs.productservice.dto.ProductRequest;
import com.codebugs.productservice.dto.ProductResponse;
import com.codebugs.productservice.exception.ProductNotFoundException;
import com.codebugs.productservice.model.Product;
import com.codebugs.productservice.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public ProductResponse createProduct(ProductRequest request) {
        Product product = new Product(
            request.name(), request.description(),
            request.price(), request.stock(), request.category());
        Product saved = productRepository.save(product);
        log.info("Product created: {}", saved.getId());
        return ProductResponse.from(saved);
    }

    public ProductResponse getProduct(String id) {
        return productRepository.findById(id)
            .map(ProductResponse::from)
            .orElseThrow(() -> new ProductNotFoundException(id));
    }

    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
            .map(ProductResponse::from)
            .toList();
    }

    public List<ProductResponse> getByCategory(String category) {
        return productRepository.findByCategory(category).stream()
            .map(ProductResponse::from)
            .toList();
    }

    public ProductResponse updateProduct(String id, ProductRequest request) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ProductNotFoundException(id));

        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setStock(request.stock());
        product.setCategory(request.category());

        Product saved = productRepository.save(product);
        log.info("Product updated: {}", saved.getId());
        return ProductResponse.from(saved);
    }

    public void deleteProduct(String id) {
        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException(id);
        }
        productRepository.deleteById(id);
        log.info("Product deleted: {}", id);
    }
}
