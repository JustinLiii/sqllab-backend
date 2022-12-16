import java.sql.*;

public class SQLHandler {
    // MySQL 8.0 以下版本 - JDBC 驱动名及数据库 URL
//    static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
//    static final String DB_URL = "jdbc:mysql://localhost:3306/RUNOOB";

    public SQLHandler(){
        initDB();
    }

    // MySQL 8.0 以上版本 - JDBC 驱动名及数据库 URL
    static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://10.250.220.87:3306";


    // 数据库的用户名与密码，需要根据自己的设置
    static final String USER = "mysql";
    static final String PASS = "mysql";

    protected Connection conn = null;
    protected Statement stmt = null;

    protected void initDB() {
        try{
            // 注册 JDBC 驱动
            Class.forName(JDBC_DRIVER);

            // 打开链接
            System.out.println("连接数据库...");
            conn = DriverManager.getConnection(DB_URL,USER,PASS);

            // 执行查询
            System.out.println("实例化Statement对象...");
            stmt = conn.createStatement();
            String sql;
            sql = "USE test";
            stmt.execute(sql);
            sql = "SELECT name, passwd FROM users";
            ResultSet rs = stmt.executeQuery(sql);

            // 展开结果集数据库
            while(rs.next()){
                // 通过字段检索
                String name = rs.getString("name");
                String passwd = rs.getString("passwd");

                // 输出数据
                System.out.print("用户名: " + name);
                System.out.print(", 密码: " + passwd);
                System.out.print("\n");
            }
            // 完成后关闭
            rs.close();
            stmt.close();
            conn.close();
        }catch(SQLException se){
            // 处理 JDBC 错误
            se.printStackTrace();
        }catch(Exception e){
            // 处理 Class.forName 错误
            e.printStackTrace();
        }finally{
            // 关闭资源
            try{
                if(stmt!=null) stmt.close();
            }catch(SQLException se2){
            }// 什么都不做
            try{
                if(conn!=null) conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }
        System.out.println("Goodbye!");
    }

    public boolean loginCheck(String name, String passwd) {
        String sql = "SELECT passwd  FROM users WHERE name=" + name;
        try {
            ResultSet rs = stmt.executeQuery(sql);
            if(rs.getString("passwd").contentEquals(passwd)){
                return true;
            } else {
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }
}
