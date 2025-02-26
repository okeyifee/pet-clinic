package com.samuel.sniffers.service.impl;

import com.samuel.sniffers.api.exception.IllegalStateTransitionException;
import com.samuel.sniffers.api.exception.ResourceNotFoundException;
import com.samuel.sniffers.api.factory.EntityFactory;
import com.samuel.sniffers.api.factory.LoggerFactory;
import com.samuel.sniffers.api.logging.Logger;
import com.samuel.sniffers.dto.BatchBasketUpdateDTO;
import com.samuel.sniffers.dto.UpdateBasketDTO;
import com.samuel.sniffers.dto.response.BasketBatchUpdateResponseDTO;
import com.samuel.sniffers.dto.response.BasketResponseDTO;
import com.samuel.sniffers.dto.response.BatchUpdateFailure;
import com.samuel.sniffers.entity.Customer;
import com.samuel.sniffers.entity.ShoppingBasket;
import com.samuel.sniffers.enums.BasketStatus;
import com.samuel.sniffers.repository.ShoppingBasketRepository;
import com.samuel.sniffers.security.SecurityService;
import com.samuel.sniffers.service.CustomerService;
import com.samuel.sniffers.service.ShoppingBasketService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ShoppingBasketServiceImpl implements ShoppingBasketService {

    private static final String BASKET_NOT_FOUND = "Basket not found or access denied";

    private final Logger logger;
    private final ShoppingBasketRepository basketRepository;
    private final CustomerService customerService;
    private final SecurityService securityService;
    private final EntityFactory entityFactory;

    public ShoppingBasketServiceImpl(ShoppingBasketRepository basketRepository, CustomerService customerService, SecurityService securityService, EntityFactory entityFactory) {
        this.basketRepository = basketRepository;
        this.customerService = customerService;
        this.securityService = securityService;
        this.entityFactory = entityFactory;
        this.logger = LoggerFactory.getLogger(this.getClass());
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public BasketResponseDTO createBasket(String customerId) {
        Customer customer = customerService.getCustomer(customerId);

        ShoppingBasket basket = new ShoppingBasket();
        basket.setCustomer(customer);
        basket.setStatus(BasketStatus.NEW);

        return entityFactory.convertToDTO(basketRepository.save(basket), BasketResponseDTO.class);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public BasketResponseDTO getBasket(String customerId, String basketId) {
        validateCustomerExists(customerId);
        return entityFactory.convertToDTO(getCustomerBasket(customerId, basketId), BasketResponseDTO.class);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public List<BasketResponseDTO> getAllBaskets(String customerId) {
        validateCustomerExists(customerId);
        return basketRepository.findByCustomerWithAccess(
                        customerId,
                        securityService.getCurrentCustomerToken(),
                        securityService.isAdmin(securityService.getCurrentCustomerToken())
                )
                .stream()
                .map(basket -> entityFactory.convertToDTO(basket, BasketResponseDTO.class))
                .toList();
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public BasketResponseDTO updateBasket(String customerId, String basketId, UpdateBasketDTO dto) {
        validateCustomerExists(customerId);

        ShoppingBasket shoppingBasket = getCustomerBasket(customerId, basketId);

        updateBasketStatus(shoppingBasket, dto.getStatus());
        basketRepository.save(shoppingBasket);

        return entityFactory.convertToDTO(shoppingBasket, BasketResponseDTO.class);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public BasketBatchUpdateResponseDTO batchUpdateBasket(String customerId, BatchBasketUpdateDTO dto) {
        validateCustomerExists(customerId);

        final List<String> basketIds = dto.getUpdates().stream()
                .map(BatchBasketUpdateDTO.BasketPatchDTO::getBasketId)
                .toList();

        List<ShoppingBasket> baskets = basketRepository.findByCustomerIdAndBasketIds(
                customerId,
                basketIds,
                securityService.getCurrentCustomerToken(),
                securityService.isAdmin(securityService.getCurrentCustomerToken())
        );

        Map<String, ShoppingBasket> basketMap = baskets.stream()
                .collect(Collectors.toMap(ShoppingBasket::getId, b -> b));

        List<ShoppingBasket> updatedBaskets = new ArrayList<>();
        List<BatchUpdateFailure> failedUpdates = new ArrayList<>();

        for (BatchBasketUpdateDTO.BasketPatchDTO updateRequest : dto.getUpdates()) {
            try {
                ShoppingBasket basket = basketMap.get(updateRequest.getBasketId());

                if (basket == null) {
                    failedUpdates.add(new BatchUpdateFailure(updateRequest.getBasketId(), BASKET_NOT_FOUND));
                    continue;
                }

                updateBasketStatus(basket, updateRequest.getStatus());
                updatedBaskets.add(basket);
            } catch (Exception ex) {
                failedUpdates.add(new BatchUpdateFailure(updateRequest.getBasketId(), "Failed to update basket: " + ex.getMessage()));
            }
        }

        basketRepository.saveAll(updatedBaskets);
        return new BasketBatchUpdateResponseDTO(
                !updatedBaskets.isEmpty() ? updatedBaskets.size() : null,
                !failedUpdates.isEmpty() ? failedUpdates.size() : null,
                entityFactory.convertToEntityList(updatedBaskets, BasketResponseDTO.class),
                failedUpdates
        );
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void deleteBasket(String customerId, String basketId) {
        validateCustomerExists(customerId);

        logger.info("deleting basket with id {}...", basketId);
        basketRepository.delete(getCustomerBasket(customerId, basketId));
        logger.info("deleted basket with id {}.", basketId);
    }

    private void updateBasketStatus(ShoppingBasket shoppingBasket, BasketStatus newBasketStatus) {

        final Map<BasketStatus, Set<BasketStatus>> allowedStatusTransitionMap = Map.of(
                BasketStatus.NEW, Set.of(BasketStatus.PAID),
                BasketStatus.PAID, Set.of(BasketStatus.PROCESSED),
                BasketStatus.PROCESSED, Set.of(BasketStatus.UNKNOWN),
                BasketStatus.UNKNOWN, Set.of() // No allowed transitions from UNKNOWN
        );

        final BasketStatus basketStatus = shoppingBasket.getStatus();
        if (!allowedStatusTransitionMap.getOrDefault(basketStatus, Set.of()).contains(newBasketStatus)) {
            logger.error("Basket status transition not allowed. From {} to {}.", basketStatus, newBasketStatus);
            throw new IllegalStateTransitionException(basketStatus.name(), newBasketStatus.name());
        }

        shoppingBasket.setStatus(newBasketStatus);
        shoppingBasket.setStatusDate(LocalDateTime.now());
    }

    private void validateCustomerExists(String customerId) {
        if (!customerService.customerExist(customerId)) {
            logger.error("Customer with id: {} not found", customerId);
            throw new ResourceNotFoundException("Customer not found");
        }
    }

    private ShoppingBasket getCustomerBasket(String customerId, String basketId) {
        return basketRepository.findByIdWithAccess(
                        basketId,
                        customerId,
                        securityService.getCurrentCustomerToken(),
                        securityService.isAdmin(securityService.getCurrentCustomerToken())
                )
                .orElseThrow(() -> new ResourceNotFoundException(BASKET_NOT_FOUND));
    }
}