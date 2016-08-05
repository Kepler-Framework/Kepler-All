package com.kepler.generic.convert.internal;

import java.io.File;

import com.kepler.generic.convert.Convert;
import com.kepler.org.apache.commons.lang.StringUtils;

/**
 * @author KimShen
 *
 */
public class FileConvert implements Convert {

	private static final String NAME = "file";

	@Override
	public Object convert(Object source, String extension) throws Exception {
		// 存在扩展则指定目录
		return StringUtils.isEmpty(extension) ? new File(source.toString()) : new File(extension, source.toString());
	}

	@Override
	public String name() {
		return FileConvert.NAME;
	}

}
