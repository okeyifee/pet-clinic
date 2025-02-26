package com.samuel.sniffers.api.factory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.samuel.sniffers.api.exception.EntityMappingException;

import java.util.List;

/**
 * This interface provides utility methods for entity mapping using Jackson's ObjectMapper.
 * Implementations of this interface will handle data transformations and serialization.
 */
public interface EntityFactory {

    /**
     * Converts an entity to a Data Transfer Object (DTO).
     *
     * @param <D> The type of the entity.
     * @param <P> The type of the DTO.
     * @param entity The entity to be converted.
     * @param pojoClass The target DTO class.
     * @return The converted DTO.
     * @throws EntityMappingException if mapping fails.
     */
    <D, P> P convertToDTO(D entity, Class<P> pojoClass);

    /**
     * Converts a Data Transfer Object (DTO) to an entity.
     *
     * @param <D> The type of the entity.
     * @param <P> The type of the DTO.
     * @param pojo The DTO to be converted.
     * @param entityClass The target entity class.
     * @return The converted entity.
     * @throws EntityMappingException if mapping fails.
     */
    <D, P> D convertToEntity(P pojo, Class<D> entityClass);

    /**
     * Updates entity with non-null fields from pojo object.
     *
     * @param <D> The type of the entity.
     * @param <P> The type of the DTO.
     * @param pojo The DTO with fields for updating entity.
     * @param entity The entity class to be updated.
     * @return The updated entity.
     * @throws EntityMappingException if mapping fails.
     */
    public <D, P> D patchEntity(P pojo, D entity);

    /**
     * Converts a list of entities to a list of Data Transfer Objects (DTOs).
     *
     * @param <D> The type of the entity.
     * @param <P> The type of the DTO.
     * @param entities The list of entities to be converted.
     * @param pojoClass The target DTO class.
     * @return The list of converted DTOs.
     * @throws EntityMappingException if any mapping in the list fails.
     */
    <D, P> List<P> convertToDTOList(List<D> entities, Class<P> pojoClass);

    /**
     * Converts a list of Data Transfer Objects (DTOs) to a list of entities.
     *
     * @param <D> The type of the entity.
     * @param <P> The type of the DTO.
     * @param pojos The list of DTOs to be converted.
     * @param entityClass The target entity class.
     * @return The list of converted entities.
     * @throws EntityMappingException if any mapping in the list fails.
     */
    <D, P> List<D> convertToEntityList(List<P> pojos, Class<D> entityClass);

    /**
     * Converts a JSON string to an object of the specified target type.
     *
     * @param <T> The type of the object to be converted.
     * @param json The JSON string to be converted.
     * @param targetType The class type of the object.
     * @return The converted object.
     * @throws EntityMappingException if mapping fails.
     */
    <T> T convertFromString(String json, Class<T> targetType);

    /**
     * Converts a JSON string to a list of objects of the specified target type.
     *
     * @param <T> The type of the objects in the list.
     * @param json The JSON string to be converted.
     * @param targetClass The class type of the objects in the list.
     * @return The list of converted objects.
     * @throws EntityMappingException if mapping fails.
     */
    <T> List<T> convertFromStringToList(String json, Class<T> targetClass);

    /**
     * Converts an object to its JSON string representation.
     *
     * @param <T> The type of the object.
     * @param object The object to be converted.
     * @return The JSON string representation of the object.
     * @throws EntityMappingException if mapping fails.
     */
    <T> String convertToString(T object);

    /**
     * Converts a list of entities to their JSON string representation.
     *
     * @param <T> The type of the entities.
     * @param entityList The list of entities to be converted.
     * @return The JSON string representation of the list of entities.
     * @throws EntityMappingException if mapping fails.
     */
    <T> String convertEntityListToString(List<T> entityList);

    /**
     * Return an Object mapper configured for writing streams.
     */
    ObjectMapper getObjectMapperForStreaming();
}
