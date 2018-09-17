package com.kepler.mock.impl;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.kepler.config.PropertiesUtils;
import com.kepler.config.manager.EtcdConfigManager;
import com.kepler.invoker.Invoker;
import com.kepler.mock.Mocker;
import com.kepler.org.apache.commons.lang.StringUtils;
import com.kepler.protocol.Request;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @author fatduo
 * @date 17/09/2018
 */
public class DefaultMocker implements Mocker {

    private static final Log LOGGER = LogFactory.getLog(DefaultMocker.class);

    /**
     * 是否需要Default Mocker
     */
    private static final boolean ACTIVED = PropertiesUtils.get(DefaultMocker.class.getName().toLowerCase() + ".actived", false);

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final EtcdConfigManager etcdConfigManager;

    public DefaultMocker(EtcdConfigManager etcdConfigManager) {
        this.etcdConfigManager = etcdConfigManager;
    }

    @Override
    public Object mock(Request request, Method method) throws Exception {
        Object rtn = Invoker.EMPTY;
        String mock = etcdConfigManager.get(getRequestKey(request), "mock");
        if (StringUtils.isNotEmpty(mock)) {
            if (method.getGenericReturnType() == null) {
                return mock;
            }

            try {
                JavaType javaType = getJavaType(method.getGenericReturnType());
                rtn = OBJECT_MAPPER.readValue(mock, javaType);
            } catch (Exception e) {
                DefaultMocker.LOGGER.error("Cannot mock request: " + request.toString(), e);
            }
        }

        return rtn;
    }

    public boolean actived() {
        return DefaultMocker.ACTIVED;
    }

    private String getRequestKey(Request request) {
        StringBuilder sb = new StringBuilder();
        sb.append(request.service().service()).append("/").append(request.service().versionAndCatalog()).append("/");
        sb.append(request.method());
        sb.append("(");
        for (Class<?> aClass : request.types()) {
            sb.append(aClass.getName());
            sb.append(",");
        }
        sb.deleteCharAt(sb.length() - 1).append(")");

        return sb.toString();
    }

    private JavaType getJavaType(Type type) {
        // 判断是否带有泛型
        if (type instanceof ParameterizedType) {
            Type[] actualTypeArguments = ((ParameterizedType) type).getActualTypeArguments();
            // 获取泛型类型
            Class rowClass = (Class) ((ParameterizedType) type).getRawType();

            JavaType[] javaTypes = new JavaType[actualTypeArguments.length];

            for (int i = 0; i < actualTypeArguments.length; i++) {
                // 泛型也可能带有泛型，递归获取
                javaTypes[i] = getJavaType(actualTypeArguments[i]);
            }
            return TypeFactory.defaultInstance().constructParametricType(rowClass, javaTypes);
        } else {
            // 简单类型直接用该类构建JavaType
            Class cla = (Class) type;
            return TypeFactory.defaultInstance().constructParametricType(cla, new JavaType[0]);
        }
    }

}
