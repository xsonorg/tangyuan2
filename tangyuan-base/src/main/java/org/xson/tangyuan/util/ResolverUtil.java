package org.xson.tangyuan.util;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

public class ResolverUtil<T> {
	/*
	 * An instance of Log to use for logging in this class.
	 */
	// private static XSONLogger log = XSONLogger.getLogger(ResolverUtil.class);
	// private static final Log log = LogFactory.getLog(ResolverUtil.class);

	/**
	 * A simple interface that specifies how to test classes to determine if they are to be included in the results produced by the ResolverUtil.
	 */
	public static interface Test {
		/**
		 * Will be called repeatedly with candidate classes. Must return True if a class is to be included in the results, false otherwise.
		 */
		boolean matches(Class<?> type);
	}

	/**
	 * A Test that checks to see if each class is assignable to the provided class. Note that this test will match the parent type itself if it is
	 * presented for matching.
	 */
	public static class IsA implements Test {
		private Class<?> parent;

		/** Constructs an IsA test using the supplied Class as the parent class/interface. */
		public IsA(Class<?> parentType) {
			this.parent = parentType;
		}

		/** Returns true if type is assignable to the parent type supplied in the constructor. */
		public boolean matches(Class<?> type) {
			return type != null && parent.isAssignableFrom(type);
		}

		@Override
		public String toString() {
			return "is assignable to " + parent.getSimpleName();
		}
	}

	/**
	 * A Test that checks to see if each class is annotated with a specific annotation. If it is, then the test returns true, otherwise false.
	 */
	public static class AnnotatedWith implements Test {
		private Class<? extends Annotation> annotation;

		/** Constructs an AnnotatedWith test for the specified annotation type. */
		public AnnotatedWith(Class<? extends Annotation> annotation) {
			this.annotation = annotation;
		}

		/** Returns true if the type is annotated with the class provided to the constructor. */
		public boolean matches(Class<?> type) {
			return type != null && type.isAnnotationPresent(annotation);
		}

		@Override
		public String toString() {
			return "annotated with @" + annotation.getSimpleName();
		}
	}

	/** The set of matches being accumulated. */
	private Set<Class<? extends T>>	matches	= new HashSet<Class<? extends T>>();

	/**
	 * The ClassLoader to use when looking for classes. If null then the ClassLoader returned by Thread.currentThread().getContextClassLoader() will
	 * be used.
	 */
	private ClassLoader				classloader;

	/**
	 * Provides access to the classes discovered so far. If no calls have been made to any of the {@code find()} methods, this set will be empty.
	 * 
	 * @return the set of classes that have been discovered.
	 */
	public Set<Class<? extends T>> getClasses() {
		return matches;
	}

	/**
	 * Returns the classloader that will be used for scanning for classes. If no explicit ClassLoader has been set by the calling, the context class
	 * loader will be used.
	 * 
	 * @return the ClassLoader that will be used to scan for classes
	 */
	public ClassLoader getClassLoader() {
		return classloader == null ? Thread.currentThread().getContextClassLoader() : classloader;
	}

	/**
	 * Sets an explicit ClassLoader that should be used when scanning for classes. If none is set then the context classloader will be used.
	 * 
	 * @param classloader
	 *            a ClassLoader to use when scanning for classes
	 */
	public void setClassLoader(ClassLoader classloader) {
		this.classloader = classloader;
	}

	/**
	 * Attempts to discover classes that are assignable to the type provided. In the case that an interface is provided this method will collect
	 * implementations. In the case of a non-interface class, subclasses will be collected. Accumulated classes can be accessed by calling
	 * {@link #getClasses()}.
	 * 
	 * @param parent
	 *            the class of interface to find subclasses or implementations of
	 * @param packageNames
	 *            one or more package names to scan (including subpackages) for classes
	 */
	public ResolverUtil<T> findImplementations(Class<?> parent, String... packageNames) {
		if (packageNames == null)
			return this;

		Test test = new IsA(parent);
		for (String pkg : packageNames) {
			find(test, pkg);
		}

		return this;
	}

	/**
	 * Attempts to discover classes that are annotated with the annotation. Accumulated classes can be accessed by calling {@link #getClasses()}.
	 * 
	 * @param annotation
	 *            the annotation that should be present on matching classes
	 * @param packageNames
	 *            one or more package names to scan (including subpackages) for classes
	 */
	public ResolverUtil<T> findAnnotated(Class<? extends Annotation> annotation, String... packageNames) {
		if (packageNames == null)
			return this;

		Test test = new AnnotatedWith(annotation);
		for (String pkg : packageNames) {
			find(test, pkg);
		}

		return this;
	}

	/**
	 * Scans for classes starting at the package provided and descending into subpackages. Each class is offered up to the Test as it is discovered,
	 * and if the Test returns true the class is retained. Accumulated classes can be fetched by calling {@link #getClasses()}.
	 * 
	 * @param test
	 *            an instance of {@link Test} that will be used to filter classes
	 * @param packageName
	 *            the name of the package from which to start scanning for classes, e.g. {@code net.sourceforge.stripes}
	 */
	public ResolverUtil<T> find(Test test, String packageName) {
		// String path = getPackagePath(packageName);
		//
		// try {
		// List<String> children = VFS.getInstance().list(path);
		// for (String child : children) {
		// if (child.endsWith(".class"))
		// addIfMatching(test, child);
		// }
		// } catch (IOException ioe) {
		// log.error("Could not read package: " + packageName, ioe);
		// }

		return this;
	}

	/**
	 * Converts a Java package name to a path that can be looked up with a call to {@link ClassLoader#getResources(String)}.
	 * 
	 * @param packageName
	 *            The Java package name to convert to a path
	 */
	protected String getPackagePath(String packageName) {
		return packageName == null ? null : packageName.replace('.', '/');
	}

	/**
	 * Add the class designated by the fully qualified class name provided to the set of resolved classes if and only if it is approved by the Test
	 * supplied.
	 * 
	 * @param test
	 *            the test used to determine if the class matches
	 * @param fqn
	 *            the fully qualified name of a class
	 */
	@SuppressWarnings("unchecked")
	protected void addIfMatching(Test test, String fqn) {
		try {
			String externalName = fqn.substring(0, fqn.indexOf('.')).replace('/', '.');
			ClassLoader loader = getClassLoader();
			// log.debug("Checking to see if class " + externalName + " matches criteria [" + test + "]");

			Class<?> type = loader.loadClass(externalName);
			if (test.matches(type)) {
				matches.add((Class<T>) type);
			}
		} catch (Throwable t) {
			// log.warn("Could not examine class '" + fqn + "'" + " due to a " + t.getClass().getName() +
			// " with message: "
			// + t.getMessage());
		}
	}
}