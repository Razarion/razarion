package org.teavm.flavour.json;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Dummy annotation for GWT compatibility.
 * TeaVM will use the real annotation from com.frequal.flavour (Flavour fork for TeaVM 0.10+).
 * GWT will ignore this annotation but needs it on the classpath to compile.
 *
 * Note: The package name remains org.teavm.flavour.json for backward compatibility,
 * even though the Maven artifact is now under com.frequal.flavour group.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface JsonPersistable {
}
