package ahd.basics.xml;

import java.io.IOException;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.Source;

public class SOAP {

	public String header = "";
	public String body = "";
	
	public SOAP (String hl7msg) {
		
		try {
			SOAPMessage sm = MessageFactory.newInstance().createMessage();
			SOAPPart sp = sm.getSOAPPart();
			SOAPEnvelope se = sp.getEnvelope();
			
			SOAPHeader sh = se.getHeader();
			sh.addNamespaceDeclaration("wsa", "http://www.w3.org/2005/08/addressing");
			SOAPHeaderElement she_to = sh.addHeaderElement(se.createName("To", "wsa", "http://localhost/DeviceObservationConsumer_Service"));
			she_to.addAttribute(se.createName("mustUnderstand"),"true");
			
			SOAPBody sb = se.getBody();
			SOAPBodyElement sbe = sb.addBodyElement(se.createName("CommunicatePCDData", "xxx", hl7msg));
			sbe.addAttribute(se.createName("xmlns"), "urn:ihe:pcd:dec:2010");
			
			Source source = sp.getContent();
			
			sm.writeTo(System.out);
			System.out.println();
			
		} catch (SOAPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}//SOAP
	
}
