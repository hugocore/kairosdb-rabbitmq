/**
 * 
 */
package org.kairosdb.plugin.rabbitmq.consumers;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class JSONConsumer.
 * 
 * @author Hugo Sequeira
 */
public class JSONConsumer extends AbstractConsumer {

	/**
	 * The Constant LOGGER.
	 */
	public static final Logger LOGGER = LoggerFactory
			.getLogger(JSONConsumer.class);

	/**
	 * The JSON field for value.
	 */
	private String fieldValue = "";

	/**
	 * The JSON field for timestamp.
	 */
	private String fieldTimestamp = "";

	/**
	 * The JSON field for tags (if available).
	 */
	private String fieldTags = "";

	/**
	 * Instantiates a new JSON consumer.
	 * 
	 * @param fieldValue
	 *            the field value
	 * @param fieldTimestamp
	 *            the field timestamp
	 * @param fieldTags
	 *            the field tags
	 */
	public JSONConsumer(String fieldValue, String fieldTimestamp,
			String fieldTags) {
		this.fieldValue = fieldValue;
		this.fieldTimestamp = fieldTimestamp;
		this.fieldTags = fieldTags;
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

			// Create message
			JSONObject jsonobj = new JSONObject(message);
			Boolean hasTags = false;

			if (jsonobj.has(fieldValue)) {

				if (jsonobj.has(fieldTimestamp)) {
					
					// Gets value and timestamp
					setValue(jsonobj.get(fieldValue).toString());
					setTimestamp(jsonobj.getLong(fieldTimestamp));

					String tagname = "";
					String tagvalue = "";
					String[] elements = null;

					// Check if message has tag JSONArray property
					if (jsonobj.has(fieldTags)) {

						JSONArray tags = jsonobj.getJSONArray(fieldTags);
						for (int i = 0; i < tags.length(); i++) {

							JSONObject tag = tags.getJSONObject(i);

							elements = JSONObject.getNames(tag);
							for (String property : elements) {
								tagname = property;
								tagvalue = tag.getString(tagname);
								addTag(tagname, tagvalue);
								hasTags = true;
							}

						}

					} else {

						// Otherwise use the rest of properties of the has tags
						elements = JSONObject.getNames(jsonobj);
						for (String property : elements) {
							tagname = property;
							tagvalue = jsonobj.getString(tagname);

							// Ignore value and time stamp properties
							if (!tagname.equals(fieldValue)
									&& !tagname.equals(fieldTimestamp)) {
								addTag(tagname, tagvalue);
								hasTags = true;
							}

						}

					}

					// Insert fake tag if no tag was provided.
					// KairosDB demands at least one tag for every push.
					if (!hasTags)
						addTag("add", "tag");

					// Success
					return true;

				} else {
					throw new IllegalArgumentException(
							"Message does not have the '" + fieldTimestamp
									+ "' property.");
				}

			} else {
				throw new IllegalArgumentException(
						"Message does not have the '" + fieldValue
								+ "' property.");
			}

		} catch (JSONException e) {
			LOGGER.error(
					"[KRMQ] The consumed message is not a valid JSON object: ",
					e);
		} catch (IllegalArgumentException e) {
			LOGGER.error(
					"[KRMQ] An error occurred while consuming this JSON message: ",
					e);
		}

		return false;

	}

}
