package com.renko.service;

import com.renko.entities.CustomerEntity;

import java.util.List;

public interface CustomerService
{
    CustomerEntity createCustomer(CustomerEntity customerEntity);
    CustomerEntity updateCustomer(Long id, CustomerEntity customerEntity) throws Exception;
    void deleteCustomer(Long id) throws Exception;
    CustomerEntity getCustomer(Long id) throws Exception;
    List<CustomerEntity> getAllCustomers();
    List<CustomerEntity> getCustomersByStoreEntity_Id(Long id);
    List<CustomerEntity> searchCustomer(String keyword);
    void deleteAllCustomers();
}
