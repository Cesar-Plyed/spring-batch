package com.example.batch.processor;

import java.time.LocalDateTime;

import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.stereotype.Component;

import com.example.batch.model.Product;
import com.example.batch.model.ProductCSV;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ProductProcessor implements ItemProcessor<ProductCSV, Product> {@Override
    public Product process(ProductCSV item) throws Exception {
        if (item.getPrice() == null || item.getPrice() <= 0){
            log.warn("Invalid price, removing: {}", item.getName());
            return null;
        }

        Product product = new Product();
        product.setName(item.getName());
        product.setPrice(item.getPrice());
        product.setCategory(item.getCategory());
        product.setStock(item.getStock());

        product.setAvailable(item.getStock() != null && item.getStock() > 0);

        product.setPriceLevel(calculateProceLevel(item.getPrice()));

        product.setProcessedDate(LocalDateTime.now());

        return product;
    }
    
    private String calculateProceLevel(double price) {
        if (price < 500)        return "CHEAP";
        else if (price < 5000)  return "MEDIUM";
        else                     return "PREMIUM";
    }

}
