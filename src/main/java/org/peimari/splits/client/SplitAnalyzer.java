package org.peimari.splits.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.peimari.domain.ClassResult;
import org.peimari.domain.CompetitorStatus;
import org.peimari.domain.Result;
import org.peimari.domain.ResultList;
import org.peimari.domain.SplitTime;
import org.peimari.util.Util;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class SplitAnalyzer extends FlowPanel {
	private FlowPanel settingsPanel;

	private Grid grid;

	private List<ClassResult> classResults;

	private Widget graph;

	private boolean splits = true;
	private boolean cumutimes = false;
	private boolean inlineDifferences = false;

	private ListBox classSelector;

	private Button graphButton;

	private Collection<Result> selectedResults = new ArrayList<Result>();

	private CheckBox supermanCheckBox;
	
	public SplitAnalyzer(String url) {
		addStyleName("splitanalyzer");
		settingsPanel = new FlowPanel();
		settingsPanel.addStyleName("settings");
		add(settingsPanel);

		if (url != null) {
			getResults(url);
			settingsPanel.add(new Label("Loading..."));
			return;
		}

		final TextBox nameField = new TextBox();
		final Button sendButton = new Button("Show splits");
		nameField.setText("Splits url (TODO)");
		// We can add style names to widgets
		sendButton.addStyleName("sendButton");
		settingsPanel.add(nameField);
		settingsPanel.add(sendButton);

		// Focus the cursor on the name field when the app loads
		nameField.setFocus(true);
		nameField.selectAll();

		nameField.setValue(GWT.getHostPageBaseURL() + "tasti2.xml");

		sendButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				getResults(nameField.getValue());

			}
		});

	}

	public void setResultList(ResultList result) {
		settingsPanel.clear();

		classResults = result.getClassResults();

		classSelector = new ListBox();
		for (ClassResult classResult : classResults) {
			classSelector.addItem(classResult.getClassName());
		}
		classSelector.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				renderSelectedClass();
			}
		});

		settingsPanel.add(classSelector);
		Label l = new Label("Split times:");
		CheckBox checkBox = new CheckBox();
		settingsPanel.add(l);
		settingsPanel.add(checkBox);
		checkBox.setValue(true);
		checkBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				splits = event.getValue();
				renderSelectedClass();
			}
		});
		l = new Label("Cumulative times:");
		checkBox = new CheckBox();
		settingsPanel.add(l);
		settingsPanel.add(checkBox);
		checkBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				cumutimes = event.getValue();
				renderSelectedClass();
			}
		});

		l = new Label("Inline differences:");
		checkBox = new CheckBox();
		settingsPanel.add(l);
		settingsPanel.add(checkBox);
		checkBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				inlineDifferences = event.getValue().booleanValue();
				renderSelectedClass();
			}
		});

		setClassResults(classResults.get(0));

		graphButton = new Button("Visualize selected");
		graphButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				doVisualization();
			}

		});

		l = new Label("Visualize to 'superman':");
		String supermanExplanation = "'Superman' makes always the best time for each control. When this is selected charts baseline is the superman calculated from selected runners. Otherwise the first (in selection order) is used as baseline.";
		l.setTitle(supermanExplanation);
		supermanCheckBox = new CheckBox();
		supermanCheckBox.setTitle(supermanExplanation);
		supermanCheckBox.setValue(true);
		settingsPanel.add(l);
		settingsPanel.add(supermanCheckBox);
		supermanCheckBox
				.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

					@Override
					public void onValueChange(ValueChangeEvent<Boolean> event) {
						if (graph != null) {
							doVisualization();
						}
					}
				});

		settingsPanel.add(graphButton);

	}

	private void renderSelectedClass() {
		int selectedIndex = classSelector.getSelectedIndex();
		ClassResult classResult = classResults.get(selectedIndex);
		setClassResults(classResult);
	}

	protected void setClassResults(ClassResult classResult) {
		if (graph != null) {
			graph.removeFromParent();
		}
		selectedResults.clear();
		classResult.analyzeSplitTimes();

		List<Result> results = classResult.getResults();
		ArrayList<SplitTime> superman = new ArrayList<SplitTime>();
		calculateSuperman(results, superman);

		int size = results.get(0).getSplitTimes().size();

		long bestTime = superman.get(superman.size() - 1).getTime();

		int cols = size + 3;
		int rows = results.size() + 1; // competitors + header

		if (grid != null) {
			grid.removeFromParent();
		}
		grid = new Grid(rows, cols);

		grid.getRowFormatter().addStyleName(0, "header");

		grid.setHTML(0, 1, "Total");
		grid.setHTML(0, 2, "K-1");
		for (int i = 1; i < size; i++) {
			grid.setHTML(0, i + 2, i + "-" + (i + 1));
		}

		for (int i = 0; i < results.size(); i++) {
			final Result r = results.get(i);
			if (r.getCompetitorStatus() != CompetitorStatus.OK) {
				continue;
			}
			final int row = i + 1;
			String html = r.getPosition() + ". " + r.getPerson().toString();
			Label name = new HTML(html);
			name.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					toggleSelection(row, r);
				}
			});
			name.setTitle("Click to highlight/dehightlight");
			grid.setWidget(row, 0, name);
			grid.getCellFormatter().addStyleName(row, 0, "name");

			if (r.getCompetitorStatus() == CompetitorStatus.OK) {
				long time = r.getTime();
				grid.setHTML(row, 1, Util.formatTime(time));
			} else {
				grid.setHTML(row, 1, r.getCompetitorStatus().name());
			}

			List<SplitTime> splitTimes = r.getSplitTimes();
			for (int j = 0; j < splitTimes.size(); j++) {
				SplitTime splitTime = splitTimes.get(j);
				// Direct HTML generation for better performance performance
				StringBuilder sb = new StringBuilder();
				// TODO relative time should ignore worst n splits
				// TODO consider making split pos matter a bit

				Long superManDelta = superman.get(j).getDeltaTime();
				double expectedTime = (superManDelta * r.getTime() / (double) bestTime);
				double relativeSuccesBasedOnTime = splitTime.getDeltaTime()
						/ expectedTime;
				String styleNameForSuccess = getStyleNameForSuccess(relativeSuccesBasedOnTime);
				if (splits) {
					boolean best = splitTime.getDeltaPosition() == 1;
					long toBest = splitTime.getDeltaTime() - superManDelta;
					String toBestStr = "+" + Util.formatTime(toBest);
					if (best) {
						styleNameForSuccess += " best";
					}
					sb.append("<div class='");
					sb.append(styleNameForSuccess);
					sb.append("'");
					if (!inlineDifferences) {
						sb.append(" title='");
						sb.append(toBestStr);
						sb.append("'");
					}
					String deltaRow = ">(" + splitTime.getDeltaPosition()
							+ ") " + Util.formatTime(splitTime.getDeltaTime());
					sb.append(deltaRow);
					sb.append("</div>");

					if (inlineDifferences) {
						sb.append(toBestStr);
					}
				}
				if (cumutimes) {
					long toBest = splitTime.getTime()
							- superman.get(j).getTime();
					String toBestStr = "+" + Util.formatTime(toBest);
					sb.append("<div");
					if (!inlineDifferences) {
						sb.append(" title='");
						sb.append(toBestStr);
						sb.append("'");
					}

					sb.append(">");
					sb.append("(" + splitTime.getPosition() + ") "
							+ Util.formatTime(splitTime.getTime()));
					sb.append("</div>");
					if (inlineDifferences) {
						sb.append(toBestStr);
					}
				}

				grid.setHTML(row, j + 2, sb.toString());
			}
			if (i < 3) {
				toggleSelection(row, r);
			}
		}
		add(grid);
	}

	private String getStyleNameForSuccess(double relativeSuccesBasedOnTime) {
		if (relativeSuccesBasedOnTime > 1.25) {
			return "verybad";
		}
		if (relativeSuccesBasedOnTime > 1.1) {
			return "bad";
		}
		if (relativeSuccesBasedOnTime > 0.95) {
			return "ok";
		}
		if (relativeSuccesBasedOnTime > 0.8) {
			return "good";
		}
		return "verygood";
	}

	/**
	 * Send the name from the nameField to the server and wait for a response.
	 * 
	 * @param url
	 */
	private void getResults(String url) {
		AsyncCallback<ResultList> callback = new AsyncCallback<ResultList>() {
			public void onFailure(Throwable caught) {
				Window.alert(caught.getMessage());
			}

			@Override
			public void onSuccess(ResultList result) {
				setResultList(result);
			}
		};
		new XhrProvider().getResults(url, this);
	}

	private void toggleSelection(final int row, Result r) {
		String styleName = grid.getRowFormatter().getStyleName(row);
		if (styleName.contains("selected")) {
			grid.getRowFormatter().removeStyleName(row, "selected");
			selectedResults.remove(r);
		} else {
			grid.getRowFormatter().addStyleName(row, "selected");
			if (!selectedResults.contains(r)) {
				selectedResults.add(r);
			}
		}
	}

	private void doVisualization() {
		if (selectedResults.size() < 2) {
			Window.alert("Selelct at least two results for graph");
		} else {
			if (graph != null) {
				graph.removeFromParent();
			}
			graph = new SplitChart(selectedResults, supermanCheckBox.getValue()
					.booleanValue());
			insert(graph, 1);
		}
	}

	public static void calculateSuperman(Collection<Result> results,
			ArrayList<SplitTime> zeroLine) {
		boolean first = true;
		for (Iterator<Result> iterator = results.iterator(); iterator.hasNext();) {
			Result r = (Result) iterator.next();
			if (r.getCompetitorStatus() == CompetitorStatus.OK) {
				List<SplitTime> splitTimes = r.getSplitTimes();
				for (int i = 0; i < splitTimes.size(); i++) {
					SplitTime splitTime = splitTimes.get(i);
					if (first) {
						SplitTime s = new SplitTime();
						s.setDeltaTime(splitTime.getDeltaTime());
						zeroLine.add(s);
					} else {
						SplitTime s = zeroLine.get(i);
						if (splitTime.getDeltaTime() < s.getDeltaTime()) {
							s.setDeltaTime(splitTime.getDeltaTime());
						}
					}
				}
				first = false;
			}
		}
		long cumu = 0;
		for (SplitTime splitTime : zeroLine) {
			cumu += splitTime.getDeltaTime();
			splitTime.setTime(cumu);
		}
	}
}
