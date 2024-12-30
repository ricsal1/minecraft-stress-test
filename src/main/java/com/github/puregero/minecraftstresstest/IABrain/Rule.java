package com.github.puregero.minecraftstresstest.IABrain;

import java.util.*;

public class Rule {
    RuleBase rb;						// a base de regras 'a qual a regra pertence
    String name;						// o nome da regra
    Vector antecedents;  		// antecedentes
    Vector consequents;  		// consequentes
    Boolean truth;       		// states = (null=unknown, true, or false)
    boolean fired = false;		// se a regra foi utilizada ou nao

    //**********************************************************************************/
    // Constructor
    Rule(RuleBase Rb, String Name) {
        rb = Rb;
        name = Name;
        antecedents = new Vector();
        consequents = new Vector();
        truth = null;
    }

    //**********************************************************************************/
    // addAction
    public void addAction(Clause act1) {
        consequents.addElement(act1);
        act1.addRuleRef(this);
        act1.isConsequent();
    }

    //**********************************************************************************/
    // addCondition
    public void addCondition(Clause cond1) {
        antecedents.addElement(cond1);
        cond1.addRuleRef(this);
    }

    //**********************************************************************************/
    // numAntecedents
    int numAntecedents() {
        return antecedents.size();
    }

    //**********************************************************************************/
    // numConsequents
    int numConsequents() {
        return consequents.size();
    }

    //**********************************************************************************/
    // actions
    public Vector actions() {
        Vector acts1 = new Vector();
        Enumeration enum = consequents.elements();
        while (enum.hasMoreElements()) {
            Clause temp = (Clause) enum.nextElement();
            acts1.addElement(temp.getActionName());
        }
        return acts1;
    }


    //**********************************************************************************/
    // check
    //
// used by forward chaining only !
    Boolean check(RuleBase rb1) {
        // if antecedent is true and rule has not fired
        Enumeration enum = antecedents.elements();
        while (enum.hasMoreElements()) {
            Clause temp = (Clause) enum.nextElement();
            temp.evaluate(rb1);
            if (temp.truth == null)
                return null;
            if (temp.truth.booleanValue() == true)
                continue;
            else
                return truth = new Boolean(false); //don't fire this rule
        } // endfor
        return truth = new Boolean(true);  // could fire this rule
    }


}



