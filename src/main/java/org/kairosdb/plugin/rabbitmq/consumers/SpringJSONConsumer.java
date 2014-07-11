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

import org.springframework.amqp.core.Message;

/**
 * The Class SpringJSONConsumer.
 * 
 * @author Hugo Sequeira
 */
public class SpringJSONConsumer extends
        JSONConsumer {

    /**
     * Instantiates a new Spring JSON consumer.
     * 
     * @param fieldValue the field value
     * @param fieldTimestamp the field timestamp
     * @param fieldTags the field tags
     */
    public SpringJSONConsumer(String fieldValue, String fieldTimestamp, String fieldTags) {
        super(fieldValue, fieldTimestamp, fieldTags);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kairosdb.plugin.rabbitmq.consumers.JSONConsumer#consume(byte[])
     */
    /**
     * Decodes consumed messaged as a Sprint Message Object and treat its body as a usual
     * JSON message.
     * 
     * @param msg the message
     */
    @Override
    public Boolean consume(byte[] msg) {
        // Decodes message first
        Message message = new Message(msg, null);
        return super.consume(message.getBody());
    }
}
