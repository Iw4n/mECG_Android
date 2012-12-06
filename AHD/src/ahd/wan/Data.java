package ahd.wan;

import java.util.Calendar;

public class Data {

	public long timestamp = 0;
	public int[] data = new int[1000];
	
	public Data(long ts, int[] d) {
		timestamp = ts;
		data = d;
	}
	
	public Data() {
		Calendar cal = Calendar.getInstance();
		timestamp = cal.getTimeInMillis();
		for(int i=0;i<1000;i++)
			data[i] = (int) (Math.random()*1023);
	}

}
