package nl.tue.sec.cairis.db;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import nl.tue.sec.cairis.util.DBJerseyConfig;

public class DBAbstraction {
private static Connection connection = null;

	private static void connect(){
		try{
			Class.forName("com.mysql.jdbc.Driver");
			DBJerseyConfig dbProperties = new DBJerseyConfig();
			
			try {
				dbProperties.getPropValues();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
	  		// DBParameters.getDBHOST()
			String dbHost = dbProperties.getDBHost();

			// DBParameters.getDBPORT()
			String dbPort = dbProperties.getDBPort();
			
			// DBParameters.getDB()
			String db = dbProperties.getDB();
				
			// DBParameters.getDBUSER()
			String dbUser = dbProperties.getDBUser();
			
			// DBParameters.getDBPWD()
			String dbPassword = dbProperties.getDBPassword();
			
			/*
		    connection = DriverManager.getConnection("jdbc:mysql://"
		    				+DBParameters.getDBHOST()+":"+DBParameters.getDBPORT()+"/"+DBParameters.getDB(), 
		    				DBParameters.getDBUSER(), DBParameters.getDBPWD()); */
			
			connection = DriverManager.getConnection("jdbc:mysql://"
    				+ dbHost + ":" + dbPort + "/" + db, 
    				dbUser, dbPassword);
		} catch(ClassNotFoundException | SQLException e){
			System.out.println("Error in DBAbstraction connect method of sfxservice");
		   	e.printStackTrace();
		}
	}
	
	private static boolean disconnect(){
		try{
		    if(connection != null){
		    	 System.out.println("SQL Connection to database closed");
		        connection.close();
		    	return true;
		    }
		    else
		    	 return false;
		}catch(SQLException e){
			e.printStackTrace();
		}
		return false;
	}
	
	public static int insertStatement(String query, List<String> variables){
	 try{	
		 		connect();
				PreparedStatement ps = connection.prepareStatement(query,Statement.RETURN_GENERATED_KEYS);
				for(int i=0;i<variables.size();i++)
					ps.setString((i+1), variables.get(i));
				int rs = ps.executeUpdate();
				int auto_id=0;
				if(rs>0) {
				    ResultSet idrs = ps.getGeneratedKeys();
				    idrs.next();
				    auto_id = idrs.getInt(1);
				}
				System.out.println("generated key is "+ auto_id);
				disconnect();
				return auto_id;
		}catch(Exception e){
				 e.printStackTrace();
		}
	 return 0;
	}
	
	public static boolean updateStatement(String query, List<String> variables){
		 try{		
				connect();
				PreparedStatement ps = connection.prepareStatement(query);
				for(int i=0;i<variables.size();i++)
					ps.setString((i+1), variables.get(i));
				int updateStatus = ps.executeUpdate();
			
				disconnect();
				return (updateStatus>0);
		 }catch(Exception e){
			 e.printStackTrace();
	}
		 return false;
		
	}
	
	public static boolean deleteStatement(String query, List<String> variables){
		 try{		
				connect();
				PreparedStatement ps = connection.prepareStatement(query);
				for(int i=0;i<variables.size();i++)
					ps.setInt((i+1), Integer.parseInt(variables.get(i)));
				int updateStatus = ps.executeUpdate();
			
				disconnect();
				return (updateStatus>0);
		 }catch(Exception e){
			 e.printStackTrace();
	}
		 return false;
		
	}
	
	public static ArrayList<String> selectRecords(String query, List<String> variables){
		ArrayList<String> values=new ArrayList<String>();
		try{
			connect();
			PreparedStatement ps = connection.prepareStatement(query);
			if(variables==null)
				variables=new ArrayList<String>();
			for(int i=0;i<variables.size();i++)
				ps.setString((i+1), variables.get(i));
			System.out.println(ps.toString());
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {              
		        values.add(rs.getString(1));
		        }
			disconnect();
		}catch(SQLException e){
			e.printStackTrace();
		}
		return values;
	}
	
	public static String selectRecord(String query, List<String> variables){
		ArrayList<String> values=new ArrayList<String>();
		try{
			connect();
			PreparedStatement ps = connection.prepareStatement(query);
			if(variables==null)
				variables=new ArrayList<String>();
			for(int i=0;i<variables.size();i++)
				ps.setString((i+1), variables.get(i));
			System.out.println(ps.toString());
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {              
		        values.add(rs.getString(1));
		        }
			disconnect();
		}catch(SQLException e){
			e.printStackTrace();
		}
		if(values.size()>0)
			return values.get(0);
		else
			return null;
	}
	// CAIRIS Updates
	public static ArrayList<String> selectColumns(String query, List<String> variables){
		ArrayList<String> columns=new ArrayList<String>();

		try{
			connect();
			PreparedStatement ps = connection.prepareStatement(query);
			if(variables==null)
				variables=new ArrayList<String>();
			for(int i=0;i<variables.size();i++)
				ps.setString((i+1), variables.get(i));
			System.out.println(ps.toString());
			ResultSet rs = ps.executeQuery();

			if (!rs.isBeforeFirst() ) {    
				System.out.println("Empty Result set");
				columns=null;
			} 
			else{
				while (rs.next()) {
					columns.add(rs.getString("ccid"));
					columns.add(rs.getString("cairisuname"));
					columns.add(rs.getString("cairispwd"));
				}
			}
			disconnect();
		}catch(SQLException e){
			e.printStackTrace();
		}

		return columns;
	}
// End CAIRIS updates
}