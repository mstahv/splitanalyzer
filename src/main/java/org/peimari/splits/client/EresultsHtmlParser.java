package org.peimari.splits.client;

import org.peimari.domain.ClassResult;
import org.peimari.domain.CompetitorStatus;
import org.peimari.domain.Person;
import org.peimari.domain.Result;
import org.peimari.domain.ResultList;
import org.peimari.domain.SplitTime;

public class EresultsHtmlParser {

    public ResultList parse(String text) {

        // remove some formatting for easier parsing
        text = text.replaceAll("<span style=\"[^\"]+\">", "");
        text = text.replaceAll("</span>", "");

        ResultList rl = new ResultList();
        String[] lines = text.split("\n");

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if (line.toLowerCase().startsWith("<h3")) {
                // Example:  Lapset, tilanne rasteilla, rastivälien ajat
                ClassResult classResult = new ClassResult();
                line = line.substring(4, line.lastIndexOf("3>") + 2);
                classResult.setClassName(line.substring(0, line.indexOf(",")).
                        trim());
                i = readResults(classResult, lines, i + 1);
                rl.getClassResults().add(classResult);
            }
        }
        return rl;
    }

    private int readResults(ClassResult classResult, String[] lines, int i) {
        for (; i < lines.length; i++) {
            String line = lines[i];
            if (line.startsWith("<PRE>")) {
                i++;
                continue;
            }
            if (line.startsWith("</PRE>")) {
                i++;
                break;
            }
            if (line.startsWith("    ")) {
                continue;
            }

            if (line.startsWith(" ")) { // bad logic, support only < 1000 per series
                Result r = new Result();
                String start = line.substring(0,35);
                String resultset = line.substring(34);
                if (line.charAt(line.length() - 1) == '-') {
                    r.setCompetitorStatus(CompetitorStatus.DISQUALIFIED);
                } else {
                    r.setCompetitorStatus(CompetitorStatus.OK);
                }
                String[] a = start.split("[ ]+");
                String firstName = a[2];
                String lastName = a.length > 3 ? a[3] : "";
                Person person = new Person();
                person.setFirstName(firstName);
                person.setLastName(lastName);
                r.setPerson(person);
                try {
                    final int RESLENGHT = 11;
                    String[] ra = new String[resultset.length()/RESLENGHT - 1];
                    for (int j = 0; j < ra.length; j++) {
                        int s = j*RESLENGHT;
                        int e = s + RESLENGHT;
                        ra[j] = resultset.substring(s , e);
                    }
                    readSplits(r, ra);
               } catch (Exception e) {
                    r.setCompetitorStatus(CompetitorStatus.DIDNOTFINISH);
                }
                if (r.getCompetitorStatus() == CompetitorStatus.OK) {
                    long time = r.getSplitTimes()
                            .get(r.getSplitTimes().size() - 1)
                            .getTime();
                    r.setTime(time);
                    classResult.getResults().add(r);
                }
            }
        }
        return i;
    }

    private void readSplits(Result r, String[] a) {
        for (int i = 0; i < a.length; i++) {
            String string = a[i].trim();
            if (string.isEmpty() || string.equals("-")) {
                // missed punch
                SplitTime splitTime = new SplitTime();
                r.setCompetitorStatus(CompetitorStatus.DISQUALIFIED);
                r.getSplitTimes().add(splitTime);
            } else if (string.contains("-")) {
                // considered as time if no alphanumerics
                String[] split = string.split("-");
                int curCumu = 0;
                String time = split[1];

                String[] split2 = time.split("\\.");
                for (int j = split2.length - 1; j >= 0; j--) {
                    String s = split2[j];
                    int k = split2.length - j - 1;
                    curCumu += (int) (Integer.parseInt(s) * Math.pow(60, k) * 1000);
                }
                SplitTime splitTime = new SplitTime();
                splitTime.setTime(curCumu);
                r.getSplitTimes().add(splitTime);
            } else {
                // NOP name at the end
                System.out.println("SS");
            }
        }
    }
}
