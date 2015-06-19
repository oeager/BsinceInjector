package com.developer.bsince.ioc;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import android.support.annotation.BoolRes;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * Bind a field to the specified boolean resource ID.
 * 
 */
@Retention(CLASS)
@Target(FIELD)
public @interface ResBool {
	/** Boolean resource ID to which the field will be bound. */

	@BoolRes
	int value();
}
