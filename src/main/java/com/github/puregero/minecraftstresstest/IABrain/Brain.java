package com.github.puregero.minecraftstresstest.IABrain;

import java.io.PrintStream;
import java.lang.*;
import java.util.*;


class Brain extends Thread implements SensorInput {
    //===========================================================================
    // Private members
    private SendCommand m_jogador;			// robot which is controled by this brain
    private Memory m_memory;				// place where all information is stored
    private char m_side;
    volatile private boolean m_timeOver;
    private int m_type;
    private int m_number;
    private PrintStream m_debug;
    private PlayerParameters m_params;				// parametros do jogador
    private MyBody m_body;					// informa��o do jogador

    //---------------------------------------------------------------------------
    // This constructor:
    // - stores connection to krislet
    // - starts thread for this object
    public Brain(SendCommand jogador, String team,
                 char side, int number, String playMode, int type, int number1, PrintStream pt1) {
        m_timeOver = false;
        m_jogador = jogador;
        m_memory = new Memory();
//		m_team = team;
        m_side = side;
        m_body = new MyBody(number1, side);
//		m_number = number;
//		m_playMode = playMode;
        m_type = type;
        m_number = number1;
        m_debug = pt1;
        // Read Player Parameters
        m_params = new PlayerParameters(m_type, m_number, m_side);
        start();
    }


    //---------------------------------------------------------------------------
    // This is main brain function used to make decision
    // In each cycle we decide which command to issue based on
    // current situation. The decision is based on the rule system
    public void run() {
        ObjectInfo object;
        Vector actions;
        // variables for time counting
        long base_time;
        long sleep_time;

        // Time counting
        base_time = System.currentTimeMillis();

        // Reads the rule base
        RuleBase rules1 = new RuleBase("JSBach01", "bach_brain.rul");

        // Puts the player in the initial position
        m_body.x = m_params.get_x_base_pos(SoccerLib.DEF1);
        m_body.y = m_params.get_y_base_pos(SoccerLib.DEF1);
        m_body.dir = 0;
        m_jogador.move(m_body.x, m_body.y);
        m_debug.println("Moveu: " + m_body.x + " " + m_body.y); //Para Debug

        // While not end of the game
        while (!m_timeOver) {
            m_debug.println("Ciclo de Simula��o: " + m_memory.getTime()); //Para Debug

            // Treat perceptions
            treat_perceptions(rules1);
            //m_debug.println("-----");
            //m_memory.printObjects(m_debug); //Para debug
            //m_debug.println("-----");
            //rules1.printPerceptions(m_debug,m_body);  //Para debug

            // Choose actions
            actions = rules1.forwardChain(m_debug);
            //printActions(actions,m_debug);   //Para debug

            // Execute actions
            execute_actions(actions);

            // sleep to ensure that we will not send too many commands in one cycle.
            sleep_time = SoccerLib.simulator_step -
                    ((System.currentTimeMillis() - base_time) % SoccerLib.simulator_step);
            m_debug.println("Sleep time: " + sleep_time); //Para Debug
            try {
                Thread.sleep(sleep_time);
            } catch (Exception e) {
            }
        }

    }



//===========================================================================
// treat_perceptions
//
// Computes the rule variable values
    private void treat_perceptions(RuleBase rb1) {
        String val1;
        RuleVariable aux1;
        int zone1;
        CalcPos current_pos = new CalcPos(m_body);
        // calcula a posicao do jogador e respectiva zona
        current_pos.get_my_pos(m_memory);
        current_pos.getZone();
        // estado_equipa
        aux1 = rb1.getRuleVariable("estado_equipa");
        aux1.setValue(estado_equipa());
        // funcao_jogador
        aux1 = rb1.getRuleVariable("funcao_jogador");
        aux1.setValue(funcao_jogador());
        // ve_bola
        aux1 = rb1.getRuleVariable("ve_bola");
        aux1.setValue(ve_bola());
        // alinhado_bola
        aux1 = rb1.getRuleVariable("alinhado_bola");
        aux1.setValue(alinhado_bola());
        // bola_zona_accao
        aux1 = rb1.getRuleVariable("bola_zona_accao");
        aux1.setValue(bola_zona_accao());
        // bola_dir
        aux1 = rb1.getRuleVariable("bola_dir");
        aux1.setValue(bola_dir());
        // bola_perto
        aux1 = rb1.getRuleVariable("bola_perto");
        aux1.setValue(bola_perto());
        // posicao_base
        aux1 = rb1.getRuleVariable("posicao_base");
        aux1.setValue(posicao_base());
        // alinhado_posicao_base
        aux1 = rb1.getRuleVariable("alinhado_posicao_base");
        aux1.setValue(alinhado_posicao_base());
        // adversario_frente
        aux1 = rb1.getRuleVariable("adversario_frente");
        aux1.setValue(adversario_frente());
        // colega_frente
        aux1 = rb1.getRuleVariable("colega_frente");
        aux1.setValue(colega_frente());
        // alinhado_zona_accao
        aux1 = rb1.getRuleVariable("alinhado_zona_accao");
        aux1.setValue(alinhado_zona_accao());
        // zona_accao
        aux1 = rb1.getRuleVariable("zona_accao");
        aux1.setValue(zona_accao());
        // ve_objectos
        aux1 = rb1.getRuleVariable("ve_objectos");
        aux1.setValue(ve_objectos());
        // fora_do_campo
        aux1 = rb1.getRuleVariable("fora_do_campo");
        aux1.setValue(fora_do_campo());
    }

//===========================================================================
// execute_actions
//
// Executes the actions selected by the rules
    private void execute_actions(Vector acts1) {
        Enumeration enum = acts1.elements();
        String action1;
        while (enum.hasMoreElements()) {
            action1 = (String) enum.nextElement();
            doAction(action1);
        }
    }

//===========================================================================
// printActions
//
// Prints the actions selected by the rules
    private void printActions(Vector acts1, PrintStream pt1) {
        Enumeration enum = acts1.elements();
        String action1;
        pt1.println("-----");
        while (enum.hasMoreElements()) {
            action1 = (String) enum.nextElement();
            pt1.println("Action: " + action1);
        }
    }

//===========================================================================
// doAction
//
// Executes the action in act1
    private void doAction(String act1) {
        String str1 = act1;
        if (str1.equals("vira_para_bola"))
            vira_para_bola();
        else if (str1.equals("vai_para_bola"))
            vai_para_bola();
        else if (str1.equals("alivia_bola"))
            alivia_bola();
        else if (str1.equals("vira_para_posicao_base"))
            vira_para_posicao_base();
        else if (str1.equals("procura_bola"))
            procura_bola();
        else if (str1.equals("vai_para_posicao_base"))
            vai_para_posicao_base();
        else if (str1.equals("acompanha_bola"))
            acompanha_bola();
        else if (str1.equals("passa_bola"))
            passa_bola();
        else if (str1.equals("avanca_com_bola"))
            avanca_com_bola();
        else if (str1.equals("centra_bola"))
            centra_bola();
        else if (str1.equals("remata_bola"))
            remata_bola();
        else if (str1.equals("vai_para_campo"))
            vai_para_campo();
    }


//===========================================================================
// Implementation of SensorInput Interface

    //---------------------------------------------------------------------------
    // This function sends see information
    public void see(VisualInfo info) {
        m_memory.store(info);
    }


    //---------------------------------------------------------------------------
    // This function receives hear information from player
    public void hear(int time, int direction, String message) {
    }

    //---------------------------------------------------------------------------
    // This function receives hear information from referee
    public void hear(int time, String message) {
        /*StringTokenizer tokenizer = new StringTokenizer(message,"() ", true);
        String token;

        // First is referee token and time token
        tokenizer.nextToken();
        tokenizer.nextToken();
        tokenizer.nextToken();
        token = tokenizer.nextToken();

        if(token.compareTo("time_over") == 0)
            m_timeOver = true;*/
    }


//===========================================================================
// Perception treatment functions

    //---------------------------------------------------------------------------
    // estado_equipa
    // determina o estado em que a equipa esta', dependendo apenas da posicao
    // e posse de bola, em caso de nao ver a bola, entao o estado por default
    // e' o estado de defesa
    // Defesa - 1; M�dio - 2; Ataque - 3;
    private int estado_equipa() {
        int zone1,closer1;  // 0 - we ; 1 - they
        float bola_dist,bola_dir;
        DPoint pos1;
        CalcPos cpos1 = new CalcPos(m_body);
        ObjectInfo obj1 = m_memory.getObject("ball");
        //Caso em que esta' a ver a bola e tem a distancia da bola
        if ((obj1 != null) && (obj1.m_distance > 0)) {
            bola_dist = obj1.m_distance;
            bola_dir = obj1.m_direction;
            pos1 = SoccerLib.getXY(bola_dist, bola_dir, m_body); //vai buscar as coordenadas XY da bola
            zone1 = cpos1.getObjectZone(pos1.x, pos1.y); //identifica a zona da bola
            m_debug.println("Posicao da bola, x:" + pos1.x + " y:" + pos1.y + " dist:" + bola_dist + " dir:" + bola_dir + " zona:" + zone1);
            closer1 = m_memory.closerToBall(pos1.x, pos1.y, m_jogador.getTeam(), m_body);
            if (closer1 == 0) {
                m_body.my_team_state = 3;
                //m_debug.println("Estado da Equipa:3 - atacar"); //Para debug
                return 3; //Atacking
            }
            if (zone1 > 6) {
                m_body.my_team_state = 2;
                //m_debug.println("Estado da Equipa:2 - medio");//Para debug
                return 2; //Middle
            }
            m_body.my_team_state = 1;
            //m_debug.println("Estado da Equipa:1 - defender");//Para debug
            return 1; //Defending
        }
        //Caso em que esta' a ver a bola mas nao tem a distancia da bola
        if (obj1 != null) {
            obj1.printObject(m_debug);
        }
        if (m_body.my_team_state <= 0)
            m_body.my_team_state = 1;
        //m_debug.println("Estado da Equipa:"+MyBody.my_team_state);//Para debug
        return m_body.my_team_state; //Caso contrario fica o anterior
    }

    //---------------------------------------------------------------------------
    // funcao_jogador
    // determina a funcao do jogador dependendo do estado da equipa
    // defender_baliza - 1; defender - 2; roubar_bola - 3; atacar - 4;
    private int funcao_jogador() {
        int func1 = m_params.getPlayerFunction(m_body.my_team_state);
        //m_debug.println("Funcao do jogador: "+func1);//Para debug
        return func1;
    }

    //---------------------------------------------------------------------------
    // ve_bola
    private boolean ve_bola() {
        ObjectInfo obj1 = m_memory.getObject("ball");
        if ((obj1 != null) && (obj1.m_distance > 0)) {
            //m_debug.println("Ve bola:");//Para debug
            //obj1.printObject(m_debug);  //Para debug
            return true;
        }
        //m_debug.println("NAO ve bola !");//Para debug
        return false;
    }

    //---------------------------------------------------------------------------
    // alinhado_bola
    // devolve verdadeiro se est� alinhado com a bola
    private boolean alinhado_bola() {
        ObjectInfo obj1 = m_memory.getObject("ball");
        if ((obj1 != null) && (obj1.m_distance > 0)) {
            if ((obj1.m_direction > -5) && (obj1.m_direction < 5))
                return true;
        }
        return false;
    }

    //---------------------------------------------------------------------------
    // bola_zona_accao
    private boolean bola_zona_accao() {
        int zone1;
        DPoint pos1;
        CalcPos cpos1 = new CalcPos(m_body);
        ObjectInfo obj1 = m_memory.getObject("ball");
        if ((obj1 != null) && (obj1.m_distance > 0)) {
            float bola_dist = obj1.m_distance;
            float bola_dir = obj1.m_direction;
            pos1 = SoccerLib.getXY(bola_dist, bola_dir, m_body); //vai buscar as coordenadas XY da bola
            zone1 = cpos1.getObjectZone(pos1.x, pos1.y); //identifica a zona da bola
            return m_params.objectInActionZone(zone1, m_body);	//verifica se a bola esta' dentro da zona de accao
        }
        return false;
    }

    //---------------------------------------------------------------------------
    // zona_accao
    private boolean zona_accao() {
        int zone1;
        zone1 = m_body.my_zone;
        m_debug.println("Zona: " + zone1);
        return m_params.objectInActionZone(zone1, m_body);
    }

    //---------------------------------------------------------------------------
    // bola_dir
    // devolve a direccao da bola, arrendonda a direccao para um inteiro
    // devolve 1000 se nao tem informacao sobre a bola
    private int bola_dir() {
        ObjectInfo obj1 = m_memory.getObject("ball");
        if ((obj1 != null) && (obj1.m_distance > 0)) {
            return (int) obj1.m_direction;
        }
        return 1000;
    }

    //---------------------------------------------------------------------------
    // bola_perto
    // verdadeiro se a bola esta' a uma distancia inferior a 1,5
    private boolean bola_perto() {
        ObjectInfo obj1 = m_memory.getObject("ball");
        if ((obj1 != null) && (obj1.m_distance <= 1.8)) {
            return true;
        }
        return false;
    }

    //---------------------------------------------------------------------------
    // posicao_base
    // verdadeiro se o jogador esta' na sua posicao de base (com um erro de 4 metros)
    private boolean posicao_base() {
        boolean b1 = m_params.inBasePosition(m_debug, m_body);
        //m_debug.println("posicao_base = "+b1);
        return b1;
    }

    //---------------------------------------------------------------------------
    // alinhado_posicao_base
    // verdadeiro se o jogador esta' alinhado com a sua posicao de base
    private boolean alinhado_posicao_base() {
        boolean b1 = m_params.alignedWithBasePosition(m_debug, m_body);
        //m_debug.println("alinhado_posicao_base = "+b1);
        return b1;
    }

    //---------------------------------------------------------------------------
    // adversario_frente
    // verdadeiro se existe algum adversario no angulo de visao entre -15 e +15 graus
    private boolean adversario_frente() {
        PlayerInfo player1;
        Vector players = m_memory.getObjects("player");
        Enumeration enum = players.elements();
        while (enum.hasMoreElements()) {
            player1 = (PlayerInfo) enum.nextElement();
            if ((player1.m_teamName != null) &&
                    (player1.m_teamName.equals(m_jogador.getTeam()) != true) &&
                    (player1.m_direction > -15) &&
                    (player1.m_direction < 15))
                return true;
        }
        return false;
    }

    //---------------------------------------------------------------------------
    // colega_frente
    // verdadeiro se existe algum colega no angulo de visao entre -15 e +15 graus
    private boolean colega_frente() {
        PlayerInfo player1;
        Vector players = m_memory.getObjects("player");
        Enumeration enum = players.elements();
        while (enum.hasMoreElements()) {
            player1 = (PlayerInfo) enum.nextElement();
            if ((player1.m_teamName != null) &&
                    (player1.m_teamName.equals(m_jogador.getTeam())) &&
                    (player1.m_direction > -15) &&
                    (player1.m_direction < 15))
                return true;
        }
        return false;
    }

    //---------------------------------------------------------------------------
    // alinhado_zona_accao
    // verdadeiro se o jogador esta' alinhado com a sua zona de accao
    // a condicao e' que o angulo com a posicao de base esteja entre -15 e +15 graus
    // se estiver na zona de accao entao esta condincao e' verdadeira
    private boolean alinhado_zona_accao() {
        double xa = m_body.x,ya = m_body.y,xp,yp,teta_p,teta_a,dx,dy;
        DPoint Pp;
        int zona_accao1 = m_params.getActionZone(m_body);
        Vector base_points = m_params.getBasePoints(m_body);
        Enumeration enum = base_points.elements();
        teta_a = m_body.dir;
        if (zona_accao())
            return true;
        while (enum.hasMoreElements()) {
            Pp = (DPoint) enum.nextElement();
            xp = Pp.x;
            yp = Pp.y;
            dx = xp - xa;
            dy = yp - ya;
            if ((dx == 0) && (ya < yp))
                teta_p = 90;
            else if ((dx == 0) && (ya >= yp))
                teta_p = 270;
            else if ((dy == 0) && (xa < xp))
                teta_p = 0;
            else if ((dy == 0) && (xa >= xp))
                teta_p = 180;
            else {
                teta_p = Math.atan(dy / dx);
                teta_p = SoccerLib.Rad2Deg(teta_p);
                teta_p += 180;
            }
            m_debug.println("Zona Ac��o:" + zona_accao1 + " teta_a:" + teta_a + " teta_p:" + teta_p);
            if ((teta_a > teta_p - 15) && (teta_a < teta_p + 15))
                return true;
        }
        return false;
    }

    //---------------------------------------------------------------------------
    // ve_objectos
    // verdadeiro se o jogador esta' a ver objectos no campo
    private boolean ve_objectos() {
        if (m_memory.getObjectNumber() > 0)
            return true;
        return false;
    }

    //---------------------------------------------------------------------------
    // fora_do_campo
    // verdadeiro se o jogador esta' fora do campo
    // o que e' verdade se o jogador estiver a ver duas linhas opostas
    private boolean fora_do_campo() {
        int xline, yline;
        ObjectInfo o;
        xline = yline = 0;
        if ((o = m_memory.getObject("line l")) != null)
            yline++;
        if ((o = m_memory.getObject("line r")) != null)
            yline++;
        if ((o = m_memory.getObject("line t")) != null)
            xline++;
        if ((o = m_memory.getObject("line b")) != null)
            xline++;
        if ((xline == 2) || (yline == 2))
            return true;
        return false;
    }

//===========================================================================
// Action functions

    //---------------------------------------------------------------------------
    // vira_para_bola
    // vira o jogador na direccao da bola
    private void vira_para_bola() {
        ObjectInfo obj1 = m_memory.getObject("ball");
        if (obj1 != null) {
            m_debug.println("turn " + obj1.m_direction); //Para debug
            m_jogador.turn(obj1.m_direction);
        }
    }

    //---------------------------------------------------------------------------
    // vai_para_bola
    // o jogador vai para a bola
    private void vai_para_bola() {
        ObjectInfo obj1 = m_memory.getObject("ball");
        if (obj1 != null) {
            DPoint p1 = SoccerLib.getXY(obj1.m_distance, obj1.m_direction, m_body);
            //m_debug.println("Vai para a bola, dist:"+obj1.m_distance+", dir:"+obj1.m_direction+")");  //Para debug
            double aux1 = SoccerLib.dashXY(p1.x, p1.y, m_body);
            //m_debug.println("Vai para a bola("+p1.x+","+p1.y+")");  //Para debug
            m_debug.println("dash " + aux1);  //Para debug
            m_jogador.dash(aux1);
        }
    }

    //---------------------------------------------------------------------------
    // alivia_bola
    // chuta a bola na direccao do campo adversario
    private void alivia_bola() {
        ObjectInfo obj1 = m_memory.getObject("ball");
        if ((obj1 != null) && (obj1.m_distance < 2)) {
            DPoint p1 = SoccerLib.getClearPoint(m_side);
            double dir = SoccerLib.Calc_dir(p1.x, p1.y, m_body);
            double aux1 = SoccerLib.kickXY(p1.x, p1.y, dir, m_body);
            m_debug.println("kick " + aux1 + " " + dir);  //Para debug
            m_jogador.kick(aux1, dir);
        }
    }

    //---------------------------------------------------------------------------
    // vira_para_posicao_base
    // vira o jogador na direccao da posicao de base
    private void vira_para_posicao_base() {
        double px = m_params.get_x_base_pos(m_body.my_team_state);
        double py = m_params.get_y_base_pos(m_body.my_team_state);
        double aux1 = SoccerLib.Calc_dir(px, py, m_body);
        m_debug.println("turn " + aux1);  //Para debug
        m_jogador.turn(aux1);
    }

    //---------------------------------------------------------------------------
    // vai_para_posicao_base
    // o jogador vai para a posicao de base
    private void vai_para_posicao_base() {
        double px = m_params.get_x_base_pos(m_body.my_team_state);
        double py = m_params.get_y_base_pos(m_body.my_team_state);
        double aux1 = SoccerLib.dashXY(px, py, m_body);
        m_debug.println("dash " + aux1);  //Para debug
        m_jogador.dash(aux1);
    }

    //---------------------------------------------------------------------------
    // procura_bola
    // procura a bola rodando
    private void procura_bola() {
        m_debug.println("turn 30");  //Para debug
        m_jogador.turn(30);
    }

    //---------------------------------------------------------------------------
    // acompanha_bola
    // vira o jogador tenta acompanhar a bola
    private void acompanha_bola() {
        ObjectInfo obj1 = m_memory.getObject("ball");
        if ((obj1 != null) && (obj1.m_dirChange != 0)) {
            double aux1 = obj1.m_direction + obj1.m_dirChange;
            m_debug.println("turn " + aux1);  //Para debug
            m_jogador.turn(aux1);
        }
    }

    //---------------------------------------------------------------------------
    // passa_bola
    // passa a bola na direccao de um colega
    private void passa_bola() {
        PlayerInfo player1 = m_memory.getPlayerAhead(m_side, m_jogador.getTeam(), m_body);
        if (player1 != null) {
            DPoint p1 = SoccerLib.getXY(player1.m_distance, player1.m_direction, m_body);
            double dir = SoccerLib.Calc_dir(p1.x, p1.y, m_body);
            double aux1 = SoccerLib.kickXY(p1.x, p1.y, dir, m_body);
            m_debug.println("kick " + aux1 + " " + dir);  //Para debug
            m_jogador.kick(aux1, dir);
        }
    }

    //---------------------------------------------------------------------------
    // avanca_com_bola
    // o jogador chuta a bola para a frente para tentar avancar com a bola
    private void avanca_com_bola() {
        ObjectInfo obj1 = m_memory.getObject("ball");
        if ((obj1 != null) && (obj1.m_distance < 2)) {
            DPoint p1 = SoccerLib.getDriblePosition(m_side, m_body);
            double dir = SoccerLib.Calc_dir(p1.x, p1.y, m_body);
            double aux1 = SoccerLib.kickXY(p1.x, p1.y, dir, m_body);
            aux1 = aux1 / 4;
            m_debug.println("kick power:" + aux1 + " dir:" + dir + " px:" + p1.x + " py:" + p1.y);  //Para debug
            m_jogador.kick(aux1, dir);
        }
    }

    //---------------------------------------------------------------------------
    // centra_bola
    // chuta a bola para a grande area adversaria
    private void centra_bola() {
        double dir,x,y,aux1;
        ObjectInfo obj1 = m_memory.getObject("ball");
        if ((obj1 != null) && (obj1.m_distance < 2)) {
            x = 3 * SoccerLib.theFieldLength / 8 + 5;
            y = 0;
            dir = SoccerLib.Calc_dir(x, y, m_body);
            aux1 = SoccerLib.kickXY(x, y, dir, m_body);
            m_debug.println("kick " + aux1 + " " + dir);  //Para debug
            m_jogador.kick(aux1, dir);
        }
    }

    //---------------------------------------------------------------------------
    // remata_bola
    // chuta a bola na direccao da baliza adversaria
    private void remata_bola() {
        double dir,x,y,aux1;
        ObjectInfo obj1 = m_memory.getObject("ball");
        if ((obj1 != null) && (obj1.m_distance < 2)) {
            x = SoccerLib.theFieldLength / 2;
            y = 0;
            dir = SoccerLib.Calc_dir(x, y, m_body);
            aux1 = SoccerLib.kickXY(x, y, dir, m_body);
            m_debug.println("kick " + aux1 + " " + dir);  //Para debug
            m_jogador.kick(aux1, dir);
        }
    }

    //---------------------------------------------------------------------------
    // vai_para_campo
    // o jogador vai para dentro do campo
    private void vai_para_campo() {
        m_debug.println("dash 20.0");  //Para debug
        m_jogador.dash(20.0);
    }
}