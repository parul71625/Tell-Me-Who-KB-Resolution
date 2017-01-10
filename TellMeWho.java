package TellMeWhoPkg;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class TellMeWho {

    int noOfClauseToProve;
    int noInKB;

    Map KBMap = new HashMap();
    Map parsedClauseMap = new HashMap();
    Map newCreatedRulesWithCNF = new HashMap();

    Map noOfTimesASentIsUsed = new HashMap();

    ArrayList databaseCTP = new ArrayList();
    ArrayList clearValuesFromMap = new ArrayList();

    ArrayList finalKB = new ArrayList();

    Map outputMap = new HashMap();

    ArrayList predAlrdyResolved = new ArrayList();

    long startTime;
    long timeOut;

    private void resolveCTP() {
        for (Object sntnc : databaseCTP) {
            String sentCTP = (String) sntnc;
            predAlrdyResolved.clear();
            noOfTimesASentIsUsed.clear();

            boolean findSol = false;
            try {
                findSol = resolveSentCTP(sentCTP, sentCTP, 1, false);
            } catch (StackOverflowError e) {
                //System.err.println("ouch!");
            }
            //System.out.println(findSol);
            outputMap.put(sentCTP, findSol);
        }
        //System.out.println("");
        //printMap(outputMap);

    }

    private boolean resolveSentCTP(String currSent, String currPredToResolve, int depth, boolean switchOnDuplicates) {

        if(predAlrdyResolved.contains(currSent))
         {
             return false;
         }
        if (currPredToResolve.contains("|")) {
            String currSentCopyParts[] = currPredToResolve.split("[|]");

            boolean duplicates = false;

            if(switchOnDuplicates)
            {
                for (int j=0;j<currSentCopyParts.length;j++)
                  for (int l=j+1;l<currSentCopyParts.length;l++)
                   if (l!=j && currSentCopyParts[l].equals(currSentCopyParts[j]))
                     duplicates=true;
                if(duplicates)
                 {
                     return false;
                }
            }
            
            int i;
            for (i = 0; i < currSentCopyParts.length; i++) {
                boolean result = resolveSentCTP(currSent, currSentCopyParts[i], depth, switchOnDuplicates);
                if (result) {
                    return true;
                }

            }
            if (i == currSentCopyParts.length) {
                //outputMap.add(false);
                return false;
            }

        }

        Map keepCountMapMap = new HashMap();

        int no = 0;

        for (Object inKB : finalKB) {

            no++;

            String sentInKB = (String) inKB;

            if (currPredToResolve.contains("~")) {
                int indexOfNeg = currPredToResolve.indexOf("~");
                int indexOfLeftPar = currPredToResolve.indexOf("(");
                int indexOfRightPar = currPredToResolve.indexOf(")");

                String currPred = currPredToResolve.substring(indexOfNeg + 1, indexOfLeftPar + 1);

                String parametersInCurrSent = currPredToResolve.substring(indexOfLeftPar + 1, indexOfRightPar);
                ArrayList parsInCurrSent = new ArrayList();

                if (parametersInCurrSent.contains(",")) {
                    String pars[] = parametersInCurrSent.split(",");
                    for (String i : pars) {
                        parsInCurrSent.add(i);
                    }
                } else {
                    parsInCurrSent.add(parametersInCurrSent);
                }

                String neg = "~";
                String sentInKBToCheck = sentInKB;
                if ((sentInKBToCheck.contains(neg + currPred))) {
                    int rightParaCurrSentIndex = 0;
                    do {
                        if (!sentInKBToCheck.contains(neg + currPred)) {
                            break;
                        }

                        rightParaCurrSentIndex = sentInKBToCheck.indexOf(")");
                        while (rightParaCurrSentIndex < sentInKBToCheck.indexOf(neg + currPred)) {
                            rightParaCurrSentIndex = sentInKBToCheck.indexOf(")", rightParaCurrSentIndex + 1);
                        }

                        if (sentInKBToCheck.indexOf(neg + currPred) == 0 && rightParaCurrSentIndex != sentInKBToCheck.length() - 1) {
                            String toRemove = sentInKBToCheck.substring(sentInKBToCheck.indexOf(neg + currPred), rightParaCurrSentIndex + 2);
                            sentInKBToCheck = sentInKBToCheck.replace(toRemove, "");
                        } else if (sentInKBToCheck.indexOf(neg + currPred) == 0 && rightParaCurrSentIndex == sentInKBToCheck.length() - 1) {
                            String toRemove = sentInKBToCheck.substring(sentInKBToCheck.indexOf(neg + currPred), rightParaCurrSentIndex + 1);
                            sentInKBToCheck = sentInKBToCheck.replace(toRemove, "");
                        } else {
                            String toRemove = sentInKBToCheck.substring(sentInKBToCheck.indexOf(neg + currPred) - 1, rightParaCurrSentIndex + 1);
                            sentInKBToCheck = sentInKBToCheck.replace(toRemove, "");
                        }
                    } while (true);
                }

                String currSentInSentKB = sentInKBToCheck;

                int countOfSame = 0;
                for (int index2 = currSentInSentKB.indexOf(currPred); index2 >= 0; index2 = currSentInSentKB.indexOf(currPred, index2 + 1)) {
                    countOfSame++;
                }

                if (countOfSame > 1) {
                    if (!keepCountMapMap.containsKey(no)) {
                        ArrayList par = new ArrayList();
                        keepCountMapMap.put(no, par);
                    }
                }

                if ((sentInKBToCheck.contains(currPred)) && (sentInKBToCheck.indexOf(currPred) == 0 || !(sentInKBToCheck.substring(sentInKBToCheck.indexOf(currPred) - 1, sentInKBToCheck.indexOf(currPred))).equals("~"))) {
                    String sentInKBCopy = sentInKB;

                    /* int leftParaIndex = sentInKB.indexOf("(");
                    while (leftParaIndex < sentInKB.indexOf(currPred)) {
                        leftParaIndex = sentInKB.indexOf("(", leftParaIndex + 1);
                    }

                    int rightParaIndex = sentInKB.indexOf(")");
                    while (rightParaIndex < sentInKB.indexOf(currPred)) {
                        rightParaIndex = sentInKB.indexOf(")", rightParaIndex + 1);
                    }

                    //Split using COMMA
                    //String parametersInKBSent = sentInKB.substring(leftParaIndex + 1, rightParaIndex);
                    
                     */
                    String parametersInKBSent = "";
                    ArrayList storedParameters = null;
                    if (keepCountMapMap.containsKey(no)) {
                        storedParameters = (ArrayList) keepCountMapMap.get(no);
                    }

                    int leftParaIndex = sentInKBToCheck.indexOf("(");
                    int rightParaIndex = sentInKBToCheck.indexOf(")");
                    int currPredIndex = sentInKBToCheck.indexOf(currPred);
                    if (keepCountMapMap.containsKey(no)) {
                        do {
                            while (leftParaIndex < currPredIndex) {
                                leftParaIndex = sentInKBToCheck.indexOf("(", leftParaIndex + 1);
                            }

                            while (rightParaIndex < currPredIndex) {
                                rightParaIndex = sentInKBToCheck.indexOf(")", rightParaIndex + 1);
                            }

                            //Split using COMMA
                            parametersInKBSent = sentInKBToCheck.substring(leftParaIndex + 1, rightParaIndex);

                            //System.out.println(parametersInKBSent);
                            if (storedParameters.contains(parametersInKBSent)) {
                                currPredIndex = sentInKBToCheck.indexOf(currPred, currPredIndex + 1);
                            } else {
                                storedParameters.add(parametersInKBSent);
                                break;
                            }
                        } while (leftParaIndex != -1);

                    } else {
                        leftParaIndex = sentInKBToCheck.indexOf("(");
                        while (leftParaIndex < sentInKBToCheck.indexOf(currPred)) {
                            leftParaIndex = sentInKBToCheck.indexOf("(", leftParaIndex + 1);
                        }

                        rightParaIndex = sentInKBToCheck.indexOf(")");
                        while (rightParaIndex < sentInKBToCheck.indexOf(currPred)) {
                            rightParaIndex = sentInKBToCheck.indexOf(")", rightParaIndex + 1);
                        }

                        //Split using COMMA
                        parametersInKBSent = sentInKBToCheck.substring(leftParaIndex + 1, rightParaIndex);
                    }

                    Map unification = new HashMap();
                    if (parametersInKBSent.contains(",")) {
                        String pars[] = parametersInKBSent.split(",");
                        boolean isMoreInKB = false;
                        
                        for (int i = 0; i < pars.length; i++) {
                            for (int j = i+1; j < pars.length; j++) {
                                if(pars[i].equals(pars[j]) && !((String)parsInCurrSent.get(i)).equals((String)parsInCurrSent.get(j)))
                                {
                                    if (!((String) finalKB.get(finalKB.size() - 1)).equals(sentInKB)) {
                                        isMoreInKB = true;
                                        break;
                                    }
                                    return false;
                                }
                            }
                            if(isMoreInKB)
                            {
                                break;
                            }
                        }
                        if(isMoreInKB)
                        {
                            continue;
                        }
                        
                        
                        for (int i = 0; i < pars.length; i++) {

                            if ((pars[i].charAt(0) >= 'A') && (pars[i].charAt(0) <= 'Z')
                                    && (((String) parsInCurrSent.get(i)).charAt(0) >= 'A') && (((String) parsInCurrSent.get(i)).charAt(0) <= 'Z')) {
                                if (!pars[i].equals(parsInCurrSent.get(i))) {
                                    if (!((String) finalKB.get(finalKB.size() - 1)).equals(sentInKB)) {
                                        isMoreInKB = true;
                                        break;
                                    }
                                    return false;
                                }
                            } else if ((pars[i].charAt(0) >= 'A') && (pars[i].charAt(0) <= 'Z')) {
                                unification.put(parsInCurrSent.get(i), pars[i]);
                            } else if ((((String) parsInCurrSent.get(i)).charAt(0) >= 'a')
                                    && ((((String) parsInCurrSent.get(i)).charAt(0) <= 'z'))) {
                                unification.put(pars[i], parsInCurrSent.get(i) + "1");
                                //unification.put(pars[i], parsInCurrSent.get(i)+"1");
                            } else {
                                unification.put(pars[i], parsInCurrSent.get(i));
                            }

                        }
                        if (isMoreInKB) {
                            continue;
                        }

                    } else if ((parametersInKBSent.charAt(0) >= 'A') && (parametersInKBSent.charAt(0) <= 'Z')
                            && (((String) parsInCurrSent.get(0)).charAt(0) >= 'A') && (((String) parsInCurrSent.get(0)).charAt(0) <= 'Z')) {
                        if (!parametersInKBSent.equals(parsInCurrSent.get(0))) {
                            if (!((String) finalKB.get(finalKB.size() - 1)).equals(sentInKB)) {
                                //isMoreInKB = true;
                                continue;
                            }
                            return false;
                        }
                    } else if ((parametersInKBSent.charAt(0) >= 'A') && (parametersInKBSent.charAt(0) <= 'Z')) {
                        unification.put(parsInCurrSent.get(0), parametersInKBSent);
                    } else if ((((String) parsInCurrSent.get(0)).charAt(0) >= 'a') && ((((String) parsInCurrSent.get(0)).charAt(0) <= 'z'))) {
                        unification.put(parametersInKBSent, parsInCurrSent.get(0) + "1");
                        //unification.put(parsInCurrSent.get(0), parsInCurrSent.get(0)+"1");
                    } else {
                        unification.put(parametersInKBSent, parsInCurrSent.get(0));
                    }

                    String toResolveNext = "";

                    String currPredCopy = currPred;
                    currPred += parametersInKBSent;

                    rightParaIndex = sentInKB.indexOf(")");
                    while (rightParaIndex < sentInKB.indexOf(currPred)) {
                        rightParaIndex = sentInKB.indexOf(")", rightParaIndex + 1);
                    }

                    int k = sentInKB.indexOf(currPred);
                    //F(x) | H(x)
                    if (sentInKB.indexOf(currPred) == 0 && rightParaIndex != sentInKB.length() - 1) {
                        String resolvedClause = (String) sentInKB.substring(sentInKB.indexOf(currPred), rightParaIndex + 2);
                        toResolveNext = sentInKBCopy.replace(resolvedClause, "");

                        currPred = currPredCopy;

                        String resolvedClause2 = "";
                        String toRemove = "~" + currPred;

                        int rightParaCurrSentIndex = currSent.indexOf(")");
                        while (rightParaCurrSentIndex < currSent.indexOf(toRemove)) {
                            rightParaCurrSentIndex = currSent.indexOf(")", rightParaCurrSentIndex + 1);
                        }

                        if (currSent.indexOf(toRemove) == 0 && rightParaCurrSentIndex != currSent.length() - 1) {
                            resolvedClause2 = (String) currSent.substring(currSent.indexOf(toRemove), rightParaCurrSentIndex + 2);
                        } else if (currSent.indexOf(toRemove) == 0 && rightParaCurrSentIndex == currSent.length() - 1) {
                            resolvedClause2 = (String) currSent.substring(currSent.indexOf(toRemove), rightParaCurrSentIndex + 1);
                        } else {
                            resolvedClause2 = (String) currSent.substring(currSent.indexOf(toRemove) - 1, rightParaCurrSentIndex + 1);
                        }

                        String currSentCopy = currSent.replace(resolvedClause2, "").replaceAll("\\s+", "");
                        if (!currSentCopy.equals("") && !toResolveNext.equals("")) {
                            toResolveNext += "|" + currSentCopy.replace(resolvedClause, "");
                        }
                        if (!currSentCopy.equals("") && toResolveNext.equals("")) {
                            toResolveNext += currSentCopy.replace(resolvedClause, "");
                        }
                        //System.out.println("Hello"+toResolveNext);

                    } //F(x)
                    else if (sentInKB.indexOf(currPred) == 0 && rightParaIndex == sentInKB.length() - 1) {
                        String resolvedClause = (String) sentInKB.substring(sentInKB.indexOf(currPred), rightParaIndex + 1);
                        toResolveNext = sentInKBCopy.replace(resolvedClause, "");

                        currPred = currPredCopy;

                        String resolvedClause2 = "";
                        String toRemove = "~" + currPred;

                        int rightParaCurrSentIndex = currSent.indexOf(")");
                        while (rightParaCurrSentIndex < currSent.indexOf(toRemove)) {
                            rightParaCurrSentIndex = currSent.indexOf(")", rightParaCurrSentIndex + 1);
                        }

                        if (currSent.indexOf(toRemove) == 0 && rightParaCurrSentIndex != currSent.length() - 1) {
                            resolvedClause2 = (String) currSent.substring(currSent.indexOf(toRemove), rightParaCurrSentIndex + 2);
                        } else if (currSent.indexOf(toRemove) == 0 && rightParaCurrSentIndex == currSent.length() - 1) {
                            resolvedClause2 = (String) currSent.substring(currSent.indexOf(toRemove), rightParaCurrSentIndex + 1);
                        } else {
                            resolvedClause2 = (String) currSent.substring(currSent.indexOf(toRemove) - 1, rightParaCurrSentIndex + 1);
                        }

                        String currSentCopy = currSent.replace(resolvedClause2, "").replaceAll("\\s+", "");
                        if (!currSentCopy.equals("") && !toResolveNext.equals("")) {
                            toResolveNext += "|" + currSentCopy.replace(resolvedClause, "");
                        }
                        if (!currSentCopy.equals("") && toResolveNext.equals("")) {
                            toResolveNext += currSentCopy.replace(resolvedClause, "");
                        }
                        //System.out.println("Hello"+toResolveNext);
                    } //G(x) | F(x) | H(x)   OR   G(x) | H(x) | F(x)
                    else {
                        String resolvedClause = (String) sentInKB.substring(sentInKB.indexOf(currPred) - 1, rightParaIndex + 1);
                        toResolveNext = sentInKBCopy.replace(resolvedClause, "");

                        currPred = currPredCopy;

                        String resolvedClause2 = "";
                        String toRemove = "~" + currPred;

                        int rightParaCurrSentIndex = currSent.indexOf(")");
                        while (rightParaCurrSentIndex < currSent.indexOf(toRemove)) {
                            rightParaCurrSentIndex = currSent.indexOf(")", rightParaCurrSentIndex + 1);
                        }

                        if (currSent.indexOf(toRemove) == 0 && rightParaCurrSentIndex != currSent.length() - 1) {
                            resolvedClause2 = (String) currSent.substring(currSent.indexOf(toRemove), rightParaCurrSentIndex + 2);
                        } else if (currSent.indexOf(toRemove) == 0 && rightParaCurrSentIndex == currSent.length() - 1) {
                            resolvedClause2 = (String) currSent.substring(currSent.indexOf(toRemove), rightParaCurrSentIndex + 1);
                        } else {
                            resolvedClause2 = (String) currSent.substring(currSent.indexOf(toRemove) - 1, rightParaCurrSentIndex + 1);
                        }

                        String currSentCopy = currSent.replace(resolvedClause2, "").replaceAll("\\s+", "");
                        if (!currSentCopy.equals("") && !toResolveNext.equals("")) {
                            toResolveNext += "|" + currSentCopy.replace(resolvedClause, "");
                        }
                        if (!currSentCopy.equals("") && toResolveNext.equals("")) {
                            toResolveNext += currSentCopy.replace(resolvedClause, "");
                        }
                        //System.out.println("Hello"+toResolveNext);
                    }

                    toResolveNext = toResolveNext.replaceAll("\\s+", "");

                    if ((toResolveNext.isEmpty() || toResolveNext.trim().equals("") || toResolveNext.trim().equals("\n"))) {
                        //outputMap.add(true);
                        return true;
                    } else {

                        //Unify parameters in next ToResolve Sentence                        
                        ArrayList arrOfPar = null;
                        ArrayList arrayOfStartPos = new ArrayList();
                        ArrayList arrayOfEndPos = new ArrayList();
                        int pairofPar = -1;

                        //String currSentCopyParts[] = toResolveNext.split("|"); 
                        //System.out.println("the answer is"+toResolveNext.split("[|]")[0]);
                        String toResolveNextCopy = toResolveNext;

                        int firstloop = 0;
                        boolean duplicates = false;
                        for (int index = 0; index < toResolveNext.length(); index++) {
                            char c = toResolveNext.charAt(index);
                            if (c == '(') {
                                pairofPar++;
                                if (pairofPar == 0) {
                                    if (arrayOfStartPos.isEmpty()) {
                                        firstloop = 1;
                                        arrayOfStartPos.add(index);
                                    } else {
                                        firstloop = 1;
                                        arrayOfStartPos.set(0, index);
                                    }
                                }
                            }

                            if (c == ')') {

                                if (firstloop > 1) {
                                    //firstloop=1;
                                    continue;
                                }

                                pairofPar--;

                                if (pairofPar == -1) {
                                    //arrayOfEndPos.add(index);
                                    if (arrayOfEndPos.isEmpty()) {
                                        arrayOfEndPos.add(index);
                                    } else {
                                        arrayOfEndPos.set(0, index);
                                    }

                                    String parStringOfRest = toResolveNext.substring((int) arrayOfStartPos.get(0) + 1, (int) arrayOfEndPos.get(0));

                                    if (parStringOfRest.contains(",")) {
                                        String pars[] = parStringOfRest.split(",");
                                        boolean toBeUnified = false;
                                        for (int i = 0; i < pars.length; i++) {
                                            //unification.put(pars[i], parsInCurrSent.get(i));
                                            if (unification.containsKey(pars[i])) {
                                                pars[i] = (String) unification.get(pars[i]);
                                                toBeUnified = true;
                                            }
                                        }

                                        if (toBeUnified) {
                                            if(depth>10)
                                            {
                                                switchOnDuplicates = true;
                                            }
                                            if(switchOnDuplicates)
                                            {
                                                 for (int j=0;j<pars.length;j++)
                                                   for (int l=j+1;l<pars.length;l++)
                                                     if (l!=j && pars[l].equals(pars[j]))
                                                       duplicates=true;
                                            }

                                            if (duplicates) {

                                            }

                                            String nameBuilder = "";
                                            for (String n : pars) {
                                                nameBuilder += n + ",";
                                                // can also do the following
                                                // nameBuilder.append("'").append(n.replace("'", "''")).append("',");
                                            }

                                            //nameBuilder.deleteCharAt(nameBuilder.length() - 1);
                                            String a = nameBuilder.substring(nameBuilder.length() - 1);
                                            nameBuilder = nameBuilder.substring(0, nameBuilder.length() - 1);

                                            String newParStringOfRest = nameBuilder;
                                            //System.out.println(newParStringOfRest);

                                            //String currSentCopyParts[] = newParStringOfRest.split("|");  
                                            //toResolveNext = toResolveNext.replace(parStringOfRest, newParStringOfRest);
                                            toResolveNext = toResolveNext.substring(0, (int) arrayOfStartPos.get(0) + 1) + newParStringOfRest + toResolveNext.substring((int) arrayOfEndPos.get(0));

                                        }

                                        //String currSentCopyParts[] = toResolveNext.split("|");  
                                        //System.out.println("Hello After Paramters: "+toResolveNext);
                                        //break;
                                    } else {
                                        String newParStringOfRest = "";
                                        if (unification.containsKey(parStringOfRest)) {
                                            newParStringOfRest = (String) unification.get(parStringOfRest);
                                            String rep = toResolveNext.substring((int) arrayOfStartPos.get(0) + 1, (int) arrayOfEndPos.get(0));
                                            //toResolveNext = toResolveNext.replace(toResolveNext.substring((int)arrayOfStartPos.get(0)+1, (int)arrayOfEndPos.get(0)), newParStringOfRest);                                        
                                            toResolveNext = toResolveNext.substring(0, (int) arrayOfStartPos.get(0) + 1) + newParStringOfRest + toResolveNext.substring((int) arrayOfEndPos.get(0));
                                        }

                                        //System.out.println("Hello After Paramters: "+toResolveNext);
                                        //break;
                                    }
                                    firstloop++;
                                }
                            }

                        }

                        if(duplicates)
                        {
                            if(!((String)finalKB.get(finalKB.size()-1)).equals(sentInKB))
                            {
                                //isMoreInKB = true;
                                continue;
                            }
                            return false;
                        }
                        predAlrdyResolved.add(currSent);
                        boolean result = resolveSentCTP(toResolveNext, toResolveNext, depth+1, switchOnDuplicates);

                        if (result) {
                            return true;
                        }

                        // System.out.println("Current Sentence After Coming Back: "+currSent);
                        // System.out.println("Current Predicate To Resolve After Coming Back: "+currPredToResolve);
                        // System.out.println("");
                    }

                }

            } //IF NO ~ IN THE QUERY
            else {
                String neg = "~";
                int indexOfLeftPar = currPredToResolve.indexOf("(");
                int indexOfRightPar = currPredToResolve.indexOf(")");

                String currPred = neg + currPredToResolve.substring(0, indexOfLeftPar + 1);

                String parametersInCurrSent = currPredToResolve.substring(indexOfLeftPar + 1, indexOfRightPar);
                ArrayList parsInCurrSent = new ArrayList();

                if (parametersInCurrSent.contains(",")) {
                    String pars[] = parametersInCurrSent.split(",");
                    for (String i : pars) {
                        parsInCurrSent.add(i);
                    }
                } else {
                    parsInCurrSent.add(parametersInCurrSent);
                }

                //String parametersInKBSent = "";
                //ArrayList storedParameters = null;
                String sentInKBToCheck = sentInKB;

                Map keepCountMap = new HashMap();

                int countOfSame = 0;
                for (int index2 = sentInKBToCheck.indexOf(currPred); index2 >= 0; index2 = sentInKBToCheck.indexOf(currPred, index2 + 1)) {
                    countOfSame++;
                }

                if (countOfSame > 1) {
                    if (!keepCountMap.containsKey(no)) {
                        ArrayList par = new ArrayList();
                        keepCountMap.put(no, par);
                    }
                }

                ///////Start looking!!!!!!!!
                if ((sentInKB.contains(currPred))) {

                    String sentInKBCopy = sentInKB;

                    String parametersInKBSent = "";
                    ArrayList storedParameters = null;

                    if (keepCountMap.containsKey(no)) {
                        storedParameters = (ArrayList) keepCountMap.get(no);
                    }

                    int leftParaIndex = sentInKBToCheck.indexOf("(");
                    int rightParaIndex = sentInKBToCheck.indexOf(")");
                    int currPredIndex = sentInKBToCheck.indexOf(currPred);
                    if (keepCountMap.containsKey(no)) {
                        do {
                            while (leftParaIndex < currPredIndex) {
                                leftParaIndex = sentInKBToCheck.indexOf("(", leftParaIndex + 1);
                            }

                            while (rightParaIndex < currPredIndex) {
                                rightParaIndex = sentInKBToCheck.indexOf(")", rightParaIndex + 1);
                            }

                            //Split using COMMA
                            parametersInKBSent = sentInKBToCheck.substring(leftParaIndex + 1, rightParaIndex);

                            //System.out.println(parametersInKBSent);
                            if (storedParameters.contains(parametersInKBSent)) {
                                currPredIndex = sentInKB.indexOf(currPred, currPredIndex + 1);
                            } else {
                                storedParameters.add(parametersInKBSent);
                                break;
                            }
                        } while (currPredIndex != -1);

                    } else {
                        leftParaIndex = sentInKB.indexOf("(");
                        while (leftParaIndex < sentInKB.indexOf(currPred)) {
                            leftParaIndex = sentInKB.indexOf("(", leftParaIndex + 1);
                        }

                        rightParaIndex = sentInKB.indexOf(")");
                        while (rightParaIndex < sentInKB.indexOf(currPred)) {
                            rightParaIndex = sentInKB.indexOf(")", rightParaIndex + 1);
                        }

                        //Split using COMMA
                        parametersInKBSent = sentInKB.substring(leftParaIndex + 1, rightParaIndex);
                    }

                    Map unification = new HashMap();
                    if (parametersInKBSent.contains(",")) {
                        String pars[] = parametersInKBSent.split(",");
                        boolean isMoreInKB = false;
                        
                        for (int i = 0; i < pars.length; i++) {
                            for (int j = i+1; j < pars.length; j++) {
                                if(pars[i].equals(pars[j]) && !((String)parsInCurrSent.get(i)).equals((String)parsInCurrSent.get(j)))
                                {
                                    if (!((String) finalKB.get(finalKB.size() - 1)).equals(sentInKB)) {
                                        isMoreInKB = true;
                                        break;
                                    }
                                    return false;
                                }
                            }
                            if(isMoreInKB)
                            {
                                break;
                            }
                        }
                        if(isMoreInKB)
                        {
                            continue;
                        }
                        
                        
                        for (int i = 0; i < pars.length; i++) {

                            if ((pars[i].charAt(0) >= 'A') && (pars[i].charAt(0) <= 'Z')
                                    && (((String) parsInCurrSent.get(i)).charAt(0) >= 'A') && (((String) parsInCurrSent.get(i)).charAt(0) <= 'Z')) {
                                if (!pars[i].equals(parsInCurrSent.get(i))) {
                                    if (!((String) finalKB.get(finalKB.size() - 1)).equals(sentInKB)) {
                                        isMoreInKB = true;
                                        break;
                                    }
                                    return false;
                                }
                            } else if ((pars[i].charAt(0) >= 'A') && (pars[i].charAt(0) <= 'Z')) {
                                unification.put(parsInCurrSent.get(i), pars[i]);
                            } else if ((((String) parsInCurrSent.get(i)).charAt(0) >= 'a')
                                    && ((((String) parsInCurrSent.get(i)).charAt(0) <= 'z'))) {
                                unification.put(pars[i], parsInCurrSent.get(i) + "1");
                                //unification.put(pars[i], parsInCurrSent.get(i)+"1");
                            } else {
                                unification.put(pars[i], parsInCurrSent.get(i));
                            }

                        }
                        if (isMoreInKB) {
                            continue;
                        }

                    } else if ((parametersInKBSent.charAt(0) >= 'A') && (parametersInKBSent.charAt(0) <= 'Z')
                            && (((String) parsInCurrSent.get(0)).charAt(0) >= 'A') && (((String) parsInCurrSent.get(0)).charAt(0) <= 'Z')) {
                        if (!parametersInKBSent.equals(parsInCurrSent.get(0))) {
                            if (!((String) finalKB.get(finalKB.size() - 1)).equals(sentInKB)) {
                                //isMoreInKB = true;
                                continue;
                            }
                            return false;

                        }
                    } else if ((parametersInKBSent.charAt(0) >= 'A') && (parametersInKBSent.charAt(0) <= 'Z')) {
                        unification.put(parsInCurrSent.get(0), parametersInKBSent);
                    } else if ((((String) parsInCurrSent.get(0)).charAt(0) >= 'a') && ((((String) parsInCurrSent.get(0)).charAt(0) <= 'z'))) {
                        unification.put(parametersInKBSent, parsInCurrSent.get(0) + "1");
                        //unification.put(parsInCurrSent.get(0), parsInCurrSent.get(0)+"1");
                    } else {
                        unification.put(parametersInKBSent, parsInCurrSent.get(0));
                    }

                    String toResolveNext = "";

                    String currPredCopy = currPred;
                    currPred += parametersInKBSent;

                    rightParaIndex = sentInKB.indexOf(")");
                    while (rightParaIndex < sentInKB.indexOf(currPred)) {
                        rightParaIndex = sentInKB.indexOf(")", rightParaIndex + 1);
                    }

                    int k = sentInKB.indexOf(currPred);
                    //F(x) | H(x)
                    if (sentInKB.indexOf(currPred) == 0 && rightParaIndex != sentInKB.length() - 1) {
                        String resolvedClause = (String) sentInKB.substring(sentInKB.indexOf(currPred), rightParaIndex + 2);
                        toResolveNext = sentInKBCopy.replace(resolvedClause, "");

                        currPred = currPredCopy;

                        String resolvedClause2 = "";
                        String toRemove = currPred.replace(neg, "");

                        int rightParaCurrSentIndex = currSent.indexOf(")");
                        while (rightParaCurrSentIndex < currSent.indexOf(toRemove)) {
                            rightParaCurrSentIndex = currSent.indexOf(")", rightParaCurrSentIndex + 1);
                        }

                        if (currSent.indexOf(toRemove) == 0 && rightParaCurrSentIndex != currSent.length() - 1) {
                            resolvedClause2 = (String) currSent.substring(currSent.indexOf(toRemove), rightParaCurrSentIndex + 2);
                        } else if (currSent.indexOf(toRemove) == 0 && rightParaCurrSentIndex == currSent.length() - 1) {
                            resolvedClause2 = (String) currSent.substring(currSent.indexOf(toRemove), rightParaCurrSentIndex + 1);
                        } else {
                            resolvedClause2 = (String) currSent.substring(currSent.indexOf(toRemove) - 1, rightParaCurrSentIndex + 1);
                        }

                        String currSentCopy = currSent.replace(resolvedClause2, "").replaceAll("\\s+", "");
                        if (!currSentCopy.equals("") && !toResolveNext.equals("")) {
                            toResolveNext += "|" + currSentCopy.replace(resolvedClause, "");
                        }
                        if (!currSentCopy.equals("") && toResolveNext.equals("")) {
                            toResolveNext += currSentCopy.replace(resolvedClause, "");
                        }
                        //System.out.println("Hello"+toResolveNext);

                    } //F(x)
                    else if (sentInKB.indexOf(currPred) == 0 && rightParaIndex == sentInKB.length() - 1) {
                        String resolvedClause = (String) sentInKB.substring(sentInKB.indexOf(currPred), rightParaIndex + 1);
                        toResolveNext = sentInKBCopy.replace(resolvedClause, "");

                        currPred = currPredCopy;

                        String resolvedClause2 = "";
                        String toRemove = currPred.replace(neg, "");

                        int rightParaCurrSentIndex = currSent.indexOf(")");
                        while (rightParaCurrSentIndex < currSent.indexOf(toRemove)) {
                            rightParaCurrSentIndex = currSent.indexOf(")", rightParaCurrSentIndex + 1);
                        }

                        if (currSent.indexOf(toRemove) == 0 && rightParaCurrSentIndex != currSent.length() - 1) {
                            resolvedClause2 = (String) currSent.substring(currSent.indexOf(toRemove), rightParaCurrSentIndex + 2);
                        } else if (currSent.indexOf(toRemove) == 0 && rightParaCurrSentIndex == currSent.length() - 1) {
                            resolvedClause2 = (String) currSent.substring(currSent.indexOf(toRemove), rightParaCurrSentIndex + 1);
                        } else {
                            resolvedClause2 = (String) currSent.substring(currSent.indexOf(toRemove) - 1, rightParaCurrSentIndex + 1);
                        }

                        String currSentCopy = currSent.replace(resolvedClause2, "").replaceAll("\\s+", "");
                        if (!currSentCopy.equals("") && !toResolveNext.equals("")) {
                            toResolveNext += "|" + currSentCopy.replace(resolvedClause, "");
                        }
                        if (!currSentCopy.equals("") && toResolveNext.equals("")) {
                            toResolveNext += currSentCopy.replace(resolvedClause, "");
                        }
                        //System.out.println("Hello"+toResolveNext);
                    } //G(x) | F(x) | H(x)   OR   G(x) | H(x) | F(x)
                    else {
                        String resolvedClause = (String) sentInKB.substring(sentInKB.indexOf(currPred) - 1, rightParaIndex + 1);
                        toResolveNext = sentInKBCopy.replace(resolvedClause, "");

                        currPred = currPredCopy;

                        String resolvedClause2 = "";
                        String toRemove = currPred.replace(neg, "");

                        int rightParaCurrSentIndex = currSent.indexOf(")");
                        while (rightParaCurrSentIndex < currSent.indexOf(toRemove)) {
                            rightParaCurrSentIndex = currSent.indexOf(")", rightParaCurrSentIndex + 1);
                        }

                        if (currSent.indexOf(toRemove) == 0 && rightParaCurrSentIndex != currSent.length() - 1) {
                            resolvedClause2 = (String) currSent.substring(currSent.indexOf(toRemove), rightParaCurrSentIndex + 2);
                        } else if (currSent.indexOf(toRemove) == 0 && rightParaCurrSentIndex == currSent.length() - 1) {
                            resolvedClause2 = (String) currSent.substring(currSent.indexOf(toRemove), rightParaCurrSentIndex + 1);
                        } else {
                            resolvedClause2 = (String) currSent.substring(currSent.indexOf(toRemove) - 1, rightParaCurrSentIndex + 1);
                        }

                        String currSentCopy = currSent.replace(resolvedClause2, "").replaceAll("\\s+", "");
                        if (!currSentCopy.equals("") && !toResolveNext.equals("")) {
                            toResolveNext += "|" + currSentCopy.replace(resolvedClause, "");
                        }
                        if (!currSentCopy.equals("") && toResolveNext.equals("")) {
                            toResolveNext += currSentCopy.replace(resolvedClause, "");
                        }
                        //System.out.println("Hello"+toResolveNext);
                    }

                    toResolveNext = toResolveNext.replaceAll("\\s+", "");

                    if ((toResolveNext.isEmpty() || toResolveNext.trim().equals("") || toResolveNext.trim().equals("\n"))) {
                        //outputMap.add(true);
                        return true;
                    } else {

                        //Unify parameters in next ToResolve Sentence                        
                        ArrayList arrOfPar = null;
                        ArrayList arrayOfStartPos = new ArrayList();
                        ArrayList arrayOfEndPos = new ArrayList();
                        int pairofPar = -1;

                        //String currSentCopyParts[] = toResolveNext.split("|"); 
                        //System.out.println("the answer is"+toResolveNext.split("[|]")[0]);
                        String toResolveNextCopy = toResolveNext;

                        int firstloop = 0;
                        boolean duplicates = false;
                        for (int index = 0; index < toResolveNext.length(); index++) {
                            char c = toResolveNext.charAt(index);
                            if (c == '(') {
                                pairofPar++;
                                if (pairofPar == 0) {
                                    if (arrayOfStartPos.isEmpty()) {
                                        firstloop = 1;
                                        arrayOfStartPos.add(index);
                                    } else {
                                        firstloop = 1;
                                        arrayOfStartPos.set(0, index);
                                    }
                                }
                            }

                            if (c == ')') {

                                if (firstloop > 1) {
                                    //firstloop=1;
                                    continue;
                                }

                                pairofPar--;

                                if (pairofPar == -1) {
                                    //arrayOfEndPos.add(index);
                                    if (arrayOfEndPos.isEmpty()) {
                                        arrayOfEndPos.add(index);
                                    } else {
                                        arrayOfEndPos.set(0, index);
                                    }

                                    String parStringOfRest = toResolveNext.substring((int) arrayOfStartPos.get(0) + 1, (int) arrayOfEndPos.get(0));

                                    if (parStringOfRest.contains(",")) {
                                        String pars[] = parStringOfRest.split(",");
                                        boolean toBeUnified = false;
                                        for (int i = 0; i < pars.length; i++) {
                                            //unification.put(pars[i], parsInCurrSent.get(i));
                                            if (unification.containsKey(pars[i])) {
                                                pars[i] = (String) unification.get(pars[i]);
                                                toBeUnified = true;
                                            }
                                        }

                                        if (toBeUnified) {
                                            if(depth>10)
                                            {
                                                switchOnDuplicates = true;
                                            }
                                            if(switchOnDuplicates)
                                            {
                                                 for (int j=0;j<pars.length;j++)
                                                   for (int l=j+1;l<pars.length;l++)
                                                     if (l!=j && pars[l].equals(pars[j]))
                                                       duplicates=true;
                                            }
                                            

                                            if (duplicates) {

                                            }

                                            String nameBuilder = "";
                                            for (String n : pars) {
                                                nameBuilder += n + ",";
                                                // can also do the following
                                                // nameBuilder.append("'").append(n.replace("'", "''")).append("',");
                                            }

                                            //nameBuilder.deleteCharAt(nameBuilder.length() - 1);
                                            String a = nameBuilder.substring(nameBuilder.length() - 1);
                                            nameBuilder = nameBuilder.substring(0, nameBuilder.length() - 1);

                                            String newParStringOfRest = nameBuilder;
                                            //System.out.println(newParStringOfRest);

                                            //String currSentCopyParts[] = newParStringOfRest.split("|");  
                                            //toResolveNext = toResolveNext.replace(parStringOfRest, newParStringOfRest);
                                            toResolveNext = toResolveNext.substring(0, (int) arrayOfStartPos.get(0) + 1) + newParStringOfRest + toResolveNext.substring((int) arrayOfEndPos.get(0));
                                        }

                                        //String currSentCopyParts[] = toResolveNext.split("|");  
                                        //System.out.println("Hello After Paramters: "+toResolveNext);
                                        //break;
                                    } else {
                                        String newParStringOfRest = "";
                                        if (unification.containsKey(parStringOfRest)) {
                                            newParStringOfRest = (String) unification.get(parStringOfRest);
                                            String rep = toResolveNext.substring((int) arrayOfStartPos.get(0) + 1, (int) arrayOfEndPos.get(0));
                                            //toResolveNext = toResolveNext.replace(toResolveNext.substring((int)arrayOfStartPos.get(0)+1, (int)arrayOfEndPos.get(0)), newParStringOfRest);                                        
                                            toResolveNext = toResolveNext.substring(0, (int) arrayOfStartPos.get(0) + 1) + newParStringOfRest + toResolveNext.substring((int) arrayOfEndPos.get(0));
                                        }

                                        //System.out.println("Hello After Paramters: "+toResolveNext);
                                        //break;
                                    }
                                    firstloop++;
                                }
                            }

                        }

                       if(duplicates)
                        {
                            if(!((String)finalKB.get(finalKB.size()-1)).equals(sentInKB))
                            {
                                //isMoreInKB = true;
                                continue;
                            }
                            return false;
                        }
                        predAlrdyResolved.add(currSent);
                        boolean result = resolveSentCTP(toResolveNext, toResolveNext, depth+1, switchOnDuplicates);

                        if (result) {
                            return true;
                        }

                        // System.out.println("Current Sentence After Coming Back: "+currSent);
                        // System.out.println("Current Predicate To Resolve After Coming Back: "+currPredToResolve);
                        // System.out.println("");
                    }

                }
            }

        }
        return false;

    }

    private void createKB() {
        Iterator it = KBMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();

            String ruleKB = (String) pair.getKey();

            if (((HashMap) pair.getValue()).size() == 0) {
                finalKB.add(0, ruleKB);
            } else {
                parsedClauseMap = (HashMap) pair.getValue();
                String finalSent = formulateCNFSent((double) 1.0, "");

                //System.out.println(finalSent);
                if (finalSent.contains("&") || finalSent.contains("|")) {
                    //System.out.println(finalSent + "\n\n");
                    placeOrOverAnd(finalSent);
                    continue;
                }
                finalKB.add(0,finalSent);
            }
        }
        //System.out.println(finalKB);
        int n = 0;
        for (Object i : finalKB) {
            n++;
            System.out.println(n + " :  " + (String) i);
        }
    }

    private void placeOrOverAnd(String line) {
        ArrayList sentANDORList = new ArrayList();

        sentANDORList.add(line);

        simplifyKB(sentANDORList);

        finalKB.remove(line);

        //System.out.println("");
        //System.out.println(finalKB);
    }

    private void simplifyKB(ArrayList sentANDORList) {
        ArrayList newList = null;

        for (Object ob : sentANDORList) {
            String line = (String) ob;
            newList = simplifyKBSent(line, "", "");
            //System.out.println(newList);
            int i;
            boolean containsAND = false;

            ArrayList strToRemove = new ArrayList();
            for (i = 0; i < newList.size(); i++) {
                String finalLine = (String) newList.get(i);
                if (!finalLine.contains("&")) {
                    if (finalLine.equals("Reversed")) {
                        continue;
                    }
                    if (finalLine.contains("|")) {
                        finalKB.add(finalLine);
                    } else {
                        finalKB.add(0, finalLine);
                    }
                    //System.out.println(finalLine);
                    strToRemove.add(i);
                } else {
                    containsAND = true;
                    //break;
                }
            }
            if (!containsAND) {
                continue;
            }

            for (Object obRem : strToRemove) {
                int index = (int) obRem;
                newList.remove(i);
            }
            simplifyKB(newList);
        }

    }

    private ArrayList simplifyKBSent(String leftPar, String rightPar, String symWithPar) {
        ArrayList arrOfPar = null;
        ArrayList arrayOfStartPos = new ArrayList();
        ArrayList arrayOfEndPos = new ArrayList();

        int pairofPar = -1;
        if (leftPar.contains("&")) {
            if (leftPar.charAt(0) == '(') {
                for (int i = 0; i < leftPar.length(); i++) {
                    char c = leftPar.charAt(i);
                    if (c == '(') {
                        pairofPar++;
                        if (pairofPar == 0) {
                            arrayOfStartPos.add(i);
                        }
                    }

                    if (c == ')') {
                        pairofPar--;

                        if (pairofPar == -1) {
                            arrayOfEndPos.add(i);
                        }
                    }

                }

                String leftParToSend = leftPar.substring((int) arrayOfStartPos.get(0) + 1, (int) arrayOfEndPos.get(0));
                String rightParToSend = "";
                if ((int) arrayOfEndPos.get(0) != leftPar.length() - 1) {
                    rightParToSend = leftPar.substring((int) arrayOfEndPos.get(0) + 2, leftPar.length());
                }

                String mainSym = "";
                if ((int) arrayOfEndPos.get(0) != leftPar.length() - 1) {
                    mainSym = leftPar.substring((int) arrayOfEndPos.get(0) + 1, (int) arrayOfEndPos.get(0) + 2);
                }

                arrOfPar = simplifyKBSent(leftParToSend, rightParToSend, mainSym);

                if (arrOfPar.get(arrOfPar.size() - 1).equals("Reversed")) {
                    arrOfPar.remove(arrOfPar.size() - 1);
                    return arrOfPar;
                }

                ArrayList returnList = null;
                if ((int) arrayOfEndPos.get(0) != leftPar.length() - 1) {
                    returnList = new ArrayList();
                    for (Object str : arrOfPar) {
                        String line = (String) str;

                        String sym = leftPar.substring((int) arrayOfEndPos.get(0) + 1, (int) arrayOfEndPos.get(0) + 2);
                        if (sym.equals("|")) {
                            /*if (rightPar.equals("")) {
                                returnList.add(line + "|" + leftPar.substring((int) arrayOfEndPos.get(0) + 2, leftPar.length()));
                            } else {
                                returnList.add(line + "|" + rightPar);
                            }*/

                            if (rightPar.equals("")) {
                                ArrayList arrOfPar2 = simplifyKBSent(leftPar.substring((int) arrayOfEndPos.get(0) + 2, leftPar.length()), "", "|");
                                for (Object str2 : arrOfPar2) {
                                    String par2 = (String)str2;
                                    returnList.add(line + "|" + par2);
                                }
                            } else {
                                ArrayList arrOfPar2 = simplifyKBSent(rightPar, "", "|");
                                for (Object str2 : arrOfPar2) {
                                    String par2 = (String)str2;
                                    returnList.add(line + "|" + par2);
                                }
                                //returnList.add(line + "|" + arrOfPar.get(0));
                            }
                        } else {
                            returnList.add(line);

                            if (rightPar.equals("")) {
                                arrOfPar = simplifyKBSent(leftPar.substring((int) arrayOfEndPos.get(0) + 2, leftPar.length()), "", "|");
                                for (Object strRight : arrOfPar) {
                                    String lineRight = (String) strRight;
                                    returnList.add(lineRight);
                                }
                            } else {
                                arrOfPar = simplifyKBSent(rightParToSend, "", "|");
                                for (Object strRight : arrOfPar) {
                                    String lineRight = (String) strRight;
                                    returnList.add(lineRight);
                                }
                            }
                        }
                    }
                } else {
                    returnList = arrOfPar;
                }
                return returnList;
            } else if (leftPar.contains("&") && leftPar.contains("|")) {
                if (leftPar.indexOf("&") < leftPar.indexOf("|")) {
                    int prevANDIndex = 0;
                    ArrayList returnList = new ArrayList();
                    for (int index = leftPar.indexOf("&"); index >= 0 && (leftPar.indexOf("&") < leftPar.indexOf("|")); index = leftPar.indexOf("&", index + 1)) {
                        int indexOfAND = leftPar.indexOf("&");

                        String leftParToSend = leftPar.substring(prevANDIndex, indexOfAND);
                        String rightParToSend = leftPar.substring(indexOfAND + 1, leftPar.length());

                        String mainSym = leftPar.substring(indexOfAND, indexOfAND + 1);
                        arrOfPar = simplifyKBSent(leftParToSend, rightParToSend, mainSym);

                        boolean isAddedToReturnList = false;
                        for (Object str : arrOfPar) {
                            String line = (String) str;
                            if (line.equals("Reversed")) {
                                continue;
                            }
                            String sym = leftPar.substring(indexOfAND, indexOfAND + 1);
                            isAddedToReturnList = true;
                            if (sym.equals("|")) {
                                returnList.add(line + "|" + leftPar.substring((int) arrayOfEndPos.get(0) + 2, leftPar.length()));
                            } else {
                                returnList.add(line);
                            }
                        }

                        if (isAddedToReturnList) {
                            break;
                        }

                        prevANDIndex = indexOfAND;
                    }
                    return returnList;
                } else {
                    int prevANDIndex = 0;
                    ArrayList returnList = new ArrayList();
                    for (int index = leftPar.indexOf("|"); index >= 0 && (leftPar.indexOf("|") < leftPar.indexOf("&")); index = leftPar.indexOf("|", index + 1)) {
                        int indexOfOR = leftPar.indexOf("|");

                        String leftParToSend = leftPar.substring(prevANDIndex, indexOfOR);
                        String rightParToSend = leftPar.substring(indexOfOR + 1, leftPar.length());

                        String mainSym = leftPar.substring(indexOfOR, indexOfOR + 1);
                        arrOfPar = simplifyKBSent(rightParToSend, leftParToSend, mainSym);

                        boolean isAddedToReturnList = false;
                        for (Object str : arrOfPar) {
                            String line = (String) str;
                            if (line.equals("Reversed")) {
                                continue;
                            }
                            String sym = leftPar.substring(indexOfOR, indexOfOR + 1);
                            isAddedToReturnList = true;

                            if (sym.equals("|")) {
                                returnList.add(line + "|" + leftParToSend);
                            } else {
                                returnList.add(line);
                            }
                        }

                        if (isAddedToReturnList) {
                            break;
                        }

                        prevANDIndex = indexOfOR;
                    }
                    return returnList;
                }
            } else if (leftPar.contains("&")) {
                if (!leftPar.contains("&(")) {
                    String[] arr = leftPar.split("&");
                    arrOfPar = new ArrayList();

                    for (String str : arr) {
                        arrOfPar.add(str);
                    }

                    return arrOfPar;
                } else {
                    ArrayList returnList = new ArrayList();
                    int indexOfLeftPar = leftPar.indexOf("&(");
                    String leftOfAND = leftPar.substring(0, indexOfLeftPar);
                    returnList.add(leftOfAND);

                    String rightOfAND = leftPar.substring(indexOfLeftPar + 1);
                    arrOfPar = simplifyKBSent(rightOfAND, "", "&");

                    for (Object str3 : arrOfPar) {
                        String line = (String) str3;
                        returnList.add(line);
                    }
                    return returnList;
                }
            } else {
                arrOfPar.add(leftPar);
            }
        } //No AND in the sentence, just remove the brackets
        else if (leftPar.contains("|") && !leftPar.contains("&")) {
            //System.out.println(leftPar);

            if (leftPar.charAt(0) == '(') {
                for (int i = 0; i < leftPar.length(); i++) {
                    char c = leftPar.charAt(i);
                    if (c == '(') {
                        pairofPar++;
                        if (pairofPar == 0) {
                            arrayOfStartPos.add(i);
                        }
                    }

                    if (c == ')') {
                        pairofPar--;

                        if (pairofPar == -1) {
                            arrayOfEndPos.add(i);
                        }
                    }

                }

                String leftParToSend = leftPar.substring((int) arrayOfStartPos.get(0) + 1, (int) arrayOfEndPos.get(0));
                String rightParToSend = "";
                if ((int) arrayOfEndPos.get(0) != leftPar.length() - 1) {
                    rightParToSend = leftPar.substring((int) arrayOfEndPos.get(0) + 2, leftPar.length());
                }

                String mainSym = "";
                if ((int) arrayOfEndPos.get(0) != leftPar.length() - 1) {
                    mainSym = leftPar.substring((int) arrayOfEndPos.get(0) + 1, (int) arrayOfEndPos.get(0) + 2);
                }

                arrOfPar = simplifyKBSent(leftParToSend, rightParToSend, mainSym);

                ArrayList returnList = null;
                if ((int) arrayOfEndPos.get(0) != leftPar.length() - 1) {
                    returnList = new ArrayList();
                    for (Object str : arrOfPar) {
                        String line = (String) str;

                        String sym = leftPar.substring((int) arrayOfEndPos.get(0) + 1, (int) arrayOfEndPos.get(0) + 2);
                        if (sym.equals("|")) {
                            if (rightPar.equals("")) {
                                ArrayList arrOfPar2 = simplifyKBSent(leftPar.substring((int) arrayOfEndPos.get(0) + 2, leftPar.length()), "", "|");
                                for (Object str2 : arrOfPar2) {
                                    String par2 = (String)str2;
                                    returnList.add(line + "|" + par2);
                                }
                                //returnList.add(line + "|" + arrOfPar.get(0));
                            } else {
                                ArrayList arrOfPar2 = simplifyKBSent(rightParToSend, "", "|");
                                for (Object str2 : arrOfPar2) {
                                    String par2 = (String)str2;
                                    returnList.add(line + "|" + par2);
                                }
                                //returnList.add(line + "|" + arrOfPar.get(0));
                            }
                        } else {
                            returnList.add(line);
                        }
                    }
                } else {
                    returnList = arrOfPar;
                }
                return returnList;

            } else {
                ArrayList returnList = new ArrayList();

                if (!leftPar.contains("|(")) {
                    returnList.add(leftPar);
                } else {
                    int indexOfLeftPar = leftPar.indexOf("|(");
                    String leftOfOR = leftPar.substring(0, indexOfLeftPar);
                    //returnList.add(leftOfOR);

                    String rightOfOR = leftPar.substring(indexOfLeftPar + 1);
                    arrOfPar = simplifyKBSent(rightOfOR, "", "|");

                    for (Object str3 : arrOfPar) {
                        String line = (String) str3;
                        returnList.add(leftOfOR + "|" + line);
                    }

                }

                return returnList;
            }

        } else {
            ArrayList returnList = new ArrayList();
            returnList.add(leftPar);
            return returnList;
        }

        arrOfPar = new ArrayList();
        arrOfPar.add(leftPar);

        if (!rightPar.contains("&")) {
            arrOfPar.add(rightPar);
        } else {
            String leftParToSend = rightPar;
            String rightParToSend = leftPar;
            arrOfPar = simplifyKBSent(leftParToSend, rightParToSend, symWithPar);
            ArrayList returnList = null;

            returnList = new ArrayList();
            for (Object str : arrOfPar) {
                String line = (String) str;

                String sym = symWithPar;
                if (sym.equals("|")) {
                    returnList.add(line + "|" + rightParToSend);
                } else {
                    returnList.add(line);
                }
            }

            if (symWithPar.equals("&")) {
                returnList.add(rightParToSend);
            }

            returnList.add("Reversed");
            return returnList;
        }

        return arrOfPar;
    }

    private String formulateCNFSent(double currNode, String parentSym) {
        Map currNodeMap = (HashMap) parsedClauseMap.get(currNode);

        Iterator it = currNodeMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();

            String currSym = (String) pair.getKey();
            ArrayList currNodeList = (ArrayList) pair.getValue();

            String sentCNF = "";
            for (int i = 0; i < currNodeList.size(); i++) {
                if (currNodeList.get(i) instanceof String) {
                    if (currSym.equals("~")) {
                        return currSym + (String) currNodeList.get(i);
                    }

                    if (i == 0) {
                        //if ((currSym.equals(parentSym)) || parentSym.equals("")) {
                        //    sentCNF += (String) currNodeList.get(i) + currSym;
                        //} else {
                        sentCNF += "(" + (String) currNodeList.get(i) + currSym;
                        //}
                    }
                    if (i == 1) {
                        //if (currSym.equals(parentSym) || parentSym.equals("")) {
                        //    sentCNF += (String) currNodeList.get(i);
                        //} else {
                        sentCNF += (String) currNodeList.get(i) + ")";
                        //}
                    }

                    if (i == 1) {
                        return sentCNF;
                    }

                } else {
                    String sentCreated = formulateCNFSent((double) currNodeList.get(i), currSym);

                    Map childMap = (HashMap) parsedClauseMap.get(currNodeList.get(i));
                    String childSym = "";
                    Iterator itChild = childMap.entrySet().iterator();
                    while (itChild.hasNext()) {
                        Map.Entry pairChild = (Map.Entry) itChild.next();
                        childSym = (String) pairChild.getKey();
                    }

                    if (i == 0) {
                        //if ((currSym.equals(parentSym)) || parentSym.equals("")) {
                        //    sentCNF += sentCreated + currSym;
                        //} else {
                        sentCNF += "(" + sentCreated + currSym;
                        //}
                    }
                    if (i == 1) {
                        //if ((currSym.equals(parentSym)) || parentSym.equals("")) {
                        //    sentCNF += sentCreated;
                        //} else {
                        sentCNF += sentCreated + ")";
                        //}
                    }

                    if (i == 1) {
                        //if(currSym.equals(childSym))
                        return sentCNF;
                        //else
                        //    return "("+sentCNF+")";
                    }
                }
            }

        }

        return "";
    }

    private void readInputFile(BufferedReader input) throws IOException {
        String line = "";
        int count = 1;
        while ((line = input.readLine()) != null) {
            if ((line.isEmpty() || line.trim().equals("") || line.trim().equals("\n"))) {
                continue;
            }

            line = line.replaceAll("\\s+", "");
            if (count == 1) {
                noOfClauseToProve = Integer.parseInt(line);
                createCTPDatabase(input, noOfClauseToProve);
            }

            if (count == 2) {
                noInKB = Integer.parseInt(line);
                createKBWithParsedInput(input);
            }
            count++;
        }
    }

    private void createCTPDatabase(BufferedReader input, int noOfClauseToProve) throws IOException {
        String line = "";
        int count = 0;
        while (count < noOfClauseToProve && (line = input.readLine()) != null) {
            if ((line.isEmpty() || line.trim().equals("") || line.trim().equals("\n"))) {
                continue;
            }
            line = line.replaceAll("\\s+", "");

            if (line.charAt(0) == '~') {
                line = line.substring(1, line.length());
            } else {
                line = "~" + line;
            }

            databaseCTP.add(line);
            count++;
        }

        //System.out.println(databaseCTP);
        //System.out.println();
    }

    private void createKBWithParsedInput(BufferedReader input) throws IOException {
        String line = "";
        int count = 0;
        while ((line = input.readLine()) != null && count < noInKB) {
            if ((line.isEmpty() || line.trim().equals("") || line.trim().equals("\n"))) {
                continue;
            }
            line = line.replaceAll("\\s+", "");

            parseInput(line);

            Iterator it = newCreatedRulesWithCNF.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                parsedClauseMap.put(pair.getKey(), pair.getValue());
            }

            Map clonedClauseMap = new HashMap(parsedClauseMap);
            KBMap.put(line, clonedClauseMap);

            parsedClauseMap.clear();
            newCreatedRulesWithCNF.clear();

            count++;
        }

        //printMap(KBMap);
        //System.out.println();
    }

    private void parseInput(String line) {
        line = line.replaceAll("\\s+", "");

        int level = 0;
        double returnVal = parseInputData(line, level);

        //    printMap(parsedClauseMap);
        //    System.out.println("");
        //    System.out.println("");            
        int iterationComp = -3;
        do {
            removeValuesFromMap();
            iterationComp = convertToCNF();
        } while (iterationComp != -1);

        //     printMap(parsedClauseMap);
        //System.out.println("");
        //     printMap(newCreatedRulesWithCNF);
        //     System.out.println("");
        //     System.out.println("");
        do {
            removeValuesFromMap();
            iterationComp = applyDeMorgans();
        } while (iterationComp != -1);

        //     printMap(parsedClauseMap);
        //System.out.println("");
        //     printMap(newCreatedRulesWithCNF);
    }

    public void printMap(Map mp) {
        Iterator it = mp.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            System.out.println(pair.getKey() + " = " + pair.getValue());
        }
    }

    private void removeValuesFromMap() {
        for (Object i : clearValuesFromMap) {
            double index = (double) i;
            parsedClauseMap.remove(index);
        }
    }

    private int applyDeMorgans() {
        clearValuesFromMap.clear();
        Iterator it = parsedClauseMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            //System.out.println(pair.getKey() + " = " + pair.getValue());

            double currNode = (double) pair.getKey();

            Map currNodeMap = (HashMap) pair.getValue();

            Iterator itNodeChild = currNodeMap.entrySet().iterator();
            while (itNodeChild.hasNext()) {
                Map.Entry pairCurrNodeChildren = (Map.Entry) itNodeChild.next();

                if (pairCurrNodeChildren.getKey() == "~") {
                    String currNodeSym = (String) pairCurrNodeChildren.getKey();
                    ArrayList currNodeList = (ArrayList) pairCurrNodeChildren.getValue();

                    if (currNodeList.get(0) instanceof String) {
                        continue;
                    } else {
                        double nodeInNeg = (double) currNodeList.get(0);
                        int returnVal = applyNegation(nodeInNeg);

                        if (currNode != (double) 1.0) {
                            /*   Map.Entry pairParentToCurrNode = (Map.Entry)it.next();                        
                            //double parentNode = (double)pairParentToCurrNode.getKey();
                            //Map parentMap = (HashMap)pairParentToCurrNode.getValue();
                            //Iterator itNodeParent = parentMap.entrySet().iterator();
                            while (itNodeParent.hasNext()) {
                               Map.Entry pairParent = (Map.Entry)itNodeParent.next();

                               String parentSym = (String)pairParent.getKey();
                               ArrayList parentList = (ArrayList)pairParent.getValue();
                               int indexInList = parentList.indexOf(currNode);
                               parentList.set(indexInList, nodeInNeg);

                               Map newParentPredMap = new HashMap();
                               newParentPredMap.put(parentSym, parentList);
                               parsedClauseMap.put(parentNode, newParentPredMap);

                               clearValuesFromMap.add(currNode);
                            }*/

                            Iterator itfindNode = parsedClauseMap.entrySet().iterator();
                            while (itfindNode.hasNext()) {
                                Map.Entry pairFindNode = (Map.Entry) itfindNode.next();
                                double parentNode = (double) pairFindNode.getKey();
                                Map parentMap = (HashMap) pairFindNode.getValue();

                                Iterator itNodeParent = parentMap.entrySet().iterator();
                                while (itNodeParent.hasNext()) {
                                    Map.Entry pairParent = (Map.Entry) itNodeParent.next();

                                    String parentSym = (String) pairParent.getKey();
                                    ArrayList parentList = (ArrayList) pairParent.getValue();

                                    if (parentList.contains(currNode)) {
                                        int indexInList = parentList.indexOf(currNode);
                                        parentList.set(indexInList, nodeInNeg);

                                        Map newParentPredMap = new HashMap();
                                        newParentPredMap.put(parentSym, parentList);
                                        parsedClauseMap.put(parentNode, newParentPredMap);
                                        clearValuesFromMap.add(currNode);
                                    }
                                }
                            }

                        } else {
                            //clearValuesFromMap.add(currNode);
                            Map childMap = (HashMap) parsedClauseMap.get(nodeInNeg);
                            parsedClauseMap.put(currNode, childMap);
                            clearValuesFromMap.add(nodeInNeg);
                        }

                        //break;
                        return 0;
                    }
                }
            }
        }

        return -1;
    }

    private int applyNegation(double currNode) {
        Map predMap = null;
        if (parsedClauseMap.containsKey(currNode)) {
            predMap = (HashMap) parsedClauseMap.get(currNode);
        }

        if (newCreatedRulesWithCNF.containsKey(currNode)) {
            predMap = (HashMap) newCreatedRulesWithCNF.get(currNode);
        }

        Iterator it = predMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();

            String currSym = (String) pair.getKey();
            ArrayList currNodeList = (ArrayList) pair.getValue();

            for (int i = 0; i < currNodeList.size(); i++) {
                if (currNodeList.get(i) instanceof String) {

                    if (currSym.equals("~")) {
                        return 0;
                    }

                    ArrayList newChildListForNeg = new ArrayList();
                    newChildListForNeg.add(currNodeList.get(i));

                    Map newChilMapForNeg = new HashMap();
                    newChilMapForNeg.put("~", newChildListForNeg);

                    newCreatedRulesWithCNF.put((double) (currNode + 0.001 * (i + 1)), newChilMapForNeg);

                    currNodeList.set(i, (double) (currNode + 0.001 * (i + 1)));

                } else {
                    double nextNode = (double) currNodeList.get(i);

                    int returnVal = applyNegation(nextNode);

                    //When ~
                    if (returnVal == 0) {
                        Map nextNodePredMap = null;
                        if (parsedClauseMap.containsKey(nextNode)) {
                            nextNodePredMap = (HashMap) parsedClauseMap.get(nextNode);
                            clearValuesFromMap.add(nextNode);
                        }

                        if (newCreatedRulesWithCNF.containsKey(nextNode)) {
                            nextNodePredMap = (HashMap) newCreatedRulesWithCNF.get(nextNode);
                            newCreatedRulesWithCNF.remove(nextNode);
                        }

                        Iterator itNextNode = nextNodePredMap.entrySet().iterator();
                        while (itNextNode.hasNext()) {
                            Map.Entry pairNextNode = (Map.Entry) itNextNode.next();

                            ArrayList nextNodeList = (ArrayList) pairNextNode.getValue();
                            String valueToBeTrans = (String) nextNodeList.get(0);

                            currNodeList.set(i, valueToBeTrans);

                        }
                    }

                }
            }

            Map newPredMap = new HashMap();

            if (currSym.equals("&")) {
                newPredMap.put("|", currNodeList);
            }

            if (currSym.equals("|")) {
                newPredMap.put("&", currNodeList);
            }

            parsedClauseMap.put(currNode, newPredMap);

        }

        return -1;
    }

    private int convertToCNF() {
        clearValuesFromMap.clear();
        Iterator it = parsedClauseMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            //System.out.println(pair.getKey() + " = " + pair.getValue());

            double currNodeParent = (double) pair.getKey();

            Map currNodeMap = (HashMap) pair.getValue();

            Iterator itNodeChild = currNodeMap.entrySet().iterator();
            while (itNodeChild.hasNext()) {
                Map.Entry pairCurrNodeChildren = (Map.Entry) itNodeChild.next();

                if (pairCurrNodeChildren.getKey() == "=>") {
                    String currNodeSymParent = (String) pairCurrNodeChildren.getKey();
                    ArrayList currNodeListParent = (ArrayList) pairCurrNodeChildren.getValue();

                    if (currNodeListParent.get(0) instanceof String) {
                        ArrayList newChildListForNeg = new ArrayList();
                        newChildListForNeg.add(currNodeListParent.get(0));

                        Map newChilMapForNeg = new HashMap();
                        newChilMapForNeg.put("~", newChildListForNeg);

                        newCreatedRulesWithCNF.put((double) (currNodeParent + 0.001), newChilMapForNeg);

                        currNodeListParent.set(0, (double) (currNodeParent + 0.001));
                        Map newPredMap = new HashMap();
                        newPredMap.put("|", currNodeListParent);

                        parsedClauseMap.put(currNodeParent, newPredMap);
                    } else {
                        Map newPredMap = new HashMap();
                        newPredMap.put("|", currNodeListParent);

                        parsedClauseMap.put(currNodeParent, newPredMap);

                        Map nextMap = (HashMap) parsedClauseMap.get(currNodeListParent.get(0));
                        Iterator itNext = nextMap.entrySet().iterator();
                        while (itNext.hasNext()) {
                            Map.Entry pairNext = (Map.Entry) itNext.next();

                            double currNode = (double) currNodeListParent.get(0);
                            String currNodeSym = (String) pairNext.getKey();
                            ArrayList currNodeList = (ArrayList) pairNext.getValue();

                            double returnVal = (double) applyConvCNF(currNode, currNodeSym, currNodeList);

                            if (Double.compare(returnVal, (double) (0.01)) == 0) {
                                if (currNodeList.get(0) instanceof Double) {
                                    double nextNode = (double) currNodeList.get(0);

                                    currNodeListParent.set(0, nextNode);
                                    Map newPredMap2 = new HashMap();
                                    newPredMap2.put("|", currNodeListParent);

                                    parsedClauseMap.put(currNodeParent, newPredMap2);

                                    if (parsedClauseMap.containsKey(currNode)) {
                                        clearValuesFromMap.add(currNode);
                                    }

                                    if (newCreatedRulesWithCNF.containsKey(currNode)) {
                                        newCreatedRulesWithCNF.remove(currNode);
                                    }

                                    /*if (parsedClauseMap.containsKey(nextNode)) {
                                        Map childNodeMap = (HashMap) parsedClauseMap.get(nextNode);

                                        Iterator itChild = childNodeMap.entrySet().iterator();
                                        while (itChild.hasNext()) {
                                            Map.Entry pairNextNodeChildren = (Map.Entry) itChild.next();

                                            String childNextNodeKey = (String) pairNextNodeChildren.getKey();
                                            ArrayList childNextNodeList = (ArrayList) pairNextNodeChildren.getValue();

                                            if (childNextNodeList.get(0) instanceof String) {
                                                String node = (String) childNextNodeList.get(0);

                                                currNodeList.set(0, node);
                                                Map newMap = new HashMap();
                                                if (currNodeSym.equals("&")) {
                                                    newMap.put("|", currNodeList);
                                                }

                                                if (currNodeSym.equals("|")) {
                                                    newMap.put("&", currNodeList);
                                                }

                                                parsedClauseMap.put(currNode, newMap);

                                                if (parsedClauseMap.containsKey(nextNode)) {
                                                    //parsedClauseMap.remove(nextNode);
                                                    clearValuesFromMap.add(nextNode);

                                                }

                                                if (newCreatedRulesWithCNF.containsKey(nextNode)) {
                                                    newCreatedRulesWithCNF.remove(nextNode);
                                                }
                                            }

                                            if (childNextNodeList.get(0) instanceof Double) {
                                                double nextNodeChild = (double) childNextNodeList.get(0);

                                                currNodeList.set(0, nextNodeChild);
                                                Map newMap = new HashMap();

                                                if (currNodeSym.equals("&")) {
                                                    newMap.put("|", currNodeList);
                                                }
                                                if (currNodeSym.equals("|")) {
                                                    newMap.put("&", currNodeList);
                                                }

                                                parsedClauseMap.put(currNode, newMap);
                                                if (parsedClauseMap.containsKey(nextNode)) {
                                                    //parsedClauseMap.remove(nextNodeChild);

                                                    clearValuesFromMap.add(nextNode);
                                                }

                                                if (newCreatedRulesWithCNF.containsKey(nextNode)) {
                                                    newCreatedRulesWithCNF.remove(nextNode);
                                                }

                                            }

                                        }
                                    }*/
                                } else {
                                    String nextNode = (String) currNodeList.get(0);

                                    currNodeListParent.set(0, nextNode);
                                    Map newPredMap2 = new HashMap();
                                    newPredMap2.put("|", currNodeListParent);

                                    parsedClauseMap.put(currNodeParent, newPredMap2);

                                    if (parsedClauseMap.containsKey(currNode)) {
                                        clearValuesFromMap.add(currNode);
                                    }

                                    if (newCreatedRulesWithCNF.containsKey(currNode)) {
                                        newCreatedRulesWithCNF.remove(currNode);
                                    }

                                    // }
                                }

                            }

                            return 0;
                        }
                    }
                }
            }
        }

        return -1;
    }

    private double applyConvCNF(double currNode, String currNodeSym, ArrayList currNodeList) {
        //2.0 = {=>=[A(x), B(y)]}   Modify only A(x)       
        if (currNodeSym.equals("~")) {
            return (double) (0.01);
        }

        for (int i = 0; i < currNodeList.size(); i++) {
            boolean isDouble = false;
            if (currNodeList.get(i) instanceof Double) {
                double nextNode = (double) currNodeList.get(i);

                if (parsedClauseMap.containsKey(nextNode)) {
                    Map currNodeMap = (HashMap) parsedClauseMap.get(nextNode);
                    Iterator itNodeChild = currNodeMap.entrySet().iterator();
                    while (itNodeChild.hasNext()) {
                        Map.Entry pairCurrNodeChildren = (Map.Entry) itNodeChild.next();

                        String childSym = (String) pairCurrNodeChildren.getKey();
                        ArrayList childList = (ArrayList) pairCurrNodeChildren.getValue();

                        double returnVal = (double) applyConvCNF(nextNode, childSym, childList);

                        if (currNodeList.size() == 1) {
                            return (double) (0.01);
                        }

                        if (Double.compare(returnVal, (double) (0.01)) == 0) {
                            if (parsedClauseMap.containsKey(nextNode)) {
                                Map childNodeMap = (HashMap) parsedClauseMap.get(nextNode);

                                Iterator itChild = childNodeMap.entrySet().iterator();
                                while (itChild.hasNext()) {
                                    Map.Entry pairNextNodeChildren = (Map.Entry) itChild.next();

                                    String childNextNodeKey = (String) pairNextNodeChildren.getKey();
                                    ArrayList childNextNodeList = (ArrayList) pairNextNodeChildren.getValue();

                                    if (childNextNodeList.get(0) instanceof String) {
                                        String node = (String) childNextNodeList.get(0);

                                        currNodeList.set(i, node);
                                        Map newMap = new HashMap();
                                        if (currNodeSym.equals("&")) {
                                            newMap.put("|", currNodeList);
                                        }

                                        if (currNodeSym.equals("|")) {
                                            newMap.put("&", currNodeList);
                                        }

                                        parsedClauseMap.put(currNode, newMap);

                                        if (parsedClauseMap.containsKey(nextNode)) {
                                            //parsedClauseMap.remove(nextNode);
                                            clearValuesFromMap.add(nextNode);

                                        }

                                        if (newCreatedRulesWithCNF.containsKey(nextNode)) {
                                            newCreatedRulesWithCNF.remove(nextNode);
                                        }
                                    }

                                    if (childNextNodeList.get(0) instanceof Double) {
                                        double nextNodeChild = (double) childNextNodeList.get(0);

                                        currNodeList.set(i, nextNodeChild);
                                        Map newMap = new HashMap();

                                        if (currNodeSym.equals("&")) {
                                            newMap.put("|", currNodeList);
                                        }
                                        if (currNodeSym.equals("|")) {
                                            newMap.put("&", currNodeList);
                                        }

                                        parsedClauseMap.put(currNode, newMap);
                                        if (parsedClauseMap.containsKey(nextNode)) {
                                            //parsedClauseMap.remove(nextNodeChild);

                                            clearValuesFromMap.add(nextNode);
                                        }

                                        if (newCreatedRulesWithCNF.containsKey(nextNode)) {
                                            newCreatedRulesWithCNF.remove(nextNode);
                                        }

                                    }

                                }
                            }

                        } else {
                            /*
                           double v = (returnVal-nextNode);
                           double roundOff = (double)(Math.round(v * 1000.0) / 1000.0);
                           double val = (double)(roundOff/0.001);
                          // double val = (double)(/0.001);
                           if(Double.compare((double)(val), (double)(2)) == 0 )
                           {
                               childList.set(1, returnVal);
                           }
                           else
                           {
                               childList.set(0, returnVal);
                           }
                           
                            Map newPredMap = new HashMap();



                            if(childSym.equals("&"))
                                newPredMap.put("|", childList);

                             if(childSym.equals("|"))
                                newPredMap.put("&", childList);

                            parsedClauseMap.put(nextNode, newPredMap);
                             */
                        }

                    }
                }

                if (newCreatedRulesWithCNF.containsKey(nextNode)) {
                    Map currNodeMap = (HashMap) newCreatedRulesWithCNF.get(nextNode);
                    Iterator itNodeChild = currNodeMap.entrySet().iterator();
                    while (itNodeChild.hasNext()) {
                        Map.Entry pairCurrNodeChildren = (Map.Entry) itNodeChild.next();

                        String childSym = (String) pairCurrNodeChildren.getKey();
                        ArrayList childList = (ArrayList) pairCurrNodeChildren.getValue();

                        double returnVal = (double) applyConvCNF(nextNode, childSym, childList);

                        if (currNodeList.size() == 1) {
                            return (double) (0.01);
                        }

                        if (Double.compare(returnVal, (double) (0.01)) == 0) {
                            if (newCreatedRulesWithCNF.containsKey(nextNode)) {
                                Map childNodeMap = (HashMap) newCreatedRulesWithCNF.get(nextNode);

                                Iterator itChild = childNodeMap.entrySet().iterator();
                                while (itChild.hasNext()) {
                                    Map.Entry pairNextNodeChildren = (Map.Entry) itChild.next();

                                    String childNextNodeKey = (String) pairNextNodeChildren.getKey();
                                    ArrayList childNextNodeList = (ArrayList) pairNextNodeChildren.getValue();

                                    if (childNextNodeList.get(0) instanceof String) {
                                        String node = (String) childNextNodeList.get(0);

                                        currNodeList.set(i, node);
                                        Map newMap = new HashMap();
                                        if (currNodeSym.equals("&")) {
                                            newMap.put("|", currNodeList);
                                        }

                                        if (currNodeSym.equals("|")) {
                                            newMap.put("&", currNodeList);
                                        }

                                        parsedClauseMap.put(currNode, newMap);

                                        if (parsedClauseMap.containsKey(nextNode)) {
                                            //parsedClauseMap.remove(nextNode);
                                            clearValuesFromMap.add(nextNode);
                                        }

                                        if (newCreatedRulesWithCNF.containsKey(nextNode)) {
                                            newCreatedRulesWithCNF.remove(nextNode);
                                        }
                                    }

                                    if (childNextNodeList.get(0) instanceof Double) {
                                        double nextNodeChild = (double) childNextNodeList.get(0);

                                        currNodeList.set(i, nextNodeChild);
                                        Map newMap = new HashMap();

                                        if (currNodeSym.equals("&")) {
                                            newMap.put("|", currNodeList);
                                        }
                                        if (currNodeSym.equals("|")) {
                                            newMap.put("&", currNodeList);
                                        }

                                        parsedClauseMap.put(currNode, newMap);
                                        if (parsedClauseMap.containsKey(nextNode)) {
                                            //parsedClauseMap.remove(nextNodeChild);

                                            clearValuesFromMap.add(nextNode);
                                        }

                                        if (newCreatedRulesWithCNF.containsKey(nextNode)) {
                                            newCreatedRulesWithCNF.remove(nextNode);
                                        }

                                    }

                                }
                            }

                        } else {
                            /*   double v = (returnVal-nextNode);
                           double roundOff = (double)(Math.round(v * 1000.0) / 1000.0);
                           double val = (double)(roundOff/0.001);
                         
                           if(Double.compare((double)(val), (double)(2)) == 0 )
                           {
                               childList.set(1, returnVal);
                           }
                           else
                           {
                               childList.set(0, returnVal);
                           }
                           
                            Map newPredMap = new HashMap();



                            if(childSym.equals("&"))
                                newPredMap.put("|", childList);

                             if(childSym.equals("|"))
                                newPredMap.put("&", childList);

                            parsedClauseMap.put(nextNode, newPredMap);
                             */
                        }

                    }
                }

                isDouble = true;

            }

            if (currNodeList.get(i) instanceof String) {
                if (isDouble) {
                    continue;
                }

                if (currNodeList.size() == 1) {
                    return (double) (0.01);
                }
                ArrayList newChildListForNeg = new ArrayList();
                newChildListForNeg.add(currNodeList.get(i));

                Map newChilMapForNeg = new HashMap();
                newChilMapForNeg.put("~", newChildListForNeg);

                newCreatedRulesWithCNF.put(currNode + 0.001 * (i + 1), newChilMapForNeg);

                //return ((double)(currNode+0.001*(i+1)));
                double returnVal = (double) (currNode + 0.001 * (i + 1));

                double v = (returnVal - currNode);
                double roundOff = (double) (Math.round(v * 1000.0) / 1000.0);
                double val = (double) (roundOff / 0.001);
                // double val = (double)(/0.001);
                if (Double.compare((double) (val), (double) (2)) == 0) {
                    currNodeList.set(1, returnVal);
                } else {
                    currNodeList.set(0, returnVal);
                }

                Map newPredMap = new HashMap();

                if (currNodeSym.equals("&")) {
                    newPredMap.put("|", currNodeList);
                }

                if (currNodeSym.equals("|")) {
                    newPredMap.put("&", currNodeList);
                }

                parsedClauseMap.put(currNode, newPredMap);

            }

        }

        return -1;

    }

    private double parseInputData(String line, double level) {
        int pairofPar = -1;
        ArrayList arrayOfStartPos = new ArrayList();
        ArrayList arrayOfEndPos = new ArrayList();

        if (line.charAt(0) == '(') {
            for (int i = 0; i < line.length(); i++) {
                char c = line.charAt(i);
                if (c == '(') {
                    pairofPar++;
                    if (pairofPar == 0) {
                        arrayOfStartPos.add(i);
                    }
                }

                if (c == ')') {
                    pairofPar--;

                    if (pairofPar == -1) {
                        arrayOfEndPos.add(i);
                    }
                }

            }

            double childLevelLeftPred = parseInputData(line.substring((int) arrayOfStartPos.get(0) + 1, (int) arrayOfEndPos.get(0)), level + 1);

            if ((int) arrayOfEndPos.get(0) != line.length() - 1) {

                String nextLn = line.substring((int) arrayOfEndPos.get(0) + 1, line.length());

                if (nextLn.charAt(0) == '&') {
                    int index = nextLn.indexOf("&");

                    ArrayList predList = new ArrayList();
                    Map predMap = new HashMap();
                    //System.out.println(line.substring(0,(int)index));
                    //System.out.println(line.substring((int)index+1));
                    predList.add(childLevelLeftPred);

                    double returnVal = parseInputData(nextLn.substring((int) index + 1), (double) (level + 0.01));
                    if (returnVal == -1) {
                        predList.add(nextLn.substring((int) index + 1));
                    } else {
                        predList.add(returnVal);
                    }

                    predMap.put("&", predList);
                    //System.out.println("Hello1");
                    parsedClauseMap.put(level, predMap);

                    return level;
                }

                if (nextLn.charAt(0) == '|') {
                    int index = nextLn.indexOf("|");

                    ArrayList predList = new ArrayList();
                    Map predMap = new HashMap();
                    //System.out.println(line.substring(0,(int)index));
                    //System.out.println(line.substring((int)index+1));
                    predList.add(childLevelLeftPred);
                    //predList.add(nextLn.substring((int)index+1));

                    double returnVal = parseInputData(nextLn.substring((int) index + 1), (double) (level + 0.01));
                    if (returnVal == -1) {
                        predList.add(nextLn.substring((int) index + 1));
                    } else {
                        predList.add(returnVal);
                    }

                    predMap.put("|", predList);
                    parsedClauseMap.put(level, predMap);

                    return level;
                }

                if (nextLn.charAt(0) == '=') {
                    int index = nextLn.indexOf("=>");

                    ArrayList predList = new ArrayList();
                    Map predMap = new HashMap();
                    //System.out.println(line.substring(0,(int)index));
                    //System.out.println(line.substring((int)index+1));
                    predList.add(childLevelLeftPred);
                    //predList.add(nextLn.substring((int)index+2));

                    double returnVal = parseInputData(nextLn.substring((int) index + 2), (double) (level + 0.01));
                    if (returnVal == -1) {
                        predList.add(nextLn.substring((int) index + 2));
                    } else {
                        predList.add(returnVal);
                    }

                    predMap.put("=>", predList);
                    parsedClauseMap.put(level, predMap);

                    return level;
                }
            }

            if (line.contains("~")) {
                return childLevelLeftPred;
            }

            if (childLevelLeftPred != -1) {
                return childLevelLeftPred;
            }
        } else {

            if (line.charAt(0) == '~') {
                //int index = line.indexOf("~");
                ArrayList predList = new ArrayList();
                Map predMap = new HashMap();

                //predList.add(line.substring(0));
                double returnVal = parseInputData(line.substring(1), level);
                if (returnVal == -1) {
                    predList.add(line.substring(1, line.length()));
                } else {
                    predList.add(returnVal);
                }

                predMap.put("~", predList);
                // System.out.println("Hello2");
                parsedClauseMap.put(level, predMap);

                return level;
            }

            if ((line.contains("&") && line.contains("|") && line.contains("=>") && (line.indexOf("&") < line.indexOf("|")) && line.indexOf("&") < line.indexOf("=>"))
                    || (line.contains("&") && line.contains("|") && !line.contains("=>") && (line.indexOf("&") < line.indexOf("|")))
                    || (line.contains("&") && line.contains("=>") && !line.contains("|") && (line.indexOf("&") < line.indexOf("=>")))
                    || (line.contains("&") && !line.contains("|") && !line.contains("=>"))) {

                int index = line.indexOf("&");

                ArrayList predList = new ArrayList();
                Map predMap = new HashMap();

                predList.add(line.substring(0, (int) index));

                double returnVal = parseInputData(line.substring((int) index + 1), level);
                if (returnVal == -1) {
                    predList.add(line.substring((int) index + 1));
                } else {
                    predList.add(returnVal);
                }

                predMap.put("&", predList);
                // System.out.println("Hello2");
                parsedClauseMap.put(level, predMap);

                return level;
            } else if ((line.contains("|") && line.contains("&") && line.contains("=>") && (line.indexOf("|") < line.indexOf("&")) && line.indexOf("|") < line.indexOf("=>"))
                    || (line.contains("|") && line.contains("&") && !line.contains("=>") && (line.indexOf("|") < line.indexOf("&")))
                    || (line.contains("|") && line.contains("=>") && !line.contains("&") && (line.indexOf("|") < line.indexOf("=>")))
                    || (line.contains("|") && !line.contains("&") && !line.contains("=>"))) {
                int index = line.indexOf("|");

                ArrayList predList = new ArrayList();
                Map predMap = new HashMap();
                //System.out.println(line.substring(0,(int)index));
                //System.out.println(line.substring((int)index+1));
                predList.add(line.substring(0, (int) index));
                //predList.add(line.substring((int)index+1));

                double returnVal = parseInputData(line.substring((int) index + 1), level);
                if (returnVal == -1) {
                    predList.add(line.substring((int) index + 1));
                } else {
                    predList.add(returnVal);
                }

                //System.out.println("Hello1 " + returnVal);
                predMap.put("|", predList);
                parsedClauseMap.put(level, predMap);

                return level;
            } else if ((line.contains("|") && line.contains("&") && line.contains("=>") && (line.indexOf("=>") < line.indexOf("&")) && line.indexOf("=>") < line.indexOf("|"))
                    || (line.contains("=>") && line.contains("&") && !line.contains("|") && (line.indexOf("=>") < line.indexOf("&")))
                    || (line.contains("|") && line.contains("=>") && !line.contains("&") && (line.indexOf("=>") < line.indexOf("|")))
                    || (line.contains("=>") && !line.contains("&") && !line.contains("|"))) {
                int index = line.indexOf("=>");

                ArrayList predList = new ArrayList();
                Map predMap = new HashMap();
                //System.out.println(line.substring(0,(int)index));
                //System.out.println(line.substring((int)index+1));
                predList.add(line.substring(0, (int) index));
                //predList.add(line.substring((int)index+2));

                double returnVal = parseInputData(line.substring((int) index + 2), level);
                if (returnVal == -1) {
                    predList.add(line.substring((int) index + 2));
                } else {
                    predList.add(returnVal);
                }

                predMap.put("=>", predList);
                parsedClauseMap.put(level, predMap);

                return level;
            } else if (line.contains("~")) {
                int index = line.indexOf("~");
                ArrayList predList = new ArrayList();
                Map predMap = new HashMap();
                //System.out.println(line.substring(0,(int)index));
                //System.out.println(line.substring((int)index+1));
                //predList.add(childLevelLeftPred);
                //predList.add(line.substring((int)index+1));

                double returnVal = parseInputData(line.substring((int) index + 1), level);

                //System.out.println("Hello2 " + returnVal);
                if (returnVal == -1) {
                    predList.add(line.substring((int) index + 1));
                } else {
                    predList.add(returnVal);
                }

                predMap.put("~", predList);
                parsedClauseMap.put(level, predMap);

                return level;
            } else {
                return -1;
            }

        }
        return -1;
    }

    public static void main(String[] args) throws FileNotFoundException, IOException {

        BufferedReader input = null;
        BufferedWriter output = null;
        TellMeWho ob = new TellMeWho();
        try {
            input = new BufferedReader(new FileReader("input.txt"));
            ob.readInputFile(input);

        } finally {
            input.close();
        }

        ob.createKB();

        ob.resolveCTP();

        try {
            output = new BufferedWriter(new FileWriter("output.txt"));

            for (Object str : ob.databaseCTP) {
                String query = (String) str;
                String answer = String.valueOf((Boolean) ob.outputMap.get(query));
                output.write(answer.toUpperCase());
                output.write("\n");
            }

            //output.write("Hello");
        } finally {
            output.close();
        }
    }

}
