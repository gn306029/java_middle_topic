/*
 * 簡易回合制 RPG 遊戲
 * 資料庫 bbs_game 放 玩家編號,玩家 bbs 帳號
 * 資料庫 bbs_game_detail 放 ,角色名稱,角色職業,角色等級,角色能力
 * 角色能力：攻擊,防禦,血量,力量,智力
 */
package java_middel_topic;

import java.io.*;
import java.net.*;
import java.util.*;

public class Game {

    static Vector Player = new Vector(2);
    static int[] roomplayer = new int[2];
    static boolean game_ready[] = {false, false};
    static boolean gameover = false;
    private static BBS_Client[] Back = new BBS_Client[2];
    private static int P1Port;
    private static int P2Port;
    protected Socket incoming;
    private BufferedReader in;
    private PrintWriter out;
    private JDBC jdbc = new JDBC();
    private Novice novice;
    private Magician magician;
    private String[] career;

    public Game(Socket incoming, BufferedReader in, PrintWriter out, int OneOrTwo, BBS_Client bbs_client) {
        this.incoming = incoming;
        this.in = in;
        this.out = out;
        if (OneOrTwo == 1) {
            game_ready[0] = true;
            P1Port = this.incoming.getPort();
            Back[0] = bbs_client;
        } else if (OneOrTwo == 2) {
            game_ready[1] = true;
            P2Port = this.incoming.getPort();
            Back[1] = bbs_client;
        }
    }

    public BBS_Client[] GameStart(String useraccount) {
        gameover = false;
        String line;
        String sql = "Select `game_career`,`game_name` From `bbs_game_detail` Join `bbs_game` On `bbs_game`.`game_player_id` = `bbs_game_detail`.`game_player_id` Where `game_bbs_account` = '" + useraccount + "'";
        career = jdbc.SearchSql(sql).split("\t");
        System.out.println(career[0] + " " + career[1]);
        if (career[0].equalsIgnoreCase("Novice")) {
            novice = new Novice(career[1]);
        } else if (career[0].equalsIgnoreCase("Magician")) {
            magician = new Magician(career[1]);
        }
        synchronized (Player) {
            Player.addElement(this);
        }

        try {
            loopA:
            while (!(line = in.readLine()).equalsIgnoreCase("/quit") && !gameover) {
                // 10 等才可轉職
                if (line.equalsIgnoreCase("/Transfer")) {
                    if (this.novice.getGame_level() >= 10) {
                        if (this.career[0].equalsIgnoreCase("Novice")) {
                            //先做法師就好
                            this.out.println("Input your want career , You can select /Magician or /Warrior");
                            this.out.println("If you do not want to do it , You can input /quit to do go back");
                            this.out.flush();
                            String cm;
                            while (!(cm = in.readLine()).equalsIgnoreCase("/quit")) {
                                if (cm.equalsIgnoreCase("/Magician")) {
                                    this.out.println("decide to select a Magician ? (Yes or No)");
                                    this.out.flush();
                                    cm = in.readLine();
                                    if (cm.equalsIgnoreCase("yes")) {
                                        jdbc.OtherSql("Update `bbs_game_detail` Set `game_career` = 'Magician',"
                                                + "`game_atk` = '" + 75 + "',"
                                                + "`game_def` = '" + 40 + "',"
                                                + "`game_hp` = '" + 700 + "',"
                                                + "`game_str` = '" + 40 + "',"
                                                + "`game_int` = '" + 70 + "'"
                                                + "Where `game_name` = '" + this.career[1].trim().replace("\r\n", "") + "'");
                                        this.novice = null;
                                        this.magician = new Magician(this.career[1].trim());
                                        this.career[0] = "Magician";
                                        break;
                                    }
                                } else {
                                    this.out.println("Please Input a valid message");
                                    this.out.flush();
                                    break;
                                }
                            }
                        } else {
                            this.out.println("You not a Novice");
                            this.out.flush();
                        }
                    } else {
                        this.out.println("Noob , you must do more exercises");
                        this.out.flush();
                    }
                }

                if (game_ready[0] == true && game_ready[1] == true) {

                    for (int i = 0; i < Player.size(); i++) {
                        Game g = (Game) Player.elementAt(i);
                        g.out.println("Game Start");
                        g.out.flush();
                    }
                    String action;
                    while (!gameover) {
                        action = in.readLine();
                        if (action.equalsIgnoreCase("/attack")) {
                            Game G_P1 = (Game) Player.elementAt(0);
                            Game G_P2 = (Game) Player.elementAt(1);
                            if (G_P1 == this) {
                                if (G_P1.novice != null) {
                                    GetDamage(G_P1.novice.attack());
                                } else if (G_P1.magician != null) {
                                    GetDamage(G_P1.magician.attack());
                                }
                            } else if(G_P2 == this){
                                if (G_P2.novice != null) {
                                    GetDamage(G_P2.novice.attack());
                                } else if (G_P2.magician != null) {
                                    GetDamage(G_P2.magician.attack());
                                }
                            }
                            
                            if (CheckOver() == 1) {
                                BoardCast("P1 Winner\r\n");
                                for (int i = 0; i < Player.size(); i++) {
                                    Game g = (Game) Player.elementAt(i);
                                    if (this == g) {
                                        g.out.println("You Get the 10 Exp");
                                        if (g.career[0].equalsIgnoreCase("Novice")) {
                                            Get_EXP(g.career[1].trim(), 10, g.novice);
                                        } else if (g.career[0].equalsIgnoreCase("Magician")) {
                                            Get_EXP(g.career[1].trim(), 10, g.magician);
                                        }
                                        g.out.flush();
                                    } else if (this != g) {
                                        g.out.println("You Get the 5 Exp");
                                        if (g.career[0].equalsIgnoreCase("Novice")) {
                                            Get_EXP(g.career[1].trim(), 5, g.novice);
                                        } else if (g.career[0].equalsIgnoreCase("Magician")) {
                                            Get_EXP(g.career[1].trim(), 5, g.magician);
                                        }
                                        g.out.flush();
                                    }
                                }
                                Player.removeAllElements();
                                return Back;
                            } else if (CheckOver() == 2) {
                                BoardCast("P2 Winner\r\n");
                                for (int i = 0; i < Player.size(); i++) {
                                    Game g = (Game) Player.elementAt(i);
                                    if (this == g) {
                                        g.out.println("You Get the 10 Exp");
                                        if (g.career[0].equalsIgnoreCase("Novice")) {
                                            Get_EXP(g.career[1].trim(), 10, g.novice);
                                        } else if (g.career[0].equalsIgnoreCase("Magician")) {
                                            Get_EXP(g.career[1].trim(), 10, g.magician);
                                        }
                                        g.out.flush();
                                    } else if (this != g) {
                                        g.out.println("You Get the 5 Exp");
                                        if (g.career[0].equalsIgnoreCase("Novice")) {
                                            Get_EXP(g.career[1].trim(), 5, g.novice);
                                        } else if (g.career[0].equalsIgnoreCase("Magician")) {
                                            Get_EXP(g.career[1].trim(), 5, g.magician);
                                        }
                                        g.out.flush();
                                    }
                                }
                                Player.removeAllElements();
                                return Back;
                            }
                        } else if (action.equalsIgnoreCase("/StrongHit")) {
                            Game G_P1 = (Game) Player.elementAt(0);
                            Game G_P2 = (Game) Player.elementAt(1);
                            if (G_P1 == this) {
                                if (G_P1.novice != null) {
                                    GetDamage(G_P1.novice.StrongHit());
                                } else if (G_P1.magician != null) {
                                    GetDamage(G_P1.magician.StrongHit());
                                }
                            } else if(G_P2 == this){
                                if (G_P2.novice != null) {
                                    GetDamage(G_P2.novice.StrongHit());
                                } else if (G_P2.magician != null) {
                                    GetDamage(G_P2.magician.StrongHit());
                                }
                            }
                            
                            if (CheckOver() == 1) {
                                BoardCast("P1 Winner\r\n");
                                //贏了拿 10 Exp
                                for (int i = 0; i < Player.size(); i++) {
                                    Game g = (Game) Player.elementAt(i);
                                    if (this == g) {
                                        g.out.println("You Get the 10 Exp");
                                        if (g.career[0].equalsIgnoreCase("Novice")) {
                                            Get_EXP(g.career[1].trim(), 10, g.novice);
                                        } else if (g.career[0].equalsIgnoreCase("Magician")) {
                                            Get_EXP(g.career[1].trim(), 10, g.magician);
                                        }
                                        g.out.flush();
                                    } else if (this != g) {
                                        g.out.println("You Get the 5 Exp");
                                        if (g.career[0].equalsIgnoreCase("Novice")) {
                                            Get_EXP(g.career[1].trim(), 5, g.novice);
                                        } else if (g.career[0].equalsIgnoreCase("Magician")) {
                                            Get_EXP(g.career[1].trim(), 5, g.magician);
                                        }
                                        g.out.flush();
                                    }
                                }
                                Player.removeAllElements();
                                return Back;
                            } else if (CheckOver() == 2) {
                                BoardCast("P2 Winner\r\n");
                                //輸了拿 5 Exp
                                for (int i = 0; i < Player.size(); i++) {
                                    Game g = (Game) Player.elementAt(i);
                                    if (this == g) {
                                        g.out.println("You Get the 10 Exp");
                                        if (g.career[0].equalsIgnoreCase("Novice")) {
                                            Get_EXP(g.career[1].trim(), 10, g.novice);
                                        } else if (g.career[0].equalsIgnoreCase("Magician")) {
                                            Get_EXP(g.career[1].trim(), 10, g.magician);
                                        }
                                        g.out.flush();
                                    } else if (this != g) {
                                        g.out.println("You Get the 5 Exp");
                                        if (g.career[0].equalsIgnoreCase("Novice")) {
                                            Get_EXP(g.career[1].trim(), 5, g.novice);
                                        } else if (g.career[0].equalsIgnoreCase("Magician")) {
                                            Get_EXP(g.career[1].trim(), 5, g.magician);
                                        }
                                        g.out.flush();
                                    }
                                }
                                Player.removeAllElements();
                                return Back;
                            }
                        } else if (action.equalsIgnoreCase("/MagicClaw")) {
                            if (this.magician != null) {
                                GetDamage(this.magician.MagicClaw());
                                if (CheckOver() == 1) {
                                    BoardCast("P1 Winner\r\n");
                                    //贏了拿 10 Exp
                                    for (int i = 0; i < Player.size(); i++) {
                                        Game g = (Game) Player.elementAt(i);
                                        if (this == g) {
                                            g.out.println("You Get the 10 Exp");
                                            if (g.career[0].equalsIgnoreCase("Novice")) {
                                                Get_EXP(g.career[1].trim(), 10, g.novice);
                                            } else if (g.career[0].equalsIgnoreCase("Magician")) {
                                                Get_EXP(g.career[1].trim(), 10, g.magician);
                                            }
                                            g.out.flush();
                                        } else if (this != g) {
                                            g.out.println("You Get the 5 Exp");
                                            if (g.career[0].equalsIgnoreCase("Novice")) {
                                                Get_EXP(g.career[1].trim(), 5, g.novice);
                                            } else if (g.career[0].equalsIgnoreCase("Magician")) {
                                                Get_EXP(g.career[1].trim(), 5, g.magician);
                                            }
                                            g.out.flush();
                                        }
                                    }
                                    Player.removeAllElements();
                                    return Back;
                                } else if (CheckOver() == 2) {
                                    BoardCast("P2 Winner\r\n");
                                    //輸了拿 5 Exp
                                    for (int i = 0; i < Player.size(); i++) {
                                        Game g = (Game) Player.elementAt(i);
                                        if (this == g) {
                                            g.out.println("You Get the 10 Exp");
                                            if (g.career[0].equalsIgnoreCase("Novice")) {
                                                Get_EXP(g.career[1].trim(), 10, g.novice);
                                            } else if (g.career[0].equalsIgnoreCase("Magician")) {
                                                Get_EXP(g.career[1].trim(), 10, g.magician);
                                            }
                                            g.out.flush();
                                        } else if (this != g) {
                                            g.out.println("You Get the 5 Exp");
                                            if (g.career[0].equalsIgnoreCase("Novice")) {
                                                Get_EXP(g.career[1].trim(), 5, g.novice);
                                            } else if (g.career[0].equalsIgnoreCase("Magician")) {
                                                Get_EXP(g.career[1].trim(), 5, g.magician);
                                            }
                                            g.out.flush();
                                        }
                                    }
                                    Player.removeAllElements();
                                    return Back;
                                }
                            } else {
                                this.out.println("You not a Magician");
                                this.out.flush();
                            }
                        }
                    }

                }
            }
        } catch (IOException e) {
            System.out.println(e + " in GameStart");
        }
        BBS_Client b[] = new BBS_Client[0];
        return b;
    }

    //UP_Level & Get_EXP 增加經驗還要檢查是否升等 等級 10 以下 50 經驗升一等 , 11 ~ 20 等 100 經驗升一等
    private void Up_Level(String game_name, Magician magician) {
        int level = magician.getGame_level();
        if (level >= 1 && level <= 10) {
            if (magician.getGame_exp() >= 50) {
                jdbc.OtherSql("Update `bbs_game_detail` Set "
                        + "`game_level` = '" + (level + 1) + "',"
                        + "`game_atk` = '" + (magician.getGame_atk() + 5) + "',"
                        + "`game_def` = '" + (magician.getGame_def() + 5) + "',"
                        + "`game_hp` = '" + (magician.Getinit_hp() + 50) + "',"
                        + "`game_str` = '" + (magician.getGame_str() + 5) + "',"
                        + "`game_int` = '" + (magician.getGame_int() + 5) + "',"
                        + "`game_exp` = '" + (magician.getGame_exp()) % 50 + "' where `game_name` = '" + game_name + "'");
            }
        } else if (level >= 11 && level <= 20) {
            if (magician.getGame_exp() >= 100) {
                jdbc.OtherSql("Update `bbs_game_detail` Set "
                        + "`game_level` = '" + (level + 1) + "'"
                        + "`game_atk` = '" + (magician.getGame_atk() + 5) + "',"
                        + "`game_def` = '" + (magician.getGame_def() + 5) + "',"
                        + "`game_hp` = '" + (magician.Getinit_hp() + 50) + "',"
                        + "`game_str` = '" + (magician.getGame_str() + 5) + "',"
                        + "`game_int` = '" + (magician.getGame_int() + 5) + "',"
                        + "`game_exp` = '" + (magician.getGame_exp() % 100) + "' where `game_name` = '" + game_name + "'");
            }
        }
    }

    private void Up_Level(String game_name, Novice novice) {
        int level = novice.getGame_level();
        if (level >= 1 && level <= 10) {
            if (novice.getGame_exp() >= 50) {
                jdbc.OtherSql("Update `bbs_game_detail` Set "
                        + "`game_level` = '" + (level + 1) + "',"
                        + "`game_atk` = '" + (novice.getGame_atk() + 5) + "',"
                        + "`game_def` = '" + (novice.getGame_def() + 5) + "',"
                        + "`game_hp` = '" + (novice.Getinit_hp() + 50) + "',"
                        + "`game_str` = '" + (novice.getGame_str() + 5) + "',"
                        + "`game_int` = '" + (novice.getGame_int() + 5) + "',"
                        + "`game_exp` = '" + (novice.getGame_exp()) % 50 + "' where `game_name` = '" + game_name + "'");
            }
        } else if (level >= 11 && level <= 20) {
            if (novice.getGame_exp() >= 100) {
                jdbc.OtherSql("Update `bbs_game_detail` Set "
                        + "`game_level` = '" + (level + 1) + "'"
                        + "`game_atk` = '" + (novice.getGame_atk() + 5) + "',"
                        + "`game_def` = '" + (novice.getGame_def() + 5) + "',"
                        + "`game_hp` = '" + (novice.Getinit_hp() + 50) + "',"
                        + "`game_str` = '" + (novice.getGame_str() + 5) + "',"
                        + "`game_int` = '" + (novice.getGame_int() + 5) + "',"
                        + "`game_exp` = '" + (novice.getGame_exp() % 100) + "' where `game_name` = '" + game_name + "'");
            }
        }
    }

    private void Get_EXP(String game_name, int increase_Exp, Novice novice) {
        jdbc.OtherSql("Update `bbs_game_detail` Set "
                + "`game_exp` = '" + ((novice.getGame_exp() + increase_Exp)) + "' where `game_name` = '" + game_name + "'");
        novice.Resert_Ability(game_name);
        Up_Level(game_name, novice);
    }

    private void Get_EXP(String game_name, int increase_Exp, Magician magician) {
        jdbc.OtherSql("Update `bbs_game_detail` Set "
                + "`game_exp` = '" + ((magician.getGame_exp() + increase_Exp)) + "' where `game_name` = '" + game_name + "'");
        magician.Resert_Ability(game_name);
        Up_Level(game_name, magician);
    }

    public void BoardCast(String line) {
        for (int i = 0; i < Player.size(); i++) {
            Game g = (Game) Player.elementAt(i);
            g.out.print(line);
            g.out.flush();
        }
    }

    public void GetDamage(int damage) {
        System.out.println(damage);
        for (int i = 0; i < Player.size(); i++) {
            synchronized (Player) {
                Game g = (Game) Player.elementAt(i);
                if (g != this) {
                    g.out.println("Opponent give you damage");
                    try {
                        g.novice.GetDamage(damage);
                    } catch (NullPointerException npe) {
                        g.magician.GetDamage(damage);
                    }
                    try {
                        g.out.println("Your hp the rest " + g.novice.getGame_hp());
                    } catch (NullPointerException npe) {
                        g.out.println("Your hp the rest " + g.magician.getGame_hp());
                    }
                    g.out.flush();
                }
            }
        }
    }

    public int CheckOver() {
        if (((Game) Player.elementAt(0)).novice != null) {
            if (((Game) Player.elementAt(0)).novice.getGame_hp() <= 0) {
                gameover = true;
                return 2;
            }
        } else if (((Game) Player.elementAt(1)).novice != null) {
            if (((Game) Player.elementAt(1)).novice.getGame_hp() <= 0) {
                gameover = true;
                return 2;
            }
        } else if (((Game) Player.elementAt(0)).magician != null) {
            if (((Game) Player.elementAt(0)).magician.getGame_hp() <= 0) {
                gameover = true;
                return 2;
            }
        } else if (((Game) Player.elementAt(1)).magician != null) {
            if (((Game) Player.elementAt(1)).magician.getGame_hp() <= 0) {
                gameover = true;
                return 2;
            }
        }
        return 3;
    }

}
