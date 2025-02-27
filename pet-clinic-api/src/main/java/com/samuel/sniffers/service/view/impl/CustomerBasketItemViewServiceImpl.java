package com.samuel.sniffers.service.view.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.samuel.sniffers.api.factory.EntityFactory;
import com.samuel.sniffers.dto.response.view.CustomerBasketItemViewDTO;
import com.samuel.sniffers.entity.view.CustomerBasketItemView;
import com.samuel.sniffers.repository.view.CustomerBasketItemViewRepository;
import com.samuel.sniffers.security.SecurityService;
import com.samuel.sniffers.service.impl.AbstractPaginationService;
import com.samuel.sniffers.service.view.CustomerBasketItemViewService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomerBasketItemViewServiceImpl extends AbstractPaginationService implements CustomerBasketItemViewService {

    private final CustomerBasketItemViewRepository repository;
    private final SecurityService securityService;
    private final EntityFactory entityFactory;

    public CustomerBasketItemViewServiceImpl(CustomerBasketItemViewRepository repository, SecurityService securityService, EntityFactory entityFactory) {
        this.repository = repository;
        this.securityService = securityService;
        this.entityFactory = entityFactory;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerBasketItemViewDTO> findAllWithAccess() {
        String token = securityService.getCurrentCustomerToken();
        boolean isAdmin = securityService.isAdmin(token);
        List<CustomerBasketItemView> views = repository.findAllWithAccess(token, isAdmin);
        return views.stream()
                .map(i -> entityFactory.convertToDTO(i, CustomerBasketItemViewDTO.class))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerBasketItemViewDTO> findByCustomerNameWithAccess(String customerName) {
        String token = securityService.getCurrentCustomerToken();
        boolean isAdmin = securityService.isAdmin(token);
        List<CustomerBasketItemView> views = repository.findByCustomerNameWithAccess(customerName, token, isAdmin);
        return views.stream()
                .map(i -> entityFactory.convertToDTO(i, CustomerBasketItemViewDTO.class))
                .collect(Collectors.toList());
    }
}