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
	
	public AcquisitionControllerImpl(String prev,String to,int splitNum) {
		try {
			this.con = DriverManager.getConnection("jdbc:mysql://localhost:3306/test", "root", "1021");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		this.splitNum = splitNum;
		updateStation(prev, to);
	}
	
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
	public boolean canAcquisition(boolean result,long recentTime) {
		if(result){//Running
			long interval = 0;
			
			if(lastGettingTime > 0){//その区間で一度以上の測位をしている場合
				interval = recentTime - lastGettingTime;
			} else {
				interval = recentTime - lastStopTime;
			}
			
			if(interval > betweenTime){//測位間隔を超えた場合
				lastGettingTime = recentTime;
				return true;
			} else {
				return false;
			}
			
		} else {//Stop or other
			lastStopTime = recentTime;
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
	

	private long getBetweenTime(int prev,int to){
		try {
			Statement statement = con.createStatement();
			//昇順に変換
			int[] idArray = prev > to ? new int[]{to,prev}:new int[]{prev,to};
			String sql = "select * from join_table where prev_station="+Integer.toString(idArray[0])+" and to_station="+Integer.toString(idArray[1])+";";
			ResultSet resultSet = statement.executeQuery(sql);
			while(resultSet.next()){
				return resultSet.getLong("time");
				
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
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


	
	
}
