package com.kepler.queue;

import com.kepler.annotation.Queue;
import com.kepler.service.Service;

/**
 * @author KimShen
 *
 */
public interface QueueRegister {

	public void register(Service service, Queue queue);
}
