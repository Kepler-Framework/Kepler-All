package com.kepler.zookeeper;

import com.kepler.extension.Extension;
import com.kepler.service.Service;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;

/**
 * 服务发布的后置处理
 * Created by longyaokun on 2018/4/27.
 */
public class ExportedPostProcessors implements ExportedPostProcessor, Extension {

    private static final Log LOGGER = LogFactory.getLog(ExportedPostProcessors.class);

    private ExecutorService executor;

    public ExportedPostProcessors(ExecutorService executor) {
        this.executor = executor;
    }

    // sort越小排序越靠前, 如果相同就使用ClassName排序
    private final Set<ExportedPostProcessor> processors = new TreeSet<ExportedPostProcessor>(new Comparator<ExportedPostProcessor>() {
        @Override
        public int compare(ExportedPostProcessor o1, ExportedPostProcessor o2) {
            int sort = o1.sort() - o2.sort();
            return sort != 0 ? sort : o1.getClass().getName().compareTo(o2.getClass().getName());
        }
    });

    @Override
    public void afterExported(Service service, Object instance) throws Exception {
        if (!this.processors.isEmpty()) {
            for (ExportedPostProcessor processor : this.processors) {
                try {
                    executor.execute(new ProcessTask(service, instance, processor));
                } catch (Throwable e) {
                    LOGGER.warn(processor.getClass().getName() + ":" + e.getMessage());
                }
            }
        }
    }

    @Override
    public int sort() {
        return 0;
    }

    @Override
    public Extension install(Object instance) {
        this.processors.add(ExportedPostProcessor.class.cast(instance));
        return this;
    }

    @Override
    public Class<?> interested() {
        return ExportedPostProcessor.class;
    }

    class ProcessTask implements Runnable {

        private Service service;

        private Object instance;

        private ExportedPostProcessor processor;

        public ProcessTask(Service service, Object instance, ExportedPostProcessor processor) {
            this.service = service;
            this.instance = instance;
            this.processor = processor;
        }

        @Override
        public void run() {
            try {
                processor.afterExported(this.service, this.instance);
            } catch (Throwable e) {
                LOGGER.warn(processor.getClass().getName() + ":" + e.getMessage());
            }
        }

    }
}
