package com.kepler.com.caucho.hessian.io;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * @author KimShen
 *
 */
public class LocalDateTimeSerializer extends AbstractSerializer {

	@Override
	public void writeObject(Object obj, AbstractHessianOutput out) throws IOException {
		if (obj == null) {
			out.writeNull();
		} else {
			Class<?> cl = obj.getClass();
			if (out.addRef(obj)) {
				return;
			}
			int ref = out.writeObjectBegin(cl.getName());
			if (ref < -1) {
				out.writeString("value");
				Long milliSecond = ((LocalDateTime) obj).toInstant(ZoneOffset.of("+8")).toEpochMilli();
				out.writeUTCDate(milliSecond);
				out.writeMapEnd();
			} else {
				if (ref == -1) {
					out.writeInt(1);
					out.writeString("value");
					out.writeObjectBegin(cl.getName());
				}
				Long milliSecond = ((LocalDateTime) obj).toInstant(ZoneOffset.of("+8")).toEpochMilli();
				out.writeUTCDate(milliSecond);
			}
		}
	}
}
