package blogic;

public class Constant {

	public static final String TABLE_NAME = "Person";
	public static final String CREATE_TABLE = "CREATE TABLE "+TABLE_NAME+" ( ID INT PRIMARY KEY, FNAME VARCHAR(255),LNAME VARCHAR(255), AGE INT);";

	public enum FIELDS 
	{ID, FNAME, LNAME, AGE}
						
	
	
}
