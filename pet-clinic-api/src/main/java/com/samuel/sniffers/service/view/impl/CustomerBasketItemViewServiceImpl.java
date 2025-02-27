package com.samuel.sniffers.service.view.impl;

import com.samuel.sniffers.api.factory.EntityFactory;
import com.samuel.sniffers.api.response.PagedResponse;
import com.samuel.sniffers.dto.response.view.CustomerBasketItemViewDTO;
import com.samuel.sniffers.entity.view.CustomerBasketItemView;
import com.samuel.sniffers.repository.view.CustomerBasketItemViewRepository;
import com.samuel.sniffers.security.SecurityService;
import com.samuel.sniffers.service.impl.AbstractPaginationService;
import com.samuel.sniffers.service.view.CustomerBasketItemViewService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<CustomerBasketItemViewDTO> getPaginatedView(
            int page, int size, String sortBy, String direction, String baseUrl) {

        // Create page request
        PageRequest pageRequest = PageRequest.of(
                Math.max(0, page - 1), // Spring Data's pagination follows a zero-based indexing approach
                size,
                getSanitizedSortDirection(direction),
                getSanitizedViewSortBy(sortBy)
        );

        String token = securityService.getCurrentCustomerToken();
        boolean isAdmin = securityService.isAdmin(token);

        Page<CustomerBasketItemView> viewsPage = repository.findAllWithAccess(token, isAdmin, pageRequest);

        // Map entities to DTOs
        List<CustomerBasketItemViewDTO> viewsDTOs = viewsPage.getContent().stream()
                .map(view -> entityFactory.convertToDTO(view, CustomerBasketItemViewDTO.class))
                .toList();

        // Create paged response
        PagedResponse<CustomerBasketItemViewDTO> response = new PagedResponse<>(
                viewsDTOs,
                viewsPage.getNumber() + 1,
                viewsPage.getSize(),
                viewsPage.getTotalElements(),
                viewsPage.getTotalPages(),
                viewsPage.isLast()
        );

        // Add pagination links
        response.setLinks(buildPaginationLinks(baseUrl, size, sortBy, direction, page, viewsPage));
        return response;
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