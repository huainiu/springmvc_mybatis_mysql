package com.hb.util.db.tool;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 利用Java将Oracle数据表生成JPA实体对象<a>http://201212262922.iteye.com/blog/2091849</a>
 * 
 * 从数据库表反射出实体类，自动生成实体类
 *
 */
public class GenEntityOracle {
	
	private String packageOutPath = "cn.lry.iteye.jpa.po";//指定实体生成所在包的路径
	private String authorName = "lry";//作者名字
	private String tablename = "";//表名
	private String[] colnames; // 列名数组
	private String[] colTypes; //列名类型数组
	private int[] colSizes; //列名大小数组
	private boolean f_util = false; // 是否需要导入包java.util.*
	private boolean f_sql = false; // 是否需要导入包java.sql.*
	private boolean f_jpa = true; // 是否需要生成基于注解的JPA实体对象
    
    //数据库连接
	 private static final String URL ="jdbc:oracle:thin:@211.83.72.133:1521:orcl";
	 private static final String NAME = "group0103";
	 private static final String PASS = "root";
	 private static final String DRIVER ="oracle.jdbc.driver.OracleDriver";
	 
	
	/*
	 * 构造函数
	 */
	public GenEntityOracle(){
		//获取表名称
		List<String> list=getTableName();
		for(int p=0;p<list.size();p++){
			tablename=list.get(p);
	    	//创建连接
	    	Connection con=null;
			//查要生成实体类的表
	    	String sql = "select * from " + tablename;
	    	Statement pStemt = null;
	    	try {
	    		try {
					Class.forName(DRIVER);
				} catch (ClassNotFoundException e1) {
					e1.printStackTrace();
				}
	    		con = DriverManager.getConnection(URL,NAME,PASS);
				pStemt = (Statement) con.createStatement();
				ResultSet rs = pStemt.executeQuery(sql);
				ResultSetMetaData rsmd = rs.getMetaData();
				int size = rsmd.getColumnCount();	//统计列
				colnames = new String[size];
				colTypes = new String[size];
				colSizes = new int[size];
				for (int i = 0; i < size; i++) {
					colnames[i] = rsmd.getColumnName(i + 1);
					colTypes[i] = rsmd.getColumnTypeName(i + 1);
					
					if(colTypes[i].equalsIgnoreCase("date") || colTypes[i].equalsIgnoreCase("timestamp")){
						f_util = true;
					}
					if(colTypes[i].equalsIgnoreCase("blob") || colTypes[i].equalsIgnoreCase("char")){
						f_sql = true;
					}
					colSizes[i] = rsmd.getColumnDisplaySize(i + 1);
				}
				
				String content = parse(colnames,colTypes,colSizes);
				
				try {
					File directory = new File("");
					String outputPath = directory.getAbsolutePath()+ "/src/"+this.packageOutPath.replace(".", "/")+"/"+initcap(tablename) + ".java";
					FileWriter fw = new FileWriter(outputPath);
					PrintWriter pw = new PrintWriter(fw);
					pw.println(content);
					pw.flush();
					pw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
			} catch (SQLException e) {
				e.printStackTrace();
			} finally{
				try {
					con.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		System.out.println("读写完毕！");
    }
	 /**
	  * Java方法 得到当前数据库下所有的表名 
	  * @param con
	  */
	private List<String> getTableName() {
		 List<String> list=new ArrayList<String>();
		 try {  
			 DatabaseMetaData meta = DriverManager.getConnection(URL,NAME,PASS).getMetaData();  
			 ResultSet rs = meta.getTables(null, null, null,new String[]{"TABLE"});
			 while (rs.next()) {
				 //获取当前管理员的表的名称
				 if(rs.getString(2).equals(NAME.toUpperCase())){
					 list.add(rs.getString(3));  
				 }
			 }  
		   }catch(Exception e){  
			   e.printStackTrace();  
		   }
		return list;
	 }
	/**
	 * 功能：生成实体类主体代码
	 * @param colnames
	 * @param colTypes
	 * @param colSizes
	 * @return
	 */
	private String parse(String[] colnames, String[] colTypes, int[] colSizes) {
		StringBuffer sb = new StringBuffer();
		
		sb.append("package " + this.packageOutPath + ";\r\n");
		sb.append("\r\n");
		
		//判断是否导入工具包
		if(f_util){
			sb.append("import java.util.Date;\r\n");
		}
		if(f_sql){
			sb.append("import java.sql.*;\r\n");
		}
		if(f_jpa){
			sb.append("import javax.persistence.Entity;\r\n");
			sb.append("import javax.persistence.GeneratedValue;\r\n");
			sb.append("import javax.persistence.GenerationType;\r\n");
			sb.append("import javax.persistence.Id;\r\n");
		}
		
		//注释部分
		sb.append("/**\r\n");
		sb.append("  * "+tablename+" 实体类\r\n");
		sb.append("  * "+new Date()+"\r\n");
		sb.append("  * @"+this.authorName+"\r\n");
		sb.append("  */\r\n");
		
		if(f_jpa){
			sb.append("@Entity\r\n");
		}
		//实体部分
		sb.append("public class " + initcap(tablename) + "{\r\n");
		processAllAttrs(sb);//属性
		processAllMethod(sb);//get set方法
		sb.append("}\r\n");
		
		return sb.toString();
	}
	
	/**
	 * 功能：生成所有属性
	 * @param sb
	 */
	private void processAllAttrs(StringBuffer sb) {
		
		for (int i = 0; i < colnames.length; i++) {
			sb.append("\tprivate " + sqlType2JavaType(colTypes[i]) + " " + colnames[i] + ";\r\n");
		}
		
	}

	/**
	 * 功能：生成所有方法
	 * @param sb
	 */
	private void processAllMethod(StringBuffer sb) {
		
		for (int i = 0; i < colnames.length; i++) {
			sb.append("\tpublic void set" + initcap(colnames[i]) + "(" + sqlType2JavaType(colTypes[i]) + " " + colnames[i] + "){\r\n");
			sb.append("\t\tthis." + colnames[i] + "=" + colnames[i] + ";\r\n");
			sb.append("\t}\r\n");
			if(f_jpa){
				if(i==0){
					sb.append("\t@Id\r\n");
					sb.append("\t@GeneratedValue(strategy = GenerationType.AUTO)\r\n");
					sb.append("\tpublic " + sqlType2JavaType(colTypes[i]) + " get" + initcap(colnames[i]) + "(){\r\n");
				}else{
					sb.append("\tpublic " + sqlType2JavaType(colTypes[i]) + " get" + initcap(colnames[i]) + "(){\r\n");
				}
			}else{
				sb.append("\tpublic " + sqlType2JavaType(colTypes[i]) + " get" + initcap(colnames[i]) + "(){\r\n");
			}
			sb.append("\t\treturn " + colnames[i] + ";\r\n");
			sb.append("\t}\r\n");
		}
		
	}
	
	/**
	 * 功能：将输入字符串的首字母改成大写
	 * @param str
	 * @return
	 */
	private String initcap(String str) {
		
		char[] ch = str.toCharArray();
		if(ch[0] >= 'a' && ch[0] <= 'z'){
			ch[0] = (char)(ch[0] - 32);
		}
		
		return new String(ch);
	}

	/**
	 * 功能：获得列的数据类型
	 * @param sqlType
	 * @return
	 */
	private String sqlType2JavaType(String sqlType) {
		
		if(sqlType.equalsIgnoreCase("binary_double")){
			return "double";
		}else if(sqlType.equalsIgnoreCase("binary_float")){
			return "float";
		}else if(sqlType.equalsIgnoreCase("blob")){
			return "byte[]";
		}else if(sqlType.equalsIgnoreCase("blob")){
			return "byte[]";
		}else if(sqlType.equalsIgnoreCase("char") || sqlType.equalsIgnoreCase("nvarchar2") 
				|| sqlType.equalsIgnoreCase("varchar2")){
			return "String";
		}else if(sqlType.equalsIgnoreCase("date") || sqlType.equalsIgnoreCase("timestamp")
				 || sqlType.equalsIgnoreCase("timestamp with local time zone") 
				 || sqlType.equalsIgnoreCase("timestamp with time zone")){
			return "Date";
		}else if(sqlType.equalsIgnoreCase("number")){
			return "Long";
		}
		
		return "String";
	}
	
	/**
	 * 出口
	 * TODO
	 * @param args
	 */
	public static void main(String[] args) {
		new GenEntityOracle();
	}

}
