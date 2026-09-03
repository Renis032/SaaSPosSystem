package com.renko.service.impl;

import com.renko.configuration.JwtProvider;
import com.renko.domain.UserRole;
import com.renko.entities.BranchEntity;
import com.renko.entities.StoreEntity;
import com.renko.entities.UserEntity;
import com.renko.exceptions.UserException;
import com.renko.mapper.UserMapper;
import com.renko.payload.dto.UserDto;
import com.renko.repository.BranchRepository;
import com.renko.repository.OrderRepository;
import com.renko.repository.RefundRepository;
import com.renko.repository.ShiftReportRepository;
import com.renko.repository.StoreRepository;
import com.renko.repository.UserRepository;
import com.renko.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService
{
    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final OrderRepository orderRepository;
    private final RefundRepository refundRepository;
    private final ShiftReportRepository shiftReportRepository;
    private final StoreRepository storeRepository;
    private final BranchRepository branchRepository;

    @Override
    public UserDto getUserFromJwtToken(String token) throws UserException
    {
        String email = jwtProvider.getEmailFromToken(token);
        UserEntity userEntity = userRepository.findByEmail(email);

        if(userEntity == null)
        {
            throw UserException.withDetail(
                    "JWT is valid but no user matches the token email",
                    "email",
                    email
            );
        }

        return UserMapper.toDto(userEntity);
    }

    @Override
    public UserDto getCurrentUser() throws UserException
    {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity currentUserEntity = userRepository.findByEmail(email);
        if(currentUserEntity == null)
        {
            throw UserException.withDetail(
                    "Authenticated principal does not match any user in the database",
                    "email",
                    email
            );
        }

        return  UserMapper.toDto(currentUserEntity);
    }

    @Override
    public UserDto getUserByEmail(String email) throws UserException
    {
        UserEntity userEntity = userRepository.findByEmail(email);
        if(userEntity == null)
        {
            throw UserException.withDetail(
                    "User not found with the given email",
                    "email",
                    email
            );
        }

        return  UserMapper.toDto(userEntity);
    }

    @Override
    public UserDto getUserById(Long id) throws Exception
    {
        return UserMapper.toDto(userRepository.findById(id).orElseThrow(() ->
                new Exception("User not found with id: " + id)));
    }

    @Override
    public List<UserDto> getAllUsers()
    {
        List<UserEntity> users = userRepository.findAll();
        return users.stream()
                    .map(UserMapper::toDto)
                    .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteById(Long id) throws UserException
    {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> UserException.withDetail(
                        "User not found; cannot delete",
                        "userId",
                        id
                ));

        int orderCount = orderRepository.findByCashierEntity_Id(id).size();
        int refundCount = refundRepository.findByCashierEntity_Id(id).size();
        int shiftCount = shiftReportRepository.findByCashierEntity_Id(id).size();
        int branchCount = branchRepository.findByUserEntity_Id(id).size();
        StoreEntity adminOfStore = storeRepository.findByStoreAdmin_Id(id);

        Map<String, Object> blockers = new LinkedHashMap<>();
        if(orderCount > 0)
        {
            blockers.put("ordersAsCashier", orderCount);
        }
        if(refundCount > 0)
        {
            blockers.put("refundsAsCashier", refundCount);
        }
        if(shiftCount > 0)
        {
            blockers.put("shiftReportsAsCashier", shiftCount);
        }
        if(branchCount > 0)
        {
            blockers.put("branchesAsManager", branchCount);
        }
        if(adminOfStore != null)
        {
            blockers.put("storeAdminOfStoreId", adminOfStore.getId());
        }

        if(false == blockers.isEmpty())
        {
            // Keep order/refund/shift history — do not cascade-delete financial records
            throw new UserException(
                    "Cannot delete userId=" + id + " (" + user.getEmail()
                            + ") because other records still reference this user. "
                            + "Remove or reassign those records first.",
                    blockers
            );
        }

        userRepository.delete(user);
    }

    @Override
    @Transactional
    public void deleteAllUsers() throws UserException
    {
        // Playground cleanup: remove dependent rows so FK constraints do not block user wipe
        refundRepository.deleteAll();
        shiftReportRepository.deleteAll();
        orderRepository.deleteAll();

        for(BranchEntity branch : branchRepository.findAll())
        {
            if(branch.getUserEntity() != null)
            {
                branch.setUserEntity(null);
                branchRepository.save(branch);
            }
        }

        for(StoreEntity store : storeRepository.findAll())
        {
            if(store.getStoreAdmin() != null)
            {
                store.setStoreAdmin(null);
                storeRepository.save(store);
            }
        }

        for(UserEntity user : userRepository.findAll())
        {
            if(user.getStoreEntity() != null)
            {
                user.setStoreEntity(null);
                userRepository.save(user);
            }
        }

        userRepository.deleteAll();
    }

    @Override
    public UserDto getAdminUser() throws UserException
    {
        return UserMapper.toDto(userRepository.findByRole(UserRole.ADMIN)
                .orElseThrow(() -> UserException.withDetail(
                        "Admin user not found",
                        "role",
                        UserRole.ADMIN
                )));
    }
}
