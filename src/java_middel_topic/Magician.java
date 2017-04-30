
package java_middel_topic;

public class Magician extends PlayerInformation{
    private JDBC jdbc = new JDBC();
    private int init_hp;
    public Magician(String game_name) {
        String sql = "Select * From `bbs_game_detail` Where `game_name` = '" + game_name.trim() + "'";
        String infor[] = jdbc.SearchSql(sql).split("\t");
        setGame_name(infor[2]);
        setGame_career(infor[3]);
        setGame_level(Integer.parseInt(infor[4]));
        setGame_atk(Integer.parseInt(infor[5]));
        setGame_def(Integer.parseInt(infor[6]));
        setGame_hp(Integer.parseInt(infor[7]));
        setGame_str(Integer.parseInt(infor[8]));
        setGame_int(Integer.parseInt(infor[9]));
        setGame_exp(Integer.parseInt(infor[10].trim()));
        init_hp = Integer.parseInt(infor[7]);
    }
    public void Resert_Ability(String game_name){
        String sql = "Select * From `bbs_game_detail` Where `game_name` = '" + game_name + "'";
        String infor[] = jdbc.SearchSql(sql).split("\t");
        setGame_name(infor[2]);
        setGame_career(infor[3]);
        setGame_level(Integer.parseInt(infor[4]));
        setGame_atk(Integer.parseInt(infor[5]));
        setGame_def(Integer.parseInt(infor[6]));
        setGame_hp(Integer.parseInt(infor[7]));
        setGame_str(Integer.parseInt(infor[8]));
        setGame_int(Integer.parseInt(infor[9]));
        setGame_exp(Integer.parseInt(infor[10].trim()));
    }
    public int Getinit_hp(){
        return this.init_hp;
    }
    public int attack() {
        return getGame_atk();
    }
    public int StrongHit() {
        return getGame_atk() * 2;
    }
    public void GetDamage(int damage){
        setGame_hp(getGame_hp() - (damage-(getGame_def()*3/4)));
    }
    public int MagicClaw(){
        //無法使用 Integer.parseInt
        return (getGame_atk()*2+getGame_int()*2);
    }
    public String MyInfor(){
        String infor = "";
        infor += getGame_name() +" ";
        infor += getGame_career() + " ";
        infor += getGame_level() + " ";
        infor += getGame_atk() + " ";
        infor += getGame_def() + " ";
        infor += getGame_hp() + " ";
        infor += getGame_str() + " ";
        infor += getGame_int() + " ";
        infor += getGame_exp() + " ";
        return infor;
    }
    
}

