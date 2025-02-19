package com.samuel.sniffers.internal;

import org.junit.jupiter.api.BeforeEach;

import com.samuel.sniffers.api.exception.ConversionException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class JacksonModelFactoryTest {

    private JacksonModelFactory modelFactory;

    @BeforeEach
    void setUp() {
        modelFactory = new JacksonModelFactory();
    }

    @ParameterizedTest
    @MethodSource("entityToDtoDataProvider")
    void shouldConvertToDTO(TestEntity entity, String expectedName, Integer expectedValue) {

        TestDTO dto = modelFactory.convertToDTO(entity, TestDTO.class);

        // Assertions
        assertThat(dto).isNotNull();
        assertThat(dto.getName()).isEqualTo(expectedName);
        assertThat(dto.getValue()).isEqualTo(expectedValue);
    }

    @ParameterizedTest
    @MethodSource("dtoToEntityDataProvider")
    void shouldConvertToEntity(TestDTO dto, String expectedName, Integer expectedValue) {

        TestEntity entity = modelFactory.convertToEntity(dto, TestEntity.class);

        // Assertions
        assertThat(entity).isNotNull();
        assertThat(entity.getName()).isEqualTo(expectedName);
        assertThat(entity.getValue()).isEqualTo(expectedValue);
    }

    @ParameterizedTest
    @MethodSource("entityToDtoListDataProvider")
    void shouldConvertToDTOList(List<TestEntity> entities, List<String> expectedNames, List<Integer> expectedValues) {

        List<TestDTO> dtos = modelFactory.convertToDTOList(entities, TestDTO.class);

        // Assertions
        assertThat(dtos).hasSize(entities.size());
        for (int i = 0; i < dtos.size(); i++) {
            assertThat(dtos.get(i).getName()).isEqualTo(expectedNames.get(i));
            assertThat(dtos.get(i).getValue()).isEqualTo(expectedValues.get(i));
        }
    }

    @ParameterizedTest
    @MethodSource("dtoToEntityListDataProvider")
    void shouldConvertToEntityList(List<TestDTO> dtos, List<String> expectedNames, List<Integer> expectedValues) {

        List<TestEntity> entities = modelFactory.convertToEntityList(dtos, TestEntity.class);

        // Assertions
        assertThat(entities).hasSize(dtos.size());
        for (int i = 0; i < entities.size(); i++) {
            assertThat(entities.get(i).getName()).isEqualTo(expectedNames.get(i));
            assertThat(entities.get(i).getValue()).isEqualTo(expectedValues.get(i));
        }
    }

    @ParameterizedTest
    @MethodSource("stringToEntityDataProvider")
    void shouldConvertFromStringToEntity(String json, TestEntity expectedEntity) {
        TestEntity entity = modelFactory.convertFromString(json, TestEntity.class);

        // Assertions
        assertThat(entity).isNotNull();
        assertThat(entity.getName()).isEqualTo(expectedEntity.getName());
        assertThat(entity.getValue()).isEqualTo(expectedEntity.getValue());
    }

    @ParameterizedTest
    @MethodSource("stringToEntityListDataProvider")
    void shouldConvertFromStringToEntityList(String json, List<TestEntity> expectedEntities) {

        List<TestEntity> entities = modelFactory.convertFromStringToList(json, TestEntity.class);

        // Assertions
        assertThat(entities).hasSize(expectedEntities.size());
        for (int i = 0; i < entities.size(); i++) {
            assertThat(entities.get(i).getName()).isEqualTo(expectedEntities.get(i).getName());
            assertThat(entities.get(i).getValue()).isEqualTo(expectedEntities.get(i).getValue());
        }
    }

    @ParameterizedTest
    @MethodSource("entityToStringDataProvider")
    void shouldConvertEntityToString(TestEntity entity, String expectedJson) {
        String json = modelFactory.convertToString(entity);

        // Assertions
        assertThat(json).isEqualTo(expectedJson);
    }

    @ParameterizedTest
    @MethodSource("entityListToStringDataProvider")
    void shouldConvertEntityListToString(List<TestEntity> entityList, String expectedJson) {
        String json = modelFactory.convertEntityListToString(entityList);

        // Assertions
        assertThat(json).isEqualTo(expectedJson);
    }

    @ParameterizedTest
    @NullSource
    void shouldHandleNullInput(TestEntity entity) {
        assertThat(modelFactory.convertToDTO(entity, TestDTO.class)).isNull();
        assertThat(modelFactory.convertToDTOList(null, TestDTO.class)).isNull();
    }

    @ParameterizedTest
    @MethodSource("invalidConversionTestCases")
    void shouldThrowConversionExceptionForInvalidConversion(Object invalidEntity, String expectedErrorMessage) {
        assertThatThrownBy(() -> modelFactory.convertToDTO(invalidEntity, TestDTO.class))
                .isInstanceOf(ConversionException.class)
                .hasMessageContaining(expectedErrorMessage);
    }

    // Data providers
    private static Stream<Arguments> dtoToEntityDataProvider() {
        return Stream.of(
                arguments(new TestDTO("test1", 123), "test1", 123),
                arguments(new TestDTO("test2", 456), "test2", 456),
                arguments(new TestDTO("", 0), "", 0)
        );
    }

    private static Stream<Arguments> entityToDtoDataProvider() {
        return Stream.of(
                arguments(new TestEntity("test1", 123), "test1", 123),
                arguments(new TestEntity("test2", 456), "test2", 456),
                arguments(new TestEntity("", 0), "", 0)
        );
    }

    private static Stream<Arguments> entityToDtoListDataProvider() {
        return Stream.of(
                arguments(
                        Arrays.asList(
                                new TestEntity("test1", 123),
                                new TestEntity("test2", 456)
                        ),
                        Arrays.asList("test1", "test2"),
                        Arrays.asList(123, 456)
                ),
                arguments(
                        List.of(new TestEntity("", 0)),
                        List.of(""),
                        List.of(0)
                )
        );
    }

    private static Stream<Arguments> dtoToEntityListDataProvider() {
        return Stream.of(
                arguments(
                        Arrays.asList(new TestDTO("test1", 123), new TestDTO("test2", 456)),
                        Arrays.asList("test1", "test2"),
                        Arrays.asList(123, 456)
                ),
                arguments(
                        List.of(new TestDTO("", 0)),
                        List.of(""),
                        List.of(0)
                )
        );
    }

    private static Stream<Arguments> stringToEntityDataProvider() {
        return Stream.of(
                arguments("{\"name\":\"test1\",\"value\":123}", new TestEntity("test1", 123)),
                arguments("{\"name\":\"test2\",\"value\":456}", new TestEntity("test2", 456)),
                arguments("{\"name\":\"\",\"value\":0}", new TestEntity("", 0))
        );
    }

    private static Stream<Arguments> stringToEntityListDataProvider() {
        return Stream.of(
                arguments("[{\"name\":\"test1\",\"value\":123}, {\"name\":\"test2\",\"value\":456}]",
                        Arrays.asList(new TestEntity("test1", 123), new TestEntity("test2", 456))),
                arguments("[{\"name\":\"\",\"value\":0}]",
                        List.of(new TestEntity("", 0))),
                arguments("[{\"name\":\"\",\"value\":0}]",
                        List.of(new TestEntity("", 0))),
                // Case for null or blank input string
                arguments(null, Collections.emptyList()),
                arguments("", Collections.emptyList()),
                arguments("   ", Collections.emptyList()) // Blank space case
        );
    }

    private static Stream<Arguments> entityToStringDataProvider() {
        return Stream.of(
                arguments(new TestEntity("test1", 123), "{\"name\":\"test1\",\"value\":123}"),
                arguments(new TestEntity("test2", 456), "{\"name\":\"test2\",\"value\":456}"),
                arguments(new TestEntity("", 0), "{\"name\":\"\",\"value\":0}")
        );
    }

    private static Stream<Arguments> entityListToStringDataProvider() {
        return Stream.of(
                arguments(
                        Arrays.asList(new TestEntity("test1", 123), new TestEntity("test2", 456)),
                        "[{\"name\":\"test1\",\"value\":123},{\"name\":\"test2\",\"value\":456}]"
                ),
                arguments(List.of(new TestEntity("test3", 789)), "[{\"name\":\"test3\",\"value\":789}]"),
                arguments(List.of(new TestEntity("", 0)), "[{\"name\":\"\",\"value\":0}]")
        );
    }

    private static Stream<Arguments> invalidConversionTestCases() {
        return Stream.of(
                arguments(new InvalidEntity(), "Failed to convert entity to POJO"),
                arguments(new InvalidCyclicEntity(), "Failed to convert entity to POJO")
        );
    }

    // Test Data Classes
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class TestEntity {
        private String name;
        private Integer value;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class TestDTO {
        private String name;
        private Integer value;
    }

    static class InvalidEntity {
        private final Object invalidField = new Object();
    }

    static class InvalidCyclicEntity {
        private final InvalidCyclicEntity self = this;
    }
}