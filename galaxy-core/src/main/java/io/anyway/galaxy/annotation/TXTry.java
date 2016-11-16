package io.anyway.galaxy.annotation;

import java.lang.annotation.*;

/**
 * Created by yangzz on 16/7/20.
 * 声明式事务尝试方法,并指定confirm和cancel方法
 *
 * //减库存操作
 * @Transactional
 * @TXTry(cancel="cancelDecreaseRepository")
 * public void decreaseRepository(RepositoryDO repository){
 *     ...
 * }
 *
 * @Transactional
 * @TXCancel
 * public void cancelDecreaseRepository(RepositoryDO repository){
 *     ...
 * }
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
@Documented
@Inherited
public @interface TXTry {
    /**
     * 业务操作类型
     * @return
     */
    String bizType() default "";

    /**
     * 提交方法
     * @return
     */
    String confirm() default "";

    /**
     * 回滚方法
     * @return
     */
    String cancel() default "";
}
