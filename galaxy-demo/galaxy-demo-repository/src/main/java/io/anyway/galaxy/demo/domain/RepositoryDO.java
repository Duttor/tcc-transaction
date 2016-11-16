package io.anyway.galaxy.demo.domain;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Created by yangzz on 16/7/19.
 */
@Setter
@Getter
public class RepositoryDO implements Serializable{

    private long productId;

    private String category;

    private long amount;

    private long price;

    public RepositoryDO(long productId, long amount) {
        this.productId = productId;
        this.amount = amount;
    }

    public RepositoryDO(long productId, String category, long amount, long price) {
        this.productId = productId;
        this.category = category;
        this.amount = amount;
        this.price = price;
    }

}
