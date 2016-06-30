package com.kepler.service.imported;

import com.kepler.config.PropertiesUtils;
import com.kepler.connection.Connect;
import com.kepler.host.HostsContext;
import com.kepler.service.ImportedListener;
import com.kepler.service.InstanceBlocker;
import com.kepler.service.Service;
import com.kepler.service.ServiceInstance;

/**
 * @author 张皆浩 2015年9月11日
 */
public class DefaultImportedListener implements ImportedListener {

	private static final boolean REPAIR = PropertiesUtils.get(DefaultImportedListener.class.getName().toLowerCase() + ".repair", true);

	private final HostsContext context;

	private final Connect connect;
	
	private final InstanceBlocker blocker;

	public DefaultImportedListener(HostsContext context, Connect connect, InstanceBlocker blocker) {
		super();
		this.context = context;
		this.connect = connect;
		this.blocker = blocker;
	}

	@Override
	public void add(ServiceInstance instance) throws Exception {
		if(this.blocker.blocked(instance)) {
			return;
		}
		this.context.getOrCreate(new Service(instance.service(), instance.version(), instance.catalog())).wait(instance.host());
		this.connect.connect(instance.host());
	}

	@Override
	public void delete(ServiceInstance instance) throws Exception {
		this.context.remove(instance.host(), new Service(instance.service(), instance.version(), instance.catalog()));
	}

	@Override
	public void change(ServiceInstance current, ServiceInstance newInstance) throws Exception {
		if(this.blocker.blocked(newInstance)) {
			this.delete(current);
			return;
		}
		this.context.getOrCreate(new Service(current.service(), current.version(), current.catalog())).replace(current.host(), newInstance.host());
		this.repair(newInstance);
	}

	private void repair(ServiceInstance newInstance) throws Exception {
		// 故障恢复 (If necessory)
		if (DefaultImportedListener.REPAIR) {
			this.connect.connect(newInstance.host());
		}
	}
}
