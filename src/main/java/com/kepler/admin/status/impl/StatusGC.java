package com.kepler.admin.status.impl;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.kepler.admin.status.Refresh;
import com.kepler.config.PropertiesUtils;

/**
 * @author kim 2016年1月2日
 */
public class StatusGC extends StatusDynamic implements Refresh {

	// 允许收集的最大数量(每个周期)
	private static final byte MAX = PropertiesUtils.get(StatusGC.class.getName().toLowerCase() + ".max", (byte) 10);

	private final List<GarbageCollectorMXBean> collector = ManagementFactory.getGarbageCollectorMXBeans();

	// 最后统计快照
	private final Map<String, Long> snapshot = new HashMap<String, Long>();

	public StatusGC() {
		super(StatusGC.names());
		this.init();
	}

	/**
	 * 获取GC收集器名称
	 * 
	 * @return
	 */
	private static String[] names() {
		List<GarbageCollectorMXBean> collector = ManagementFactory.getGarbageCollectorMXBeans();
		String[] names = new String[collector.size()];
		for (int index = 0; index < collector.size(); index++) {
			names[index] = collector.get(index).getName();
		}
		return names;
	}

	// 初始化快照
	private void init() {
		for (GarbageCollectorMXBean each : this.collector) {
			this.snapshot.put(each.getName(), 0L);
		}
	}

	@Override
	public void refresh() {
		// 当前时间(秒)
		long current = TimeUnit.SECONDS.convert(System.currentTimeMillis(), TimeUnit.SECONDS);
		for (GarbageCollectorMXBean each : this.collector) {
			// 获取GC总时间
			long time = each.getCollectionTime();
			// 添加本次GC时间
			super.add(each.getName(), current, time - this.snapshot.get(each.getName()));
			// 更新GC快照时间
			this.snapshot.put(each.getName(), time);
		}
	}

	@Override
	protected byte max() {
		return StatusGC.MAX;
	}
}
