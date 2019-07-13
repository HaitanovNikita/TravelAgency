package blogic;

import java.sql.SQLException;
import java.util.ArrayList;

public interface iPersonDao 
{
	void create(Person p) throws SQLException ;
	ArrayList<Person> read() throws SQLException;
	void update(Person p) throws SQLException;
	void delete(int id) throws SQLException;
	
}
