package program;

import mosaic.ui.MainWindow;

public class JWrapper {
	public static void main(String[] args) throws Exception {
		/*
		if (OS.isMacOS()) {			
			//JWMacOS.registerURLSchemeForVirtualApp("jwsample", vappName);
			String appBundle = JWSystem.getAppBundleName();		
			String bundleID = JWConstants.buildOsxDomainFromBundle(appBundle);

			JWMacOS.registerAppAsURLHandler("jwsample", bundleID);

			JWMacOS.setOSXEventListener(new JWOSXEventListener() {
				@Override
				public void openURL(final String url) {
					new Thread() {
						@Override
						public void run() {
							System.out.println("[SampleApp] Showing URL dialog for "+url);
						}
					}.start();
				}				
			});

			String url = JWMacOS.getRequestedURL(); // URL CAN NOW BE HANDLED
		}
		else if (OS.isWindows()) {
			String vappName = JWApp.getMyVirtualApp().getUserVisibleName();

			JWWindowsOS.registerURLSchemeForVirtualApp("jwsample", vappName);

			String url = JWWindowsOS.getRequestedURL(); // URL CAN NOW BE HANDLED
		}

		String message = JWSystem.getAppLaunchProperty("message"); //message from launch properties - such as from the shortcut.

		//Demonstrates saving a parameterised shortcut
		//saveShortcut();

		String myJwVersion;
		try {
			//App is running from JWrapper, so has access to JW APIs
			myJwVersion = JWSystem.getAppBundleVersion();
		} catch (Exception x) {
			//App is running outside of JWrapper
			myJwVersion = "(not running inside JWrapper)";
		}*/

		MainWindow.main(null);
		//JOptionPane.showMessageDialog(null, "Example Minimal App ("+message+") "+JWSystem.getAppBundleVersion());
	}

	/**
	 * Demonstrates saving a shortcut to a virtual app with a different set of launch properties
	 */
	/*
	public static void saveShortcut() throws Exception {
		File desktop = new File(System.getProperty("user.home"),"Desktop");

		Properties props = new Properties();
		props.setProperty("message", "Property message from shortcut");

		JWSystem.saveLauncherShortcutForVirtualApp(desktop, "LDDMC", "LDDMC", props, false);
	}//*/
}
