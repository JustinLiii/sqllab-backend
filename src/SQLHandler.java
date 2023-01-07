import com.alibaba.fastjson2.JSONObject;

import java.sql.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SQLHandler {
    // MySQL 8.0 以下版本 - JDBC 驱动名及数据库 URL
//    static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
//    static final String DB_URL = "jdbc:mysql://localhost:3306/RUNOOB";
    public enum ActivityType {
        Creator,
        Applied,
        Approved,
        All
    }

    public SQLHandler(){
        initDB();
    }

    // MySQL 8.0 以上版本 - JDBC 驱动名及数据库 URL
    static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
//    static final String DB_URL = "jdbc:mysql://10.250.220.87:3306";

    static final String DB_URL = "jdbc:mysql://192.168.31.217:3306";

    static final String DB_NAME = "sqllab";


    // 数据库的用户名与密码，需要根据自己的设置
    static final String USER = "mysql";
    static final String PASS = "mysql";

    protected Connection conn = null;

    protected void initDB() {
//        Statement stmt = null;
        try{
            // 注册 JDBC 驱动
            Class.forName(JDBC_DRIVER);

            // 打开链接
            System.out.println("连接数据库...");
            conn = DriverManager.getConnection(DB_URL,USER,PASS);
            System.out.println("完成");

//            // 执行查询
//            System.out.println("实例化Statement对象...");
//            stmt = conn.createStatement();
//            String sql;
//            sql = "USE test";
//            stmt.execute(sql);
//            sql = "SELECT name, passwd FROM users";
//            ResultSet rs = stmt.executeQuery(sql);
//
//            // 展开结果集数据库
//            while(rs.next()){
//                // 通过字段检索
//                String name = rs.getString("name");
//                String passwd = rs.getString("passwd");
//
//                // 输出数据
//                System.out.print("用户名: " + name);
//                System.out.print(", 密码: " + passwd);
//                System.out.print("\n");
//            }
            // 完成后关闭
//            rs.close();
//            stmt.close();
        }catch(SQLException se){
            // 处理 JDBC 错误
            se.printStackTrace();
        }catch(Exception e){
            // 处理 Class.forName 错误
            e.printStackTrace();
        }
//        finally{
//            try{
//                if(stmt!=null) stmt.close();
//            }catch(SQLException se){
//                se.printStackTrace();
//            }
//        }
    }

    public boolean[] loginCheck(String name, String passwd) {
        String sql = "SELECT u_passwd, u_admin FROM userInfo WHERE userInfo.u_name='" + name+"'";
        System.out.println("Logging user "+name);
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("use "+ DB_NAME);
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next() && rs.getString("u_passwd").contentEquals(passwd)) {
                if (rs.getBoolean("u_admin")) {
                    return new boolean[]{true,true};
                } else {
                    return new boolean[]{true,false};
                }
            } else {
                return new boolean[]{false,false};
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new boolean[]{false,false};
    }

    public boolean checkSignUp(String name){
        String sql = "SELECT u_name  FROM userInfo WHERE userInfo.u_name='" + name+"'";
        System.out.println("Checking user "+name);
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("use "+ DB_NAME);
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                return false;
            } else {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void signUp(String name, String passwd, boolean admin) {
        String sql = "INSERT INTO userInfo VALUES ('"+name+"','"+passwd+"',"+admin;
        System.out.println("Signing user "+name);
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("use "+ DB_NAME);
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<JSONObject> getList(String name, ActivityType type) {
        switch (type) {
            case Creator -> {
                String sql = "SELECT * FROM sqllab.activity WHERE a_creator='" + name + "'";
                System.out.println(sql);
                List<JSONObject> returnList = new ArrayList<>();
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("use " + DB_NAME);
                    ResultSet rs = stmt.executeQuery(sql);
                    while (rs.next()) {
                        JSONObject result = getActivityInfo(rs);

                        System.out.println("Get activity"+ result.getString("name"));

                        returnList.add(result);
                    }
                    return returnList;
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            case All -> {
                String sql = "SELECT * FROM sqllab.activity";
                System.out.println(sql);
                List<JSONObject> returnList = new ArrayList<>();
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("use " + DB_NAME);
                    ResultSet rs = stmt.executeQuery(sql);
                    while (rs.next()) {
                        JSONObject result = getActivityInfo(rs);

                        System.out.println("Get activity"+ result.getString("name"));

                        returnList.add(result);
                    }
                    return returnList;
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            case Applied -> {return null;}
            case Approved -> {return null;}
        }
        return null;
    }

    public void close(){
        try {
            conn.close();
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    private JSONObject getActivityInfo(ResultSet rs) throws SQLException{
        JSONObject result = new JSONObject();
        result.put("id", rs.getInt("a_id"));
        result.put("creator", rs.getString("a_creator"));
        result.put("name", rs.getString("a_name"));
        result.put("start_time", rs.getDate("a_start_time").getTime() + rs.getTime("a_start_time").getTime() + 28800000);
        result.put("end_time", rs.getDate("a_end_time").getTime() + rs.getTime("a_end_time").getTime() + 28800000);
        result.put("deadline", rs.getDate("a_apply_deadline").getTime() + rs.getTime("a_apply_deadline").getTime() + 28800000);
        result.put("person_needed", rs.getInt("a_person_needed"));
        result.put("description", rs.getString("a_description"));
        result.put("pic", rs.getBytes("a_pic"));
        result.put("active", rs.getBoolean("a_active"));
        result.put("address", rs.getString("a_active"));
        result.put("modified_time", rs.getDate("a_modified_time").getTime() + rs.getTime("a_modified_time").getTime() + 28800000);

        return result;
    }
}
