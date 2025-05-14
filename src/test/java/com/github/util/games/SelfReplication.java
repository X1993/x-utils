package com.github.util.games;

import java.util.ArrayList;
import java.util.List;

public class SelfReplication {
    public static void main(String[] args) {
        List<String> codes = new ArrayList<>();
        codes.add("package com.github.util.games;");
        codes.add("import java.util.ArrayList;");
        codes.add("import java.util.List;");
        codes.add("public class SelfReplication {");
        codes.add("    public static void main(String[] args) {");
        codes.add("        List<String> codes = new ArrayList<>();");

        for (String code : codes){
            System.out.println(code);
        }
        for (String code : codes) {
            System.out.println("        codes.add(\"" + code2Txt(code) + "\");");
        }

        codes = new ArrayList<>();
        codes.add("        for (String code : codes) {");
        codes.add("            System.out.println(code");
        codes.add("        }");
        codes.add("        for (String code : codes) {");
        codes.add("            System.out.println(\"        codes.add(\\\"\" + code2Txt(code) + \"\\\");\");");
        codes.add("        }");
        codes.add("        codes = new ArrayList<>();");
        codes.add("        for (String code : codes) {");
        codes.add("            System.out.println(code");
        codes.add("        }");
        codes.add("        for (String code : codes) {");
        codes.add("            System.out.println(\"        codes.add(\\\"\" + code2Txt(code) + \"\\\");\");");
        codes.add("        }");
        codes.add("    }");
        codes.add("    private static String code2Txt(String s){\n" +
                "        StringBuilder sb = new StringBuilder();\n" +
                "        for (int i = 0; i < s.toCharArray().length; i++) {\n" +
                "            if (s.charAt(i) == '\\''){\n" +
                "                sb.append('\\'').append(s.charAt(i + 1)).append('\\'').append(s.charAt(i + 1));\n" +
                "                i++;\n" +
                "            }else {\n" +
                "                sb.append(s.charAt(i));\n" +
                "            }\n" +
                "        }\n" +
                "        return sb.toString();\n" +
                "    }");
        codes.add("}");

        for (String code : codes) {
            System.out.println(code);
        }
        for (String code : codes) {
            System.out.println("        codes.add(\"" + code2Txt(code) + "\");");
        }
    }

    private static String code2Txt(String s){
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.toCharArray().length; i++) {
            if (s.charAt(i) == '\''){
                sb.append('\'').append(s.charAt(i + 1)).append('\'').append(s.charAt(i + 1));
                i++;
            }else {
                sb.append(s.charAt(i));
            }
        }
        return sb.toString();
    }

}
