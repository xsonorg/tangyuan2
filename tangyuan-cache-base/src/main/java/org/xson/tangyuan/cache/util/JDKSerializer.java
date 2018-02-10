package org.xson.tangyuan.cache.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.xson.tangyuan.cache.CacheSerializer;

public class JDKSerializer implements CacheSerializer {

	public static JDKSerializer instance = new JDKSerializer();

	@Override
	public Object serialize(Object object) throws Throwable {
		if (object == null) {
			return null;
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
		try {
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(object);
			oos.flush();
			byte[] result = baos.toByteArray();
			oos.close();
			return result;
		} catch (IOException ex) {
			throw new IllegalArgumentException("Failed to serialize object of type: " + object.getClass(), ex);
		}

	}

	@Override
	public Object deserialize(Object object) throws Throwable {
		if (object == null) {
			return null;
		}
		byte[] buf = (byte[]) object;
		try {
			ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(buf));
			Object result = ois.readObject();
			ois.close();
			return result;
		} catch (IOException ex) {
			throw new IllegalArgumentException("Failed to deserialize object", ex);
		}
	}

}
