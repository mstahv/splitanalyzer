package org.peimari.splits.client;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import org.peimari.domain.ClassResult;
import org.peimari.domain.CompetitorStatus;
import org.peimari.domain.Person;
import org.peimari.domain.Result;
import org.peimari.domain.ResultList;
import org.peimari.domain.SplitTime;

public class AaltonenHtmlParser {

    public ResultList parse(String text) {
        ResultList rl = new ResultList();

        String className = Document.get().getElementsByTagName("h2").getItem(0).
                getInnerText();
        ClassResult classResult = new ClassResult();
        classResult.setClassName(className);

        NodeList<Element> tables = Document.get().getElementsByTagName("table");
        for (int i = 0; i < tables.getLength(); i++) {
            Element item = tables.getItem(i);
            if ("rva_hk".equals(item.getClassName())) {
                NodeList<Element> trs = item.getElementsByTagName("tr");
                for (int j = 1; j < trs.getLength(); j++) {
                    try {

                        Element tr = trs.getItem(j);
                        NodeList<Element> tds = tr.getElementsByTagName("td");
                        if (tds.getLength() < 5) {
                            continue;
                        }
                        Element namecell = tds.getItem(1);
                        if ("nimi".equals(namecell.getClassName())) {

                            Result result = new Result();
                            Person person = new Person();
                            final String innerText = namecell.
                                    getFirstChildElement().getInnerText();
                            person.setFirstName(innerText.substring(0,
                                    innerText.indexOf(" ")));
                            person.setLastName(innerText.substring(innerText.
                                    indexOf(" ") + 1));
                            result.setPerson(person);

                            String rcell = tds.getItem(tds.getLength() - 2).
                                    getInnerText();
                            // only try to parse splits if result is available
                            if (rcell.matches("[0-9].*")) {
                                result.setCompetitorStatus(
                                        CompetitorStatus.OK);
                                parseSplits(tds, result);
                                long time = result.getSplitTimes()
                                        .get(result.getSplitTimes().size() - 1)
                                        .getTime();
                                result.setTime(time);
                                if (time != 0) {
                                    classResult.getResults().add(result);
                                }
                            }
                        }
                    } catch (Exception e) {
                    }
                }

            }
        }

        if (classResult.getResults().isEmpty()) {
            throw new RuntimeException("Not an 'aaltonen format' !? ");
        }

        rl.getClassResults().add(classResult);

        return rl;
    }

    private void parseSplits(NodeList<Element> tds, Result result) throws NumberFormatException {
        for (int k = 2; k < tds.getLength() - 2; k++) {
            Element el = tds.getItem(k);
            String timestr = el.getElementsByTagName(
                    "span").
                    getItem(0).getInnerText();
            if (timestr.equals("-")) {
                // missed punch
                SplitTime splitTime = new SplitTime();
                result.getSplitTimes().add(splitTime);
            } else {
                // positions not always calculated by server, scheduled ?
                if (timestr.contains("(")) {
                    timestr = timestr.substring(0,
                            timestr.
                            indexOf("(") - 1);
                }
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

}
