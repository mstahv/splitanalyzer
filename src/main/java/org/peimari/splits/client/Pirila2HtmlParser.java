package org.peimari.splits.client;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.user.client.Window;
import org.peimari.domain.ClassResult;
import org.peimari.domain.CompetitorStatus;
import org.peimari.domain.Person;
import org.peimari.domain.Result;
import org.peimari.domain.ResultList;
import org.peimari.domain.SplitTime;

public class Pirila2HtmlParser {

    public ResultList parse(String text) {
        ResultList rl = new ResultList();

        NodeList<Element> elementsByTagName = Document.get().
                getElementsByTagName("h3");
        for (int i = 0; i < elementsByTagName.getLength(); i++) {

            Element item = elementsByTagName.getItem(i);
            String className = item.getInnerText().split(" ")[0];

            ClassResult classResult = new ClassResult();
            classResult.setClassName(className);
            Element table = (Element) item.getNextSibling();
            NodeList<Element> trs = table.getElementsByTagName("tr");

            for (int j = 1; j < trs.getLength(); j = j + 2) {
                try {
                    Element tr = trs.getItem(j);
                    NodeList<Element> tds = tr.getElementsByTagName("td");
                    if (tds.getLength() < 5) {
                        continue;
                    }
                    Element namecell = tds.getItem(1);
                    Result result = new Result();
                    Person person = new Person();
                    final String innerText = namecell.getInnerText();
                    person.setFirstName(innerText.substring(0,
                            innerText.indexOf(" ")));
                    person.setLastName(innerText.substring(innerText.
                            indexOf(" ") + 1));
                    result.setPerson(person);

                    String rcell = tds.getItem(0).
                            getInnerText();
                    // only try to parse splits if result is available
                    if (rcell.contains(".")) {
                        result.setCompetitorStatus(
                                CompetitorStatus.OK);
                        consoleLog("Parsing splits...");
                        parseSplits(tds, result);
                        long time = result.getSplitTimes()
                                .get(result.getSplitTimes().size() - 1)
                                .getTime();
                        result.setTime(time);
                        if (time != 0) {
                            classResult.getResults().add(result);
                        }
                    }
                } catch (Exception e) {
                    consoleLog(e.getMessage());
                }
            }

            rl.getClassResults().add(classResult);
        }

        if (rl.getClassResults().isEmpty()) {
            throw new RuntimeException("Not a 'pirlä 2 format' !? ");
        }

        return rl;
    }
    
      native void consoleLog( String message) /*-{
      console.log( "me:" + message );
  }-*/;

    private void parseSplits(NodeList<Element> tds, Result result) throws NumberFormatException {
        for (int k = 3; k < tds.getLength() - 2; k = k + 2) {
            Element el = tds.getItem(k);
            String timestr = el.getInnerText();
            String[] split2 = timestr.split(":");
            int curCumu = 0;
            for (int l = split2.length - 1; l >= 0; l--) {
                String s = split2[l].trim();
                int m = split2.length - l - 1;
                curCumu += (int) (Integer.
                        parseInt(s) * Math.
                        pow(60, m) * 1000);
            }
            SplitTime splitTime = new SplitTime();
            splitTime.setTime(curCumu);
            result.getSplitTimes().add(splitTime);
        }
    }

}
