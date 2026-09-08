package com.renko.service.impl;

import com.renko.entities.CustomerEntity;
import com.renko.entities.StoreEntity;
import com.renko.exceptions.ExceptionMessages;
import com.renko.exceptions.UserException;
import com.renko.payload.dto.UserDto;
import com.renko.repository.CustomerRepository;
import com.renko.repository.StoreRepository;
import com.renko.service.CustomerService;
import com.renko.service.StoreAccessService;
import com.renko.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService
{
    private final CustomerRepository customerRepository;
    private final StoreRepository storeRepository;
    private final UserService userService;
    private final StoreAccessService storeAccessService;

    @Override
    public CustomerEntity createCustomer(CustomerEntity customerEntity) throws Exception
    {
        StoreEntity storeEntity = resolveStoreForCreate(customerEntity);
        customerEntity.setStoreEntity(storeEntity);
        return customerRepository.save(customerEntity);
    }

    @Override
    public CustomerEntity updateCustomer(Long id, CustomerEntity customerEntity) throws Exception
    {
        CustomerEntity customer = customerRepository.findById(id)
                .orElseThrow(() -> ExceptionMessages.notFound("Customer", id, "update"));

        if(customerEntity.getFullName() != null)
        {
            customer.setFullName(customerEntity.getFullName());
        }
        if(customerEntity.getEmail() != null)
        {
            customer.setEmail(customerEntity.getEmail());
        }

        if(customerEntity.getPhone() != null)
        {
            customer.setPhone(customerEntity.getPhone());
        }

        if(customerEntity.getStoreEntity() != null && customerEntity.getStoreEntity().getId() != null)
        {
            StoreEntity storeEntity = storeRepository.findById(customerEntity.getStoreEntity().getId())
                    .orElseThrow(() -> ExceptionMessages.notFound(
                            "Store",
                            customerEntity.getStoreEntity().getId(),
                            "update customerId=" + id
                    ));
            customer.setStoreEntity(storeEntity);
        }

        return customerRepository.save(customer);
    }

    @Override
    public void deleteCustomer(Long id) throws Exception
    {
        CustomerEntity customer = customerRepository.findById(id)
                .orElseThrow(() -> ExceptionMessages.notFound("Customer", id, "delete"));

        customerRepository.delete(customer);
    }

    @Override
    public CustomerEntity getCustomer(Long id) throws Exception
    {
        if(id == null)
        {
            throw ExceptionMessages.required("customerId");
        }
        return customerRepository.findById(id)
                .orElseThrow(() -> ExceptionMessages.notFound("Customer", id));
    }

    @Override
    public List<CustomerEntity> getAllCustomers()
    {
        return customerRepository.findAll();
    }

    @Override
    public List<CustomerEntity> getCustomersByStoreEntity_Id(Long id) throws Exception
    {
        storeAccessService.requireStoreAccess(id);
        return customerRepository.findByStoreEntity_Id(id);
    }

    @Override
    public List<CustomerEntity> searchCustomer(String keyword)
    {
        return customerRepository.findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(keyword, keyword);
    }

    @Override
    public void deleteAllCustomers()
    {
        customerRepository.deleteAll();
    }

    private StoreEntity resolveStoreForCreate(CustomerEntity customerEntity) throws Exception
    {
        if(customerEntity.getStoreEntity() != null && customerEntity.getStoreEntity().getId() != null)
        {
            Long storeId = customerEntity.getStoreEntity().getId();
            storeAccessService.requireStoreAccess(storeId);
            return storeRepository.findById(storeId)
                    .orElseThrow(() -> ExceptionMessages.notFound("Store", storeId, "create customer"));
        }

        UserDto currentUser = userService.getCurrentUser();
        if(currentUser.getStoreId() == null)
        {
            throw ExceptionMessages.required(
                    "storeId",
                    "A store is required before creating a customer. Create a store first or send storeEntity.id."
            );
        }

        storeAccessService.requireStoreAccess(currentUser.getStoreId());
        return storeRepository.findById(currentUser.getStoreId())
                .orElseThrow(() -> ExceptionMessages.notFound(
                        "Store",
                        currentUser.getStoreId(),
                        "create customer for userId=" + currentUser.getId()
                ));
    }
}
