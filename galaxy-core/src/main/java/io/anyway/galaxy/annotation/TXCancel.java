package io.anyway.galaxy.annotation;

import java.lang.annotation.*;

/**
 * Created by yangzz on 16/7/20.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
@Documented
@Inherited
public @interface TXCancel {
    int tryTime() default 3;
}
