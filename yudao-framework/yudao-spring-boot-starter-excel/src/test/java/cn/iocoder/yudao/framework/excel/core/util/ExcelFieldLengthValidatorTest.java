package cn.iocoder.yudao.framework.excel.core.util;

import cn.iocoder.yudao.framework.excel.core.annotations.ExcelFieldLength;
import cn.idev.excel.annotation.ExcelProperty;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link ExcelFieldLengthValidator} 的单元测试
 */
class ExcelFieldLengthValidatorTest {

    @Test
    void shouldPassWhenValueWithinLimit() {
        AnnotatedSample sample = new AnnotatedSample("12345");

        List<String> errors = ExcelFieldLengthValidator.validate(sample);

        assertThat(errors).isEmpty();
    }

    @Test
    void shouldFailWhenValueExceedsLimit() {
        AnnotatedSample sample = new AnnotatedSample("123456");

        List<String> errors = ExcelFieldLengthValidator.validate(sample);

        assertThat(errors)
                .hasSize(1)
                .first()
                .satisfies(message -> {
                    assertThat(message).contains("字段A");
                    assertThat(message).contains("5");
                });
    }

    @Test
    void shouldUseExcelHeaderWhenConfiguredByMap() {
        ConfigSample sample = new ConfigSample("abcd");

        List<String> errors = ExcelFieldLengthValidator.validate(sample, Collections.singletonMap("配置字段", 3));

        assertThat(errors)
                .hasSize(1)
                .first()
                .satisfies(message -> {
                    assertThat(message).contains("配置字段");
                    assertThat(message).contains("3");
                });
    }

    @Test
    void shouldAggregateCollectionValidationErrors() {
        List<Object> samples = Arrays.asList(
                new AnnotatedSample("123456"),
                new ConfigSample("abcd")
        );

        List<String> errors = ExcelFieldLengthValidator.validate(samples, Collections.singletonMap("配置字段", 3));

        assertThat(errors).hasSize(2);
    }

    @Test
    void shouldValidateNumericTypes() {
        NumericSample withinLimit = new NumericSample(123, 12345L);
        NumericSample exceedLimit = new NumericSample(1234, 123456L);

        List<String> withinErrors = ExcelFieldLengthValidator.validate(withinLimit);
        List<String> exceedErrors = ExcelFieldLengthValidator.validate(exceedLimit);

        assertThat(withinErrors).isEmpty();
        assertThat(exceedErrors).hasSize(2);
        assertThat(exceedErrors).anySatisfy(message -> assertThat(message).contains("编号"));
        assertThat(exceedErrors).anySatisfy(message -> assertThat(message).contains("编码"));
    }

    @Test
    void shouldSupportCustomErrorMessage() {
        CustomMessageSample sample = new CustomMessageSample("abcdef");

        List<String> errors = ExcelFieldLengthValidator.validate(sample);

        assertThat(errors)
                .hasSize(1)
                .first()
                .isEqualTo("字段: 自定义, 限制: 5, 实际: 6");
    }

    private static final class AnnotatedSample {

        @ExcelFieldLength(value = 5, fieldName = "字段A")
        private final String name;

        private AnnotatedSample(String name) {
            this.name = name;
        }

    }

    private static final class ConfigSample {

        @ExcelProperty("配置字段")
        private final String title;

        private ConfigSample(String title) {
            this.title = title;
        }

    }

    private static final class NumericSample {

        @ExcelFieldLength(value = 3, fieldName = "编号")
        private final Integer number;

        @ExcelFieldLength(value = 5, fieldName = "编码")
        private final Long code;

        private NumericSample(Integer number, Long code) {
            this.number = number;
            this.code = code;
        }

    }

    private static final class CustomMessageSample {

        @ExcelFieldLength(value = 5, fieldName = "自定义", message = "字段: %s, 限制: %d, 实际: %d")
        private final String content;

        private CustomMessageSample(String content) {
            this.content = content;
        }

    }

}
