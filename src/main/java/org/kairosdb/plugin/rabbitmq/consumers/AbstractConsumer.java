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

package org.kairosdb.plugin.rabbitmq.consumers;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class AbstractConsumer.
 */
abstract class AbstractConsumer
        implements Consumer {

    /**
     * The Constant LOGGER.
     */
    public static final Logger LOGGER = LoggerFactory.getLogger(AbstractConsumer.class);

    /**
     * The value.
     */
    private String value = "";

    /**
     * The timestamp.
     */
    private Long timestamp = null;

    /**
     * The tags.
     */
    private Map<String, String> tags = new HashMap<String, String>();

    /**
     * Consume.
     * 
     * @param message the message
     * @return the boolean
     */
    public Boolean consume(String message) {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kairosdb.plugin.rabbitmq.consumers.Consumer#getValue()
     */
    public String getValue() {
        return value;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kairosdb.plugin.rabbitmq.consumers.Consumer#setValue(java.lang.String)
     */
    public void setValue(String value) {
        this.value = value;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kairosdb.plugin.rabbitmq.consumers.Consumer#getTimestamp()
     */
    public Long getTimestamp() {
        return timestamp;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kairosdb.plugin.rabbitmq.consumers.Consumer#setTimestamp(java.lang.Long)
     */
    public void setTimestamp(Long timestamp) {
        // Copied from KairosDB source:
        // Backwards compatible hack for the next 30 years
        // This allows clients to send seconds to us
        if (timestamp < 3000000000L)
            timestamp *= 1000;
        this.timestamp = timestamp;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kairosdb.plugin.rabbitmq.consumers.Consumer#getTags()
     */
    public Map<String, String> getTags() {
        return tags;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kairosdb.plugin.rabbitmq.consumers.Consumer#addTag(java.lang.String,
     * java.lang.String)
     */
    public void addTag(String key, String value) {
        tags.put(key, value);
    }

}
