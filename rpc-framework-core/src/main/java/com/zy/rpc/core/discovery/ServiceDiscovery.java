package com.zy.rpc.core.discovery;


import com.zy.rpc.core.extension.SPI;
import com.zy.rpc.core.message.RpcRequest;
import com.zy.rpc.core.message.ServiceInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * 服务发现接口类
 *
 * @author zy
 * @version 1.0
 */
@SPI
public interface ServiceDiscovery {

    /**
     * 进行服务发现
     *
     * @param request Rpc请求，封装了请求的服务名
     * @return 返回服务提供方信息
     */
    ServiceInfo discover(RpcRequest request);

    /**
     * 返回服务的所有提供方，若未实现，默认返回空的 ArrayList
     *
     * @param serviceName 服务名称
     * @return 所有的服务提供方信息
     */
    default List<ServiceInfo> getServices(String serviceName) throws Exception {

        return new ArrayList<>();
    }

    /**
     * 关闭与服务注册中心的连接
     */
    void destroy() throws Exception;

}
