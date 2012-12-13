package ahd.wan;

import org.apache.commons.collections.Buffer;
import org.apache.commons.collections.BufferUtils;
import org.apache.commons.collections.buffer.CircularFifoBuffer;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;

import android.content.Context;
import android.widget.LinearLayout;

public class DefaultDataThread extends LinearLayout implements Runnable {


	public Buffer fifo = BufferUtils.synchronizedBuffer(new CircularFifoBuffer(30));
	private GraphViewSeries ecg = null;
	private GraphView gv = null;
	private LinearLayout ll_graphview = null;
	
	public DefaultDataThread(Context context) {
		super(context);

        gv = new LineGraphView(context, "mECG");
        gv.setViewPort(2, 40);  
        gv.setScrollable(true);  
        // optional - activate scaling / zooming  
        gv.setScalable(true); 

//        ll_graphview = (LinearLayout)findViewById(R.id.ecg_graphview);
//        ll_graphview.addView(gv);
	}
	
	public void run() {
		Data x = new Data();
		//for(int i=0;i<x.data.length;i++)
			//ecg.appendData(x.data[i], true);
		//gv.addSeries(ecg);
		fifo.add(x);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			System.out.println("DefaultDataThread-run: "+e.getMessage());
		}
	}
	
}
