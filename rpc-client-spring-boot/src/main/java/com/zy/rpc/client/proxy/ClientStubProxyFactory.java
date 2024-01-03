package com.zy.rpc.client.proxy;

import com.zy.rpc.client.config.RpcClientProperties;
import com.zy.rpc.client.transport.RpcClient;
import com.zy.rpc.core.discovery.ServiceDiscovery;
import com.zy.rpc.core.util.ServiceUtil;
import net.sf.cglib.proxy.Enhancer;

import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 客户端代理工厂类，返回服务代理类
 *
 * @author zy
 * @version 1.0
 */
public class ClientStubProxyFactory {

    /**
     * 服务发现中心实现类
     */
    private final ServiceDiscovery discovery;

    /**
     * RpcClient 传输实现类
     */
    private final RpcClient rpcClient;

    /**
     * 客户端配置属性
     */
    private final RpcClientProperties properties;

    public ClientStubProxyFactory(ServiceDiscovery discovery, RpcClient rpcClient, RpcClientProperties properties) {
        this.discovery = discovery;
        this.rpcClient = rpcClient;
        this.properties = properties;
    }

    /**
     * 代理对象缓存
     */
    private static final Map<String, Object> proxyMap = new ConcurrentHashMap<>();

    /**
     * 获取代理对象
     *
     * @param clazz   服务接口类型
     * @param version 版本号
     * @param <T>     代理对象的参数类型
     * @return 对应版本的代理对象
     */
    @SuppressWarnings("unchecked")
    public <T> T getProxy(Class<T> clazz, String version) {
        return (T) proxyMap.computeIfAbsent(ServiceUtil.serviceKey(clazz.getName(), version), serviceName -> {
            // 如果目标类是一个接口或者 是 java.lang.reflect.Proxy 的子类 则默认使用 JDK 动态代理
            if (clazz.isInterface() || Proxy.isProxyClass(clazz)) {

                return Proxy.newProxyInstance(clazz.getClassLoader(),
                        new Class[]{clazz}, // 注意，这里的接口是 clazz 本身（即，要代理的实现类所实现的接口）
                        new ClientStubInvocationHandler(discovery, rpcClient, properties, serviceName));
            } else { // 使用 CGLIB 动态代理
                // 创建动态代理增加类
                Enhancer enhancer = new Enhancer();
                // 设置类加载器
                enhancer.setClassLoader(clazz.getClassLoader());
                // 设置被代理类
                enhancer.setSuperclass(clazz);
                // 设置方法拦截器
                enhancer.setCallback(new ClientStubMethodInterceptor(discovery, rpcClient, properties, serviceName));
                // 创建代理类
                return enhancer.create();
            }
        });
    }

}
