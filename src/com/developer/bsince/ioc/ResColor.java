package com.developer.bsince.ioc;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import android.support.annotation.ColorRes;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * Bind a field to the specified color resource ID. Type can be {@code int} or
 *
 */
@Retention(CLASS) @Target(FIELD)
public @interface ResColor {
  /** Color resource ID to which the field will be bound. */
	@ColorRes
  int value();
}
