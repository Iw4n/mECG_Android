package ahd.basics.xml;

public class SOAPhard {

	public String header = "";
	public String body = "";
	private String all = "";
	
	public SOAPhard (String hl7msg) {
	
		all = "<soapenv:Envelope xmlns:soapenv=\"http://www.w3.org/2003/05/soap-envelope\">";
		header = "soapenv:Header xmlns:wsa=\"http://www.w3.org/2005/08/addressing\"><wsa:To soapenv:mustUnderstand=\"true\">http://localhost/DeviceObservationConsumer_Service</wsa:To>" +
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
	
	
}
