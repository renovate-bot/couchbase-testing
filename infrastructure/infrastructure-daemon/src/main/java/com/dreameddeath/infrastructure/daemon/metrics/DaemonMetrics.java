/*
 * Copyright Christophe Jeunesse
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dreameddeath.infrastructure.daemon.metrics;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jvm.*;
import com.codahale.metrics.logback.InstrumentedAppender;
import org.slf4j.LoggerFactory;

import javax.management.MBeanServer;
import java.lang.management.ManagementFactory;
import java.util.concurrent.TimeUnit;

/**
 * Created by Christophe Jeunesse on 29/11/2015.
 */
public class DaemonMetrics {
    private final MetricRegistry metricRegistry = new MetricRegistry();


    public DaemonMetrics() {
        final LoggerContext factory = (LoggerContext) LoggerFactory.getILoggerFactory();
        final Logger root = factory.getLogger(Logger.ROOT_LOGGER_NAME);

        final InstrumentedAppender metrics = new InstrumentedAppender(metricRegistry);
        metrics.setContext(root.getLoggerContext());
        metrics.start();
        root.addAppender(metrics);

        MBeanServer beanServer = ManagementFactory.getPlatformMBeanServer();
        metricRegistry.registerAll(new BufferPoolMetricSet(beanServer));
        metricRegistry.registerAll(new CachedThreadStatesGaugeSet(1, TimeUnit.MINUTES));
        metricRegistry.registerAll(new ClassLoadingGaugeSet());
        metricRegistry.register("file.descr.ratio.gauge", new FileDescriptorRatioGauge());
        metricRegistry.registerAll(new GarbageCollectorMetricSet());
        metricRegistry.registerAll(new MemoryUsageGaugeSet());

    }

    public MetricRegistry getMetricRegistry() {
        return metricRegistry;
    }
}