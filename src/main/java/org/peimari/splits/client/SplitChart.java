package org.peimari.splits.client;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.moxieapps.gwt.highcharts.client.Axis;
import org.moxieapps.gwt.highcharts.client.Chart;
import org.moxieapps.gwt.highcharts.client.DateTimeLabelFormats;
import org.moxieapps.gwt.highcharts.client.Point;
import org.moxieapps.gwt.highcharts.client.Series;
import org.moxieapps.gwt.highcharts.client.ToolTip;
import org.moxieapps.gwt.highcharts.client.ToolTipData;
import org.moxieapps.gwt.highcharts.client.ToolTipFormatter;
import org.moxieapps.gwt.highcharts.client.labels.AxisLabelsData;
import org.moxieapps.gwt.highcharts.client.labels.AxisLabelsFormatter;
import org.moxieapps.gwt.highcharts.client.labels.YAxisLabels;
import org.peimari.domain.Result;
import org.peimari.domain.SplitTime;
import org.peimari.util.Util;

public class SplitChart extends Chart {

	public SplitChart(Collection<Result> results, boolean compareToSuperman) {
		addStyleName("split-chart");
		setType(Series.Type.SPLINE);
		setChartTitleText("Split time visualization");
		setToolTip(new ToolTip().setFormatter(new ToolTipFormatter() {
			public String format(ToolTipData toolTipData) {
				long delta = -toolTipData.getYAsLong();
				long cumulative = toolTipData.getXAsLong() + delta;
				return "<b>" + toolTipData.getSeriesName() + "</b>"
						+ toolTipData.getPointName() + "<br/>"
						+ Util.formatTime(cumulative) + " : +"
						+ Util.formatTime(delta) + " s";
			}
		}));

		getXAxis()
				.setType(Axis.Type.DATE_TIME)
				.setDateTimeLabelFormats(
						new DateTimeLabelFormats().setDay(""))
				.setAxisTitleText("Reference time/controls");

		getYAxis().setAxisTitleText("Difference");
		getYAxis().setLabels(
				new YAxisLabels().setFormatter(new AxisLabelsFormatter() {
					@Override
					public String format(AxisLabelsData axisLabelsData) {
						long valueAsLong = axisLabelsData.getValueAsLong();
						if (valueAsLong < 0) {
							valueAsLong = -valueAsLong;
						}
						return Util.formatTime(valueAsLong);
					}
				}));

		getYAxis().setDateTimeLabelFormats(new DateTimeLabelFormats());

		ArrayList<SplitTime> zeroLine = new ArrayList<SplitTime>();
		if(compareToSuperman) {
			SplitAnalyzer.calculateSuperman(results, zeroLine);
		} else {
			// compare to first result
			Result r = results.iterator().next();
			List<SplitTime> splitTimes2 = r.getSplitTimes();
			for (int i = 0; i < splitTimes2.size(); i++) {
				zeroLine.add(splitTimes2.get(i));
			}
		}

		for (Result result : results) {
			Series s = createSeries().setName(result.getPerson().toString());
			List<SplitTime> splitTimes = result.getSplitTimes();
			Point point = new Point(0, 0);
			point.setName("Start");
			s.addPoint(point);
			for (int i = 0; i < splitTimes.size(); i++) {
				SplitTime splitTime = splitTimes.get(i);
				double delta = (zeroLine.get(i).getTime() - splitTime
						.getTime());
				point = new Point(zeroLine.get(i).getTime(), delta);
				point.setName("Control " + (i+1));
				s.addPoint(point);
			}
			addSeries(s);

		}

	}
}
