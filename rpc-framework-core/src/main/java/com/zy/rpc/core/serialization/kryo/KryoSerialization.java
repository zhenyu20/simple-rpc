package com.zy.rpc.core.serialization.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.zy.rpc.core.message.RpcRequest;
import com.zy.rpc.core.message.RpcResponse;
import com.zy.rpc.core.exception.SerializeException;
import com.zy.rpc.core.serialization.Serialization;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * Kryo 序列化算法
 * <p>
 * <a href="https://www.cnblogs.com/lxyit/p/12511645.html">相关简介</a><br>
 * <a href="https://github.com/EsotericSoftware/kryo">github地址</a>
 * </p>
 *
 * @author zy
 * @version 1.0
 */
public class KryoSerialization implements Serialization {

    // kryo 线程不安全，所以使用 ThreadLocal 保存 kryo 对象
    private final ThreadLocal<Kryo> kryoThreadLocal = ThreadLocal.withInitial(() -> {
        Kryo kryo = new Kryo();
        kryo.register(RpcRequest.class);
        kryo.register(RpcResponse.class);
        return kryo;
    });

    @Override
    public <T> byte[] serialize(T object) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Output output = new Output(baos);
            Kryo kryo = kryoThreadLocal.get();
            // 将对象序列化为 byte 数组
            kryo.writeObject(output, object);
            kryoThreadLocal.remove();
            return output.toBytes();
        } catch (Exception e) {
            throw new SerializeException("Kryo serialize failed.", e);
        }
    }

    @Override
    public <T> T deserialize(Class<T> clazz, byte[] bytes) {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            Input input = new Input(bais);
            Kryo kryo = kryoThreadLocal.get();
            // 将 byte 数组反序列化为 T 对象
            T object = kryo.readObject(input, clazz);
            kryoThreadLocal.remove();
            return object;
        } catch (Exception e) {
            throw new SerializeException("Kryo deserialize failed.", e);
        }
    }
}
