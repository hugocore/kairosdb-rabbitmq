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
 
package org.kairosdb.plugin.rabbitmq.exceptions;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Set;

import org.kairosdb.plugin.rabbitmq.consumers.Consumer;
import org.reflections.Reflections;

/**
 * Exception for InvalidContentType.
 * 
 * @author Hugo Sequeira
 */
public class InvalidContentType extends
        Exception {

    /**
     * The Constant serialVersionUID.
     */
    private static final long serialVersionUID = -6041156448388434113L;

    /**
     * Gets the available message consumers.
     * 
     * @return the available consumers
     */
    private static ArrayList<String> getAvailableConsumers() {

        Reflections reflections = new Reflections("org.kairosdb.plugin.rabbitmq.consumers");
        Set<Class<? extends Consumer>> consumers = reflections.getSubTypesOf(Consumer.class);
        ArrayList<String> consumerlist = new ArrayList<String>();
        for (Class<? extends Consumer> consumer : consumers) {
            if (!Modifier.isAbstract(consumer.getModifiers()))
                consumerlist.add(consumer.getName());
        }
        return consumerlist;

    }

    /**
     * Gets the invalid message.
     * 
     * @param contentType the content type
     * @return the invalid message
     */
    private static String getInvalidMessage(String contentType) {
        return "This message could not be consumed because the plugin"
                + " could not found a suitable consumer for its ContentType: '" + contentType
                + "'." + " Check the documentation for supported content types."
                + " Available consumers of messages: " + getAvailableConsumers();
    }

    /**
     * Instantiates a new invalid content type exception.
     * 
     * @param message the message
     */
    public InvalidContentType(String message) {
        super(getInvalidMessage(message));
    }

    /**
     * Instantiates a new invalid content type exception.
     * 
     * @param message the message
     * @param cause the cause
     */
    public InvalidContentType(String message, Throwable cause) {
        super(getInvalidMessage(message), cause);
    }

    /**
     * Instantiates a new invalid content type exception.
     * 
     * @param message the message
     * @param cause the cause
     * @param enableSuppression the enable suppression
     * @param writableStackTrace the writable stack trace
     */
    public InvalidContentType(String message,
                              Throwable cause,
                              boolean enableSuppression,
                              boolean writableStackTrace) {
        super(getInvalidMessage(message), cause, enableSuppression, writableStackTrace);
    }

}
