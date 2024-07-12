package code;

// Copyright (c) 2024, NoCodeNoLife-cloud. All rights reserved.
// Author: NoCodeNoLife-cloud
// stay hungry，stay foolish

/**
 * This interface provides methods for custom serialization and deserialization.
 * Implement this interface to provide your own serialization/deserialization logic.
 *
 * @author admin
 */
public interface CustomizedSerializer {
	/**
	 * Method to deserialize bytes into an object of a given class
	 *
	 * @param clazz clazz
	 * @param bytes bytes
	 * @param <T>   T
	 *
	 * @return T
	 */
	<T> T deserialize(Class<T> clazz, byte[] bytes);

	/**
	 * Method to serialize an object into bytes
	 *
	 * @param object object
	 * @param <T>    T
	 *
	 * @return data
	 */
	<T> byte[] serialize(T object);
}