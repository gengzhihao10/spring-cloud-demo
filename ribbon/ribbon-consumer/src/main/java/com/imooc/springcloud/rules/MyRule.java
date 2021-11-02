package com.imooc.springcloud.rules;

import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.AbstractLoadBalancerRule;
import com.netflix.loadbalancer.IRule;
import com.netflix.loadbalancer.Server;
import lombok.NoArgsConstructor;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

@NoArgsConstructor
public class MyRule extends AbstractLoadBalancerRule implements IRule {

    @Override
    public void initWithNiwsConfig(IClientConfig iClientConfig) {

    }

    @Override
    public Server choose(Object o) {
        HttpServletRequest request =
                ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes())
                        .getRequest();
        String uri = request.getServletPath() + "?" + request.getQueryString();
        return route(uri.hashCode(), getLoadBalancer().getAllServers());
    }

    public Server route(int hashId, List<Server> addressList){
        //服务器列表为空，则返回空
        if (CollectionUtils.isEmpty(addressList)){
            return null;
        }
        TreeMap<Long, Server> address = new TreeMap<Long, Server>();
        addressList.stream().forEach(e -> {
            //虚化若干个服务节点，到环上
            for (int i = 0; i < 8; i++){
                long hash = hash(e.getId() + i);
                address.put(hash, e);
            }
        });
        long hash = hash(String.valueOf(hashId));
        //找到比hash值更大的address中的key（服务器节点id的哈希值）
        SortedMap<Long, Server> last = address.tailMap(hash);

        //当request URL的hash值大于任意一个服务器对应的hashKey,
        //取address中的第一个节点
        if (last.isEmpty()){
            address.firstEntry().getValue();
        }
        //一致性哈希：返回比hash大的value(某一个服务器节点对象)
        return last.get(last.firstKey());
    }

    public long hash(String key){
        MessageDigest md5;
        try {
            md5 = MessageDigest.getInstance("MD5");
        }catch (NoSuchAlgorithmException e){
            throw new RuntimeException(e);
        }

        byte[] keyByte = null;
        try {
            keyByte = key.getBytes("UTF-8");
        }catch (UnsupportedEncodingException e){
            throw new RuntimeException(e);
        }

        md5.update(keyByte);
        byte[] digest = md5.digest();

        long hashCode = ((long)(digest[2] & 0xFF << 16))
                | ((long) (digest[1] & 0xFF << 8))
                | ((long) (digest[0] & 0xFF));
        return hashCode & 0xFFFFFFFFL;
    }
}
