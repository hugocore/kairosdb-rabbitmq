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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class CSVConsumer.
 * 
 * @author Hugo Sequeira
 */
public class CSVConsumer extends
        AbstractConsumer {

    /**
     * The Constant LOGGER.
     */
    public static final Logger LOGGER = LoggerFactory.getLogger(CSVConsumer.class);

    /**
     * The seperator.
     */
    private String seperator = ",";

    /**
     * Instantiates a new CSV consumer.
     * 
     * @param seperator the separator
     */
    public CSVConsumer(String seperator) {
        this.seperator = seperator;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kairosdb.plugin.rabbitmq.consumers.Consumer#consume(byte[])
     */
    @Override
    public Boolean consume(byte[] msg) {

        try {

            // Cast the message to string
            String message = new String(msg);

            // Crop message for one line
            if (message.contains("\n"))
                message = message.substring(message.indexOf("\n"));

            // Split message by the separator but
            // ignore separators surrounded by quotes
            String[] messageList = message.split(seperator + "(?=([^\"]*\"[^\"]*\")*[^\"]*$)");

            // Get value and timestamp
            if (messageList.length >= 2) {
                setTimestamp(Long.parseLong(messageList[0].trim()));
                setValue(messageList[1].trim());
            } else {
                throw new IllegalArgumentException(
                        "The format of the consumed text message is not valid."
                                + " A text message must begin with '<timestamp>" + seperator
                                + "<value>'." + " Check the documentation for more information.");
            }

            // Get tags
            if (messageList.length > 2) {

                if (messageList.length % 2 != 0) {
                    throw new IllegalArgumentException(
                            "The consumed text message is incomplete or invalid."
                                    + " The number of elements in a CSV/TEXT message must be even.");
                }

                // Save each pair of messages
                for (int i = 2; i + 1 <= messageList.length; i = i + 2) {
                    String tagname = messageList[i];
                    String tagvalue = messageList[i + 1];
                    addTag(tagname, tagvalue);
                }

            } else {
                // Insert fake tag if no tag was provided in the
                // message.
                // KairosDB demands at least one tag for every push.
                addTag("add", "tag");
            }

            // Success
            return true;

        } catch (IllegalArgumentException e) {
            LOGGER.error("[KRMQ] An error occurred while consuming this CSV/TEXT message: ", e);
        }

        return false;

    }
}
