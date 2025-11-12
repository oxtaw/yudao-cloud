package cn.iocoder.yudao.framework.excel.core.util;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.excel.core.annotations.ExcelFieldLength;
import cn.idev.excel.annotation.ExcelProperty;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Excel 导入字段长度校验工具
 */
public final class ExcelFieldLengthValidator {

    private ExcelFieldLengthValidator() {
    }

    /**
     * 校验单个对象的字段长度
     *
     * @param bean 被校验的对象
     * @return 校验失败的提示列表
     */
    public static List<String> validate(Object bean) {
        return validate(bean, Collections.emptyMap());
    }

    /**
     * 基于注解或手动配置的限制，校验单个对象的字段长度
     *
     * @param bean            被校验的对象
     * @param fieldLengthMap  字段长度限制配置，key 支持字段名、Excel 表头或注解中声明的 fieldName
     * @return 校验失败的提示列表
     */
    public static List<String> validate(Object bean, Map<String, Integer> fieldLengthMap) {
        if (bean == null) {
            return Collections.emptyList();
        }
        Map<String, Integer> config = fieldLengthMap != null ? fieldLengthMap : Collections.emptyMap();

        List<String> errors = new ArrayList<>();
        for (Field field : getAllFields(bean.getClass())) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            ExcelFieldLength annotation = field.getAnnotation(ExcelFieldLength.class);
            Integer maxLength = resolveMaxLength(field, annotation, config);
            if (maxLength == null || maxLength <= 0) {
                continue;
            }

            Object value = getFieldValue(bean, field);
            if (value == null) {
                continue;
            }
            int actualLength = calculateLength(value);
            if (actualLength <= maxLength) {
                continue;
            }

            String fieldLabel = resolveFieldLabel(field, annotation);
            errors.add(buildMessage(annotation, fieldLabel, maxLength, actualLength));
        }
        return errors;
    }

    /**
     * 校验集合中每一个对象的字段长度
     *
     * @param beans 待校验的数据集合
     * @return 校验失败的提示列表
     */
    public static List<String> validate(Collection<?> beans) {
        return validate(beans, Collections.emptyMap());
    }

    /**
     * 基于注解或手动配置的限制，校验集合中每一个对象的字段长度
     *
     * @param beans           待校验的数据集合
     * @param fieldLengthMap  字段长度限制配置，key 支持字段名、Excel 表头或注解中声明的 fieldName
     * @return 校验失败的提示列表
     */
    public static List<String> validate(Collection<?> beans, Map<String, Integer> fieldLengthMap) {
        if (beans == null || beans.isEmpty()) {
            return Collections.emptyList();
        }
        Map<String, Integer> config = fieldLengthMap != null ? fieldLengthMap : Collections.emptyMap();

        List<String> errors = new ArrayList<>();
        for (Object bean : beans) {
            errors.addAll(validate(bean, config));
        }
        return errors;
    }

    private static List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            Field[] declaredFields = current.getDeclaredFields();
            Collections.addAll(fields, declaredFields);
            current = current.getSuperclass();
        }
        return fields;
    }

    private static Integer resolveMaxLength(Field field, ExcelFieldLength annotation, Map<String, Integer> config) {
        if (annotation != null && annotation.value() > 0) {
            return annotation.value();
        }
        if (config.isEmpty()) {
            return null;
        }
        Set<String> candidates = buildCandidateKeys(field, annotation);
        for (String candidate : candidates) {
            Integer length = config.get(candidate);
            if (length != null && length > 0) {
                return length;
            }
        }
        return null;
    }

    private static Set<String> buildCandidateKeys(Field field, ExcelFieldLength annotation) {
        Set<String> candidates = new LinkedHashSet<>();
        candidates.add(field.getName());
        if (annotation != null && StrUtil.isNotBlank(annotation.fieldName())) {
            candidates.add(annotation.fieldName());
        }
        ExcelProperty property = field.getAnnotation(ExcelProperty.class);
        if (property != null) {
            String[] headers = property.value();
            if (headers != null) {
                for (String header : headers) {
                    if (StrUtil.isNotBlank(header)) {
                        candidates.add(header);
                    }
                }
            }
        }
        return candidates;
    }

    private static String resolveFieldLabel(Field field, ExcelFieldLength annotation) {
        if (annotation != null && StrUtil.isNotBlank(annotation.fieldName())) {
            return annotation.fieldName();
        }
        ExcelProperty property = field.getAnnotation(ExcelProperty.class);
        if (property != null) {
            String[] headers = property.value();
            if (headers != null) {
                for (String header : headers) {
                    if (StrUtil.isNotBlank(header)) {
                        return header;
                    }
                }
            }
        }
        return field.getName();
    }

    private static String buildMessage(ExcelFieldLength annotation, String fieldLabel, int maxLength, int actualLength) {
        if (annotation != null && StrUtil.isNotBlank(annotation.message())) {
            return String.format(annotation.message(), fieldLabel, maxLength, actualLength);
        }
        return StrUtil.format("字段【{}】长度不能超过 {} 个字符，当前长度为 {}", fieldLabel, maxLength, actualLength);
    }

    private static Object getFieldValue(Object bean, Field field) {
        try {
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            return field.get(bean);
        } catch (IllegalAccessException ex) {
            throw new IllegalStateException(StrUtil.format("无法读取字段[{}]进行校验", field.getName()), ex);
        }
    }

    private static int calculateLength(Object value) {
        if (value instanceof CharSequence) {
            return ((CharSequence) value).length();
        }
        if (value instanceof Number) {
            return value.toString().length();
        }
        if (value.getClass().isArray()) {
            return java.lang.reflect.Array.getLength(value);
        }
        if (value instanceof Collection<?>) {
            return ((Collection<?>) value).size();
        }
        if (value instanceof Map<?, ?>) {
            return ((Map<?, ?>) value).size();
        }
        return String.valueOf(value).length();
    }

}
