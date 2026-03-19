package com.codebugs.productservice.service;

import com.codebugs.productservice.dto.ProductRequest;
import com.codebugs.productservice.dto.ProductResponse;
import com.codebugs.productservice.exception.ProductNotFoundException;
import com.codebugs.productservice.model.Product;
import com.codebugs.productservice.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product product;
    private ProductRequest request;

    @BeforeEach
    void setUp() {
        product = new Product("Laptop", "Gaming laptop", new BigDecimal("999.99"), 10, "Electronics");
        setId(product, "prod-1");
        request = new ProductRequest("Laptop", "Gaming laptop", new BigDecimal("999.99"), 10, "Electronics");
    }

    @Test
    void createProduct_savesAndReturnsResponse() {
        when(productRepository.save(any())).thenReturn(product);

        ProductResponse response = productService.createProduct(request);

        assertThat(response.name()).isEqualTo("Laptop");
        assertThat(response.price()).isEqualByComparingTo("999.99");
        verify(productRepository).save(any());
    }

    @Test
    void getProduct_returnsProduct() {
        when(productRepository.findById("prod-1")).thenReturn(Optional.of(product));

        ProductResponse response = productService.getProduct("prod-1");

        assertThat(response.id()).isEqualTo("prod-1");
        assertThat(response.category()).isEqualTo("Electronics");
    }

    @Test
    void getProduct_notFound_throws() {
        when(productRepository.findById("x")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProduct("x"))
            .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void getAllProducts_returnsAll() {
        when(productRepository.findAll()).thenReturn(List.of(product));

        List<ProductResponse> responses = productService.getAllProducts();

        assertThat(responses).hasSize(1);
    }

    @Test
    void getByCategory_filtersCorrectly() {
        when(productRepository.findByCategory("Electronics")).thenReturn(List.of(product));

        List<ProductResponse> responses = productService.getByCategory("Electronics");

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).category()).isEqualTo("Electronics");
    }

    @Test
    void updateProduct_updatesFields() {
        when(productRepository.findById("prod-1")).thenReturn(Optional.of(product));
        when(productRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ProductRequest updateRequest = new ProductRequest(
            "Laptop Pro", "Updated", new BigDecimal("1199.99"), 5, "Electronics");

        ProductResponse response = productService.updateProduct("prod-1", updateRequest);

        assertThat(response.name()).isEqualTo("Laptop Pro");
        assertThat(response.price()).isEqualByComparingTo("1199.99");
    }

    @Test
    void deleteProduct_deletesWhenExists() {
        when(productRepository.existsById("prod-1")).thenReturn(true);

        productService.deleteProduct("prod-1");

        verify(productRepository).deleteById("prod-1");
    }

    @Test
    void deleteProduct_notFound_throws() {
        when(productRepository.existsById("x")).thenReturn(false);

        assertThatThrownBy(() -> productService.deleteProduct("x"))
            .isInstanceOf(ProductNotFoundException.class);
    }

    private static void setId(Product p, String id) {
        try {
            var field = p.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(p, id);
        } catch (Exception e) { throw new RuntimeException(e); }
    }
}
