package io.anyway.galaxy.console.service;

import io.anyway.galaxy.console.domain.BusinessTypeInfo;
import io.anyway.galaxy.console.domain.TransactionInfo;

import java.util.List;

/**
 * Created by xiong.j on 2016/8/4.
 */
public interface TransactionInfoService {

    List<TransactionInfo> list(TransactionInfo transactionInfo);

}
