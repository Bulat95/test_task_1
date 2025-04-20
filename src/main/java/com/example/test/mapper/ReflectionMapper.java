package com.example.test.mapper;

import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

@Service
public class ReflectionMapper {

    public static <D, E> E toEntity(D dto, Class<E> entityClass) {
        try {
            E entity = entityClass.getDeclaredConstructor().newInstance();
            copyProperties(dto, entity);
            return entity;
        } catch (Exception e) {
            throw new RuntimeException("Ошибка преобразования DTO в Entity: " + e.getMessage(), e);
        }
    }

    public static <E, D> D toDto(E entity, Class<D> dtoClass) {
        try {
            D dto = dtoClass.getDeclaredConstructor().newInstance();
            copyProperties(entity, dto);
            return dto;
        } catch (Exception e) {
            throw new RuntimeException("Ошибка преобразования Entity в DTO: " + e.getMessage(), e);
        }
    }

    private static void copyProperties(Object source, Object target) {
        Map<String, Field> sourceFields = getFieldMap(source.getClass());
        Map<String, Field> targetFields = getFieldMap(target.getClass());

        for (Map.Entry<String, Field> sourceEntry : sourceFields.entrySet()) {
            String fieldName = sourceEntry.getKey();
            Field sourceField = sourceEntry.getValue();
            Field targetField = targetFields.get(fieldName);

            if (targetField != null && isCompatible(sourceField, targetField)) {
                try {
                    sourceField.setAccessible(true);
                    targetField.setAccessible(true);
                    Object value = sourceField.get(source);
                    targetField.set(target, value);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Ошибка копии поля : " + fieldName, e);
                }
            }
        }
    }

    private static Map<String, Field> getFieldMap(Class<?> clazz) {
        Map<String, Field> fieldMap = new HashMap<>();
        while (clazz != null) {
            for (Field field : clazz.getDeclaredFields()) {
                fieldMap.put(field.getName(), field);
            }
            clazz = clazz.getSuperclass();
        }
        return fieldMap;
    }


    private static boolean isCompatible(Field sourceField, Field targetField) {
        return targetField.getType().isAssignableFrom(sourceField.getType());
    }

}