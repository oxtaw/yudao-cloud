package cn.iocoder.yudao.framework.excel.core.annotations;

import java.lang.annotation.*;

/**
 * Excel 导入字段长度限制
 *
 * <p>
 * 用于标记字段在 Excel 导入时允许的最大长度，通常与数据库字段长度保持一致。
 * </p>
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface ExcelFieldLength {

    /**
     * 字段允许的最大长度
     *
     * @return 最大长度
     */
    int value();

    /**
     * 字段的人类可读名称。当为空时，默认取字段名或 Excel 表头
     *
     * @return 字段名称
     */
    String fieldName() default "";

    /**
     * 自定义的错误提示文案，支持使用 {@link java.lang.String#format(String, Object...)} 风格的占位符。
     * <p>
     * 如果不填，则默认使用「字段【字段名称】长度不能超过 X 个字符，当前长度为 Y」。
     * </p>
     *
     * @return 自定义提示
     */
    String message() default "";

}
