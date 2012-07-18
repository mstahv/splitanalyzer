package org.peimari.splits.client;

import org.peimari.domain.ClassResult;
import org.peimari.domain.CompetitorStatus;
import org.peimari.domain.Person;
import org.peimari.domain.Result;
import org.peimari.domain.ResultList;
import org.peimari.domain.SplitTime;

public class PirilaHtmlParser {

	public ResultList parse(String text) {
		ResultList rl = new ResultList();
		String[] lines = text.split("\n");
		for (int i = 0; i < lines.length; i++) {
			String line = lines[i];
			if (line.startsWith("<H3") && line.contains("Tilanne rasteilla")) {
				ClassResult classResult = new ClassResult();
				line = line.substring(line.lastIndexOf("<H3>") + 4);
				classResult.setClassName(line.substring(0,
						line.indexOf(" ")));
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
			if(line.startsWith("</PRE>")) {
				i++;
				break;
			}
			if (line.startsWith(" ")) {
				Result r = new Result();
				r.setCompetitorStatus(CompetitorStatus.OK);
				String[] a = line.split("[ ]+");
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
							long time = r.getSplitTimes().get(r.getSplitTimes().size() -1).getTime();
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
			String[] split = string.split("-");
			if(split.length == 2) {
				int curCumu = 0;
				String[] split2 = split[1].split("\\.");
				for (int j = split2.length - 1; j >= 0; j--) {
					String s = split2[j];
					int k = split2.length - j - 1;
					curCumu += (int) (Integer.parseInt(s) * Math.pow(60, k)*1000);
				}
				SplitTime splitTime = new SplitTime();
				splitTime.setTime(curCumu);
				r.getSplitTimes().add(splitTime);
			}
		}
	}
}
