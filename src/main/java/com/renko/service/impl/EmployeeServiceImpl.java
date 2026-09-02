package com.renko.service.impl;

import com.renko.domain.UserRole;
import com.renko.entities.StoreEntity;
import com.renko.entities.UserEntity;
import com.renko.mapper.UserMapper;
import com.renko.payload.dto.CreateEmployeeDto;
import com.renko.payload.dto.UserDto;
import com.renko.payload.dto.updates.UserUpdateDto;
import com.renko.payload.response.ApiResponse;
import com.renko.repository.StoreRepository;
import com.renko.repository.UserRepository;
import com.renko.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService
{
    private final StoreRepository storeRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    @Override
    public UserDto createStoreEmployee(CreateEmployeeDto employee, Long storeId) throws Exception
    {
        StoreEntity storeEntity = storeRepository.findById(storeId)
                                                 .orElseThrow(() -> new Exception("Store not found"));

        UserEntity userEntity = new UserEntity();
        userEntity.setEmail(employee.getEmail());
        userEntity.setFullName(employee.getFullName());
        userEntity.setPhoneNumber(employee.getPhoneNumber());
        userEntity.setRole(employee.getRole());
        userEntity.setStoreEntity(storeEntity);
        userEntity.setPassword(passwordEncoder.encode(employee.getPassword()));

        UserEntity savedUser = userRepository.save(userEntity);

        return UserMapper.toDto(savedUser);
    }

    @Override
    public UserDto updateStoreEmployee(Long employeeId, UserUpdateDto employeeDto) throws Exception
    {
        UserEntity existingEmployee = userRepository.findById(employeeId)
                                                    .orElseThrow(() -> new Exception("Employee does not exist"));

        existingEmployee.updateFrom(employeeDto);
        // Update password only if non-empty
        if(employeeDto.getPassword() != null && false == employeeDto.getPassword().isBlank())
        {
            existingEmployee.setPassword(passwordEncoder.encode(employeeDto.getPassword()));
        }

        // Update store if provided
        if(employeeDto.getStoreEntity() != null)
        {
            StoreEntity storeEntity = storeRepository.findById(employeeDto.getStoreEntity().getId())
                                                     .orElseThrow(() -> new Exception("Store not found"));

            existingEmployee.setStoreEntity(storeEntity);
        }

        UserEntity savedUser = userRepository.save(existingEmployee);

        return UserMapper.toDto(savedUser);
    }

    @Override
    public void deleteEmployee(Long employeeId) throws Exception
    {
        UserEntity employee = userRepository.findById(employeeId)
                                            .orElseThrow(()-> new Exception("Employee not found"));

        userRepository.delete(employee);
    }

    @Override
    public List<UserDto> findStoreEmployeesByRole(Long storeId, UserRole role) throws Exception
    {
        StoreEntity storeEntity = storeRepository.findById(storeId)
                                                 .orElseThrow(() -> new Exception("Store not found"));

        return userRepository.findByStoreEntity(storeEntity)
                             .stream()
                             .filter(user -> role == null || user.getRole() == role)
                             .map(UserMapper::toDto)
                             .collect(Collectors.toList());
    }
}
