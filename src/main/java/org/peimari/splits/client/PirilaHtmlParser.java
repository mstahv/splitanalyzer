package org.peimari.splits.client;

import org.peimari.domain.ClassResult;
import org.peimari.domain.CompetitorStatus;
import org.peimari.domain.Person;
import org.peimari.domain.Result;
import org.peimari.domain.ResultList;
import org.peimari.domain.SplitTime;

import com.google.gwt.regexp.shared.RegExp;

public class PirilaHtmlParser {

	public ResultList parse(String text) {
		ResultList rl = new ResultList();
		String[] lines = text.split("\n");
		for (int i = 0; i < lines.length; i++) {
			String line = lines[i];
			if (line.toLowerCase().startsWith("<h3")
					&& line.contains("Tilanne rasteilla")) {
				ClassResult classResult = new ClassResult();
				line = line.substring(line.lastIndexOf("3>") + 2);
				classResult.setClassName(line.substring(0, line.indexOf(" ")));
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
				break;
			}
			if (line.startsWith("</PRE>")) {
				i++;
				break;
			}
			if (line.startsWith(" ")) {
				Result r = new Result();
				String[] a = line.split("[ ]+");
				if (a[1].contains(".")) {
					r.setCompetitorStatus(CompetitorStatus.OK);
				} else {
					r.setCompetitorStatus(CompetitorStatus.DISQUALIFIED);
				}
				String firstName;
				String lastName = a[2];
				for (int j = 3; j < a.length; j++) {
					try {
						String str = a[j];
						String nextstr = a[j + 1];
						if (nextstr.matches("[0-9].*")) {
							firstName = str;
							Person person = new Person();
							person.setFirstName(firstName);
							person.setLastName(lastName);
							r.setPerson(person);
							readSplits(r, a, j + 1);
							long time = r.getSplitTimes()
									.get(r.getSplitTimes().size() - 1)
									.getTime();
							r.setTime(time);
							classResult.getResults().add(r);
							break;
						} else {
							lastName = lastName + " " + str;
						}
					} catch (Exception e) {
						// No splits or something
						break;
					}
				}
			}
		}
		return i;
	}
	
	private void readSplits(Result r, String[] a, int i) {
		for (; i < a.length; i++) {
			String string = a[i].trim();
			if (string.equals("-")) {
				// missed punch
				SplitTime splitTime = new SplitTime();
				r.getSplitTimes().add(splitTime);
			} else if (!string.matches("[^\\d].*")) {
				// considered as time if no alphanumerics
				String[] split = string.split("-");
				int curCumu = 0;
				String time;
				if (split.length == 1) {
					time = string;
				} else {
					time = split[1];
				}
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
