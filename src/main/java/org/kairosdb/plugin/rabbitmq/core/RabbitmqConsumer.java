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

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.kairosdb.core.DataPoint;
import org.kairosdb.core.DataPointSet;
import org.kairosdb.core.datapoints.DoubleDataPoint;
import org.kairosdb.core.datapoints.LongDataPoint;
import org.kairosdb.core.datastore.KairosDatastore;
import org.kairosdb.core.exception.DatastoreException;
import org.kairosdb.plugin.rabbitmq.consumers.Consumer;
import org.kairosdb.plugin.rabbitmq.consumers.ConsumerFactory;
import org.kairosdb.plugin.rabbitmq.exceptions.InvalidBindingConfiguration;
import org.kairosdb.plugin.rabbitmq.exceptions.InvalidContentType;
import org.kairosdb.util.Util;
import org.kairosdb.util.ValidationException;
import org.kairosdb.util.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;

/**
 * The Class RabbitmqConsumer.
 * 
 * @author Hugo Sequeira
 */
public class RabbitmqConsumer implements Runnable {

	/**
	 * The is alive.
	 */
	private volatile boolean isAlive = false;

	/**
	 * The Constant LOGGER.
	 */
	public static final Logger LOGGER = LoggerFactory
			.getLogger(RabbitmqConsumer.class);

	/**
	 * Configuration properties.
	 */
	private ConnectionFactory factory = null;

	/**
	 * The datastore.
	 */
	private KairosDatastore datastore = null;

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
	 * The default content type.
	 */
	private String defaultContentType = "";

	/**
	 * The bindingsfile.
	 */
	private String bindingsfile = "";

	/**
	 * Connection variables.
	 */
	private Connection connection = null;

	/**
	 * The channel.
	 */
	private Channel channel = null;

	/**
	 * The bindingconf.
	 */
	private JSONObject bindingconf = null;

	/**
	 * The consumer.
	 */
	private QueueingConsumer consumer = null;

	/**
	 * Instantiates a new RabbitMQ message consumer.
	 * 
	 * @param datastore
	 *            the datastore
	 * @param factory
	 *            the factory
	 * @param bindingsfile
	 *            the bindingsfile
	 * @param fieldValue
	 *            the field value
	 * @param fieldTimestamp
	 *            the field timestamp
	 * @param fieldTags
	 *            the field tags
	 * @param seperator
	 *            the seperator
	 * @param defaultContentType
	 *            the default content type
	 */
	public RabbitmqConsumer(KairosDatastore datastore,
			ConnectionFactory factory, String bindingsfile, String fieldValue,
			String fieldTimestamp, String fieldTags, String seperator,
			String defaultContentType) {
		this.datastore = datastore;
		this.factory = factory;
		this.bindingsfile = bindingsfile;
		this.fieldValue = fieldValue;
		this.fieldTimestamp = fieldTimestamp;
		this.fieldTags = fieldTags;
		this.seperator = seperator;
		this.defaultContentType = defaultContentType;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {

		try {

			// Configure consumer
			readConfigurations();

			// Reads message from queue for ever (or until be killed)
			// Continue to run even even if catch exceptions in the way
			// Run only if configured correctly
			while (isAlive) {

				QueueingConsumer.Delivery delivery = null;

				try {

					delivery = consumer.nextDelivery();
					BasicProperties props = delivery.getProperties();
					String routingKey = delivery.getEnvelope().getRoutingKey();

					// Consume message
					LOGGER.debug("[KRMQ] ");
					LOGGER.debug("[KRMQ] >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
					LOGGER.debug("[KRMQ] Consuming new message.");

					consumeMessage(routingKey, delivery.getBody(), props);

					// Acknowledges RabbitMQ server that message was processed
					// with or without a success consume
					channel.basicAck(delivery.getEnvelope().getDeliveryTag(),
							false);

				} catch (InterruptedException e) {
					throw new InterruptedException();
				} catch (IOException | ShutdownSignalException
						| ConsumerCancelledException e) {
					LOGGER.error("[KRMQ] An error occurred: ", e);
					throw new InterruptedException();
				}

				LOGGER.debug("[KRMQ] <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");

			}

		} catch (InterruptedException e) {

			isAlive = false;
			LOGGER.info("[KRMQ] Execution interrupted."
					+ " Disconnecting from RabbitMQ broker.");

			if (connection != null) {
				try {
					connection.close();
				} catch (IOException ignore) {
					// Channel closed already
				}
			}

			Thread.currentThread().interrupt();

		}

	}

	/**
	 * Consumes and save messages in KairosDB.
	 * 
	 * @param routingKey
	 *            the routing key
	 * @param body
	 *            the body
	 * @param props
	 *            the props
	 */
	private void consumeMessage(String routingKey, byte[] body,
			BasicProperties props) {

		try {

			// KairosDB Metric name = RMQ RoutingKey
			String metricName = routingKey;

			Validator.validateNotNullOrEmpty("metricName", metricName);
			Validator.validateCharacterSet("metricName", metricName);

			// Gets consumer implementation
			ConsumerFactory factory = new ConsumerFactory(fieldValue,
					fieldTimestamp, fieldTags, seperator);
			Consumer consumer = factory.getConsumer(props.getContentType(),
					defaultContentType);

			// Consumes message
			if (consumer.consume(body)) {

				String value = consumer.getValue();
				Long timestamp = consumer.getTimestamp();
				
				// Hold data point in a set
				DataPointSet dps = new DataPointSet(metricName);

				// Construct Data Point
				if (NumberUtils.isNumber(value)) {
					if (value.contains(".")) {
						dps.addDataPoint(new DoubleDataPoint(timestamp, Double.parseDouble(value)));
					} else {
						dps.addDataPoint(new LongDataPoint(timestamp, Util.parseLong(value)));
					}
				} else {
					throw new IllegalArgumentException("The value of the consumed message is not a number.");
				}

				// Get Tags
				Map<String, String> tags = consumer.getTags();
				for (Map.Entry<String, String> entry : tags.entrySet())
					dps.addTag(entry.getKey(), entry.getValue());

				// Push Data Points
				for (DataPoint dataPoint : dps.getDataPoints()) {
					datastore.putDataPoint(dps.getName(), dps.getTags(),
							dataPoint);
				}

				LOGGER.debug("[KRMQ] Message consumed:" + "\n  Metric: "
						+ metricName + "\n  Datapoints: " + dps + "\n  Tags: "
						+ tags + "\n  ContentType: " + props.getContentType()
						+ "\n  Consumer: " + consumer.getClass().getName()
						+ "\n  Message: " + new String(body, "UTF-8"));

			} else {

				LOGGER.debug("[KRMQ] Message was not consumed due to incorrect format:"
						+ "\n  RoutingKey: "
						+ routingKey
						+ "\n  Message: "
						+ new String(body, "UTF-8"));

			}

		} catch (InvalidContentType | DatastoreException | ValidationException
				| UnsupportedEncodingException e) {
			LOGGER.error("[KRMQ] An error occurred: ", e);
		}

	}

	/**
	 * Critical method. Read configurations from file and configure bindings. If
	 * any Exception occurs interrupt execution.
	 * 
	 * @throws InterruptedException
	 *             the interrupted exception
	 */
	private void readConfigurations() throws InterruptedException {

		try {

			// Channel connection with broker
			connection = factory.newConnection();
			channel = connection.createChannel();

			// Declare consumer
			consumer = new QueueingConsumer(channel);

			// Read binding configuration file and bind queues to exchanges
			configureBindings();

			isAlive = true;

		} catch (Exception e) {
			// Interrupt start if any goes wrong
			LOGGER.error("[KRMQ] An error occurred: ", e);
			throw new InterruptedException();
		}

	}

	/**
	 * Configure bindings between Queues and Exchanges.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	private void configureBindings() throws Exception {

		String path = "conf/" + bindingsfile;

		try {

			LOGGER.info("[KRMQ] Connecting to RabbitMQ broker.");

			// Read bindings configuration
			LOGGER.info("[KRMQ] Reading binding configuration from '" + path
					+ "'.");

			bindingconf = new JSONObject(FileUtils.readFileToString(new File(
					path)));

			if (bindingconf.has("bindings")) {

				JSONArray bindings = bindingconf.getJSONArray("bindings");

				for (int i = 0; i < bindings.length(); i++) {

					JSONObject binding = bindings.getJSONObject(i);

					if (binding.has("exchange") && binding.has("exchangeType")
							&& binding.has("exchangeDurable")
							&& binding.has("exchangeAutoDelete")
							&& binding.has("exchangeInternal")
							&& binding.has("binds")) {

						String exchange = binding.getString("exchange");
						String exchangeType = binding.getString("exchangeType");
						Boolean exchangeDurable = Boolean.parseBoolean(binding
								.getString("exchangeDurable"));
						Boolean exchangeAutoDelete = Boolean
								.parseBoolean(binding
										.getString("exchangeAutoDelete"));
						Boolean exchangeInternal = Boolean.parseBoolean(binding
								.getString("exchangeInternal"));

						JSONArray binds = binding.getJSONArray("binds");

						for (int j = 0; j < binds.length(); j++) {

							JSONObject bind = binds.getJSONObject(j);

							if (bind.has("queueName") && bind.has("bindingkey")) {

								String queuename = bind.getString("queueName");
								String bindingkey = bind
										.getString("bindingkey");

								LOGGER.debug("[KRMQ] Binding consumer to exchange '"
										+ exchange
										+ "' and queue '"
										+ queuename
										+ "' with bindingkey '"
										+ bindingkey + "'.");

								// Read queue properties
								Boolean durable = Boolean
										.parseBoolean(getQueueProperty(
												bindingconf, queuename,
												"queueDurable"));
								Boolean exclusive = Boolean
										.parseBoolean(getQueueProperty(
												bindingconf, queuename,
												"queueExclusive"));
								Boolean autodelete = Boolean
										.parseBoolean(getQueueProperty(
												bindingconf, queuename,
												"queueAutoDelete"));

								// Bind queue to exchange
								bindQueueToExchange(exchange, exchangeType,
										exchangeDurable, exchangeAutoDelete,
										exchangeInternal, queuename, durable,
										exclusive, autodelete, bindingkey);

								LOGGER.info("[KRMQ] Waiting for messages of exchange '"
										+ exchange
										+ "' to queue '"
										+ queuename
										+ "' with bindingkey '"
										+ bindingkey
										+ "'.");

							} else {
								throw new InvalidBindingConfiguration(
										"Binding configuration no. "
												+ i
												+ 1
												+ ","
												+ j
												+ 1
												+ " is missing either the 'queue' "
												+ " or 'topic' property.");
							}
						}

					} else {
						throw new InvalidBindingConfiguration(
								"Binding configuration no. "
										+ i
										+ 1
										+ " is missing either the 'exchange' properties"
										+ " or the 'binds' JSONArray.");
					}

				}

			} else {
				throw new InvalidBindingConfiguration(
						"Binding configuration file"
								+ " is missing the 'bindings' JSONArray.");
			}

		} catch (JSONException e) {
			throw new JSONException("Binding configuration '" + path
					+ "' is not a valid JSON file.");
		}
	}

	/**
	 * Bind RabbitMQ Queue to Exchange.
	 * 
	 * @param exchange
	 *            the exchange
	 * @param exchangeType
	 *            the exchange type
	 * @param exchangeDurable
	 *            the exchange durable
	 * @param exchangeAutoDelete
	 *            the exchange auto delete
	 * @param exchangeInternal
	 *            the exchange internal
	 * @param queuename
	 *            the queuename
	 * @param durable
	 *            the durable
	 * @param exclusive
	 *            the exclusive
	 * @param autodelete
	 *            the autodelete
	 * @param bindingkey
	 *            the bindingkey
	 * @throws Exception
	 *             the exception
	 */
	private void bindQueueToExchange(String exchange, String exchangeType,
			Boolean exchangeDurable, Boolean exchangeAutoDelete,
			Boolean exchangeInternal, String queuename, Boolean durable,
			Boolean exclusive, Boolean autodelete, String bindingkey)
			throws Exception {

		// Declarations
		channel.exchangeDeclare(exchange, exchangeType, exchangeDurable,
				exchangeAutoDelete, exchangeInternal, null);
		channel.queueDeclare(queuename, durable, exclusive, autodelete, null);

		// Binding
		channel.queueBind(queuename, exchange, bindingkey);

		// false - autoack disable
		channel.basicConsume(queuename, false, consumer);

	}

	/**
	 * Gets the queue property.
	 * 
	 * @param bindingconf
	 *            the bindingconf
	 * @param queuename
	 *            the queuename
	 * @param queueproperty
	 *            the queueproperty
	 * @return the queue property
	 * @throws Exception
	 *             the exception
	 */
	private String getQueueProperty(JSONObject bindingconf, String queuename,
			String queueproperty) throws Exception {

		if (bindingconf.has("queues")) {

			JSONArray queues = bindingconf.getJSONArray("queues");

			// find for queue configuration
			for (int i = 0; i < queues.length(); i++) {

				JSONObject queueconf = queues.getJSONObject(i);

				if (queueconf.has("queueName")) {
					if (queueconf.getString("queueName").equals(queuename)) {
						if (queueconf.has(queueproperty)) {
							return queueconf.getString(queueproperty);
						} else {
							throw new InvalidBindingConfiguration(
									"Queue configuration '" + i
											+ "' is missing '" + queueproperty
											+ "' property.");
						}
					}
				} else {
					throw new InvalidBindingConfiguration(
							"Queue configuration '" + i
									+ "' is missing 'queue' property.");
				}

			}

			// if no configuration is found report error
			throw new InvalidBindingConfiguration(
					"The configuration of queue '" + queuename
							+ "' is missing.");

		} else {
			throw new InvalidBindingConfiguration(
					"Binding configuration is missing 'queues' configurations.");
		}

	}

}
