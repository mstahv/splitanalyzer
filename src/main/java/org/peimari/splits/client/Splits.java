package org.peimari.splits.client;




import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Splits implements EntryPoint {

	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		boolean publishJsApi = publishJsApi();
		if(publishJsApi) {
			load(null, null);
		}
	}

	private native boolean publishJsApi()
	/*-{
		if($wnd.splitanalyzerform) {
			return true;
		}
		$wnd.splitanalyze = $entry(@org.peimari.splits.client.Splits::load(Ljava/lang/String;Ljava/lang/String;));
		return false;
	}-*/;

	public static void load(String url, String id) {
		MyResources.INSTANCE.css().ensureInjected();
		final SplitAnalyzer w = new SplitAnalyzer(url);
		if (id != null) {
			RootPanel.get(id).clear();
			RootPanel.get(id).add(w);
		} else {
			RootPanel.get().add(w);
		}
	}

}
