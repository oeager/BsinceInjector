package com.developer.bsince.ioc;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import android.support.annotation.DimenRes;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * Bind a field to the specified dimension resource ID. Type can be {@code int} for pixel size or
 *
 */
@Retention(CLASS) @Target(FIELD)
public @interface ResDimen {
  /** Dimension resource ID to which the field will be bound. */
	@DimenRes
  int value();
}
