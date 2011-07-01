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
package org.nuclos.client.main;



import java.awt.EventQueue;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.util.Locale;

import javax.jnlp.ServiceManager;
import javax.jnlp.UnavailableServiceException;
import javax.naming.NamingException;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.apache.log4j.Logger;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.xml.DOMConfigurator;
import org.nuclos.client.common.MetaDataClientProvider;
import org.nuclos.client.common.NuclosCollectableComponentFactory;
import org.nuclos.client.common.prefs.NuclosPreferencesFactory;
import org.nuclos.client.common.security.SecurityDelegate;
import org.nuclos.client.genericobject.Modules;
import org.nuclos.client.login.LoginController;
import org.nuclos.client.login.LoginEvent;
import org.nuclos.client.login.LoginListener;
import org.nuclos.client.login.ServerConfiguration;
import org.nuclos.client.synthetica.NuclosSyntheticaUtils;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.client.ui.collect.component.CollectableComponentFactory;
import org.nuclos.common.ApplicationProperties;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Controller responsible for starting up the Nucleus client.
 * This is a temporary object which will be discarded after the startup is done.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:christoph.radig@novabit.de">christoph.radig</a>
 * @version 01.00.00
 */
public class StartUp  {

	// These progress constants sum up to to 100:
	static final int PROGRESS_COMPARE_VERSIONS = 5;
	static final int PROGRESS_INIT_SECURITYCACHE = 5;
	static final int PROGRESS_INIT_NOTIFICATION = 5;
	static final int PROGRESS_READ_ATTRIBUTES = 8;
	static final int PROGRESS_READ_LOMETA = 5;
	static final int PROGRESS_READ_SEARCHFILTER = 5;
	static final int PROGRESS_CREATE_MAINFRAME = 28;
	static final int PROGRESS_INIT_ONLINEHELP = 4;
	static final int PROGRESS_CREATE_MAINMENU = 6;
	static final int PROGRESS_RESTORE_WORKSPACE = 29;

	/**
	 * log4j category
	 * @todo this shouldn't be a member probably
	 */
	private Logger log;

	private final String[] args;

	public StartUp(String[] asArgs) {
		this.args = asArgs;

		// setup client side logging:
		this.setupClientLogging();

		new ClassPathXmlApplicationContext("classpath*:META-INF/nuclos/**/*-beans.xml");

		// set the default locale:
		// this makes sure the client is independent of the host's locale.
		/** @todo i18n */
		Locale.setDefault(Locale.GERMANY);

		log.info("Java-Version " + System.getProperty("java.version"));

		// set the PreferencesFactory:
		System.setProperty("java.util.prefs.PreferencesFactory", NuclosPreferencesFactory.class.getName());

		// initialize the Errors instance:
		Errors.getInstance().setAppName(ApplicationProperties.getInstance().getName());
		// first, set the non-strict version of the critical error handler to avoid locking-out on initialization errors.
		// on successful initialization, the strict version of the critical error handler will be set.
		Errors.getInstance().setCriticalErrorHandler(new NuclosCriticalErrorHandler(false));

		// from here, we can issue error messages using the Errors class.

		// Install the CollectableComponentFactory for the application:
		CollectableComponentFactory.setInstance(newCollectableComponentFactory());

		// we create the gui completely in the event dispatch thread, as recommended by Sun:

		EventQueue.invokeLater(new Runnable() {
			@Override
            public void run() {
				createGUI();
			}
		});

	}	// ctor

	private static CollectableComponentFactory newCollectableComponentFactory() {
		try {
			final String sClassName = LangUtils.defaultIfNull(
					ApplicationProperties.getInstance().getCollectableComponentFactoryClassName(),
					NuclosCollectableComponentFactory.class.getName());

			return (CollectableComponentFactory) Class.forName(sClassName).newInstance();
		}
		catch (Exception ex) {
			throw new CommonFatalException("CollectableComponentFactory cannot be created.", ex);
		}
	}

	private void setupClientLogging() {
		LogLog.setInternalDebugging(true);
		String sLog4jUrl = System.getProperty("log4j.url");
		if (sLog4jUrl != null) {
			try {
				System.out.println("Try to configure loggging from " + sLog4jUrl + ".");
				DOMConfigurator.configure(new URL(sLog4jUrl));
				log = Logger.getLogger(this.getClass().getName());
				log.info("Logging configured from " + sLog4jUrl);
				LogLog.setInternalDebugging(true);
				return;
			}
			catch(Throwable e) {
				System.out.println("Failed to configure logging from " + sLog4jUrl + ": " + e.getMessage());
			}
		}

		try {
			System.out.println("Try to configure loggging from default configuration file.");
			DOMConfigurator.configure(this.getClass().getClassLoader().getResource("log4j.xml"));
			log = Logger.getLogger(this.getClass().getName());
			log.info("Logging configured from default log4j configuration.");
			return;
		}
		catch (Throwable t) {
			throw new NuclosFatalException("The client-side logging could not be initialized, because the configuration file log4j.xml was not found in the Classpath.", t);
		}
		finally {
			LogLog.setInternalDebugging(true);
		}
	}

	private void setInitialLocaleBundle() {
		// is set in login screen
//		Preferences prefs = ClientPreferences.getUserPreferences();
//		Preferences localeNode = prefs.node("locale");
//		LocaleInfo localeInfo = LocaleDelegate.getInstance().getBestLocale(LocaleInfo.parseTag(localeNode.get("locale", null)));
//		CommonLocaleDelegate.setLocaleInfo(localeInfo);
	}

	private void createGUI() {
		this.setupLookAndFeel();

		try {
			ServerConfiguration serverConfig = ServerConfiguration.getDefaultServerConfiguration();

			// perform login:
			final LoginController ctlLogin = new LoginController(null, serverConfig);

			ctlLogin.addLoginListener(new LoginListener() {
				@Override
                public void loginSuccessful(final LoginEvent ev) {
					log.debug("login start");

					UIUtils.runCommand(null, new Runnable() {
						@Override
                        public void run() {
							try {
								Modules.initialize();
								MetaDataClientProvider.initialize();
								setInitialLocaleBundle();
								compareClientAndServerVersions();
								ctlLogin.increaseLoginProgressBar(PROGRESS_COMPARE_VERSIONS);
								createMainController(ev.getAuthenticatedUserName(), ev.getConnectedServerName(), ctlLogin);

								try {
									// notify LaunchListeners
									ServiceManager.lookup("javax.jnlp.SingleInstanceService");
									Main.notifyListeners(StartUp.this.args);
							    }
								catch (UnavailableServiceException ex) {
							    	// no webstart context
							    }

								// After successful initialization, set the strict version of the critical error handler:
								SwingUtilities.invokeLater(new Runnable() {
									@Override
                                    public void run() {
										Errors.getInstance().setCriticalErrorHandler(new NuclosCriticalErrorHandler(true));
										log.debug("login done");
									}
								});

								if (Main.isMacOSX()) {
									Class<?> macAppClass = Class.forName("com.apple.eawt.Application");
									Object macAppObject = macAppClass.getConstructor().newInstance();

									// register about handler
									Class<?> macAboutHandlerClass = Class.forName("com.apple.eawt.AboutHandler");
									Method macAppSetAboutHandlerMethod = macAppClass.getDeclaredMethod("setAboutHandler", new Class[] { macAboutHandlerClass });

									Object macAboutHandler = Proxy.newProxyInstance(Main.class.getClassLoader(), new Class[] { macAboutHandlerClass }, new InvocationHandler() {
										@Override
										public Object invoke(Object proxy, Method method, Object[] args)	throws Throwable {
											if (method != null && "handleAbout".equals(method.getName()) && args.length == 1) {
												Main.getMainController().cmdShowAboutDialog();
											}

											return null;
										}
									});
									macAppSetAboutHandlerMethod.invoke(macAppObject, new Object[] { macAboutHandler });

									// register preferences handler
									Class<?> macPreferencesHandlerClass = Class.forName("com.apple.eawt.PreferencesHandler");
									Method macAppSetPreferencesHandlerMethod = macAppClass.getDeclaredMethod("setPreferencesHandler", new Class[] { macPreferencesHandlerClass });

									Object macPreferencesHandler = Proxy.newProxyInstance(Main.class.getClassLoader(), new Class[] { macPreferencesHandlerClass }, new InvocationHandler() {
										@Override
										public Object invoke(Object proxy, Method method, Object[] args)	throws Throwable {
											if (method != null && "handlePreferences".equals(method.getName()) && args.length == 1) {
												Main.getMainController().cmdOpenSettings();
											}

											return null;
										}
									});
									macAppSetPreferencesHandlerMethod.invoke(macAppObject, new Object[] { macPreferencesHandler });

									// register quit handler
						            Class<?> macQuitHandlerClass = Class.forName("com.apple.eawt.QuitHandler");
						            Method macAppSetQuitHandlerMethod = macAppClass.getDeclaredMethod("setQuitHandler", new Class[] { macQuitHandlerClass });

						            Object macQuitHandler = Proxy.newProxyInstance(Main.class.getClassLoader(), new Class[] { macQuitHandlerClass }, new InvocationHandler() {
										@Override
										public Object invoke(Object proxy, Method method, Object[] args)	throws Throwable {
											if (method != null && "handleQuitRequestWith".equals(method.getName()) && args.length == 2) {
												Class<?> macQuitResponseClass = Class.forName("com.apple.eawt.QuitResponse");
												Method macQuitResponsePerformQuitMethod = macQuitResponseClass.getDeclaredMethod("performQuit");
												Method macQuitResponseCancelQuitMethod = macQuitResponseClass.getDeclaredMethod("cancelQuit");

												if (Main.getMainController().cmdWindowClosing()) {
													macQuitResponsePerformQuitMethod.invoke(args[1]);
												} else {
													macQuitResponseCancelQuitMethod.invoke(args[1]);
												}
											}
											return null;
										}
									});
						            macAppSetQuitHandlerMethod.invoke(macAppObject, new Object[] { macQuitHandler });

						            //register dock menu
						            PopupMenu macDockMenu = new PopupMenu();
						            MenuItem miDockLogoutExit = new MenuItem(CommonLocaleDelegate.getResource("miLogoutExit", "Abmelden und Beenden"));
						            miDockLogoutExit.addActionListener(new ActionListener() {
										@Override
										public void actionPerformed(ActionEvent e) {
											MainController.cmdLogoutExit();
										}
									});
						            macDockMenu.add(miDockLogoutExit);

						            Method macAppSetDockMenuMethod = macAppClass.getDeclaredMethod("setDockMenu", PopupMenu.class);
						            macAppSetDockMenuMethod.invoke(macAppObject, macDockMenu);
								}
							}
							catch (Exception ex) {
								Errors.getInstance().showExceptionDialog(null, ex);
								Main.exit(Main.ExitResult.ABNORMAL);
							}
						}

						private void compareClientAndServerVersions() {
							final ApplicationProperties.Version versionClient = ApplicationProperties.getInstance().getCurrentVersion();
							final ApplicationProperties.Version versionServer = SecurityDelegate.getInstance().getCurrentApplicationVersionOnServer();
							if (!versionClient.equals(versionServer)) {
								final String sMessage = "The version of this client is not compatible with the version of the connected server." +
										"\nClient-version: " + versionClient.getVersionNumber() + "\nServer-version: " + versionServer.getVersionNumber() +
										"\n\nPlease contact the system administrator.";
									//"Die Version dieses Clients ist nicht kompatibel mit der Version des verbundenen Servers.\n" + "Client-Version: " + versionClient + "\n" + "Server-Version: " + versionServer + "\n" + "\nBitte wenden Sie sich an den Systemadministrator.";
								throw new NuclosFatalException(sMessage);
							}
						}

					});
				}

				@Override
                public void loginCanceled(LoginEvent ev) {
					/** @todo adjust semantics (failed vs. canceled) */
					Main.exit(Main.ExitResult.LOGIN_FAILED);
				}
			});

			ctlLogin.run();

			/** @todo move exception handling to LoginController.run() */
		}
		catch (CommonFatalException ex) {
			try {
				final Throwable tCause = ex.getCause();
				if (tCause instanceof Exception) {
					throw (Exception) tCause;
				}
				else if (tCause instanceof Error) {
					throw (Error) tCause;
				}
				else {
					throw new CommonFatalException("Unknown Throwable", ex);
				}
			}
			catch (NamingException exCause) {
				final String sMessage = "No connection could be establish to the server.\nPlease contact the system administrator.";
					//"Es konnte keine Verbindung zum Server hergestellt werden.\n" + "Bitte wenden Sie sich an den Systemadministrator.";
				Errors.getInstance().showExceptionDialog(null, sMessage, ex);
			}
			catch (Exception exCause) {	// everything else
				final String sMessage = "A fatal error occurred.";//"Es ist ein fataler Fehler aufgetreten.";
				Errors.getInstance().showExceptionDialog(null, sMessage, ex);
			}
			finally {
				Main.exit(Main.ExitResult.ABNORMAL);
			}
		}
		catch (Exception ex) {
			Errors.getInstance().showExceptionDialog(null, null, ex);
			Main.exit(Main.ExitResult.ABNORMAL);
		}

	}

	@SuppressWarnings("unchecked")
	private void createMainController(String sUserName, String sNucleusServerName, LoginController lc)
			throws CommonPermissionException {

		try {
			final String sClassName = LangUtils.defaultIfNull(
					ApplicationProperties.getInstance().getMainControllerClassName(),
					MainController.class.getName());

			final Class<? extends MainController> clsMainController = (Class<? extends MainController>) Class.forName(sClassName);
			final Constructor<? extends MainController> ctor = clsMainController.getConstructor(String.class, String.class, LoginController.class);
			ctor.newInstance(sUserName, sNucleusServerName, lc);
		}
		catch (InvocationTargetException ex) {
			final Throwable tTarget = ex.getTargetException();
			if (tTarget instanceof CommonPermissionException) {
				throw ((CommonPermissionException) tTarget);
			}
			else {
				throw new CommonFatalException(tTarget);
			}
		}
		catch (Exception ex) {
			throw new CommonFatalException("MainController cannot be created.", ex);
		}


	}

	private void setupLookAndFeel() {
		try {
			NuclosSyntheticaUtils.setLookAndFeel();

			UIManager.put("TabbedPane.contentOpaque", Boolean.FALSE);
			UIManager.put("DesktopIconUI", "org.nuclos.client.ui.NuclosDesktopIconUI");
			UIManager.put("TextArea.font", UIManager.get("TextField.font"));

			Toolkit.getDefaultToolkit().setDynamicLayout(false);
			System.setProperty("awt.dynamicLayoutSupported", "false");
		}
		catch (Exception ex) {
			// If the look&feel can't be set, don't worry. Just print stack trace and continue:
			log.warn("Look&Feel cannot be set", ex);
		}
	}



}	// class StartUp