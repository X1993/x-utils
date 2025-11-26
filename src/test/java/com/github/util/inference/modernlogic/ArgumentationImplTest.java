package com.github.util.inference.modernlogic;

import com.github.util.inference.modernlogic.statement.*;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @author wangjj7
 * @date 2025/11/24
 * @description
 */
public class ArgumentationImplTest {

    @Test
    public void testIsValidFormByCTTM()
    {
        SimpleStatementImpl B = new SimpleStatementImpl("B");
        SimpleStatementImpl W = new SimpleStatementImpl("W");
        SimpleStatementImpl G = new SimpleStatementImpl("G");
        SimpleStatementImpl S = new SimpleStatementImpl("S");
        SimpleStatementImpl C = new SimpleStatementImpl("C");
        SimpleStatementImpl P = new SimpleStatementImpl("P");

        CombinationStatement B_if_W = new ImplicationStatementImpl(B, W);
        CombinationStatement no_S = new NegationStatementImpl(S);
        CombinationStatement G_if_no_S = new ImplicationStatementImpl(G, no_S);
        CombinationStatement B_if_W_and_G_if_no_S = new ConjunctionStatementImpl(B_if_W, G_if_no_S);
        CombinationStatement no_B = new NegationStatementImpl(B);
        CombinationStatement no_G = new NegationStatementImpl(G);
        CombinationStatement no_B_if_no_G = new ImplicationStatementImpl(no_B ,no_G);
        CombinationStatement C_and_P = new ConjunctionStatementImpl(C,P);
        CombinationStatement no_B_if_no_G_if_C_and_P = new ImplicationStatementImpl(no_B_if_no_G, C_and_P);
        CombinationStatement no_W = new NegationStatementImpl(W);

        CombinationStatement conclusion = new ImplicationStatementImpl(C, no_G);

        Argumentation argumentation = new ArgumentationImpl(conclusion ,B_if_W_and_G_if_no_S, no_B_if_no_G_if_C_and_P ,no_W ,P);
        System.out.println(argumentation.formatDescription());
        System.out.println("=======================================================");
        String[][] cttmTable = StatementUtils.generateCTTM(argumentation);
        printCTTMTable(cttmTable);
        System.out.println("=======================================================");
        Assert.assertFalse(StatementUtils.isTautologies(argumentation.equivalenceStatement()));
    }

    private void printCTTMTable(String[][] cttmTable)
    {
        int rowCnt = cttmTable.length;
        int columntCnt = cttmTable[0].length;
        int[] columnMaxLengthArray = new int[columntCnt];
        for (int i = 0; i < rowCnt; i++) {
            for (int j = 0; j < columntCnt; j++) {
                String value = cttmTable[i][j];
                if (StringUtils.isNotBlank(value)) {
                    columnMaxLengthArray[j] = Math.max(columnMaxLengthArray[j], value.length());
                }
            }
        }

        for (int i = 0; i < rowCnt; i++) {
            for (int j = 0; j < columntCnt; j++) {
                String value = cttmTable[i][j];
                value = value == null ? "" : value;
                cttmTable[i][j] = StringUtils.rightPad(value ,columnMaxLengthArray[j] ," ");
            }
            if (i > 1 && cttmTable[i][columntCnt - 1].contains("T")){
                continue;
            }
            System.out.println(Arrays.stream(cttmTable[i]).collect(Collectors.joining(" | ")));
        }
    }

    public void testIsValidFormBySTTT() {
    }
}