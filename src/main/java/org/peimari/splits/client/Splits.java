package org.peimari.splits.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.ScriptInjector;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
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
        if (analyzeSelf()) {
            // Detect if aaltosen online
            Element sisalto = Document.get().getElementById("sisalto");
            if (sisalto != null) {
                Element el = Document.get().createElement("div");
                el.setId("sa");
                Element h3 = sisalto.getElementsByTagName("h3").getItem(0);
                if (h3 != null) {
                    h3.getParentElement().insertAfter(el, h3);
                } else {
                    sisalto.insertFirst(el);
                }
                load(Window.Location.getHref(), "sa");
                return;
            }
            // Detect if new pirilä
            NodeList<Element> elementsByTagName = Document.get().
                    getElementsByTagName("rvatbl");
            NodeList<Element> elementsByTagName2 = Document.get().
                    getElementsByTagName("otsikko");
            if (elementsByTagName.getLength() > 0 && elementsByTagName2.
                    getLength() > 0) {
                Element el = Document.get().createElement("div");
                el.setId("sa");
                Element h = elementsByTagName2.getItem(0);
                h.getParentElement().insertAfter(el, h);
                load(Window.Location.getHref(), "sa");
                return;
            }

            load(Window.Location.getHref(), null);
        } else if (publishJsApi) {
            load(null, null);
        }
    }

    private native boolean analyzeSelf() /*-{
     return $wnd.splitanalyzeself ? true :false;
     }-*/;

    private native boolean publishJsApi() /*-{
     if($wnd.splitanalyzerform) {
     return true;
     }
     $wnd.splitanalyze = $entry(@org.peimari.splits.client.Splits::load(Ljava/lang/String;Ljava/lang/String;));
     return false;
     }-*/;

    public static void load(String url, String id) {
        MyResources.INSTANCE.css().ensureInjected();
        ensureHighcharts();
        final SplitAnalyzer w = new SplitAnalyzer(url);
        if (id != null) {
            RootPanel.get(id).clear();
            RootPanel.get(id).add(w);
        } else {
            RootPanel.get().insert(w, 0);
        }
    }

    private static void ensureHighcharts() {
        if (!isInjected()) {
            inject(MyResources.INSTANCE.jQuery().getText());
            inject(MyResources.INSTANCE.highcharts().getText());
        }
    }

    public static void inject(String javascript) {
        ScriptInjector.fromString(javascript).setWindow(topWindow()).inject();
    }

    /**
     * TODO
     *
     * @return
     */
    private static native JavaScriptObject topWindow() /*-{
     return $wnd;
     }-*/;

    private native static boolean isInjected() /*-{
     return $wnd.Highcharts ? true: false;
     }-*/;

}
