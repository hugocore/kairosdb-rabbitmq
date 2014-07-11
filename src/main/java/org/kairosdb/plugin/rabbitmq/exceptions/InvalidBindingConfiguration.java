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

/**
 * Exception for InvalidBindingConfiguration.
 * 
 * @author Hugo Sequeira
 */
public class InvalidBindingConfiguration extends
        Exception {

    /**
     * The Constant serialVersionUID.
     */
    private static final long serialVersionUID = 6839219933437811528L;

    /**
     * Instantiates a new invalid binding configuration exception.
     * 
     * @param message the message
     */
    public InvalidBindingConfiguration(String message) {
        super(message);
    }

    /**
     * Instantiates a new invalid binding configuration exception.
     * 
     * @param cause the cause
     */
    public InvalidBindingConfiguration(Throwable cause) {
        super(cause);
    }

    /**
     * Instantiates a new invalid binding configuration exception.
     * 
     * @param message the message
     * @param cause the cause
     */
    public InvalidBindingConfiguration(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Instantiates a new invalid binding configuration exception.
     * 
     * @param message the message
     * @param cause the cause
     * @param enableSuppression the enable suppression
     * @param writableStackTrace the writable stack trace
     */
    public InvalidBindingConfiguration(String message,
                                       Throwable cause,
                                       boolean enableSuppression,
                                       boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
