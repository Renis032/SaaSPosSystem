package com.renko.controller;

import com.renko.entities.CustomerEntity;
import com.renko.exceptions.UserException;
import com.renko.payload.dto.UserDto;
import com.renko.payload.response.ApiResponse;
import com.renko.service.CustomerService;
import com.renko.service.StoreService;
import com.renko.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/customers")
public class CustomerController
{
    private final CustomerService customerService;
    private final UserService userService;
    private final StoreService storeService;

    @PostMapping
    public ResponseEntity<CustomerEntity> createCustomer(@RequestBody CustomerEntity customerEntity,
                                                         @RequestHeader("Authorization") String jwt) throws Exception
    {
//        UserDto user = userService.getUserFromJwtToken(jwt);
//        if(user.getStoreEntity() == null)
//        {
//            throw new Exception("User not associated with any store");
//        }

//        customerEntity.setStoreEntity(user.getStoreEntity());
        return ResponseEntity.ok(customerService.createCustomer(customerEntity));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerEntity> updateCustomer(@PathVariable Long id,
                                                         @RequestBody CustomerEntity customerEntity) throws Exception
    {
        return ResponseEntity.ok(customerService.updateCustomer(id, customerEntity));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteCustomer(@PathVariable Long id) throws Exception
    {
        customerService.deleteCustomer(id);

        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setMessage("Customer deleted successfully");
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping
    public ResponseEntity<List<CustomerEntity>> getAllCustomer(@RequestHeader("Authorization") String jwt) throws Exception
    {
        UserDto userDto = userService.getUserFromJwtToken(jwt);
        if(userDto.getStoreId() != null)
        {
            Long storeId = storeService.getStoreById(userDto.getStoreId()).getId();
            return ResponseEntity.ok(customerService.getCustomersByStoreEntity_Id(storeId));
        }

        return ResponseEntity.ok(customerService.getAllCustomers());
    }

    @GetMapping("/search")
    public ResponseEntity<List<CustomerEntity>> searchCustomer(@RequestParam String keyword)
    {
        return ResponseEntity.ok(customerService.searchCustomer(keyword));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerEntity> getCustomerById(@PathVariable Long id) throws Exception
    {
        return ResponseEntity.ok(customerService.getCustomer(id));
    }
}
