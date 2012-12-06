package ahd.wan;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.LoggingPermission;

import org.apache.commons.collections.Buffer;
import org.apache.commons.collections.BufferUtils;
import org.apache.commons.collections.buffer.CircularFifoBuffer;

import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewSeries;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class TestWanActivity extends Activity {

	private int recTime = 30;
	private int runState = 0;
	private Button btn_save = null;
	private Button btn_start = null;
	private TextView tv_status = null;
	
	private Data[] data = new Data[recTime];
	private final String tag = "TestWanActivity";
	
	private final int MENU_30rec = Menu.FIRST;
	private final int MENU_10rec = Menu.FIRST+1;
	private final int MENU_5rec = Menu.FIRST+2;
	
	private DefaultDataThread ddt = new DefaultDataThread();
	private Thread t = new Thread(ddt);
	
	private GraphViewData[] gvd = new GraphViewData[10000];
	private GraphViewSeries ecg = new GraphViewSeries(gvd);
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ecg);
        
        
        tv_status = (TextView)findViewById(R.id.ecg_status);
        btn_save = (Button)findViewById(R.id.ecg_save);
        btn_start = (Button)findViewById(R.id.ecg_start);
        
        btn_start.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	if(runState==0) {
	            	t.start();
	            	
	            	runState=1;
	            	tv_status.setText("running");
            	}
            	else
            	{
            		runState=0;
	            	tv_status.setText("idle");
	            	t.interrupt();
            	}
            }
        });
        
        btn_save.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	saveSD();
            }
        });
        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	menu.add(0, MENU_30rec, 0, "30s");
    	menu.add(0, MENU_10rec, 0, "10s");
    	menu.add(0, MENU_5rec, 0, "5s");
        return true;
    }
    
	public boolean onOptionsItemSelected(MenuItem item) {    
		switch (item.getItemId()) {    
			case MENU_30rec:        
				recTime = 30;
				data = new Data[recTime];
				return true;    
			case MENU_10rec:        
				recTime = 10;
				data = new Data[recTime];
				return true;      
			case MENU_5rec:        
				recTime = 5;
				data = new Data[recTime];
				return true;   
		}    
		return false;
	}
    
    public boolean open() {
    	try{
    	    	
			StringBuilder sb = new StringBuilder("");
	    	File filename = new File("/sdcard/ecg.txt");
	    	if(!filename.exists())
	    	{
	    		Toast t = Toast.makeText(this, "Datei nicht gefunden", 5);
	    		t.show();
	    		return false;
	    	}
	    	else
	    	{
		    	String ls = System.getProperty("line.separator");
		    	
		    	FileReader fos = new FileReader(filename);
		    	BufferedReader osw = new BufferedReader(fos);
		    	
		    	String line = "";
		    	int i=0;
		    	while((line=osw.readLine()) != null || i<1000) {
		    		//data[i]=Integer.parseInt(line);
		    		i++;
		    	}
		    	
		    	osw.close();
		    	fos.close();
		    	
		    	return true;
	    	}
	    
    	}catch(IOException e)
    	{
    		Log.e(tag, "open: "+e.getMessage());
    		return false;
    	}
    }
    
    public boolean save() {
    	DBConnection con = new DBConnection("localhost", "mecg", "prt", "prt");
    	
    	String cre = "INSERT INTO dev1 (pid,data) VALUES (1,'";
		for(int i=0;i<data.length;i++)
		{
			cre+=""+data[i]+",";
		}
		cre = cre.substring(0, cre.length()-1)+"');";
		System.out.println(cre);
		int c = con.sql_update(cre);
		if(c==1)
			return true;
		else
			return false;
		
    }
    
    public boolean saveSD() {
    	try{
	    	
			StringBuilder sb = new StringBuilder("");
	    	File filename = new File("/sdcard/ecg.txt");
	    	if(!filename.exists())
	    	{
	    		filename.createNewFile();
	    		Toast t = Toast.makeText(this, "Datei nicht gefunden", 5);
	    		t.show();
	    		return false;
	    	}
	    	else
	    	{
		    	String ls = System.getProperty("line.separator");
		    	
		    	FileWriter fos = new FileWriter(filename);
		    	BufferedWriter osw = new BufferedWriter(fos);
		    	
		    	String line = "";

            	for(int i=0; i<recTime;i++) {
            		data[i] = (Data) ddt.fifo.get();
            		osw.append(data[i].getData());
            	}
		    	
		    	osw.close();
		    	fos.close();
		    	
		    	tv_status.setText("saved");
		    	return true;
	    	}
	    
    	}catch(IOException e)
    	{
    		Log.e(tag, "TestWanActivity-save: "+e.getMessage());
    		return false;
    	}
    }
}
