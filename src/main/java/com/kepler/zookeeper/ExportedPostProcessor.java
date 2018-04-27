package com.kepler.zookeeper;

import com.kepler.service.Service;

/**
 * Created by longyaokun on 2018/4/27.
 */
public interface ExportedPostProcessor {

    void afterExported(Service service, Object instance) throws Exception;

    /**
     * 排序号
     *
     * @return
     */
    int sort();
}
