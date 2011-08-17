//Copyright (C) 2010  Novabit Informationssysteme GmbH
//
//This file is part of Nuclos.
//
//Nuclos is free software: you can redistribute it and/or modify
//it under the terms of the GNU Affero General Public License as published by
//the Free Software Foundation, either version 3 of the License, or
//(at your option) any later version.
//
//Nuclos is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU Affero General Public License for more details.
//
//You should have received a copy of the GNU Affero General Public License
//along with Nuclos.  If not, see <http://www.gnu.org/licenses/>.

package org.nuclos.server.customcode.codegenerator;

import static org.nuclos.common.collection.Factories.memoizingFactory;
import static org.nuclos.common.collection.Factories.synchronizingFactory;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.log4j.Logger;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.SpringApplicationContextHolder;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Factory;
import org.nuclos.server.common.NuclosSystemParameters;
import org.nuclos.server.common.RuleCache;
import org.nuclos.server.customcode.NuclosRule;
import org.nuclos.server.customcode.NuclosTimelimitRule;
import org.nuclos.server.customcode.codegenerator.CodeGenerator.JavaSourceAsString;
import org.nuclos.server.customcode.codegenerator.RuleCodeGenerator.RuleSourceAsString;
import org.nuclos.server.customcode.valueobject.CodeVO;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.ruleengine.NuclosCompileException;
import org.nuclos.server.ruleengine.NuclosCompileException.ErrorMessage;
import org.nuclos.server.ruleengine.ejb3.RuleEngineFacadeBean;
import org.nuclos.server.ruleengine.ejb3.TimelimitRuleFacadeBean;
import org.nuclos.server.ruleengine.valueobject.RuleVO;
import org.springframework.core.io.Resource;

public class NuclosJavaCompiler implements Closeable {

	private static final Logger log = Logger.getLogger(NuclosJavaCompiler.class);

	private static final String JAVAC_CLASSNAME = "com.sun.tools.javac.api.JavacTool";

	public static final String ENCODING = "UTF-8";

	public static final File JARFILE = new File(NuclosSystemParameters.getDirectory(NuclosSystemParameters.GENERATOR_OUTPUT_PATH), "Nuclet.jar");

	private static Attributes.Name NUCLOS_CODE_NUCLET = new Attributes.Name("Nuclos-Code-Nuclet");
	private static Attributes.Name NUCLOS_CODE_HASH = new Attributes.Name("Nuclos-Code-Hash");

	public static JavaCompiler getJavaCompilerTool() {
		JavaCompiler tool = ToolProvider.getSystemJavaCompiler();
		if (tool != null)
			return tool;

		// No system Java compiler found, try to locate Javac ourself
		// (maybe we found a "bundled" Javac class on our classpath).
		try {
			Class<?> clazz = Class.forName(JAVAC_CLASSNAME);
			try {
				return (JavaCompiler) clazz.newInstance();
			} catch (Exception ex) {
				log.error(ex);
			}
		} catch(ClassNotFoundException e) {
		}
		return null;
	}

	/** the output path where generated java and class files are stored */
	public static final File getOutputPath() {
		File dir = NuclosSystemParameters.getDirectory(NuclosSystemParameters.GENERATOR_OUTPUT_PATH);
		if (!dir.exists())
			dir.mkdirs();
		return dir;
	}

	public static final File getSourceOutputPath() {
		File dir = new File(getOutputPath(), "src");
		if (!dir.exists())
			dir.mkdirs();
		return dir;
	}

	public static final File getBuildOutputPath() {
		File dir = new File(getOutputPath(), "build");
		if (!dir.exists())
			dir.mkdirs();
		return dir;
	}

	private final JavaCompiler javac;
	private final Locale locale;
	private final CodeGeneratorDiagnosticListener diagnosticListener;
	private final StandardJavaFileManager stdFileManager;

	private NuclosJavaCompiler() {
		this(null);
	}

	private NuclosJavaCompiler(Locale locale) {
		// We use Java 6's compiler API...
		javac = getJavaCompilerTool();
		if (javac == null) {
			throw new NuclosFatalException("No registered system Java compiler found");
		}
		this.locale = locale;
		this.diagnosticListener = new CodeGeneratorDiagnosticListener(locale);
		this.stdFileManager = javac.getStandardFileManager(diagnosticListener, locale, null);
		init();
	}

	private void init() {
		try {
			List<File> classpath = new ArrayList<File>();
			classpath.addAll(getExpandedSystemParameterClassPath());
			classpath.addAll(getLibs(NuclosSystemParameters.getDirectory(NuclosSystemParameters.WSDL_GENERATOR_LIB_PATH)));
			stdFileManager.setLocation(StandardLocation.CLASS_PATH, new ArrayList<File>(classpath));
			stdFileManager.setLocation(StandardLocation.SOURCE_OUTPUT, Collections.singleton(getSourceOutputPath()));
			stdFileManager.setLocation(StandardLocation.CLASS_OUTPUT, Collections.singleton(getBuildOutputPath()));
		} catch(IOException e) {
			throw new NuclosFatalException(e);
		}
	}

	private synchronized Map<String, byte[]> javac(List<CodeGenerator> generators, boolean save) throws NuclosCompileException {
		log.info("Compiler Classpath: " + stdFileManager.getLocation(StandardLocation.CLASS_PATH));
		final Set<JavaFileObject> sources = new HashSet<JavaFileObject>();
		for (CodeGenerator generator : generators) {
			for (JavaFileObject jfo : generator.getSourceFiles()) {
				if (!sources.add(jfo)) {
					throw new NuclosCompileException("nuclos.compiler.duplicateclasses");
				}
			}
		}
		log.info("Execute Java compiler for source files: " + sources);

		if (save) {
			try {
				saveSrc(generators);
			}
			catch(IOException e1) {
				// Source is saved to disk just for debugging purposes
				log.warn(e1);
			}
		}

		if (sources.size() > 0) {
			List<String> options = Arrays.asList("-g");

			ByteArrayOutputFileManager byteFileManager = new ByteArrayOutputFileManager(stdFileManager);
			CompilationTask task = javac.getTask(null, byteFileManager, diagnosticListener, options, null, sources);

			task.setLocale(locale);

			boolean success = task.call();

			List<ErrorMessage> errors = diagnosticListener.clearErrors();
			if (!success) {
				log.info(errors);
				throw new NuclosCompileException(errors);
			}

			Map<String, byte[]> output;
			try {
				output = byteFileManager.getOutput();
				byteFileManager.flush();
			}
			catch(IOException e) {
				throw new NuclosCompileException(e);
			}
			return output;
		}
		else {
			return new HashMap<String, byte[]>();
		}
	}

	private synchronized void jar(Map<String, byte[]> javacresult, List<CodeGenerator> generators) {
		try {
			if (JARFILE.exists()) {
				JARFILE.delete();
			}
			if (javacresult.size() > 0) {
				JarOutputStream jos = new JarOutputStream(new FileOutputStream(JARFILE), getManifest());

				try {
					for(Map.Entry<String, byte[]> e : javacresult.entrySet()) {
						byte[] bytecode = e.getValue();
						for(CodeGenerator generator : generators) {
							for(JavaSourceAsString src : generator.getSourceFiles()) {
								String name = src.getFQName();

								if (e.getKey().startsWith(name.replaceAll("\\.", "/"))) {
									bytecode = generator.postCompile(e.getKey(), e.getValue());
								}
							}
						}
						jos.putNextEntry(new ZipEntry(e.getKey()));
						jos.write(bytecode);
						jos.closeEntry();
					}
				}
				finally {
					jos.close();
				}
			}
		}
		catch(IOException ex) {
			throw new NuclosFatalException(ex);
		}
	}

	private File[] saveSrc(List<CodeGenerator> generators) throws IOException {
		List<File> result = new ArrayList<File>();
		for (CodeGenerator generator : generators) {
			for (JavaSourceAsString srcobject : generator.getSourceFiles()) {
				File f = getFile(srcobject);
				if (!f.exists()) {
					f.getParentFile().mkdirs();
					f.createNewFile();
				}
				result.add(f);
				Writer out = new OutputStreamWriter(new FileOutputStream(f), ENCODING);
			    try {
			    	out.write(srcobject.getCharContent(true).toString());
			    }
			    finally {
			    	out.close();
			    }
			}
		}
		return result.toArray(new File[result.size()]);
	}

	private File getFile(JavaSourceAsString srcobject) {
		return new File(CollectionUtils.getFirst(stdFileManager.getLocation(StandardLocation.SOURCE_OUTPUT)), srcobject.getPath());
	}

	public static void compile() throws NuclosCompileException {
		NuclosJavaCompiler c = new NuclosJavaCompiler();
		List<CodeGenerator> generators = getAllArtifacts();
		try {
			c.jar(c.javac(generators, true), generators);
		}
		finally {
			try {
				c.close();
			}
			catch(IOException e) {
				log.warn(e.getMessage(), e);
			}
		}
	}

	public static void check(CodeGenerator generator, boolean remove) throws NuclosCompileException {
		List<CodeGenerator> artifacts = getAllArtifacts();
		int index = artifacts.indexOf(generator);
		if (index > -1) {
			if (remove) {
				artifacts.remove(index);
			}
			else {
				artifacts.set(index, generator);
			}
		}
		else {
			artifacts.add(generator);
		}

		NuclosJavaCompiler c = new NuclosJavaCompiler();
		try {
			c.javac(artifacts, false);
		}
		finally {
			try {
				c.close();
			}
			catch(IOException e) {
				log.warn(e.getMessage(), e);
			}
		}
	}

	/**
	 * A diagnostic listener which collects the error messages.  It recognizes {@link GeneratedJavaFileObject}s
	 * and automatically adjust the line and position offsets.
	 */
	static class CodeGeneratorDiagnosticListener implements DiagnosticListener<JavaFileObject> {

		private final Locale locale;
		private final List<NuclosCompileException.ErrorMessage> errors;

		CodeGeneratorDiagnosticListener(Locale locale) {
			this.locale = locale;
			this.errors = new ArrayList<NuclosCompileException.ErrorMessage>();
		}

		@SuppressWarnings("rawtypes")
		@Override
		public synchronized void report(Diagnostic<? extends JavaFileObject> diag) {
			if (diag.getKind() == Diagnostic.Kind.ERROR) {
				JavaFileObject source = diag.getSource();
				String message = getMessageWithoutPath(diag);
				if (message == null || message.isEmpty())
					message = "Unknown error";
				long dl = 0, dp = 0; // line and position deltas


				if (source instanceof RuleSourceAsString) {
					RuleSourceAsString t = (RuleSourceAsString) source;

					long line = diag.getLineNumber();
					if (line != Diagnostic.NOPOS && source.getKind() == JavaFileObject.Kind.SOURCE) {
						if (message.startsWith(line + ":"))
							message = message.substring((line + ":").length());
						line = shift(line, t.getLineDelta());
					}
					dl = t.getLineDelta();
					dp = t.getPositionDelta();
				}

				String sourcename = source.getName(); // physical or symbolic source name
				String entityname = null;
				Long id = -1L;
				if (source instanceof JavaSourceAsString) {
					JavaSourceAsString jas = (JavaSourceAsString) source;
					sourcename = jas.getLabel();
					entityname = jas.getEntityname();
					id = jas.getId();
				}

				errors.add(new ErrorMessage(diag.getKind(), sourcename, message, entityname, id,
					shift(diag.getLineNumber(), dl), diag.getColumnNumber(),
					shift(diag.getPosition(), dp),
					shift(diag.getStartPosition(), dp),
					shift(diag.getEndPosition(), dp)));
			}
		}

		public synchronized List<NuclosCompileException.ErrorMessage> clearErrors() {
			List<NuclosCompileException.ErrorMessage> result = new ArrayList<NuclosCompileException.ErrorMessage>(errors);
			errors.clear();
			return result;
		}

		private String getMessageWithoutPath(Diagnostic<? extends JavaFileObject> diag) {
			String message = diag.getMessage(locale);
			JavaFileObject source = diag.getSource();
			if (source != null && message != null) {
				String path = source.toUri().getPath();
				if (path != null &&  message.startsWith(path + ":"))
					message = message.substring(path.length() + 1);
				String lineNumber = "" + diag.getLineNumber();
				if (message.startsWith(diag.getLineNumber() + ":"))
					message = message.substring(lineNumber.length() + 1);
				message = message.trim();
			}
			return message;
		}

		private static long shift(long pos, long delta) {
			if (pos != Diagnostic.NOPOS) {
				pos -= delta;
				if (pos < 0)
					pos = 0;
			}
			return pos;
		}
	}

	/**
	 * Returns the expanded class path for system parameter {@code nuclos.codegenerator.class.path}.
	 * Note: WSDL libraries are not included.
	 */
	public static synchronized List<File> getExpandedSystemParameterClassPath() {
		return expandedGeneratorClassPathFactory.create();
	}

	private static final Factory<List<File>> expandedGeneratorClassPathFactory =
		synchronizingFactory(memoizingFactory(new Factory<List<File>>() {
			@Override
			public List<File> create() {
				List<File> classPath = new ArrayList<File>();
				Resource r = SpringApplicationContextHolder.getApplicationContext().getResource("WEB-INF/lib/");
				try {
					classPath.addAll(getLibs(r.getFile()));
				} catch (IOException e) {
					throw new NuclosFatalException(e);
				}
				return classPath;
			}
		}));

	private static List<File> getLibs(File folder) {
		List<File> files = new ArrayList<File>();
		if(!folder.isDirectory()) {
			// just return empty list, compiler will give notice if classes are missing
			return files;
		}

		for(File file : folder.listFiles()) {
			files.add(file);
		}

		return files;
	}

	private static List<CodeGenerator> getAllArtifacts() {
		List<CodeGenerator> result = new ArrayList<CodeGenerator>();

		if (RuleCache.getInstance().getWebservices().size() > 0) {
			for (MasterDataVO ws : RuleCache.getInstance().getWebservices()) {
				result.add(new WsdlCodeGenerator(ws));
			}
		}

		if (RuleCache.getInstance().getCommonCode().size() > 0) {
			for (CodeVO code : RuleCache.getInstance().getCommonCode()) {
				if (code.isActive()) {
					result.add(new PlainCodeGenerator(code));
				}
			}
		}

		if (RuleCache.getInstance().getAllRules().size() > 0) {
			for (RuleVO rule : RuleCache.getInstance().getAllRules()) {
				if (rule.isActive()) {
					result.add(new RuleCodeGenerator<NuclosRule>(new RuleEngineFacadeBean.RuleTemplateType(), rule));
				}
			}
		}

		if (RuleCache.getInstance().getTimelimitRules().size() > 0) {
			for (RuleVO rule : RuleCache.getInstance().getTimelimitRules()) {
				if (rule.isActive()) {
					result.add(new RuleCodeGenerator<NuclosTimelimitRule>(new TimelimitRuleFacadeBean.TimelimitRuleCodeTemplate(), rule));
				}
			}
		}

		return result;
	}

	private static Manifest getManifest() {
		HashCodeBuilder builder = new HashCodeBuilder(11, 17);
		for (CodeGenerator gen : getAllArtifacts()) {
			builder.append(gen.hashCode());
		}

		Manifest manifest = new Manifest();
		Attributes mainAttributes = manifest.getMainAttributes();
		mainAttributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");
		mainAttributes.put(NUCLOS_CODE_NUCLET, "default");
		mainAttributes.put(NUCLOS_CODE_HASH, String.valueOf(builder.toHashCode()));
		return manifest;
	}

	public static boolean validate() throws NuclosCompileException {
		if (JARFILE.exists()) {
			try {
				JarFile jar = new JarFile(JARFILE);
				if (!jar.getManifest().equals(getManifest())) {
					compile();
				}
				else {
					return false;
				}
			}
			catch(IOException e) {
				compile();
			}
		}
		else {
			compile();
		}
		return true;
	}

	@Override
	public void close() throws IOException {
		if (stdFileManager != null) {
			stdFileManager.close();
		}
	}

}