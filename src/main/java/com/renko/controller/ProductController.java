package com.renko.controller;

import com.renko.entities.ProductEntity;
import com.renko.entities.UserEntity;
import com.renko.payload.dto.ProductDto;
import com.renko.payload.response.ApiResponse;
import com.renko.service.ProductService;
import com.renko.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController
{
    private final ProductService productService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<ProductDto> createProduct(@RequestBody ProductDto productDto,
                                                    @RequestHeader("Authorization") String jwt) throws Exception
    {
        UserEntity userEntity = userService.getUserFromJwtToken(jwt);
        return ResponseEntity.ok(productService.createProduct(productDto, userEntity));
    }


    @GetMapping("/store/{storeId}")
    public ResponseEntity<List<ProductDto>> getByStoreId(@PathVariable Long storeId,
                                                         @RequestHeader("Authorization") String jwt) throws Exception
    {
        return ResponseEntity.ok(productService.getProductsByStoreId(storeId));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ProductDto> updateProduct(@PathVariable Long id,
                                                    @RequestBody ProductDto productDto,
                                                    @RequestHeader("Authorization") String jwt) throws Exception
    {
        UserEntity userEntity = userService.getUserFromJwtToken(jwt);
        return ResponseEntity.ok(productService.updateProduct(id, productDto, userEntity));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteProduct(@PathVariable Long id,
                                                     @RequestHeader("Authorization") String jwt) throws Exception
    {
        UserEntity userEntity = userService.getUserFromJwtToken(jwt);
        productService.deleteProduct(id, userEntity);

        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setMessage("Product deleted successfully");
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/store/{storeId}/search")
    public ResponseEntity<List<ProductDto>> searchByKeyword(@PathVariable Long storeId,
                                                           @RequestParam String keyword,
                                                           @RequestHeader("Authorization") String jwt)
    {
        return ResponseEntity.ok(productService.searchByKeyword(storeId, keyword));
    }
}
