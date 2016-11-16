package io.anyway.galaxy.demo.service.impl;

import io.anyway.galaxy.annotation.TXCancel;
import io.anyway.galaxy.annotation.TXTry;
import io.anyway.galaxy.context.ModuleContext;
import io.anyway.galaxy.context.ModuleContextAdapter;
import io.anyway.galaxy.context.TXContext;
import io.anyway.galaxy.demo.dao.RepositoryDao;
import io.anyway.galaxy.demo.domain.RepositoryDO;
import io.anyway.galaxy.demo.service.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**add
 * Created by yangzz on 16/7/19.
 */
@Service
public class RepositoryServiceImpl implements RepositoryService,ModuleContextAdapter {

    @Autowired
    private ModuleContext moduleContext;

    @Autowired
    private RepositoryDao dao;


    @Transactional
    public void addRepository(RepositoryDO repositoryDO){
        dao.add(repositoryDO);
    }

    @Override
    @Transactional
    @TXTry(cancel = "increaseRepository")
    public boolean decreaseRepository(TXContext ctx,long productId, long amount){
        return 0 < dao.decrease(new RepositoryDO(productId, amount));
    }

    @Override
    @Transactional
    @TXCancel
    public boolean increaseRepository(TXContext ctx,long productId,long amount){
        return 0 < dao.increase(new RepositoryDO(productId, amount));
    }

    @Override
    public ModuleContext getModuleContext() {
        return moduleContext;
    }

}
