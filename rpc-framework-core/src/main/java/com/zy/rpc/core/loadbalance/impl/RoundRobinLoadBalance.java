package com.zy.rpc.core.loadbalance.impl;


import com.zy.rpc.core.loadbalance.AbstractLoadBalance;
import com.zy.rpc.core.message.RpcRequest;
import com.zy.rpc.core.message.ServiceInfo;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 轮询负载均衡算法
 *
 * @author zy
 * @version 1.0
 */
public class RoundRobinLoadBalance extends AbstractLoadBalance {

    private static final AtomicInteger atomicInteger = new AtomicInteger(0);

    @Override
    public ServiceInfo doSelect(List<ServiceInfo> invokers, RpcRequest request) {
        return invokers.get(getAndIncrement() % invokers.size());
    }

    /**
     * 返回当前值并加一，通过 CAS 原子更新，当当前值到达 {@link Integer#MAX_VALUE} 时，重新设值为 0
     *
     * @return 返回当前的值
     */
    public final int getAndIncrement() {
        int prev, next;
        do {
            prev = atomicInteger.get();
            next = prev == Integer.MAX_VALUE ? 0 : prev + 1;
        } while (!atomicInteger.compareAndSet(prev, next));
        return prev;
    }

}
