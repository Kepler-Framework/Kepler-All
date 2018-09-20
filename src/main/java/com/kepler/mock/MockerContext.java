package com.kepler.mock;

import com.kepler.service.Service;
import org.springframework.core.Ordered;

/**
 * @author kim 2016年1月13日
 */
public interface MockerContext extends Ordered {

    Mocker get(Service service);

}
