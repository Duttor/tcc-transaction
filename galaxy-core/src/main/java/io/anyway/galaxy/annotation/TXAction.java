package io.anyway.galaxy.annotation;

import io.anyway.galaxy.common.TransactionTypeEnum;

import java.lang.annotation.*;

/**
 * Created by yangzz on 16/7/20.
 * 分布式事务入口
 *
 * @Transactional
 * @TXAction
 * public void purchase(){
 *      RepositoryDO repository= ...;
 *      repositoryService.decreaseRepository(repository);
 *      OrderDO order= ...;
 *      orderService.addNewOrder(order);
 * }
 */

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
@Documented
@Inherited
public @interface TXAction {
    /**
     * 业务操作类型
     * @return
     */
    String bizType() default "";
    /**
     * 定义分布式事务执行的超时时间,默认不启用
     * @return
     */
    int timeout() default -1;

    /**
     * 事务类型TC|TCC
     * @return
     */
    TransactionTypeEnum value() default TransactionTypeEnum.TC;

}
