package ahd.wan;

import java.sql.*;
import java.util.ArrayList;


/**
* Verwaltet die Verbindung mit der Datenbank
* Baut die Verbindung auf, stellt die Anfragen an die Datenbank und schlieﬂt die Verbindung wieder
*/

public class DBConnection
{
	private String DatabaseIP;
	private Connection con = null;
	private String url;
	private String user;
	private String password;
	private ArrayList<String> sqlbatch = new ArrayList<String>();

	public DBConnection(String DatabaseIP, String dbName, String user, String password)
	{
		this.DatabaseIP=DatabaseIP;
		this.user=user;
		this.password=password;
		try
		{
			Class.forName("com.mysql.jdbc.Driver");
		}catch(ClassNotFoundException ex){System.out.println("Treiberfehler!");}
		
		//String con_str = "//" + DatabaseIP + ":3306/"+dbName;
		String con_str = "//localhost/"+dbName;
		url= "jdbc:mysql:"+con_str;
		connect();
	}

	public void connect()
	{
		try
		{
			con = DriverManager.getConnection(url, user, password);
		}catch (SQLException ex){ System.out.println(ex.getMessage());}	
	}

	public void close()
	{
		try
		{
			con.close();
		}catch(SQLException ex){System.out.println("ERRORclose: "+ex.getMessage());}
	}

	public int sql_update( String query )
	{
		try
		{
			Statement stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			int c = stmt.executeUpdate(query);
			stmt.close();
			return c;
		}catch(Exception e){System.out.println("ERRORupd: "+e.getMessage()); return 0;}
	}
	
	public ResultSet sql_query(String query)
	{
		//System.out.println(query);
		try
		{
			Statement stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			ResultSet rs = stmt.executeQuery(query);
			return rs;
		}catch(Exception e){System.out.println("ERRORqry: "+e.getMessage()+" ; "+query);}
		return null;
	}
	
	public void sql_addbatch(String query)
	{
		sqlbatch.add(query);
	}
	
	public int[] sql_executebatch()
	{
		try
		{
			Statement stmt = con.createStatement();
			for (String i: sqlbatch)
			{
				stmt.addBatch(i);
			}
			sqlbatch.clear();
			return stmt.executeBatch();
		}
		catch(Exception e)
		{
			System.out.println("ERRORbatch: "+e.getMessage()+" ; ");
			sqlbatch.clear();
			return null;
		}
	}

}