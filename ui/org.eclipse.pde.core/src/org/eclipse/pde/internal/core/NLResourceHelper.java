package org.eclipse.pde.internal.core;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.util.*;

public class NLResourceHelper {
	public static final String KEY_PREFIX = "%";
	public static final String KEY_DOUBLE_PREFIX = "%%";
	private PropertyResourceBundle bundle = null;

	public NLResourceHelper(String name, URL[] locations) {
		try {
			InputStream stream = getResourceStream(name, locations);
			if (stream != null) {
				bundle = new PropertyResourceBundle(stream);
				stream.close();
			}
		} catch (IOException e) {
		}
	}
	
	public void dispose() {
		bundle = null;
	}

	private InputStream getResourceStream(String name, URL[] locations) {
		URLClassLoader resourceLoader = new URLClassLoader(locations);
		Locale locale = Locale.getDefault();
		String suffix1 = "_" + locale.getLanguage() + "_" + locale.getCountry() + "_" + locale.getVariant();
		String suffix2 = "_" + locale.getLanguage() + "_" + locale.getCountry();
		String suffix3 = "_" + locale.getLanguage();
		String suffix4 = "";

		String[] suffices = new String[] { suffix1, suffix2, suffix3, suffix4 };

		InputStream stream = null;
		for (int i = 0; i < suffices.length; i++) {
			stream =
				resourceLoader.getResourceAsStream(
					name + suffices[i] + ".properties");
			if (stream != null)
				break;
		}
		return stream;
	}

	public String getResourceString(String value) {
		String s = value.trim();

		if (!s.startsWith(KEY_PREFIX))
			return s;

		if (s.startsWith(KEY_DOUBLE_PREFIX))
			return s.substring(1);

		int ix = s.indexOf(" ");
		String key = ix == -1 ? s : s.substring(0, ix);
		String dflt = ix == -1 ? s : s.substring(ix + 1);

		if (bundle == null)
			return dflt;

		try {
			return bundle.getString(key.substring(1));
		} catch (MissingResourceException e) {
			return dflt;
		}
	}

}
