package com.boudy.orders.components;

import java.util.Calendar;
import java.util.TimerTask;

public class CheckAlfaProductsEmailTask  extends TimerTask {
		  public void run() {
			  //myThreadObj.start();
			  System.out.println("Starting RUN in CheckAlfaProductsEmailTask to Start UpdateECWID on "+Calendar.getInstance().getTime());
			  (new Thread(new UpdateEcwid())).start();
			  System.out.println("Started thread RUN in CheckAlfaProductsEmailTask to Start UpdateECWID on "+Calendar.getInstance().getTime());
		  }
		
}
