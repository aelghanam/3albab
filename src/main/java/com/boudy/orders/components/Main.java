package com.boudy.orders.components;

import java.awt.Toolkit;
import java.io.IOException;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class Main {

	Timer timer;
	Toolkit toolkit;

	public Main(int seconds) {
		System.out.println("Order Updating will work every "+seconds/60+" minutes.");
		timer = new Timer();
		timer.scheduleAtFixedRate(new RemindTask(),
				seconds*1000,        //initial delay
                seconds*1000);  //subsequent rate
		
		Calendar date = Calendar.getInstance();
//		date.set(
//		  Calendar.DAY_OF_WEEK,
//		  Calendar.SUNDAY
//		);		
//		date.set(Calendar.HOUR, 11);
//		date.set(Calendar.MINUTE, 0);
//		date.set(Calendar.SECOND, 0);
//		date.set(Calendar.MILLISECOND, 0);
//		// Schedule to run every Sunday in midnight
//		timer.schedule(
//		  new CheckAlfaProductsEmailTask(new UpdateEcwid()),
//		  date.getTime(),
//		  1000 * 60 * 60 * 24 * 7
//		);		
		// Schedule to run every Sunday in midnight
//		date.set(Calendar.HOUR_OF_DAY, 24);
//		date.set(Calendar.MINUTE, 0);
//		date.set(Calendar.SECOND, 0);
//		date.set(Calendar.MILLISECOND, 0);
//		1000 * 60 * 60 * 24 * 7
		System.out.println("Will Check for New Emails for products every "+3600/60+" minutes.");
		timer.scheduleAtFixedRate(
		  new CheckAlfaProductsEmailTask(),
		  0,
		  1000 * 3600
		);

	}
	
	
	public Main(int seconds,int emailSeconds) {
		System.out.println("Order Updating will work every "+seconds/60+" minutes.");
		timer = new Timer();
		timer.scheduleAtFixedRate(new RemindTask(),
				seconds*1000,        //initial delay
                seconds*1000);  //subsequent rate
		
		Calendar date = Calendar.getInstance();
//		date.set(
//		  Calendar.DAY_OF_WEEK,
//		  Calendar.SUNDAY
//		);		
//		date.set(Calendar.HOUR, 11);
//		date.set(Calendar.MINUTE, 0);
//		date.set(Calendar.SECOND, 0);
//		date.set(Calendar.MILLISECOND, 0);
//		// Schedule to run every Sunday in midnight
//		timer.schedule(
//		  new CheckAlfaProductsEmailTask(new UpdateEcwid()),
//		  date.getTime(),
//		  1000 * 60 * 60 * 24 * 7
//		);		
		// Schedule to run every Sunday in midnight
//		date.set(Calendar.HOUR_OF_DAY, 24);
//		date.set(Calendar.MINUTE, 0);
//		date.set(Calendar.SECOND, 0);
//		date.set(Calendar.MILLISECOND, 0);
//		1000 * 60 * 60 * 24 * 7
		if(emailSeconds==0){
			emailSeconds=3600;
		}
		System.out.println("Will Check for New Emails for products every "+emailSeconds/60+" minutes.");
		timer.scheduleAtFixedRate(
		  new CheckAlfaProductsEmailTask(),
		  0,
		  1000 * emailSeconds
		);
	}
	
	



	class RemindTask extends TimerTask {
		public void run() {
			System.out.println("Starting RUN TimerTask to START the Orders on "+Calendar.getInstance().getTime());
			(new Thread(new Orders())).start();
			System.out.println("Finished RUN TimerTask & START the Orders on "+Calendar.getInstance().getTime());
		}
	}

	public void cancel(){
		timer.cancel(); // Terminate the timer thread
	}
	
//	public static void main(String[] args) {
//		Orders orders=new Orders();
//		try {
//			orders.startApp();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		Main main=new Main(60,720);
//	}
	
}
