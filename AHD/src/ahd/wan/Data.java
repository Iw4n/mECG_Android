package ahd.wan;

import java.util.Calendar;

import com.jjoe64.graphview.GraphView.GraphViewData;

public class Data {

	public long timestamp = 0;
	public GraphViewData[] data = new GraphViewData[1000];
	public double count = 0;
	
	public Data(long ts, GraphViewData[] d) {
		timestamp = ts;
		data = d;
	}
	
	public Data() {
		Calendar cal = Calendar.getInstance();
		timestamp = cal.getTimeInMillis();
		for(int i=0;i<1000;i++)
		{
			data[i] = new GraphViewData(count,(Math.random()*1023));
			count++;
		}
	}
	
	public String getData() {
		String d = "\n##data:";
		for(int i=0;i<data.length;i++) {
			d+=data[i].valueY+";";
		}
		return d;
	}

}
