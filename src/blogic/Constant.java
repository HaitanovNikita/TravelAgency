package blogic;

public class Constant {

	public static final String URL_H2 = "jdbc:h2:~/test";
	public static final String USER_NAME_H2 = "sa";
	public static final String PASSWORD_H2 = "";
	public static final String TABLE_NAME = "Person";
	public static final String CREATE_TABLE = "CREATE TABLE "+TABLE_NAME+" ( ID INT PRIMARY KEY, FNAME VARCHAR(255),LNAME VARCHAR(255), AGE INT);";
	public static final String DRIVER_CLASS_H2 = "org.h2.Driver";
	
	

	
	public enum FIELDS 
	{ID, FNAME, LNAME, AGE}
						
	
	
}
