package java_middel_topic;

import java.io.*;
import java.net.*;
import java.util.*;

public class Gossiping {

    private static ArrayList<BoardContent> article_list = new ArrayList<BoardContent>();
    private static Vector gossiping;
    private static int article_id;
    private JDBC jdbc = new JDBC();

    //Server 啟動後初始化 八卦版
    public Gossiping() {
        article_list = jdbc.initGossiping();
        String article_id = jdbc.SearchSql("Select MAX(article_id) From `bbs_gossiping`");
        this.article_id = Integer.parseInt(article_id.trim());
    }

    //顯示文章清單
    public void ShowArticaleList(PrintWriter out) {
        try {
            out = out;
            out.print("articale_id\tauthor_id\tarticle_title\r\n");
            out.flush();
            for (BoardContent i : article_list) {
                out.print(i.getarticleID() + "\t\t");
                out.print(i.getauthorID() + "\t\t");
                out.print(i.getarticleTitle() + "\r\n");
                out.flush();
                article_id = Integer.parseInt(i.getarticleID());
            }
        } catch (Exception e) {
            System.out.println(e + " in Gossiping ShowArticaleList");
        }
    }

    public void ScaneerArticaleByID(String article_id,PrintWriter out) {
        boolean state = false;
        for (BoardContent i : article_list) {
            if (i.getarticleID().equalsIgnoreCase(article_id)) {
                state = true;
                out.println(i.getcontent());
                out.flush();
                break;
            }
        }
        if (!state) {
            out.println("NotFound");
            out.flush();
        }
    }

    public void InsertArticale(String author_id, String articale_title, String articale_content,PrintWriter out) {
        String sql = "Insert into `bbs_gossiping`(`author_id`,`article_title`,`article_content`) Values ('" + author_id + "','" + articale_title + "','" + articale_content + "')";
        int state = jdbc.OtherSql(sql);
        if (state == 1) {
            out.println("Insert Succese");
            article_id++;
            BoardContent boardcontent = new BoardContent();
            boardcontent.setarticleID(this.article_id+"");
            boardcontent.setauthorID(author_id);
            boardcontent.setarticleTitle(articale_title);
            boardcontent.setcontent(articale_content);
            article_list.add(boardcontent);
            
        } else if (state == 0) {
            out.println("Insert Fail");
        }
        out.flush();
    }

    public void DeleteArticle(String article_id, String author_id,PrintWriter out) {
        boolean isMe = false;
        for (BoardContent i : article_list) {
            if (i.getauthorID().equalsIgnoreCase(author_id)) {
                if (i.getarticleID().equalsIgnoreCase(article_id)) {
                    String sql = "Delete From `bbs_gossiping` Where article_id = '" + article_id + "'";
                    int state = jdbc.OtherSql(sql);
                    if (state == 1) {
                        out.println("Delete Succese");
                        article_list.remove(i);
                    } else if (state == 0) {
                        out.println("Delete Fail");
                    }
                    out.flush();
                    isMe = true;
                    break;
                }
            }
        }
        if (!isMe) {
            out.println("This is not your article,You can't delete it");
            out.flush();
        }
    }
}
