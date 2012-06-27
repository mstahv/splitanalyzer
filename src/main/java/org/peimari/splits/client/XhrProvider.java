package org.peimari.splits.client;


import java.util.ArrayList;
import java.util.List;

import org.peimari.domain.ClassResult;
import org.peimari.domain.CompetitorStatus;
import org.peimari.domain.Person;
import org.peimari.domain.Result;
import org.peimari.domain.ResultList;
import org.peimari.domain.SplitTime;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Window;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.Text;
import com.google.gwt.xml.client.XMLParser;

public class XhrProvider {
	static Class[] providers = new Class[] {};

	public void getResults(String url, final SplitAnalyzer splitAnalyzer) {
		RequestBuilder rb = new RequestBuilder(RequestBuilder.GET, url);
		rb.setCallback(new RequestCallback() {

			@Override
			public void onResponseReceived(Request request, Response response) {
				String text = response.getText();
				ResultList rl = null;
				try {
					rl = new PeimariParser().parseXml(text);
				} catch (Exception e) {
					e.printStackTrace();
				}
				if(rl == null) {
					try {
						rl = new IofParser().parse(text);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				if(rl == null) {
					try {
						rl = new PirilaHtmlParser().parse(text);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				if(rl == null) {
					Window.alert("Results cound't be handled format must be either 'peimari xml' or 'iof xml'.");
				} else {
					splitAnalyzer.setResultList(rl);
				}
			}

			@Override
			public void onError(Request request, Throwable exception) {
				Window.alert("Unable to fetch results");
			}
		});
		try {
			rb.send();
		} catch (RequestException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static ResultList parseXml(String text) {
		ResultList rl = new ResultList();
		Document parse = XMLParser.parse(text);
		NodeList elementsByTagName = parse.getElementsByTagName("classResults");
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
