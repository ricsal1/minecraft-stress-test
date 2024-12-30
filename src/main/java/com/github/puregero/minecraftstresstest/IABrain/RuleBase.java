package com.github.puregero.minecraftstresstest.IABrain;

import java.util.*;
import java.io.*;

public class RuleBase {
    String name;								// name of the rule base
    String file;								// name of the file containning the rule base, including path
    Hashtable variableList;    // all variables in the rulebase
    Clause clauseVarList[];
    Vector ruleList;           // list of all rules
    Vector conclusionVarList; // queue of variables
    Rule rulePtr;              // working pointer to current rule
    Clause clausePtr;          // working pointer to current clause

    //**********************************************************************************/
    // Constructor
    RuleBase(String Name, String FileName) {
        name = Name;
        file = FileName;
        variableList = new Hashtable();
        initRuleVariables();
        read();               // read the rules from the rule file
    }

    //**********************************************************************************/
    // read
    //
    // reads the rules from the file
    // rule format:
    // < NAME : if CONDITIONS then ACTIONS end_if >
    // 'CONDITIONS' format is: < var1 cond1 value1 and var2 cond2 value2 and ... >
    // 'ACTIONS' format is: < action1 and action2 and ... >
    // 'var' is a variable name
    // 'cond' can be: =, !=, <, >
    // 'value' is a string
    // 'action' is a name of an action
    private void read() {
        String line,str1 = "";
        try {
            DataInputStream inFile = new DataInputStream(new FileInputStream(file));
            while ((line = inFile.readLine()) != null) {
                str1 = str1.concat(line);
            }
            StringTokenizer str_tok1 = new StringTokenizer(str1, " \n\r\t", false);
            if (parse(str_tok1) == 0)
                System.out.println("Error reading the rule base!");
            inFile.close();
        } catch (Exception e) {
            System.out.println("Error reading the rule base : " + e);
        }
    }

    //**********************************************************************************/
    // parse
    //
    // parses the read tokens from the rule base file
    private int parse(StringTokenizer str1) {
        int state = 0;
        String token,var1 = "",cond1 = "";
        Clause clause1;
        Condition c_cond1;
        RuleVariable r_var1;
        ruleList = new Vector(); //initialize the rule list of the rule base
        Rule rule1 = null;
        while ((str1.hasMoreTokens()) && (state < 9)) {
            token = str1.nextToken();
            switch (state) {
                case 0:
                    // Creates a new rule
                    rule1 = new Rule(this, token);
                    state = 1;
                    break;
                case 1:
                    // Must read a ':'
                    if (token.equals(":"))
                        state = 2;
                    else {
                        System.out.println("Error reading the rule base: expecting ':' " + token);
                        state = 10;  //error reading the rule
                    }
                    break;
                case 2:
                    // Must read an 'if'
                    if (token.equals("if"))
                        state = 3;
                    else {
                        System.out.println("Error reading the rule base: expecting 'if' " + token);
                        state = 10;  //error reading the rule
                    }
                    break;
                case 3:
                    var1 = token;
                    state = 4;
                    break;
                case 4:
                    if ((token.equals("=")) || (token.equals("!=")) || (token.equals("<")) ||
                            (token.equals(">"))) {
                        cond1 = token;
                        state = 5;
                    } else {
                        System.out.println("Error reading the rule base: expecting operator " + token);
                        state = 10;  //error reading the rule
                    }
                    break;
                case 5:
                    r_var1 = getRuleVariable(var1);
                    c_cond1 = new Condition(cond1);
                    if ((r_var1 != null) && (c_cond1 != null)) {
                        clause1 = new Clause(r_var1, c_cond1, token);
                        rule1.addCondition(clause1);
                        state = 6;
                    } else {
                        System.out.println("Error reading the rule base: variable not found " + var1);
                        state = 10;  //error reading the rule
                    }
                    break;
                case 6:
                    if (token.equals("and"))
                        state = 3;
                    else if (token.equals("then"))
                        state = 7;
                    else {
                        System.out.println("Error reading the rule base: 'and' or 'then' expected " + token);
                        state = 10;  //error reading the rule
                    }
                    break;
                case 7:
                    r_var1 = getRuleVariable(token);
                    if (r_var1 != null) {
                        clause1 = new Clause(r_var1);
                        rule1.addAction(clause1);
                        state = 8;
                    } else {
                        System.out.println("Error reading the rule base: variable not found " + token);
                        state = 10;  //error reading the rule
                    }
                    break;
                case 8:
                    if (token.equals("and"))
                        state = 7;
                    else if (token.equals("end_if")) {
                        ruleList.addElement(rule1);
                        state = 0;
                    } else {
                        System.out.println("Error reading the rule base: 'and' or 'end_if' expected " + token);
                        state = 10;  //error reading the rule
                    }
            }
        }
        if (state != 0)
            return 0; //error reading rule
        return 1;
    }

    //**********************************************************************************/
    // reset
    //
    // reset the rule base for another round of inferencing
    // by setting all variable values to null
    public void reset() {
        Enumeration enum = variableList.elements();
        while (enum.hasMoreElements()) {
            RuleVariable temp = (RuleVariable) enum.nextElement();
            temp.setValue(null);
        }
    }

    //**********************************************************************************/
    // initRuleVariables
    //
    // initializes the domain variables
    private void initRuleVariables() {
        // estado de comportamento em que a equipa esta'
        RuleVariable estado_equipa = new RuleVariable("estado_equipa"); //define variavel
        variableList.put(estado_equipa.name, estado_equipa); //adiciona variavel

        // se o jogador esta' a ver a bola
        RuleVariable ve_bola = new RuleVariable("ve_bola");
        variableList.put(ve_bola.name, ve_bola);

        // se a bola esta' dentro da zona de accao do jogador
        RuleVariable bola_zona_accao = new RuleVariable("bola_zona_accao");
        variableList.put(bola_zona_accao.name, bola_zona_accao);

        // direccao da bola
        RuleVariable bola_dir = new RuleVariable("bola_dir");
        variableList.put(bola_dir.name, bola_dir);

        // se a bola esta' chutavel
        RuleVariable bola_perto = new RuleVariable("bola_perto");
        variableList.put(bola_perto.name, bola_perto);

        // se o jogador esta' na sua posicao de base
        RuleVariable posicao_base = new RuleVariable("posicao_base");
        variableList.put(posicao_base.name, posicao_base);

        // se o jogador esta' alinhado com a sua posicao de base
        RuleVariable alinhado_posicao_base = new RuleVariable("alinhado_posicao_base");
        variableList.put(alinhado_posicao_base.name, alinhado_posicao_base);

        // se o jogador tem algum adversario 'a frente - para mais pormenores ver funcao
        RuleVariable adversario_frente = new RuleVariable("adversario_frente");
        variableList.put(adversario_frente.name, adversario_frente);

        // se o jogador tem algum colega 'a frente - para mais pormentores ver funcao
        RuleVariable colega_frente = new RuleVariable("colega_frente");
        variableList.put(colega_frente.name, colega_frente);

        // se o jogador esta' alinhado com a zona de accao - para mais pormenores ver funcao
        RuleVariable alinhado_zona_accao = new RuleVariable("alinhado_zona_accao");
        variableList.put(alinhado_zona_accao.name, alinhado_zona_accao);

        // funcao do jogador
        RuleVariable funcao_jogador = new RuleVariable("funcao_jogador");
        variableList.put(funcao_jogador.name, funcao_jogador);

        // zona de accao
        RuleVariable zona_accao = new RuleVariable("zona_accao");
        variableList.put(zona_accao.name, zona_accao);

        // vira para a bola
        RuleVariable vira_para_bola = new RuleVariable("vira_para_bola");
        variableList.put(vira_para_bola.name, vira_para_bola);

        // vai para a bola
        RuleVariable vai_para_bola = new RuleVariable("vai_para_bola");
        variableList.put(vai_para_bola.name, vai_para_bola);

        // alivia a bola na direcao do campo adversario
        RuleVariable alivia_bola = new RuleVariable("alivia_bola");
        variableList.put(alivia_bola.name, alivia_bola);

        // vira para a posicao base do jogador
        RuleVariable vira_para_posicao_base = new RuleVariable("vira_para_posicao_base");
        variableList.put(vira_para_posicao_base.name, vira_para_posicao_base);

        // procura a bola
        RuleVariable procura_bola = new RuleVariable("procura_bola");
        variableList.put(procura_bola.name, procura_bola);

        // vai para a posicao de base do jogador
        RuleVariable vai_para_posicao_base = new RuleVariable("vai_para_posicao_base");
        variableList.put(vai_para_posicao_base.name, vai_para_posicao_base);

        // acompanha a bola
        RuleVariable acompanha_bola = new RuleVariable("acompanha_bola");
        variableList.put(acompanha_bola.name, acompanha_bola);

        // passa a bola para um colega
        RuleVariable passa_bola = new RuleVariable("passa_bola");
        variableList.put(passa_bola.name, passa_bola);

        // avanca com a bola
        RuleVariable avanca_com_bola = new RuleVariable("avanca_com_bola");
        variableList.put(avanca_com_bola.name, avanca_com_bola);

        // centra a bola, ou seja chuta para a grande 'area do adversario
        RuleVariable centra_bola = new RuleVariable("centra_bola");
        variableList.put(centra_bola.name, centra_bola);

        // remata a bola 'a baliza adversaria
        RuleVariable remata_bola = new RuleVariable("remata_bola");
        variableList.put(remata_bola.name, remata_bola);

        // se o jogador est� alinhado com a bola
        RuleVariable alinhado_bola = new RuleVariable("alinhado_bola");
        variableList.put(alinhado_bola.name, alinhado_bola);

        // se o jogador est� a ver objecto sou nao
        RuleVariable ve_objectos = new RuleVariable("ve_objectos");
        variableList.put(ve_objectos.name, ve_objectos);

        // se o jogador est� fora do campo ou nao
        RuleVariable fora_do_campo = new RuleVariable("fora_do_campo");
        variableList.put(fora_do_campo.name, fora_do_campo);

        // volta para dentro do campo
        RuleVariable vai_para_campo = new RuleVariable("vai_para_campo");
        variableList.put(vai_para_campo.name, vai_para_campo);

        // *** INSERIR NOVAS VARIAVEIS DE ESTADO AQUI *** //

    }

    //**********************************************************************************/
    // getRuleVariable
    public RuleVariable getRuleVariable(String Name) {
        return (RuleVariable) variableList.get(Name);
    }

    //**********************************************************************************/
    // match
    //
    // used for forward chaining only
    // determine which rules can fire, return a Vector
    public Vector match(boolean test) {
        Vector matchList = new Vector();
        Enumeration enum = ruleList.elements();
        while (enum.hasMoreElements()) {
            Rule testRule = (Rule) enum.nextElement();
            // test the rule antecedents
            if (test)
                testRule.check(this);
            if (testRule.truth == null)
                continue;
            // fire the rule only once for now
            if ((testRule.truth.booleanValue() == true) && (testRule.fired == false))
                matchList.addElement(testRule);
        }
        return matchList;
    }

    //**********************************************************************************/
    // selectRule
    //
    // used for forward chaining only
    // select a rule to fire based on specificity
    public Rule selectRule(Vector ruleSet) {
        Enumeration enum = ruleSet.elements();
        long numClauses;
        Rule nextRule;
        Rule bestRule = (Rule) enum.nextElement();
        long max = bestRule.numAntecedents();
        while (enum.hasMoreElements()) {
            nextRule = (Rule) enum.nextElement();
            if ((numClauses = nextRule.numAntecedents()) > max) {
                max = numClauses;
                bestRule = nextRule;
            }
        }
        return bestRule;
    }

    //**********************************************************************************/
    // forwardChain
    //
    // This is a simplified version of the forwardChain algorithm
    // it only does one level of inference
    // returns the names of the actions to be performed
    public Vector forwardChain(PrintStream m_debug) {
        Vector conflictRuleSet = new Vector();
        Vector actionSet = new Vector();
        // first test all rules, based on initial data, seeing which rules can fire
        conflictRuleSet = match(true);
        if (conflictRuleSet.size() > 0) {
            // select the "best" rule
            Rule selected = selectRule(conflictRuleSet);
            actionSet = selected.actions();
            //Para Debug
            m_debug.println("-----");
            m_debug.println("Regra seleccionada: " + selected.name);
        }
        return actionSet;
    }

    //**********************************************************************************/
    // printPerceptions
    //
    // prints the player's perceptions
    public void printPerceptions(PrintStream pt1, MyBody m_body) {
        Enumeration enum = variableList.elements();
        RuleVariable var1;
        while (enum.hasMoreElements()) {
            var1 = (RuleVariable) enum.nextElement();
            if ((var1.value != null) && (var1.value.equals("null") != true))
                var1.printValue(pt1);
        }
//Print the MyBody Information
        pt1.println("MyBody.x = " + m_body.x);
        pt1.println("MyBody.y = " + m_body.y);
        pt1.println("MyBody.dir = " + m_body.dir);
        pt1.println("MyBody.side = " + m_body.side);
    }

}