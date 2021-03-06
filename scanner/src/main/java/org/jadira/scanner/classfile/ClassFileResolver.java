/*
 *  Copyright 2012 Chris Pheby
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jadira.scanner.classfile;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import javassist.bytecode.ClassFile;
import jsr166y.ForkJoinPool;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.jadira.scanner.classfile.filter.ClassFileFilter;
import org.jadira.scanner.classfile.filter.NameFilter;
import org.jadira.scanner.classfile.filter.PackageFileFilter;
import org.jadira.scanner.classpath.projector.ClasspathProjector;
import org.jadira.scanner.core.api.Allocator;
import org.jadira.scanner.core.api.Projector;
import org.jadira.scanner.core.helper.JavassistClassFileHelper;
import org.jadira.scanner.core.spi.AbstractFileResolver;
import org.jadira.scanner.core.utils.reflection.ClassLoaderUtils;
import org.jadira.scanner.file.locator.JdkBaseClasspathUrlLocator;

import de.schlichtherle.truezip.file.TFileInputStream;

public class ClassFileResolver extends AbstractFileResolver<ClassFile> {

	private static final Projector<File> CLASSPATH_PROJECTOR = new ClasspathProjector();
	
	public static ForkJoinPool FORKJOIN_TASK = new ForkJoinPool();

    private static final List<URL> JDK_BASE_CLASSPATH_JARS = new JdkBaseClasspathUrlLocator().locate();

	private final ClassFileAssigner assigner = new ClassFileAssigner();

	private ClassLoader classLoader = ClassLoaderUtils.getClassLoader();
	
    public ClassFileResolver() {    	
        super(JDK_BASE_CLASSPATH_JARS);
	}
    
    public ClassFileResolver(ClassLoader classLoader) {    	
        super(JDK_BASE_CLASSPATH_JARS);
        this.classLoader = classLoader;
	}

	public ClassFileResolver(List<URL> classpaths) {
		super(JDK_BASE_CLASSPATH_JARS);
		getDriverData().addAll(classpaths);
	}
	
	public ClassFileResolver(List<URL> classpaths, ClassLoader classLoader) {
		super(JDK_BASE_CLASSPATH_JARS);
		getDriverData().addAll(classpaths);
		this.classLoader  = classLoader;
	}

	@Override
	public String toString() {

		ToStringBuilder builder = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
		builder.append(getDriverData().toArray());

		return builder.toString();
	}

	@Override
	protected Allocator<ClassFile, File> getAssigner() {
		return assigner;
	}

	private final class ClassFileAssigner implements Allocator<ClassFile, File> {

		@Override
		public ClassFile allocate(File e) {
			
			TFileInputStream tis = null;
			
			try {
				tis = new TFileInputStream(e);
				return JavassistClassFileHelper.constructClassFileForPath(e.getPath(), tis);
			} catch (FileNotFoundException e1) {
				throw new IllegalArgumentException(e + " is not a valid File", e1);
			} catch (IOException e1) {
				throw new IllegalArgumentException("Could not load ClassFile for " + e, e1);
			} finally {
				if (tis != null) {
					try {
						tis.close();
					} catch (IOException e1) {
						// Ignore
					}
				}
			}
		}
	}
	
	public ClassFile resolveClassFile(String name) {
		
		ClassFile cf = null;
		
		if (classLoader != null) {
			
			String className = name.replace('.', '/').concat(".class");
			InputStream is = classLoader.getResourceAsStream(className);
			BufferedInputStream fin = new BufferedInputStream(is);
			
			try {
				cf = new ClassFile(new DataInputStream(fin));
				if (cf != null) {
					return cf;
				}
			} catch (IOException e) {
				// Ignore
			}
		}
			
		cf = resolveFirst(null, CLASSPATH_PROJECTOR, new PackageFileFilter(name, true), new NameFilter(name));
		if (cf == null) {
			cf = resolveFirst(null, CLASSPATH_PROJECTOR, new ClassFileFilter(name));
		}
		return cf;
	}
}