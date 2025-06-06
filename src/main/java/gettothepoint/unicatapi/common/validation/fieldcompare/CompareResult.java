package gettothepoint.unicatapi.common.validation.fieldcompare;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 검증 결과를 나타낼 필드를 표시하는 어노테이션
 */
@Target({ElementType.FIELD,ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface CompareResult {
}
