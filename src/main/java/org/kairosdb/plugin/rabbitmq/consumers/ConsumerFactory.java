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

import org.kairosdb.plugin.rabbitmq.exceptions.InvalidContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory responsible to return the proper type of consumer based on the ContentType of
 * each message.
 * 
 * @author Hugo Sequeira
 */
public class ConsumerFactory {

    /**
     * The Constant LOGGER.
     */
    public static final Logger LOGGER = LoggerFactory.getLogger(ConsumerFactory.class);

    /**
     * The conc consumer.
     */
    private AbstractConsumer concConsumer = null;

    /**
     * The field value.
     */
    private String fieldValue = "";

    /**
     * The field timestamp.
     */
    private String fieldTimestamp = "";

    /**
     * The field tags.
     */
    private String fieldTags = "";

    /**
     * The seperator.
     */
    private String seperator = ",";

    /**
     * Instantiates a new consumer factory.
     * 
     * @param fieldValue the field value
     * @param fieldTimestamp the field timestamp
     * @param fieldTags the field tags
     * @param seperator the seperator
     */
    public ConsumerFactory(String fieldValue,
                           String fieldTimestamp,
                           String fieldTags,
                           String seperator) {
        this.fieldValue = fieldValue;
        this.fieldTimestamp = fieldTimestamp;
        this.fieldTags = fieldTags;
        this.seperator = seperator;
    }

    /**
     * Returns a message consumer based on the contentType of the message.
     * 
     * @param contentType can contain JSON, CSV, TEXT, TXT, OCTET, STREAM, SPRING
     * @param defaultContentType the default content type
     * @return concrete consumer or null if no implementation exists
     * @throws InvalidContentType the invalid content type
     */
    public Consumer getConsumer(String contentType, String defaultContentType) throws InvalidContentType {

        // Use default content type if message does not provide it
        if (contentType == null || contentType.isEmpty() && !defaultContentType.isEmpty())
            contentType = defaultContentType;

        String type = contentType.toLowerCase();

        if (type.contains("json")) {
            concConsumer = new JSONConsumer(fieldValue, fieldTimestamp, fieldTags);

        } else if (type.contains("csv") || type.contains("text") || type.contains("txt")) {
            concConsumer = new CSVConsumer(seperator);

        } else if (type.contains("spring") || type.contains("octet") || type.contains("stream")) {
            concConsumer = new SpringJSONConsumer(fieldValue, fieldTimestamp, fieldTags);
        } else {
            throw new InvalidContentType(contentType);
        }

        return concConsumer;
    }

}
