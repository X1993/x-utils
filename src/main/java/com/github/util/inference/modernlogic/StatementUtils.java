package com.github.util.inference.modernlogic;

import com.github.util.inference.modernlogic.statement.*;
import com.github.util.structure.graph.Wdg;
import com.github.util.structure.graph.WdgImpl;
import org.apache.commons.lang3.StringUtils;
import java.util.*;
import java.util.stream.Collectors;
import static com.github.util.inference.modernlogic.ModernLogicConstant.Value.FALSE;
import static com.github.util.inference.modernlogic.ModernLogicConstant.Value.TRUE;

/**
 * @author wangjj7
 * @date 2025/11/25
 * @description
 */
public class StatementUtils {

    public static String formatDescription(Statement statement)
    {
        if (statement == null) {
            return "";
        }
        String formatDescription = statement.formatDescription();
        if (statement instanceof SimpleStatement || statement instanceof NegationStatement){
            return formatDescription;
        }
        return "(" + formatDescription + ")";
    }

    /**
     * 为复合陈述生成完备真值表
     * @param combinationStatement 复合陈述
     * @param fixedValueSimpleStatementSet 真值不可变的简单陈述集
     * @return
     */
    public static String[][] generateCTTM(CombinationStatement combinationStatement ,
                                          Set<SimpleStatement> fixedValueSimpleStatementSet)
    {
        List<SimpleStatement> simpleStatements = combinationStatement.getSimpleStatements();
        int simpleStatementCnt = simpleStatements.size();

        List<SimpleStatement> dynamicValueSimpleStatements = simpleStatements.stream()
                .filter(simpleStatement -> fixedValueSimpleStatementSet == null
                        || !fixedValueSimpleStatementSet.contains(simpleStatement))
                .collect(Collectors.toList());

        int dynamicValueSimpleStatementCnt = dynamicValueSimpleStatements.size();
        int rowCnt = (1 << dynamicValueSimpleStatementCnt) + 1;

        Wdg<Statement> statementWdg = generateWdg(combinationStatement);
        List<Statement> topologicalSortStatements = statementWdg.topologicalSort();
        Collections.reverse(topologicalSortStatements);

        int columnCnt = topologicalSortStatements.size();
        //数据结构初始化
        Object[][] cttmTable = new Object[rowCnt][columnCnt];
        Object[] cttmStatementRow = cttmTable[0];

        //填充简单陈述列头
        for (int i = 0; i < simpleStatementCnt; i++) {
            SimpleStatement simpleStatement = simpleStatements.get(i);
            cttmStatementRow[i] = simpleStatement;
        }

        //填充复合陈述列头
        int combinationStatementIndex = simpleStatementCnt;
        topologicalSortStatements.removeAll(simpleStatements);
        for (Statement statement : topologicalSortStatements) {
            cttmStatementRow[combinationStatementIndex++] = statement;
        }

        //填充所有真值组合
        int bitIndexes = (1 << dynamicValueSimpleStatementCnt) - 1;
        for (int i = 0; i <= bitIndexes; i++) {
            //真值陈述不可变的简单陈述

            //真值陈述可变的简单陈述
            for (int j = 0; j < dynamicValueSimpleStatementCnt; j++) {
                SimpleStatement dynamicValueSimpleStatement = dynamicValueSimpleStatements.get(j);
                dynamicValueSimpleStatement.setIsTrue((i & (1 << j)) > 0);
            }
            //计算各种陈述当前组合的真值
            Object[] cttmValueRow = cttmTable[i + 1];
            for (int j = 0; j < columnCnt; j++) {
                Statement statement = (Statement) cttmStatementRow[j];
                cttmValueRow[j] = statement.isTrue() ? TRUE.getSymbol() : FALSE.getSymbol();
            }
        }

        //陈述对象转标准文案
        for (int i = 0; i < cttmStatementRow.length; i++) {
            Statement statement = (Statement) cttmStatementRow[i];
            cttmStatementRow[i] = statement.formatDescription();
        }

        String[][] formatCttmTable = new String[cttmTable.length][cttmStatementRow.length];
        for (int i = 0; i < cttmTable.length; i++) {
            for (int j = 0; j < cttmTable[i].length; j++) {
                Object value = cttmTable[i][j];
                String valueString = "";
                if (value != null){
                    if (value instanceof String){
                        valueString = (String) value;
                    }
                }
                formatCttmTable[i][j] = valueString;
            }
        }

        return formatCttmTable;
    }

    /**
     * 为复合陈述生成完备真值表
     * @param combinationStatement 复合陈述
     * @return
     */
    public static String[][] generateCTTM(CombinationStatement combinationStatement)
    {
        return generateCTTM(combinationStatement ,Collections.EMPTY_SET);
    }

    /**
     * 是否是重言式
     * @return
     */
    public static boolean isTautologies(CombinationStatement statement)
    {
        String[][] cttmTable = generateCTTM(statement);
        int rowCnt = cttmTable.length;
        int columnCnt = cttmTable[0].length;
        for (int i = 1; i < rowCnt; i++) {
            if (cttmTable[i][columnCnt - 1].contains(FALSE.getSymbol())){
                return false;
            }
        }
        return true;
    }

    /**
     * 为该论证生成完备真值表
     * @param argumentation 论证
     * @param fixedValueSimpleStatementSet 真值不可变的简单陈述集
     * @return
     */
    public static String[][] generateCTTM(Argumentation argumentation ,Set<SimpleStatement> fixedValueSimpleStatementSet)
    {
        String[][] statementCTTM = generateCTTM(argumentation.equivalenceStatement() ,fixedValueSimpleStatementSet);
        int rowCnt = statementCTTM.length;
        int columnCnt = statementCTTM[0].length;
        String[][] argumentationCTTM = new String[rowCnt + 1][columnCnt];
        for (int i = 1; i < rowCnt; i++) {
            for (int j = 0; j < columnCnt; j++) {
                argumentationCTTM[i][j] = statementCTTM[i - 1][j];
            }
        }

        String[] typeRow = argumentationCTTM[0];
        String[] statementRow = argumentationCTTM[1];

        Map<String ,String> description2TypeMap = new HashMap<>();
        List<Statement> preconditions = argumentation.getPreconditions();
        for (int i = 0; i < preconditions.size(); i++) {
            Statement precondition = preconditions.get(i);
            description2TypeMap.put(precondition.formatDescription() ,"P" + (i + 1));
        }
        Statement conclusion = argumentation.getConclusion();
        description2TypeMap.put(conclusion.formatDescription() ,"∴");

        for (int i = 0; i < columnCnt; i++) {
            String description = statementRow[i];
            String type = description2TypeMap.get(description);
            if (StringUtils.isNotBlank(type)){
                typeRow[i] = type;
            }
        }

        return argumentationCTTM;
    }

    /**
     * 为该论证生成完备真值表
     * @param argumentation 论证
     * @return
     */
    public static String[][] generateCTTM(Argumentation argumentation)
    {
        return generateCTTM(argumentation ,Collections.EMPTY_SET);
    }

    /**
     * 为复合陈述生成结构真值表
     * @param argumentation 论证
     * @return
     */
    public static String[][] generateSTTT(Argumentation argumentation)
    {
        //todo
        return null;
    }

    private static Wdg<Statement> generateWdg(Statement statement)
    {
        Wdg<Statement> wdg = new WdgImpl<>();
        Set<Statement> waitForeachStatements = new HashSet<>();
        waitForeachStatements.add(statement);

        while (!waitForeachStatements.isEmpty()) {
            List<Statement> nextForeachStatements = new ArrayList<>();
            for (Statement waitForeachStatement : waitForeachStatements) {
                if (waitForeachStatement instanceof SimpleStatementImpl) {
                    wdg.addVertex(waitForeachStatement);
                }else if (waitForeachStatement instanceof CombinationStatement){
                    CombinationStatement combinationStatement = (CombinationStatement) waitForeachStatement;
                    List<Statement> branchStatements = combinationStatement.getBranchStatements();
                    for (Statement branchStatement : branchStatements) {
                        wdg.addEdge(waitForeachStatement ,branchStatement ,1);
                    }
                    nextForeachStatements.addAll(branchStatements);
                }
            }
            waitForeachStatements.clear();
            waitForeachStatements.addAll(nextForeachStatements);
        }

        return wdg;
    }

}
