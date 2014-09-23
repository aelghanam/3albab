package com.boudy.orders.components;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.simple.JSONObject;
import org.mule.module.json.JsonData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Orders extends HttpServlet implements Runnable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2759630099837963148L;
	
	private static Map<String,String[]> districtNameAndCodes=new HashMap<String, String[]>();
	
	private static int firstTimeOrderReloadInMin=200;
	
	
	Main main=null;
	
	private static String KEY ="jibni34q1t4Z"; 
	
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
//		System.out.println("request URI=" + request.getRequestURI());
		if( !request.getRequestURI().equals("/favicon.ico") ){
			PrintWriter out = response.getWriter();
			//TODO: Add feature to get current key and to change time in seconds for refresh
			if( request.getRequestURI().indexOf("/newkey") >= 0 ){
				String key="";
				key=request.getPathInfo().substring(8);
				KEY=key;
				out.write("Setted New key with :"+key);
				out.close();
			}else{
			
				if( !request.getRequestURI().equals("/cancel") ){
					
					out.write("Started Orders Syncing");
					out.close();
					startApp();
							
					main=new Main(300);
				}else{
						if(main!=null){
							main.cancel();
							main=null;
							out.write("Canceled Orders Syncing Operation");
						}else{
							out.write("Orders Syncing has not been intialized yet");
						}
				}
			}

		}
	}
	
//	public static void main(String[] args) {
//		try {
//			
//			//TODO: Comment Before Deployment
////			Main main=new Main(15);
//			Orders orders = new Orders();
//			orders.startApp();
//////			
////			System.out.println("Last Order Number is:"+LASTORDERPROCESSED);
////			
////			orders.checkDir();
//			
////			String i ="/newkey/keykeykey";
////			i=i.substring(8);
////			System.out.println(i);
//			
//			
////			long unixTime = System.currentTimeMillis();
////			Calendar calendar = Calendar.getInstance();
////			calendar.setTimeInMillis(unixTime);
////			calendar.add(Calendar.MINUTE, -5);
////			System.out.println(calendar);
////			unixTime=calendar.getTimeInMillis();
////			System.out.println(unixTime);
//
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
	
	private static String getFromDate() {
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("EST"));
//		calendar.set(Calendar.YEAR, 2013);
//		calendar.set(Calendar.MONTH, 11);
//		calendar.set(Calendar.DAY_OF_MONTH, 8);
//		calendar.set(Calendar.HOUR_OF_DAY,13);
//		calendar.set(Calendar.MINUTE,15);
//		calendar.set(Calendar.SECOND, 12);
//		System.out.println("Timezone:"+calendar.getTimeZone()+" Millis:"+calendar.getTimeInMillis()/ 1000L);
//		calendar.add(Calendar.HOUR_OF_DAY, 5);
		calendar.add(Calendar.MINUTE, -5);
		calendar.add(Calendar.SECOND, -1);
		calendar.setTimeZone(TimeZone.getTimeZone("EST"));
//		System.out.println("Timezone:"+calendar.getTimeZone()+" Millis:"+calendar.getTimeInMillis()/ 1000L);
//		
		long x=0;
//		System.out.println(Long.valueOf(x));
		x=calendar.getTimeInMillis()/ 1000L;
//		System.out.println("After Changing timezone:"+Long.valueOf(x));
		String temp=String.valueOf(Long.valueOf(x));
		return temp;
	}
	
	
	
	
	private static String getFromDate(int i) {
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("EST"));
//		calendar.set(Calendar.YEAR, 2013);
//		calendar.set(Calendar.MONTH, 11);
//		calendar.set(Calendar.DAY_OF_MONTH, 8);
//		calendar.set(Calendar.HOUR_OF_DAY,13);
//		calendar.set(Calendar.MINUTE,15);
//		calendar.set(Calendar.SECOND, 12);
//		System.out.println("Timezone:"+calendar.getTimeZone()+" Millis:"+calendar.getTimeInMillis()/ 1000L);
//		calendar.add(Calendar.HOUR_OF_DAY, 5);
		//calendar.add(Calendar.MINUTE, -5);
		//calendar.add(Calendar.SECOND, -1);
		calendar.add(Calendar.MINUTE, -i);
		calendar.add(Calendar.SECOND, -1);
		calendar.setTimeZone(TimeZone.getTimeZone("EST"));
//		System.out.println("Timezone:"+calendar.getTimeZone()+" Millis:"+calendar.getTimeInMillis()/ 1000L);
//		
		long x=0;
//		System.out.println(Long.valueOf(x));
		x=calendar.getTimeInMillis()/ 1000L;
//		System.out.println("After Changing timezone:"+Long.valueOf(x));
		String temp=String.valueOf(Long.valueOf(x));
		return temp;
	}
	
	private static int LASTORDERPROCESSED=0;

	public Document startApp() throws IOException{
		HttpClient httpclient = new HttpClient();
//		HttpClientParams params=new HttpClientParams();

//		httpclient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY,"");
		//httpclient.getHostConfiguration().setProxy("proxy.sdc.hp.com", 8080);
		
		GetMethod get = null;
		
		if(LASTORDERPROCESSED==0){
			LASTORDERPROCESSED=1;
			get=new GetMethod("https://app.ecwid.com/api/v1/1775149/orders?secure_auth_key="+KEY+"&from_date="+getFromDate(firstTimeOrderReloadInMin));
//			System.out.println("https://app.ecwid.com/api/v1/1775149/orders?secure_auth_key="+KEY+"&from_date="+getFromDate(200));
		}else{
		//TODO: This is the method for production
			get=new GetMethod("https://app.ecwid.com/api/v1/1775149/orders?secure_auth_key="+KEY+"&from_date="+getFromDate());
//			System.out.println("https://app.ecwid.com/api/v1/1775149/orders?secure_auth_key="+KEY+"&from_date="+getFromDate());
		}
		httpclient.executeMethod(get);
		InputStream in = get.getResponseBodyAsStream();
		JsonData json = new JsonData(in);
		Document doc = checkOrders(json);
//		System.out.println("End:"+doc.toString());
		in.close();
		return doc;
	}

	private Document checkOrders(JsonData json) {
		JsonData jsonPayload = json;
//		System.out.println("JsonData:" + jsonPayload.toString());
//
//		System.out.println("Orders Size:" + jsonPayload.get("orders").size());

		Document doc = null;
		 
		doc = createJsonAndFile(jsonPayload);
		
		return doc;

	}

	private String getProductBrand(int i) {
//		System.out.println("Starting method getProductBrand");
		String productId="";
		try {
			HttpClient httpclient = new HttpClient();
			GetMethod get = new GetMethod(
					"https://app.ecwid.com/api/v1/1775149/product?id=27236597"
							+ i
							+ "&amp;hidden_products=true&amp;secure_auth_key=gyM8Nf9X8xVP");
			// execute method and handle any error responses.
			httpclient.executeMethod(get);
			InputStream in = get.getResponseBodyAsStream();
			// Process the data from the input stream.
			JsonData json = new JsonData(in);
//			System.out.println("Product JSON:" + json.toString());
			if(json.hasNode("errorMessage")){
//				System.out.println("Error with Product");
			}else{
//				System.out.println("Product ID:"+json.get("attributes[0]/internalName"));
				productId=json.get("attributes[0]/internalName").getTextValue();
			}
			// json.getAsString(expression)
			in.close();
			get.releaseConnection();
			
		} catch (IOException e) {
				System.out.println("getProductBrand IOException message:"+e.getMessage());
		}
		return productId;
	}

	private Document createJsonAndFile(JsonData jsonPayload) {
		Document doc = null;
		
		System.out.println("Orders To be Processed Size:"+jsonPayload.get("orders").size());//+" LastOrderProcessed="+LASTORDERPROCESSED);

		try {
			
			int orderNumber=1;
			
			
			for (int i = 0; i < jsonPayload.get("orders").size(); i++) {
				
				orderNumber=Integer.valueOf(jsonPayload.get("orders[" + i + "]/vendorNumber").getTextValue());
				
				
				System.out.println("Starting to process order number:"+orderNumber);
				
				doc = null;
				DocumentBuilderFactory docFactory = DocumentBuilderFactory
						.newInstance();
				DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

				// root elements
				doc = docBuilder.newDocument();
				Element rootElement = doc.createElement("NewDataSet");
				doc.appendChild(rootElement);


				// staff elements
				Element customer = doc.createElement("Customer");
				rootElement.appendChild(customer);

				Element customerTel = doc.createElement("CustomerTel");
				customerTel.setTextContent(jsonPayload.get(
						"orders[" + i + "]/shippingPerson/phone")
						.getTextValue());
				customer.appendChild(customerTel);

				Element customerName = doc.createElement("CustomerName");
				customerName
						.setTextContent(jsonPayload.get(
								"orders[" + i + "]/shippingPerson/name")
								.getTextValue());
				customer.appendChild(customerName);
				

				Element addressL1 = doc.createElement("AddressL1");
				addressL1.setTextContent(jsonPayload.get(
						"orders[" + i + "]/shippingPerson/street")
						.getTextValue());
				customer.appendChild(addressL1);

				Element addressL2 = doc.createElement("AddressL2");
				addressL2
						.setTextContent(jsonPayload.get(
								"orders[" + i + "]/shippingPerson/city")
								.getTextValue());
				customer.appendChild(addressL2);
				
				//Delivery Fees
				Element deliveryFees = doc.createElement("DelevFee");
				deliveryFees
						.setTextContent(String.valueOf(jsonPayload.get(
								"orders[" + i + "]/shippingCost")
								.getIntValue()));
				customer.appendChild(deliveryFees);
				
				//Order Comments
				if(jsonPayload.get("orders[" + i + "]").has("orderComments") ){
//					System.out.println("orderComments:"+jsonPayload.get(
//							"orders[" + i + "]/orderComments")
//							.getTextValue());
					Element orderComments = doc.createElement("orderComments");
					orderComments
					.setTextContent(String.valueOf(jsonPayload.get(
							"orders[" + i + "]/orderComments")
							.getTextValue()));
					customer.appendChild(orderComments);
				}
				
				//Payment Method
				if(jsonPayload.get("orders[" + i + "]").has("paymentMethod") ){
//					System.out.println("Payment Method:"+jsonPayload.get(
//							"orders[" + i + "]/paymentMethod")
//							.getTextValue());
					String paymentMethodString=jsonPayload.get("orders[" + i + "]/paymentMethod").getTextValue();
					int result=0;
					if(paymentMethodString.contains("Credit") ){
						result=1;
					}
					Element paymentMethod = doc.createElement("Creditcard");
					paymentMethod.setTextContent(String.valueOf(result));
					customer.appendChild(paymentMethod);
				}
				
				//Customer Email
//				Element customerEmail = doc.createElement("Mail");
//				customerEmail
//						.setTextContent(jsonPayload.get(
//								"orders[" + i + "]/customerEmail")
//								.getTextValue());
//				customer.appendChild(customerEmail);
				
//				System.out.println("volumeDiscountCost===="+jsonPayload.get("orders[" + i + "]/volumeDiscountCost").getIntValue());
				
				boolean hasCoupon=false;
				//Coupon Number
				if(jsonPayload.get("orders[" + i + "]").has("discountCoupon") ){
					hasCoupon=true;
					Element couponNo = doc.createElement("CoponNo");
					couponNo.setTextContent(jsonPayload.get(
									"orders[" + i + "]/discountCoupon")
									.getTextValue());
					customer.appendChild(couponNo);
				}else if(jsonPayload.get("orders[" + i + "]").has("volumeDiscountCost") && jsonPayload.get("orders[" + i + "]/volumeDiscountCost").getIntValue()>0){
					hasCoupon=true;
					//Removed the coupon number that was sent with volume discounts
//					Element couponNo = doc.createElement("CoponNo");
////					System.out.println("v------------------="+jsonPayload.get("orders[" + i + "]/discounts").getIntValue());
//					String coupon="vdis";
//					if(jsonPayload.get("orders[" + i + "]/discounts[0]/settings/discountType").getTextValue().equals("ABS")){
//						coupon=coupon+String.valueOf(jsonPayload.get("orders[" + i + "]/discounts[0]/settings/value").getIntValue())+"c";
//					}else{
//						coupon=coupon+String.valueOf(jsonPayload.get("orders[" + i + "]/discounts[0]/settings/value").getIntValue())+"p";
//					}
//					couponNo.setTextContent(coupon);
//					customer.appendChild(couponNo);
				}
				
				if(hasCoupon){
					Element couponVal = doc.createElement("CoponVal");
					String couponValue= String.valueOf(jsonPayload.get("orders[" + i + "]/discounts[0]/discountCost").getDecimalValue());
					couponVal.setTextContent(couponValue);
					customer.appendChild(couponVal);					
				}
				
				Element totalBeforeDiscount = doc.createElement("Total");
				String totalBeforeDiscountValue = String.valueOf(jsonPayload.get("orders[" + i + "]/subtotalCost").getDecimalValue());
				totalBeforeDiscount.setTextContent(totalBeforeDiscountValue);
				customer.appendChild(totalBeforeDiscount);
				
				int itemsSize = 0;
				itemsSize = jsonPayload.get("orders[" + i + "]/items/").size();
//				System.out.println("Items Size:" + itemsSize);
				for (int x = 0; x < itemsSize; x++) {
					Element order = doc.createElement("order");
					rootElement.appendChild(order);

					//TODO: Get Product Info
					//getProductBrand(jsonPayload.get("orders[" + i + "]/items[" + x + "]/productId").getIntValue());


					Element barcode = doc.createElement("barcode");
					barcode.setTextContent(String.valueOf(jsonPayload.get(
							"orders[" + i + "]/items[" + x + "]/sku")
							.getTextValue()));
//					System.out.println("sku:"+String.valueOf(jsonPayload.get(
//							"orders[" + i + "]/items[" + x + "]/sku")
//							.getTextValue()));
					order.appendChild(barcode);
					Element qty = doc.createElement("Qty");
					qty.setTextContent(String.valueOf(jsonPayload.get(
							"orders[" + i + "]/items[" + x + "]/quantity")
							.getIntValue()));
					order.appendChild(qty);

				}
				
				doc = createFileAndUpload(doc, jsonPayload, i);
				
				
			}
			
			

		} catch (ParserConfigurationException pce) {
			System.out.println("createJsonAndFile: ParserConfigurationException message:"+pce.getMessage());
		} 
//		System.out.println("Ending method createJsonAndFile with doc=="+doc.toString());
		return doc;
	}

	private Document createFileAndUpload(Document doc, JsonData jsonPayload, int i) {
		
		String fileName="";
		
		String zoneNumber=checkZone(jsonPayload.get("orders[" + i + "]/shippingPerson/postalCode").getTextValue());
		
		fileName=String.valueOf(jsonPayload.get("orders[" + i + "]/vendorNumber").getTextValue()) +"."+zoneNumber;
		
		//TODO:Uncomment this part to see the output file names being created along with their content 
//		System.out.println("File Name:"+fileName);
//		DOMSource source = new DOMSource(doc);
//		TransformerFactory transformerFactory = TransformerFactory
//				.newInstance();
//		Transformer transformer = transformerFactory.newTransformer();
//		StringWriter writer = new StringWriter();
//		Result result = new StreamResult(writer);
//		transformer.transform(source, result);
//		System.out.println(writer.toString());
			

		uploadFile(fileName,doc,jsonPayload.get("orders[" + i + "]/vendorNumber").getTextValue());

		
		//		createFileLocally(fileName,doc);
			
		return doc;
	}

	private String checkZone(String textValue) {
		
		if(districtNameAndCodes.size()==0){
			Orders orders=new Orders();
			URL resource = orders.getClass().getResource("Districts.txt");
			List<String> readLines=new ArrayList<String>();
			try {
				readLines = FileUtils.readLines(new File(resource.getPath()));
			} catch (IOException e) {
				System.out.println("Error While Reading File");
			}
			String key="";
			ArrayList<String> zamalek=new ArrayList<String>();
			ArrayList<String> maadi=new ArrayList<String>();
			ArrayList<String> heliopolis=new ArrayList<String>();
			for (int i=0;i<readLines.size();i++) {
				String line = (String) readLines.get(i);
//				System.out.println("Line:"+line+":aftertrimandlowercase:"+line.trim().toLowerCase()+":"+line.trim().toLowerCase().equals(new String("zamalekdistrict"))+"=ZamalekDistrict");
				
				if(line.toLowerCase().equalsIgnoreCase("zamalekdistrict") || line.toLowerCase().equalsIgnoreCase("maadidistrict") || line.toLowerCase().equalsIgnoreCase("heliopolisdistrict") ){
					if(line.toLowerCase().equalsIgnoreCase("zamalekdistrict")) key="Zamalek";
					if(line.toLowerCase().equalsIgnoreCase("maadidistrict")) key="Maadi";
					if(line.toLowerCase().equalsIgnoreCase("heliopolisdistrict")) key="Heliopolis";
				}else if(line.equalsIgnoreCase("a") || line.equalsIgnoreCase("b")){
//					System.out.println("line A OR B:"+line);
				}else{
					String[] districts = line.split(":");
					for(int x=0;x<districts.length;x++){
//						System.out.println(districts[x]+" isEqual:"+key.equalsIgnoreCase("zamalek"));
						if(key.toLowerCase().equalsIgnoreCase("zamalek")) zamalek.add(districts[x]);
						if(key.toLowerCase().equalsIgnoreCase("maadi")) maadi.add(districts[x]);
						if(key.toLowerCase().equalsIgnoreCase("heliopolis")) heliopolis.add(districts[x]);
					}
				}
			}
			String[] zamalekArray = new String[zamalek.size()];
			zamalek.toArray(zamalekArray);
			String[] maadiArray = new String[maadi.size()];
			maadi.toArray(maadiArray);
			String[] heliopoliskArray = new String[heliopolis.size()];
			heliopolis.toArray(heliopoliskArray);
			districtNameAndCodes.put("zamalek", zamalekArray);
			districtNameAndCodes.put("maadi", maadiArray);
			districtNameAndCodes.put("heliopolis", heliopoliskArray);
			
//			System.out.println("Zamalek Districts Length:"+ ((String[]) districtNameAndCodes.get("zamalek")).length		);
//			System.out.println("Maadi Districts Length:"+ ((String[]) districtNameAndCodes.get("maadi")).length		);
//			System.out.println("Heliopolis Districts Length:"+ ((String[]) districtNameAndCodes.get("heliopolis")).length	);
		}
		
		textValue=textValue.toLowerCase();
		String[] zamalekArray = districtNameAndCodes.get("zamalek");
//		System.out.println("Textvalue:"+textValue);
		for(int i=0;i<zamalekArray.length;i++){
//			System.out.println("Zamalek: "+zamalekArray[i].toLowerCase()+" isEqual:"+textValue.equalsIgnoreCase(zamalekArray[i].toLowerCase()));
			if(textValue.toLowerCase().equalsIgnoreCase(zamalekArray[i].toLowerCase())){
				return "09";
			}
		}
		String[] heliopolisArray = districtNameAndCodes.get("heliopolis");
		for(int i=0;i<heliopolisArray.length;i++){
			if(textValue.toLowerCase().equalsIgnoreCase(heliopolisArray[i].toLowerCase())){
				return "04";
			}
		}
		String[] maadiArray = districtNameAndCodes.get("maadi");
		for(int i=0;i<maadiArray.length;i++){
//			System.out.println("maadiArray: "+maadiArray[i].toLowerCase()+" isEqual:"+textValue.equalsIgnoreCase(maadiArray[i].toLowerCase()));
			if(textValue.toLowerCase().equalsIgnoreCase(maadiArray[i].toLowerCase())){
				return "03";
			}
		}
		return "";
	}

	private boolean uploadFile(String path, Document doc,String orderId) {
//		System.out.println("Starting method uploadFile with path:" + path);
		boolean done=false;
		try {
			
			TransformerFactory transformerFactory = TransformerFactory
					.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			
			String url = "smb://196.202.53.58/Inter/";
			NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(
					null, "inter", "inter");
			SmbFile dir = new SmbFile(url
					+ path.substring(path.lastIndexOf("\\") + 1), auth);
			if (dir.exists()) {
				dir.delete();
			}
			dir.createNewFile();
			StreamResult result = new StreamResult(dir.getOutputStream());

			transformer.transform(source, result);
			dir.getOutputStream().close();
			
			System.out.println("Order Routed Successfully, with file created at Alfa Servers Titled:"+path.substring(path.lastIndexOf("\\") + 1)+" on "+Calendar.getInstance(TimeZone.getTimeZone("Egypt")).getTime().toString());
			// TODO: Comment Before Production
//			if(LASTORDERPROCESSED==0){
//				dir.delete();				
//			}
			
			
			updateOrderStatusOnEcwid(orderId);
			done=true;
		} catch (IllegalStateException e) {
			System.out.println("uploadFile: IllegalStateException message: "+e.getMessage());
		} catch (SmbException e) {
			System.out.println("uploadFile: SmbException message: "+e.getMessage());
		} catch (TransformerConfigurationException e) {
			System.out.println("uploadFile: TransformerConfigurationException message: "+e.getMessage());
		} catch (TransformerException e) {
			System.out.println("uploadFile: TransformerException message: "+e.getMessage());
		} catch (MalformedURLException e) {
			System.out.println("uploadFile: MalformedURLException message: "+e.getMessage());
		} catch (IOException e) {
			System.out.println("uploadFile: IOException message: "+e.getMessage());
		}

		if(!done){
			done=uploadFileAlternativePath(path, doc, orderId);
		}
		
		return done;
	}
	
	private boolean uploadFileAlternativePath(String path, Document doc,String orderId) {
		boolean done=false;
		String url = "smb://196.221.43.2/Inter/";
		System.out.println("Trying Alternative Path "+url);
			
			try {
				TransformerFactory transformerFactory = TransformerFactory
						.newInstance();
				Transformer transformer;
				transformer = transformerFactory.newTransformer();
				DOMSource source = new DOMSource(doc);
				

				NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(
						null, "inter", "inter");
				SmbFile dir = new SmbFile(url
						+ path.substring(path.lastIndexOf("\\") + 1), auth);
				if (dir.exists()) {
					dir.delete();
				}
				dir.createNewFile();
				StreamResult result = new StreamResult(dir.getOutputStream());

				transformer.transform(source, result);
				dir.getOutputStream().close();
				
				updateOrderStatusOnEcwid(orderId);
				System.out.println("Order Routed Successfully, with file created at Alternative Alfa Servers Titled:"+path.substring(path.lastIndexOf("\\") + 1)+" on "+Calendar.getInstance(TimeZone.getTimeZone("Egypt")).getTime().toString());
				done=true;
			} catch (TransformerConfigurationException e1) {
				System.out.println("uploadFile 2nd Trial: TransformerConfigurationException message: "+e1.getMessage());
			} catch (MalformedURLException e1) {
				System.out.println("uploadFile 2nd Trial: MalformedURLException message: "+e1.getMessage());
			} catch (SmbException e1) {
				System.out.println("uploadFile 2nd Trial: SmbException message: "+e1.getMessage());
			} catch (TransformerException e1) {
				System.out.println("uploadFile 2nd Trial: TransformerException message: "+e1.getMessage());
			} catch (IOException e1) {
				done=false;
				System.out.println("uploadFile 2nd Trial: IOException message: "+e1.getMessage());
			}
			if(!done){
				//Emails.sendEmail("orders@3albab.com", "Order couldn't be created at Alfa servers on "+Calendar.getInstance(TimeZone.getTimeZone("Egypt")).getTime().toString()+" order number "+orderId, doc.toString());		
				Emails.sendEmailAndAttachment("orders@3albab.com","Order couldn't be created at Alfa servers on "+Calendar.getInstance(TimeZone.getTimeZone("Egypt")).getTime().toString()+" order number "+orderId,doc.toString(),doc,path);
			}
			return done;
	}
	
	public void updateOrderStatusOnEcwid(String orderId) throws IOException {
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("new_fulfillment_status", "PROCESSING");
		jsonObj.put("order", orderId);
		HttpClient httpclient = new HttpClient();
		PostMethod post = null;
		post=new PostMethod("https://app.ecwid.com/api/v1/1775149/orders?secure_auth_key="+KEY+"&order="+orderId+"&new_fulfillment_status=PROCESSING");//PROCESSING");
//		System.out.println(jsonObj.toString());
		StringRequestEntity requestEntity = new StringRequestEntity("["+jsonObj.toString()+"]","application/json","UTF-8");
		post.setRequestEntity(requestEntity);
//		System.out.println(requestEntity.getContent());
		post.setRequestHeader("Content-Type", "application/json");
		post.setParameter("new_fulfillment_status", "PROCESSING");
		post.setParameter("order", orderId);
		httpclient.executeMethod(post);
		InputStream in = post.getResponseBodyAsStream();
//		System.out.println("------------");
//		System.out.println(new String(post.getResponseBody()));
//		System.out.println(post.getResponseBodyAsString());
		JsonData jsonData=new JsonData(post.getResponseBodyAsString());
//		System.out.println(jsonData.get("orders").size());
//		System.out.println(post.getStatusCode());
//		System.out.println("------------");
		System.out.println("Order "+orderId+" Updated on ECWID");
	}

	private void checkDir(){
//		System.setProperty("http.proxyHost", "proxy.sdc.hp.com");
//		System.setProperty("http.proxyPort", "8080");
//		System.setProperty("https.proxyHost", "proxy.sdc.hp.com");
//		System.setProperty("https.proxyPort", "8080");
		String url = "smb://196.202.53.58/Inter/";
		NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(
				null, "inter", "inter");
		SmbFile dir;
		try {
			dir = new SmbFile(url, auth);
			for (SmbFile f : dir.listFiles()) {
				System.out.println("File Name:" + f.getName());
				InputStream inputStream = f.getInputStream();
				String content = IOUtils.toString(inputStream);
				System.out.println("File Content:" + content);
				inputStream.close();
			}
			
			if(dir.listFiles().length==0){
				System.out.println("No Documents Found");
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (SmbException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	
//	private String getDiscountValue(JsonData jsonPayload,int i){
//		String shippingCost=String.valueOf(jsonPayload.get("orders[" + i + "]/shippingCost").getIntValue());
//		if(shippingCost.equalsIgnoreCase("0")){
//			return "";
//		}else{
//			String discountCoupon=jsonPayload.get("orders[" + i + "]/discountCoupon").getTextValue();
//			String discountType=jsonPayload.get("orders[" + i + "]/discounts[0]/settings/discountType").getTextValue();
//			if(discountType.equalsIgnoreCase("ABS")){
//				return String.valueOf(jsonPayload.get("orders[" + i + "]/discounts[0]/settings/value").getIntValue());
//			}else if(discountType.equalsIgnoreCase("PERCENT")){
//				int discountPercent=jsonPayload.get("orders[" + i + "]/discounts[0]/settings/value").getIntValue();
//				int totalValue=0;
//				return String.valueOf("");
//			}
//		}
//		return "";
//	}

	private  boolean createFileLocally(String path, Document doc) {
//		System.out.println("Starting method uploadFile with path:" + path);
		try {
			
			TransformerFactory transformerFactory = TransformerFactory
					.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			
			String url = System.getProperty("java.io.tmpdir");//"E:/albab/";
			System.out.println(url);
			//SmbFile dir = new SmbFile(url + path.substring(path.lastIndexOf("\\") + 1));
			File dir=new File(url + path.substring(path.lastIndexOf("\\") + 1));
			if (dir.exists()) {
				dir.delete();
			}
			dir.createNewFile();
			FileOutputStream openOutputStream = FileUtils.openOutputStream(dir);
//			StreamResult result = new StreamResult(dir.getOutputStream());
			StreamResult result = new StreamResult(openOutputStream);

			transformer.transform(source, result);
			openOutputStream.close();
			
			System.out.println("Order Routed Successfully, with file created at Alfa Servers Titled:"+path.substring(path.lastIndexOf("\\") + 1)+" on "+Calendar.getInstance(TimeZone.getTimeZone("Egypt")).getTime().toString());
			// TODO: Comment Before Production
//			if(LASTORDERPROCESSED==0){
//				dir.delete();				
//			}

		} catch (IllegalStateException e) {
			System.out.println("createFileLocally: IllegalStateException message:"+e.getMessage());
		} catch (IOException e) {
			System.out.println("createFileLocally: IOException message:"+e.getMessage());
		} catch (TransformerConfigurationException e) {
			System.out.println("createFileLocally: TransformerConfigurationException message:"+e.getMessage());
		} catch (TransformerException e) {
			System.out.println("createFileLocally: TransformerException message:"+e.getMessage());
		}

		return true;
	}
	
	public static String createFileLocally(String path, Document doc,String p) {
//		System.out.println("Starting method uploadFile with path:" + path);
		try {
			
			TransformerFactory transformerFactory = TransformerFactory
					.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			
			String url = System.getProperty("java.io.tmpdir");//"E:/albab/";
			System.out.println(url);
			//SmbFile dir = new SmbFile(url + path.substring(path.lastIndexOf("\\") + 1));
			File dir=new File(url + path.substring(path.lastIndexOf("\\") + 1));
			p=url + path.substring(path.lastIndexOf("\\") + 1);
			if (dir.exists()) {
				dir.delete();
			}
			dir.createNewFile();
			FileOutputStream openOutputStream = FileUtils.openOutputStream(dir);
//			StreamResult result = new StreamResult(dir.getOutputStream());
			StreamResult result = new StreamResult(openOutputStream);

			transformer.transform(source, result);
			openOutputStream.close();
			
			System.out.println("Order Routed Successfully, with file created at Alfa Servers Titled:"+path.substring(path.lastIndexOf("\\") + 1)+" on "+Calendar.getInstance(TimeZone.getTimeZone("Egypt")).getTime().toString());
			// TODO: Comment Before Production
//			if(LASTORDERPROCESSED==0){
//				dir.delete();				
//			}

		} catch (IllegalStateException e) {
			System.out.println("createFileLocally: IllegalStateException message:"+e.getMessage());
		} catch (IOException e) {
			System.out.println("createFileLocally: IOException message:"+e.getMessage());
		} catch (TransformerConfigurationException e) {
			System.out.println("createFileLocally: TransformerConfigurationException message:"+e.getMessage());
		} catch (TransformerException e) {
			System.out.println("createFileLocally: TransformerException message:"+e.getMessage());
		}

		return p;
	}


public void run() {
	try {
		System.out.println("Orders Checking Thread Started at "+Calendar.getInstance().getTime().toString());
		startApp();
		System.out.println("Orders Checking Thread Finished at "+Calendar.getInstance().getTime().toString());
	} catch (IOException e) {
		System.out.println("IOException in run:"+e.getMessage());
	}
}
	
}