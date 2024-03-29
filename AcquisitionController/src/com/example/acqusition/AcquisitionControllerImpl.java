package com.example.acqusition;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.example.controller.AcquisitionController;

public class AcquisitionControllerImpl implements AcquisitionController{

	private Connection con = null;
	private final int splitNum;
	private long betweenTime = Integer.MAX_VALUE;
	private long lastGettingTime = -1;
	private long lastStopTime = Integer.MAX_VALUE;
	private List<Integer> stationIdList;
	private boolean isPreviousStopped = false;
	private boolean firsttime = true;
	
	
	public AcquisitionControllerImpl(List<Integer> idList,int splitNum) {
		try {
			this.con = DriverManager.getConnection("jdbc:mysql://localhost:3306/mysql_test1", "root", "1021");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		this.stationIdList = new ArrayList<Integer>(idList);
		
		this.splitNum = splitNum;
		
		this.updateStation();
		
	}
	
	
	
	@Override
	public boolean canAcquisition(final boolean result,final long recentTime) {
		long recentTimeMillis = recentTime/1000000;
		if(result){//Running
			if(isPreviousStopped ){
				isPreviousStopped = false;
				if(firsttime){
					firsttime = false;
				} else {
					updateStation();
				}				
			}
			
			long interval = 0;
			if(lastGettingTime > 0){//その区間で一度以上の測位をしている場合
				interval = recentTimeMillis - lastGettingTime;
			} else {
				interval = recentTimeMillis - lastStopTime;
			}
			
			if(interval > betweenTime){//測位間隔を超えた場合
				lastGettingTime = recentTimeMillis;
				return true;
			} else {
				return false;
			}
			
		} else {//Stop or other
			lastStopTime = recentTimeMillis;
			isPreviousStopped = true;
			return false;
		}
	}

	@Deprecated
	@Override
	public void updateStation(String prevStation, String newStation) {
		
	}
	
	
	@Override
	public void updateStation(int prevId, int newId) {
		long runningTime = getBetweenTime(prevId, newId);
		System.out.println(runningTime);
		betweenTime = runningTime / (splitNum + 1);
		lastGettingTime = -1;
		
	}
	

	protected long getBetweenTime(int prev,int to){
		
		try{
			int[] idArray = getOrderedStationId(prev, to);
			String sql = "select * from join_table where prev_station="+Integer.toString(idArray[0])+" and to_station="+Integer.toString(idArray[1])+";";
			ResultSet resultSet = getQueryResult(sql);
			while(resultSet.next()){
				return resultSet.getLong("time");
				
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	
	protected int[] getOrderedStationId(int prev,int to){
		return prev > to ? new int[]{to,prev}:new int[]{prev,to};
	}
	
	protected ResultSet getQueryResult(final String sql){
		try {
			Statement statement = con.createStatement();
			ResultSet resultSet = statement.executeQuery(sql);
			return resultSet;
		} catch (SQLException e) {
			e.printStackTrace();
			
		}
		return null;//仕方なくnullを返す
	}


	@Override
	public void updateStation() {
		if(this.stationIdList.size()<=1){
			System.out.println("最後の駅です");
			return;
		}
		int prevId = this.stationIdList.remove(0);
		updateStation(prevId, this.stationIdList.get(0));
		
	}
	
	protected long getBetweenTime(){
		return this.betweenTime;
	}

	protected List<Integer> getStationIdList(){
		return new ArrayList(this.stationIdList);
	}

	
	
}
