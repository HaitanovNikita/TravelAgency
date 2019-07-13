package blogic;

import java.sql.*;
import java.util.ArrayList;

public class PersonDaoMySql implements iPersonDao
{	
	private final String URL  = "jdbc:mysql://localhost/mydb";
	private final String USER = "root";
	private final String PASSWORD = "";

	
	@Override
	public void create(Person p) throws SQLException
	{
		Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
		Statement stmt = conn.createStatement();

		String query = "INSERT INTO " + Constant.TABLE_NAME + 
				"(" + Constant.FIELDS.ID + "," + Constant.FIELDS.FNAME + ", " + 
				Constant.FIELDS.LNAME + ", " + Constant.FIELDS.AGE + ") " +
				" VALUES(" + p.id + ", '" + p.fname + "', '" + p.lname + "', " + p.age + ");";
		System.out.println("query: "+query);
		int  rs = stmt.executeUpdate(query);		 
		stmt.close();
		conn.close();	 	

	}

	@Override
	public ArrayList<Person> read() throws SQLException {
		ArrayList<Person> pp = new ArrayList<>();

		try
		{
			Class.forName("com.mysql.jdbc.Driver");
			Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
			Statement st = conn.createStatement();
			ResultSet result = st.executeQuery("SELECT * FROM PERSON");

			while (result.next()) 
			{
				Person p = new Person(
						result.getInt(1),
						result.getString(2),
						result.getString(3),
						result.getInt(4)
						);

				pp.add(p);
			}

			result.close();
			st.close();
			conn.close();
		}
		catch (ClassNotFoundException e)
		{e.printStackTrace();}
		return pp;
	}

	@Override
	public void update(Person p) throws SQLException 
	{
		Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
			Statement stmt = conn.createStatement();
			String query = ("UPDATE " + Constant.TABLE_NAME + " SET " + Constant.FIELDS.FNAME+"="+ "'" + p.fname + "'"
					+ ", "+ Constant.FIELDS.LNAME+"="+"'" + p.lname + "'"+" ,"+Constant.FIELDS.AGE+"="+p.age
					+" WHERE ID=" + p.id + ";");
			 int  rs = stmt.executeUpdate(query);
			  stmt.close();
			  conn.close();	 	
	}

	@Override
	public void delete(Person p) throws SQLException 
	{
		  Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
		  Statement stmt = conn.createStatement();
		  String query = "DELETE FROM "+ Constant.TABLE_NAME+ " WHERE ID = "+p.id;
		  int  rs = stmt.executeUpdate(query);
		  stmt.close();
		  conn.close();	 	
	}

	

	
}
