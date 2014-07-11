/*  ┌────────────────────────────────────────────────────────────────────┐
 *  │ RabbitMQToKairosDB                                                 │
 *  ├────────────────────────────────────────────────────────────────────┤
 *  │ Copyright © 2014 Hugo Sequeira (https://github.com/hugocore)       │
 *  ├────────────────────────────────────────────────────────────────────┤
 *  │ Licensed under the MIT license.                                    │
 *  ├────────────────────────────────────────────────────────────────────┤
 *  │ Plugin for KairosDB subscribe to RabbitMQ brokers.                 │
 *  └────────────────────────────────────────────────────────────────────┘
 */

package org.kairosdb.plugin.rabbitmq.core;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

/**
 * The Class RabbitmqModule.
 */
public class RabbitmqModule extends
        AbstractModule {

    /*
     * (non-Javadoc)
     * 
     * @see com.google.inject.AbstractModule#configure()
     */
    @Override
    protected void configure() {
        bind(RabbitmqService.class).in(Singleton.class);
    }

}
