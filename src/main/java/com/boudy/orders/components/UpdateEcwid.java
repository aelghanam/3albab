package com.boudy.orders.components;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;

import javax.mail.Flags;
import javax.mail.Flags.Flag;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeUtility;
import javax.mail.search.AndTerm;
import javax.mail.search.ComparisonTerm;
import javax.mail.search.FlagTerm;
import javax.mail.search.ReceivedDateTerm;
import javax.mail.search.SearchTerm;
import javax.mail.search.SubjectTerm;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.mule.module.json.JsonData;

import com.googlecode.gmail4j.GmailClient;
import com.googlecode.gmail4j.GmailConnection;
import com.googlecode.gmail4j.GmailMessage;
import com.googlecode.gmail4j.http.HttpGmailConnection;
import com.googlecode.gmail4j.rss.RssGmailClient;

public class UpdateEcwid implements Runnable{
	
	

	static String USERNAME="api@durely.com";
	static String PASSWORD="3albab2013";
	
	public static class MessagesByDate implements Comparator<Message>
    {            
         public int compare(Message c1, Message c2)
         {
             Date a1=Calendar.getInstance().getTime(),a2=Calendar.getInstance().getTime();
			try {
				a1 = c1.getReceivedDate();
				a2 = c2.getReceivedDate();
			} catch (MessagingException e) {
				System.out.println("MessagingException while comparing dates");
			}
             return a2.compareTo(a1);
         }
     }
	
		
	private static void getPart(Message message){
		try {
			Multipart mp = (Multipart) message.getContent();
//			System.out.println("Multipart Content Type:"+mp.getContentType());
			//UpdateEcwid update = new UpdateEcwid();
//			System.out.println("Count:"+mp.getCount());
			for (int i = 0; i < mp.getCount(); i++) {
                Part part = mp.getBodyPart(i);
//                System.out.println("Part Content Type:"+part.getContentType());
//                System.out.println("Part Desposition:"+part.getDisposition());
                if ((part.getFileName() == null || part.getFileName() == "") && part.isMimeType("text/plain")) {
                    System.out.println("SentDate:"+message.getSentDate());
                    System.out.println("No Attachments");
                   // message = new MessageBean(message.getMessageNumber(), message.getSubject(), message.getFrom()[0].toString(), null, message.getSentDate(), (String) part.getContent(), false, null);
                } else if (part.getFileName() != null || part.getFileName() != "") {
                	System.out.println("Attachment Filename:"+part.getFileName());
                    if ((part.getDisposition() != null && part.getDisposition().equalsIgnoreCase(Part.ATTACHMENT)) || (part.getContentType().indexOf("APPLICATION/OCTET-STREAM")>=0) ) {
                    	//System.out.println("Disposition:"+part.getDisposition());
                    	//System.out.println("Content Type:"+part.getContentType());
                    	String fileName=part.getFileName();
                		String year=fileName.substring(5, 9);
                		String month=fileName.substring(9, 11);
                		String day=fileName.substring(11, 13);
                		String amPm=fileName.substring(14,16);
                		System.out.println("File Year:"+year+" Month:"+month+" Day:"+day+" AM/PM:"+amPm+" on:"+Calendar.getInstance().getTime());
                    	
                    	System.out.println("Starting to Save File"+" on:"+Calendar.getInstance().getTime());
                        String path=UpdateEcwid.saveFile(MimeUtility.decodeText(fileName), part.getInputStream());
                        System.out.println("Saved File Successfully"+" on:"+Calendar.getInstance().getTime());
                        updateEcwid(path);
                        break;
                    }
                }
            }
		} catch (IOException e) {
			System.out.println("IOException in getPart:"+e.getMessage());
		} catch (MessagingException e) {
			System.out.println("MessagingException in getPart:"+e.getMessage());
		}
	}
	
	private static String saveFile(String filename, InputStream input) {
		  String strDirectory = System.getProperty("java.io.tmpdir");//"C:\\users\\mohaabde\\";  
		  try{
		  // Create one directory
		  boolean success = (new File(strDirectory)).mkdir();
		  if (success) {
		  System.out.println("Directory: " 
		   + strDirectory + " created");
		  }else{
			  System.out.println("Directory Not Created");
		  }
		  } catch (Exception e) {//Catch exception if any
		    System.err.println("Error: " + e.getMessage());
		  }
		        String path = strDirectory+"\\" + filename;
		        try {
		            byte[] attachment = new byte[input.available()];
		            input.read(attachment);
		            File file = new File(path);
		            if(!file.exists()){
		            	file.createNewFile();
		            }
		            FileOutputStream out = new FileOutputStream(file);
		            IOUtils.copy(input, out);
		            out.flush();
		            input.close();
		            out.close();
		            return path;
		        } catch (IOException e) {
		            e.printStackTrace();
		        }
		        return path;
		    }
	
	
	
	private static void updateEcwid(String path) throws HttpException, IOException{
		System.out.println("Starting Method updateEcwid with file stored temp on path:"+path+" on:"+Calendar.getInstance().getTime());
		if(productsSKUToId==null || productsSKUToId.size()==0){
			productsSKUToId=new HashMap<String,String>();
			initProductsMap();
		}
		File file = new File(path);
		try {
			List<String> readLines = FileUtils.readLines(file);
			for (Iterator<String> iterator = readLines.iterator(); iterator.hasNext();) {
				String line = (String) iterator.next();
				//System.out.println(line);
				String[] attributes = line.split(",");
				String sku=attributes[0];
				if(sku!=null && sku.length()>0){
					sku=sku.substring(2, sku.length()-1);
					String price=attributes[1];
					String priceBefore=attributes[2];
					String inStock=attributes[3];
					Item item = new Item(sku,price,priceBefore,inStock);
					addToMainArray(item);
				}
			}
			divideProductsArray();
//			updateEcwid();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			file.delete();
			productsSKUToId=new HashMap<String,String>();
			productsToBeUpdated=new JSONArray();
		}
	}
	
//	private static Map<String,JSONArray> arraysToBeUploaded=new HashMap<String,JSONArray>();
	
	private static Map<String,String> productsSKUToId=null;//=new HashMap<String,String>();
	
	private static void initProductsMap() throws HttpException, IOException{
		System.out.println("Starting method initProductsMap to initalize products from ECWID "+" on:"+Calendar.getInstance().getTime());
		GetMethod get = null;
		//http://app.ecwid.com/api/v1/[STORE-ID]/products
		HttpClient httpclient = new HttpClient();
		get=new GetMethod("https://app.ecwid.com/api/v1/1775149/products?hidden_products=true&secure_auth_key=gyM8Nf9X8xVP");
		httpclient.executeMethod(get);
		InputStream in = get.getResponseBodyAsStream();
		JsonData json = new JsonData(in);
		System.out.println("Got a list of products from ECWID with size:"+json.get("").size()+" on:"+Calendar.getInstance().getTime());
		for(int i=0;i<json.get("").size();i++){
			String id=String.valueOf(json.get("["+i+"]/id").getIntValue());
//			System.out.println("ID:"+id);
			String sku=json.get("["+i+"]/sku").getTextValue();
//			System.out.println("SKU:"+sku);
			productsSKUToId.put(sku, id);
		}
		System.out.println("Finished init the Map "+" on:"+Calendar.getInstance().getTime());
	}
	
	private static void divideProductsArray() {
		int lastIndex=0;
		int size=productsToBeUpdated.size();
		boolean done=false;
		for(int i=0;i<size;i=lastIndex){
			JSONArray tempArray = new JSONArray();
			
			if(lastIndex+200<size){
				lastIndex=lastIndex+200;
				tempArray.addAll(productsToBeUpdated.subList(lastIndex-200, lastIndex) );
				try {
					updateEcwid(tempArray);
					done=true;
				} catch (IOException e) {
					System.out.println("IOException in method divideProductsArray message:"+e.getMessage());
				}
			}else{
				tempArray.addAll(productsToBeUpdated.subList(lastIndex, size));
				lastIndex=size;
				try {
					updateEcwid(tempArray);
					done=true;
				} catch (IOException e) {
					System.out.println("IOException in method divideProductsArray message:"+e.getMessage());
				}
			}
		}
		if(done){
			Emails.sendEmail("orders@3albab.com", "Products Prices Updated Successfully" , "Products on ECWID <b>updated</b> successfully on <b>"+Calendar.getInstance(TimeZone.getTimeZone("Africa/Cairo")).getTime()+"</b>");
		}else{
			Emails.sendEmail("orders@3albab.com", "Products Prices NOT Updated Successfully" , "Products on ECWID <b>not updated</b> successfully on <b>"+Calendar.getInstance(TimeZone.getTimeZone("Africa/Cairo")).getTime()+"</b>");
		}
	}

	private static void addToMainArray(Item item) throws HttpException, IOException{
//		System.out.println("Starting method contactEcwid with item: "+item);
		//TODO: Commented Out
//		if(productsSKUToId==null){
//			productsSKUToId=new HashMap<String,String>();
//			initProductsMap();
//		}
//		System.out.println("Will search for SKU:"+item.getSku());
		String id=productsSKUToId.get(item.getSku());
		
		if(id!=null && id.length()>0){
//			System.out.println("Product To be updated ID:"+id);			
			
			JSONObject jsonObj=new JSONObject();
			jsonObj.put("id", id);
			jsonObj.put("sku", item.getSku());
			jsonObj.put("price", item.getPrice());
			jsonObj.put("compareAtPrice", item.getPriceBefore());
			if(item.getInStock().equals("yes") || item.getInStock().equals("YES") || item.getInStock().equals("Yes")){
				jsonObj.put("enabled", true);
			}else{
				jsonObj.put("enabled", false);
			}
			productsToBeUpdated.add(jsonObj);
		}
	}
	
	private static int xx=0;
	
	private static boolean updateEcwid(JSONArray productJsonArray) throws IOException{
		System.out.println("Starting method updateEcwid with array of size "+productJsonArray.size()+" on:"+Calendar.getInstance().getTime());
		HttpClient httpclient = new HttpClient();
		PutMethod put = null;
		//put=new PutMethod("https://app.ecwid.com/api/v1/1775149/profile?secure_auth_key=gyM8Nf9X8xVP");
		put=new PutMethod("https://app.ecwid.com/api/v1/1775149/products?secure_auth_key=gyM8Nf9X8xVP");
//		System.out.println(productJsonArray);
		xx=xx+1;
//		System.out.println(xx+"--"+productJsonArray.size());
		//TODO: Part to update ECWID with new products from email
		StringRequestEntity requestEntity = new StringRequestEntity(productJsonArray.toString(),"application/json","UTF-8");
		//requestEntity = new StringRequestEntity("[{\"id\":\"33657199\",\"enabled\":false,\"price\":\"40.95\",\"compareAtPrice\":\"0\",\"sku\":\"\\\"2013\"}]","application/json","UTF-8");
//		System.out.println(requestEntity.getContent());
		put.setRequestEntity(requestEntity);
		put.setRequestHeader("Content-Type", "application/json");
		

		httpclient.executeMethod(put);
		InputStream in = put.getResponseBodyAsStream();
//		System.out.println("Getting Response");
//		System.out.println(new String(put.getResponseBody()));
		in.close();
		System.out.println("Products Updated");
		return true;
	}
	
	private static JSONArray productsToBeUpdated=new JSONArray();
	
	private static List<GmailMessage> getMessagesFromSender(){
		GmailClient client = new RssGmailClient();
		GmailConnection connection = new HttpGmailConnection(USERNAME,PASSWORD.toCharArray());
		client.setConnection(connection);
		final List<GmailMessage> messages = client.getUnreadMessages();
		for (GmailMessage message : messages) {
		    System.out.println(message);
		    System.out.println("Subject:"+message.getSubject());
		    System.out.println("Sender:"+message.getFrom());
		}
		return messages;
	}

	
	public void start() {
		System.out.println("Starting Thread UpdateECWID");
		Properties props = System.getProperties();
		props.setProperty("mail.store.protocol", "imaps");
		try {
		Session session = Session.getDefaultInstance(props, null);
		Store store = session.getStore("imaps");
		store.connect("imap.gmail.com", USERNAME, PASSWORD);
//		System.out.println(store);

		Folder inbox = store.getFolder("Inbox");
		inbox.open(Folder.READ_WRITE);
		
		 Flags seen = new Flags(Flags.Flag.SEEN);
	        FlagTerm unseenFlagTerm = new FlagTerm(seen, false);
	        Calendar cal=Calendar.getInstance();
//	        System.out.println("TimeZone:"+cal.getTimeZone());
	        cal.setTimeZone(TimeZone.getTimeZone("Africa/Cairo"));
//	        System.out.println("Calendar Egyptian Time Zone:"+cal.getTime());
	        cal.add(Calendar.HOUR_OF_DAY, -10);
//	        System.out.println("Calendar After subtracting 10 Hours:"+cal.getTime());
	        ReceivedDateTerm receivedDateTerm  = new ReceivedDateTerm(ComparisonTerm.GE,new Date(cal.getTimeInMillis()));
	        
	        SearchTerm[] searchTerm = new SearchTerm[3];
	        searchTerm[0]=new SubjectTerm("Items");
	        searchTerm[1]=receivedDateTerm;
	        searchTerm[2]=unseenFlagTerm;
	        SearchTerm st = new AndTerm(searchTerm);

	            // Get some message references
	        	
	            Message [] messages = inbox.search(st);
	            
	            Arrays.sort(messages,new MessagesByDate());
		
	            System.out.println("Unseen Emails Length:"+messages.length);
//	            System.out.println("");
	            if(messages.length>0){
//	            	System.out.println("messages[0]:"+messages[0]);
		            Message messageAlfa = messages[0];
//		            System.out.println(messageAlfa.getFrom()[0]);
		            cal= Calendar.getInstance();
		            cal.setTime(messageAlfa.getReceivedDate());
		            cal.setTimeZone(TimeZone.getTimeZone("Africa/Cairo"));
//						System.out.println(messageAlfa.getContent());
					messages[0].setFlag(Flag.SEEN, true);
		            getPart(messageAlfa);
	            }
		} catch (NoSuchProviderException e) {
			System.out.println("NoSuchProviderException in Start method in class UpdateEcwid while getting emails message:"+e.getMessage());
		} catch (MessagingException e) {
			System.out.println("MessagingException in Start method in class UpdateEcwid while getting emails message:"+e.getMessage());
		}
	}

	public void run() {
		System.out.println("MailCheckingUpdateECWIDPrices Thread Started at "+Calendar.getInstance().getTime().toString());
		start();
		System.out.println("MailCheckingUpdateECWIDPrices Thread Finished at "+Calendar.getInstance().getTime().toString());
		
	}
	
}
