
package java_middel_topic;

public class BoardContent {

    private String articleID;
    private String authorID;
    private String articleTitle;
    private String content;
    
    public BoardContent(){
        
    }
    
    public void setarticleID(String articleID){
        this.articleID = articleID;
    }
    
    public void setauthorID(String authorID){
        this.authorID = authorID;
    }
    
    public void setarticleTitle(String articleTitle){
        this.articleTitle = articleTitle;
    }
    
    public void setcontent(String content){
        this.content = content;
    }
    
    public String getarticleID(){
        return this.articleID;
    }
    
    public String getauthorID(){
        return this.authorID;
    }
    
    public String getcontent(){
        return this.content;
    }
    
    public String getarticleTitle(){
        return this.articleTitle;
    }
}
