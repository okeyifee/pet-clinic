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
import com.samuel.sniffers.repository.CustomerRepository;
import com.samuel.sniffers.repository.ShoppingBasketRepository;
import com.samuel.sniffers.security.SecurityService;
import com.samuel.sniffers.service.ShoppingBasketService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ShoppingBasketServiceImpl implements ShoppingBasketService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final String BASKET_NOT_FOUND = "Basket not found or access denied";

    private final ShoppingBasketRepository basketRepository;
    private final CustomerRepository customerRepository;
    private final SecurityService securityService;
    private final EntityFactory entityFactory;

    private static final Map<BasketStatus, Set<BasketStatus>> ALLOWED_TRANSITIONS = Map.of(
            BasketStatus.NEW, Set.of(BasketStatus.PAID),
            BasketStatus.PAID, Set.of(BasketStatus.PROCESSED),
            BasketStatus.PROCESSED, Set.of(BasketStatus.UNKNOWN),
            BasketStatus.UNKNOWN, Set.of() // No allowed transitions from UNKNOWN
    );

    @Override
    public BasketResponseDTO createBasket(String customerId) {
        Customer customer = getCustomer(customerId);

        ShoppingBasket basket = new ShoppingBasket();
        basket.setCustomer(customer);
        basket.setStatus(BasketStatus.NEW);

        return entityFactory.convertToDTO(basketRepository.save(basket), BasketResponseDTO.class);
    }

    @Override
    public BasketResponseDTO getBasket(String customerId, String basketId) {
        getCustomer(customerId);

        return basketRepository.findByIdWithAccess(
                        basketId,
                        customerId,
                        securityService.getCurrentCustomerToken(),
                        securityService.isAdmin(securityService.getCurrentCustomerToken())
                )
                .map(basket -> entityFactory.convertToDTO(basket, BasketResponseDTO.class))
                .orElseThrow(() -> new ResourceNotFoundException(BASKET_NOT_FOUND));
    }

    @Override
    public List<BasketResponseDTO> getAllBaskets(String customerId) {
        getCustomer(customerId);
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
    public BasketResponseDTO updateBasket(String customerId, String basketId, UpdateBasketDTO dto) {
        getCustomer(customerId);
        ShoppingBasket shoppingBasket = basketRepository.findByIdWithAccess(
                        basketId,
                        customerId,
                        securityService.getCurrentCustomerToken(),
                        securityService.isAdmin(securityService.getCurrentCustomerToken())
                )
                .orElseThrow(() -> new ResourceNotFoundException(BASKET_NOT_FOUND));

        updateBasketStatus(shoppingBasket, dto.getStatus());
        basketRepository.save(shoppingBasket);

        return entityFactory.convertToDTO(shoppingBasket, BasketResponseDTO.class);
    }

    @Override
    public BasketBatchUpdateResponseDTO batchUpdateBasket(String customerId, BatchBasketUpdateDTO dto) {
        getCustomer(customerId);
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
    public void deleteBasket(String customerId, String basketId) {
        getCustomer(customerId);
        ShoppingBasket basket = basketRepository.findByIdWithAccess(
                        basketId,
                        customerId,
                        securityService.getCurrentCustomerToken(),
                        securityService.isAdmin(securityService.getCurrentCustomerToken())
                )
                .orElseThrow(() -> new ResourceNotFoundException(BASKET_NOT_FOUND));

        logger.info("deleting basket {}", basket);
        basketRepository.delete(basket);
        logger.info("deleted basket {}", basket);
    }

    private void updateBasketStatus(ShoppingBasket shoppingBasket, BasketStatus newBasketStatus) {
        final BasketStatus basketStatus = shoppingBasket.getStatus();
        if (!ALLOWED_TRANSITIONS.getOrDefault(basketStatus, Set.of()).contains(newBasketStatus)) {
            logger.error("Basket status transition not allowed. From {} to {}.", basketStatus, newBasketStatus);
            throw new IllegalStateTransitionException(basketStatus.name(), newBasketStatus.name());
        }

        shoppingBasket.setStatus(newBasketStatus);
        shoppingBasket.setStatusDate(LocalDateTime.now());
    }

    private Customer getCustomer(String customerId) {
        return customerRepository.findByIdWithAccess(
                customerId,
                securityService.getCurrentCustomerToken(),
                securityService.isAdmin(securityService.getCurrentCustomerToken())
        ).orElseThrow(
                () -> new ResourceNotFoundException("Customer not found")
        );
    }
}