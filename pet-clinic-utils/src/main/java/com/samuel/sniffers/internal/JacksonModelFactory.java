package com.samuel.sniffers.internal;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.samuel.sniffers.api.exception.ConversionException;
import com.samuel.sniffers.api.factory.LoggerFactory;
import com.samuel.sniffers.api.factory.ModelFactory;
import com.samuel.sniffers.api.logging.Logger;
import java.util.Collections;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class JacksonModelFactory implements ModelFactory {
    private final ObjectMapper objectMapper;
    private final Logger logger = LoggerFactory.getLogger(JacksonModelFactory.class);


    public JacksonModelFactory() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    @Override
    public <D, P> P convertToDTO(D entity, Class<P> pojoClass) {
        try {
            if (entity == null) {
                return null;
            }
            return objectMapper.convertValue(entity, pojoClass);
        } catch (Exception e) {
            logger.error("Error converting entity to POJO: {}", e.getMessage(), e);
            throw new ConversionException("Failed to convert entity to POJO", e);
        }
    }

    @Override
    public <D, P> D convertToEntity(P pojo, Class<D> entityClass) {
        try {
            if (pojo == null) {
                return null;
            }
            return objectMapper.convertValue(pojo, entityClass);
        } catch (Exception e) {
            logger.error("Error converting POJO to entity: {}", e.getMessage(), e);
            throw new ConversionException("Failed to convert POJO to entity", e);
        }
    }

    @Override
    public <D, P> List<P> convertToDTOList(List<D> entities, Class<P> pojoClass) {
        if (entities == null) {
            return null;
        }
        return entities.stream()
                .map(entity -> convertToDTO(entity, pojoClass))
                .toList();
    }

    @Override
    public <D, P> List<D> convertToEntityList(List<P> pojos, Class<D> entityClass) {
        if (pojos == null) {
            return null;
        }
        return pojos.stream()
                .map(pojo -> convertToEntity(pojo, entityClass))
                .toList();
    }

    @Override
    public <T> List<T> convertFromStringToList(String json, Class<T> targetClass) {
        try {
            if (json == null || json.isBlank()) {
                return Collections.emptyList();
            }
            return objectMapper.readValue(json, objectMapper.getTypeFactory().constructCollectionType(List.class, targetClass));
        } catch (Exception e) {
            throw new ConversionException("Failed to convert JSON string to List", e);
        }
    }

    @Override
    public <T> T convertFromString(String json, Class<T> targetType) {
        try {
            if (json == null || json.isBlank()) {
                return null;
            }
            return objectMapper.readValue(json, targetType);
        } catch (Exception e) {
            throw new ConversionException("Failed to convert JSON string to " + targetType.getSimpleName(), e);
        }
    }

    @Override
    public <T> String convertToString(T object) {
        try {
            if (object == null) {
                return null;
            }
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            logger.error("Error converting object to JSON string: {}", e.getMessage(), e);
            throw new ConversionException("Failed to convert object to JSON string", e);
        }
    }

    @Override
    public <T> String convertEntityListToString(List<T> entityList) {
        try {
            if (entityList == null) {
                return null;
            }
            return objectMapper.writeValueAsString(entityList);
        } catch (Exception e) {
            throw new ConversionException("Failed to convert entity list to JSON string", e);
        }
    }
}
