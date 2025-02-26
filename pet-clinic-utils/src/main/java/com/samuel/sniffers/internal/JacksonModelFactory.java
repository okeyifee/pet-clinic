package com.samuel.sniffers.internal;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.samuel.sniffers.api.exception.EntityMappingException;
import com.samuel.sniffers.api.factory.LoggerFactory;
import com.samuel.sniffers.api.factory.EntityFactory;
import com.samuel.sniffers.api.logging.Logger;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class JacksonModelFactory implements EntityFactory {
    private final ObjectMapper objectMapper;
    private final Logger logger;


    public JacksonModelFactory() {
        this.objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); // Register JavaTimeModule for Java 8 date/time types
        this.objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        this.logger = LoggerFactory.getLogger(this.getClass());
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
            throw new EntityMappingException("Failed to convert entity to POJO", e);
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
            throw new EntityMappingException("Failed to convert POJO to entity", e);
        }
    }

    public <D, P> D patchEntity(P pojo, D entity) {
        try {
            if (pojo == null || entity == null) {
                return null;
            }
            // Merge only non-null fields into existing model
            objectMapper.updateValue(entity, objectMapper.convertValue(pojo, Map.class));
            return entity;
        } catch (Exception e) {
            final String entityName = entity.getClass().getName();
            logger.error("Error patching {} entity: {}", entityName, e.getMessage(), e);
            throw new EntityMappingException(String.format("Failed to patch %s entity", entityName), e);
        }
    }

    @Override
    public <D, P> List<P> convertToDTOList(List<D> entities, Class<P> pojoClass) {

        if (entities == null) {
            return Collections.emptyList();
        }

        return entities.stream()
                .map(entity -> convertToDTO(entity, pojoClass))
                .toList();
    }

    @Override
    public <D, P> List<D> convertToEntityList(List<P> pojos, Class<D> entityClass) {

        if (pojos == null) {
            return Collections.emptyList();
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
            logger.error("Failed to convert JSON string to List.", e);
            throw new EntityMappingException("Failed to convert JSON string to List.", e);
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
            logger.error("Failed to convert JSON string to: {}", targetType.getSimpleName(), e);
            throw new EntityMappingException("Failed to convert JSON string to " + targetType.getSimpleName(), e);
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
            throw new EntityMappingException("Failed to convert object to JSON string", e);
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
            logger.error("Failed to convert entity list to JSON string: {}", e.getMessage(), e);
            throw new EntityMappingException("Failed to convert entity list to JSON string", e);
        }
    }

    @Override
    public ObjectMapper getObjectMapperForStreaming() {

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonGenerator.Feature.FLUSH_PASSED_TO_STREAM, false);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addDeserializer(LocalDateTime.class,
                new LocalDateTimeDeserializer(
                        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
                )
        );

        mapper.registerModule(javaTimeModule);
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

        return mapper;
    }
}
