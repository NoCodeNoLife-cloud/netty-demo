package code;

// Copyright (c) 2024, NoCodeNoLife-cloud. All rights reserved.
// Author: NoCodeNoLife-cloud
// stay hungry，stay foolish
import java.io.*;

/**
 * Customized serializer algorithm
 */
public enum CustomizedSerializerAlgorithm implements CustomizedSerializer {
	/**
	 * Java serialization and deserialization
	 */
	Java {
		/**
		 * Method to deserialize bytes into an object of a given class using Java serialization
		 */
		@SuppressWarnings("unchecked")
		@Override
		public <T> T deserialize(Class<T> clazz, byte[] bytes) {
			try {
				ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(bytes));
				return (T) objectInputStream.readObject();
			} catch (IOException | ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
		}

		/**
		 * Method to serialize an object into bytes using Java serialization
		 */
		@Override
		public <T> byte[] serialize(T object) {
			try {
				ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
				ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
				objectOutputStream.writeObject(object);
				return byteArrayOutputStream.toByteArray();
			} catch (IOException e) {
				throw new RuntimeException("Serialization failed", e);
			}
		}
	}
	// TODO: Add more serializer algorithms
}