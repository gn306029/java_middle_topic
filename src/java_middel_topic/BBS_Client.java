package java_middel_topic;

import java.io.*;
import java.net.*;
import java.util.*;
import java.text.SimpleDateFormat;

public class BBS_Client extends Thread {

    private Socket incoming;
    private BufferedReader in;
    private PrintWriter out;
    private JDBC jdbc = new JDBC();
    static Vector lubby_1 = new Vector();
    private Gossiping gossiping;
    private String useraccount;
    private boolean isguest;

    public BBS_Client(Socket incoming, Gossiping gossiping) {
        this.incoming = incoming;
        this.gossiping = gossiping;
    }

    public void run() {
        try {
            String line;
            in = new BufferedReader(new InputStreamReader(incoming.getInputStream()));//建立 in 通道
            out = new PrintWriter(new OutputStreamWriter(incoming.getOutputStream()));//建立 out 通道
            BBS_Welcome();
            Login:
            while (true) { //登入指令迴圈
                if ((line = in.readLine()).equalsIgnoreCase("new")) {//註冊帳號
                    this.out.print("If you want cancel register , you can input '/exit' exit\r\n");
                    while (true) {
                        this.out.print("you want account : ");
                        this.out.flush();
                        String account = in.readLine();
                        if (account.equalsIgnoreCase("new") || account.equalsIgnoreCase("guest")) {
                            this.out.println("Can not use 'new' or 'guest' as an account\r\n");
                            this.out.flush();
                            continue;
                        }
                        if (account.equalsIgnoreCase("/exit")) {
                            BBS_Welcome();
                            break;
                        }
                        this.out.print("you want password : ");
                        this.out.flush();
                        String password = in.readLine();
                        if (account.equalsIgnoreCase("/exit")) {
                            break;
                        }
                        Date date = new Date();
                        SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
                        boolean state_num = jdbc.insertAccount(account, password, this.incoming.getPort(), sdFormat.format(date.getTime()));
                        if (state_num) {
                            //帳號建立成功
                            //建立成功後跳轉到看板 (未完成)
                            System.out.println("ok");
                            break Login;
                        } else {
                            //帳號重複，建立失敗
                            this.out.print("Account already exists\r\n");
                            this.out.flush();
                        }
                    }
                } else if (line.equalsIgnoreCase("guest")) {
                    //跳轉到看板
                    this.isguest = true;
                    break Login;
                } else {
                    String account = line;
                    this.out.print("input password : ");
                    this.out.flush();
                    String password = in.readLine();
                    if (jdbc.checkAccount(account, password)) {
                        //日期格式化 要存入資料庫
                        Date date = new Date();
                        SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
                        this.useraccount = account;
                        //更新資料庫使用者資料
                        jdbc.OtherSql("UPDATE `bbs_client` SET `bbs_client_port`='" + this.incoming.getPort() + "',`bbs_client_last_login`='" + sdFormat.format(date.getTime()) + "' WHERE `bbs_client_account` = '" + account + "'");
                        //登入成功
                        //跳轉到看板 (未完成)
                        synchronized (lubby_1) {
                            lubby_1.add(this);
                        }
                        this.out.print("Welcome\r\n");
                        this.out.flush();
                        break Login;
                    } else {
                        //登入失敗
                        this.out.print("Account or Password incorrect\r\n");
                        this.out.print("Input your account : ");
                        this.out.flush();
                    }
                }
            }

            //看板,聊天室,遊戲 指令迴圈 (在非任何看板,聊天室,遊戲輸入 fat nerd 的話,該使用者帳號會被刪除)
            //訪客只能瀏覽文章
            while (!(line = in.readLine()).equalsIgnoreCase("/exit")) {
                if (line.equalsIgnoreCase("/Gossiping")) {
                    this.out.println("Welcome to full love and peace the Gossiping");
                    this.out.println("You can input '/s+articale_id' to search article");
                    this.out.println("If you want publish article,You can input '/P' to do it");
                    this.out.println("Now start your gossip fantasy trip");
                    this.out.println("----------------------------------------------------------------------------");
                    this.out.flush();
                    gossiping.ShowArticaleList(this.out);
                    this.out.print("Input command : ");
                    this.out.flush();
                    String G_Command;
                    //八卦版迴圈
                    Gossiping_loop:
                    while (!(G_Command = in.readLine()).equalsIgnoreCase("/exit")) {
                        if (G_Command.substring(0, 2).equalsIgnoreCase("/s")) {
                            gossiping.ScaneerArticaleByID(G_Command.substring(2), this.out);
                        } else if (G_Command.equalsIgnoreCase("/P")) {
                            this.out.println("If you want cancel publish");
                            this.out.println("You can input /exit to do it");
                            this.out.flush();
                            String article_content = "";
                            String article_title;
                            //輸入標題
                            while (true) {
                                this.out.print("Input your article title : ");
                                this.out.flush();
                                article_title = in.readLine();
                                if (article_title.equalsIgnoreCase("/exit")) {
                                    continue Gossiping_loop;
                                }
                                this.out.print("This title ok ? ");
                                this.out.flush();
                                String answer = in.readLine();
                                if (answer.equalsIgnoreCase("OK")) {
                                    break;
                                }
                            }
                            //輸入文章內容
                            //尚未處理 SQL Injection
                            while (true) {
                                this.out.println("Input your article content");
                                this.out.println("If complete,Please input /wq to exit edit mode");
                                this.out.flush();
                                String inputcontent;
                                while (!(inputcontent = in.readLine()).equalsIgnoreCase("/wq")) {
                                    if (inputcontent.equalsIgnoreCase(line)) {
                                        continue Gossiping_loop;
                                    }
                                    article_content += inputcontent;
                                }
                                this.out.print("This content ok ? ");
                                this.out.flush();
                                String answer = in.readLine();
                                if (answer.equalsIgnoreCase("OK")) {
                                    gossiping.InsertArticale(this.useraccount, article_title, article_content, this.out);
                                    gossiping.ShowArticaleList(this.out);
                                    break;
                                }
                            }
                        } else if (G_Command.equalsIgnoreCase("/Delete")) {
                            this.out.print("Input your want to delete the article ID : ");
                            this.out.flush();
                            String article_id = in.readLine();
                            this.out.print("determine ? ");
                            this.out.flush();
                            String answer = in.readLine();
                            if (answer.equalsIgnoreCase("OK")) {
                                gossiping.DeleteArticle(article_id, this.useraccount, this.out);
                                gossiping.ShowArticaleList(this.out);
                            } else {
                                continue Gossiping_loop;
                            }
                        }
                        this.out.print("Input command : ");
                        this.out.flush();
                    }
                }else if(line.equalsIgnoreCase("fat nerd")){ //輸入肥宅會因為油脂過多,身體受不了爆炸而亡
                    if(jdbc.OtherSql("Delete From `bbs_client` Where `bbs_client_account` = '"+this.useraccount+"'")==1){
                        this.out.print("You!! Get out now!! Fucking fat nerd");
                        this.in.close();
                        this.out.close();
                        this.incoming.close();
                    }
                }
            }

        } catch (Exception e) {
            System.out.println(e + " in BBS_Client.java line 82");
        }
    }

    private void BBS_Clear_CMD() { //清除CMD
        for (byte i = 1; i <= 50; i++) {
            this.out.print("\r\n");
        }
        this.out.flush();
    }

    public void BBS_Welcome() { // 歡迎詞
        this.out.print("Welcome to Lin_BBS\r\n");
        this.out.print("If you have account,Input your account\r\n");
        this.out.print("Else input 'new' register account\r\n");
        this.out.print("Or input 'guest' scanner article\r\n");
        this.out.print("Please input Your account : ");
        this.out.flush();
    }

    public Socket getincoming() {
        return this.incoming;
    }

}
