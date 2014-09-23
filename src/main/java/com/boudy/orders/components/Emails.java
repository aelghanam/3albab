package com.boudy.orders.components;

import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;

public class Emails {

//	public static void main(String [] args)
//	   {
//		Orders orders=new Orders();
//		try {
//			orders.updateOrderStatusOnEcwid("1400501");
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	   }
	
	
	public static void sendEmail(String to,String subject,String body){
	      
		if(to==null || to.length()==0){
	      // Recipient's email ID needs to be mentioned.
	      to = "api@durely.com";
		}

		//to="boudy00@hotmail.com";
	      // Sender's email ID needs to be mentioned
	      String from = "api@durely.com";

	      Properties props = System.getProperties();
	      props.put("mail.smtp.host", "smtp.gmail.com");
			props.put("mail.smtp.socketFactory.port", "465");
			props.put("mail.smtp.socketFactory.class",
					"javax.net.ssl.SSLSocketFactory");
			props.put("mail.smtp.auth", "true");
			props.put("mail.smtp.port", "465");
	 
			Session session = Session.getInstance(props,
				new javax.mail.Authenticator() {
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(UpdateEcwid.USERNAME,UpdateEcwid.PASSWORD);
					}
				});

	      try{
	    	  
	         // Create a default MimeMessage object.
	         MimeMessage message = new MimeMessage(session);

	         // Set From: header field of the header.
	         message.setFrom(new InternetAddress(from));

	         // Set To: header field of the header.
	         message.addRecipient(Message.RecipientType.TO,
	                                  new InternetAddress(to));

	         // Set Subject: header field
	         message.setSubject(subject);

	         // Send the actual HTML message, as big as you like
	         message.setContent(body,
	                            "text/html" );

	         // Send message
	         Transport.send(message);
	         System.out.println("Sent message successfully....");
	      }catch (MessagingException mex) {
	         mex.printStackTrace();
	      }
	   }
	
	
	public static void sendEmailAndAttachment(String to,String subject,String body,Document doc,String path){
	      
		if(to==null || to.length()==0){
	      // Recipient's email ID needs to be mentioned.
	      to = "api@durely.com";
		}

		//to="boudy00@hotmail.com";
	      // Sender's email ID needs to be mentioned
	      String from = "api@durely.com";

	      Properties props = System.getProperties();
	      props.put("mail.smtp.host", "smtp.gmail.com");
			props.put("mail.smtp.socketFactory.port", "465");
			props.put("mail.smtp.socketFactory.class",
					"javax.net.ssl.SSLSocketFactory");
			props.put("mail.smtp.auth", "true");
			props.put("mail.smtp.port", "465");
	 
			Session session = Session.getInstance(props,
				new javax.mail.Authenticator() {
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(UpdateEcwid.USERNAME,UpdateEcwid.PASSWORD);
					}
				});

	      try{
	    	  
	         // Create a default MimeMessage object.
	         MimeMessage message = new MimeMessage(session);

	         // Set From: header field of the header.
	         message.setFrom(new InternetAddress(from));

	         // Set To: header field of the header.
	         message.addRecipient(Message.RecipientType.TO,
	                                  new InternetAddress(to));

	         // Set Subject: header field
	         message.setSubject(subject);

	         // Send the actual HTML message, as big as you like
	         message.setText(body);
	         
	         Multipart multipart = new MimeMultipart("mixed");
				DocumentBuilderFactory docFactory = DocumentBuilderFactory
						.newInstance();
				DocumentBuilder docBuilder=null;
				try {
					docBuilder = docFactory.newDocumentBuilder();
				} catch (ParserConfigurationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				// root elements
				//doc = docBuilder.newDocument();
				//Element rootElement = doc.createElement("NewDataSet");
				//doc.appendChild(rootElement);
	         
				path=Orders.createFileLocally(path, doc,"");
				System.out.println(path);
	         MimeBodyPart messageBodyPart = new MimeBodyPart();
	 	     DataSource sourcee = new FileDataSource(path);
	 	     messageBodyPart.setDataHandler(new DataHandler(sourcee));
	 	     messageBodyPart.setFileName(sourcee.getName());
	 	     multipart.addBodyPart(messageBodyPart);
	 	    
	         
	         message.setContent(multipart);

	         
	         // Send message
	         Transport.send(message);
	         System.out.println("Sent message successfully....");
	      }catch (MessagingException mex) {
	         mex.printStackTrace();
	      }
	   }

}
