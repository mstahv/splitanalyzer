package org.peimari.splits.client;


import java.util.ArrayList;
import java.util.List;

import org.peimari.domain.ClassResult;
import org.peimari.domain.CompetitorStatus;
import org.peimari.domain.Person;
import org.peimari.domain.Result;
import org.peimari.domain.ResultList;
import org.peimari.domain.SplitTime;

import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.Text;
import com.google.gwt.xml.client.XMLParser;

public class IofParser implements Parser {

	/**
	 * Unmarshals IOF-XML file into a ResultList.
	 * 
	 * @param text
	 * @return
	 */
	public ResultList parse(String text) {
		ResultList rl = new ResultList();
		Document parse = XMLParser.parse(text);
		NodeList elementsByTagName = parse.getElementsByTagName("ClassResult");
		for (int i = 0; i < elementsByTagName.getLength(); i++) {
			Node item = elementsByTagName.item(i);
			rl.getClassResults().add(parseClassResult((Element) item));
		}
		if(rl.getClassResults().isEmpty()) {
			throw new RuntimeException("No results found");
		}
		return rl;
	}

	private static ClassResult parseClassResult(Element item) {
		ClassResult classResult = new ClassResult();
		Text item2 = (Text) item.getElementsByTagName("ClassShortName").item(0)
				.getFirstChild();
		classResult.setClassName(item2.getData());
		NodeList personresult = item.getElementsByTagName("PersonResult");
		for (int i = 0; i < personresult.getLength(); i++) {
			Element person = (Element) personresult.item(i);
			classResult.getResults().add(parseResult(person));
		}
		return classResult;
	}

	private static Result parseResult(Element personResult) {
		Element item = (Element) personResult
				.getElementsByTagName("PersonName").item(0);
		Person person = new Person();
		Node familyname = item.getElementsByTagName("Family").item(0);
		if (familyname.getFirstChild() != null) {
			String fn = ((Text) familyname.getFirstChild()).getData();
			person.setLastName(fn);
		} else {
			person.setLastName("?");
		}
		Node given = item.getElementsByTagName("Given").item(0);
		if (given.getFirstChild() != null) {
			String gn = ((Text) given.getFirstChild()).getData();
			person.setFirstName(gn);
		} else {
			person.setFirstName("?");
		}
		Result result = new Result();
		result.setPerson(person);
		personResult = (Element) personResult.getElementsByTagName("Result")
				.item(0);

		Element s = (Element) personResult.getElementsByTagName(
				"CompetitorStatus").item(0);
		String status = s.getAttribute("value");

		result.setCompetitorStatus(CompetitorStatus.valueOf(status
				.toUpperCase()));
		NodeList childNodes = personResult.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node item2 = childNodes.item(i);
			if ("Time".equals(item2.getNodeName())) {
				Text time = (Text) item2.getFirstChild();
				result.setTime(parseTime(time));
			}
		}
		result.setSplitTimes(parseSplits(personResult
				.getElementsByTagName("SplitTime")));
		return result;
	}

	private static List<SplitTime> parseSplits(NodeList elementsByTagName) {
		ArrayList<SplitTime> arrayList = new ArrayList<SplitTime>();
		for (int i = 0; i < elementsByTagName.getLength(); i++) {
			Element item = (Element) elementsByTagName.item(i);
			Text el = (Text) item.getElementsByTagName("ControlCode").item(0)
					.getFirstChild();
			Text time = (Text) item.getElementsByTagName("Time").item(0)
					.getFirstChild();
			SplitTime splitTime = new SplitTime();
			String nodeValue = el.getData();
			if (nodeValue == null) {
				splitTime.setControlCode(0);
			} else {
				splitTime.setControlCode(Integer.parseInt(nodeValue));
			}
			splitTime.setTime(parseTime(time));
			arrayList.add(splitTime);
		}
		return arrayList;
	}

	private static long parseTime(Text time) {
		if (time == null) {
			return 0;
		}
		String timeString = time.getData();
		if (timeString == null) {
			return 0;
		}
		String[] split = timeString.split(":");
		if (split.length == 3) {

			long t = Integer.parseInt(split[0]) * 60 * 60 * 1000
					+ Integer.parseInt(split[1]) * 60 * 1000
					+ (long) (Double.parseDouble(split[2]) * 1000);
			return t;
		} else {
			return 0;
		}
	}

}
