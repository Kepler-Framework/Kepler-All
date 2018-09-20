package com.kepler.config.manager;

/**
 * @author fatduo
 * @date 14/09/2018
 */
public interface ConfigManager {

    String get(String namespace, String key);

}
