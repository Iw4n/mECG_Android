package com.example.XML;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.protocol.HTTP;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class SOAPhard {

	public String header = "";
	public String body = "";
	private String all = "";
	public String host = "10.201.74.241:8080";//"192.168.0.132:8080";
	public String ur = "/BloodPressureWS/services/DeviceObservationConsumer_Service";


	public SOAPhard (String hl7msg) {
	
		all = "<soapenv:Envelope xmlns:soapenv=\"http://www.w3.org/2003/05/soap-envelope\">";
		header = "<soapenv:Header xmlns:wsa=\"http://www.w3.org/2005/08/addressing\"><wsa:To soapenv:mustUnderstand=\"true\">http://"+host+ur+"</wsa:To>" +
				"<wsa:From soapenv:mustUnderstand=\"true\"><wsa:Address>http://www.w3.org/2005/08/addressing/anonymous</wsa:Address></wsa:From><wsa:MessageID soapenv:mustUnderstand=\"true\">urn:uuid:A52590343911955D1A1251497585530</wsa:MessageID>" +
				"<wsa:Action soapenv:mustUnderstand=\"true\">urn:ihe:pcd:2010:CommunicatePCDData</wsa:Action> </soapenv:Header>";
		body = "<soapenv:Body><CommunicatePCDData xmlns=\"urn:ihe:pcd:dec:2010\">"+hl7msg+"</CommunicatePCDData></soapenv:Body>";
		all += header;
		all += body;
		all += "</soapenv:Envelope>";
	}//SOAP
	
	public String getSoap () {
		return all;
	}//getSoap
	
    public File saveSoapTxt(String path) throws IOException {
    	
		StringBuilder sb = new StringBuilder("");
    	File filename = new File(path);
    	if(!filename.exists())
    		filename.createNewFile();
    	else
    	{
    		filename.delete();
    		filename.createNewFile();
    	}
    	String ls = System.getProperty("line.separator");
    	
    	FileWriter fos = new FileWriter(filename);
    	BufferedWriter osw = new BufferedWriter(fos);
    	
    	osw.append(sb);
    	osw.append(all);

    	osw.append(ls);
    	osw.append(ls);
    	osw.flush();
    	osw.close();
    	fos.close();
    	
    	return filename;

	}//sendSoap
    
    
    public StringBuilder sendSoapHttp() {

    	StringBuilder sb = new StringBuilder();
    	
    	//httpclient
    	HttpClient httpclient = new DefaultHttpClient();
        // Your URL
        HttpPost httppost = new HttpPost("http://"+ur);
        httppost.setHeader("HOST", host);
        try {
			httppost.setURI(new URI("http://"+host+ur));

			StringEntity se;
			se = new StringEntity(all, HTTP.UTF_8);
			se.setContentType("application/soap+xml");
			httppost.setEntity(se);
		
			HttpResponse response;
			response = httpclient.execute(httppost);
                
        
	        HttpEntity entity = response.getEntity();
	        
	        if (entity != null) {
	
	            InputStream instream = entity.getContent();
	            BufferedReader reader = new BufferedReader(new InputStreamReader(instream));
	            sb = new StringBuilder();
	            
	            String line = null;
	            while ((line = reader.readLine()) != null) {
	                sb.append(line + "\n");
	            }
	            instream.close();
	            
	            return sb;
	        }
        } catch (Exception e) {
			sb = new StringBuilder("SOAP ERROR: "+e.getMessage());
			return sb;
		}
		return null;
    }//sendSoapHttp
	
}
