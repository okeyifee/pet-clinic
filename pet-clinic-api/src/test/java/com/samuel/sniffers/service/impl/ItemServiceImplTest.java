package com.samuel.sniffers.service.impl;

import com.samuel.sniffers.api.exception.InvalidRequestException;
import com.samuel.sniffers.api.exception.ResourceNotFoundException;
import com.samuel.sniffers.api.factory.EntityFactory;
import com.samuel.sniffers.api.logging.Logger;
import com.samuel.sniffers.dto.BatchItemUpdateDTO;
import com.samuel.sniffers.dto.ItemDTO;
import com.samuel.sniffers.dto.UpdateItemDTO;
import com.samuel.sniffers.dto.response.ItemBatchUpdateResponseDTO;
import com.samuel.sniffers.dto.response.ItemResponseDTO;
import com.samuel.sniffers.entity.Customer;
import com.samuel.sniffers.entity.Item;
import com.samuel.sniffers.entity.ShoppingBasket;
import com.samuel.sniffers.repository.ItemRepository;
import com.samuel.sniffers.security.SecurityService;
import com.samuel.sniffers.service.CustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    private static final String CUSTOMER_ID = "customer-1";
    private static final String BASKET_ID = "basket-1";
    private static final String ITEM_ID = "item-1";
    private static final String TOKEN = "user-token";
    private static final String ITEM_NOT_FOUND = "Item not found or access denied";

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private CustomerService customerService;

    @Mock
    private SecurityService securityService;

    @Mock
    private EntityFactory entityFactory;

    @Mock
    private Logger logger;

    private ItemServiceImpl itemService;

    @BeforeEach
    void setUp() {
        itemService = new ItemServiceImpl(itemRepository, customerService, securityService, entityFactory);
    }

    @Test
    @DisplayName("createItem - Should create item successfully")
    void createItemSuccess() {
        // Arrange
        String customerId = CUSTOMER_ID;
        String basketId = BASKET_ID;

        ItemDTO dto = new ItemDTO();
        dto.setDescription("Test Item");
        dto.setAmount(2);

        Customer customer = new Customer();
        customer.setId(customerId);

        ShoppingBasket basket = new ShoppingBasket();
        basket.setId(basketId);
        customer.setBaskets(Set.of(basket));

        Item item = new Item();
        item.setId(ITEM_ID);
        item.setDescription("Test Item");
        item.setAmount(2);

        ItemResponseDTO expectedResponse = new ItemResponseDTO();
        expectedResponse.setId(ITEM_ID);
        expectedResponse.setDescription("Test Item");
        expectedResponse.setAmount(2);

        when(customerService.getCustomer(customerId)).thenReturn(customer);
        when(entityFactory.convertToEntity(dto, Item.class)).thenReturn(item);
        when(itemRepository.save(item)).thenReturn(item);
        when(entityFactory.convertToDTO(item, ItemResponseDTO.class)).thenReturn(expectedResponse);

        // Act
        ItemResponseDTO result = itemService.createItem(customerId, basketId, dto);

        // Assert
        assertThat(result).isEqualTo(expectedResponse);
        assertThat(item.getBasket()).isEqualTo(basket);
        verify(itemRepository).save(item);
    }

    @Test
    @DisplayName("createItem - Should throw exception when basket not found")
    void createItemBasketNotFound() {
        // Arrange
        String customerId = CUSTOMER_ID;
        String basketId = "non-existent-basket";

        ItemDTO dto = new ItemDTO();

        Customer customer = new Customer();
        customer.setId(customerId);
        customer.setBaskets(Collections.emptySet());

        when(customerService.getCustomer(customerId)).thenReturn(customer);

        // Act & Assert
        assertThatThrownBy(() -> itemService.createItem(customerId, basketId, dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Basket not found");

        verify(itemRepository, never()).save(any(Item.class));
    }

    @Test
    @DisplayName("getItem - Should return item when found")
    void getItemFound() {
        // Arrange
        String customerId = CUSTOMER_ID;
        String basketId = BASKET_ID;
        String itemId = ITEM_ID;

        Item item = new Item();
        item.setId(itemId);
        item.setDescription("Test Item");

        ItemResponseDTO expectedResponse = new ItemResponseDTO();
        expectedResponse.setId(itemId);
        expectedResponse.setDescription("Test Item");

        when(customerService.customerExist(customerId)).thenReturn(true);
        when(securityService.getCurrentCustomerToken()).thenReturn(TOKEN);
        when(securityService.isAdmin(TOKEN)).thenReturn(false);
        when(itemRepository.findByIdWithAccess(itemId, basketId, customerId, TOKEN, false))
                .thenReturn(Optional.of(item));
        when(entityFactory.convertToDTO(item, ItemResponseDTO.class)).thenReturn(expectedResponse);

        // Act
        ItemResponseDTO result = itemService.getItem(customerId, basketId, itemId);

        // Assert
        assertThat(result).isEqualTo(expectedResponse);
    }

    @Test
    @DisplayName("getItem - Should throw exception when customer not found")
    void getItemCustomerNotFound() {
        // Arrange
        String customerId = "non-existent-customer";

        when(customerService.customerExist(customerId)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> itemService.getItem(customerId, BASKET_ID, ITEM_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Customer not found");

        verify(itemRepository, never()).findByIdWithAccess(anyString(), anyString(), anyString(), anyString(), anyBoolean());
    }

    @Test
    @DisplayName("getItem - Should throw exception when item not found")
    void getItemNotFound() {
        // Arrange
        String customerId = CUSTOMER_ID;
        String basketId = BASKET_ID;
        String itemId = "non-existent-item";

        when(customerService.customerExist(customerId)).thenReturn(true);
        when(securityService.getCurrentCustomerToken()).thenReturn(TOKEN);
        when(securityService.isAdmin(TOKEN)).thenReturn(false);
        when(itemRepository.findByIdWithAccess(itemId, basketId, customerId, TOKEN, false))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> itemService.getItem(customerId, basketId, itemId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(ITEM_NOT_FOUND);
    }

    @Test
    @DisplayName("getAllItems - Should return all items for a basket")
    void getAllItemsSuccess() {
        // Arrange
        String customerId = CUSTOMER_ID;
        String basketId = BASKET_ID;

        List<Item> items = Arrays.asList(
                createItem(ITEM_ID, "Item 1", 1),
                createItem("item-2", "Item 2", 2)
        );

        List<ItemResponseDTO> expectedResponses = Arrays.asList(
                createItemResponseDTO(ITEM_ID, "Item 1", 1),
                createItemResponseDTO("item-2", "Item 2", 2)
        );

        when(customerService.customerExist(customerId)).thenReturn(true);
        when(securityService.getCurrentCustomerToken()).thenReturn(TOKEN);
        when(securityService.isAdmin(TOKEN)).thenReturn(false);
        when(itemRepository.findByCustomerWithAccess(basketId, customerId, TOKEN, false))
                .thenReturn(items);

        when(entityFactory.convertToDTO(items.get(0), ItemResponseDTO.class)).thenReturn(expectedResponses.get(0));
        when(entityFactory.convertToDTO(items.get(1), ItemResponseDTO.class)).thenReturn(expectedResponses.get(1));

        // Act
        List<ItemResponseDTO> results = itemService.getAllItems(customerId, basketId);

        // Assert
        assertThat(results).hasSize(2);
        assertThat(results.get(0).getId()).isEqualTo(ITEM_ID);
        assertThat(results.get(1).getId()).isEqualTo("item-2");
    }

    @Test
    @DisplayName("getAllItems - Should throw exception when customer not found")
    void getAllItemsCustomerNotFound() {
        // Arrange
        String customerId = "non-existent-customer";

        when(customerService.customerExist(customerId)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> itemService.getAllItems(customerId, BASKET_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Customer not found");

        verify(itemRepository, never()).findByCustomerWithAccess(anyString(), anyString(), anyString(), anyBoolean());
    }

    @Test
    @DisplayName("updateItem - Should update item successfully with DTO")
    void updateItemWithDTOSuccess() {
        // Arrange
        String customerId = CUSTOMER_ID;
        String basketId = BASKET_ID;
        String itemId = ITEM_ID;

        ItemDTO dto = new ItemDTO();
        dto.setDescription("Updated Item");
        dto.setAmount(3);

        Item item = createItem(itemId, "Original Item", 1);

        ItemResponseDTO expectedResponse = createItemResponseDTO(itemId, "Updated Item", 3);

        when(customerService.customerExist(customerId)).thenReturn(true);
        when(securityService.getCurrentCustomerToken()).thenReturn(TOKEN);
        when(securityService.isAdmin(TOKEN)).thenReturn(false);
        when(itemRepository.findByIdWithAccess(itemId, basketId, customerId, TOKEN, false))
                .thenReturn(Optional.of(item));
        when(itemRepository.save(item)).thenReturn(item);
        when(entityFactory.convertToDTO(item, ItemResponseDTO.class)).thenReturn(expectedResponse);

        // Act
        ItemResponseDTO result = itemService.updateItem(customerId, basketId, itemId, dto);

        // Assert
        assertThat(result).isEqualTo(expectedResponse);
        assertThat(item.getDescription()).isEqualTo("Updated Item");
        assertThat(item.getAmount()).isEqualTo(3);
    }

    @Test
    @DisplayName("updateItem - Should update item successfully with UpdateItemDTO")
    void updateItemWithUpdateDTOSuccess() {
        // Arrange
        String customerId = CUSTOMER_ID;
        String basketId = BASKET_ID;
        String itemId = ITEM_ID;

        UpdateItemDTO dto = new UpdateItemDTO();
        dto.setDescription("Patched Item");

        Item item = createItem(itemId, "Original Item", 1);
        Item patchedItem = createItem(itemId, "Patched Item", 1);

        ItemResponseDTO expectedResponse = createItemResponseDTO(itemId, "Patched Item", 1);

        when(customerService.customerExist(customerId)).thenReturn(true);
        when(securityService.getCurrentCustomerToken()).thenReturn(TOKEN);
        when(securityService.isAdmin(TOKEN)).thenReturn(false);
        when(itemRepository.findByIdWithAccess(itemId, basketId, customerId, TOKEN, false))
                .thenReturn(Optional.of(item));
        when(entityFactory.patchEntity(dto, item)).thenReturn(patchedItem);
        when(itemRepository.save(patchedItem)).thenReturn(patchedItem);
        when(entityFactory.convertToDTO(patchedItem, ItemResponseDTO.class)).thenReturn(expectedResponse);

        // Act
        ItemResponseDTO result = itemService.updateItem(customerId, basketId, itemId, dto);

        // Assert
        assertThat(result).isEqualTo(expectedResponse);
    }

    @Test
    @DisplayName("updateItem - Should throw exception when UpdateItemDTO is empty")
    void updateItemWithEmptyUpdateDTO() {

        UpdateItemDTO dto = new UpdateItemDTO(); // Both description and amount are null

        // Act & Assert
        assertThatThrownBy(() -> itemService.updateItem(CUSTOMER_ID, BASKET_ID, ITEM_ID, dto))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("You must provide either 'description' or 'amount' in the PATCH request. Both fields cannot be empty.");
    }

    @Test
    @DisplayName("batchUpdateItems - Should update multiple items")
    void batchUpdateItemsSuccess() {
        // Arrange
        String customerId = CUSTOMER_ID;
        String basketId = BASKET_ID;

        BatchItemUpdateDTO.ItemPatchDTO patch1 = new BatchItemUpdateDTO.ItemPatchDTO();
        patch1.setItemId(ITEM_ID);
        patch1.setDescription("Updated Item 1");

        BatchItemUpdateDTO.ItemPatchDTO patch2 = new BatchItemUpdateDTO.ItemPatchDTO();
        patch2.setItemId("item-2");
        patch2.setAmount(3);

        BatchItemUpdateDTO dto = new BatchItemUpdateDTO();
        dto.setUpdates(Arrays.asList(patch1, patch2));

        Item item1 = createItem(ITEM_ID, "Original Item 1", 1);
        Item item2 = createItem("item-2", "Item 2", 2);

        Item updatedItem1 = createItem(ITEM_ID, "Updated Item 1", 1);
        Item updatedItem2 = createItem("item-2", "Item 2", 3);

        List<ItemResponseDTO> expectedResponseDTOs = Arrays.asList(
                createItemResponseDTO(ITEM_ID, "Updated Item 1", 1),
                createItemResponseDTO("item-2", "Item 2", 3)
        );

        when(customerService.customerExist(customerId)).thenReturn(true);
        when(securityService.getCurrentCustomerToken()).thenReturn(TOKEN);
        when(securityService.isAdmin(TOKEN)).thenReturn(false);
        when(itemRepository.findByCustomerIdAndBasketIds(
                eq(customerId), eq(basketId), eq(Arrays.asList(ITEM_ID, "item-2")), anyString(), anyBoolean()))
                .thenReturn(Arrays.asList(item1, item2));

        when(entityFactory.patchEntity(patch1, item1)).thenReturn(updatedItem1);
        when(entityFactory.patchEntity(patch2, item2)).thenReturn(updatedItem2);
        when(itemRepository.saveAll(anyList())).thenReturn(Arrays.asList(updatedItem1, updatedItem2));
        when(entityFactory.convertToEntityList(anyList(), eq(ItemResponseDTO.class))).thenReturn(expectedResponseDTOs);

        // Act
        ItemBatchUpdateResponseDTO result = itemService.batchUpdateItems(customerId, basketId, dto);

        // Assert
        assertThat(result.getSuccessfulUpdatesCount()).isEqualTo(2);
        assertThat(result.getFailedUpdatesCount()).isNull();
        assertThat(result.getSuccessfulUpdates()).hasSize(2);
        assertThat(result.getFailedUpdates()).isEmpty();
    }

    @Test
    @DisplayName("batchUpdateItems - Should handle failures in batch update")
    void batchUpdateItemsWithFailures() {
        // Arrange
        String customerId = CUSTOMER_ID;
        String basketId = BASKET_ID;

        BatchItemUpdateDTO.ItemPatchDTO patch1 = new BatchItemUpdateDTO.ItemPatchDTO();
        patch1.setItemId(ITEM_ID);
        patch1.setDescription("Updated Item 1");

        BatchItemUpdateDTO.ItemPatchDTO patch2 = new BatchItemUpdateDTO.ItemPatchDTO();
        patch2.setItemId("non-existent");
        patch2.setAmount(3);

        BatchItemUpdateDTO dto = new BatchItemUpdateDTO();
        dto.setUpdates(Arrays.asList(patch1, patch2));

        Item item1 = createItem(ITEM_ID, "Original Item 1", 1);
        Item updatedItem1 = createItem(ITEM_ID, "Updated Item 1", 1);

        List<ItemResponseDTO> expectedResponseDTOs = Collections.singletonList(
                createItemResponseDTO(ITEM_ID, "Updated Item 1", 1)
        );

        when(customerService.customerExist(customerId)).thenReturn(true);
        when(securityService.getCurrentCustomerToken()).thenReturn(TOKEN);
        when(securityService.isAdmin(TOKEN)).thenReturn(false);
        when(itemRepository.findByCustomerIdAndBasketIds(
                eq(customerId), eq(basketId), eq(Arrays.asList(ITEM_ID, "non-existent")), anyString(), anyBoolean()))
                .thenReturn(Collections.singletonList(item1));

        when(entityFactory.patchEntity(patch1, item1)).thenReturn(updatedItem1);
        when(itemRepository.saveAll(anyList())).thenReturn(Collections.singletonList(updatedItem1));
        when(entityFactory.convertToEntityList(anyList(), eq(ItemResponseDTO.class))).thenReturn(expectedResponseDTOs);

        // Act
        ItemBatchUpdateResponseDTO result = itemService.batchUpdateItems(customerId, basketId, dto);

        // Assert
        assertThat(result.getSuccessfulUpdatesCount()).isEqualTo(1);
        assertThat(result.getFailedUpdatesCount()).isEqualTo(1);
        assertThat(result.getSuccessfulUpdates()).hasSize(1);
        assertThat(result.getFailedUpdates()).hasSize(1);
        assertThat(result.getFailedUpdates().get(0).getId()).isEqualTo("non-existent");
        assertThat(result.getFailedUpdates().get(0).getError()).contains(ITEM_NOT_FOUND);
    }

    @Test
    @DisplayName("deleteItem - Should delete item when found")
    void deleteItemSuccess() {
        // Arrange
        String customerId = CUSTOMER_ID;
        String basketId = BASKET_ID;
        String itemId = ITEM_ID;

        Item item = createItem(itemId, "Item to delete", 1);

        when(customerService.customerExist(customerId)).thenReturn(true);
        when(securityService.getCurrentCustomerToken()).thenReturn(TOKEN);
        when(securityService.isAdmin(TOKEN)).thenReturn(false);
        when(itemRepository.findByIdWithAccess(itemId, basketId, customerId, TOKEN, false))
                .thenReturn(Optional.of(item));

        // Act
        itemService.deleteItem(customerId, basketId, itemId);

        // Assert
        verify(itemRepository).delete(item);
    }

    @Test
    @DisplayName("deleteItem - Should throw exception when customer not found")
    void deleteItemCustomerNotFound() {
        // Arrange
        String customerId = "non-existent-customer";

        when(customerService.customerExist(customerId)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> itemService.deleteItem(customerId, BASKET_ID, ITEM_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Customer not found");

        verify(itemRepository, never()).delete(any(Item.class));
    }

    @Test
    @DisplayName("deleteItem - Should throw exception when item not found")
    void deleteItemNotFound() {
        // Arrange
        String customerId = CUSTOMER_ID;
        String basketId = BASKET_ID;
        String itemId = "non-existent-item";

        when(customerService.customerExist(customerId)).thenReturn(true);
        when(securityService.getCurrentCustomerToken()).thenReturn(TOKEN);
        when(securityService.isAdmin(TOKEN)).thenReturn(false);
        when(itemRepository.findByIdWithAccess(itemId, basketId, customerId, TOKEN, false))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> itemService.deleteItem(customerId, basketId, itemId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(ITEM_NOT_FOUND);

        verify(itemRepository, never()).delete(any(Item.class));
    }

    // Helper methods
    private Item createItem(String id, String description, int amount) {
        Item item = new Item();
        item.setId(id);
        item.setDescription(description);
        item.setAmount(amount);
        return item;
    }

    private ItemResponseDTO createItemResponseDTO(String id, String description, int amount) {
        ItemResponseDTO dto = new ItemResponseDTO();
        dto.setId(id);
        dto.setDescription(description);
        dto.setAmount(amount);
        return dto;
    }
}