# tcc-transaction
Distribute Transaction Framework 分布式事务框架tcc

提供基于两阶段提交（Try/Confirm/Cancel）的Java分布式事务实现方案

## 支持范围
* JDK6+
* All RPC
* Springframework 3.2+
* MySQL

## 功能简介

* 事务预提交
* 事务确认提交
* 事务回滚

Try: 尝试执行业务

    完成所有业务检查（一致性）

    预留必须业务资源（准隔离性）

Confirm: 确认执行业务

    真正执行业务

    不作任何业务检查

    只使用Try阶段预留的业务资源

    Confirm操作满足幂等性

Cancel: 取消执行业务

    释放Try阶段预留的业务资源

    Cancel操作满足幂等性
    
## Demo
* galaxy-demo-purchase：商城系统
* galaxy-demo-repository：库存系统
* galaxy-demo-order：订单系统

### 测试用例
* 正常下单：localhost:8080/purchase/rest/{quantity}   
* 模拟减库存失败：localhost:8080/purchase/rest/case/1     
* 模拟下单失败：localhost:8080/purchase/rest/case/2     
* 模拟减库存超时：localhost:8080/purchase/rest/case/3     
* 模拟下单超时：localhost:8080/purchase/rest/case/4     
