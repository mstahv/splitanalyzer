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

public class PeimariParser {

	/**
	 * Unmarshals peimari projects XML file into a ResultList.
	 * 
	 * @param text
	 * @return
	 */
	public ResultList parseXml(String text) {
		Document parse = XMLParser.parse(text);
		NodeList elementsByTagName = parse.getElementsByTagName("classResults");
		if(elementsByTagName.getLength() == 0) {
			return null;
		}
		ResultList rl = new ResultList();
		for (int i = 0; i < elementsByTagName.getLength(); i++) {
			Node item = elementsByTagName.item(i);
			rl.getClassResults().add(parseClassResult((Element) item));
		}
		return rl;
	}

	private static ClassResult parseClassResult(Element item) {
		ClassResult classResult = new ClassResult();
		Text item2 = (Text) item.getElementsByTagName("className").item(0)
				.getFirstChild();
		classResult.setClassName(item2.getData());
		NodeList personresult = item.getElementsByTagName("results");
		for (int i = 0; i < personresult.getLength(); i++) {
			Element person = (Element) personresult.item(i);
			classResult.getResults().add(parseResult(person));
		}
		return classResult;
	}

	private static Result parseResult(Element personResult) {
		Element item = (Element) personResult.getElementsByTagName("person")
				.item(0);
		Person person = new Person();
		Node familyname = item.getElementsByTagName("lastName").item(0);
		if (familyname.getFirstChild() != null) {
			String fn = ((Text) familyname.getFirstChild()).getData();
			person.setLastName(fn);
		} else {
			person.setLastName("?");
		}
		Node given = item.getElementsByTagName("firstName").item(0);
		if (given.getFirstChild() != null) {
			String gn = ((Text) given.getFirstChild()).getData();
			person.setFirstName(gn);
		} else {
			person.setFirstName("?");
		}
		Result result = new Result();
		result.setPerson(person);

		Text s = (Text) personResult.getElementsByTagName("competitorStatus")
				.item(0).getFirstChild();
		String status = s.getData();

		result.setCompetitorStatus(CompetitorStatus.valueOf(status
				.toUpperCase()));
		NodeList childNodes = personResult.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node item2 = childNodes.item(i);
			if ("time".equals(item2.getNodeName())) {
				Text time = (Text) item2.getFirstChild();
				result.setTime(parseTime(time));
			}
		}
		result.setSplitTimes(parseSplits(personResult
				.getElementsByTagName("splitTimes")));
		return result;
	}

	private static List<SplitTime> parseSplits(NodeList elementsByTagName) {
		ArrayList<SplitTime> arrayList = new ArrayList<SplitTime>();
		for (int i = 0; i < elementsByTagName.getLength(); i++) {
			Element item = (Element) elementsByTagName.item(i);
			Text el = (Text) item.getElementsByTagName("controlCode").item(0)
					.getFirstChild();
			Text time = (Text) item.getElementsByTagName("time").item(0)
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
		return Long.parseLong(timeString);
	}

}
