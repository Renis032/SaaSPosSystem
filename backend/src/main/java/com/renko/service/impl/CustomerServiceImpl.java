package com.renko.service.impl;

import com.renko.entities.CustomerEntity;
import com.renko.repository.CustomerRepository;
import com.renko.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService
{
    private final CustomerRepository customerRepository;

    @Override
    public CustomerEntity createCustomer(CustomerEntity customerEntity)
    {
        return customerRepository.save(customerEntity);
    }

    @Override
    public CustomerEntity updateCustomer(Long id, CustomerEntity customerEntity) throws Exception
    {
        CustomerEntity customer = customerRepository.findById(id)
                                                    .orElseThrow(() -> new Exception("Customer not found with id: " + id + "; cannot update"));

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

        return customerRepository.save(customer);
    }

    @Override
    public void deleteCustomer(Long id) throws Exception
    {
        CustomerEntity customer = customerRepository.findById(id)
                .orElseThrow(() -> new Exception("Customer not found with id: " + id + "; cannot delete"));

        customerRepository.deleteById(id);
    }

    @Override
    public CustomerEntity getCustomer(Long id) throws Exception
    {
        return customerRepository.findById(id)
                .orElseThrow(() -> new Exception("Customer not found with id: " + id));
    }

    @Override
    public List<CustomerEntity> getAllCustomers()
    {
        return customerRepository.findAll();
    }

    @Override
    public List<CustomerEntity> getCustomersByStoreEntity_Id(Long id)
    {
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
}
