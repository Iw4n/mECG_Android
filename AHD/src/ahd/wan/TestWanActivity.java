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

import com.example.cardiograph.R;
import com.example.cardiograph.R.layout;
import com.example.cardiograph.R.menu;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class TestWanActivity extends Activity {

	private int recTime = 30;
	private Button btn_save = null;
	private int[] data = new int[recTime*1000];
	private final String tag = "TestWanActivity";
	private Buffer fifo = BufferUtils.synchronizedBuffer(new CircularFifoBuffer(30));
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_wan);
        
        
        
        btn_save = (Button)findViewById(R.id.test_wan_btn_save);
        
        btn_save.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	if(open())//later replaced with get Data from actual stream
            		save();
            }
        });
        
    }
    
    public void createData() {
    	fifo.add(new Data());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	getMenuInflater().inflate(R.menu.activity_test_wan, menu);
    	menu.add(0, Menu.FIRST, 0, "Quit");
    	menu.add(0, MENU_QUIT, 0, "Quit")
        return true;
    }
    
    public boolean onOptionsItemSelected(MenuItem item) {    
    	switch (item.getItemId()) {    
    		case MENU_NEW_GAME:        
    			newGame();        
    			return true;    
    		case MENU_QUIT:        
    			quit();        
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
		    		data[i]=Integer.parseInt(line);
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
}
