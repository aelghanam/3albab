package com.boudy.orders.components;

public class Item{
	
	private String sku;
	private String priceBefore;
	private String price;
	private String inStock;
	
	public Item(String skuInput,String priceInput,String priceBeforeInput,String inStockInput) {
		setInStock(inStockInput);
		setPrice(priceInput);
		setPriceBefore(priceBeforeInput);
		setSku(skuInput);
	}
	
	@Override
	public String toString() {
		String output="Item sku:"+sku+" price:"+price+" price before:"+priceBefore+" inStock:"+inStock;
		return output;
	}
	
	public String getSku() {
		return sku;
	}
	public void setSku(String sku) {
		this.sku = sku;
	}
	public String getPriceBefore() {
		return priceBefore;
	}
	public void setPriceBefore(String priceBefore) {
		this.priceBefore = priceBefore;
	}
	public String getPrice() {
		return price;
	}
	public void setPrice(String price) {
		this.price = price;
	}
	public String getInStock() {
		return inStock;
	}
	public void setInStock(String inStock) {
		this.inStock = inStock;
	}
	
}
