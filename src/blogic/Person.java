package blogic;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Person 
{	
	@Id	
	public int id;
	public String fname;
	public String lname;
	public int age;

	public Person() {}

	public Person(int id, String fname, String lname, int age)
	{
	  this.id=id;
	  this.fname=fname;
	  this.lname=lname;
	  this.age=age;
		
	}
	@Override
	public String toString() 
	{
		return  "<person>\r\n"
				+"<id>"+id+"</id>\r\n"
				+"<fname>"+fname+"</fname>\r\n"
				+"<lname>"+lname+"</lname>\r\n"
				+"<age>"+age+"</age>\r\n"
				+"</person>\r\n";
	}
}


