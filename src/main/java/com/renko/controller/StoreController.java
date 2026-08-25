package com.renko.controller;

import com.renko.exceptions.UserException;
import com.renko.mapper.StoreMapper;
import com.renko.model.StoreEntity;
import com.renko.model.UserEntity;
import com.renko.payload.dto.StoreDto;
import com.renko.payload.response.ApiResponse;
import com.renko.service.StoreService;
import com.renko.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/store")
public class StoreController
{
    private final StoreService storeService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<StoreDto> createStore(@RequestBody StoreDto storeDto,
                                                @RequestHeader("Authorization") String jwt) throws UserException
    {
        UserEntity userEntity = userService.getUserFromJwtToken(jwt);
        return ResponseEntity.ok(storeService.createStore(storeDto, userEntity));
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<StoreDto> getStoreById(@PathVariable("id") Long id) throws Exception
    {
        return ResponseEntity.ok(storeService.getStoreById(id));
    }

    @GetMapping
    public ResponseEntity<List<StoreDto>> getAllStore() throws Exception
    {
        return ResponseEntity.ok(storeService.getAllStores());
    }

    @GetMapping("/admin")
    public ResponseEntity<StoreDto> getStoreByAdmin() throws Exception
    {
        return ResponseEntity.ok(StoreMapper.toDto(storeService.getStoreByAdmin()));
    }

    @GetMapping("/employee")
    public ResponseEntity<StoreDto> getStoreByEmployee() throws Exception
    {
        return ResponseEntity.ok(storeService.getStoreByEmployee());
    }

    @PutMapping("/{id}")
    public ResponseEntity<StoreDto> updateStore(@PathVariable("id") Long id,
                                                @RequestBody StoreDto storeDto) throws UserException
    {
        return ResponseEntity.ok(storeService.updateStore(id, storeDto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteStore(@PathVariable("id") Long id) throws UserException
    {
        storeService.deleteStore(id);
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setMessage("Store deleted successfully");

        return ResponseEntity.ok(apiResponse);
    }
}
