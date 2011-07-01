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

import java.awt.Frame;
import java.util.HashMap;
import java.util.Map;

import javax.jnlp.ServiceManager;
import javax.jnlp.SingleInstanceService;
import javax.jnlp.UnavailableServiceException;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.jdesktop.swingx.JXErrorPane;
import org.jdesktop.swingx.error.ErrorInfo;
import org.nuclos.client.LaunchListener;
import org.nuclos.client.NuclosIcons;
import org.nuclos.client.main.mainframe.MainFrame;
import org.nuclos.common.SpringApplicationContextHolder;
import org.nuclos.common2.StringUtils;

/**
 * The main class of the Nucleus client. Contains some global constants and objects.
 * Provides access to the main frame and its controller.
 * Keep this class as small as possible.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class Main {
	
	private static boolean macOSX = false;
	
	public static enum ExitResult {
		NORMAL,
		LOGIN_FAILED,
		ABNORMAL
	}

	/**
	 * the controller for the main frame.
	 */
	private static MainController maincontroller;

	/**
	 * creates an empty main object.
	 */
	private Main() {
	}

	/**
	 * (the only!) exit point of the application. Does some cleanup and finally
	 * calls <code>System.exit(iResult)</code>.
	 * todo this is public only to be used in JFCUnit!
	 */
	static void exit(ExitResult exitresult) {
		try {
			System.runFinalization();
		}
		finally {
			System.exit(exitresult.ordinal());
		}
	}

	static void setMainController(MainController maincontroller) {
		Main.maincontroller = maincontroller;
	}

	public static MainController getMainController() {
		return maincontroller;
	}

	/**
	 * @todo this shouldn't be a singleton. Use child window as parameter!
	 * @return the <code>MainFrame</code> of this application, if any.
	 */
	public static MainFrame getMainFrame() {
		for (Frame frm : JFrame.getFrames()) {
			if (frm instanceof MainFrame) {
				return (MainFrame) frm;
			}
		}
		return null;
	}

	/**
	 * the starting point of the Nucleus client
	 * @param asArgs
	 */
	public static void main(String[] asArgs) throws Exception {
		// for Mac OS X ...
		System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Nuclos");
		String osName = System.getProperty("os.name").toLowerCase();
		macOSX = osName.startsWith("mac os x");
		if (macOSX) {
			try {
				// check if java 1.6.0_22+ (Java for Mac OS X 10.6, Update 3+ | Java for Mac OS X 10.5, Update 8+)
				String runtimeVersion = System.getProperty("java.version");
				String[] fragments1 = runtimeVersion.split("_"); //e.g. "1.6.0", "22"
				String[] fragments2 = fragments1[0].split("\\."); //e.g. "1", "6", "0"
				
		        int minorVers = Integer.parseInt(fragments2[1]);
		        if (minorVers < 6) {
		        		JOptionPane.showMessageDialog(null, 
		        			isMacOSXSnowLeopardOrBetter() ? 
		        				"Nuclos Client requires Java for Mac OS X 10.6, Update 3+" : 
		        					"Nuclos Client requires Java for Mac OS X 10.5, Update 8+");
		        		Main.exit(Main.ExitResult.ABNORMAL);
		        } else if (minorVers == 6) {
		        		int bugfixVers = Integer.parseInt(fragments1[1]);
		        		if (bugfixVers < 22) {
		        			JOptionPane.showMessageDialog(null, 
		        				isMacOSXSnowLeopardOrBetter() ? 
		        				"Nuclos Client requires Java for Mac OS X 10.6, Update 3+" : 
		        					"Nuclos Client requires Java for Mac OS X 10.5, Update 8+");
			        		Main.exit(Main.ExitResult.ABNORMAL);
		        		}
		        }
				
				Class<?> macAppClass = Class.forName("com.apple.eawt.Application");
				Object macAppObject = macAppClass.getConstructor().newInstance();
				// set Nuclos dock icon
				macAppClass.getMethod("setDockIconImage", java.awt.Image.class).invoke(macAppObject, NuclosIcons.getInstance().getBigTransparentApplicationIcon512().getImage());
			} catch (Exception ex) {
				ex.printStackTrace();
				ErrorInfo ei = new ErrorInfo("Fatal Error", ex.getMessage(), null, null, ex, null, null);
				JXErrorPane.showDialog(null, ei);
				Main.exit(Main.ExitResult.ABNORMAL);
			}
		}
		
		System.out.println(System.getProperty("java.version"));
		System.out.println(System.getProperty("java.vendor"));
		
		if ("true".equals(System.getProperty("nuclos.client.singleinstance"))) {
			// register instance as single instance
			try {
				SingleInstanceService service = (SingleInstanceService)ServiceManager.lookup("javax.jnlp.SingleInstanceService");
				service.addSingleInstanceListener(new JWSSingleInstanceListener());
				System.out.println("Client started in single instance mode.");
		    }
			catch (UnavailableServiceException ex) {
				System.out.println("Client cannot be started in single instance mode because there is no webstart context available.");
			}
		}

		try {
			new StartUp(asArgs);
		} catch (Exception ex) {
			ex.printStackTrace();
			ErrorInfo ei = new ErrorInfo("Fatal Error", ex.getMessage(), null, null, ex, null, null);
			JXErrorPane.showDialog(null, ei);
			System.exit(1);
		}
	}

	protected static void notifyListeners(String[] args) {
		Logger log = Logger.getLogger(Main.class);
		if (args != null && args.length > 0) {
			log.info("Client launched with arguments " + StringUtils.join(";", args));
		}
		else {
			log.info("Client launched without arguments.");
		}

		Map<String, LaunchListener> listeners = SpringApplicationContextHolder.getApplicationContext().getBeansOfType(LaunchListener.class);
		for (Map.Entry<String, LaunchListener> listener : listeners.entrySet()) {
			log.info("Notify LaunchListener " + listener.getKey());
			try {
				listener.getValue().launched(parseArguments(args));
			}
			catch (Throwable t) {
				log.error("Exception in LaunchListener" + listener.getKey(), t);
			}
		}
	}

	private static Map<String, String> parseArguments(String[] args) {
		Map<String, String> params = new HashMap<String, String>();
		for (String arg : args) {
			if (arg.contains("=")) {
				params.put(arg.substring(0, arg.indexOf("=")), arg.substring(arg.indexOf("=") + 1));
			}
			else {
				params.put(arg, null);
			}
		}
		return params;
	}

	public static boolean isMacOSX() {
		return macOSX;
	}
	
	public static boolean isMacOSXSnowLeopardOrBetter() {
	    String osName = System.getProperty("os.name");
	    if (!osName.startsWith("Mac OS X")) return false;

	    // split the "10.x.y" version number
	    String osVersion = System.getProperty("os.version");
	    String[] fragments = osVersion.split("\\.");

	    // sanity check the "10." part of the version
	    if (!fragments[0].equals("10")) return false;
	    if (fragments.length < 2) return false;

	    // check if Mac OS X 10.6(.y)
	    try {
	        int minorVers = Integer.parseInt(fragments[1]);
	        if (minorVers >= 6) return true;
	    } catch (NumberFormatException e) {
	        // was not an integer
	    }

	    return false;
	}

}	// class Main
