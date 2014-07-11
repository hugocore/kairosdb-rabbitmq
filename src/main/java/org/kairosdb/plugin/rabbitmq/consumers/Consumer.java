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

import java.util.Map;

/**
 * The Interface for a KairosDB-RabbitMQ Consumer.
 */
public interface Consumer {

    /**
     * Consume.
     * 
     * @param msg the msg
     * @return the boolean
     */
    Boolean consume(byte[] msg);

    /**
     * Gets the value.
     * 
     * @return the value
     */
    String getValue();

    /**
     * Sets the value.
     * 
     * @param value the new value
     */
    void setValue(String value);

    /**
     * Gets the timestamp.
     * 
     * @return the timestamp
     */
    Long getTimestamp();

    /**
     * Sets the timestamp.
     * 
     * @param timestamp the new timestamp
     */
    void setTimestamp(Long timestamp);

    /**
     * Gets the tags.
     * 
     * @return the tags
     */
    Map<String, String> getTags();

    /**
     * Adds the tag.
     * 
     * @param key the key
     * @param value the value
     */
    void addTag(String key, String value);

}
