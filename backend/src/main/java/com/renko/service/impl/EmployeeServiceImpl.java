package com.renko.service.impl;

import com.renko.domain.UserRole;
import com.renko.entities.BranchEntity;
import com.renko.entities.OrderEntity;
import com.renko.entities.RefundEntity;
import com.renko.entities.ShiftReportEntity;
import com.renko.entities.StoreEntity;
import com.renko.entities.UserEntity;
import com.renko.exceptions.ExceptionMessages;
import com.renko.mapper.UserMapper;
import com.renko.payload.dto.CreateEmployeeDto;
import com.renko.payload.dto.UserDto;
import com.renko.payload.dto.updates.UserUpdateDto;
import com.renko.repository.BranchRepository;
import com.renko.repository.OrderRepository;
import com.renko.repository.RefundRepository;
import com.renko.repository.ShiftReportRepository;
import com.renko.repository.StoreRepository;
import com.renko.repository.UserRepository;
import com.renko.service.EmployeeService;
import com.renko.service.AuditLogService;
import com.renko.service.StoreAccessService;
import com.renko.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService
{
    private final StoreRepository storeRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final UserService userService;
    private final OrderRepository orderRepository;
    private final RefundRepository refundRepository;
    private final ShiftReportRepository shiftReportRepository;
    private final BranchRepository branchRepository;
    private final StoreAccessService storeAccessService;
    private final AuditLogService auditLogService;

    @Override
    public UserDto createStoreEmployee(CreateEmployeeDto employee, Long storeId) throws Exception
    {
        if(storeId == null)
        {
            throw ExceptionMessages.required(
                    "storeId",
                    "storeId is required to create an employee. Create a store first."
            );
        }

        storeAccessService.requireStoreAccess(storeId);

        StoreEntity storeEntity = storeRepository.findById(storeId)
                .orElseThrow(() -> ExceptionMessages.notFound(
                        "Store",
                        storeId,
                        "create employee email=" + employee.getEmail()
                ));

        UserEntity userEntity = new UserEntity();
        userEntity.setEmail(employee.getEmail());
        userEntity.setFullName(employee.getFullName());
        userEntity.setPhoneNumber(employee.getPhoneNumber());
        userEntity.setRole(employee.getRole());
        userEntity.setStoreEntity(storeEntity);
        userEntity.setPassword(passwordEncoder.encode(employee.getPassword()));

        UserEntity savedUser = userRepository.save(userEntity);
        auditLogService.record(
                storeId,
                "EMPLOYEE_CREATE",
                "User",
                String.valueOf(savedUser.getId()),
                "role=" + savedUser.getRole() + "; email=" + savedUser.getEmail()
        );

        return UserMapper.toDto(savedUser);
    }

    @Override
    public UserDto getEmployeeById(Long employeeId) throws Exception
    {
        UserEntity employee = userRepository.findById(employeeId)
                .orElseThrow(() -> ExceptionMessages.notFound("Employee", employeeId));
        return UserMapper.toDto(employee);
    }

    @Override
    public List<UserDto> getAllEmployees()
    {
        return userRepository.findAll().stream()
                .filter(user -> user.getRole() != UserRole.ADMIN && user.getRole() != UserRole.OWNER)
                .map(UserMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto updateStoreEmployee(Long employeeId, UserUpdateDto employeeDto) throws Exception
    {
        UserEntity existingEmployee = userRepository.findById(employeeId)
                .orElseThrow(() -> ExceptionMessages.notFound("Employee", employeeId, "update"));

        existingEmployee.updateFrom(employeeDto);
        if(employeeDto.getPassword() != null && false == employeeDto.getPassword().isBlank())
        {
            existingEmployee.setPassword(passwordEncoder.encode(employeeDto.getPassword()));
        }

        if(employeeDto.getStoreId() != null)
        {
            storeAccessService.requireStoreAccess(employeeDto.getStoreId());
            StoreEntity storeEntity = storeRepository.findById(employeeDto.getStoreId())
                    .orElseThrow(() -> ExceptionMessages.notFound(
                            "Store",
                            employeeDto.getStoreId(),
                            "reassign employeeId=" + employeeId
                    ));

            existingEmployee.setStoreEntity(storeEntity);
        }

        UserEntity savedUser = userRepository.save(existingEmployee);

        return UserMapper.toDto(savedUser);
    }

    @Override
    public void deleteEmployee(Long employeeId) throws Exception
    {
        // Same FK guards as /api/users/{id} — cashiers with orders/refunds/shifts cannot be removed
        userService.deleteById(employeeId);
    }

    @Override
    @Transactional
    public void deleteAllEmployees() throws Exception
    {
        List<UserEntity> employees = userRepository.findAll().stream()
                .filter(user -> user.getRole() != UserRole.ADMIN && user.getRole() != UserRole.OWNER)
                .toList();

        for(UserEntity employee : employees)
        {
            forceDeleteEmployee(employee);
        }
    }

    private void forceDeleteEmployee(UserEntity employee)
    {
        Long id = employee.getId();

        List<RefundEntity> refunds = refundRepository.findByCashierEntity_Id(id);
        refundRepository.deleteAll(refunds);

        List<ShiftReportEntity> shifts = shiftReportRepository.findByCashierEntity_Id(id);
        shiftReportRepository.deleteAll(shifts);

        List<OrderEntity> orders = orderRepository.findByCashierEntity_Id(id);
        orderRepository.deleteAll(orders);

        for(BranchEntity branch : branchRepository.findByUserEntity_Id(id))
        {
            branch.setUserEntity(null);
            branchRepository.save(branch);
        }

        StoreEntity adminOfStore = storeRepository.findByStoreAdmin_Id(id);
        if(adminOfStore != null)
        {
            adminOfStore.setStoreAdmin(null);
            storeRepository.save(adminOfStore);
        }

        employee.setStoreEntity(null);
        userRepository.save(employee);
        userRepository.delete(employee);
    }

    @Override
    public List<UserDto> findStoreEmployeesByRole(Long storeId, UserRole role) throws Exception
    {
        storeAccessService.requireStoreAccess(storeId);
        StoreEntity storeEntity = storeRepository.findById(storeId)
                                                 .orElseThrow(() -> ExceptionMessages.notFound(
                                                         "Store",
                                                         storeId,
                                                         "list employees" + (role != null ? " for role=" + role : "")
                                                 ));

        return userRepository.findByStoreEntity(storeEntity)
                             .stream()
                             .filter(user -> role == null || user.getRole() == role)
                             .map(UserMapper::toDto)
                             .collect(Collectors.toList());
    }
}
