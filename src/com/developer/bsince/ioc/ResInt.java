package com.developer.bsince.ioc;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import android.support.annotation.IntegerRes;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * Bind a field to the specified integer resource ID.
 * <pre><code>
 * {@literal @}ResourceInt(R.int.columns) int columns;
 * </code></pre>
 */
@Retention(CLASS) @Target(FIELD)
public @interface ResInt {
  /** Integer resource ID to which the field will be bound. */
	@IntegerRes
  int value();
}
