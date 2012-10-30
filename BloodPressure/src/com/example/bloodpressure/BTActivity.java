package com.example.bloodpressure;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;

import javax.xml.ws.spi.http.HttpContext;

import org.apache.http.HttpRequest;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;

import com.example.HL7.*;
import com.example.XML.*;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHealth;
import android.bluetooth.BluetoothHealthAppConfiguration;
import android.bluetooth.BluetoothHealthCallback;
import android.bluetooth.BluetoothProfile;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class BTActivity extends Activity {

	private final static String TAG = "BTActivity";
	private Button start;
	private Button reg;
	private TextView val_syst;
	private TextView val_diast;
	private TextView val_map;
	private TextView val_pul;
	private TextView val_time;
	private TextView info;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice[] mAllBondedDevices;
    private BluetoothDevice mDevice;
    private BluetoothHealthAppConfiguration mHealthAppConfig;
    private BluetoothHealth mBluetoothHealth;
    private int mDeviceIndex = 0;
    private int mChannelId;
    private int count;
    private Handler h = new Handler();
    private Handler h2 = new Handler();
    private SOAPhard sh;

    private int sys;
    private int dia;
    private int map;
    private int pulse;
    private String dat;
    private byte[] invoke = new byte[2];
    
    private String msg = "";
    private String obs_dat = "";
    
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        start = (Button)findViewById(R.id.bp_start);
        reg = (Button)findViewById(R.id.bp_reg);
        val_syst = (TextView)findViewById(R.id.bp_val_syst);
        val_diast = (TextView)findViewById(R.id.bp_val_diast);
        val_map = (TextView)findViewById(R.id.bp_val_map);
        val_pul = (TextView)findViewById(R.id.bp_val_pul);
        val_time = (TextView)findViewById(R.id.bp_time);
        info = (TextView)findViewById(R.id.bp_info);

        val_syst.setText("Systole: ---");
        val_diast.setText("Diastole: ---");
        val_map.setText("MAP: ---");
        val_pul.setText("Puls: ---");
        val_time.setText("Datum: ---");
        info.setText("---");
        
        StrictMode.ThreadPolicy pol = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(pol);
        

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        
        mBluetoothAdapter.getProfileProxy(this, mBluetoothServiceListener,BluetoothProfile.HEALTH);
        info.setText("gotProfileProxy");
        
        //register
        mBluetoothHealth.registerSinkAppConfiguration(TAG, 0x1007, mHealthCallback);
        
        //connect -> select device
        start.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mAllBondedDevices =
                        (BluetoothDevice[]) mBluetoothAdapter.getBondedDevices().toArray(
                                new BluetoothDevice[0]);

                if (mAllBondedDevices.length > 0) {
                    int deviceCount = mAllBondedDevices.length;
                    if (mDeviceIndex < deviceCount) mDevice = mAllBondedDevices[mDeviceIndex];
                    else {
                        mDeviceIndex = 0;
                        mDevice = mAllBondedDevices[0];
                    }
                    String[] deviceNames = new String[deviceCount];
                    int i = 0;
                    for (BluetoothDevice device : mAllBondedDevices) {
                        deviceNames[i++] = device.getName();
                    }
                    SelectDeviceDialogFragment deviceDialog =
                            SelectDeviceDialogFragment.newInstance(deviceNames, mDeviceIndex);
                    deviceDialog.show(getFragmentManager(), "deviceDialog");
                }
            }
        });
        
        reg.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	mBluetoothHealth.disconnectChannel(mDevice, mHealthAppConfig, mChannelId);
            	//mBluetoothHealth.unregisterAppConfiguration(mHealthAppConfig);
            	info.setText("---");
            }
        });

    }//onCreate

    // Callbacks to handle connection set up and disconnection clean up.
    private final BluetoothProfile.ServiceListener mBluetoothServiceListener =
            new BluetoothProfile.ServiceListener() {
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            if (profile == BluetoothProfile.HEALTH) {
                mBluetoothHealth = (BluetoothHealth) proxy;
                Log.d(TAG, "onServiceConnected to profile: " + profile);
            }
        }

        public void onServiceDisconnected(int profile) {
            if (profile == BluetoothProfile.HEALTH) {
                mBluetoothHealth = null;
            }
        }
    };
    
    public static class SelectDeviceDialogFragment extends DialogFragment {

        public static SelectDeviceDialogFragment newInstance(String[] names, int position) {
            SelectDeviceDialogFragment frag = new SelectDeviceDialogFragment();
            Bundle args = new Bundle();
            args.putStringArray("names", names);
            args.putInt("position", position);
            frag.setArguments(args);
            return frag;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            String[] deviceNames = getArguments().getStringArray("names");
            int position = getArguments().getInt("position", -1);
            if (position == -1) position = 0;
            return new AlertDialog.Builder(getActivity())
                    .setTitle("gerät wählen")
                    .setPositiveButton("ok",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                ((BTActivity) getActivity()).connectChannel();
                            }
                        })
                    .setSingleChoiceItems(deviceNames, position,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                ((BTActivity) getActivity()).setDevice(which);
                            }
                        }
                    )
                    .create();
        }
    }//SelectDeviceDialogFragment
    
    public void setDevice(int position) {
        mDevice = this.mAllBondedDevices[position];
        mDeviceIndex = position;
    }
    
    public void connectChannel() {
    	mBluetoothHealth.connectChannelToSource(mDevice, mHealthAppConfig);
    }
    
    private final BluetoothHealthCallback mHealthCallback = new BluetoothHealthCallback() {
        // Callback to handle application registration and unregistration events.  The service
        // passes the status back to the UI client.
        public void onHealthAppConfigurationStatusChange(BluetoothHealthAppConfiguration config,
                int status) {

            Log.i(TAG, "***chgReg");
            if (status == BluetoothHealth.APP_CONFIG_REGISTRATION_FAILURE) {
                mHealthAppConfig = null;
                info.setText("reg fail");
            } else if (status == BluetoothHealth.APP_CONFIG_REGISTRATION_SUCCESS) {
                mHealthAppConfig = config;
                info.setText("reg suc");
            }
        }

        // Callback to handle channel connection state changes.
        // Note that the logic of the state machine may need to be modified based on the HDP device.
        // When the HDP device is connected, the received file descriptor is passed to the
        // ReadThread to read the content.
        public void onHealthChannelStateChange(BluetoothHealthAppConfiguration config,
                BluetoothDevice device, int prevState, int newState, ParcelFileDescriptor fd,
                int channelId) {

        Log.i(TAG, "***chgChan");
            Log.d(TAG, String.format("prevState\t%d ---> newState\t%d",
                    prevState, newState));
            if (prevState == BluetoothHealth.STATE_CHANNEL_DISCONNECTED &&
                    newState == BluetoothHealth.STATE_CHANNEL_CONNECTED) {
                Log.i(TAG, "***connected");
                if (config.equals(mHealthAppConfig)) {
                    mChannelId = channelId;
                    Log.i(TAG, "***connected+config(disc)");
                    //info.setText("con suc");
                    (new ReadThread(fd)).start();
                } else {
                    Log.i(TAG, "***connected-config(disc)");
                    //info.setText("conf fail");
                }
            } else if (prevState == BluetoothHealth.STATE_CHANNEL_CONNECTING &&
                       newState == BluetoothHealth.STATE_CHANNEL_DISCONNECTED) {
                Log.i(TAG, "***connect failed");
                //info.setText("con fail");
            } else if (prevState == BluetoothHealth.STATE_CHANNEL_CONNECTING &&
                    newState == BluetoothHealth.STATE_CHANNEL_CONNECTED) {
            	if (config.equals(mHealthAppConfig)) {
                mChannelId = channelId;
                Log.i(TAG, "***connected+config(con)");
                //info.setText("con suc");
                (new ReadThread(fd)).start();
            	}
            	else {
            		Log.i(TAG, "***connected-config(con)");
                    //info.setText("conf fail");
            	}
            }
        }
    };//BluetoothHealthCallback
    
    
    
    private class ReadThread extends Thread {
        private ParcelFileDescriptor mFd;

        public ReadThread(ParcelFileDescriptor fd) {
            super();
            mFd = fd;
        }

        @Override
        public void run() {
            FileInputStream fis = new FileInputStream(mFd.getFileDescriptor());
            byte data[] = new byte[300];
            try {

                Log.d(TAG, "***reading");
                //info.setText("reading");
                while(fis.read(data) > -1) {
                    // At this point, the application can pass the raw data to a parser that
                    // has implemented the IEEE 11073-xxxxx specifications.  Instead, this sample
                    // simply indicates that some data has been received.
                	if (data[0] != (byte) 0x00)
                    {
                        String test = byte2hex(data);
                        Log.i(TAG, test);
                        if(data[0] == (byte) 0xE2){	//associating
                            Log.i(TAG, "E2_1");
                            
                            count = 1;
                            (new WriteThread(mFd)).start();
                            try {
                                sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            Log.i(TAG, "E2_2");
                            count = 2;
                            (new WriteThread(mFd)).start();
                        }
                        else if (data[0] == (byte)0xE7){	//associated -> operating|configuring
                            Log.i(TAG, "E7");

                            if (data[26] == (byte) 0x10 && data[27] == (byte) 0x07)  //mds response ?
                            {
                            	Log.i(TAG, "mds response"); 
                            }
                            else if (data[18] == (byte) 0x0d && data[19] == (byte) 0x1d)  //operating
                            {
                            	Log.i(TAG, "operating"); 
                                count = 3; 
                                invoke[0] = data[6];
                                invoke[1] = data[7];
                                (new WriteThread(mFd)).start(); 
                                
                                //parse data!!
                                int obs = data[27];
                                int pos = 31;
                                for (int i=0; i<obs; i++)
                                {
                                	int handle = data[pos];
                                	switch(handle)
                                	{
                                	case 1: //handle1 (sys/dia/map)
                                        sys = byteToUnsignedInt(data[pos+8]);
                                        dia = byteToUnsignedInt(data[pos+10]);
                                        map = byteToUnsignedInt(data[pos+12]);
                                        dat = String.format("%02X", data[pos+13])+
                                        		String.format("%02X", data[pos+14])+
                                        		"-"+String.format("%02X", data[pos+15])+
                                        		"-"+String.format("%02X", data[pos+16])+
                                        		" "+String.format("%02X", data[pos+17])+
                                        		":"+String.format("%02X", data[pos+18])+
			                                	":"+String.format("%02X", data[pos+19]);
                                        obs_dat = String.format("%02X", data[pos+13])+
                                        		String.format("%02X", data[pos+14])+
                                        		String.format("%02X", data[pos+15])+
                                        		String.format("%02X", data[pos+16])+
                                        		String.format("%02X", data[pos+17])+
                                        		String.format("%02X", data[pos+18])+
			                                	String.format("%02X", data[pos+19]);
                                        
                                        pos += 22;
                                        break;
                                	case 2: //handle2 (pulse)
                                		pulse = byteToUnsignedInt(data[pos+4]);
                                		pos += 14;
                                		
                                		Log.i(TAG, sys+"/"+dia+", "+map+" - "+pulse);
                                        Log.i(TAG, dat);
                                        
                                        h.post(new Runnable() {
                                        	public void run() {
        								        val_syst.setText("Systole: "+sys);
        								        val_diast.setText("Diastole: "+dia);
        								        val_map.setText("MAP: "+map);
        								        val_pul.setText("Puls: "+pulse);
        								        val_time.setText("Datum: "+dat);
        								        
        								        sendData();
        									}
                                        });
                                	}

                                    
                                }
                                
                                
                            }
                            else if (data[18] == (byte) 0x0d && data[19] == (byte) 0x1c)	//configuring
                            {
                            	Log.i(TAG, "configuring"); 
                            	count = 5;
                                (new WriteThread(mFd)).start();
                            }
                        }
                        else if (data[0] == (byte) 0xE4 || data[0] == (byte) 0xE6)
                        {
                            count = 4;
                            (new WriteThread(mFd)).start();
                        }
                        
                        //zero out the data
                        /*for (int i = 0; i < data.length; i++){
                            data[i] = (byte) 0x00;
                        }*/
                        data = new byte[300];
                    }
                }
            } catch(IOException ioe) {
                Log.i(TAG, "exception read, "+ioe.getMessage());}
            if (mFd != null) {
                try {
                    mFd.close();
                    Log.i(TAG, "reader closed");
                } catch (IOException e) {}
            }
            Log.i(TAG, "data read");
            //info.setText("data read");
        }
    }
    
    public static String byte2hex(byte[] bs) {
        StringBuffer ret = new StringBuffer(bs.length);
        for (int i = 0; i < bs.length; i++) {
            String hex = Integer.toHexString(0x0100 + (bs[i] & 0x00FF)).substring(1);
            ret.append((hex.length() < 2 ? "0" : "") + hex);
        }
        return ret.toString();
    }//byte2hex
    
    public static int byteToUnsignedInt(byte b) {
        return 0x00 << 24 | b & 0xff;
      }
    
    private class WriteThread extends Thread {
        private ParcelFileDescriptor mFd;

        public WriteThread(ParcelFileDescriptor fd) {
            super();
            mFd = fd;
        }

        @Override
        public void run() {
            FileOutputStream fos = new FileOutputStream(mFd.getFileDescriptor());
            final byte data_AR[] = new byte[] {         (byte) 0xE3, (byte) 0x00,
                                                        (byte) 0x00, (byte) 0x2C, 
                                                        (byte) 0x00, (byte) 0x00,
                                                        (byte) 0x50, (byte) 0x79,
                                                        (byte) 0x00, (byte) 0x26,
                                                        (byte) 0x80, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                                                        (byte) 0x80, (byte) 0x00,
                                                        (byte) 0x80, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                                                        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                                                        (byte) 0x80, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                                                        (byte) 0x00, (byte) 0x08,
                                                        (byte) 0x00, (byte) 0x09, (byte) 0x1F, (byte) 0xFF, 
                                                        (byte) 0xFE, (byte) 0x80, (byte) 0x06, (byte) 0x1C,
                                                        (byte) 0x00, (byte) 0x00,
                                                        (byte) 0x00, (byte) 0x00,
                                                        (byte) 0x00, (byte) 0x00, 
                                                        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};
            
            final byte data_DR[] = new byte[] {         (byte) 0xE7, (byte) 0x00,
                                                        (byte) 0x00, (byte) 0x12,
                                                        (byte) 0x00, (byte) 0x10,
                                                        invoke[0], invoke[1],
                                                        (byte) 0x02, (byte) 0x01,
                                                        (byte) 0x00, (byte) 0x0A,
                                                        (byte) 0x00, (byte) 0x00,
                                                        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                                                        (byte) 0x0D, (byte) 0x1D,
                                                        (byte) 0x00, (byte) 0x00 };

            final byte get_MDS[] = new byte[] {         (byte) 0xE7, (byte) 0x00,
                                                        (byte) 0x00, (byte) 0x0E,
                                                        (byte) 0x00, (byte) 0x0C,
                                                        (byte) 0x00, (byte) 0x24,
                                                        (byte) 0x01, (byte) 0x03,
                                                        (byte) 0x00, (byte) 0x06,
                                                        (byte) 0x00, (byte) 0x00,
                                                        (byte) 0x00, (byte) 0x00,
                                                        (byte) 0x00, (byte) 0x00 };
            
            final byte get_Conf[] = new byte[] {        (byte) 0xE7, (byte) 0x00,
									                    (byte) 0x00, (byte) 0x16,
									                    (byte) 0x00, (byte) 0x14,
									                    (byte) 0x00, (byte) 0x53,
									                    (byte) 0x02, (byte) 0x01,
									                    (byte) 0x00, (byte) 0x0E,
									                    (byte) 0x00, (byte) 0x00,
									                    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
									                    (byte) 0x0D, (byte) 0x1C,
									                    (byte) 0x00, (byte) 0x04,
									                    (byte) 0x40, (byte) 0x00,
									                    (byte) 0x00, (byte) 0x00 };

            final byte data_RR[] = new byte[] {         (byte) 0xE5, (byte) 0x00,
                                                        (byte) 0x00, (byte) 0x02,
                                                        (byte) 0x00, (byte) 0x00 };

            try {
                Log.i(TAG, String.valueOf(count));
                if (count == 1)
                {
                    fos.write(data_AR);
                    Log.i(TAG, "Association Responsed!");
                    fos.close();
                }  
                else if (count == 2)
                {
                    fos.write(get_MDS);
                    Log.i(TAG, "Get MDS object attributes!");
                    fos.close();
                }
                else if (count == 3) 
                {
                    fos.write(data_DR);
                    Log.i(TAG, "Data Responsed!");
                    fos.close();
                }
                else if (count == 4)
                {
                    fos.write(data_RR);
                    Log.i(TAG, "Data Released!");
                    fos.close();
                }
                else if (count == 5)
                {
                    fos.write(get_Conf);
                    Log.i(TAG, "Config Reponsed!");
                    fos.close();
                }
            } catch(IOException ioe) {
                Log.i(TAG, "exception write");}
        }
    }//WriteThread
    
    
    public void sendData() {
    	Calendar cal = Calendar.getInstance();
    	String time = ""+cal.get(Calendar.YEAR)+""+(cal.get(Calendar.MONTH)+1)+""+cal.get(Calendar.DAY_OF_MONTH)+""+cal.get(Calendar.HOUR_OF_DAY)+
    			""+cal.get(Calendar.MINUTE)+""+cal.get(Calendar.SECOND)+"+0200";
    			
    	MSH m = new MSH("HIO_DOR",time,"msgid123","IHE PCD ORU-R01 2006^HL7^2.16.840.1.113883.9.n.m^HL7");
    	PID p = new PID("1","171122869^^^","Starek^Christian^^BSc^","Starek^Dagmar^^^^","19890727000000","M");
    	OBR rep = new OBR("1","pon123","fon123","528391^MDC_DEV_SPEC_PROFILE_BP^MDC",obs_dat);
    	ArrayList<OBX> res = new ArrayList<OBX>();
    	OBX tim = new OBX("1","CWE","68220^MDC_TIME_SYNC_PROTOCOL^MDC","0.0.0.1","","532224^MDC_TIME_SYNC_NONE^MDC","R","","");
    	OBX r1 = new OBX("2","","528391^MDC_DEV_SPEC_PROFILE_BP^MDC","1","","","X","","FHTW");
    	OBX r2 = new OBX("3","","150020^MDC_PRESS_BLD_NONINV^MDC","1.0.1","","","X",obs_dat,"");
    	OBX r3 = new OBX("4","NM","150021^MDC_PRESS_BLD_NONINV_SYS^MDC","1.0.1.1",sys+"","266016^MDC_DIM_MMHG^MDC","R","","");
    	OBX r4 = new OBX("5","NM","150022^MDC_PRESS_BLD_NONINV_DIA^MDC","1.0.1.2",dia+"","266016^MDC_DIM_MMHG^MDC","R","","");
    	OBX r5 = new OBX("6","NM","150023^MDC_PRESS_BLD_NONINV_MEAN^MDC","1.0.1.3",map+"","266016^MDC_DIM_MMHG^MDC","R","","");
    	OBX r6 = new OBX("7","NM","149546^MDC_PULS_RATE_NON_INV^MDC","1.0.0.1",pulse+"","264864^MDC_DIM_BEAT_PER_MIN^MDC","R","","");
    	res.add(tim);
    	res.add(r1);
    	res.add(r2);
    	res.add(r3);
    	res.add(r4);
    	res.add(r5);
    	res.add(r6);
    	HL7Message hl = new HL7Message(m, p, rep, res);
    	sh = new SOAPhard(hl.getHL7Message());
    	msg = sh.getSoap();
    	
    	Log.d(TAG,msg);
    	info.setText("soap created");
    	
    	try {
			sh.saveSoapTxt("/sdcard/soap.txt");
	    	info.setText("soap saved");	
		} catch (IOException e) {
			Log.e(TAG, e.getMessage());
    		Toast t = Toast.makeText(this, "Schreibfehler IO", 3);
    		t.show();
		}
    	
    	
		try {
			h2.post(new Runnable() {
	        	public void run() {
	    			Log.i(TAG, sh.sendSoapHttp().toString());
	    			info.setText("soap sent");	
				}
	        });
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
    		Toast t = Toast.makeText(this, "Sendefehler SOAP_HTTP", 3);
    		t.show();
		}

    }
    
 


}//class
