package org.peimari.splits.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.HeadElement;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.ScriptElement;
import com.google.gwt.user.client.Window;
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
		if(analyzeSelf()) {
            // Detect if aaltosen online
            Element sisalto = Document.get().getElementById("sisalto");
            if(sisalto != null) {
                Element el = Document.get().createElement("div");
                el.setId("sa");
                Element h3 = sisalto.getElementsByTagName("h3").getItem(0);
                if(h3 != null) {
                    h3.getParentElement().insertAfter(el, h3);
                } else {
                    sisalto.insertFirst(el);
                }
    			load(Window.Location.getHref(), "sa");
            } else {
    			load(Window.Location.getHref(), null);
            }
		} else if(publishJsApi) {
			load(null, null);
		}
	}

	private native boolean analyzeSelf()
	/*-{
		return $wnd.splitanalyzeself ? true :false;
	}-*/;

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
		injectScripts();
		final SplitAnalyzer w = new SplitAnalyzer(url);
		if (id != null) {
			RootPanel.get(id).clear();
			RootPanel.get(id).add(w);
		} else {
			RootPanel.get().insert(w, 0);
		}
	}

	private static void injectScripts() {
		if(!isInjected()) {
			inject(MyResources.INSTANCE.jQuery().getText());
			inject(MyResources.INSTANCE.highcharts().getText());
		}
	}

    private native static boolean isInjected() 
    /*-{
		return $wnd.Highcharts ? true: false;
	}-*/;

	private static HeadElement head;
    
    public static void inject(String javascript) {
        HeadElement head = getHead();
        ScriptElement element = createScriptElement();
        element.setText(javascript);
        head.appendChild(element);
    }
 
    private static ScriptElement createScriptElement() {
        ScriptElement script = Document.get().createScriptElement();
        script.setAttribute("language", "javascript");
        return script;
    }
 
    private static HeadElement getHead() {
        if (head == null) {
            Element element = Document.get().getElementsByTagName("head")
                    .getItem(0);
            assert element != null : "HTML Head element required";
            head = HeadElement.as(element);
        }
        return head;
    }

	
}
