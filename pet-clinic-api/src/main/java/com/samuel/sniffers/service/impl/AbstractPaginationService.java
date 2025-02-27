package com.samuel.sniffers.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractPaginationService {

    private static final String SORT_BY_NAME = "name";
    private static final String SORT_BY_ID = "id";
    private static final String SORT_BY_TIMEZONE = "timezone";
    private static final String SORT_BY_CREATED = "created";
    private static final String SORT_BY_STATUS = "status";
    private static final String SORT_BY_STATUS_DATE = "status_date";
    private static final String SORT_BY_CUSTOMER = "customer";
    private static final String SORT_BY_DESCRIPTION = "description";
    private static final String SORT_BY_AMOUNT = "amount";

    protected String buildPageUrl(String baseUrl, int page, int size, String sortBy, String direction) {
        return String.format("%s?page=%d&size=%d&sortBy=%s&direction=%s",
                baseUrl, page, size, sortBy, direction);
    }

    protected String getSanitizedCustomerSortBy(String sortBy) {
        return switch (sortBy.toLowerCase()) {
            case SORT_BY_ID -> SORT_BY_ID;
            case SORT_BY_NAME -> SORT_BY_NAME;
            case SORT_BY_TIMEZONE -> SORT_BY_TIMEZONE;
            case SORT_BY_CREATED -> SORT_BY_CREATED;
            default -> SORT_BY_ID; // default sorted by id
        };
    }

    protected String getSanitizedBasketSortBy(String sortBy) {
        return switch (sortBy.toLowerCase()) {
            case SORT_BY_ID -> SORT_BY_ID;
            case SORT_BY_STATUS -> SORT_BY_STATUS;
            case SORT_BY_STATUS_DATE -> SORT_BY_STATUS_DATE;
            case SORT_BY_CUSTOMER -> SORT_BY_CUSTOMER;
            case SORT_BY_CREATED -> SORT_BY_CREATED;
            default -> SORT_BY_ID; // default sorted by id
        };
    }

    protected String getSanitizedItemSortBy(String sortBy) {
        return switch (sortBy.toLowerCase()) {
            case SORT_BY_ID -> SORT_BY_ID;
            case SORT_BY_DESCRIPTION -> SORT_BY_DESCRIPTION;
            case SORT_BY_AMOUNT -> SORT_BY_AMOUNT;
            default -> SORT_BY_ID; // default sorted by id
        };
    }

    protected String getSanitizedViewSortBy(String sortBy) {
        return switch (sortBy.toLowerCase()) {
            case SORT_BY_ID -> SORT_BY_ID;
            case SORT_BY_NAME -> SORT_BY_NAME;
            default -> SORT_BY_ID; // default sorted by id
        };
    }

    protected Sort.Direction getSanitizedSortDirection(String direction) {

        String sortDirection;
        if (direction.equalsIgnoreCase("asc")) {
            sortDirection = "ASC";
        } else {
            sortDirection = "DESC";
        }

        return Sort.Direction.fromString(sortDirection);
    }

    protected Map<String, String> buildPaginationLinks (
            String baseUrl, int size, String sortBy, String direction, int page, Page<?> entityPage){

        Map<String, String> links = new HashMap<>();

        // First page
        links.put("first", buildPageUrl(baseUrl, 1, size, sortBy, direction));

        // Previous page
        if (page > 1) {
            links.put("prev", buildPageUrl(baseUrl, page - 1, size, sortBy, direction));
        }

        // Next page
        if (!entityPage.isLast()) {
            links.put("next", buildPageUrl(baseUrl, page + 1, size, sortBy, direction));
        }

        // Last page
        int lastPage = entityPage.getTotalPages();
        if (lastPage > 0) {
            links.put("last", buildPageUrl(baseUrl, lastPage, size, sortBy, direction));
        }

        return links;
    }

}
