package com.skogtek.dmk.ui;

import java.util.ArrayList;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ListAdapter extends BaseAdapter 
{
	private ArrayList<String> items;
	private ArrayList<DataSetObserver> observers;
	private Context context;
	private final int MAX_SIZE = 100;
	
	public ListAdapter(Context context)
	{
		this.context = context;
		items = new ArrayList<String>();
		observers = new ArrayList<DataSetObserver>();
	}
	
	public void appendItem(String text)
	{
		items.add(text);
		
		if(items.size() > MAX_SIZE)
			items.remove(0);
		
		for(int i=0; i<observers.size(); i++)
		{
			observers.get(i).onChanged();
		}
	}

	@Override
	public boolean areAllItemsEnabled() {
		return true;
	}

	@Override
	public boolean isEnabled(int position) {
		return position < items.size();
	}

	@Override
	public int getCount() {
		return items.size();
	}

	@Override
	public Object getItem(int position) {
		return items.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public int getItemViewType(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) 
	{
		TextView tv;
		if(convertView == null)
		{
			tv = new TextView(this.context);
			
		}else
		{
			tv = (TextView)convertView;
		}
		tv.setText(items.get(position));
		
		return tv;
	}

	@Override
	public int getViewTypeCount() {
		return 1;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public boolean isEmpty() {
		return items.size() == 0;
	}

	@Override
	public void registerDataSetObserver(DataSetObserver observer) {
		observers.add(observer);
	}

	@Override
	public void unregisterDataSetObserver(DataSetObserver observer) {
		observers.remove(observer);
		
	}

}
