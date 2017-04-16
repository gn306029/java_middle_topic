package java_middel_topic;

import java.sql.*;
import java.util.ArrayList;

public class JDBC {

    String classname = "com.mysql.jdbc.Driver";
    String jdbcURL = "jdbc:mysql://db.mis.kuas.edu.tw/s1104137110";
    String UID = "s1104137110";
    String PWD = "gn306029";
    Statement statement;
    Connection conn = null;

    public boolean insertAccount(String account, String password,int port,String last_login) { //新增使用者

        boolean state_num = false; //狀態碼 true = 帳號無重複 , false = 帳號重複
        if (checkAccount(account)) {
            //無帳號重複
            String sql = "INSERT INTO `bbs_client`(`bbs_client_id`, `bbs_client_account`, `bbs_client_password`,`bbs_client_port`,`bbs_client_last_login`) VALUES (null,'" + account + "','" + password + "','"+port+"','"+last_login+"')";
            try {
                sqlConnection();
                this.statement.executeUpdate(sql);
                this.statement.close();
                this.conn.close();
                state_num = true;
            } catch (Exception e) {
                System.out.println(e + " in JDBC.java line 25");
                state_num = false;
            }
        } else {
            //帳號重複
            state_num = false;
        }

        return state_num;
    }

    private void sqlConnection() {
        try {
            Class.forName(this.classname).newInstance();
            this.conn = DriverManager.getConnection(jdbcURL, UID, PWD);
            this.statement = conn.createStatement();
        } catch (Exception e) {
            System.out.println(e + " in JDBC.java line 38");
        }
    }

    private boolean checkAccount(String account) { //檢查帳號是否重複

        String sql = "SELECT `bbs_client_account` FROM `bbs_client` WHERE `bbs_client_account` = '" + account + "'";
        boolean isAccount = true; // true = 可以使用 , false = 不可以使用
        try {
            sqlConnection();
            ResultSet rs = statement.executeQuery(sql);
            if (rs.next()) {
                isAccount = false;
            } else {
                isAccount = true;
            }
            statement.close();
            conn.close();
        } catch (Exception e) {
            System.out.println(e + " in JDBC.java line 52");
            isAccount = false;
        }
        return isAccount;
    }

    public boolean checkAccount(String account, String password) { //登入用 檢查帳號密碼

        String sql = "SELECT `bbs_client_account` , `bbs_client_password` FROM `bbs_client` WHERE `bbs_client_account` = '" + account + "' and `bbs_client_password` = '" + password + "'";
        boolean isAccount = true; // true 登入成功 , false = 登入失敗
        try {
            sqlConnection();
            ResultSet rs = this.statement.executeQuery(sql);
            if (rs.next()) {
                isAccount = true;
            } else {
                isAccount = false;
            }
        } catch (Exception e) {
            System.out.println(e + " in JDBC.Java line 65");
            isAccount = false;
        }
        return isAccount;
    }

    //使用者登入八卦版後
    //顯示文章用
    public ArrayList<BoardContent> initGossiping() {

        ArrayList<BoardContent> articale_list = new ArrayList<BoardContent>();
        String sql = "Select * From `bbs_gossiping`";
        sqlConnection();
        try {
            ResultSet rs = this.statement.executeQuery(sql);
            while (rs.next()) {
                BoardContent boardcontent = new BoardContent();
                boardcontent.setarticleID(rs.getString(1));
                boardcontent.setauthorID(rs.getString(2));
                boardcontent.setarticleTitle(rs.getString(3));
                boardcontent.setcontent(rs.getString(4));
                articale_list.add(boardcontent);
            }

        } catch (Exception e) {
            System.out.println(e);
        }
        return articale_list;
    }

    //除了 Search 之外的 SQL
    public int OtherSql(String sql) {
        int state = 0;
        sqlConnection();
        try {
            state = this.statement.executeUpdate(sql);
        } catch (Exception e) {
            System.out.println(e + " in JDBC.java OtherSql");
        }
        return state;
    }

    //要回傳值的查詢
    public String SearchSql(String sql) {
        sqlConnection();
        String result = "";
        try {
            ResultSet rs = this.statement.executeQuery(sql);
            while(rs.next()){
                for(int i =1;i<=rs.getMetaData().getColumnCount();i++){
                    if(i>1){
                        result += "\t";
                    }
                    result += rs.getString(i);
                }
                result += "\r\n";
            }
        } catch (Exception e) {
            System.out.println(e + " in JDBC.java OtherSql_2");
        }
        return result;
    }
}
