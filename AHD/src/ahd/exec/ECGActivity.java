package ahd.exec;


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
import java.util.ArrayList;
import java.util.Calendar;

import com.example.cardiograph.R;


import ahd.basics.hl7.*;
import ahd.basics.xml.*;
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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class ECGActivity extends Activity {

	private final static String TAG = "BTActivity";
	//blood pressure UI elements
	private Button start;
	private Button reg;
	/*private TextView val_syst;
	private TextView val_diast;
	private TextView val_map;
	private TextView val_pul;
	private TextView val_time;
	private TextView info;
	*/
	
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice[] mAllBondedDevices;
    private BluetoothDevice mDevice;
    private BluetoothHealthAppConfiguration mHealthAppConfig;
    private BluetoothHealth mBluetoothHealth;
    private int mDeviceIndex = 0;
    private int mChannelId;
    private int count;
    private Handler h = new Handler();


    //Variables for HR
    private byte[] invoke_id = new byte[2];
    private int BPM;
    private String BPMRelativeTimeStamp;
    private int Ticks1;
    private String Ticks1RelativeTimeStamp;
    private int Ticks2;
    private String Ticks2RelativeTimeStamp;
    
    //Variables for Blood Pressure
    /**private int sys;
    private int dia;
    private int map;
    private int pulse;
    private String dat;
    private byte[] invoke = new byte[2];
    */
    
    //for HL7 message
    private String msg = "";
    private String obs_dat = "";
    
    
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //set view on blood pressure
        setContentView(R.layout.activity_main);
        //initialize the blood pressure UI
        /**
         * start = (Button)findViewById(R.id.bp_start);
         * reg = (Button)findViewById(R.id.bp_reg);
         * val_syst = (TextView)findViewById(R.id.bp_val_syst);
         * val_diast = (TextView)findViewById(R.id.bp_val_diast);
         * val_map = (TextView)findViewById(R.id.bp_val_map);
         * val_pul = (TextView)findViewById(R.id.bp_val_pul);
         * val_time = (TextView)findViewById(R.id.bp_time);
         * info = (TextView)findViewById(R.id.bp_info);
         * val_syst.setText("Systole: ---");
         * val_diast.setText("Diastole: ---");
         * val_map.setText("MAP: ---");
         * val_pul.setText("Puls: ---");
         * val_time.setText("Datum: ---");
         * info.setText("---");
        */
        //setContentView(R.layout.activity_ecg);
        
        //BluetoothAdapter is the entry point for the bluetooth interaction 
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        
        //get the profile ==> here HDP / HEALTH
        mBluetoothAdapter.getProfileProxy(this, mBluetoothServiceListener,BluetoothProfile.HEALTH);
        //TODO: display status message elsehwere
        //info.setText("gotProfileProxy");
        
        //register
        //sink is the smart device that receives the medical data
        //ECG MPED ID as in ieee-11073-10406 page 62 define MDC_DEV_SPEC_PROFILE_ECG 4102
        //0x1006 ==> basic ECG
        //if true callback will be called
        
        mBluetoothHealth.registerSinkAppConfiguration(TAG, 0x1006, mHealthCallback);
        
        //connect -> select device
       
        /**
         * Dialog to display a list of bonded Bluetooth devices for user to select from.  This is
         * needed only for channel connection initiated from the application.
         */
        //TODO: change to corresponding button in ECG UI
        //start button in BloodPressure UI
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
        
        //TODO: change unregister button to ECG UI button
        reg.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	mBluetoothHealth.disconnectChannel(mDevice, mHealthAppConfig, mChannelId);
            	//mBluetoothHealth.unregisterAppConfiguration(mHealthAppConfig);
            	//TODO: display status elsewhere
            	
            	//info.setText("---");
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
                    .setTitle("Gerät wählen")
                    .setPositiveButton("ok",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                ((ECGActivity) getActivity()).connectChannel();
                            }
                        })
                    .setSingleChoiceItems(deviceNames, position,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                ((ECGActivity) getActivity()).setDevice(which);
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
                //TODO: show register and unregister status ==> maybe use toast instead of text field
                //info.setText("reg fail");
            } else if (status == BluetoothHealth.APP_CONFIG_REGISTRATION_SUCCESS) {
                mHealthAppConfig = config;
                //TODO: show register and unregister status ==> maybe use toast instead of text field
                //info.setText("reg suc");
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

        //TODO: change read thread to read the ECG data
        @Override
        public void run() {
        	
            FileInputStream fis = new FileInputStream(mFd.getFileDescriptor());
            byte data[] = new byte[300];
            try {
            
                Log.d(TAG, "***reading");
                //info.setText("reading");
                while(fis.read(data) > -1) {
                	//check the agents response messages
                	if(data[0] != (byte)0x00)
                	{
                		String test = byte2hex(data);
                		if(data[0] == (byte)0xE2)
                		{ 	//first byte in the agents response is 0xE2 ==> associating
                			//count for LOG
                			Log.i(TAG, "E2_1");
                			count = 1;
                			(new WriteThread(mFd)).start();
                			try {
                				sleep(100);
                			}catch (InterruptedException e){
                				e.printStackTrace();
                			}
                			Log.i(TAG, "E2_2");
                			count = 2;
                			(new WriteThread(mFd)).start();
                		}
                	}
                	else if(data[0] == (byte)0xE7)//config / operating
                	{
                		Log.i(TAG, "E7");
                		//check if we have a MDS response! starts also with 0xE7
                		//11073_10406_2012 E.4.1.3 page 75
                		if(data[26] == (byte)0x10 && data[27] == (byte)0x8D) //mds response for type MDC_DEV_SUB_SPEC_PROFILE_HR
                		{
                			Log.i(TAG, "MDS response received");
                		}
                		//check data reporting/operating 11073_10406_2012 E5 page 76
                		else if(data[18] == (byte)0x0D && data[19] == (byte)0x1D)//event-type = MDC_NOTI_SCAN_REPORT_FIXED
                		{
                			//get the ECG data
                			Log.i(TAG, "data reporting");
                			//parser for the basic data reporting message ==> maybe our agent implements a different method in that case
                			//TODO: adept the parser! ==> we need to have a "Confirmed" measurement data transmission!
                			
                			invoke_id[0]= data[6];
                			invoke_id[1] = data[7];
                			(new WriteThread(mFd)).start();
                			
                			 int NumberOfObservations = data[27]; //ScanReportInfoFixed.obs-scan-fixed.count = 3
                			 int Position = 31; //data[31] = first handle 
                			 
                			 for (int i=0; i<NumberOfObservations; i++)
                			 {
                				int CurrentHandle = data[Position];
                				switch(CurrentHandle)
                				{
	                				case 1: //handle1 
	                					
	                					//get BPM value
	                					BPM = byteToUnsignedInt(data[Position+4]);
	                					//TODO: timestamp POS+5/6/7/8
	                					//BPMRelativeTimeStamp = byteToUnsignedInt(data[Position+5]);
	                					//set Position to next handle
	                					Position += 10;
	                					break;
	                				case 2: //handle2
	                					Ticks1 = byteToUnsignedInt(data[Position+4]);
	                					//TODO: timestamp
	                					//set Position to next handle
	                					Position +=10;
	                					break;
	                				case 3: //handle3
	                					Ticks2 = byteToUnsignedInt(data[Position+4]);
	                					break;
                				}
                			 }
                		}
                		else if(data[18] == (byte) 0x0d && data[19] == (byte) 0x1c)	//configuring
                		{
                			Log.i(TAG,"Configuration");
                			(new WriteThread(mFd)).start();
                		}
                	}
                	else if(data[0] == (byte) 0xE4 || data[0] == (byte) 0xE6)
                	{
                		(new WriteThread(mFd)).start();
                	}
//                    // At this point, the application can pass the raw data to a parser that
//                    // has implemented the IEEE 11073-xxxxx specifications.  Instead, this sample
//                    // simply indicates that some data has been received.
//                	if (data[0] != (byte) 0x00)
//                    {
//                        String test = byte2hex(data);
//                        Log.i(TAG, test);
//                        if(data[0] == (byte) 0xE2){	//associating
//                            Log.i(TAG, "E2_1");
//                            
//                            count = 1;
//                            (new WriteThread(mFd)).start();
//                            try {
//                                sleep(100);
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            }
//                            Log.i(TAG, "E2_2");
//                            count = 2;
//                            (new WriteThread(mFd)).start();
//                        }
//                        else if (data[0] == (byte)0xE7)
//                		  {	//associated -> operating|configuring
//                            Log.i(TAG, "E7");
//
//                            if (data[26] == (byte) 0x10 && data[27] == (byte) 0x07)  //mds response ?
//                            {
//                            	Log.i(TAG, "mds response"); 
//                            }
//                            else if (data[18] == (byte) 0x0d && data[19] == (byte) 0x1d)  //operating
//                            {
//                            	Log.i(TAG, "operating"); 
//                                count = 3; 
//                                invoke[0] = data[6];
//                                invoke[1] = data[7];
//                                (new WriteThread(mFd)).start(); 
//                                
//                                //parse data!!
//                                int obs = data[27];
//                                int pos = 31;
//                                for (int i=0; i<obs; i++)
//                                {
//                                	int handle = data[pos];
//                                	switch(handle)
//                                	{
//                                	case 1: //handle1 (sys/dia/map)
//                                        sys = byteToUnsignedInt(data[pos+8]);
//                                        dia = byteToUnsignedInt(data[pos+10]);
//                                        map = byteToUnsignedInt(data[pos+12]);
//                                        dat = String.format("%02X", data[pos+13])+
//                                        		String.format("%02X", data[pos+14])+
//                                        		"-"+String.format("%02X", data[pos+15])+
//                                        		"-"+String.format("%02X", data[pos+16])+
//                                        		" "+String.format("%02X", data[pos+17])+
//                                        		":"+String.format("%02X", data[pos+18]);
//                                        obs_dat = String.format("%02X", data[pos+13])+
//                                        		String.format("%02X", data[pos+14])+
//                                        		String.format("%02X", data[pos+15])+
//                                        		String.format("%02X", data[pos+16])+
//                                        		String.format("%02X", data[pos+17])+
//                                        		String.format("%02X", data[pos+18]);
//                                        
//                                        pos += 22;
//                                        break;
//                                	case 2: //handle2 (pulse)
//                                		pulse = byteToUnsignedInt(data[pos+4]);
//                                		pos += 14;
//                                	}
//
//                                    Log.i(TAG, sys+"/"+dia+", "+map+" - "+pulse);
//                                    Log.i(TAG, dat);
//                                }
//                                
//                                
//                                h.post(new Runnable() {
//                                	public void run() {
//								        val_syst.setText("Systole: "+sys);
//								        val_diast.setText("Diastole: "+dia);
//								        val_map.setText("MAP: "+map);
//								        val_pul.setText("Puls: "+pulse);
//								        val_time.setText("Datum: "+dat);
//								        
//								        sendData(); //send via SOAP
//									}
//                                });
//                            }
//                            else if (data[18] == (byte) 0x0d && data[19] == (byte) 0x1c)	//configuring
//                            {
//                            	Log.i(TAG, "configuring"); 
//                            	count = 5;
//                                (new WriteThread(mFd)).start();
//                            }
//                        }
//                        else if (data[0] == (byte) 0xE4 || data[0] == (byte) 0xE6)
//                        {
//                            count = 4;
//                            (new WriteThread(mFd)).start();
//                        }
//                        
//                        //zero out the data
//                        /*for (int i = 0; i < data.length; i++){
//                            data[i] = (byte) 0x00;
//                        }*/
//                        data = new byte[300];
//                    }
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
    
    
    
    //write thread class
    private class WriteThread extends Thread {
        private ParcelFileDescriptor mFd;

        public WriteThread(ParcelFileDescriptor fd) {
            super();
            mFd = fd;
        }

        @Override
        public void run() {
            FileOutputStream fos = new FileOutputStream(mFd.getFileDescriptor());
            
            //TODO: hardcoded? can we keep only the case that the config is known or do we need to implement for later the case that the device sends us the config?
            
         
            //TODO: next manager messages
            
            //associate state
            //ECG association response config unknown
            //manage response that it can associate but does not have the basic ECG extended config
            final byte data_AssocRespConfigUnknown[] = new byte []{	
            		
            											(byte) 0xE3, (byte) 0x00,/*APDU Choice Type (AareApdu)*/
            											(byte) 0x00, (byte) 0x2C,/*Choice.length = 44*/
            											(byte) 0x00, (byte) 0x03,/*result accepted-unknown-config*/
            											(byte) 0x50, (byte) 0x79,/*data-proto-id = 20601*/
            											(byte) 0x00, (byte) 0x26,/*data-proto-info length = 38*/
            											(byte) 0x40, (byte) 0x00, (byte) 0x00, (byte) 0x00, /*protocolVersion*/
            											(byte) 0x80, (byte) 0x00, /*encoding rules = MDER*/
            											(byte) 0x80, (byte) 0x00, (byte) 0x00, (byte) 0x00, /*nomenclatureVersion*/
            											(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, /*functionalUnits - normal Association*/
            											(byte) 0x80, (byte) 0x00, (byte) 0x00, (byte) 0x00, /*systemType = sys-type-manager*/
            											(byte) 0x00, (byte) 0x08, /*system-id length = 8 and value (manufacturer- and device specific TODO CARE!?*/
            											(byte) 0x38, (byte) 0x37, (byte) 0x36, (byte) 0x35, /* is this manufacturer and device specific? TODO what to choose here?*/
            											(byte) 0x34, (byte) 0x33, (byte) 0x32, (byte) 0x31, 
            											(byte) 0x00, (byte) 0x00, /*manager's response to config-id is always 0*/
            											(byte) 0x00, (byte) 0x00, /*data-req-mode-flags*/
            											(byte) 0x00, (byte) 0x00, /*data-req-init-agent-count = 0| data-req-init-manager-count = 0*/
            											(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00 /*optionList.count = 0 | optionList.length = 0*/
            };
            
            
            //ECG association response extended config known
            
            final byte data_AssocRespExtendedConfigKnown[] = new byte []{ 
            
														(byte) 0xE3, (byte) 0x00,/*APDU Choice Type (AareApdu)*/
														(byte) 0x00, (byte) 0x2C,/*Choice.length = 44*/
														(byte) 0x00, (byte) 0x00,/*result accepted*/
														(byte) 0x50, (byte) 0x79,/*data-proto-id = 20601*/
														(byte) 0x00, (byte) 0x26,/*data-proto-info length = 38*/
														(byte) 0x40, (byte) 0x00, (byte) 0x00, (byte) 0x00, /*protocolVersion*/
														(byte) 0x80, (byte) 0x00, /*encoding rules = MDER*/
														(byte) 0x80, (byte) 0x00, (byte) 0x00, (byte) 0x00, /*nomenclatureVersion*/
														(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, /*functionalUnits - normal Association*/
														(byte) 0x80, (byte) 0x00, (byte) 0x00, (byte) 0x00, /*systemType = sys-type-manager*/
														(byte) 0x00, (byte) 0x08, /*system-id length = 8 and value (manufacturer- and device specific TODO CARE!?*/
														(byte) 0x38, (byte) 0x37, (byte) 0x36, (byte) 0x35, /* is this manufacturer and device specific? TODO what to choose here?*/
														(byte) 0x34, (byte) 0x33, (byte) 0x32, (byte) 0x31, 
														(byte) 0x00, (byte) 0x00, /*manager's response to config-id is always 0*/
														(byte) 0x00, (byte) 0x00, /*data-req-mode-flags*/
														(byte) 0x00, (byte) 0x00, /*data-req-init-agent-count = 0| data-req-init-manager-count = 0*/
														(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00 /*optionList.count = 0 | optionList.length = 0*/
            };
            
            //ECG association response standard config known
            //has the basic ECG standard config manager does not start a test association
            final byte data_AssocRespStandConfigKnown[] = new byte[]{
            		
														(byte) 0xE3, (byte) 0x00,/*APDU Choice Type (AareApdu)*/
														(byte) 0x00, (byte) 0x2C,/*Choice.length = 44*/
														(byte) 0x00, (byte) 0x03,/*result accepted-unknown-config*/
														(byte) 0x50, (byte) 0x79,/*data-proto-id = 20601*/
														(byte) 0x00, (byte) 0x26,/*data-proto-info length = 38*/
														(byte) 0x40, (byte) 0x00, (byte) 0x00, (byte) 0x00, /*protocolVersion*/
														(byte) 0x80, (byte) 0x00, /*encoding rules = MDER*/
														(byte) 0x80, (byte) 0x00, (byte) 0x00, (byte) 0x00, /*nomenclatureVersion*/
														(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, /*functionalUnits - normal Association*/
														(byte) 0x80, (byte) 0x00, (byte) 0x00, (byte) 0x00, /*systemType = sys-type-manager*/
														(byte) 0x00, (byte) 0x08, /*system-id length = 8 and value (manufacturer- and device specific TODO CARE!?*/
														(byte) 0x38, (byte) 0x37, (byte) 0x36, (byte) 0x35, /* is this manufacturer and device specific? TODO what to choose here?*/
														(byte) 0x34, (byte) 0x33, (byte) 0x32, (byte) 0x31, 
														(byte) 0x00, (byte) 0x00, /*manager's response to config-id is always 0*/
														(byte) 0x00, (byte) 0x00, /*data-req-mode-flags*/
														(byte) 0x00, (byte) 0x00, /*data-req-init-agent-count = 0| data-req-init-manager-count = 0*/
														(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00 /*optionList.count = 0 | optionList.length = 0*/
            		
            };
            
            //configure state
            //associate result accepted unknown
            //remote operation response event report configuration
            final byte data_ConfirmedEventReportResponse[] = new byte[]{
            											
            											(byte)0xE7, (byte) 0x00, /*APPDU Choice (PrstApdu)*/
            											(byte)0x00, (byte) 0x16, /*Choice.lenght= 22*/
            											(byte)0x00, (byte) 0x14, /*Octet String-length = 20*/
            											invoke_id[0], invoke_id[1],/*invokde-id = mirrored from invocation*/
            											(byte)0x02, (byte)0x01, /*Choice Remote Operation Response | Confirmed Event Report)*/
            											(byte)0x00, (byte)0x0E, /*Choice length = 14*/
            											(byte)0x00, (byte)0x00, /*obj-handle=0 (MDS object)*/
            											(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, /*curent time = 0*/
            											(byte)0x0D, (byte)0x1C, /*event-type=MDC_NOTI_CONFIG*/
            											(byte)0x00, (byte)0x04, /*event-reply-info.length = 4*/
            											(byte)0x40, (byte)0x00, /*configreportrsp.config-report-id = 16384*/
            											(byte)0x00, (byte)0x00 /*configreportrsp.config-result = accepted-config*/
            };
            
            //now in associated state
            //get MDS attributes 
            //response ==>11073_2012 E.4.1.3 page 75 
            final byte data_getMDS[] = new byte[] {
            	
            						(byte)0xE7, (byte)0x00, /*APDU Choice Type (PrstApdu)*/
            						(byte)0x00, (byte)0x03, /*Choice.length = 14*/
            						(byte)0x00, (byte)0x0C, /*OCTET String-length = 12*/
            						invoke_id[0], invoke_id[1],/*invoke_id differentiates this message fromany other outstanding, choie is implementation specific*/
            						(byte)0x01, (byte)0x03, /*Choice Remote Operation Invoke|Get*/
            						(byte)0x00, (byte)0x06, /*choice length = 6*/
            						(byte)0x00, (byte)0x00, /*handle = 0 MDS Object*/
            						(byte)0x00, (byte)0x00, /*attribute-id-list.count = 0 (all attributes)*/
            						(byte)0x00, (byte)0x00 /*attribute-id-list.length = 0*/
            };
            
            //data reporting
            //10073_2012 E.5.1
            
            //disassociation
            //association release request
            
            final byte data_AssociationReleaseRequest[] = new byte[] {
            								(byte)0xE5, (byte)0x00, /*APDU Choice Type (RlreApdu)*/
            								(byte)0x00, (byte)0x02, /*Choice.length = 2*/
            								(byte)0x00, (byte)0x00, /*reasin = normal*/
            };
            
            /**
            //blood pressure actual RR
            final byte data_RR[] = new byte[] {         (byte) 0xE5, (byte) 0x00,
                                                        (byte) 0x00, (byte) 0x02,
                                                        (byte) 0x00, (byte) 0x00 };
			//TODO: write thread adaption
            try {//count from read thread
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
               */
        }
    }//WriteThread
    
    
    
    //Blood Pressure: send data ==> compile HL7 message
    //TODO: implement ECG send data
    public void sendData() {
    	/**Calendar cal = Calendar.getInstance();
    	String time = ""+cal.get(Calendar.YEAR)+""+(cal.get(Calendar.MONTH)+1)+""+cal.get(Calendar.DAY_OF_MONTH)+""+cal.get(Calendar.HOUR_OF_DAY)+
    			""+cal.get(Calendar.MINUTE)+""+cal.get(Calendar.SECOND)+"+0200";
    			
    	MSH m = new MSH("HIO_DOR",time,"msgid123","IHE PCD ORU-R01 2006^HL7^2.16.840.1.113883.9.n.m^HL7");
    	PID p = new PID("1","171122869^^^","Starek^Christian^^BSc^","Starek^Dagmar^^^^","19890727000000","M");
    	OBR rep = new OBR("1","pon123","fon123","528391^MDC_DEV_SPEC_PROFILE_BP^MDC",obs_dat);
    	ArrayList<OBX> res = new ArrayList<OBX>();
		//   	0.0.0.1
    	OBX r1 = new OBX("1","","528391^MDC_DEV_SPEC_PROFILE_BP^MDC","1","","","X","","FHTW");
    	OBX r2 = new OBX("2","","150020^MDC_PRESS_BLD_NONINV^MDC","1.0.1","","","X",obs_dat,"");
    	OBX r3 = new OBX("3","NM","150021^MDC_PRESS_BLD_NONINV_SYS^MDC","1.0.1.1",sys+"","266016^MDC_DIM_MMHG^MDC","R","","");
    	OBX r4 = new OBX("4","NM","150022^MDC_PRESS_BLD_NONINV_DIA^MDC","1.0.1.2",dia+"","266016^MDC_DIM_MMHG^MDC","R","","");
    	OBX r5 = new OBX("5","NM","150023^MDC_PRESS_BLD_NONINV_MEAN^MDC","1.0.1.3",map+"","266016^MDC_DIM_MMHG^MDC","R","","");
		//    	149546^MDC_PULS_RATE_NON_INV^MDC	264864^MDC_DIM_BEAT_PER_MIN 1.0.0.1
    	res.add(r1);
    	res.add(r2);
    	res.add(r3);
    	res.add(r4);
    	res.add(r5);
    	HL7Message hl = new HL7Message(m, p, rep, res);
    	SOAPhard sh = new SOAPhard(hl.getHL7Message());
    	
    	//TODO: message for soap transfer!
    	msg = sh.getSoap();
    	
    	Log.d(TAG,msg);
    	//TODO: change output to toast?
    	info.setText("soap created");*/
    	
    	saveSoapTxt();
    	sendSoapHttp();
    }
    
    
    
    public void saveSoapTxt() {
    	
		StringBuilder sb = new StringBuilder("");
    	File filename = new File("/sdcard/soap.txt");
    	String ls = System.getProperty("line.separator");
    	
    	try {
	    	FileReader ir = new FileReader(filename);
	    	
	    	BufferedReader br = new BufferedReader(ir);
			String line;
		    while((line=br.readLine())!=null) {
		    	sb.append(line);
		    	sb.append(ls);
			}
		    sb.append(ls);
		    
			ir.close();
			br.close();
    	}catch(Exception e){}
	

    	try {	

	    	FileWriter fos = new FileWriter(filename);
	    	BufferedWriter osw = new BufferedWriter(fos);
	    	
	    	osw.append(sb);
	    	osw.append(msg);

	    	osw.append(ls);
	    	osw.append(ls);
	    	osw.flush();
	    	osw.close();
	    	fos.close();
	    	
	    	//TODO: display status message elsewhere
	    	//info.setText("soap saved");	    	
	    	
    	}catch(IOException e) {
    		Toast t = Toast.makeText(this, "Schreibfehler IO", 3);
    		t.show();
    	}

	}//sendSoap
	
    
    public void sendSoapHttp() {
    	
    	try{
        	//socket
    		String hostname = "127.0.0.1";
        	int port = 8080;
        	InetAddress adr = InetAddress.getByName(hostname);
        	Socket soc = new Socket (adr, port);
        	
        	//header
            String path = "/rcx-ws/rcx";
            BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(soc.getOutputStream(),"UTF-8"));
            wr.write("POST " + path + " HTTP/1.0\r\n");
            wr.write("Host: localhost\r\n");
            wr.write("Content-Length: " + msg.length() + "\r\n");
            wr.write("Content-Type: text/xml; charset=\"utf-8\"\r\n");
            wr.write("\r\n");
        	
           //Send data
            wr.write(msg);
            wr.flush();
      			
            // Response
            BufferedReader rd = new BufferedReader(new InputStreamReader(soc.getInputStream()));
            String line;
            while((line = rd.readLine()) != null)
            	Log.d(TAG,line);
    	} catch(Exception ex){
    		Log.e(TAG, ex.getMessage());
    	}
    	
    	
    }//sendSoapHttp


}//class
