package com.boudy.orders.components;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;





public class Booking {

	public static ArrayList<int[]> nondecreasing_subsequences(int[] input){
		int now=0,next=0;
		ArrayList<Integer> tempList = new ArrayList<Integer>();
		ArrayList<int[]> list = new ArrayList<int[]>();
		for(int i=0;(i)<input.length;i++){
			if(i+1<input.length){
				now=input[i];
				next=input[i+1];
				if(now>next){
					tempList.add(now);
					list.add(ArrayUtils.toPrimitive( (Integer[]) tempList.toArray(new Integer[tempList.size()]) ));
					tempList = new ArrayList<Integer>();
				}else{
					tempList.add(now);
				}
			}else{
					tempList.add(next);
					list.add(ArrayUtils.toPrimitive( (Integer[]) tempList.toArray(new Integer[tempList.size()]) ));
			}
			
		}
		System.out.println("List Size:"+list.size());
		Object[] h=new Object[list.size()];
		for (int i = 0; i < list.size(); i++) {
			h[i]=list.get(i);
		}
		return list;
	}
	
	public static String[] all_anagram_groups(String[] x){
		HashMap<String, ArrayList<String>> map=new HashMap<String, ArrayList<String>>();
		for (int i = 0; i < x.length; i++) {
			String word = x[i];
			System.out.println("Before Sorting:"+word);
			char[] charArray=word.toCharArray();
			Arrays.sort(charArray);
			String sortedWord=new String(charArray);
			System.out.println("After Sorting:"+sortedWord);
			if(map.containsKey(sortedWord.trim())){
				( (ArrayList<String>) map.get(sortedWord.trim())).add(x[i]);
			}else{
				ArrayList<String> list = new ArrayList<String>();
				list.add(x[i]);
				map.put(sortedWord.trim(), list);
			}
		}
		Set<String> keySet = map.keySet();
		int t=0;
		for (String string : keySet) {
			ArrayList<String> list = (ArrayList) map.get(string);
			for (Iterator iterator = list.iterator(); iterator.hasNext();) {
				String stringg = (String) iterator.next();
				System.out.print("+"+stringg+"+");
				x[t]=stringg;
				t=t+1;
			}
			System.out.println("");
		}
		
		return x;
	}
	
//			page1 : distance == 0
//			   page2 : distance == 1
//			     page3 : distance == 2
//			     page4 : distance == 2
//			   page5 : distance == 1
//			     page3 : distance == 2
//			     page7 : distance == 2
	
	public static HashMap<String,HashMap<String,Integer>> hopsAvailable = new HashMap<String,HashMap<String,Integer>>();
	public static String get_hops_from(Page page1,Page page2){
		int distance=0;
		ArrayList<Page> get_links = get_links(page1);
		if(get_links.contains(page2)){
			return "1";
		}
		
		for (Iterator iterator = get_links.iterator(); iterator.hasNext();) {
			Page page = (Page) iterator.next();
			if(get_links(page).contains(page2)){
				return "2";
			}
		}
		
		return "Nop";
	}
	
	
	
	class Page {
		
	}
	
	public static ArrayList<Page> get_links(Page a_page){
		// will return an array/list of all pages that a_page links to
		return new ArrayList<Page>();
	}
	
	

	public static void main(String[] args) {
//		int[] x={1,2,3,9,8,76,5,4,3,2,1};
//		ArrayList<int[]> nondecreasing_subsequences = nondecreasing_subsequences(x);
//		for(int i=0;i<nondecreasing_subsequences.size();i++){
//			int[] temp=nondecreasing_subsequences.get(i);
//			for(int y:temp){
//				System.out.print("'"+y+"'");
//			}
//			System.out.println("----");
//		}
		String[] x={ "pear","dirty room","amleth","reap","tinsel","hamlet","dormitory","listen","silent" };
		all_anagram_groups(x);
	}

}